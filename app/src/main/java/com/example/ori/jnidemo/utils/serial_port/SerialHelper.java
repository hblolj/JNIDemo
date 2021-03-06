package com.example.ori.jnidemo.utils.serial_port;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.ori.jnidemo.bean.ComBean;
import com.example.ori.jnidemo.bean.MessageEvent;
import com.example.ori.jnidemo.bean.Order;
import com.example.ori.jnidemo.bean.OrderValidate;
import com.example.ori.jnidemo.interfaces.ComDataReceiverInterface;
import com.example.ori.jnidemo.utils.ByteUtil;
import com.example.ori.jnidemo.utils.CommonUtil;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android_serialport_api.SerialPort;


/**
 * @author hblolj
 * 串口辅助工具类
 */
public class SerialHelper{

	public static final String TAG = SerialHelper.class.getSimpleName();

	/**
	 * 待响应指令 key = 待接收的指令
	 */
	public static Map<String, OrderValidate> waitReplys = new ConcurrentHashMap<>();
	/**
	 * 等待执行结果的指令 key = 待接收的指令的发送地址 + 执行结果操作码
	 */
	public static Map<String, Order> waitResults = new ConcurrentHashMap<>();

	private SerialPort mSerialPort;

	private OutputStream mOutputStream;

	private InputStream mInputStream;

	// 数据接收线程
	private ReadThread mReadThread;

	// 数据发送线程
	private SendThread mSendThread;

	// 默认串口地址
	private String sPort = "/dev/ttyUSB";
//	private String sPort = "/dev/ttyUSB20";

	// 默认波特率
	private Integer iBaudRate = 9600;

	private Boolean _isOpen = false;

	// 循环发送数据
	private byte[] _bLoopData = new byte[]{0x30};

	private Integer iDelay = 500;

	private Integer iSendDelay = 100;

	private Integer validateDelay = 500;

	private ComDataReceiverInterface receiverInterface;

	//----------------------------------------------------

	public SerialHelper(String sPort, Integer iBaudRate, ComDataReceiverInterface i){
		this.sPort = sPort;
		this.iBaudRate=iBaudRate;
		this.receiverInterface = i;
	}

	public SerialHelper(ComDataReceiverInterface i){
		this.receiverInterface = i;
	}

	public SerialHelper(String sPort, ComDataReceiverInterface i){
		this(sPort,9600, i);
	}

	public SerialHelper(String sPort, String sBaudRate, ComDataReceiverInterface i){
		this(sPort, Integer.parseInt(sBaudRate), i);
	}

	/**
	 * 开启串口
	 * @param parity 奇偶校验位
	 * @param dataBits 数据位
	 * @param stopBit 停止位
	 * @throws SecurityException
	 * @throws IOException
	 * @throws InvalidParameterException
	 */
	public void open(Integer parity, Integer dataBits, Integer stopBit) throws SecurityException, IOException,InvalidParameterException {
		mSerialPort =  new SerialPort(new File(sPort), iBaudRate, 0, parity, dataBits, stopBit);
		mOutputStream = mSerialPort.getOutputStream();
		mInputStream = mSerialPort.getInputStream();
		mReadThread = new ReadThread();
		mReadThread.start();
		mSendThread = new SendThread();
		mSendThread.setSuspendFlag();
		mSendThread.start();
		_isOpen=true;
	}

	/**
	 * 串口关闭
	 */
	public void close(){
		if (mReadThread != null)
			mReadThread.interrupt();
		if (mSerialPort != null) {
			mSerialPort.close();
			mSerialPort = null;
		}
		_isOpen=false;
	}

	/**
	 * 向串口发送 byte 数组数据
	 * @param bOutArray
	 */
	public void send(byte[] bOutArray) throws IOException, InterruptedException {
        if (mOutputStream == null){
            EventBus.getDefault().post(new MessageEvent("串口尚未连接上!", MessageEvent.MESSAGE_TYPE_NOTICE));
            return;
        }
        mOutputStream.write(bOutArray);
		Thread.sleep(iSendDelay);
	}

	/**
	 * 向串口发送字符串数据
	 * @param sendOrder
	 * @param waitReceiverContent
	 * @param myHandler
	 */
	public void sendHex(Order sendOrder, String waitReceiverContent, Handler myHandler) throws IOException, InterruptedException {
		byte[] bOutArray = CommonUtil.toByteArray(sendOrder.getOrderContent());
		send(bOutArray);
		// 开启一个延迟任务
		Message message = new Message();
		message.obj = waitReceiverContent;
		myHandler.sendMessageDelayed(message, validateDelay);
	}

	/**
	 * 向串口发送 txt 数据
	 * @param sTxt
	 */
	public void sendTxt(String sTxt){
		byte[] bOutArray = sTxt.getBytes();
		try {
			send(bOutArray);
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 串口数据监听线程
	 */
	private class ReadThread extends Thread {
		@Override
		public void run() {
			super.run();
			while(!isInterrupted()) {
				try
				{
					if (mInputStream == null) return;
					byte[] buffer = new byte[1];
					int size = mInputStream.read(buffer);
					// 按单个字节做处理
					byte[] bs = ByteUtil.handlerByte(buffer[0]);
					if (bs != null){
						String s = CommonUtil.bytesToHexString(bs);
						ComBean ComRecData = new ComBean(sPort, s);
						onDataReceived(ComRecData);
					}

//					if (size > 0){
//						ComBean ComRecData = new ComBean(sPort, buffer, size);
//						onDataReceived(ComRecData);
//					}
//					try {
//						Thread.sleep(50);//延时50ms
//					} catch (InterruptedException e) {
//						e.printStackTrace();
//					}
				} catch (Throwable e) {
					e.printStackTrace();
					return;
				}
			}
		}
	}

	/**
	 * 循环向串口发送数据线程
	 */
	private class SendThread extends Thread {
		public boolean suspendFlag = true;// 控制线程的执行
		@Override
		public void run() {
			super.run();
			while(!isInterrupted()) {
				synchronized (this) {
					while (suspendFlag) {
						try {
							wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
				try {
					send(getbLoopData());
					Thread.sleep(iDelay);
				} catch (InterruptedException | IOException e) {
					e.printStackTrace();
				}
			}
		}

		//线程暂停
		public void setSuspendFlag() {
			this.suspendFlag = true;
		}

		//唤醒线程
		public synchronized void setResume() {
			this.suspendFlag = false;
			notify();
		}
	}

	public int getBaudRate() {
		return iBaudRate;
	}

	public boolean setBaudRate(int iBaud) {
		if (_isOpen) {
			return false;
		} else {
			iBaudRate = iBaud;
			return true;
		}
	}

	public boolean setBaudRate(String sBaud) {
		int iBaud = Integer.parseInt(sBaud);
		return setBaudRate(iBaud);
	}

	public String getPort() {
		return sPort;
	}

	public boolean setPort(String sPort) {
		if (_isOpen) {
			return false;
		} else {
			this.sPort = sPort;
			return true;
		}
	}

	public boolean isOpen() {
		return _isOpen;
	}

	public byte[] getbLoopData() {
		return _bLoopData;
	}

	public void setbLoopData(byte[] bLoopData) {
		this._bLoopData = bLoopData;
	}

	public void setTxtLoopData(String sTxt){
		this._bLoopData = sTxt.getBytes();
	}

	public void setHexLoopData(String sHex){
		this._bLoopData = CommonUtil.toByteArray(sHex);
	}

	public int getiDelay() {
		return iDelay;
	}

	public void setiDelay(int iDelay) {
		this.iDelay = iDelay;
	}

	public void startSend() {
		if (mSendThread != null) {
			mSendThread.setResume();
		}
	}

	public void stopSend() {
		if (mSendThread != null) {
			mSendThread.setSuspendFlag();
		}
	}

	public ComDataReceiverInterface getReceiverInterface() {
		return receiverInterface;
	}

	public void setReceiverInterface(ComDataReceiverInterface receiverInterface) {
		this.receiverInterface = receiverInterface;
	}

	/**
	 * 数据接收回调方法
	 * @param comRecData
	 */
	private void onDataReceived(ComBean comRecData){
		receiverInterface.onDataReceived(comRecData);
	}

	/**
	 * 关闭串口
	 */
	public void closeComPort(){
		stopSend();
		close();
	}

	/**
	 * 开启串口
	 */
	public void openComPort(){
		try {
			open(0, 8, 1);
			Log.d(TAG, "连接串口" + this.sPort + "成功!");
		} catch (SecurityException e) {
			EventBus.getDefault().post(new MessageEvent("打开串口失败:没有串口读/写权限!", MessageEvent.MESSAGE_TYPE_NOTICE));
		} catch (IOException e) {
			EventBus.getDefault().post(new MessageEvent("打开串口失败:未知错误!", MessageEvent.MESSAGE_TYPE_NOTICE));
		} catch (InvalidParameterException e) {
			EventBus.getDefault().post(new MessageEvent("打开串口失败:参数错误!", MessageEvent.MESSAGE_TYPE_NOTICE));
		}
	}
}