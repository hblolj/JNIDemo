package com.example.ori.jnidemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ori.jnidemo.utils.ComBean;
import com.example.ori.jnidemo.utils.SerialHelper;

import java.io.IOException;
import java.security.InvalidParameterException;

import android_serialport_api.SerialPortFinder;

public class MainActivity extends AppCompatActivity {

    private SerialControl com;

    private Button btnSend;

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        com = new SerialControl();
//        com.setPort("/dev/ttyUSB8");
//        com.setBaudRate("9600");
//        openComPort(com);

        SerialPortFinder finder = new SerialPortFinder();
        String[] devices = finder.getAllDevices();
        Log.d(MainActivity.class.getSimpleName(), "串口列表: " + devices.toString());

        initViews();
    }

    private void initViews() {
        btnSend = findViewById(R.id.btn_send);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                com.sendHex("7777777");
            }
        });
    }


    private void ShowMessage(String sMsg) {
        Toast.makeText(this, sMsg, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeComPort(com);
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    private class SerialControl extends SerialHelper {
        public SerialControl(){
        }

        @Override
        protected void onDataReceived(final ComBean ComRecData) {
        }
    }

    //----------------------------------------------------关闭串口
    private void closeComPort(SerialHelper ComPort){
        if (ComPort!=null){
            ComPort.stopSend();
            ComPort.close();
        }
    }
    //----------------------------------------------------开启串口
    private void openComPort(SerialHelper ComPort){
        try {
            ComPort.open();
        } catch (SecurityException e) {
            ShowMessage("打开串口失败:没有串口读/写权限!");
        } catch (IOException e) {
            ShowMessage("打开串口失败:未知错误!");
        } catch (InvalidParameterException e) {
            ShowMessage("打开串口失败:参数错误!");
        }
    }
}
