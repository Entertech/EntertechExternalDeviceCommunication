package cn.entertech.serialport

import android.app.IntentService
import android.content.Intent
import android.serialport.SerialPort
import cn.entertech.communication.log.ExternalDeviceCommunicateLog
import cn.entertech.serialport.SerialPortCommunicationManage.Companion.SERIAL_PORT_HANDSHAKE_END

class SerialPortService : IntentService("SerialPortService") {

    companion object{
        private const val TAG="SerialPortService"
    }

    @Deprecated("Deprecated in Java")
    override fun onHandleIntent(intent: Intent?) {
        ExternalDeviceCommunicateLog.d(TAG,"SerialPortService init")
        SerialPort.init()
        sendBroadcast(Intent(SERIAL_PORT_HANDSHAKE_END))
    }
}