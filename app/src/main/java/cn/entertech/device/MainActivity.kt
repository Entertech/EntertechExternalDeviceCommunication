package cn.entertech.device

import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import cn.entertech.communication.api.BaseExternalDeviceCommunicationManage
import cn.entertech.communication.api.IExternalDevice
import cn.entertech.serialport.ExternalDeviceSerialPort
import cn.entertech.serialport.SerialPortCommunicationManage

class MainActivity : AppCompatActivity(), OnClickListener {
    private lateinit var connect: Button
    private lateinit var disconnect: Button
    private lateinit var startListener: Button
    private lateinit var endListener: Button
    private lateinit var tvMsg: TextView
    private var manage: BaseExternalDeviceCommunicationManage? = null

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
        tvMsg = findViewById(R.id.tvMsg)
        connect.setOnClickListener(this)
        disconnect.setOnClickListener(this)
        startListener.setOnClickListener(this)
        endListener.setOnClickListener(this)
        manage = SerialPortCommunicationManage

    }

    private fun showMsg(msg: String) {
        tvMsg.text = (msg)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.connect -> {
                manage?.connectDevice(this, {}) { errorCode, errorMsg -> }
            }

            R.id.disconnect -> {
                manage?.disConnectDevice()
            }

            R.id.startListener -> {
                manage?.startHeartAndBrainCollection()
            }

            R.id.endListener -> {
                manage?.stopHeartAndBrainCollection()
            }

        }
    }

}