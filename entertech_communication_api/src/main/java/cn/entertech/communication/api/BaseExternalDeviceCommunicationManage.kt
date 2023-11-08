package cn.entertech.communication.api

import android.content.Context
import java.util.concurrent.CopyOnWriteArrayList

abstract class BaseExternalDeviceCommunicationManage {
    protected var externalDevice: IExternalDevice? = null
    private var isConnected = false
    protected val rawDataListeners = CopyOnWriteArrayList<(ByteArray) -> Unit>()
    protected val heartRateListeners = CopyOnWriteArrayList<(Int) -> Unit>()
    protected val contactListeners = CopyOnWriteArrayList<(Int) -> Unit>()
    protected val connectListeners = CopyOnWriteArrayList<() -> Unit>()
    protected val disconnectListeners = CopyOnWriteArrayList<(String) -> Unit>()


    /**
     * 连接设备
     * */
    abstract fun connectDevice(
        context: Context,
        connectSuccess: () -> Unit,
        connectFail: (Int, String) -> Unit
    )

    fun addConnectListener(listener: () -> Unit) {
        connectListeners.add(listener)
    }

    fun removeConnectListener(listener: () -> Unit) {
        connectListeners.remove(listener)
    }

    /**
     * 断开设备
     * */
    abstract fun disConnectDevice()

    fun addDisConnectListener(listener: (String) -> Unit) {
        disconnectListeners.add(listener)
    }

    fun removeDisConnectListener(listener: (String) -> Unit) {
        disconnectListeners.remove(listener)
    }

    /**
     * 获取当前设备连接状态
     * */
    fun isConnected() = isConnected

    /**
     * 添加原始脑波监听
     * 通过该监听可从硬件中获取原始脑波数据
     * */
    open fun addRawDataListener(listener: (ByteArray) -> Unit) {
        this.rawDataListeners.add(listener)
    }

    /**
     * 移除原始脑波监听
     * */
    open fun removeRawDataListener(listener: (ByteArray) -> Unit) {
        rawDataListeners.remove(listener)
    }

    /**
     * 添加心率监听，通过该监听可从硬件中获取心率数据
     * */
    fun addHeartRateListener(listener: (Int) -> Unit) {
        this.heartRateListeners.add(listener)
    }

    /**
     * 移除心率监听
     * 如果不想收到心率，移除监听即可
     * */
    fun removeHeartRateListener(listener: (Int) -> Unit) {
        heartRateListeners.remove(listener)
    }


    /**
     * 添加佩戴信号监听
     * 添加该监听，可实时获取设备佩戴质量
     * 佩戴信号回调。0:接触良好，其他值：未正常佩戴
     * */
    fun addContactListener(listener: (Int) -> Unit) {
        this.contactListeners.add(listener)
    }

    /**
     * 移除佩戴信号监听
     * 移除该监听，则不会受到佩戴信号
     * */
    fun removeContactListener(listener: (Int) -> Unit) {
        contactListeners.remove(listener)
    }


    /**
     * 添加电量监听
     * */
    fun addBatteryListener(listener: (Byte) -> Unit) {

    }

    /**
     * 移除电量监听
     * 移除后，将不会收到电量回调
     * */
    fun removeBatteryListener(listener: (Byte) -> Unit) {

    }

    /**
     * 添加电池电压监听
     * */
    fun addBatteryVoltageListener(listener: (Double) -> Unit) {

    }

    fun removeBatteryVoltageListener(listener: (Double) -> Unit) {

    }

    /**
     * 开始脑波数据采集
     * 调用这个接口开始采集脑波数据
     * */
    fun startBrainCollection() {

    }


    /**
     * 停止脑波数据采集
     * 停止采集，调用该方法停止采集脑波数据
     * */
    fun stopBrainCollection() {

    }

    /**
     * 开始心率数据采集
     * 调用这个接口开始采集心率数据
     * */
    fun startHeartRateCollection() {

    }

    fun stopHeartRateCollection() {

    }

    /**
     * 开始脑波和心率数据同时采集
     * */
    abstract fun startHeartAndBrainCollection()

    /**
     * 停止脑波和心率数据采集
     * */
    abstract fun stopHeartAndBrainCollection()
}