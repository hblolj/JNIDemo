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
import com.example.ori.jnidemo.constant.ComConstant;
import com.example.ori.jnidemo.enums.ActionResultEnum;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 回收 Fragment
 */
public class RecycleFragment extends com.example.ori.jnidemo.base.Fragment {

    private static final String TAG = RecycleFragment.class.getSimpleName();

    public static final Integer FRAGMENT_ID = 2;

    // 回收门关闭时间默认设为 30 秒
    public static final Integer CLOSE_DOOR_COUNT_DOWN_TIME = 30000;

    private Integer tempTimer;

    private CategoryItem mCurrentItem;

    @BindView(R.id.tv_count_down_time)
    TextView tvCountDownTime;

    @BindView(R.id.tv_recycle_notice)
    TextView tvRecycleNotice;

    @BindView(R.id.btn_recycle_complete)
    Button btnRecycleComplate;

    @BindView(R.id.ll_recycle)
    LinearLayout llRecycle;

    @BindView(R.id.ll_brify_summary)
    LinearLayout llBrifySummary;

    @BindView(R.id.tv_curren_recycle_validate_count)
    TextView tvCurrenRecycleValidateCount;

    private CountDownTimer closeDoorCountDownTimer;

    private CountDownTimer forceRecycleCountDownTimer;

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
        openingDoor();
    }

    @Override
    protected void initData() {
        super.initData();
        tempTimer = CLOSE_DOOR_COUNT_DOWN_TIME;
        initCloseDoorCountDownTimer();
        initForceRecycleCountDownTimer();

        // TODO: 2018/12/28 发送开门指令，暂时写死为塑料瓶回收地址
        EventBus.getDefault().post(new ActionMessageEvent(ComConstant.PLASTIC_BOTTLE_RECYCLE_IC_ADDRESS,
                ComConstant.OPEN_USER_RECYCLE_ACTION_CODE, null, 1, true));
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
        if (ActionResultEnum.OPEN_DOOR_SUCCESS.equals(result)){
            openDoorSuccess();
        }else if (ActionResultEnum.OPEN_DOOR_FAILD.equals(result)){
            openDoorFaild();
        }else if (ActionResultEnum.CLOSE_DOOR_SUCCESS.equals(result)){
            closeDoorSuccess();
        }else if (ActionResultEnum.PREFIX_CLOSE_DOOR_SUCCESS.equals(result)){
            prefixCloseDoorSuccess();
        }else if (ActionResultEnum.CLOSE_DOOR_FAILD.equals(result)){
            closeDoorFaild();
        }else if (ActionResultEnum.REFRESH_CLOSE_DOOR_COUNT_DOWN_TIME.equals(result)){
            // 刷新关门倒计时
            resetTimer(closeDoorCountDownTimer, null);
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
            // 跳转到 AcountFragment
            EventBus.getDefault().post(new FragmentMessageEvent(FragmentMessageEvent.SWITCH_FRAGMENT, AcountFragment.FRAGMENT_ID, ""));
        }else if (ActionResultEnum.FORCE_RECYCLE_FAILD.equals(result)){
            // 强制回收失败
            // 跳转到 AcountFragment
            EventBus.getDefault().post(new FragmentMessageEvent(FragmentMessageEvent.SWITCH_FRAGMENT, AcountFragment.FRAGMENT_ID, ""));
        }
    }

    private void showView(LinearLayout showll, LinearLayout hidell){
        showll.setVisibility(View.VISIBLE);
        hidell.setVisibility(View.GONE);
    }

    private void calcCurrentNum(){
        String s = "已投递" + HomeActivity.CURRENT_RECYCLE_PLASTIC_BOTTLE_NUM + "个";
        tvCurrenRecycleValidateCount.setText(s);
    }

    /**
     * 用户成功取回投递物
     */
    private void userTakeBack() {
        // 1. 强制回收任务关闭
        forceRecycleCountDownTimer.cancel();
        tvCountDownTime.setVisibility(View.GONE);
        // 2. 延时关门任务开启
        resetTimer(closeDoorCountDownTimer, null);
        // 显示结算页面
        String s = "已投递" + HomeActivity.CURRENT_RECYCLE_PLASTIC_BOTTLE_NUM + "个";
        tvCurrenRecycleValidateCount.setText(s);
        showView(llBrifySummary, llRecycle);
    }

    /**
     * 开门中
     */
    private void openingDoor() {
        tvRecycleNotice.setText(R.string.opening_recycle_door);
        btnRecycleComplate.setText(R.string.button_open_recycle_door);
        enableButton(btnRecycleComplate);
    }

    /**
     * 开门成功
     */
    private void openDoorSuccess(){
        tvRecycleNotice.setText(R.string.open_door_success);
        btnRecycleComplate.setText(R.string.button_recycle_complate);
        enableButton(btnRecycleComplate);
        // 倒计时开始 -> 显示 + 计时
        resetTimer(closeDoorCountDownTimer, null);
    }

    /**
     * 开门失败
     */
    private void openDoorFaild(){
        tvRecycleNotice.setText(R.string.open_door_faild);
        btnRecycleComplate.setText(R.string.button_open_recycle_door_faild);
        disableButton(btnRecycleComplate);
    }

    /**
     * 关门成功
     * 投递完成成功 -> 结算页面
     */
    private void closeDoorSuccess(){
        // 按钮置为灰色
        disableButton(btnRecycleComplate);
        // 跳转到结算 Fragement
        EventBus.getDefault().post(new FragmentMessageEvent(FragmentMessageEvent.SWITCH_FRAGMENT, AcountFragment.FRAGMENT_ID, ""));
    }

    /**
     * 强制回收前置关门成功
     */
    private void prefixCloseDoorSuccess(){
        // 投递完成按钮置为不可点击
        disableButton(btnRecycleComplate);
    }

    /**
     * 关门失败
     */
    private void closeDoorFaild(){
        tvRecycleNotice.setText(R.string.close_door_faild);
        btnRecycleComplate.setText(R.string.button_close_recycle_door_faild);
        disableButton(btnRecycleComplate);
    }

    /**
     * 条码扫描校验成功
     */
    private void barCodeValidateSuccess(){
        // 投递完成按钮置为不可点击, 重置关门倒计时
        disableButton(btnRecycleComplate);
        resetTimer(closeDoorCountDownTimer, null);
    }

    /**
     * 条码扫描校验失败
     */
    private void barCodeValidateFaild(){
        // 投递完成按钮置为不可点击
        disableButton(btnRecycleComplate);
        // 提示用户取回物品
        tvRecycleNotice.setText(R.string.barcode_validate_faild);
        // 延时关门任务如何处理
        closeDoorCountDownTimer.cancel();
        // 开启强制回收延时任务，延时触发，执行强制回收 -> 倒计时
        resetTimer(forceRecycleCountDownTimer, null);
    }

    /**
     * 投递完成
     */
    private void recyclerComplete(){
        // 关门延时任务关闭
        closeDoorCountDownTimer.cancel();
        tvCountDownTime.setVisibility(View.GONE);
        disableButton(btnRecycleComplate);
        // 发送关门指令
        EventBus.getDefault().post(new ActionMessageEvent(ComConstant.PLASTIC_BOTTLE_RECYCLE_IC_ADDRESS,
                ComConstant.CLOSE_USER_RECYCLE_ACTION_CODE, null, 1, true));
    }

    private void initCloseDoorCountDownTimer() {
        if (closeDoorCountDownTimer == null){
            closeDoorCountDownTimer = new CountDownTimer(tempTimer, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    String s = String.valueOf(millisUntilFinished / 1000) + "秒";
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

    private void initForceRecycleCountDownTimer(){
        if (forceRecycleCountDownTimer == null){
            forceRecycleCountDownTimer = new CountDownTimer(60000L, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    String s = String.valueOf(millisUntilFinished / 1000) + "秒";
                    tvCountDownTime.setText(s);
                }

                @Override
                public void onFinish() {
                    // 强制回收前置关门指令
                    forceRecycleCountDownTimer.cancel();
                    tvCountDownTime.setVisibility(View.GONE);
                    EventBus.getDefault().post(new ActionMessageEvent(ComConstant.PLASTIC_BOTTLE_RECYCLE_IC_ADDRESS,
                            ComConstant.CLOSE_USER_RECYCLE_ACTION_CODE, "0001", 1, true));
                }
            };
        }
    }

    private void resetTimer(CountDownTimer timer, Integer newTime){
        if (newTime != null && newTime > 0){
            tempTimer = newTime;
        }
        tvCountDownTime.setVisibility(View.VISIBLE);
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
    public void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().register(this);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }
}
