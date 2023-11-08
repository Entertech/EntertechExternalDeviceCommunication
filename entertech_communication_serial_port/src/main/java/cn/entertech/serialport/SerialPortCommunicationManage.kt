package cn.entertech.serialport

import android.content.Context
import cn.entertech.communication.ProcessDataTools
import cn.entertech.communication.Tools.hexStringToByteArray
import cn.entertech.communication.api.BaseExternalDeviceCommunicationManage
import cn.entertech.communication.log.ExternalDeviceCommunicateLog
import com.vi.vioserial.NormalSerial
import com.vi.vioserial.listener.OnSerialDataListener

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
                normalSerial?.setSerialDataListener(object : OnSerialDataListener {
                    override fun onSend(hexData: String?) {
                        ExternalDeviceCommunicateLog.d(
                            TAG, "onSend:$hexData"
                        )
                    }

                    override fun onReceive(hexData: String?) {
                        ExternalDeviceCommunicateLog.d(
                            TAG,
                            "onReceive:$hexData"
                        )
                        if (!(contactListeners.isEmpty() && rawDataListeners.isEmpty() &&
                                    heartRateListeners.isEmpty())
                        ) {
                            hexData?.toByteArray()?.forEach {
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
        externalDevice?.disConnect()
        disconnectListeners.forEach {
            it("")
        }
    }


    override fun startHeartAndBrainCollection() {
        externalDevice?.write(hexStringToByteArray("01"))
    }

    override fun stopHeartAndBrainCollection() {
        externalDevice?.write(hexStringToByteArray("02"))
    }
}