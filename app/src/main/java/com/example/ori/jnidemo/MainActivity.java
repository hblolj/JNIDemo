package com.example.ori.jnidemo;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.ori.jnidemo.bean.ComBean;
import com.example.ori.jnidemo.bean.MessageEvent;
import com.example.ori.jnidemo.bean.OrderValidate;
import com.example.ori.jnidemo.constant.ComConstant;
import com.example.ori.jnidemo.interfaces.ComDataReceiverInterface;
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
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements ComDataReceiverInterface{

    private static final String TAG = MainActivity.class.getSimpleName();

    /**
     * 回收机中塑料瓶总数
     */
    public static Integer TOTAL_PLASTICS_BOTTLE = 0;
    /**
     * 回收机中有效的塑料瓶总数(扫码 + 光电3)
     */
    public static Integer TOTAL_VALID_PLASTICS_BOTTLE = 0;
    /**
     * 本次开门 - 关门累计回收的塑料瓶总数
     */
    public static Integer CURRENT_PLASTICS_BOTTLE = 0;

    // 开始投递
    private static final Integer START_RECYCLE = 1;
    // 投递完成
    private static final Integer RECYCLE_COMPLATE = 2;
    // 继续投递
    private static final Integer CONTINUE_RECYCLE = 3;

    /**
     * 扫码成功，开启回收成功延时(通过光电 3 是否触发判断物品是否进入)
     */
    private static Runnable PLASTIC_RECYCLE_VALIDATE_DELAY_TASK = null;

    private static final Long PLASTIC_RECYCLE_VALIDATE_RESULT_DELAY_TIME = 30000L;

    private SerialHelper comHelper;

    @BindView(R.id.tv_notice)
    TextView tvNotice;

    @BindView(R.id.tv_close_door_count_down_time)
    TextView tvCloseDoorCountDownTime;

    @BindView(R.id.tv_fetch_count_down_time)
    TextView tvFetchCountDownTime;

    @BindView(R.id.btn_recycle_plastics_bottle)
    Button btnRecycle;

    @BindView(R.id.btn_finish_recycle_plastics_bottle)
    Button btnFinishRecyclePlasticsBottle;

    @BindView(R.id.btn_continue_recycle_plastics_bottle)
    Button btnContinueRecyclePlasticsBottle;

    @BindView(R.id.btn_reconnection)
    Button btnReconnection;

    // TODO: 2018/12/24 投递完成后，倒计时。没有做下一步选择，自动结束投递
    
    // 关门倒计时
    private CountDownTimer timer1;

    // 投递物取回倒计时
    private CountDownTimer timer2;

    private Long countDown = 30000L;

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

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
                logMessage(TAG, "指令发送失败 -> content: " + orderValidate.getSendOrder().getOrderContent());
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        reConnectionSerial();
        initViews();
        initDatas();

        for (int id : InputDevice.getDeviceIds()) {
            String name = InputDevice.getDevice(id).getName().trim();
            logMessage("DeviceNames", name);
        }
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

    private void initDatas() {
        initCloseDoorTimer();
        initFetchTimer();
    }

    private void initViews() {
        btnRecycle.setText("开始投递");
        btnRecycle.setTag(START_RECYCLE);
    }

    @OnClick(R.id.btn_reconnection)
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
     * 塑料瓶回收按钮点击事件
     */
    @OnClick(R.id.btn_recycle_plastics_bottle)
    public void recyclePlasticsBottle(){

        // 根据当前按钮的 Tag 来决定点击事件
        if (btnRecycle.getTag().equals(START_RECYCLE)){
            // 发送开门指令
            OrderUtils.sendNeedResponseOrder(comHelper, ComConstant.PLASTIC_BOTTLE_RECYCLE_IC_ADDRESS, ComConstant.OPEN_USER_RECYCLE_ACTION_CODE, null, myHandler, 1);
            // 界面显示开门中，请等待
            tvNotice.setText("开门中，请等待!");
            // 开门按钮置为不可点击
            disableButton(btnRecycle);
            btnRecycle.setText("正在努力开门.....");
        }else if (btnRecycle.getTag().equals(RECYCLE_COMPLATE)){
            // 投递完成
            timer1.cancel();
            tvCloseDoorCountDownTime.setText("延迟关门倒计时");
            finishRecycle();
        }
    }

    /**
     * 投递完成
     */
    private void finishRecycle(){
        // 关门
        OrderUtils.sendNeedResponseOrder(comHelper, ComConstant.PLASTIC_BOTTLE_RECYCLE_IC_ADDRESS, ComConstant.CLOSE_USER_RECYCLE_ACTION_CODE, null, myHandler, 1);
        // 投递完成按钮设为不可点击
        disableButton(btnRecycle);
    }

    @OnClick(R.id.btn_finish_recycle_plastics_bottle)
    public void finishRecyclePlasticsBottle(){
        // 结束投递
        // 开始投递
        btnContinueRecyclePlasticsBottle.setVisibility(View.GONE);
        btnFinishRecyclePlasticsBottle.setVisibility(View.GONE);
        btnRecycle.setTag(START_RECYCLE);
        enableButton(btnRecycle);
        btnRecycle.setText("开始投递");
        btnRecycle.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.btn_continue_recycle_plastics_bottle)
    public void continueRecyclePlasticsBottle(){
        // 继续投递
        // 开门、投递 -> 投递完成
        btnContinueRecyclePlasticsBottle.setVisibility(View.GONE);
        btnFinishRecyclePlasticsBottle.setVisibility(View.GONE);

        // 发送开门指令
        OrderUtils.sendNeedResponseOrder(comHelper, ComConstant.PLASTIC_BOTTLE_RECYCLE_IC_ADDRESS, ComConstant.OPEN_USER_RECYCLE_ACTION_CODE, null, myHandler, 1);
        // 界面显示开门中，请等待
        tvNotice.setText("开门中，请等待!");
        // 开门按钮置为不可点击
        disableButton(btnRecycle);
        btnRecycle.setText("正在努力开门.....");
        btnRecycle.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDataReceived(final ComBean comRecData) {
        new Thread(new Runnable() {
            @Override
            public void run() {
//                String receiverData = CommonUtil.bytesToHexString(comRecData.getbRec()).replace(" ", "").toUpperCase();
//                OrderHandleUtil.handlerReceiveData(receiverData, myHandler);
            }
        }).start();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEventMain(MessageEvent event){

        String receiverMessage = (String) event.getMessage();

        if (MessageEvent.MESSAGE_TYPE_NOTICE.equals(event.getType())){
            ToastHelper.showShortMessage(this, (String) event.getMessage());
        }else if (MessageEvent.MESSAGE_TYPE_OPEN_DOOR_SUCCESS.equals(event.getType())){
            // 界面提示，箱门已打开，请投递, 按钮改变为投递完成
            SerialHelper.waitResults.remove(receiverMessage);
            tvNotice.setText("箱门已打开，请投递.....");
            btnRecycle.setText("投递完成");
            enableButton(btnRecycle);
            btnRecycle.setTag(RECYCLE_COMPLATE);
            // 清空条码扫描结果，准备接受新的扫码
            BarCodeScanUtil.getInstance().clearData();
            // 开启关门倒计时
            resetTimer1(timer1);
        }else if (MessageEvent.MESSAGE_TYPE_OPEN_DOOR_FAILD.equals(event.getType())){
            // 界面提示，开门失败，请联系管理员
            SerialHelper.waitResults.remove(receiverMessage);
            tvNotice.setText("开门失败, 请联系管理员!");
            btnRecycle.setText("开始投递");
            enableButton(btnRecycle);
        }else if (MessageEvent.MESSAGE_TYPE_RESET_CLOSE_DOOR_COUNTDOWN.equals(event.getType())){
            // 重置关门倒计时
            resetTimer1(timer1);
        }else if (MessageEvent.MESSAGE_TYPE_REQUEST_SCAN_RESULT.equals(event.getType())){
            // IC 请求扫码结果 校验扫码结果
            Boolean result = BarCodeScanUtil.getInstance().validateScanResult();
            // 向 IC 返回扫码结果
            OrderUtils.sendOrder(comHelper, ComConstant.PLASTIC_BOTTLE_RECYCLE_IC_ADDRESS, ComConstant.BAR_CODE_SCAN_VALIDATE_RESULT_ACTION_CODE, result ? "FFFF" : "0000", myHandler, 1);
            // 投递完成按钮，置为不可点击
            disableButton(btnRecycle);
            resetTimer1(timer1);
            if (result){
                // 扫码成功
                // 开启一个延时任务，X 秒后仍未收到光电 3 的投递完成信息，结算本次投递 ???
                if (PLASTIC_RECYCLE_VALIDATE_DELAY_TASK != null){
                    myHandler.removeCallbacks(PLASTIC_RECYCLE_VALIDATE_DELAY_TASK);
                    PLASTIC_RECYCLE_VALIDATE_DELAY_TASK = null;
                }
                PLASTIC_RECYCLE_VALIDATE_DELAY_TASK = getPlasticRecycleValidateDelayTask();
                myHandler.postDelayed(PLASTIC_RECYCLE_VALIDATE_DELAY_TASK, PLASTIC_RECYCLE_VALIDATE_RESULT_DELAY_TIME);
            }else {
                // 扫码失败
                // 界面提示，倒计时显示
                String notice = "瓶子条码检测失败，请在 30 秒内从投放口取出瓶子，不然会被回收机吞掉哦!";
                tvNotice.setText(notice);
                timer1.cancel();
                tvCloseDoorCountDownTime.setText("延迟关门倒计时");
                // 如果失败，开启一个 30 秒的延迟没收任务
                resetTimer2(timer2);
            }
        }else if (MessageEvent.MESSAGE_TYPE_IC_NOT_RECEIVE_SCAN_RESULT.equals(event.getType())){
            // IC 未收到扫码结果，做扫码失败处理
            // 界面提示，倒计时显示
            String notice = "瓶子条码检测失败，请在 30 秒内从投放口取出瓶子，不然会被回收机吞掉哦!";
            tvNotice.setText(notice);
            // 如果失败，开启一个 30 秒的延迟没收任务
            timer1.cancel();
            tvCloseDoorCountDownTime.setText("延迟关门倒计时");
            resetTimer2(timer2);
        }else if (MessageEvent.MESSAGE_TYPE_RECYCLE_BRIEF_SUMMARY.equals(event.getType())){
            // 光电 3
            // 光电 2 成功取回 -> 区分光电 2 与 光电 3
            // 光电 2 触发，且成功后，开启延时，延时触发仍未收到光电 3
            String param = (String) event.getMessage();
            String notice = "";
            // 判断是否触发了光电 3，触发了表示有物品投入，未触发表示没有
            if ("0000".equals(param)){
                // 触发了光电 3，表示有物品投入
                // 移除延时任务
                myHandler.removeCallbacks(PLASTIC_RECYCLE_VALIDATE_DELAY_TASK);
                // 检查扫码结果
                TOTAL_PLASTICS_BOTTLE++;
                if (BarCodeScanUtil.getInstance().validateScanResult()){
                    // 移除第一个扫码结果，有效投递数 + 1
                    BarCodeScanUtil.getInstance().consumeScanResult();
                    CURRENT_PLASTICS_BOTTLE++;
                    TOTAL_VALID_PLASTICS_BOTTLE++;
                }
                notice = "已投递" + CURRENT_PLASTICS_BOTTLE + "个";
            }else if ("0001".equals(param)){
                // 扫码失败，用户成功取回物品
                timer2.cancel();
                tvFetchCountDownTime.setText("投递物取回倒计时");
                resetTimer1(timer1);
                notice = "投递物取回倒计时---已投递" + CURRENT_PLASTICS_BOTTLE + "个";
            }else if ("0002".equals(param)){
                // 扫码成功，延时任务触发，仍未收到光电 3 信号
                // 一种是 Android 自己的延时，一种是 IC 触发的延时
                if (PLASTIC_RECYCLE_VALIDATE_DELAY_TASK != null){
                    myHandler.removeCallbacks(PLASTIC_RECYCLE_VALIDATE_DELAY_TASK);
                    PLASTIC_RECYCLE_VALIDATE_DELAY_TASK = null;
                }
                notice = "扫码成功，接收光电 3 信号超时---已投递" + CURRENT_PLASTICS_BOTTLE + "个";
            }else if ("0003".equals(param)){
                // 强制回收成功
                btnRecycle.setVisibility(View.GONE);
                // 显示结束投递、继续投递按钮
                btnFinishRecyclePlasticsBottle.setVisibility(View.VISIBLE);
                btnContinueRecyclePlasticsBottle.setVisibility(View.VISIBLE);
                // 总结算 开门 - 关门
                notice = "强制回收成功-----已投递饮料瓶" + CURRENT_PLASTICS_BOTTLE + "个!";
                tvNotice.setText(notice);
                // 清空累计投递数 开门 - 关门
                CURRENT_PLASTICS_BOTTLE = 0;
                return;
            }else if ("0004".equals(param)){
                // 强制回收失败
                btnRecycle.setVisibility(View.GONE);
                // 显示结束投递、继续投递按钮
                btnFinishRecyclePlasticsBottle.setVisibility(View.VISIBLE);
                btnContinueRecyclePlasticsBottle.setVisibility(View.VISIBLE);
                // 总结算 开门 - 关门
                notice = "强制回收失败-----已投递饮料瓶" + CURRENT_PLASTICS_BOTTLE + "个!";
                tvNotice.setText(notice);
                // 清空累计投递数 开门 - 关门
                CURRENT_PLASTICS_BOTTLE = 0;
                return;
            }
            tvNotice.setText(notice);
            enableButton(btnRecycle);
        }else if (MessageEvent.MESSAGE_TYPE_CLOSE_DOOR_SUCCESS.equals(event.getType())){
            // 关门成功
            SerialHelper.waitResults.remove(event.getMessage());
            if (StringUtil.isNotEmpty((String) event.getExtra())){
                // 强制回收前置关门操作
                // 发送强制回收指令
                OrderUtils.sendNeedResponseOrder(comHelper, ComConstant.PLASTIC_BOTTLE_RECYCLE_IC_ADDRESS, ComConstant.FORCE_RECYCLE_ACTION_CODE, null, myHandler, 1);
            }else {
                btnRecycle.setVisibility(View.GONE);
                // 显示结束投递、继续投递按钮
                btnFinishRecyclePlasticsBottle.setVisibility(View.VISIBLE);
                btnContinueRecyclePlasticsBottle.setVisibility(View.VISIBLE);
                // 总结算 开门 - 关门
                String notice = "投递成功，已投递饮料瓶" + CURRENT_PLASTICS_BOTTLE + "个!";
                tvNotice.setText(notice);
                // 清空累计投递数 开门 - 关门
                CURRENT_PLASTICS_BOTTLE = 0;
            }
            // 清空扫码结果
            BarCodeScanUtil.getInstance().clearData();
        }else if (MessageEvent.MESSAGE_TYPE_CLOSE_DOOR_FAILD.equals(event.getType())){
            // TODO: 2018/12/24 关门失败处理 界面提示
            SerialHelper.waitResults.remove(event.getMessage());
            tvNotice.setText("关门失败，请联系管理员!");
        }else if (MessageEvent.MESSAGE_TYPE_FORCE_RECYCLE_SUCCESS.equals(event.getType())){
            // 强制回收成功
            EventBus.getDefault().post(new MessageEvent("0003", MessageEvent.MESSAGE_TYPE_RECYCLE_BRIEF_SUMMARY));
            SerialHelper.waitResults.remove(event.getMessage());
        }else if (MessageEvent.MESSAGE_TYPE_FORCE_RECYCLE_FAILD.equals(event.getType())){
            // 强制回收失败
            EventBus.getDefault().post(new MessageEvent("0004", MessageEvent.MESSAGE_TYPE_RECYCLE_BRIEF_SUMMARY));
            SerialHelper.waitResults.remove(event.getMessage());
        }
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
    protected void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().register(this);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (comHelper != null){
            comHelper.closeComPort();
        }
    }

    public static void logMessage(String tag, String message){
        Log.d(tag, message);
//        EventBus.getDefault().post(new MessageEvent(tag + ": " + message, MessageEvent.MESSAGE_TYPE_LOG_VIEW));
    }

    private void initCloseDoorTimer(){
        timer1 = new CountDownTimer(countDown, 1000L) {
            @Override
            public void onTick(long millisUntilFinished) {
                String s = "关门倒计时: " + String.valueOf(millisUntilFinished / 1000);
                tvCloseDoorCountDownTime.setText(s);
            }

            @Override
            public void onFinish() {
                // 延时完毕, 投递完成
                tvCloseDoorCountDownTime.setText("延迟关门倒计时");
                finishRecycle();
            }
        };
    }

    private void initFetchTimer(){
        timer2 = new CountDownTimer(countDown, 1000L) {
            @Override
            public void onTick(long millisUntilFinished) {
                String s = "强制回收倒计时: " + String.valueOf(millisUntilFinished / 1000);
                tvFetchCountDownTime.setText(s);
            }

            @Override
            public void onFinish() {
                // 延时完毕
                // 强制回收未收回的投递物
                // 发送强制回收前置关门操作 -> 关门成功后再发送强制回收指令
                tvFetchCountDownTime.setText("投递物取回倒计时");
                OrderUtils.sendNeedResponseOrder(comHelper, ComConstant.PLASTIC_BOTTLE_RECYCLE_IC_ADDRESS, ComConstant.CLOSE_USER_RECYCLE_ACTION_CODE, "0001", myHandler, 1);
            }
        };
    }

    private void resetTimer1(CountDownTimer timer){
        if (timer == null){
            initCloseDoorTimer();
        }
        // 重置倒计时
        timer.cancel();
        timer.start();
    }

    private void resetTimer2(CountDownTimer timer){
        if (timer == null){
            initFetchTimer();
        }
        // 重置倒计时
        timer.cancel();
        timer.start();
    }

    /**
     * 回收成功确认(光电 3 是否触发)
     * @return
     */
    private static Runnable getPlasticRecycleValidateDelayTask(){
        return new Runnable() {
            @Override
            public void run() {
                // 收到光电 2 触发后，计时 2 秒，如果还是没有收到光电 3 信号，做结算处理！
                EventBus.getDefault().post(new MessageEvent("0002", MessageEvent.MESSAGE_TYPE_RECYCLE_BRIEF_SUMMARY));
            }
        };
    }
}
