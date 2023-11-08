package cn.entertech.communication.log

interface ILogPrinter {

    fun d(tag: String, msg: String)

    fun i(tag: String, msg: String)

    fun e(tag: String, msg: String)
}