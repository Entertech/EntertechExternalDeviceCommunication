package cn.entertech.serialport

import android.content.Context
import cn.entertech.communication.ProcessDataTools
import cn.entertech.communication.api.BaseExternalDeviceCommunicationManage
import cn.entertech.communication.bean.ExternalDeviceType
import cn.entertech.communication.log.ExternalDeviceCommunicateLog
import com.google.auto.service.AutoService

@AutoService(BaseExternalDeviceCommunicationManage::class)
class SerialPortCommunicationManage : BaseExternalDeviceCommunicationManage() {
    companion object {
        private const val TAG = "SerialPortCommunicationManage"
    }

    private var normalSerial: NormalSerial? = null

    override fun connectDevice(
        context: Context,
        connectSuccess: () -> Unit,
        connectFail: (Int, String) -> Unit
    ) {
        normalSerial = NormalSerial.instance()
        val result = normalSerial?.open(
            "/dev/ttyHS1",
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

    override fun getType() = ExternalDeviceType.SERIAL_PORT
}