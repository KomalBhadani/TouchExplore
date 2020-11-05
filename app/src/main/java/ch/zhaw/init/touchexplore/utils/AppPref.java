package ch.zhaw.init.touchexplore.utils;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.Map;
import java.util.Set;

public class AppPref {

    private static AppPref sInstance;
    private static SharedPreferences sPref;
    private static SharedPreferences.Editor sEditor;

    public static final String FEATURE_TYPE = "FEATURE_TYPE";


    private AppPref(Context context) {
        sPref = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        sEditor = sPref.edit();
    }

    public static AppPref getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new AppPref(context);
        }
        return sInstance;
    }

    //set methods
    public void set(String key, String value) {
        sEditor.putString(key, value).apply();
    }

    public void set(String key, boolean value) {
        sEditor.putBoolean(key, value).apply();
    }

    public void set(String key, float value) {
        sEditor.putFloat(key, value).apply();
    }

    public void set(String key, int value) {
        sEditor.putInt(key, value).apply();
    }

    public void set(String key, long value) {
        sEditor.putLong(key, value).apply();
    }

    public void set(String key, Set<String> value) {
        sEditor.putStringSet(key, value).apply();
    }

    // get methods
    public int getInt(String key, int defaultVal) {
        return sPref.getInt(key, defaultVal);
    }

    public int getInt(String key) {
        return sPref.getInt(key,0);
    }

    public String getString(String key, String defaultVal) {
        return sPref.getString(key, defaultVal);
    }
    public String getString(String key) {
        return sPref.getString(key, "");
    }


    public boolean getBoolean(String key, boolean defaultVal) {
        return sPref.getBoolean(key, defaultVal);
    }
    public boolean getBoolean(String key) {
        return sPref.getBoolean(key,false);
    }


    public float getFloat(String key, float defaultVal) {
        return sPref.getFloat(key, defaultVal);
    }

    public float getFloat(String key) {
        return sPref.getFloat(key,0);
    }

    public long getLong(String key, long defaultVal) {
        return sPref.getLong(key, defaultVal);
    }

    public long getLong(String key) {
        return sPref.getLong(key, 0);
    }

    public Set<String> getStringSet(String key) {
        return sPref.getStringSet(key, null);
    }

    public boolean contains(String key) {
        return sPref.contains(key);
    }

    public void remove(String key) {
        sEditor.remove(key);
    }

    public Map<String, ?> getAll() {
        return sPref.getAll();
    }


}