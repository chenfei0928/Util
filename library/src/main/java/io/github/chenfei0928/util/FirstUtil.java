package io.github.chenfei0928.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.text.format.DateUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.github.chenfei0928.content.ContextKt;

/**
 * 记录首次启动的帮助类
 *
 * @author MrFeng
 * @date 2016/8/29
 */
public class FirstUtil {
    private static final String SP_NAME = "first";
    /**
     * 可重复使用的，根据md5值判断是否本次md5已经被消费的事件前缀
     */
    public static final String EVENT_PREFIX_STRING = "strEvent_";
    /**
     * 使用一次就失效的首次使用判断
     */
    public static final String EVENT_PREFIX_NORMAL = "norEvent_";
    /**
     * 用于处理在某个时间节点后多长时间后重新判断为未处理事件时使用的前缀
     */
    public static final String EVENT_PREFIX_TIME_INTERVAL = "timeInterval_";
    /**
     * 用于配合字符串事件保存当前事件的md5，判断事件是否被消费
     */
    private static final String EVENT_STRING_SUFFIX_NEW = "_suffixNew";
    /**
     * 用于配合字符串事件保存上次事件的md5，判断事件是否被消费
     */
    private static final String EVENT_STRING_SUFFIX_PRE = "_suffixPre";

    @IntDef({EventType.EVENT_NORMAL, EventType.EVENT_STRING})
    @Retention(RetentionPolicy.SOURCE)
    private @interface EventType {
        int EVENT_NORMAL = 0;
        int EVENT_STRING = 1;
    }

    private static SharedPreferences getSP(Context context) {
        return context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
    }

    @EventType
    private static int checkEventType(@NonNull String event) {
        if (event.startsWith(EVENT_PREFIX_STRING)) {
            return EventType.EVENT_STRING;
        } else if (event.startsWith(EVENT_PREFIX_NORMAL)) {
            return EventType.EVENT_NORMAL;
        } else {
            throw new IllegalArgumentException("事件必须有类型前缀：" + event);
        }
    }

    /**
     * 校验字符串事件名
     *
     * @param event 事件名
     */
    private static void checkStringEvent(@NonNull String event) {
        if (event.endsWith(EVENT_STRING_SUFFIX_PRE) || event.endsWith(EVENT_STRING_SUFFIX_NEW)) {
            throw new IllegalArgumentException(
                    "事件名不能以 " + EVENT_STRING_SUFFIX_PRE + " 或 " + EVENT_STRING_SUFFIX_NEW + " 结尾");
        }
    }

    /**
     * 设置一个普通事件为未被消费
     *
     * @param context 上下文
     * @param event   事件名
     */
    public static void setNormalFirst(@NonNull Context context, @NonNull String event) {
        checkStringEvent(event);
        if (EventType.EVENT_NORMAL == checkEventType(event)) {
            getSP(context).edit().putBoolean(event, true).apply();
        } else {
            throw new IllegalArgumentException(
                    "setNormalFirst 方法只能接受 " + EVENT_PREFIX_NORMAL + " 事件: " + event);
        }
    }

    /**
     * 判断事件是否未消费
     *
     * @param context 上下文
     * @param event   事件名
     * @return 如果事件未消费
     */
    public static boolean isFirst(@NonNull Context context, @NonNull String event) {
        checkStringEvent(event);
        SharedPreferences sp = getSP(context);
        switch (checkEventType(event)) {
            case EventType.EVENT_NORMAL:
                return sp.getBoolean(event, true);
            case EventType.EVENT_STRING:
                // 获取当前的事件md5
                String eventNew = sp.getString(event + EVENT_STRING_SUFFIX_NEW, "");
                // 获取上次事件md5
                String eventPre = sp.getString(event + EVENT_STRING_SUFFIX_PRE, null);
                // 如果事件md5不为空且不相等，则为未消费该事件
                return !TextUtils.isEmpty(eventNew) && !eventNew.equals(eventPre);
            default:
                return false;
        }
    }

    /**
     * 保存一个事件已经消费
     *
     * @param context 上下文
     * @param event   事件名
     */
    public static void saveLaunched(@NonNull Context context, @NonNull String event) {
        checkStringEvent(event);
        SharedPreferences sp = getSP(context);
        switch (checkEventType(event)) {
            case EventType.EVENT_NORMAL:
                sp.edit().putBoolean(event, false).apply();
                break;
            case EventType.EVENT_STRING:
                // 保存进本地
                saveStringEvent(sp, event, sp.getString(event + EVENT_STRING_SUFFIX_NEW, ""));
                break;
            default:
        }
    }

    /**
     * 保存字符串事件
     *
     * @param context  上下文
     * @param event    事件类型
     * @param eventNew 事件的md5
     */
    public static void saveStringEvent(@NonNull Context context, @NonNull String event, @Nullable String eventNew) {
        checkStringEvent(event);
        SharedPreferences sp = getSP(context);
        // 获取本地保存的最后的事件md5
        String spLatest = sp.getString(event + EVENT_STRING_SUFFIX_NEW, null);

        // 如果新的事件md5与本地保存的不一致，将事件更新
        if (!TextUtils.isEmpty(eventNew) && !eventNew.equals(spLatest)) {
            saveStringEvent(sp, event, eventNew);
        }
    }

    /**
     * 对一个键值对的事件进行更新保存
     *
     * @param sp       首选项保存
     * @param event    事件类型
     * @param eventNew 事件 id
     */
    private static void saveStringEvent(@NonNull SharedPreferences sp, @NonNull String event, @NonNull String eventNew) {
        SharedPreferences.Editor edit = sp.edit();
        // 获取上一次保存的事件id
        String eventPre = sp.getString(event + EVENT_STRING_SUFFIX_NEW, "");
        // 保存本次事件的id
        edit.putString(event + EVENT_STRING_SUFFIX_NEW, eventNew);
        // 将上次保存的事件id保存进上次事件保存的位置
        edit.putString(event + EVENT_STRING_SUFFIX_PRE, eventPre);
        edit.apply();
    }

    public static int getPreAppVersion(Context context) {
        return getSP(context).getInt("preAppVersion", -1);
    }

    public static void saveAppVersion(Context context) {
        saveAppVersion(context, ContextKt.getPackageInfo(context).versionCode);
    }

    public static void saveAppVersion(Context context, int versionCode) {
        getSP(context).edit().putInt("preAppVersion", versionCode).apply();
    }

    /**
     * 以一个时间间隔作为判断依据的判断两次事件是否超时
     *
     * @param context      上下文
     * @param event        事件类型
     * @param timeInterval 时间间隔，以多长时间的时间间隔作为判断为已经超时，0为当天
     * @return 如果事件已经超时
     */
    public static boolean needShowWhenTimeInterval(@NonNull Context context, @NonNull String event, long timeInterval) {
        if (!event.startsWith(EVENT_PREFIX_TIME_INTERVAL)) {
            throw new IllegalArgumentException("事件必须有类型前缀：" + event);
        }
        // 该事件本地保存的上次触发时间
        long preTime = getSP(context).getLong(event, 0);
        // 本次触发事件
        long currentTime = System.currentTimeMillis();
        // 将本次触发事件时间持久化保存
        getSP(context).edit().putLong(event, currentTime).apply();
        if (timeInterval == 0) {
            return !DateUtils.isToday(preTime);
        } else {
            return currentTime - preTime > timeInterval;
        }
    }

    public static void clear(Context context) {
        getSP(context).edit().clear().apply();
    }
}
