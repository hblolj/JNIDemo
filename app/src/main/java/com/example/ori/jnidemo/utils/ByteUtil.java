package com.example.ori.jnidemo.utils;

import android.util.Log;

import com.example.ori.jnidemo.utils.crc.CRCUtils;

/**
 * @author: hblolj
 * @date: 2019/1/8 11:13
 * @description:
 */
public class ByteUtil {

    private static final String TAG = ByteUtil.class.getSimpleName();

    private static final Byte start = CommonUtil.toByteArray("19")[0];

    private static byte[] temp = new byte[512];

    private static Integer nowIndex = 0;

    private static Integer tLen = 0;

    public static byte[] handlerByte(Byte b){
        if (0 == temp[0]){
            if (start.equals(b)){
                temp[0] = b;
                nowIndex++;
            }
        }else if (0 == temp[1]){
            if (start.equals(b)){
                temp[1] = b;
                nowIndex++;
            }else {
                temp = new byte[512];
                nowIndex = 0;
            }
        }else if (0 == temp[2]){
            // 长度
            byte[] bs = new byte[1];
            bs[0] = b;
            String sLen = CommonUtil.bytesToHexString(bs);
            Integer iLen = Integer.parseInt(sLen, 16);
            tLen = iLen + 2;
            temp[2] = b;
            nowIndex++;
        }else if (tLen - 1 == nowIndex){
            // 添加最后一个元素，指令完整，进行 CRC 校验
            Boolean result = false;
            String r = null;
            if (nowIndex == tLen-1){
                temp[tLen-1] = b;
                r = CommonUtil.bytesToHexString(temp);
                r = r.substring(0, tLen * 2).toUpperCase();
                // CRC 校验，如果通过，进行业务处理
                String crc = r.substring(r.length() - 4);
                String noCrcContent = r.substring(0, r.length() - 4);
                result = CRCUtils.validateOrderCrc(noCrcContent, crc);
                Log.d(TAG, "noCrcContent: " + noCrcContent + " crc: " + crc);
            }
            temp = new byte[512];
            tLen = 0;
            nowIndex = 0;
            if(result){
                return CommonUtil.toByteArray(r);
            }
        }else {
            // 添加进数组
            temp[nowIndex++] = b;
        }

        return null;
    }
}
