package io.github.chenfei0928.demo

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import com.google.protobuf.ProtobufParceler
import io.github.chenfei0928.app.fragment.ArgumentDelegate
import io.github.chenfei0928.app.fragment.ArgumentDelegate.Companion.argBoolean
import io.github.chenfei0928.app.fragment.ArgumentDelegate.Companion.argInt
import io.github.chenfei0928.app.fragment.ArgumentDelegate.Companion.argParcelable
import io.github.chenfei0928.app.fragment.ArgumentDelegate.Companion.argParcelableList
import io.github.chenfei0928.app.fragment.ArgumentDelegate.Companion.argParcelableNull
import io.github.chenfei0928.app.fragment.ArgumentDelegate.Companion.argString
import io.github.chenfei0928.app.fragment.ArgumentDelegate.Companion.argStringNull
import io.github.chenfei0928.demo.databinding.ActivityMainBinding
import io.github.chenfei0928.lang.toString0
import io.github.chenfei0928.os.BundleSupportType
import io.github.chenfei0928.os.Debug
import io.github.chenfei0928.reflect.parameterized.getParentParameterizedTypeBoundsContractDefinedImplInChild
import io.github.chenfei0928.reflect.parameterized.getParentParameterizedTypeClassDefinedImplInChild
import io.github.chenfei0928.view.listener.setNoDoubleOnClickListener
import io.github.chenfei0928.viewbinding.setContentViewBinding

/**
 * @author chenf()
 * @date 2024-07-09 11:44
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = setContentViewBinding<ActivityMainBinding>(
            R.layout.activity_main, ActivityMainBinding::bind
        )
        binding.btnTest.setNoDoubleOnClickListener {
            val typeUseOld =
                Debug.countTime(TAG, "getParentParameterizedTypeBoundsContractDefinedImplInChild") {
                    I.I1.IArrayList()
                        .getParentParameterizedTypeBoundsContractDefinedImplInChild<I<*>, ArrayList<Any>>(
                            0
                        )
                }
            Log.i(TAG, "onCreate: $typeUseOld")
            val typeUseKt =
                Debug.countTime(TAG, "getParentParameterizedTypeClassDefinedImplInChild true") {
                    I.I1.IArrayList()
                        .getParentParameterizedTypeClassDefinedImplInChild<I<*>, ArrayList<Any>>(
                            0, true
                        )
                }
            Log.i(TAG, "onCreate: $typeUseKt")
            val type =
                Debug.countTime(TAG, "getParentParameterizedTypeClassDefinedImplInChild false") {
                    I.I1.IArrayList()
                        .getParentParameterizedTypeClassDefinedImplInChild<I<*>, ArrayList<Any>>(
                            0, false
                        )
                }
            Log.i(TAG, "onCreate: $type")
            Fragment()
            // 加载Protobuf runtime，其会消耗较长时间
            Debug.traceAndTime(TAG, "Test_toByteArray_${System.currentTimeMillis()}") {
                Test.getDefaultInstance().toByteArray()
                Test.getDefaultInstance().parserForType
            }
            // ProtoBufType 支持类，不知为何会有很多的loadClass
            Debug.traceAndTime(TAG, "ProtoBufType_${System.currentTimeMillis()}") {
                BundleSupportType.ProtoBufType(Test::class.java, false)
            }
            // Frag 类加载时会初始化其委托属性的 KProperty 信息实例，会消耗一些时间
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
                    j = "123"
                    k = Bean(Test.getDefaultInstance())
                    l = Bean(Test.getDefaultInstance())
                    m = Bean(Test.getDefaultInstance())
                    n = Test.getDefaultInstance()
                    o = Test.getDefaultInstance()
                }
            }
            Log.i(TAG, "onCreate: $f")
        }
    }

    class Frag : Fragment() {
        var a: Int by argInt()
        var b: String by argString()
        var c: List<String> by ArgumentDelegate(BundleSupportType.ListStringType())
        var d: Array<String> by ArgumentDelegate(BundleSupportType.ArrayStringType())
        var e: Lifecycle.State by ArgumentDelegate(BundleSupportType.EnumType())
        var f: Boolean by argBoolean()
        var g: Array<Bean> by ArgumentDelegate(BundleSupportType.ArrayParcelableType())
        var h: Bean by argParcelable()
        var i: List<Bean> by argParcelableList()
        var j: String? by argStringNull()
        var k: Bean? by argParcelableNull()
        var l: Bean? by ArgumentDelegate<Bean?>(true)
        var m: Bean by ArgumentDelegate<Bean>()
        var n: Test by ArgumentDelegate(ProtobufParceler())
        var o: Test by ArgumentDelegate()

        override fun toString(): String = toString0(
            ::a, ::b, ::c, ::d, ::e, ::f, ::g, ::h, ::i, ::j, ::k, ::l, ::m, ::n, ::o
        )
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
