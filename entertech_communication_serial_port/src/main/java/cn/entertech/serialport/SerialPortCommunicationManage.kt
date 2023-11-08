package cn.entertech.serialport

import android.content.Context
import cn.entertech.communication.ProcessDataTools
import cn.entertech.communication.api.BaseExternalDeviceCommunicationManage
import cn.entertech.communication.log.ExternalDeviceCommunicateLog

object SerialPortCommunicationManage : BaseExternalDeviceCommunicationManage() {
    private var normalSerial: NormalSerial? = null
    private const val TAG = "SerialPortCommunicationManage"
    override fun connectDevice(
        context: Context,
        connectSuccess: () -> Unit,
        connectFail: (Int, String) -> Unit
    ) {
        normalSerial = NormalSerial.instance()
        val result = normalSerial?.open(
            "/dev/ttyMSM0",
            115200, 1, 8,
            0, 0
        ) ?: -5
        when (result) {
            0 -> {
                connectSuccess()
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
                        ExternalDeviceCommunicateLog.d(
                            TAG,
                            "onReceive:${hexData.map { it.toInt() and 0xff }}"
                        )
                        if (!(contactListeners.isEmpty() && rawDataListeners.isEmpty() &&
                                    heartRateListeners.isEmpty())
                        ) {
                            hexData?.forEach {
                                ProcessDataTools.process(
                                    it, contactListeners,
                                    rawDataListeners,
                                    heartRateListeners
                                )
                            }
                        }
                    }

                    override fun onReceiveFullData(hexData: String?) {

                    }
                })
            }

            -1 -> {
                connectFail(
                    result,
                    "Failed to open the serial port: no serial port read/write permission!"
                )
            }

            -2 -> {
                connectFail(result, "Failed to open serial port: unknown error!")
            }

            -3 -> {
                connectFail(result, "Failed to open the serial port: the parameter is wrong!")
            }

            -4 -> {
                connectFail(result, "Failed to open the serial port: other error!")
            }

            -5 -> {
                connectFail(result, "normalSerial init fail")
            }
        }


    }


    override fun disConnectDevice() {
        normalSerial?.close()
        disconnectListeners.forEach {
            it("")
        }
    }


    override fun startHeartAndBrainCollection() {
        normalSerial?.sendHex("01")
    }

    override fun stopHeartAndBrainCollection() {
        normalSerial?.sendHex("02")
    }
}