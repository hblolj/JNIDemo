package com.example.ori.jnidemo.interfaces;

import com.example.ori.jnidemo.bean.ComBean;

/**
 * @author hblolj
 * @description 串口数据接收接口
 */
public interface ComDataReceiverInterface {

   void onDataReceived(ComBean comRecData);
}
