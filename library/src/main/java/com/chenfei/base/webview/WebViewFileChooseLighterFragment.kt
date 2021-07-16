package com.chenfei.base.webview

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.chenfei.util.Log
import com.chenfei.util.ToastUtil
import com.chenfei.util.kotlin.removeSelf

/**
 * webView 文件选择器启动者（打火器）
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2019-11-30 21:15
 */
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class WebViewFileChooseLighterFragment : Fragment() {
    var filePathCallback: ValueCallback<Array<Uri>>? = null
    var fileChooserParams: WebChromeClient.FileChooserParams? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 如果回调或参数不存在，移除自身
        if (filePathCallback == null || fileChooserParams == null) {
            removeSelf()

        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val fileChooserParams = fileChooserParams
                ?: return
        val intent = fileChooserParams.createIntent()
        try {
            startActivityForResult(intent, REQUEST_CODE)
        } catch (e: ActivityNotFoundException) {
            ToastUtil.showShort(context, "打开文件选择器失败")
            Log.w(TAG, "启动文件选择器失败：title: ${fileChooserParams.title}, " +
                    "filenameHint:  ${fileChooserParams.filenameHint}, " +
                    "acceptTypes: ${fileChooserParams.acceptTypes?.contentToString()}, " +
                    "isCaptureEnabled: ${fileChooserParams.isCaptureEnabled}, " +
                    "mode: ${fileChooserParams.mode}, " +
                    "intent: $intent", e)
            removeSelf()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // 通知回调
        filePathCallback?.onReceiveValue(
                WebChromeClient.FileChooserParams.parseResult(resultCode, data))
        // 移除自身
        removeSelf()
    }

    companion object {
        private const val TAG = "KW_WVFileChooseLighterF"
        const val REQUEST_CODE = 1
    }
}
