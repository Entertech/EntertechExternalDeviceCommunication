package cn.entertech.communication.api

interface IProcessDataHelper {

    fun process(
        byteInt: Byte,
        contactListeners: List<((Int) -> Unit)?>,
        bioAndAffectDataListeners: List<((ByteArray) -> Unit)?>,
        heartRateListeners: List<((Int) -> Unit)>?,
        finish: (() -> Unit)? = null
    )
}