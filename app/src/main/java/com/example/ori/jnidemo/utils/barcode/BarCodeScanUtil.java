package com.example.ori.jnidemo.utils.barcode;

import android.util.Log;
import android.view.KeyEvent;

import com.example.ori.jnidemo.utils.StringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author hblolj
 * @description 条码扫描工具类
 */
public class BarCodeScanUtil {

    private static final String TAG = BarCodeScanUtil.class.getSimpleName();

//    public static final String DEVICE_NAME = "USB Barcode Scanner USB Barcode Scanner";
    public static final String DEVICE_NAME = "二维条码枪 2D BarCode Scanner";

    private static Map<Integer, String> barCodeDictionary = new HashMap<>();

    private StringBuffer buffer = new StringBuffer();

    private List<String> scanResult = new ArrayList<>();

    private Boolean mCaps;

    private static BarCodeScanUtil instance;

    public static BarCodeScanUtil getInstance() {
        if (instance == null){
            synchronized (BarCodeScanUtil.class){
                if (instance == null){
                    instance = new BarCodeScanUtil();
                }
            }
        }
        return instance;
    }

    private BarCodeScanUtil() {

        barCodeDictionary.put(29, "a");
        barCodeDictionary.put(30, "b");
        barCodeDictionary.put(31, "c");
        barCodeDictionary.put(32, "d");
        barCodeDictionary.put(33, "e");
        barCodeDictionary.put(34, "f");
        barCodeDictionary.put(35, "g");
        barCodeDictionary.put(36, "h");
        barCodeDictionary.put(37, "i");
        barCodeDictionary.put(38, "j");
        barCodeDictionary.put(39, "k");
        barCodeDictionary.put(40, "l");
        barCodeDictionary.put(41, "m");
        barCodeDictionary.put(42, "n");
        barCodeDictionary.put(43, "o");
        barCodeDictionary.put(44, "p");
        barCodeDictionary.put(45, "q");
        barCodeDictionary.put(46, "r");
        barCodeDictionary.put(47, "s");
        barCodeDictionary.put(48, "t");
        barCodeDictionary.put(49, "u");
        barCodeDictionary.put(50, "v");
        barCodeDictionary.put(51, "w");
        barCodeDictionary.put(52, "x");
        barCodeDictionary.put(53, "y");
        barCodeDictionary.put(54, "z");
        barCodeDictionary.put(69, "-");
    }

    public void clearBuffer(){
        if (buffer != null){
            buffer.delete(0, BarCodeScanUtil.getInstance().buffer.length());
        }
    }

    public void clearData(){
        clearBuffer();
        scanResult.clear();
    }

    public void saveScanResult(){
        String result = BarCodeScanUtil.getInstance().buffer.toString();
        Log.d(TAG, "条码扫描结果: " + result);
        if (StringUtil.isNotEmpty(result)){
            scanResult.add(result);
        }
        clearBuffer();
    }

    public Boolean validateScanResult(){
        if (scanResult != null){
            return scanResult.size() > 0;
        }
        return false;
    }

    public void consumeScanResult(){
        // TODO: 2019/1/15 持久化记录回收成功的条码
        scanResult.remove(0);
    }

    /**
     * 检查shift键, 判断大小写
     * @param event
     */
    public void checkLetterStatus(KeyEvent event) {
        int keyCode = event.getKeyCode();
        if (keyCode == KeyEvent.KEYCODE_SHIFT_RIGHT || keyCode == KeyEvent.KEYCODE_SHIFT_LEFT) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                //按着shift键，表示大写
                mCaps = true;
            } else {
                //松开shift键，表示小写
                mCaps = false;
            }
        }
    }

    /**
     * 根据keycode得到对应的字母和数字
     * @param keycode
     */
    public void keyCodeToNum(int keycode) {

        if (keycode >= KeyEvent.KEYCODE_A && keycode <= KeyEvent.KEYCODE_Z) {
            if (mCaps) {
                buffer.append(barCodeDictionary.get(keycode).toUpperCase());
            } else {
                buffer.append(barCodeDictionary.get(keycode));
            }
        } else if ((keycode >= KeyEvent.KEYCODE_0 && keycode <= KeyEvent.KEYCODE_9)) {
            buffer.append(keycode - KeyEvent.KEYCODE_0);
        } else {
            //暂不处理特殊符号
            if (barCodeDictionary.get(keycode) != null){
                buffer.append(barCodeDictionary.get(keycode));
            }
        }
    }
}
