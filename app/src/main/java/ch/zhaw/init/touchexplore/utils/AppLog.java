package ch.zhaw.init.touchexplore.utils;

import android.util.Log;

public class AppLog{
    public static void e(String TAG, String msg)
    {
         Log.e(TAG,msg);
    }
    public static void i(String TAG, String msg)
    {
        Log.i(TAG,msg);
    }
    public static void d(String TAG, String msg)
    {
        Log.d(TAG,msg);
    }
    public static void v(String TAG, String msg)
    {
        Log.v(TAG,msg);
    }

}