package cn.entertech.communication.usb

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import cn.entertech.communication.api.IExternalDevice
import cn.entertech.communication.api.IExternalDeviceListener
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import com.hoho.android.usbserial.util.SerialInputOutputManager
import com.hoho.android.usbserial.util.SerialInputOutputManager.Listener
import java.io.IOException

class ExternalDeviceUsb : IExternalDevice {
    private var usbSerialPort: UsbSerialPort? = null
    private var connected = false
    private var usbIoManager: SerialInputOutputManager? = null
    private var listener: IExternalDeviceListener? = null
    private var contactListener: ((Int) -> Unit)? = null
    private var rawListener: ((ByteArray) -> Unit)? = null
    private var heartRateListener: ((Int) -> Unit)? = null

    companion object {
        private const val READ_WAIT_MILLIS = 2000
        private val WRITE_WAIT_MILLIS = 2000

    }

    override fun write(byteArray: ByteArray) {
        if (!connected) {
            listener?.writeFail("not connected")
            return
        }
        try {
            usbSerialPort?.write(
                byteArray, WRITE_WAIT_MILLIS
            )
        } catch (e: java.lang.Exception) {
            listener?.writeFail("connection lost: " + e.message)
            disConnect()
        }
    }

    override fun setExternalDeviceListener(listener: IExternalDeviceListener) {
        this.listener = listener
    }

    override fun addConnectListener(listener: (String) -> Unit) {

    }

    override fun removeConnectListener(listener: (String) -> Unit) {
    }

    override fun addRawDataListener(rawListener: (ByteArray) -> Unit) {
        this.rawListener = rawListener
    }

    override fun addHeartRateListener(heartRateListener: (Int) -> Unit) {
        this.heartRateListener = heartRateListener
    }

    override fun addContactListener(contactListener: (Int) -> Unit) {
        this.contactListener = contactListener
    }

    override fun connect(context: Context) {
        var device: UsbDevice? = null
        val usbManager = context.getSystemService(Context.USB_SERVICE) as? UsbManager ?: return
        for (v in usbManager.deviceList.values) {
            if (v.vendorId == UsbId.VENDOR_ID && v.productId == UsbId.PRODUCT_ID) {
                device = v
            }
        }
        if (device == null) {
            listener?.connectFail("connection failed: device not found")
            return
        }
        val driver = UsbSerialProber.getDefaultProber().probeDevice(device)
        if (driver == null) {
            listener?.connectFail("connection failed: no driver for device")
            return
        }
        if (driver.ports.size < 1) {
            listener?.connectFail("connection failed: not enough ports at device")
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
                listener?.connectFail("connection failed: permission denied")
                return
            } else {
                listener?.connectFail(
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
                listener?.connectFail("unsupport setparameters")
            }
            usbIoManager = SerialInputOutputManager(usbSerialPort, object : Listener {
                override fun onNewData(data: ByteArray?) {
                    listener?.readSuccess(data)
                    data?.forEach {
                        ProcessDataTools.process(
                            it, contactListener,
                            rawListener,
                            heartRateListener
                        )
                    }
                }

                override fun onRunError(e: java.lang.Exception?) {
                    listener?.readFail("e message : ${e?.message}")
                }
            })
            usbIoManager?.start()
            listener?.connectSuccess()
            connected = true
        } catch (e: Exception) {
            listener?.connectFail("connection failed: " + e.message)
            disConnect()
        }
    }

    override fun read(byteArray: ByteArray): Int {
        if (!connected) {
            listener?.connectFail("not connected")
            return -1
        }
        return try {
            usbSerialPort!!.read(
                byteArray,
                READ_WAIT_MILLIS
            )
        } catch (e: IOException) {
            // when using read with timeout, USB bulkTransfer returns -1 on timeout _and_ errors
            // like connection loss, so there is typically no exception thrown here on error
            listener?.connectFail("connection lost: " + e.message)
            disConnect()
            -1
        }
    }

    override fun startHeartAndBrainCollection() {
        write(hexStringToByteArray("01"))
    }

    override fun stopHeartAndBrainCollection() {
        write(hexStringToByteArray("02"))
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