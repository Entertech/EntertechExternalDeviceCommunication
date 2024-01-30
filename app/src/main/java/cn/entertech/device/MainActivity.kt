package cn.entertech.device

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import cn.entertech.affectivesdk.manager.EnterAffectiveSDKManager
import cn.entertech.communication.api.BaseExternalDeviceCommunicationManage
import cn.entertech.communication.bean.ExternalDeviceType
import cn.entertech.communication.log.DefaultLogPrinter
import cn.entertech.communication.log.ExternalDeviceCommunicateLog
import java.io.File
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Arrays
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
    private var enterAffectiveSDKManager: EnterAffectiveSDKManager? = null
    private var mFileSaveTools: FileSaveTools? = null


    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ExternalDeviceCommunicateLog.printer = DefaultLogPrinter
        enterAffectiveSDKManager = EnterAffectiveSDKManager.getInstance(applicationContext)
        enterAffectiveSDKManager?.addEEGRealtimeListener {
            showMsg("实时脑电数据：${it}")
            showMsg("实时睡眠：relaxation ${it.relaxation} ")
            showMsg("实时睡眠：sleepDegree ${it.sleepDegree} sleepState ${it.sleepState}")
        }
        enterAffectiveSDKManager?.addHrRealtimeListener {
            showMsg("实时心率数据：${it}")
        }
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
        initPermission()
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
            if (enterAffectiveSDKManager?.isInited() == true) {
                enterAffectiveSDKManager?.appendHR(it)
            } else {
                Log.e(TAG, "enterAffectiveSDKManager is not init")
            }
        }

        manage?.addBioAndAffectDataListener {
            mFileSaveTools?.appendData(this, it)
            showMsg("BioAndAffectData ${Arrays.toString(it)}")
            if (enterAffectiveSDKManager?.isInited() == true) {
                enterAffectiveSDKManager?.appendEEG(it)
            } else {
                Log.e(TAG, "enterAffectiveSDKManager is not init")
            }
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
                val startIndex = screenText.indexOfFirst {
                    it == '\n'
                }
                screenText = screenText.substring(startIndex + 1, screenText.length)
            }
            tvMsg.text = screenText
            scrollViewLogs.fullScroll(ScrollView.FOCUS_DOWN)
        }

    }

    /**
     * Android6.0 auth
     */
    private fun initPermission() {
        val needPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.MANAGE_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        } else {
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.MANAGE_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
        val needRequestPermissions = ArrayList<String>()
        for (i in needPermission.indices) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    needPermission[i]
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                needRequestPermissions.add(needPermission[i])
            }
        }
        if (needRequestPermissions.size != 0) {
            val permissions = arrayOfNulls<String>(needRequestPermissions.size)
            for (i in needRequestPermissions.indices) {
                permissions[i] = needRequestPermissions[i]
            }
            ActivityCompat.requestPermissions(this@MainActivity, permissions, 1)
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
                enterAffectiveSDKManager?.init()
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
                        mFileSaveTools?.finishAppendData()
                        stopHeartAndBrainCollection()
                    } else {
                        showMsg("设备未连接")
                    }
                }
                enterAffectiveSDKManager?.release()
            }

        }
    }

    override fun onDestroy() {
        manage?.disConnectDevice()
        super.onDestroy()

    }
}