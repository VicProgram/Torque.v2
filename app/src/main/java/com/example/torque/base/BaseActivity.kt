package com.example.torque.base

import android.content.Context
import androidx.activity.ComponentActivity
import com.example.torque.utils.AyudaIdioma

abstract class BaseActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(AyudaIdioma.wrapContext(newBase))
    }
}
