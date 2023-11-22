package cn.entertech.device

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import cn.entertech.communication.api.BaseExternalDeviceCommunicationManage
import cn.entertech.communication.bean.ExternalDeviceType
import java.io.File
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity(), OnClickListener {
    private lateinit var connect: Button
    private lateinit var disconnect: Button
    private lateinit var startListener: Button
    private lateinit var endListener: Button
    private lateinit var tvMsg: TextView
    private var manage: BaseExternalDeviceCommunicationManage? = null
    private val sim by lazy {
        SimpleDateFormat("yyyy年MM月dd日HH:mm:ss:SSS")
    }
    private var isFirst = true

    private fun getSaveFileDirectory(context: Context): File {
        return File(
            context.getExternalFilesDir("") ?: context.filesDir,
            "serialPortData"
        )
    }

    private var printWriter: PrintWriter? = null

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
        manage = BaseExternalDeviceCommunicationManage.getManage(ExternalDeviceType.SERIAL_PORT)
        manage?.initDevice(this)
        manage?.addRawDataListener {
            Log.d(TAG, "RawData ${it.map { byte -> byte.toInt() and 0xff }}")
        }
        manage?.addContactListener {
            Log.d(TAG, "Contact it $it")
        }

        manage?.addHeartRateListener {
            Log.d(TAG, "hr it $it")
        }
     /*   val file =
            File(
                getSaveFileDirectory(this),
                sim.format(Date(System.currentTimeMillis())) + ".txt"
            )
        if (!file.exists()) {
            file.parentFile?.mkdirs()
        }
        file.createNewFile()
        printWriter = PrintWriter(file)*/
        manage?.addBioAndAffectDataListener {
            Log.d(TAG, "BioAndAffectData ${it.map { byte -> byte.toInt() and 0xff }}")
            /*it.forEach { byte ->
                if (isFirst) {
                    printWriter?.print((byte.toInt() and 0xff).toString())
                    isFirst = false
                } else {
                    printWriter?.append((byte.toInt() and 0xff).toString())
                }
                printWriter?.append(",")
            }
            printWriter?.flush()*/
        }

        manage?.connectDevice(this, {
            Log.d(TAG, "connectDevice success")
            manage?.startHeartAndBrainCollection()
        }) { errorCode, errorMsg ->
            Log.e(TAG, "errorCode: $errorCode  errorMsg: $errorMsg")
        }

        thread {
            Thread.sleep(1000 * 60 * 3)
            printWriter?.close()
            Log.d(TAG, "stopHeartAndBrainCollection")
            manage?.stopHeartAndBrainCollection()
        }
    }

    private fun showMsg(msg: String) {
        tvMsg.text = (msg)
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.connect -> {
                manage?.connectDevice(this, {
                    Log.d(TAG, "connectDevice success")
                }) { errorCode, errorMsg ->
                    Log.e(TAG, "errorCode: $errorCode  errorMsg: $errorMsg")
                }
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

    override fun onDestroy() {
        manage?.disConnectDevice()
        super.onDestroy()

    }
}