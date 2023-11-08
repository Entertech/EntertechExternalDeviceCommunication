package cn.entertech.communication.api

import cn.entertech.communication.bean.ExternalDeviceType

/**
 * 外部设备
 * */
interface IExternalDevice {

    fun setExternalDeviceListener(listener: IExternalDeviceListener)

    fun addConnectListener(listener: (String) -> Unit)
    fun removeConnectListener(listener: (String) -> Unit)

    fun addRawDataListener(rawListener: ((ByteArray) -> Unit))
    fun removeRawDataListener(listener: (ByteArray) -> Unit)

    fun addHeartRateListener(heartRateListener: ((Int) -> Unit))
    fun removeHeartRateListener(heartRateListener: ((Int) -> Unit))

    fun addContactListener(contactListener: ((Int) -> Unit))
    fun removeContactListener(contactListener: ((Int) -> Unit))

    fun connect()
    fun disConnect()

    fun startHeartAndBrainCollection()
    fun stopHeartAndBrainCollection()

    fun write(byteArray: ByteArray)

    fun read(byteArray: ByteArray): Int

    fun getExternalDeviceType():ExternalDeviceType
}

interface IExternalDeviceListener {

    fun connectSuccess()

    fun connectFail(msg: String)

    fun readFail(msg: String)

    fun readSuccess(byteArray: ByteArray?)

    fun writeFail(msg: String)
}
