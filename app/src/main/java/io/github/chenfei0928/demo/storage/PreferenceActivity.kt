package io.github.chenfei0928.demo.storage

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import io.github.chenfei0928.app.activity.IntentDelegate.Companion.intentString
import io.github.chenfei0928.demo.R

class PreferenceActivity : AppCompatActivity() {
    private val fragmentName: String by intentString()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_preference)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        supportFragmentManager.commit {
            @Suppress("UNCHECKED_CAST")
            add(R.id.main, Class.forName(fragmentName) as Class<out Fragment>, Bundle.EMPTY)
        }
    }
}
