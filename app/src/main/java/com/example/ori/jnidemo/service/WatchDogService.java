package com.example.ori.jnidemo.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.example.ori.jnidemo.HomeActivity;
import com.ys.myapi.MyManager;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class WatchDogService extends Service {

    private static final String TAG = WatchDogService.class.getSimpleName();

    private MyManager myManager;

    private Handler myHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            // 喂狗
            Log.d(TAG, "拍照");
//            myManager.watchDogFeedTime();
//            myManager.takeScreenshot(Environment.getExternalStorageDirectory().getPath() + "/DCIM/Camera/" + System.currentTimeMillis() + ".jpg");
//            myHandler.sendMessageDelayed(new Message(), 100);
            return false;
        }
    });

    public WatchDogService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
//        Log.d(TAG, "初始化 service");
//        if (!EventBus.getDefault().isRegistered(this)){
//            Log.d(TAG, "注册 EventBus");
//            EventBus.getDefault().register(this);
//        }
//        myHandler.sendMessageDelayed(new Message(), 100);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        EventBus.getDefault().unregister(this);
//        Log.d(TAG, "注销 EventBus");
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void receiveMessage(MyManager manager){
//        if (this.myManager == null){
//            this.myManager = manager;
//            myManager.setWatchDogEnable(1);
//            Log.d(TAG, "初始化喂狗");
//            myManager.watchDogFeedTime();
//            myHandler.sendMessageDelayed(new Message(), 20000);
//        }
    }
}
