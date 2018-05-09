package com.chenfei.library.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Looper;
import android.text.TextUtils;

/**
 * Created by MrFeng on 2017/9/19.
 */
public class CrashHandler implements Thread.UncaughtExceptionHandler {
    private static final String SP_NAME = "crashHandler";
    private static final String SP_HAS_CRASH = "hasCrash";
    private static final String SP_FULL_FILE_PATH = "fullFilePath";
    private static final String SP_EX_FILE_PATH = "exFilePath";
    private final Thread.UncaughtExceptionHandler uncaughtHandler;
    private final Context context;

    private CrashHandler(Context context) {
        this.context = context;
        if (Looper.getMainLooper().getThread().getUncaughtExceptionHandler() != this) {
            this.uncaughtHandler = Looper.getMainLooper().getThread().getUncaughtExceptionHandler();
        } else {
            uncaughtHandler = null;
        }
    }

    public static void setUp(Context context) {
        Looper.getMainLooper().getThread().setUncaughtExceptionHandler(new CrashHandler(context));
    }

    public static boolean hasCrash(Context context) {
        SharedPreferences sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        return sp.getBoolean(SP_HAS_CRASH, false)
                && !TextUtils.isEmpty(getFullLogFilePath(context))
                && !TextUtils.isEmpty(getExLogFilePath(context));
    }

    public static String getFullLogFilePath(Context context) {
        SharedPreferences sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        return sp.getString(SP_FULL_FILE_PATH, null);
    }

    public static String getExLogFilePath(Context context) {
        SharedPreferences sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        return sp.getString(SP_EX_FILE_PATH, null);
    }

    public static void clear(Context context) {
        context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE).edit().clear().apply();
    }

    @Override
    @SuppressLint("ApplySharedPref")
    public void uncaughtException(Thread thread, Throwable e) {
        SharedPreferences.Editor ed = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE).edit();
        ed.putBoolean(SP_HAS_CRASH, true);
        ed.putString(SP_FULL_FILE_PATH, FileLogUtil.getInstance(context).getFullLogFileName());
        ed.putString(SP_EX_FILE_PATH, FileLogUtil.saveExceptionToLog(context, e));
        // save now!
        ed.commit();
        if (uncaughtHandler != null) {
            uncaughtHandler.uncaughtException(thread, e);
        }
    }
}
