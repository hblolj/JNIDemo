package com.example.ori.jnidemo.fragments;


import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.ori.jnidemo.HomeActivity;
import com.example.ori.jnidemo.R;
import com.example.ori.jnidemo.bean.ActionMessageEvent;
import com.example.ori.jnidemo.bean.CategoryItem;
import com.example.ori.jnidemo.bean.FragmentMessageEvent;
import com.example.ori.jnidemo.bean.MessageEvent;
import com.example.ori.jnidemo.constant.ComConstant;
import com.example.ori.jnidemo.enums.ActionResultEnum;
import com.example.ori.jnidemo.enums.CategoryEnum;
import com.example.ori.jnidemo.enums.WeighTypeEnum;
import com.example.ori.jnidemo.utils.LanguageUtil;
import com.example.ori.jnidemo.utils.StringUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.OnClick;

import static com.example.ori.jnidemo.constant.ComConstant.getAddressCodeByCategoryItemId;

/**
 * 回收 Fragment
 */
public class RecycleFragment extends com.example.ori.jnidemo.base.Fragment {

    private static final String TAG = RecycleFragment.class.getSimpleName();

    public static final Integer FRAGMENT_ID = 2;

    // 塑料瓶回收门关闭时间默认设为 30 秒
    public static final Integer PLASTIC_BOTTLE_CLOSE_DOOR_COUNT_DOWN_TIME = 30000;

    public static final Integer PLASTIC_BOTTLE_FORCE_RECYCLE_COUNT_DOWN_TIME = 60000;

    public static final Integer COM_BACK_HOME_PAGE_COUNT_DOWN_TIME = 10000;

    private static String COMBACK_HOME_PAGE_NOTICE;

    // 金属、纸类回收门关闭时间默认设为 60 秒
    public static final Integer OTHER_CLOSE_DOOR_COUNT_DOWN_TIME = 60000;

    private Integer tempTimer;

    private CategoryItem mCurrentItem;

    @BindView(R.id.tv_count_down_time)
    TextView tvCountDownTime;

    @BindView(R.id.tv_recycle_notice)
    TextView tvRecycleNotice;

    @BindView(R.id.btn_recycle_complete)
    Button btnRecycleComplate;

    @BindView(R.id.count_down_time)
    LinearLayout llCountDownTime;

    @BindView(R.id.ll_recycle)
    LinearLayout llRecycle;

    @BindView(R.id.ll_brify_summary)
    LinearLayout llBrifySummary;

    @BindView(R.id.tv_curren_recycle_validate_count)
    TextView tvCurrenRecycleValidateCount;
    // 关门倒计时
    private CountDownTimer closeDoorCountDownTimer;
    // 强制回收倒计时
    private CountDownTimer forceRecycleCountDownTimer;
    // 返回首页倒计时
    private CountDownTimer comBackHomePageCountDownTimer;

    public RecycleFragment() {
        // Required empty public constructor
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.fragment_recycle;
    }

    @Override
    protected void initWidget(View root) {
        super.initWidget(root);
    }

    @Override
    protected void initData() {
        super.initData();

        // 初始化界面
        initView();

        String address = getAddressCodeByCategoryItemId(mCurrentItem.getItemId());
        if (StringUtil.isEmpty(address)){
            String notice;
            if (LanguageUtil.isChinese()){
                notice = "尚未支持的回收类型: " + mCurrentItem.getCategoryName();
            }else {
                notice = "Unrecognized recycling type: " + mCurrentItem.getCategoryName();
            }
            EventBus.getDefault().post(new MessageEvent(null, notice, MessageEvent.MESSAGE_TYPE_NOTICE));
            return;
        }

        // 按回收物类型匹配对应的延迟关门时间
        if (CategoryEnum.PLASTIC_BOTTLE_REGENERANT.getId().equals(mCurrentItem.getItemId())){
            tempTimer = PLASTIC_BOTTLE_CLOSE_DOOR_COUNT_DOWN_TIME;
        }else {
            tempTimer = OTHER_CLOSE_DOOR_COUNT_DOWN_TIME;
        }

        // 初始化关门倒计时
        initCloseDoorCountDownTimer();
        // 初始化强制回收倒计时
        initForceRecycleCountDownTimer(PLASTIC_BOTTLE_FORCE_RECYCLE_COUNT_DOWN_TIME);
        // 初始化返回首页倒计时
        initComBackHomePageCountDownTimer();

        // 饮料瓶 直接开门
        // 玻璃、有害垃圾 直接开门
        // 其他 先称重，称重成功之后再开门
        if (CategoryEnum.PLASTIC_BOTTLE_REGENERANT.getId().equals(mCurrentItem.getItemId())){
            EventBus.getDefault().post(new ActionMessageEvent(address, ComConstant.OPEN_USER_RECYCLE_ACTION_CODE, null, 1, true));
        }else {
            EventBus.getDefault().post(new ActionMessageEvent(address, ComConstant.WEIGH_ACTION_CODE, WeighTypeEnum.PREFIX_WEIGH.getCode(), 1, true));
        }
    }

    @Override
    public void setData(Object data) {
        CategoryItem item = (CategoryItem) data;
        mCurrentItem = item;
        Log.d(TAG, "setData: " + mCurrentItem);
    }

    /**
     * 投递完成
     */
    @OnClick(R.id.btn_recycle_complete)
    public void recycleComplate(){
        recyclerComplete();
    }

    /**
     * 指令执行结果监听
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSerialPortActionResultMessageEvent(ActionResultEnum result){

        String address = getAddressCodeByCategoryItemId(mCurrentItem.getItemId());
        if (StringUtil.isEmpty(address)){
            String notice;
            if (LanguageUtil.isChinese()){
                notice = "尚未支持的回收类型: " + mCurrentItem.getCategoryName();
            }else {
                notice = "Unrecognized recycling type: " + mCurrentItem.getCategoryName();
            }
            EventBus.getDefault().post(new MessageEvent(null, notice, MessageEvent.MESSAGE_TYPE_NOTICE));
            return;
        }

        if (ActionResultEnum.OPEN_DOOR_SUCCESS.equals(result)){
            openDoorSuccess();
        }else if (ActionResultEnum.OPEN_DOOR_FAILD.equals(result)){
            openDoorFaild();
        }else if (ActionResultEnum.CLOSE_DOOR_SUCCESS.equals(result)){
            // 普通关门成功
            closeDoorSuccess();
        }else if (ActionResultEnum.PREFIX_CLOSE_DOOR_SUCCESS.equals(result)){
            prefixCloseDoorSuccess();
        }else if (ActionResultEnum.CLOSE_DOOR_FAILD.equals(result)){
            closeDoorFaild();
        }else if (ActionResultEnum.NORMAL_CLOSE_DOOR_EXCEPTION.equals(result)){
            // 正常关门的关门异常 -> 用户点击投递完成按钮 or 关门倒计时触发 -> 重置按钮为可点击 + 重置关门倒计时
            nomarlCloseDoorException();
        }else if (ActionResultEnum.PREFIX_NORMAL_CLOSE_DOOR_EXCEPTION.equals(result)){
            // 强制回收前置关门异常 -> 重置强制回收倒计时
            prefixCloseDoorException();
        }else if (ActionResultEnum.REFRESH_CLOSE_DOOR_COUNT_DOWN_TIME.equals(result)){
            // 刷新关门倒计时
            resetTimer(closeDoorCountDownTimer);
        }else if (ActionResultEnum.PLASTIC_BOTTLE_SCAN_RESULT_VALIDATE_SUCCESS.equals(result)){
            // 塑料瓶扫码校验成功
            barCodeValidateSuccess();
        }else if (ActionResultEnum.PLASTIC_BOTTLE_SCAN_RESULT_VALIDATE_FAILD.equals(result)){
            // 塑料瓶扫码校验失败
            barCodeValidateFaild();
        }else if (ActionResultEnum.USER_TAKE_BACK.equals(result)){
            // 用户成功取回物品
            userTakeBack();
        }else if (ActionResultEnum.RECYCLE_BRIEF_SUMMARY.equals(result)){
            // 单个投递物投递结算
            calcCurrentNum();
            showView(llBrifySummary, llRecycle);
        }else if (ActionResultEnum.RECYCLE_COMPLATE_VALIDATE_FAILD.equals(result)){
            // 光电 3 未触发
            calcCurrentNum();
            showView(llBrifySummary, llRecycle);
        }else if (ActionResultEnum.FORCE_RECYCLE_SUCCESS.equals(result)){
            // 强制回收成功
            // 跳转到 AcountFragment 界面上未显示强制回收成功
            EventBus.getDefault().post(new FragmentMessageEvent(FragmentMessageEvent.SWITCH_FRAGMENT, AcountFragment.FRAGMENT_ID, mCurrentItem));
        }else if (ActionResultEnum.FORCE_RECYCLE_FAILD.equals(result)){
            // 强制回收失败
            // 跳转到 AcountFragment 界面上未显示强制回收失败
            EventBus.getDefault().post(new FragmentMessageEvent(FragmentMessageEvent.SWITCH_FRAGMENT, AcountFragment.FRAGMENT_ID, mCurrentItem));
        }else if (ActionResultEnum.PREFIX_WEIGH_SUCCESS.equals(result)){
            // 开门前置称重成功，下一步应该去开门
            EventBus.getDefault().post(new ActionMessageEvent(address, ComConstant.OPEN_USER_RECYCLE_ACTION_CODE, null, 1, true));
        }else if (ActionResultEnum.SUFFIX_WEIGH_SUCCESS.equals(result)){
            // 关门后置称重成功，下一步应该去结算，当前称重结果传递
            EventBus.getDefault().post(new FragmentMessageEvent(FragmentMessageEvent.SWITCH_FRAGMENT, AcountFragment.FRAGMENT_ID, mCurrentItem));
        }else if (ActionResultEnum.WEIGH_PREFIX_CLOSE_DOOR_SUCCESS.equals(result)){
            // 称重前置关门成功，发送称重指令
            EventBus.getDefault().post(new ActionMessageEvent(address, ComConstant.WEIGH_ACTION_CODE, WeighTypeEnum.SUFFIX_WEIGH.getCode(), 1, true));
        }
    }

    /**
     * 强制回收前置关门异常
     */
    private void prefixCloseDoorException() {
        // 重置强制回收倒计时
        tvRecycleNotice.setText(R.string.button_close_recycle_door_exception);

        initForceRecycleCountDownTimer(10000);
        resetTimer(forceRecycleCountDownTimer);
    }

    /**
     * 正常关门异常
     */
    private void nomarlCloseDoorException() {
        // 重置按钮为可点击 + 重置关门倒计时
        tvCountDownTime.setVisibility(View.VISIBLE);
        llCountDownTime.setVisibility(View.VISIBLE);
        tvRecycleNotice.setText(R.string.button_close_recycle_door_exception);
        enableButton(btnRecycleComplate);
        resetTimer(closeDoorCountDownTimer);
    }

    private void showView(LinearLayout showll, LinearLayout hidell){
        showll.setVisibility(View.VISIBLE);
        hidell.setVisibility(View.GONE);
    }

    private void calcCurrentNum(){
        String unit = mCurrentItem.getUnit().substring(mCurrentItem.getUnit().indexOf("/") + 1);
        String s;
        if (LanguageUtil.isChinese()){
            s = "已投递 " + HomeActivity.CURRENT_RECYCLE_PLASTIC_BOTTLE_NUM + " " + unit;
        }else {
            s = "Has Delivered " + HomeActivity.CURRENT_RECYCLE_PLASTIC_BOTTLE_NUM + " " + unit;
        }
        tvCurrenRecycleValidateCount.setText(s);
        enableButton(btnRecycleComplate);
    }

    /**
     * 用户成功取回投递物
     */
    private void userTakeBack() {
        // 1. 强制回收任务关闭
        forceRecycleCountDownTimer.cancel();
        tvCountDownTime.setVisibility(View.GONE);
        llCountDownTime.setVisibility(View.INVISIBLE);
        // 2. 延时关门任务开启
        resetTimer(closeDoorCountDownTimer);
        // 显示结算页面
        calcCurrentNum();
        showView(llBrifySummary, llRecycle);
    }

    /**
     * 开门中
     */
    private void initView() {
        tvRecycleNotice.setText(R.string.opening_recycle_door);
        btnRecycleComplate.setText(R.string.button_open_recycle_door);
        disableButton(btnRecycleComplate);

        llCountDownTime.setVisibility(View.INVISIBLE);
        llBrifySummary.setVisibility(View.GONE);
        llRecycle.setVisibility(View.VISIBLE);
    }

    /**
     * 开门成功，包括了塑料瓶、金属、纸类
     */
    private void openDoorSuccess(){
        tvRecycleNotice.setText(R.string.open_door_success);
        btnRecycleComplate.setText(R.string.button_recycle_complate);
        enableButton(btnRecycleComplate);
        // 倒计时开始 -> 显示 + 计时
        resetTimer(closeDoorCountDownTimer);
    }

    /**
     * 开门失败，包括了塑料瓶、金属、纸类
     */
    private void openDoorFaild(){
        tvRecycleNotice.setText(R.string.open_door_faild);
        btnRecycleComplate.setText(R.string.button_open_recycle_door_faild);
        disableButton(btnRecycleComplate);
        // 开门失败设置倒计时，倒计时完毕跳转会首页
        COMBACK_HOME_PAGE_NOTICE = btnRecycleComplate.getText().toString();
        resetTimer(comBackHomePageCountDownTimer);
    }

    /**
     * 关门成功，包括了塑料瓶、金属、纸类
     * 投递完成成功 -> 结算页面
     */
    private void closeDoorSuccess(){
        // 按钮置为灰色
        disableButton(btnRecycleComplate);
        // 跳转到结算 Fragement
        // 要标明结算目标，塑料瓶 or 纸类 or 金属，把单价传递给结算页面
        EventBus.getDefault().post(new FragmentMessageEvent(FragmentMessageEvent.SWITCH_FRAGMENT, AcountFragment.FRAGMENT_ID, mCurrentItem));
    }

    /**
     * 关门失败，包括了塑料瓶、金属、纸类
     */
    private void closeDoorFaild(){
        tvRecycleNotice.setText(R.string.close_door_faild);
        btnRecycleComplate.setText(R.string.button_close_recycle_door_faild);
        disableButton(btnRecycleComplate);
        // 关门失败设置倒计时，倒计时完毕跳转会首页
        COMBACK_HOME_PAGE_NOTICE = btnRecycleComplate.getText().toString();
        resetTimer(comBackHomePageCountDownTimer);
    }

    /**
     * 强制回收前置关门成功
     */
    private void prefixCloseDoorSuccess(){
        // 投递完成按钮置为不可点击
        disableButton(btnRecycleComplate);
        // 发送强制回收指令
        EventBus.getDefault().post(new ActionMessageEvent(ComConstant.PLASTIC_BOTTLE_RECYCLE_IC_ADDRESS,
                ComConstant.FORCE_RECYCLE_ACTION_CODE, null, 1, true));
    }

    /**
     * 条码扫描校验成功
     */
    private void barCodeValidateSuccess(){
        // 投递完成按钮置为不可点击, 重置关门倒计时
        disableButton(btnRecycleComplate);
        resetTimer(closeDoorCountDownTimer);
    }

    /**
     * 条码扫描校验失败
     */
    private void barCodeValidateFaild(){
        // 投递完成按钮置为不可点击
        disableButton(btnRecycleComplate);

        llRecycle.setVisibility(View.VISIBLE);
        llBrifySummary.setVisibility(View.GONE);

        // 提示用户取回物品
        tvRecycleNotice.setText(R.string.barcode_validate_faild);
        // 延时关门任务如何处理
        closeDoorCountDownTimer.cancel();
        // 开启强制回收延时任务，延时触发，执行强制回收 -> 倒计时
        initForceRecycleCountDownTimer(PLASTIC_BOTTLE_FORCE_RECYCLE_COUNT_DOWN_TIME);
        resetTimer(forceRecycleCountDownTimer);
    }

    /**
     * 投递完成
     */
    private void recyclerComplete(){
        // 关门延时任务关闭
        closeDoorCountDownTimer.cancel();
        tvCountDownTime.setVisibility(View.GONE);
        llCountDownTime.setVisibility(View.INVISIBLE);
        disableButton(btnRecycleComplate);
        // 发送关门指令
        // 动态地址
        String address = getAddressCodeByCategoryItemId(mCurrentItem.getItemId());
        if (StringUtil.isEmpty(address)){
            String notice;
            if (LanguageUtil.isChinese()){
                notice = "尚未支持的回收类型: " + mCurrentItem.getCategoryName();
            }else {
                notice = "Unrecognized recycling type: " + mCurrentItem.getCategoryName();
            }
            EventBus.getDefault().post(new MessageEvent(null, notice, MessageEvent.MESSAGE_TYPE_NOTICE));
            return;
        }
        // 正常业务关门 -> 塑料瓶
        // 称重前置关门 -> 金属、之类
        String param = null;
        if (!CategoryEnum.PLASTIC_BOTTLE_REGENERANT.getId().equals(mCurrentItem.getItemId())){
            param = "0002";
        }
        EventBus.getDefault().post(new ActionMessageEvent(address, ComConstant.CLOSE_USER_RECYCLE_ACTION_CODE, param, 1, true));
    }

    private void initCloseDoorCountDownTimer() {
        if (closeDoorCountDownTimer == null){
            closeDoorCountDownTimer = new CountDownTimer(tempTimer + 1100, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    String s = String.valueOf((millisUntilFinished / 1000) - 1) + "S";
                    tvCountDownTime.setText(s);
                }

                @Override
                public void onFinish() {
                    // 走投递完成逻辑
                    recyclerComplete();
                }
            };
        }
    }

    private void initForceRecycleCountDownTimer(Integer t){
        if (forceRecycleCountDownTimer != null){
            forceRecycleCountDownTimer.cancel();
            forceRecycleCountDownTimer = null;
        }
        forceRecycleCountDownTimer = new CountDownTimer(t + 1100, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                String s = String.valueOf((millisUntilFinished / 1000) - 1) + "S";
                tvCountDownTime.setText(s);
            }

            @Override
            public void onFinish() {
                forceRecycleCountDownTimer.cancel();
                tvCountDownTime.setVisibility(View.GONE);
                llCountDownTime.setVisibility(View.INVISIBLE);
                // 强制回收前置关门指令
                EventBus.getDefault().post(new ActionMessageEvent(ComConstant.PLASTIC_BOTTLE_RECYCLE_IC_ADDRESS,
                        ComConstant.CLOSE_USER_RECYCLE_ACTION_CODE, "0001", 1, true));
            }
        };
    }

    private void initComBackHomePageCountDownTimer() {
        if (comBackHomePageCountDownTimer == null){
            comBackHomePageCountDownTimer = new CountDownTimer(COM_BACK_HOME_PAGE_COUNT_DOWN_TIME + 1100, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    String s = String.valueOf((millisUntilFinished / 1000) - 1) + "S";
                    tvCountDownTime.setText(s);
                    String notice;
                    if (LanguageUtil.isChinese()){
                        notice = COMBACK_HOME_PAGE_NOTICE + s + "后将自动返回首页!";
                    }else {
                        notice = COMBACK_HOME_PAGE_NOTICE  + " After " + s + " will automatically return to the home page!";
                    }
                    tvRecycleNotice.setText(notice);
                }

                @Override
                public void onFinish() {
                    // 返回首页
                    COMBACK_HOME_PAGE_NOTICE = "";
                    comBackHomePageCountDownTimer.cancel();
                    tvCountDownTime.setVisibility(View.GONE);
                    llCountDownTime.setVisibility(View.INVISIBLE);
                    EventBus.getDefault().post(new FragmentMessageEvent(FragmentMessageEvent.SWITCH_FRAGMENT, HomeFragment.FRAGMENT_ID, null));
                }
            };
        }
    }

    private void resetTimer(CountDownTimer timer){

        tvCountDownTime.setVisibility(View.VISIBLE);
        llCountDownTime.setVisibility(View.VISIBLE);

        timer.cancel();
        timer.start();
    }

    private void enableButton(Button btn){
        btn.setEnabled(true);
        btn.setBackground(this.getResources().getDrawable(R.drawable.send_code_btn));
    }

    private void disableButton(Button btn){
        btn.setEnabled(false);
        btn.setBackground(this.getResources().getDrawable(R.drawable.disable_btn));
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().register(this);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}
