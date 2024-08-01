package io.github.chenfei0928.util

import android.os.Bundle
import androidx.activity.ComponentActivity
import io.github.chenfei0928.util.databinding.ActivityMainBinding
import io.github.chenfei0928.viewbinding.setContentViewBinding

/**
 * @author chenf()
 * @date 2024-07-09 11:44
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = setContentViewBinding(
            R.layout.activity_main, ActivityMainBinding::bind
        )
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
