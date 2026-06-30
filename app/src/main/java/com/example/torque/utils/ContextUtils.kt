package com.example.torque.utils

import android.app.Activity

fun Activity.terminar() {
    if (!isFinishing) finish()
}
