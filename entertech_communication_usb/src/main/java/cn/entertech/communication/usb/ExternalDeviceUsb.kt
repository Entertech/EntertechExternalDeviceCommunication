package cn.entertech.communication.usb

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import cn.entertech.communication.api.IExternalDevice
import cn.entertech.communication.bean.ExternalDeviceType
import cn.entertech.communication.log.ExternalDeviceCommunicateLog
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import com.hoho.android.usbserial.util.SerialInputOutputManager
import java.io.IOException

class ExternalDeviceUsb : IExternalDevice {
    private var usbSerialPort: UsbSerialPort? = null
    private var connected = false
    private var usbIoManager: SerialInputOutputManager? = null

    companion object {
        private const val READ_WAIT_MILLIS = 2000
        private const val WRITE_WAIT_MILLIS = 2000
        private const val TAG = "ExternalDeviceUsb"
    }

    override fun write(byteArray: ByteArray) {
        if (!connected) {
            ExternalDeviceCommunicateLog.e(TAG, "write error :not connect")
            return
        }
        try {
            usbSerialPort?.write(
                byteArray, WRITE_WAIT_MILLIS
            )
        } catch (e: java.lang.Exception) {
            ExternalDeviceCommunicateLog.e(TAG, "write error : ${e.message}")
        }
    }

    override fun connect(
        context: Context,
        connectSuccess: () -> Unit,
        connectFail: (Int, String) -> Unit,
        processData: (ByteArray) -> Unit
    ) {
        var device: UsbDevice? = null
        val usbManager = context.getSystemService(Context.USB_SERVICE) as? UsbManager
        if (usbManager == null) {
            connectFail(-1, "connection failed: usb manager service not found")
            return
        }
        for (v in usbManager.deviceList.values) {
            if (v.vendorId == UsbId.VENDOR_ID && v.productId == UsbId.PRODUCT_ID) {
                device = v
            }
        }
        if (device == null) {
            connectFail(-1, "connection failed: device not found")
            return
        }
        val driver = UsbSerialProber.getDefaultProber().probeDevice(device)
        if (driver == null) {
            connectFail(-1, "connection failed: no driver for device")
            return
        }
        if (driver.ports.size < 1) {
            connectFail(-1, "connection failed: not enough ports at device")
            return
        }
        usbSerialPort = driver.ports[0]
        val usbConnection = usbManager.openDevice(driver.device)

        if (usbConnection == null) {
            if (!usbManager.hasPermission(driver.device)) {
                val flags =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                        PendingIntent.FLAG_MUTABLE else 0
                val usbPermissionIntent = PendingIntent.getBroadcast(
                    context,
                    0,
                    Intent(context.packageName + ".GRANT_USB"),
                    flags
                )
                usbManager.requestPermission(driver.device, usbPermissionIntent)
                connectFail(-1, "connection failed: permission denied")
                return
            } else {
                connectFail(
                    -1,
                    "connection failed: open failed"
                )
            }
            return
        }

        try {
            usbSerialPort?.open(usbConnection)
            try {
                usbSerialPort?.setParameters(115200, 8, 1, UsbSerialPort.PARITY_NONE)
            } catch (e: UnsupportedOperationException) {
                connectFail(-1, "unsupport setparameters")
            }
            connectSuccess()
            connected = true
            usbIoManager = SerialInputOutputManager(usbSerialPort, object :
                SerialInputOutputManager.Listener {
                override fun onNewData(data: ByteArray?) {
                    processData(data ?: ByteArray(0))
                }

                override fun onRunError(e: java.lang.Exception?) {
                    ExternalDeviceCommunicateLog.e(TAG, "onRunError : ${e?.message}")
                }
            })
            usbIoManager?.start()

        } catch (e: Exception) {
            connectFail(-1, "connection failed: " + e.message)
            disConnect()
        }
    }

    override fun read(byteArray: ByteArray): Int {
        if (!connected) {
            ExternalDeviceCommunicateLog.e(TAG, "read error :not connect")
            return -1
        }
        return try {
            usbSerialPort?.read(
                byteArray,
                READ_WAIT_MILLIS
            ) ?: -1

        } catch (e: IOException) {
            // when using read with timeout, USB bulkTransfer returns -1 on timeout _and_ errors
            // like connection loss, so there is typically no exception thrown here on error
            ExternalDeviceCommunicateLog.e(TAG, "read error : ${e.message}")
            -1
        }
    }


    override fun disConnect() {
        connected = false
        usbIoManager?.listener = null
        usbIoManager?.stop()
        usbIoManager = null
        try {
            usbSerialPort?.close()
        } catch (ignored: IOException) {
        }
        usbSerialPort = null
    }

    override fun getExternalDeviceType() = ExternalDeviceType.USB

}