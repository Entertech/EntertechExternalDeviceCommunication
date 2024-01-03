package cn.entertech.device

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.widget.Button
import android.widget.ScrollView
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
    private lateinit var scrollViewLogs: ScrollView
    private var manage: BaseExternalDeviceCommunicationManage? = null

    private var mFileSaveTools: FileSaveTools? = null


    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        connect = findViewById(R.id.connect)
        scrollViewLogs = findViewById(R.id.scrollView_logs)
        disconnect = findViewById(R.id.disconnect)
        startListener = findViewById(R.id.startListener)
        endListener = findViewById(R.id.endListener)
        tvMsg = findViewById(R.id.tvMsg)
        connect.setOnClickListener(this)
        disconnect.setOnClickListener(this)
        startListener.setOnClickListener(this)
        endListener.setOnClickListener(this)
        manage = BaseExternalDeviceCommunicationManage.getManage(ExternalDeviceType.SERIAL_PORT)
        manage?.initDevice(this) ?: kotlin.run {
            showMsg("manage is nul ")
        }
        manage?.addRawDataListener {
            showMsg("RawData ${it.map { byte -> byte.toInt() and 0xff }}")
        }
        manage?.addContactListener {
            showMsg("Contact it $it")
        }

        manage?.addHeartRateListener {
            showMsg("hr it $it")
        }

        manage?.addBioAndAffectDataListener {
            val sb = StringBuilder()
            mFileSaveTools?.appendData(this,it)
            showMsg("BioAndAffectData $sb")
        }
        /*  manage?.connectDevice(this, {
              showMsg("connectDevice success")
              manage?.startHeartAndBrainCollection()
          }) { errorCode, errorMsg ->
              showMsg("errorCode: $errorCode  errorMsg: $errorMsg")
          }*/
        /* thread {
             Thread.sleep(3000 )
             runOnUiThread {
                 manage?.connectDevice(this, {
                     showMsg( "connectDevice success")
                     manage?.startHeartAndBrainCollection()
                 }) { errorCode, errorMsg ->
                     showMsg( "errorCode: $errorCode  errorMsg: $errorMsg")
                 }
             }

         }*/

        /*    thread {
                Thread.sleep(1000 * 60 * 30)
                printWriter?.close()
                showMsg("stopHeartAndBrainCollection")
                manage?.stopHeartAndBrainCollection()
            }*/
    }

    private var screenText: String = ""
    private fun showMsg(msg: String) {
        Log.d(TAG, msg)
        runOnUiThread {
            screenText += "->:$msg\n"
            if (screenText.split("\n").size >= 20) {
                var startIndex = screenText.indexOfFirst {
                    it == '\n'
                }
                screenText = screenText.substring(startIndex + 1, screenText.length)
            }
            tvMsg.text = screenText
            scrollViewLogs.fullScroll(ScrollView.FOCUS_DOWN)
        }

    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.connect -> {
                manage?.connectDevice(this, {
                    showMsg("connectDevice success")
                }) { errorCode, errorMsg ->
                    showMsg("errorCode: $errorCode  errorMsg: $errorMsg")
                }
            }

            R.id.disconnect -> {
                manage?.disConnectDevice()
            }

            R.id.startListener -> {
                manage?.apply {
                    if (isConnected) {
                        mFileSaveTools = FileSaveTools()
                        startHeartAndBrainCollection()
                    } else {
                        showMsg("设备未连接")
                    }
                }
            }

            R.id.endListener -> {
                manage?.apply {
                    if (isConnected) {
                        stopHeartAndBrainCollection()
                    } else {
                        showMsg("设备未连接")
                    }
                }
            }

        }
    }

    override fun onDestroy() {
        manage?.disConnectDevice()
        super.onDestroy()

    }
}