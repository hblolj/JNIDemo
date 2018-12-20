package com.example.ori.jnidemo.bean;

import java.text.SimpleDateFormat;

/**
 * @author hblolj
 *串口数据
 */
public class ComBean {

	public byte[] bRec = null;

	public String sRecTime = "";

	public String sComPort = "";

	public ComBean() {
	}

	public ComBean(String sPort, byte[] buffer, int size){
		sComPort = sPort;
		bRec = new byte[size];
		for (int i = 0; i < size; i++)
		{
			bRec[i] = buffer[i];
		}
		SimpleDateFormat sDateFormat = new SimpleDateFormat("hh:mm:ss");
		sRecTime = sDateFormat.format(new java.util.Date());
	}

	public byte[] getbRec() {
		return bRec;
	}

	public void setbRec(byte[] bRec) {
		this.bRec = bRec;
	}

	public String getsRecTime() {
		return sRecTime;
	}

	public void setsRecTime(String sRecTime) {
		this.sRecTime = sRecTime;
	}

	public String getsComPort() {
		return sComPort;
	}

	public void setsComPort(String sComPort) {
		this.sComPort = sComPort;
	}
}