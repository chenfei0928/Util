package io.github.chenfei0928.content.res

import android.content.ContentResolver
import android.content.res.Resources
import androidx.annotation.AnyRes

object ResUriUtil {
    @JvmStatic
    fun getResUri(context: Resources, @AnyRes id: Int) =
        ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                context.getResourcePackageName(id) + "/" +
                context.getResourceTypeName(id) + "/" +
                context.getResourceEntryName(id)

    @JvmStatic
    fun getResUri2(context: Resources, @AnyRes id: Int) =
        ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                context.getResourcePackageName(id) + "/" + id

    @JvmStatic
    fun getAssetFileUri(assetFileName: String) =
        "file:///android_asset/$assetFileName"
}
