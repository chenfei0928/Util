package io.github.chenfei0928.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

/**
 * @author ChenFei(chenfei0928 @ gmail.com)
 * @date 2019-08-12 16:28
 */
public class LhIntentUrlUtil {

    /**
     * 安全的启动一个activity
     *
     * @param context 会话上下文Activity，要求短时间内不会被finish
     * @param intent  要求打开的intent
     *                intent为第三方应用时会提示是否打开的提示，此时有可能会被用户取消，只能通过callback接收并处理
     * @return false为该intent无效（找不到跳转目标），true为该intent是有效intent，此处会处理。
     */
    static boolean safeStartActivity(@NonNull Context context, @NonNull Intent intent) {
        // 与本应用包名相同
        if (!(context instanceof Activity)) {
            // 当前context不是Activity，需添加newTask标记才能打开intent
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        try {
            context.startActivity(intent);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
