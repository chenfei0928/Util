package io.github.chenfei0928.app.arouter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.app.ActivityOptionsCompat
import com.alibaba.android.arouter.facade.Postcard

/**
 * @author chenfei(chenfei0928@gmail.com)
 * @date 2022-02-11 16:14
 */
class ARouterIntentBridgeActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val postcard = intent.run {
            Postcard(
                getStringExtra(KEY_PATH),
                getStringExtra(KEY_GROUP),
                getParcelableExtra(KEY_URI),
                getBundleExtra(KEY_BUNDLE)
            ).apply {
                withFlags(getIntExtra(KEY_FLAGS, 0))
                timeout = getIntExtra(KEY_TIMEOUT, 0)
                if (getBooleanExtra(KEY_GREEN_CHANNEL, false)) {
                    greenChannel()
                }
                withAction(getStringExtra(KEY_ACTION))
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    withOptionsCompat(object : ActivityOptionsCompat() {
                        override fun toBundle(): Bundle? {
                            return getBundleExtra(KEY_OPTIONS_COMPAT)
                        }
                    })
                }
                withTransition(
                    getIntExtra(KEY_ENTER_ANIM, -1),
                    getIntExtra(KEY_EXIT_ANIM, -1)
                )
            }
        }
        postcard.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT)
        postcard.navigation(this) {
            finish()
        }
    }

    companion object {
        // RouteMeta
        private const val KEY_PATH = "path"
        private const val KEY_GROUP = "group"

        // Postcard
        private const val KEY_URI = "uri"
        private const val KEY_BUNDLE = "mBundle"
        private const val KEY_FLAGS = "flags"
        private const val KEY_TIMEOUT = "timeout"
        private const val KEY_GREEN_CHANNEL = "greenChannel"
        private const val KEY_ACTION = "action"
        private const val KEY_OPTIONS_COMPAT = "optionsCompat"
        private const val KEY_ENTER_ANIM = "enterAnim"
        private const val KEY_EXIT_ANIM = "exitAnim"

        fun toIntent(context: Context, postcard: Postcard): Intent =
            Intent(context, ARouterIntentBridgeActivity::class.java).apply {
                putExtra(KEY_PATH, postcard.path)
                putExtra(KEY_GROUP, postcard.group)
                // Postcard
                putExtra(KEY_URI, postcard.uri)
                putExtra(KEY_BUNDLE, postcard.extras)
                putExtra(KEY_FLAGS, postcard.flags)
                putExtra(KEY_TIMEOUT, postcard.timeout)
                putExtra(KEY_GREEN_CHANNEL, postcard.isGreenChannel)
                putExtra(KEY_ACTION, postcard.action)
                putExtra(KEY_OPTIONS_COMPAT, postcard.optionsBundle)
                putExtra(KEY_ENTER_ANIM, postcard.enterAnim)
                putExtra(KEY_EXIT_ANIM, postcard.exitAnim)
            }
    }
}

abstract class ARouterResultContract<I, O> : ActivityResultContract<I, O>() {

    override fun createIntent(context: Context, input: I): Intent {
        return ARouterIntentBridgeActivity.toIntent(
            context, createPostcard(context, input)
        )
    }

    abstract fun createPostcard(context: Context, input: I): Postcard
}
