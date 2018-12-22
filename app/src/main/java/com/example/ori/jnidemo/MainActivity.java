package com.example.ori.jnidemo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.ori.jnidemo.bean.ComBean;
import com.example.ori.jnidemo.bean.MessageEvent;
import com.example.ori.jnidemo.bean.Order;
import com.example.ori.jnidemo.bean.OrderValidate;
import com.example.ori.jnidemo.constant.ComConstant;
import com.example.ori.jnidemo.interfaces.ComDataReceiverInterface;
import com.example.ori.jnidemo.utils.CommonUtil;
import com.example.ori.jnidemo.utils.OrderHandleUtil;
import com.example.ori.jnidemo.utils.StringUtil;
import com.example.ori.jnidemo.utils.TimeUtils;
import com.example.ori.jnidemo.utils.ToastHelper;
import com.example.ori.jnidemo.utils.barcode.BarCodeScanUtil;
import com.example.ori.jnidemo.utils.serial_port.SerialHelper;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ComDataReceiverInterface{

    private static final String TAG = MainActivity.class.getSimpleName();

    private SerialHelper comHelper;

    private Button btnSend;

    private TextView tvResult;

    private TextView tvLog;

    private Spinner spTargetAddress;

    private Spinner spActionCode;

    private String targetAddress;

    private String actionCode;

    private TextView tvClearSampleLog;

    private TextView tvClearOrderLog;

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
            logMessage(TAG, "handleMessage: waitReplys.Size: " + SerialHelper.waitReplys.size());
            OrderValidate orderValidate = SerialHelper.waitReplys.get(key);
            if (orderValidate == null){
                // 已经收到了响应
                logMessage(TAG, "handleMessage: 收到了响应指令 -> WaitContent: " + key);
                return true;
            }

            // 2. 判断重试次数，如果已经超过 Max 重试次数，记录指令发送失败
            Integer retryCount = orderValidate.getRetryCount();
            if (retryCount >= ComConstant.MAX_RETRY_COUNT){
                logMessage(TAG, "handleMessage: 指令发送失败 -> content: " + orderValidate.getSendOrder().getOrderContent());
                SerialHelper.waitReplys.remove(orderValidate.getWaitReceiverOrder().getOrderContent());
                return false;
            }

            // 3. 如果没有超过重试次数，且没有收到响应，重新发送指令
            comHelper.sendHex(orderValidate.getSendOrder(), orderValidate.getWaitReceiverOrder(), retryCount+1, myHandler);
            logMessage(TAG, "handleMessage: 指令重发 -> SendContent: " + orderValidate.getSendOrder().getOrderContent() +
                    " WaitContent: " + orderValidate.getWaitReceiverOrder().getOrderContent());
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initPort();
        initViews();
        initDatas();

        for (int id : InputDevice.getDeviceIds()) {
            String name = InputDevice.getDevice(id).getName().trim();
            logMessage("DeviceNames", name);
        }
    }

    /**
     * 初始化连接串口
     */
    private void initPort() {
        comHelper = new SerialHelper("/dev/ttyUSB20", "9600", this);
        comHelper.openComPort();
    }

    private void initDatas() {

        tvClearOrderLog.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                // 弹出 Dialog
                showNormalDialog(1);
                return false;
            }
        });

        tvClearSampleLog.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                showNormalDialog(2);
                return false;
            }
        });

        tvResult.setMovementMethod(ScrollingMovementMethod.getInstance());
        tvLog.setMovementMethod(ScrollingMovementMethod.getInstance());

        final List<String> address = Arrays.asList("塑料回收机", "金属回收机", "纸类回收机");
        ArrayAdapter<String> addressAdapter = new ArrayAdapter<>(this, R.layout.my_spinner_item, address);
        addressAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spTargetAddress.setAdapter(addressAdapter);
        spTargetAddress.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String result = address.get(i);
                targetAddress = ComConstant.getAddressCodeByName(result);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                targetAddress = null;
            }
        });

        final List<String> actionCodes = Arrays.asList("开用户回收门", "关用户回收门", "开管理员回收门", "关管理员回收门", "称重");
        ArrayAdapter<String> actionCodeAdapter = new ArrayAdapter<>(this, R.layout.my_spinner_item, actionCodes);
        addressAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spActionCode.setAdapter(actionCodeAdapter);
        spActionCode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String result = actionCodes.get(i);
                actionCode = ComConstant.getActionCodeByName(result);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                actionCode = null;
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    if (StringUtil.isNotEmpty(targetAddress) && StringUtil.isNotEmpty(actionCode)){
                        Order sendOrder = new Order(ComConstant.ANDROID_ADDRESS, targetAddress, actionCode, Order.DEFAULT_ORDER_PARAM);
                        Order waitReceiveOrder = new Order(targetAddress, ComConstant.ANDROID_ADDRESS, actionCode, Order.DEFAULT_ORDER_PARAM);
                        comHelper.sendHex(sendOrder, waitReceiveOrder, 1, myHandler);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

    private void initViews() {
        btnSend = findViewById(R.id.btn_send);
        tvResult = findViewById(R.id.tv_result);
        tvLog = findViewById(R.id.tv_log);
        spTargetAddress = findViewById(R.id.sp_targetAddress);
        spActionCode = findViewById(R.id.sp_actionCode);
        tvClearSampleLog = findViewById(R.id.tv_clearSampleLog);
        tvClearOrderLog = findViewById(R.id.tv_clearOrderLog);
    }

    @Override
    public void onDataReceived(final ComBean comRecData) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: receiveTime");
                String receiverData = CommonUtil.bytesToHexString(comRecData.getbRec()).replace(" ", "").toUpperCase();
                OrderHandleUtil.handlerReceiveData(receiverData);
                logMessage("onDataReceived: waitReplys.Size", SerialHelper.waitReplys.size() + "");
                EventBus.getDefault().post(new MessageEvent(receiverData, MessageEvent.MESSAGE_TYPE_RECEIVER_VIEW));
            }
        }).start();
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onMessageEvent(MessageEvent event){
        String receiverMessage = event.getMessage();
        if (MessageEvent.MESSAGE_TYPE_NOTICE.equals(event.getType())){
            ToastHelper.showShortMessage(this, event.getMessage());
            return;
        }else if (MessageEvent.MESSAGE_TYPE_RECEIVER_VIEW.equals(event.getType())){
            String r = tvResult.getText().toString();
            receiverMessage = "接收到的消息(" + TimeUtils.date2String(new Date(), TimeUtils.sdf2) + "): " + receiverMessage;
            String s = r + "\n" + receiverMessage;
            Log.d(TAG, "onMessageEvent: " + receiverMessage);
//            tvResult.setText(s);
        }else if (MessageEvent.MESSAGE_TYPE_SEND_VIEW.equals(event.getType())){
            String r = tvResult.getText().toString();
            receiverMessage = "发送的消息(" + TimeUtils.date2String(new Date(), TimeUtils.sdf2) + "): " + receiverMessage;
            String s = r + "\n" + receiverMessage;
            Log.d(TAG, "onMessageEvent: " + receiverMessage);
//            tvResult.setText(s);
        }else if (MessageEvent.MESSAGE_TYPE_LOG_VIEW.equals(event.getType())){
            String r = tvLog.getText().toString();
            String s = r + "\n" + receiverMessage;
            Log.d(TAG, "onMessageEvent: " + receiverMessage);
//            tvLog.setText(s);
        }

    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getDevice().getName().equals(BarCodeScanUtil.DEVICE_NAME)){
            int keyCode = event.getKeyCode();
            logMessage("KeyCodeAndEvent", keyCode + " | " + event.getAction());
            if (keyCode != KeyEvent.KEYCODE_ENTER){
                if (event.getAction() == KeyEvent.ACTION_DOWN){
                    BarCodeScanUtil.getInstance().checkLetterStatus(event);
                    BarCodeScanUtil.getInstance().keyCodeToNum(keyCode);
                }
            }else {
                String scanResult = BarCodeScanUtil.getInstance().buffer.toString();
                logMessage("扫描结果", scanResult);
                BarCodeScanUtil.getInstance().buffer.delete(0, BarCodeScanUtil.getInstance().buffer.length());
                // TODO: 2018/12/19 获取到扫描结果做下一步处理
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
        EventBus.getDefault().post(new MessageEvent(tag + ": " + message, MessageEvent.MESSAGE_TYPE_LOG_VIEW));
    }

    private void showNormalDialog(final Integer type){
        /* @setIcon 设置对话框图标
         * @setTitle 设置对话框标题
         * @setMessage 设置对话框消息提示
         * setXXX方法返回Dialog对象，因此可以链式设置属性
         */
        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(MainActivity.this);
        normalDialog.setTitle(1 == type ? "清除收发指令记录" : "清除日志输出记录!");
        normalDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //...To-do
                        if (1 == type){
                            // 清除收发指令记录
                            tvResult.setText("");
                        }else {
                            tvLog.setText("");
                        }
                    }
                });
        normalDialog.setNegativeButton("关闭",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //...To-do
                    }
                });
        // 显示
        normalDialog.show();
    }
}
