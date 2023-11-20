package cn.entertech.serialport

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.serialport.SerialPort
import cn.entertech.serialport.SerialPortCommunicationManage.Companion.SERIAL_PORT_HANDSHAKE_END

class SerialPortService : Service() {

    override fun onBind(intent: Intent): IBinder? {
        return null
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        SerialPort.init()
        SpUtils.setHasHandShake(true)
        sendBroadcast(Intent(SERIAL_PORT_HANDSHAKE_END))
        return super.onStartCommand(intent, flags, startId)
    }
}