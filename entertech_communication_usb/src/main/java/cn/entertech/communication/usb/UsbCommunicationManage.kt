package cn.entertech.communication.usb

import android.content.Context
import cn.entertech.communication.ProcessDataTools
import cn.entertech.communication.Tools.hexStringToByteArray
import cn.entertech.communication.api.BaseExternalDeviceCommunicationManage

object UsbCommunicationManage : BaseExternalDeviceCommunicationManage() {

    override fun connectDevice(
        context: Context,
        connectSuccess: () -> Unit,
        connectFail: (Int, String) -> Unit
    ) {
        if (externalDevice == null) {
            externalDevice = ExternalDeviceUsb()
        }
        externalDevice?.connect(context, {
            connectSuccess()
            connectListeners.forEach {
                it()
            }
        }, connectFail) { byteArray ->
            byteArray.forEach {
                ProcessDataTools.process(
                    it, contactListeners,
                    rawDataListeners,
                    heartRateListeners
                )
            }
        }
    }

    override fun disConnectDevice() {
        externalDevice?.disConnect()
        disconnectListeners.forEach {
            it.invoke("")
        }
    }

    override fun startHeartAndBrainCollection() {
        externalDevice?.write(hexStringToByteArray("01"))
    }

    override fun stopHeartAndBrainCollection() {
        externalDevice?.write(hexStringToByteArray("02"))
    }
}