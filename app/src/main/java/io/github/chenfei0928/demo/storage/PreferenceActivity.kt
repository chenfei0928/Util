package io.github.chenfei0928.demo.storage

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import io.github.chenfei0928.app.activity.ActivityDelegate
import io.github.chenfei0928.content.set
import io.github.chenfei0928.demo.R
import io.github.chenfei0928.demo.bean.Test
import io.github.chenfei0928.demo.databinding.ActivityPreferenceBinding
import io.github.chenfei0928.viewbinding.setContentViewBinding

class PreferenceActivity : AppCompatActivity() {
    private val fragmentName: String by ActivityDelegate.string()
    private val intValue: Int by ActivityDelegate.int()
    private val protobuf: Test by ActivityDelegate.protobuf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val binding = setContentViewBinding<ActivityPreferenceBinding>(
            R.layout.activity_preference
        )
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        Log.v(TAG, "onCreate: $fragmentName $intValue $protobuf")

        supportFragmentManager.commit {
            @Suppress("UNCHECKED_CAST")
            add(R.id.main, Class.forName(fragmentName) as Class<out Fragment>, Bundle.EMPTY)
        }
    }

    companion object {
        private const val TAG = "PreferenceActivity"

        fun newIntent(
            context: Context,
            fragmentClass: Class<out Fragment>,
            intValue: Int,
            protobuf: Test
        ): Intent = Intent(context, PreferenceActivity::class.java)
            .set(PreferenceActivity::fragmentName, fragmentClass.name)
            .set(PreferenceActivity::intValue, intValue)
            .set(PreferenceActivity::protobuf, protobuf)
    }
}
