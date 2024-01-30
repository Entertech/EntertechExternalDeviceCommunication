package cn.entertech.communication.log


object ExternalDeviceCommunicateLog {

    var printer: ILogPrinter? = null

    fun d(tag: String, msg: String) {
        printer?.d(tag, msg)
    }

    fun i(tag: String, msg: String) {
        printer?.i(tag, msg)
    }

    fun e(tag: String, msg: String) {
        printer?.e(tag, msg)
    }
}