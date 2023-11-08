package cn.entertech.serialport

import android.content.Context
import cn.entertech.communication.Tools
import cn.entertech.communication.api.IExternalDevice
import cn.entertech.communication.api.IExternalDeviceListener
import cn.entertech.communication.bean.ExternalDeviceType
import cn.entertech.communication.log.ExternalDeviceCommunicateLog
import com.vi.vioserial.NormalSerial
import com.vi.vioserial.listener.OnSerialDataListener


class ExternalDeviceSerialPort(context: Context) : IExternalDevice {
    private var listener: IExternalDeviceListener? = null
    private var contactListener: ((Int) -> Unit)? = null
    private var rawListener: ((ByteArray) -> Unit)? = null
    private var heartRateListener: ((Int) -> Unit)? = null
    private var connectListener:((String) ->Unit)?=null

    companion object {
        private const val TAG = "ExternalDeviceSerialPort"
    }

    override fun setExternalDeviceListener(listener: IExternalDeviceListener) {
        this.listener = listener
    }

    override fun addConnectListener(listener: (String) -> Unit) {
        this.connectListener=null
    }

    override fun removeConnectListener(listener: (String) -> Unit) {
        this.connectListener=null
    }

    override fun addRawDataListener(rawListener: (ByteArray) -> Unit) {
        this.rawListener = rawListener
    }

    override fun removeRawDataListener(listener: (ByteArray) -> Unit) {
        this.rawListener = null
    }

    override fun addHeartRateListener(heartRateListener: (Int) -> Unit) {
        this.heartRateListener = heartRateListener
    }

    override fun removeHeartRateListener(heartRateListener: (Int) -> Unit) {
        this.heartRateListener = null
    }

    override fun addContactListener(contactListener: (Int) -> Unit) {
        this.contactListener = contactListener
    }

    override fun removeContactListener(contactListener: (Int) -> Unit) {
        this.contactListener = null
    }

    override fun connect() {
        val result = NormalSerial.instance()
            .open("/dev/ttyMSM0", 115200, 1, 8, 0, 0)
        ExternalDeviceCommunicateLog.d(TAG, "connect result:$result")
        when(result){
            0->{
                NormalSerial.instance().setSerialDataListener(object :OnSerialDataListener{
                    override fun onSend(hexData: String?) {
                        ExternalDeviceCommunicateLog.d(
                            TAG,
                            "onSend:$hexData"
                        )
                    }

                    override fun onReceive(hexData: String?) {
                        ExternalDeviceCommunicateLog.d(
                            TAG,
                            "onReceive:$hexData"
                        )
                    }

                    override fun onReceiveFullData(hexData: String?) {
                        ExternalDeviceCommunicateLog.d(
                            TAG,
                            "onReceiveFullData:$hexData"
                        )
                    }
                })
//                startHeartAndBrainCollection()
//                stopHeartAndBrainCollection()
            }
        }
    }

    override fun disConnect() {

    }

    override fun startHeartAndBrainCollection() {
        NormalSerial.instance().sendHex("01")
    }

    override fun stopHeartAndBrainCollection() {
        NormalSerial.instance().sendHex("02")
    }

    override fun write(byteArray: ByteArray) {
        NormalSerial.instance().sendHex(String(byteArray))
    }

    override fun read(byteArray: ByteArray): Int {
        return 0
    }

    override fun getExternalDeviceType() = ExternalDeviceType.SERIAL_PORT
}