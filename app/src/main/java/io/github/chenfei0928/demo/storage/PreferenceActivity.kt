package io.github.chenfei0928.demo.storage

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
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

        binding.btnReload.setOnClickListener {
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                replace(R.id.main, Class.forName(fragmentName) as Class<out Fragment>, Bundle.EMPTY)
            }
        }
        supportFragmentManager.registerFragmentLifecycleCallbacks(object :
            FragmentManager.FragmentLifecycleCallbacks() {
            override fun onFragmentActivityCreated(
                fm: FragmentManager, f: Fragment, savedInstanceState: Bundle?
            ) {
                super.onFragmentActivityCreated(fm, f, savedInstanceState)
                Log.v(TAG, "onFragmentActivityCreated: $f")
            }

            override fun onFragmentAttached(
                fm: FragmentManager, f: Fragment, context: Context
            ) {
                super.onFragmentAttached(fm, f, context)
                Log.v(TAG, "onFragmentAttached: $f")
            }

            override fun onFragmentCreated(
                fm: FragmentManager, f: Fragment, savedInstanceState: Bundle?
            ) {
                super.onFragmentCreated(fm, f, savedInstanceState)
                Log.v(TAG, "onFragmentCreated: $f")
            }

            override fun onFragmentDestroyed(
                fm: FragmentManager, f: Fragment
            ) {
                super.onFragmentDestroyed(fm, f)
                Log.v(TAG, "onFragmentDestroyed: $f")
            }

            override fun onFragmentDetached(
                fm: FragmentManager, f: Fragment
            ) {
                super.onFragmentDetached(fm, f)
                Log.v(TAG, "onFragmentDetached: $f")
            }

            override fun onFragmentPaused(
                fm: FragmentManager, f: Fragment
            ) {
                super.onFragmentPaused(fm, f)
                Log.v(TAG, "onFragmentPaused: $f")
            }

            override fun onFragmentPreAttached(
                fm: FragmentManager, f: Fragment, context: Context
            ) {
                super.onFragmentPreAttached(fm, f, context)
                Log.v(TAG, "onFragmentPreAttached: $f")
            }

            override fun onFragmentPreCreated(
                fm: FragmentManager, f: Fragment, savedInstanceState: Bundle?
            ) {
                super.onFragmentPreCreated(fm, f, savedInstanceState)
                Log.v(TAG, "onFragmentPreCreated: $f")
            }

            override fun onFragmentResumed(
                fm: FragmentManager, f: Fragment
            ) {
                super.onFragmentResumed(fm, f)
                Log.v(TAG, "onFragmentResumed: $f")
            }

            override fun onFragmentSaveInstanceState(
                fm: FragmentManager, f: Fragment, outState: Bundle
            ) {
                super.onFragmentSaveInstanceState(fm, f, outState)
                Log.v(TAG, "onFragmentSaveInstanceState: $f")
            }

            override fun onFragmentStarted(
                fm: FragmentManager, f: Fragment
            ) {
                super.onFragmentStarted(fm, f)
                Log.v(TAG, "onFragmentStarted: $f")
            }

            override fun onFragmentStopped(
                fm: FragmentManager, f: Fragment
            ) {
                super.onFragmentStopped(fm, f)
                Log.v(TAG, "onFragmentStopped: $f")
            }

            override fun onFragmentViewCreated(
                fm: FragmentManager, f: Fragment, v: View, savedInstanceState: Bundle?
            ) {
                super.onFragmentViewCreated(fm, f, v, savedInstanceState)
                Log.v(TAG, "onFragmentViewCreated: $f")
            }

            override fun onFragmentViewDestroyed(
                fm: FragmentManager, f: Fragment
            ) {
                super.onFragmentViewDestroyed(fm, f)
                Log.v(TAG, "onFragmentViewDestroyed: $f")
            }
        }, true)
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
