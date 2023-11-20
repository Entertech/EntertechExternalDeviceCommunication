package cn.entertech.serialport

import android.content.Context
import android.content.SharedPreferences

object SpUtils {

    private var sp: SharedPreferences? = null
    private const val HAS_HAND_SHAKE = "hashandshake"


    fun init(context: Context) {
        sp = context.getSharedPreferences("externalDevice", Context.MODE_PRIVATE)
    }

    private fun getEdit() = sp?.edit()

    fun setHasHandShake(boolean: Boolean) {
        getEdit()?.putBoolean(HAS_HAND_SHAKE, boolean)?.apply()
    }

    fun getHasHandShake(): Boolean {
        return sp?.getBoolean(HAS_HAND_SHAKE, false) ?: false
    }
}