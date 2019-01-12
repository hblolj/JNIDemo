package com.example.ori.jnidemo;

import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.example.ori.jnidemo.base.Activity;
import com.example.ori.jnidemo.base.Fragment;
import com.example.ori.jnidemo.bean.ActionMessageEvent;
import com.example.ori.jnidemo.bean.CategoryItem;
import com.example.ori.jnidemo.bean.ComBean;
import com.example.ori.jnidemo.bean.FragmentMessageEvent;
import com.example.ori.jnidemo.bean.MessageEvent;
import com.example.ori.jnidemo.bean.OrderValidate;
import com.example.ori.jnidemo.constant.ComConstant;
import com.example.ori.jnidemo.enums.ActionResultEnum;
import com.example.ori.jnidemo.enums.CategoryEnum;
import com.example.ori.jnidemo.enums.WeighTypeEnum;
import com.example.ori.jnidemo.fragments.AcountFragment;
import com.example.ori.jnidemo.fragments.AdminFragment;
import com.example.ori.jnidemo.fragments.HomeFragment;
import com.example.ori.jnidemo.fragments.RecycleFragment;
import com.example.ori.jnidemo.helper.NavHelper;
import com.example.ori.jnidemo.interfaces.ComDataReceiverInterface;
import com.example.ori.jnidemo.utils.BizHandleUtil;
import com.example.ori.jnidemo.utils.OrderHandleUtil;
import com.example.ori.jnidemo.utils.OrderUtils;
import com.example.ori.jnidemo.utils.StringUtil;
import com.example.ori.jnidemo.utils.ToastHelper;
import com.example.ori.jnidemo.utils.barcode.BarCodeScanUtil;
import com.example.ori.jnidemo.utils.serial_port.SerialHelper;
import com.ys.myapi.MyManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import android_serialport_api.SerialPortFinder;
import butterknife.BindView;
import butterknife.OnClick;

public class HomeActivity extends Activity implements NavHelper.OnTabChangedListener<Integer>, ComDataReceiverInterface {

    private static final String TAG = HomeActivity.class.getSimpleName();

    private static final Integer ADMIN_MODEL = 1;

    private static final Integer USER_MODEL = 2;

    // 回收机中塑料瓶总数
    public static Integer TOTAL_PLASTIC_BOTTLE_NUM = 0;
    // 回收机中有效塑料瓶总数
    public static Integer TOTAL_VALID_PLASTIC_BOTTLE_NUM = 0;
    // 一次开关门之间回收的瓶子数量
    public static Integer CURRENT_RECYCLE_PLASTIC_BOTTLE_NUM = 0;
    // 回收机中金属总重量
    public static BigDecimal TOTAL_METAL_WEIGH = new BigDecimal("0");
    public static BigDecimal PREFIX_TOTAL_METAL_WEIGH = new BigDecimal("0");
    public static BigDecimal SUFIX_TOTAL_METAL_WEIGH = new BigDecimal("0");
    // 回收机中纸类总重量
    public static BigDecimal TOTAL_PAPER_WEIGH = new BigDecimal("0");
    public static BigDecimal PREFIX_TOTAL_PAPER_WEIGH = new BigDecimal("0");
    public static BigDecimal SUFIX_TOTAL_PAPER_WEIGH = new BigDecimal("0");

    private List<CategoryItem> items = new ArrayList<>();

    public NavHelper<Integer> navHelper;

    private SerialHelper comHelper;

    private MyManager myManager;

    @BindView(R.id.btn_admin)
    Button btnAdmin;

    private Handler myHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {

            // 1. 判断是否收到响应，如果已经收到响应了，重置重试次数，清除对应数据
            String key = (String) message.obj;
            OrderValidate orderValidate = SerialHelper.waitReplys.get(key);
            if (orderValidate == null){
                // 已经收到了响应
                return true;
            }

            // 2. 判断重试次数，如果已经超过 Max 重试次数，记录指令发送失败
            Integer retryCount = orderValidate.getRetryCount();
            if (retryCount >= ComConstant.MAX_RETRY_COUNT){
                Log.d(TAG, "指令发送失败 -> content: " + orderValidate.getSendOrder().getOrderContent());
                // 指令未收到应答
                OrderHandleUtil.handleTimeOutReplyOrder(orderValidate.getWaitReceiverOrder(), myHandler);
                SerialHelper.waitReplys.remove(orderValidate.getWaitReceiverOrder().getOrderContent());
                return false;
            }

            // 3. 如果没有超过重试次数，且没有收到响应，重新发送指令
            // 重发的指令，也会生成一个带应答的标记存储到map中，因为key 相同，会覆盖之前的key，计数 + 1
            OrderUtils.retrySendOrder(comHelper, orderValidate, myHandler);
            return false;
        }
    });

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_home;
    }

    @Override
    protected void initWindows() {
        super.initWindows();
        //设置屏幕为横屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//设置成全屏模式
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    @Override
    protected void initWidget() {

        super.initWidget();

//        if (myManager == null){
//            myManager = MyManager.getInstance(this);
//            Intent intent = new Intent(this, WatchDogService.class);
//            startService(intent);
//        }


        btnAdmin.setTag(ADMIN_MODEL);

        // 初始化切换到 HomeFragment 开始、投递结束
        // 投递回收 Fragment 投递中
        // 投递成功 Fragment +1
        // 投递完成 Fragment 开关门结算
        navHelper = new NavHelper<>(this, getSupportFragmentManager(), R.id.lay_container, this);
        navHelper
                .add(HomeFragment.FRAGMENT_ID, new NavHelper.Tab<>(HomeFragment.class, HomeFragment.FRAGMENT_ID))
                .add(RecycleFragment.FRAGMENT_ID, new NavHelper.Tab<>(RecycleFragment.class, RecycleFragment.FRAGMENT_ID))
                .add(AcountFragment.FRAGMENT_ID, new NavHelper.Tab<>(AcountFragment.class, AcountFragment.FRAGMENT_ID))
                .add(AdminFragment.FRAGMENT_ID, new NavHelper.Tab<>(AdminFragment.class, AdminFragment.FRAGMENT_ID));
    }

    @Override
    protected void initData() {
        super.initData();

        if (items.size() == 0){
            items.add(new CategoryItem(CategoryEnum.METAL_REGENERANT));
//        items.add(new CategoryItem(CategoryEnum.PLASTIC_REGENERANT));
//        items.add(new CategoryItem(CategoryEnum.TEXTILE_REGENERANT));
            items.add(new CategoryItem(CategoryEnum.PLASTIC_BOTTLE_REGENERANT));
            items.add(new CategoryItem(CategoryEnum.PAPER_REGENERANT));
//        items.add(new CategoryItem(CategoryEnum.GLASS_REGENERANT));
//        items.add(new CategoryItem(CategoryEnum.HARMFUL_WASTE));
        }

        for (int id : InputDevice.getDeviceIds()) {
            String name = InputDevice.getDevice(id).getName().trim();
            Log.d(TAG, "DeviceNames: " + name);
        }
        // 初始化点击
        onFragmentMessageEvent(new FragmentMessageEvent(FragmentMessageEvent.SWITCH_FRAGMENT, HomeFragment.FRAGMENT_ID, items));
        reConnectionSerial();
    }

    @OnClick(R.id.btn_admin)
    public void go2AdminPager(){
        if (ADMIN_MODEL.equals(btnAdmin.getTag())){
            btnAdmin.setText("我是用户");
            btnAdmin.setTag(USER_MODEL);
            onFragmentMessageEvent(new FragmentMessageEvent(FragmentMessageEvent.SWITCH_FRAGMENT, AdminFragment.FRAGMENT_ID, items));
        }else if (USER_MODEL.equals(btnAdmin.getTag())){
            btnAdmin.setText("我是管理员");
            btnAdmin.setTag(ADMIN_MODEL);
            onFragmentMessageEvent(new FragmentMessageEvent(FragmentMessageEvent.SWITCH_FRAGMENT, HomeFragment.FRAGMENT_ID, items));
        }
    }

    /**
     * Fragment 之间数据交互
     * Fragment A -> Activity -> Fragment B
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFragmentMessageEvent(FragmentMessageEvent event){
        if (FragmentMessageEvent.SWITCH_FRAGMENT.equals(event.getMessageType())){
            navHelper.performClickMenu((Integer) event.getData());
            ((Fragment) navHelper.getCurrentTab().getFragment()).setData(event.getExtra());
        }
        Log.d(TAG, "当前 Fragment: " + navHelper.getCurrentTab().clx.getSimpleName());
    }

    /**
     * Fragment 发送串口通信指令
     * Fragment -> Activity -> SerialPort
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onActionMessageEvent(ActionMessageEvent event){
        if (event.getNeedResult()){
            OrderUtils.sendNeedResponseOrder(comHelper, event, myHandler);
        }else {
            OrderUtils.sendOrder(comHelper, event, myHandler);
        }
    }

    /**
     * 接受到的串口消息处理结果
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveDataHandleResultMessageEvent(MessageEvent event){

        if (MessageEvent.MESSAGE_TYPE_NOTICE.equals(event.getType())){
            ToastHelper.showShortMessage(this, (String) event.getMessage());
        }else if (MessageEvent.MESSAGE_TYPE_OPEN_DOOR_RESULT.equals(event.getType())){
            // 开门结果处理
            BizHandleUtil.handleOpenDoorResult(event);
        }else if (MessageEvent.MESSAGE_TYPE_CLOSE_DOOR_RESULT.equals(event.getType())){
            // 关门结果
            BizHandleUtil.handleCloseDoorResult(event, comHelper, myHandler);
        }else if (MessageEvent.MESSAGE_TYPE_RESET_CLOSE_DOOR_COUNTDOWN.equals(event.getType())){
            // 光电 1 信号 -> 关门倒计时
            EventBus.getDefault().post(ActionResultEnum.REFRESH_CLOSE_DOOR_COUNT_DOWN_TIME);
        }else if (MessageEvent.MESSAGE_TYPE_REQUEST_SCAN_RESULT.equals(event.getType())){
            // IC 请求扫码结果 校验扫码结果
            BizHandleUtil.handleScanRequest(event, comHelper, myHandler);
        }else if (MessageEvent.MESSAGE_TYPE_IC_NOT_RECEIVE_SCAN_RESULT.equals(event.getType())){
            // IC 请求扫码结果后，规定时间内未收到扫码结果，做扫码失败相同处理
            EventBus.getDefault().post(ActionResultEnum.PLASTIC_BOTTLE_SCAN_RESULT_VALIDATE_FAILD);
        }else if (MessageEvent.MESSAGE_TYPE_RECYCLE_BRIEF_SUMMARY.equals(event.getType())){
            // 单次回收结束
            BizHandleUtil.handleSingleRecoveryComplateMessage(event, myHandler);
        }else if (MessageEvent.MESSAGE_TYPE_FORCE_RECYCLE_RESULT.equals(event.getType())){
            // 强制回收结果
            SerialHelper.waitResults.remove(event.getKey());
            // 成功与失败分别走单次回收结束流程，参数不一致
            Boolean result = (Boolean) event.getMessage();
            EventBus.getDefault().post(result ? ActionResultEnum.FORCE_RECYCLE_SUCCESS : ActionResultEnum.FORCE_RECYCLE_FAILD);
        }else if (MessageEvent.MESSAGE_TYPE_WEIGH_RESULT.equals(event.getType())){
            // 称重结果
            SerialHelper.waitResults.remove(event.getKey());
            BizHandleUtil.handleWeighResultMessage(event);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().register(this);
        }
    }

    /**
     * 串口重连
     */
    public void reConnectionSerial(){
        String[] devices = new SerialPortFinder().getAllDevices();
        String result = null;
        for (String d : devices) {
            if (d.contains("USB")){
                d = d.replace(" ", "").trim();
                d = d.substring(0, d.indexOf("("));
                result = d;
                break;
            }
            System.out.println("串口名称: " + d);
        }
        if (StringUtil.isEmpty(result)){
            ToastHelper.showShortMessage(this, "没有扫描到合法串口!");
            return;
        }
        initPort(result);
    }

    /**
     * 初始化连接串口
     * @param result
     */
    private void initPort(String result) {
        if (comHelper != null){
            comHelper.closeComPort();
            comHelper = null;
        }
        comHelper = new SerialHelper("/dev/" + result, "9600", this);
        comHelper.openComPort();
    }

    @Override
    public void onTabChanged(NavHelper.Tab<Integer> newTab, NavHelper.Tab<Integer> oldTab) {

    }

    /**
     * 串口返回的数据
     * 通过 EventBus 发送给 Fragment 处理
     * @param comRecData
     */
    @Override
    public void onDataReceived(ComBean comRecData) {
        String receiverData = comRecData.getsRec().toUpperCase();
        OrderHandleUtil.handlerReceiveData(receiverData, myHandler);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getDevice().getName().equals(BarCodeScanUtil.DEVICE_NAME)){
            int keyCode = event.getKeyCode();
            if (keyCode != KeyEvent.KEYCODE_ENTER){
                if (event.getAction() == KeyEvent.ACTION_DOWN){
                    BarCodeScanUtil.getInstance().checkLetterStatus(event);
                    BarCodeScanUtil.getInstance().keyCodeToNum(keyCode);
                }
            }else {
                if (event.getAction() == KeyEvent.ACTION_DOWN){
                    BarCodeScanUtil.getInstance().saveScanResult();
                }
            }
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        //判断是否有焦点
        if(hasFocus && Build.VERSION.SDK_INT >= 19){
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            |View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            |View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            |View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            |View.SYSTEM_UI_FLAG_FULLSCREEN
                            |View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            );
        }
    }

    /**
     * @param sourceAddress 地址 -> 回收物类型
     * @param type 称重类型，开门前置回收还是关门后置回收
     * @param w 重量
     */
    public static void setWeigh(String sourceAddress, String type, BigDecimal w){

        if (ComConstant.METAL_RECYCLE_IC_ADDRESS.equals(sourceAddress)){
            if (WeighTypeEnum.PREFIX_WEIGH.getCode().equals(type)){
                // 金属开门前置称重
                PREFIX_TOTAL_METAL_WEIGH = w;
            }else if (WeighTypeEnum.SUFFIX_WEIGH.getCode().equals(type)){
                // 金属关门后置称重
                SUFIX_TOTAL_METAL_WEIGH = w;
            }
        }else if (ComConstant.PAPER_RECYCLE_IC_ADDRESS.equals(sourceAddress)){
            if (WeighTypeEnum.PREFIX_WEIGH.getCode().equals(type)){
                // 纸类开门前置称重
                PREFIX_TOTAL_PAPER_WEIGH = w;
            }else if (WeighTypeEnum.SUFFIX_WEIGH.getCode().equals(type)){
                // 纸类关门后置称重
                SUFIX_TOTAL_PAPER_WEIGH = w;
            }
        }
    }

    public static BigDecimal getValidWeigh(CategoryItem categoryItem){
        if (CategoryEnum.METAL_REGENERANT.getId().equals(categoryItem.getItemId())){
            // 计算有效金属重量
            Log.d(TAG, "开门前金属重量: " + PREFIX_TOTAL_METAL_WEIGH + " 开门后金属重量: " + SUFIX_TOTAL_METAL_WEIGH);
            BigDecimal result = SUFIX_TOTAL_METAL_WEIGH.subtract(PREFIX_TOTAL_METAL_WEIGH);
            TOTAL_METAL_WEIGH = TOTAL_METAL_WEIGH.add(result);
            return result;
        }else if (CategoryEnum.PAPER_REGENERANT.getId().equals(categoryItem.getItemId())){
            // 计算有效纸类重量
            Log.d(TAG, "开门前纸类重量: " + PREFIX_TOTAL_PAPER_WEIGH + " 开门后纸类重量: " + SUFIX_TOTAL_PAPER_WEIGH);
            BigDecimal result = SUFIX_TOTAL_PAPER_WEIGH.subtract(PREFIX_TOTAL_PAPER_WEIGH);
            TOTAL_PAPER_WEIGH = TOTAL_PAPER_WEIGH.add(result);
            return result;
        }
        return new BigDecimal("0");
    }

    public static String getDataByCategoryItem(CategoryItem item){
        if (CategoryEnum.METAL_REGENERANT.getId().equals(item.getItemId())){
            // 金属
            return HomeActivity.TOTAL_METAL_WEIGH + " " + item.getUnit();
        }else if (CategoryEnum.PAPER_REGENERANT.getId().equals(item.getItemId())){
            // 纸类
            return HomeActivity.TOTAL_PAPER_WEIGH + " " + item.getUnit();
        }else if (CategoryEnum.PLASTIC_BOTTLE_REGENERANT.getId().equals(item.getItemId())){
            // 塑料瓶
            return TOTAL_PLASTIC_BOTTLE_NUM + " " + item.getUnit();
        }
        return "";
    }

    /**
     * 按类型清除数据
     * @param item
     */
    public static void cleanDataByCategoryItem(CategoryItem item){
        if (CategoryEnum.METAL_REGENERANT.getId().equals(item.getItemId())){
            // 金属
            TOTAL_METAL_WEIGH = new BigDecimal("0");
        }else if (CategoryEnum.PAPER_REGENERANT.getId().equals(item.getItemId())){
            // 纸类
            TOTAL_PAPER_WEIGH = new BigDecimal("0");
        }else if (CategoryEnum.PLASTIC_BOTTLE_REGENERANT.getId().equals(item.getItemId())){
            // 塑料瓶
            TOTAL_PLASTIC_BOTTLE_NUM = 0;
            TOTAL_VALID_PLASTIC_BOTTLE_NUM = 0;
            CURRENT_RECYCLE_PLASTIC_BOTTLE_NUM = 0;
        }
    }
}
