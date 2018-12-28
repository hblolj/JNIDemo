package com.example.ori.jnidemo;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.ori.jnidemo.base.Activity;
import com.example.ori.jnidemo.base.Fragment;
import com.example.ori.jnidemo.bean.ActionMessageEvent;
import com.example.ori.jnidemo.bean.ComBean;
import com.example.ori.jnidemo.bean.FragmentMessageEvent;
import com.example.ori.jnidemo.bean.MessageEvent;
import com.example.ori.jnidemo.bean.OrderValidate;
import com.example.ori.jnidemo.constant.ComConstant;
import com.example.ori.jnidemo.enums.ActionResultEnum;
import com.example.ori.jnidemo.enums.RecycleBriefSummaryTypeEnum;
import com.example.ori.jnidemo.fragments.AcountFragment;
import com.example.ori.jnidemo.fragments.HomeFragment;
import com.example.ori.jnidemo.fragments.RecycleFragment;
import com.example.ori.jnidemo.helper.NavHelper;
import com.example.ori.jnidemo.interfaces.ComDataReceiverInterface;
import com.example.ori.jnidemo.utils.CommonUtil;
import com.example.ori.jnidemo.utils.OrderHandleUtil;
import com.example.ori.jnidemo.utils.OrderUtils;
import com.example.ori.jnidemo.utils.StringUtil;
import com.example.ori.jnidemo.utils.ToastHelper;
import com.example.ori.jnidemo.utils.barcode.BarCodeScanUtil;
import com.example.ori.jnidemo.utils.serial_port.SerialHelper;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import android_serialport_api.SerialPortFinder;

public class HomeActivity extends Activity implements NavHelper.OnTabChangedListener<Integer>, ComDataReceiverInterface {

    private static final String TAG = HomeActivity.class.getSimpleName();

    // 回收机中塑料瓶总数
    public static Integer TOTAL_PLASTIC_BOTTLE_NUM = 0;
    // 回收机中有效塑料瓶总数
    public static Integer TOTAL_VALID_PLASTIC_BOTTLE_NUM = 0;
    // 一次开关门之间回收的瓶子数量
    public static Integer CURRENT_RECYCLE_PLASTIC_BOTTLE_NUM = 0;

    /**
     * 塑料瓶回收完成确认延迟任务(光电 3 信号延迟检验)
     */
    private static Runnable PLASTIC_BOTTLE_RECYCLE_COMPLETE_CONFIRM_DELAY_TASK = null;
    /**
     * 塑料瓶回收完成确认延迟任务(光电 3 信号延迟检验) 延迟时间
     */
    private static final Long PLASTIC_BOTTLE_RECYCLE_COMPLETE_CONFIRM_DELAY_TIME = 30000L;

    public NavHelper<Integer> navHelper;

    private SerialHelper comHelper;

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
    private Runnable recycleCompleteConfirmDelayTask;

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_home;
    }

    @Override
    protected void initWidget() {
        super.initWidget();
        // 初始化切换到 HomeFragment 开始、投递结束
        // 投递回收 Fragment 投递中
        // 投递成功 Fragment +1
        // 投递完成 Fragment 开关门结算
        navHelper = new NavHelper<>(this, getSupportFragmentManager(), R.id.lay_container, this);
        navHelper
                .add(HomeFragment.FRAGMENT_ID, new NavHelper.Tab<>(HomeFragment.class, HomeFragment.FRAGMENT_ID))
                .add(RecycleFragment.FRAGMENT_ID, new NavHelper.Tab<>(RecycleFragment.class, RecycleFragment.FRAGMENT_ID))
                .add(AcountFragment.FRAGMENT_ID, new NavHelper.Tab<>(AcountFragment.class, AcountFragment.FRAGMENT_ID));
    }

    @Override
    protected void initData() {
        super.initData();
        // 初始化点击
        navHelper.performClickMenu(HomeFragment.FRAGMENT_ID);
        reConnectionSerial();
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
            // 开门结果
            SerialHelper.waitResults.remove(event.getKey());
            Boolean openDoorResult = (Boolean) event.getMessage();
            if (openDoorResult){
                // 开门成功
                BarCodeScanUtil.getInstance().clearData();
            }
            // 通知 RecyclerFragment 开门结果
            EventBus.getDefault().post(openDoorResult ? ActionResultEnum.OPEN_DOOR_SUCCESS : ActionResultEnum.OPEN_DOOR_FAILD);
        }else if (MessageEvent.MESSAGE_TYPE_CLOSE_DOOR_RESULT.equals(event.getType())){
            // 关门结果
            SerialHelper.waitResults.remove(event.getKey());
            Boolean closeDoorResult = (Boolean) event.getMessage();
            if (closeDoorResult){
                // 关门成功
                BarCodeScanUtil.getInstance().clearData();
                Boolean normalCloseDoor = (Boolean) event.getExtra();
                if (!normalCloseDoor){
                    // 强制回收前置关门结果
                    EventBus.getDefault().post(ActionResultEnum.PREFIX_CLOSE_DOOR_SUCCESS);
                    // 发送强制回收指令
                    OrderUtils.sendNeedResponseOrder(comHelper, ComConstant.PLASTIC_BOTTLE_RECYCLE_IC_ADDRESS,
                            ComConstant.FORCE_RECYCLE_ACTION_CODE, null, myHandler, 1);
                    return;
                }
            }
            EventBus.getDefault().post(closeDoorResult ? ActionResultEnum.CLOSE_DOOR_SUCCESS : ActionResultEnum.CLOSE_DOOR_FAILD);
        }else if (MessageEvent.MESSAGE_TYPE_RESET_CLOSE_DOOR_COUNTDOWN.equals(event.getType())){
            // 光电 1 信号 -> 关门倒计时
            EventBus.getDefault().post(ActionResultEnum.REFRESH_CLOSE_DOOR_COUNT_DOWN_TIME);
        }else if (MessageEvent.MESSAGE_TYPE_REQUEST_SCAN_RESULT.equals(event.getType())){
            // IC 请求扫码结果 校验扫码结果
            Boolean scanResult = BarCodeScanUtil.getInstance().validateScanResult();
            if (scanResult){
                // 开启一个光电 3 检测延时，延时触发，走单次回收结束流程
                startRecycleComplteConfirmDelayTask();
            }
            // 返回扫码结果给 IC
            OrderUtils.sendOrder(comHelper, ComConstant.PLASTIC_BOTTLE_RECYCLE_IC_ADDRESS, ComConstant.BAR_CODE_SCAN_VALIDATE_RESULT_ACTION_CODE, scanResult ? "FFFF" : "0000", myHandler, 1);
            EventBus.getDefault().post(scanResult ? ActionResultEnum.PLASTIC_BOTTLE_SCAN_RESULT_VALIDATE_SUCCESS : ActionResultEnum.PLASTIC_BOTTLE_SCAN_RESULT_VALIDATE_FAILD);
        }else if (MessageEvent.MESSAGE_TYPE_IC_NOT_RECEIVE_SCAN_RESULT.equals(event.getType())){
            // IC 请求扫码结果后，规定时间内未收到扫码结果，做扫码失败相同处理
            EventBus.getDefault().post(ActionResultEnum.PLASTIC_BOTTLE_SCAN_RESULT_VALIDATE_FAILD);
        }else if (MessageEvent.MESSAGE_TYPE_RECYCLE_BRIEF_SUMMARY.equals(event.getType())){
            // 单次回收结束
            String param = (String) event.getMessage();
            if (RecycleBriefSummaryTypeEnum.COMPLATE_SINGAL.getCode().equals(param)){
                // 光电 3 触发
                // 1. 移除光电 3 校验延时任务
                clearTask(PLASTIC_BOTTLE_RECYCLE_COMPLETE_CONFIRM_DELAY_TASK);
                // 2. 检查扫码结果，进行消消乐
                TOTAL_PLASTIC_BOTTLE_NUM ++;
                if (BarCodeScanUtil.getInstance().validateScanResult()){
                    // 移除第一个扫码结果，有效投递数 + 1
                    BarCodeScanUtil.getInstance().consumeScanResult();
                    // 当次投递数 + 1
                    CURRENT_RECYCLE_PLASTIC_BOTTLE_NUM++;
                    TOTAL_VALID_PLASTIC_BOTTLE_NUM++;
                }
                // 4. 通知 RecycleFragment 更新，界面显示切换 -> 当前投递数
                EventBus.getDefault().post(ActionResultEnum.RECYCLE_BRIEF_SUMMARY);
            }else if (RecycleBriefSummaryTypeEnum.SCAN_VALIDATE_FAILD_USER_TAKE_BACK.getCode().equals(param)){
                // 扫码失败，用户成功取回
                EventBus.getDefault().post(ActionResultEnum.USER_TAKE_BACK);
            }else if (RecycleBriefSummaryTypeEnum.SCAN_VALIDATE_SUCCESS_DONT_RECEIVE_COMPLATE_SINGAL.getCode().equals(param)){
                // 扫码成功，光电 3 未触发
                EventBus.getDefault().post(ActionResultEnum.RECYCLE_COMPLATE_VALIDATE_FAILD);
            }
        }else if (MessageEvent.MESSAGE_TYPE_FORCE_RECYCLE_RESULT.equals(event.getType())){
            // 强制回收结果
            SerialHelper.waitResults.remove(event.getKey());
            // 成功与失败分别走单次回收结束流程，参数不一致
            Boolean result = (Boolean) event.getMessage();
            EventBus.getDefault().post(result ? ActionResultEnum.FORCE_RECYCLE_SUCCESS : ActionResultEnum.FORCE_RECYCLE_FAILD);
        }
    }

    private void startRecycleComplteConfirmDelayTask() {
        clearTask(PLASTIC_BOTTLE_RECYCLE_COMPLETE_CONFIRM_DELAY_TASK);
        PLASTIC_BOTTLE_RECYCLE_COMPLETE_CONFIRM_DELAY_TASK = getRecycleCompleteConfirmDelayTask();
        myHandler.postDelayed(PLASTIC_BOTTLE_RECYCLE_COMPLETE_CONFIRM_DELAY_TASK, PLASTIC_BOTTLE_RECYCLE_COMPLETE_CONFIRM_DELAY_TIME);
    }

    private void clearTask(Runnable task){
        if (task != null){
            myHandler.removeCallbacks(task);
            task = null;
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
        String receiverData = CommonUtil.bytesToHexString(comRecData.getbRec()).replace(" ", "").toUpperCase();
        OrderHandleUtil.handlerReceiveData(receiverData, myHandler);
    }

    public Runnable getRecycleCompleteConfirmDelayTask() {
        return new Runnable() {
            @Override
            public void run() {
                // TODO: 2018/12/28 0002 抽取成 Enum
                // 收到光电 2 触发后，计时 2 秒，如果还是没有收到光电 3 信号，做结算处理！
                EventBus.getDefault().post(new MessageEvent("","0002", MessageEvent.MESSAGE_TYPE_RECYCLE_BRIEF_SUMMARY));
            }
        };
    }
}
