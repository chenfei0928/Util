package io.github.chenfei0928.demo

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import io.github.chenfei0928.app.fragment.ArgumentDelegate
import io.github.chenfei0928.app.fragment.ArgumentDelegate.Companion.argBoolean
import io.github.chenfei0928.app.fragment.ArgumentDelegate.Companion.argInt
import io.github.chenfei0928.app.fragment.ArgumentDelegate.Companion.argParcelableNull
import io.github.chenfei0928.app.fragment.ArgumentDelegate.Companion.argStringNull
import io.github.chenfei0928.app.fragment.ArgumentDelegate.Companion.argParcelable
import io.github.chenfei0928.app.fragment.ArgumentDelegate.Companion.argParcelableList
import io.github.chenfei0928.app.fragment.ArgumentDelegate.Companion.argString
import io.github.chenfei0928.demo.databinding.ActivityMainBinding
import io.github.chenfei0928.lang.toString0
import io.github.chenfei0928.os.BundleSupportType
import io.github.chenfei0928.os.BundleSupportType.ArrayParcelableType.Companion.ArrayParcelableType
import io.github.chenfei0928.os.BundleSupportType.EnumType.Companion.EnumType
import io.github.chenfei0928.os.Debug
import io.github.chenfei0928.viewbinding.setContentViewBinding

/**
 * @author chenf()
 * @date 2024-07-09 11:44
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = setContentViewBinding<ActivityMainBinding>(
            R.layout.activity_main
        )
        binding.btnTest.setOnClickListener {
            val f = Debug.traceAndTime(TAG, "f_${System.currentTimeMillis()}") {
                Frag().apply {
                    a = 1
                    b = "asd"
                    c = listOf("a", "b", "c")
                    d = arrayOf("1", "2", "3")
                    e = Lifecycle.State.INITIALIZED
                    f = true
                    g = arrayOf(Bean(Test.getDefaultInstance()))
                    h = Bean(Test.getDefaultInstance())
                    i = listOf(Bean(Test.getDefaultInstance()))
                    j = null
                    k = null
                }
            }
            Log.i(TAG, "onCreate: $f")
        }
    }

    class Frag : Fragment() {
        var a: Int by argInt()
        var b: String by argString()
        var c: List<String> by ArgumentDelegate(BundleSupportType.ListStringType(false))
        var d: Array<String> by ArgumentDelegate(BundleSupportType.ArrayStringType(false))
        var e: Lifecycle.State by ArgumentDelegate(EnumType())
        var f: Boolean by argBoolean()
        var g: Array<Bean> by ArgumentDelegate(ArrayParcelableType())
        var h: Bean by argParcelable()
        var i: List<Bean> by argParcelableList()
        var j: String? by argStringNull()
        var k: Bean? by argParcelableNull()

        override fun toString(): String {
            return toString0(::a, ::b, ::c, ::d, ::e, ::f, ::g, ::h, ::i, ::j, ::k)
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}