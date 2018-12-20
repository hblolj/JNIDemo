package com.example.ori.jnidemo.utils.crc;

import com.example.ori.jnidemo.utils.CommonUtil;

/**
 * @author hblolj
 */
public class CRCUtils {

    public static String getCRC(byte[] bytes) {

        int crc = 0x1919;
        for (byte b : bytes) {
            crc = (crc << 8) ^ CRCTab.crc_tab[((crc >>> 8) ^ b) & 0xff];
        }
        byte[] b = new byte[2];
        b[0] = (byte) (crc >> 8);
        b[1] = (byte) (crc >> 0);

        return CommonUtil.bytesToHexString(b);
    }

}
