package cn.entertech.communication.usb

import android.content.Context
import cn.entertech.communication.Tools.hexStringToByteArray
import cn.entertech.communication.api.BaseExternalDeviceCommunicationManage
import cn.entertech.communication.api.ICallback
import cn.entertech.communication.bean.ExternalDeviceType
import com.google.auto.service.AutoService

@AutoService(BaseExternalDeviceCommunicationManage::class)
class UsbCommunicationManage : BaseExternalDeviceCommunicationManage() {

    override fun connectDevice(
        context: Context,
        connectSuccess: (() -> Unit)?,
        connectFail: ((Int, String) -> Unit)?
    ) {
        if (externalDevice == null) {
            externalDevice = ExternalDeviceUsb()
        }
        externalDevice?.connect(context, {
            connectSuccess?.invoke()
            connectListeners.forEach {
                it()
            }
        }, connectFail) { byteArray ->
            byteArray.forEach {
                mIProcessDataHelper?.process(
                    it, contactListeners,
                    bioAndAffectDataListeners,
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

    override fun startHeartAndBrainCollection(callback: ICallback<Unit, String>) {
        externalDevice?.write(hexStringToByteArray("01"),callback)
    }

    override fun stopHeartAndBrainCollection(callback: ICallback<Unit, String>) {
        externalDevice?.write(hexStringToByteArray("02"),callback)
    }

    override fun startHeartAndBrainCollection() {
        externalDevice?.write(hexStringToByteArray("01"))
    }

    override fun stopHeartAndBrainCollection() {
        externalDevice?.write(hexStringToByteArray("02"))
    }

    override fun sendCommand(command: String, callback: ICallback<Unit, String>) {
        externalDevice?.write(hexStringToByteArray(command),callback)
    }

    override fun getType(): ExternalDeviceType {
        return externalDevice?.getExternalDeviceType() ?: ExternalDeviceType.USB
    }
}