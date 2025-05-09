package io.github.chenfei0928.demo

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import com.google.protobuf.ProtobufParceler
import com.tencent.mmkv.MMKV
import io.github.chenfei0928.app.fragment.ArgumentDelegate
import io.github.chenfei0928.app.fragment.ArgumentDelegate.Companion.argBoolean
import io.github.chenfei0928.app.fragment.ArgumentDelegate.Companion.argInt
import io.github.chenfei0928.app.fragment.ArgumentDelegate.Companion.argParcelable
import io.github.chenfei0928.app.fragment.ArgumentDelegate.Companion.argParcelableList
import io.github.chenfei0928.app.fragment.ArgumentDelegate.Companion.argParcelableNull
import io.github.chenfei0928.app.fragment.ArgumentDelegate.Companion.argString
import io.github.chenfei0928.app.fragment.ArgumentDelegate.Companion.argStringNull
import io.github.chenfei0928.collection.mapToArray
import io.github.chenfei0928.concurrent.coroutines.coroutineScope
import io.github.chenfei0928.demo.bean.Bean
import io.github.chenfei0928.demo.bean.JsonBean
import io.github.chenfei0928.demo.bean.Test
import io.github.chenfei0928.demo.databinding.ActivityMainBinding
import io.github.chenfei0928.demo.storage.JsonDataStorePreferenceFragment
import io.github.chenfei0928.demo.storage.JsonLocalFileStorage
import io.github.chenfei0928.demo.storage.JsonLocalFileStorage0.Companion.jsonLocalStorage0
import io.github.chenfei0928.demo.storage.JsonLocalFileStoragePreferenceFragment
import io.github.chenfei0928.demo.storage.MmkvSaverPreferenceFragment
import io.github.chenfei0928.demo.storage.PreferenceActivity
import io.github.chenfei0928.demo.storage.SpSaverPreferenceFragment
import io.github.chenfei0928.demo.storage.TestMmkvSaver
import io.github.chenfei0928.demo.storage.TestPreferenceFragment
import io.github.chenfei0928.demo.storage.TestSpSaver
import io.github.chenfei0928.lang.toStringAny
import io.github.chenfei0928.lang.toStringByReflect
import io.github.chenfei0928.os.BundleSupportType
import io.github.chenfei0928.os.Debug
import io.github.chenfei0928.reflect.isWriteByKotlin
import io.github.chenfei0928.repository.datastore.toDatastore
import io.github.chenfei0928.repository.local.serializer.KtxsJsonSerializer
import io.github.chenfei0928.repository.local.serializer.ProtobufSerializer
import io.github.chenfei0928.view.listener.setNoDoubleOnClickListener
import io.github.chenfei0928.viewbinding.setContentViewBinding
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.LinkedList

/**
 * @author chenf()
 * @date 2024-07-09 11:44
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        MMKV.initialize(this)

        val binding = setContentViewBinding<ActivityMainBinding>(
            R.layout.activity_main, ActivityMainBinding::bind
        )
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding.btnPreference.setNoDoubleOnClickListener {
            val items = arrayOf(
                "spSaver" to SpSaverPreferenceFragment::class.java,
                "mmkvSaver" to MmkvSaverPreferenceFragment::class.java,
                "protobufDataStore" to TestPreferenceFragment::class.java,
                "jsonDataStore" to JsonDataStorePreferenceFragment::class.java,
                "jsonLocalFileStorage" to JsonLocalFileStoragePreferenceFragment.StorageFragment::class.java,
                "jsonLocalFileStorage0" to JsonLocalFileStoragePreferenceFragment.Storage0Fragment::class.java,
            )
            AlertDialog.Builder(this)
                .setItems(items.mapToArray { it.first }) { _, which ->
                    startActivity(Intent(this, PreferenceActivity::class.java).apply {
                        putExtra("fragmentName", items[which].second.name)
                    })
                }
                .show()
        }
        binding.btnPreload.setNoDoubleOnClickListener {
            Debug.countTime(TAG, "preload TestSpSaver") {
                TestSpSaver(this)
            }
            Debug.countTime(TAG, "preload TestMmkvSaver") {
                TestMmkvSaver.json
            }
            Debug.countTime(TAG, "preload TestMmkvSaver") {
                TestMmkvSaver.toString()
            }
            Debug.countTime(TAG, "preload JsonLocalFileStorage") {
                JsonLocalFileStorage.storage(this).get()
            }
            Debug.countTime(TAG, "preload jsonLocalStorage0") {
                jsonLocalStorage0.get()
            }
            Debug.countTime(TAG, "preload jsonDataStore") {
                jsonDataStore
            }
            Debug.countTime(TAG, "preload testDataStore") {
                testDataStore
            }
            coroutineScope.launch {
                Debug.countTime(TAG, "jsonDataStore first") {
                    jsonDataStore.data.first()
                }
                Debug.countTime(TAG, "testDataStore first") {
                    testDataStore.data.first()
                }
            }
        }
        binding.btnTest.setNoDoubleOnClickListener {
            Log.i(TAG, buildString {
                append("onCreate: isKtClass ")
                append(Build::class.java.isWriteByKotlin)
                append(' ')
                append(MainActivity::class.java.isWriteByKotlin)
                append(' ')
                append(Companion::class.java.isWriteByKotlin)
            })
            Log.i(TAG, "onCreate: jClass ${Build::class.java.toStringByReflect()}")
            Log.i(TAG, "onCreate: componentObj ${I.toStringByReflect()}")
            Log.i(TAG, "onCreate: kClass ${I::class.toStringByReflect()}")
            Log.i(TAG, "onCreate: jClass ${I::class.java.toStringByReflect()}")
            Log.i(TAG, "onCreate: componentObj ${MainActivity.toStringByReflect()}")
            Log.i(TAG, "onCreate: kClass ${MainActivity::class.toStringByReflect()}")
            Log.i(TAG, "onCreate: jClass ${MainActivity::class.java.toStringByReflect()}")
            val typeUseOld = Debug.countTime(TAG, "I.I1") {
                I.I1<Any, LinkedList<Any>>(false)
            }
            Log.i(TAG, "onCreate: ${typeUseOld.eClass}\n ${typeUseOld.typeBundle}")
            val typeUseKt = Debug.countTime(TAG, "I.I1.IArrayList true") {
                I.I1.IArrayList(true)
            }
            Log.i(TAG, "onCreate: ${typeUseKt.eClass}\n ${typeUseKt.typeBundle}")
            val type = Debug.countTime(TAG, "I.I1.IArrayList false") {
                I.I1.IArrayList(false)
            }
            Log.i(TAG, "onCreate: ${type.eClass}\n ${type.typeBundle}")
            val imType = Debug.countTime(TAG, "I.I1.IM") {
                I.I1.IM<Any, LinkedList<Any>>(true)
            }
            Log.i(TAG, "onCreate: ${imType.eClass}\n ${imType.typeBundle}")
            Fragment()
            // 加载Protobuf runtime，其会消耗较长时间
            Debug.countTime(TAG, "Test_toByteArray") {
                Test.getDefaultInstance().toByteArray()
                Test.getDefaultInstance().toString()
                Test.getDefaultInstance().parserForType
            }
            // ProtoBufType 支持类，不知为何会有很多的loadClass
            Debug.countTime(TAG, "ProtoBufType") {
                BundleSupportType.ProtoBufType(Test::class.java, false)
            }
            // Frag 类加载时会初始化其委托属性的 KProperty 信息实例，会消耗一些时间
            val f = Debug.countTime(TAG, "f") {
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
                    p = null
                    q = null
                }
            }
            Log.i(TAG, "onCreate: $f")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onMenuItemSelected(featureId: Int, item: MenuItem): Boolean {
        super.onMenuItemSelected(featureId, item)
        when (item.itemId) {
            R.id.menu_test_preference -> {

            }
        }
        return true
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
        var p: Test? by ArgumentDelegate(BundleSupportType.ProtoBufType.nullable())
        var q: Lifecycle.State? by ArgumentDelegate(BundleSupportType.EnumType.nullable())

        override fun toString(): String = toStringAny(
            ::a, ::b, ::c, ::d, ::e, ::f, ::g, ::h, ::i, ::j, ::k, ::l, ::m, ::n, ::o, ::p, ::q
        )
    }

    companion object {
        private const val TAG = "MainActivity"

        val Context.testDataStore: DataStore<Test> by dataStore<Test>(
            "test.pb", ProtobufSerializer<Test>().toDatastore()
        )
        val Context.jsonDataStore: DataStore<JsonBean> by dataStore<JsonBean>(
            "jsonBean.json", KtxsJsonSerializer<JsonBean>(JsonBean()).toDatastore()
        )
    }
}
