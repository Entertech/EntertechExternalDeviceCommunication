package cn.entertech.communication.api

import android.content.Context
import cn.entertech.communication.bean.ExternalDeviceType

/**
 * 外部设备
 * */
interface IExternalDevice {

    fun connect(
        context: Context, connectSuccess: (() -> Unit)?,
        connectFail: ((Int, String) -> Unit)?,
        processData: (ByteArray) -> Unit
    )

    fun disConnect()

    fun write(byteArray: ByteArray)

    fun read(byteArray: ByteArray): Int

    fun getExternalDeviceType(): ExternalDeviceType
}
