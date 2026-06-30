package com.example.torque.base

import android.content.Context
import androidx.activity.ComponentActivity
import com.example.torque.utils.LocaleHelper

abstract class BaseActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.wrapContext(newBase))
    }
}
