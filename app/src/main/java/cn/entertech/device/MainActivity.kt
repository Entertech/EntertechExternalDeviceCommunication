package cn.entertech.device

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import cn.entertech.communication.api.IExternalDevice
import cn.entertech.communication.api.IExternalDeviceListener
import cn.entertech.communication.usb.ExternalDeviceUsb

class MainActivity : AppCompatActivity(), OnClickListener {
    private lateinit var connect: Button
    private lateinit var disconnect: Button
    private lateinit var startListener: Button
    private lateinit var endListener: Button
    private var externalDevice: IExternalDevice? = null

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        connect = findViewById(R.id.connect)
        disconnect = findViewById(R.id.disconnect)
        startListener = findViewById(R.id.startListener)
        endListener = findViewById(R.id.endListener)
        connect.setOnClickListener(this)
        disconnect.setOnClickListener(this)
        startListener.setOnClickListener(this)
        endListener.setOnClickListener(this)
        externalDevice = ExternalDeviceUsb()
        externalDevice?.setExternalDeviceListener(object : IExternalDeviceListener {
            override fun connectSuccess() {
                Log.d(TAG, "connectSuccess")
            }

            override fun connectFail(msg: String) {
                Log.d(TAG, "connectFail $msg")
            }

            override fun readFail(msg: String) {
                Log.d(TAG, "readFail $msg")
            }

            override fun readSuccess(byteArray: ByteArray?) {
                Log.d(
                    TAG, "readSuccess byteArray size ${byteArray?.size}" +
                            " ${byteToInt(byteArray)}"
                )
            }

            override fun writeFail(msg: String) {
                Log.d(TAG, "connectSuccess $msg")
            }

        })
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.connect -> {
                externalDevice?.connect(this)
            }

            R.id.disconnect -> {
                externalDevice?.disConnect()
            }

            R.id.startListener -> {
                externalDevice?.write(hexStringToByteArray("01"))
            }

            R.id.endListener -> {
                externalDevice?.write(hexStringToByteArray("02"))
            }

        }
    }

    private fun byteToInt(byteArray: ByteArray?): String {
        return byteArray?.map {
            it.toInt()
        }.toString()
    }


    private fun hexStringToByteArray(hex: String): ByteArray {
        val len = hex.length
        val data = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            data[i / 2] = ((((hex[i].digitToIntOrNull(16)
                ?: (-1 shl 4)) + hex[i + 1].digitToIntOrNull(16)!!) ?: -1)).toByte()
            i += 2
        }
        return data
    }
}