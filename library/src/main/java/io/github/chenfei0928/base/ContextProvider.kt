package io.github.chenfei0928.base

import android.app.Application
import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import io.github.chenfei0928.widget.ToastUtil

/**
 * @author chenfei(chenfei0928@gmail.com)
 * @date 2022-01-04 13:59
 */
class ContextProvider : ContentProvider() {

    override fun onCreate(): Boolean {
        val appContext = context!!.applicationContext as Application
        ContextProvider.context = appContext
        appContext.registerActivityLifecycleCallbacks(ActivityLifecycleCallback)
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        throw IllegalStateException("Not allowed.")
    }

    override fun getType(uri: Uri): String? {
        throw IllegalStateException("Not allowed.")
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        throw IllegalStateException("Not allowed.")
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        throw IllegalStateException("Not allowed.")
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        throw IllegalStateException("Not allowed.")
    }

    companion object {
        @JvmStatic
        lateinit var context: Application
            private set
    }
}
