package com.example.ori.jnidemo.utils;

import android.content.Context;
import android.widget.Toast;

public class ToastHelper {

    public static void showLongMessage(Context context, String message){
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    public static void showShortMessage(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
