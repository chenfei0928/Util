package io.github.chenfei0928.util;

import java.util.Locale;

import androidx.annotation.CheckResult;
import androidx.annotation.DoNotInline;
import androidx.annotation.NonNull;
import io.github.chenfei0928.annotation.NoSideEffects;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

/**
 * @author ChenFei(chenfei0928 @ gmail.com)
 * @date 2021-01-05 17:54
 */
public class StackTraceLogUtil {
    private static final String TAG = "KW_StackTraceLogUtil";

    private StackTraceLogUtil() {
    }

    @NonNull
    @CheckResult
    @DoNotInline
    public static Function1<Throwable, Unit> onError(int level) {
        String strings = generateTags(level);
        return throwable -> {
            Log.w(TAG, strings + "\ndefaultOnErrorHandler: ", throwable);
            return Unit.INSTANCE;
        };
    }

    /**
     * @param level 每嵌套一层方法调用 +1
     * @return 返回格式为 KW_类名 类名.调用方法名(文件名.java:行号)
     */
    @DoNotInline
    public static String generateTags(int level) {
        StackTraceElement caller = Thread.currentThread().getStackTrace()[4 + level];
        String callerClazzName = caller.getClassName();
        callerClazzName = callerClazzName.substring(callerClazzName.lastIndexOf('.') + 1);

        return String.format(Locale.getDefault(), "%s.%s(%s:%d)",
                callerClazzName, caller.getMethodName(),
                caller.getFileName(), caller.getLineNumber());
    }

    /**
     * 打印当前方法调用堆栈，打印到 ActivityThread.main() 根节点为止
     */
    @NoSideEffects
    public static void printStackTrace(String tag) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        StringBuilder sb = new StringBuilder("printStackTrace:");
        for (int i = 3; i < stackTrace.length; i++) {
            if ("android.app.ActivityThread".equals(stackTrace[i].getClassName())
                    && "main".equals(stackTrace[i].getMethodName())) {
                break;
            }
            sb.append('\n').append(stackTrace[i].toString());
        }
        Log.v(tag, sb.toString());
    }
}
