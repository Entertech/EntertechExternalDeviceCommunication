package cn.entertech.serialport

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.serialport.SerialPort

class SerialPortService : Service() {

    override fun onBind(intent: Intent): IBinder? {
        return null
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        SerialPort.init()
        return super.onStartCommand(intent, flags, startId)
    }
}