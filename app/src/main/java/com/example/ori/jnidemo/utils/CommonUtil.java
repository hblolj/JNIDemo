package com.example.ori.jnidemo.utils;

public class CommonUtil {

    /**
     * byte数组转换成16进制字符串
     * @param src
     * @return
     */
    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder();
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    /**
     * byte数组转换成16进制字符数组
     *
     * @param src
     * @return
     */
    public static String[] bytesToHexStrings(byte[] src) {
        if (src == null || src.length <= 0) {
            return null;
        }
        String[] str = new String[src.length];

        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                str[i] = "0";
            }
            str[i] = hv;
        }
        return str;
    }

    /**
     * 16进制的字符串表示转成字节数组
     *
     * @param hexString 16进制格式的字符串
     * @return 转换后的字节数组
     **/
    public static byte[] toByteArray(String hexString) {
        hexString = hexString.toLowerCase();
        final byte[] byteArray = new byte[hexString.length() / 2];
        int k = 0;
        for (int i = 0; i < byteArray.length; i++) {//因为是16进制，最多只会占用4位，转换成字节需要两个16进制的字符，高位在先
            byte high = (byte) (Character.digit(hexString.charAt(k), 16) & 0xff);
            byte low = (byte) (Character.digit(hexString.charAt(k + 1), 16) & 0xff);
            byteArray[i] = (byte) (high << 4 | low);
            k += 2;
        }
        return byteArray;
    }

    //十进制转16进制,一位的话，前面补零
    public static String Ten2Hex(int ten) {
        String hexString = Integer.toHexString(ten);
        //转换后  是奇数位 需要在前面补零
        if (hexString.length() % 2 != 0) {
            hexString = "0" + hexString;
        }

        return hexString;
    }

    //完善十六进制备注信息   20Bit 不足补零
    public static String perfectReMark(String remark) {
        int length = remark.length();
        String perfect = "";
        for (int i = 0; i < 40 - length; i++) {
            perfect = perfect + "0";
        }
        return remark + perfect;
    }

    public static String hexAdd(String hex, Integer addend){
        int iHex = Integer.parseInt(hex, 16);
        int result = iHex + addend;
        return Integer.toHexString(result).toUpperCase();
    }
}
