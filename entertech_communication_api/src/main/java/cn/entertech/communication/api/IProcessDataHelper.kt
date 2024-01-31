package cn.entertech.communication.api

interface IProcessDataHelper {

    /**
     * @param byteInt 读取出来的字节
     * @param contactListeners 佩戴监听 is [BaseExternalDeviceCommunicationManage.contactListeners]
     * @param bioAndAffectDataListeners 生物基础数据&情感数据监听 is [BaseExternalDeviceCommunicationManage.bioAndAffectDataListeners]
     * @param heartRateListeners 心率监听 is [BaseExternalDeviceCommunicationManage.heartRateListeners]
     * @param finish 拿到完整的数据包结构时候的回调
     * */
    fun process(
        byteInt: Byte,
        contactListeners: List<((Int) -> Unit)?>,
        bioAndAffectDataListeners: List<((ByteArray) -> Unit)?>,
        heartRateListeners: List<((Int) -> Unit)>?,
        finish: (() -> Unit)? = null
    )
}