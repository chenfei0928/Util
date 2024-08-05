package io.github.chenfei0928.util.retrofit

import android.os.Handler
import android.os.Looper
import androidx.core.os.postDelayed
import io.github.chenfei0928.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

/**
 * WebSocket基类
 *
 * @author MrFeng
 * @date 2018/1/19.
 */
abstract class BaseWebSocketClient(
    private val okHttpClient: OkHttpClient,
    private val address: String,
    private val keepAliveDelay: Long
) {
    protected val handler = Handler(Looper.getMainLooper())

    // 内部的webSocket，可能会存在正在连接中或或未准备好的情况
    private var _socket: WebSocket? = null

    // 如果已经成功建立连接后才能发送消息
    private var isReady = false

    // 链接被用户关闭而非因异常中断时，为true
    private var isClosed = false

    // 如果连接可用并已准备好，返回可用的webSocket
    protected val socket: WebSocket?
        get() {
            return if (isReady) {
                _socket
            } else {
                null
            }
        }

    fun connect(autoRetry: Boolean = true) {
        handler.removeCallbacksAndMessages(TOKEN_CLOSE)
        Log.i(TAG, "connect ")
        // 正在连接中或已建立连接，不处理
        if (_socket != null) {
            return
        }
        _socket = okHttpClient.newWebSocket(
            Request.Builder().url(address).build(),
            object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    super.onOpen(webSocket, response)
                    _socket = webSocket
                    Log.i(TAG, "onOpen $response")
                    // 链接聊天室
                    this@BaseWebSocketClient.onOpen(webSocket)
                    // 标记已就绪
                    isReady = true
                    // 保活
                    postSendKeepAliveDelay()
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    super.onFailure(webSocket, t, response)
                    Log.i(TAG, "onFailure ", t)
                    _socket = null
                    isReady = false
                    handler.removeCallbacksAndMessages(TOKEN_CONNECT)
                    handler.removeCallbacksAndMessages(TOKEN_CLOSE)
                    handler.removeCallbacksAndMessages(TOKEN_SEND_MSG)
                    // 如果连接失败，重连
                    if (!isClosed && autoRetry) {
                        handler.postDelayed(RETRY_CONNECT_DELAY, TOKEN_CONNECT) {
                            connect(true)
                        }
                    }
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    super.onMessage(webSocket, text)
                    Log.i(TAG, "onMessage: $text")
                    parseMsg(text)
                }

                override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                    super.onClosing(webSocket, code, reason)
                    webSocket.close(CODE_NORMAL_CLOSURE, null)
                }

                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    super.onClosed(webSocket, code, reason)
                    Log.i(TAG, "onClosed $code $reason")
                    _socket = null
                    isReady = false
                    handler.removeCallbacksAndMessages(TOKEN_CONNECT)
                    handler.removeCallbacksAndMessages(TOKEN_CLOSE)
                    handler.removeCallbacksAndMessages(TOKEN_SEND_MSG)
                    // 如果连接失败，重连
                    if (!isClosed && autoRetry) {
                        handler.postDelayed(RETRY_CONNECT_DELAY, TOKEN_CONNECT) {
                            connect(true)
                        }
                    }
                }
            })
    }

    /**
     * 发送保活的延时消息
     */
    private fun postSendKeepAliveDelay() {
        handler.postDelayed(keepAliveDelay, TOKEN_SEND_MSG) {
            socket?.send(keepAliveMsg)
            postSendKeepAliveDelay()
        }
    }

    fun close(delay: Long = 0L) {
        // 延时关闭
        if (delay > 0) {
            handler.postDelayed(delay, TOKEN_CLOSE) {
                close(0)
            }
            return
        }
        // 关闭链接
        Log.i(TAG, "close ")
        socket?.close(CODE_NORMAL_CLOSURE, null)
        _socket = null
        isReady = false
        isClosed = true
    }

    protected abstract fun onOpen(webSocket: WebSocket)

    /**
     * 保活的心跳包内容
     */
    protected abstract val keepAliveMsg: String

    protected abstract fun parseMsg(text: String)

    companion object {
        private const val TAG = "KW_BaseWebSocketClient"
        private const val TOKEN_CLOSE = "close"
        private const val TOKEN_CONNECT = "connect"
        const val TOKEN_SEND_MSG = "sendMsg"

        private const val CODE_NORMAL_CLOSURE = 1000
        private const val RETRY_CONNECT_DELAY = 1000L
    }
}