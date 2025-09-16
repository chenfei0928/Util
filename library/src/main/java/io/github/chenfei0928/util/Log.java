package io.github.chenfei0928.util;

import androidx.annotation.Nullable;

/**
 * @author ChenFei(chenfei0928 @ gmail.com)
 * @date 2021-06-02 11:28
 */
public class Log {
    private static LogInterface[] implList = {SystemLog.INSTANCE};
    // log单行大小约4096字节，防止中文日志，此处limit设置为2000
    // logcat -g指令可以读到各个分类的缓冲区上限、目前用量、每次读取量、单条日志长度的上限。
    // system/core/liblog/include/log/log_read.h下的LOGGER_ENTRY_MAX_PAYLOAD和LOGGER_ENTRY_MAX_LEN
    // https://blog.csdn.net/realDonaldTrump/article/details/128468204
    public static int SYSTEM_LOGCAT_SPLIT_LENGTH = 2000;

    private Log() {
    }

    public static void register(LogInterface logger) {
        LogInterface[] newImplList = new LogInterface[implList.length + 1];
        System.arraycopy(implList, 0, newImplList, 0, implList.length);
        implList = newImplList;
        implList[implList.length - 1] = logger;
    }

    public static void v(String tag, String msg) {
        for (LogInterface logInterface : implList) {
            logInterface.v(tag, msg);
        }
    }

    public static void v(String tag, String msg, @Nullable Throwable tr) {
        for (LogInterface logInterface : implList) {
            logInterface.v(tag, msg, tr);
        }
    }

    public static void d(String tag, String msg) {
        for (LogInterface logInterface : implList) {
            logInterface.d(tag, msg);
        }
    }

    public static void d(String tag, String msg, @Nullable Throwable tr) {
        for (LogInterface logInterface : implList) {
            logInterface.d(tag, msg, tr);
        }
    }

    public static void i(String tag, String msg) {
        for (LogInterface logInterface : implList) {
            logInterface.i(tag, msg);
        }
    }

    public static void i(String tag, String msg, @Nullable Throwable tr) {
        for (LogInterface logInterface : implList) {
            logInterface.i(tag, msg, tr);
        }
    }

    public static void w(String tag, String msg) {
        for (LogInterface logInterface : implList) {
            logInterface.w(tag, msg);
        }
    }

    public static void w(String tag, String msg, @Nullable Throwable tr) {
        for (LogInterface logInterface : implList) {
            logInterface.w(tag, msg, tr);
        }
    }

    public static void w(String tag, @Nullable Throwable tr) {
        for (LogInterface logInterface : implList) {
            logInterface.w(tag, "", tr);
        }
    }

    public static void e(String tag, String msg) {
        for (LogInterface logInterface : implList) {
            logInterface.e(tag, msg);
        }
    }

    public static void e(String tag, String msg, @Nullable Throwable tr) {
        for (LogInterface logInterface : implList) {
            logInterface.e(tag, msg, tr);
        }
    }
}
