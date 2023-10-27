package cn.entertech.communication.api

import android.content.Context

/**
 * 外部设备
 * */
interface IExternalDevice {

    fun setExternalDeviceListener(listener: IExternalDeviceListener)

    fun addConnectListener(listener: (String) -> Unit)

    fun removeConnectListener(listener: (String) -> Unit)

    fun addRawDataListener(rawListener: ((ByteArray) -> Unit))

    fun addHeartRateListener(heartRateListener: ((Int) -> Unit))

    fun addContactListener(contactListener: ((Int) -> Unit))

    fun connect(context: Context)

    fun disConnect()

    fun startHeartAndBrainCollection()

    fun stopHeartAndBrainCollection()

    fun write(byteArray: ByteArray)

    fun read(byteArray: ByteArray): Int
}

interface IExternalDeviceListener {

    fun connectSuccess()

    fun connectFail(msg: String)

    fun readFail(msg: String)

    fun readSuccess(byteArray: ByteArray?)

    fun writeFail(msg: String)
}
