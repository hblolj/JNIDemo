package com.example.ori.jnidemo.utils;

import java.util.Locale;

/**
 * @author: hblolj
 * @date: 2019/1/16 13:33
 * @description:
 */
public class LanguageUtil {

    public static Boolean isChinese (){
        return "zh".equals(Locale.getDefault().getLanguage());
    }
}
