package cn.entertech.communication.api

import android.content.Context
import cn.entertech.communication.ProcessDataTools
import cn.entertech.communication.bean.ExternalDeviceType
import java.util.ServiceLoader
//import java.util.ServiceLoader
import java.util.concurrent.CopyOnWriteArrayList

abstract class BaseExternalDeviceCommunicationManage {

    companion object {
        fun getManage(type: ExternalDeviceType): BaseExternalDeviceCommunicationManage? {
            ServiceLoader.load(BaseExternalDeviceCommunicationManage::class.java)
                .forEach {
                    if (it.getType() == type) {
                        return it
                    }
                }
            return null
        }
    }


    protected var externalDevice: IExternalDevice? = null

    var mIProcessDataHelper: IProcessDataHelper? = ProcessDataTools()

    /**
     * 获取当前设备连接状态
     * */
    var isConnected = false
        protected set

    /**
     * 未经处理的数据
     * */
    protected val rawDataListeners = CopyOnWriteArrayList<(ByteArray) -> Unit>()
    protected val bioAndAffectDataListeners = CopyOnWriteArrayList<(ByteArray) -> Unit>()
    protected val heartRateListeners = CopyOnWriteArrayList<(Int) -> Unit>()
    protected val contactListeners = CopyOnWriteArrayList<(Int) -> Unit>()
    protected val connectListeners = CopyOnWriteArrayList<() -> Unit>()
    protected val disconnectListeners = CopyOnWriteArrayList<(String) -> Unit>()

    open fun initDevice(context: Context) {

    }

    /**
     * 连接设备
     * */
    abstract fun connectDevice(
        context: Context,
        connectSuccess: (() -> Unit)?,
        connectFail: ((Int, String) -> Unit)?
    )

    /**
     * 增加连接成功的监听
     * */
    fun addConnectListener(listener: () -> Unit) {
        if (this.connectListeners.contains(listener)) {
            return
        }
        connectListeners.add(listener)
    }

    /**
     * 移除连接成功的监听
     * */
    fun removeConnectListener(listener: () -> Unit) {
        connectListeners.remove(listener)
    }

    /**
     * 断开设备
     * */
    abstract fun disConnectDevice()


    /**
     * 增加断开连接的监听
     * */
    fun addDisConnectListener(listener: (String) -> Unit) {
        if (this.disconnectListeners.contains(listener)) {
            return
        }
        disconnectListeners.add(listener)
    }

    /**
     * 移除断开成功的监听
     * */
    fun removeDisConnectListener(listener: (String) -> Unit) {
        disconnectListeners.remove(listener)
    }

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

    fun addBioAndAffectDataListener(listener: (ByteArray) -> Unit) {
        if (!bioAndAffectDataListeners.contains(listener)) {
            this.bioAndAffectDataListeners.add(listener)
        }
    }

    fun removeBioAndAffectDataListener(listener: (ByteArray) -> Unit) {
        this.bioAndAffectDataListeners.remove(listener)
    }

    /**
     * 添加心率监听，通过该监听可从硬件中获取心率数据
     * */
    fun addHeartRateListener(listener: (Int) -> Unit) {
        if (this.heartRateListeners.contains(listener)) {
            return
        }
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
        if (this.contactListeners.contains(listener)) {
            return
        }
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

    abstract fun getType(): ExternalDeviceType
}