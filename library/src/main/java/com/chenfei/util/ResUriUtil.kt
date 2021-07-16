package com.chenfei.util

import android.content.ContentResolver
import android.content.res.Resources
import androidx.annotation.AnyRes

class ResUriUtil {
    companion object {
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
    }
}
