package cn.entertech.serialport

import android.content.Context
import cn.entertech.communication.api.IExternalDevice
import cn.entertech.communication.bean.ExternalDeviceType
import cn.entertech.communication.log.ExternalDeviceCommunicateLog


class ExternalDeviceSerialPort : IExternalDevice {

    companion object {
        private const val TAG = "ExternalDeviceSerialPort"
    }

    override fun connect(
        context: Context,
        connectSuccess: (() -> Unit)?,
        connectFail: ((Int, String) -> Unit)?,
        processData: (ByteArray) -> Unit
    ) {
        val result = NormalSerial.instance()
            .open("/dev/ttyMSM0", 115200, 1, 8, 0, 0)
        ExternalDeviceCommunicateLog.d(TAG, "connect result:$result")
        when(result){
            0->{
                NormalSerial.instance().setSerialDataListener(object :
                    OnSerialDataListener {
                    override fun onSend(hexData: String?) {
                        ExternalDeviceCommunicateLog.d(
                            TAG,
                            "onSend:$hexData"
                        )
                    }

                    override fun onReceive(hexData: ByteArray) {
                        ExternalDeviceCommunicateLog.d(
                            TAG,
                            "onReceive:$hexData"
                        )
                        processData(hexData)
                    }

                    override fun onReceiveFullData(hexData: String?) {
                        ExternalDeviceCommunicateLog.d(
                            TAG,
                            "onReceiveFullData:$hexData"
                        )
                    }
                })
            }
        }
    }

    override fun disConnect() {
        NormalSerial.instance().close()
    }

    override fun write(byteArray: ByteArray) {
        NormalSerial.instance().sendHex(String(byteArray))
    }

    override fun read(byteArray: ByteArray): Int {
        return 0
    }

    override fun getExternalDeviceType() = ExternalDeviceType.SERIAL_PORT
}