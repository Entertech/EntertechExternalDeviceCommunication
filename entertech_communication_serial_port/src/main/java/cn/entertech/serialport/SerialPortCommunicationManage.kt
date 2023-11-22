package cn.entertech.serialport

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.RECEIVER_NOT_EXPORTED
import cn.entertech.communication.ProcessDataTools
import cn.entertech.communication.api.BaseExternalDeviceCommunicationManage
import cn.entertech.communication.bean.ExternalDeviceType
import cn.entertech.communication.log.ExternalDeviceCommunicateLog
import com.google.auto.service.AutoService

@AutoService(BaseExternalDeviceCommunicationManage::class)
class SerialPortCommunicationManage : BaseExternalDeviceCommunicationManage() {
    companion object {
        private const val TAG = "SerialPortCommunicationManage"
        const val SERIAL_PORT_HANDSHAKE_END = "serial_port_handshake_end"
    }

    private var normalSerial: NormalSerial? = null
    private var context: Context? = null
    private var connectSuccess: (() -> Unit)? = null
    private var connectFail: ((Int, String) -> Unit)? = null

    /**
     * 接收到上一份完整数据时间
     * */
    private var lastBioAffectDataTime = 0L

    private var inValidDataCount = 0

    private val broadcastReceive: BroadcastReceiver by lazy {
        object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                ExternalDeviceCommunicateLog.i(TAG, "broadcastReceive ${intent?.action}")
                if (SERIAL_PORT_HANDSHAKE_END == intent?.action) {
                    mainHandler.postDelayed({
                        if (System.currentTimeMillis() - lastBioAffectDataTime >= 3000) {
                            ExternalDeviceCommunicateLog.i(TAG, "handShake end no valid data")
                            disConnectDevice()
                            connectDevice(
                                context!!,
                                connectSuccess,
                                connectFail
                            )
                        } else {
                            ExternalDeviceCommunicateLog.e(TAG, "handShake end has valid data")
                        }
                    }, 3000)
                }
            }
        }
    }


    private val checkValidData: Runnable =
        Runnable {
            ExternalDeviceCommunicateLog.i(TAG, "checkValidData")
            if (System.currentTimeMillis() - lastBioAffectDataTime > 1000) {
                ExternalDeviceCommunicateLog.d(
                    TAG,
                    "has no valid data startHeartAndBrainCollection"
                )
                if (inValidDataCount < 3) {
                    startHeartAndBrainCollection()
                    inValidDataCount++
                } else {
                    context?.apply {
                        inValidDataCount = 0
                        initDevice(this)
                        runCheckValidData()
                    } ?: kotlin.run {
                        ExternalDeviceCommunicateLog.e(TAG, "context is null")
                    }
                }
            } else {
                inValidDataCount = 0
                runCheckValidData()
            }
        }

    private val mainHandler by lazy {
        Handler(Looper.getMainLooper())
    }


    override fun initDevice(context: Context) {
        ExternalDeviceCommunicateLog.i(
            TAG,
            "initDevice"
        )
        context.startService(Intent(context, SerialPortService::class.java))
    }

    override fun connectDevice(
        context: Context,
        connectSuccess: (() -> Unit)?,
        connectFail: ((Int, String) -> Unit)?
    ) {
        val intentFilter = IntentFilter()
        intentFilter.addAction(SERIAL_PORT_HANDSHAKE_END)
        ContextCompat.registerReceiver(
            context, broadcastReceive,
            intentFilter, RECEIVER_NOT_EXPORTED
        )
        this.connectSuccess = connectSuccess
        this.connectFail = connectFail
        this.context = context
        mainHandler.postDelayed({
            normalSerial = NormalSerial.instance()
            val result = normalSerial?.open(
                "/dev/ttyHS1",
                115200, 1, 8,
                0, 0
            ) ?: -5
            when (result) {
                0 -> {
                    connectSuccess?.invoke()
                    connectListeners.forEach {
                        it.invoke()
                    }
                    normalSerial?.setSerialDataListener(object :
                        OnSerialDataListener {
                        override fun onSend(hexData: String?) {
                            ExternalDeviceCommunicateLog.d(
                                TAG, "onSend:$hexData"
                            )
                        }

                        override fun onReceive(hexData: ByteArray) {
                            rawDataListeners.forEach {
                                it(hexData)
                            }
                            if (!(contactListeners.isEmpty() && bioAndAffectDataListeners.isEmpty() &&
                                        heartRateListeners.isEmpty())
                            ) {
                                hexData.forEach {
                                    ProcessDataTools.process(
                                        it, contactListeners,
                                        bioAndAffectDataListeners,
                                        heartRateListeners
                                    ) {
                                        lastBioAffectDataTime = System.currentTimeMillis()
                                    }
                                }
                            }
                        }

                        override fun onReceiveFullData(hexData: String?) {

                        }
                    })
                }

                -1 -> {
                    connectFail?.invoke(
                        result,
                        "Failed to open the serial port: no serial port read/write permission!"
                    )
                }

                -2 -> {
                    connectFail?.invoke(result, "Failed to open serial port: unknown error!")
                }

                -3 -> {
                    connectFail?.invoke(
                        result,
                        "Failed to open the serial port: the parameter is wrong!"
                    )
                }

                -4 -> {
                    connectFail?.invoke(result, "Failed to open the serial port: other error!")
                }

                -5 -> {
                    connectFail?.invoke(result, "normalSerial init fail")
                }
            }
        }, 1000)


    }


    override fun disConnectDevice() {
        mainHandler.removeCallbacksAndMessages(null)
        context?.unregisterReceiver(broadcastReceive)
        normalSerial?.close()
        disconnectListeners.forEach {
            it("")
        }
    }


    override fun startHeartAndBrainCollection() {
        runCheckValidData()
        normalSerial?.sendHex("01")

    }


    private fun runCheckValidData() {
        ExternalDeviceCommunicateLog.d(TAG, "runCheckValidData")
        mainHandler.removeCallbacks(checkValidData)
        mainHandler.postDelayed(
            checkValidData, 1000
        )
    }

    override fun stopHeartAndBrainCollection() {
        mainHandler.removeCallbacksAndMessages(null)
        normalSerial?.sendHex("02")
    }

    override fun getType() = ExternalDeviceType.SERIAL_PORT
}