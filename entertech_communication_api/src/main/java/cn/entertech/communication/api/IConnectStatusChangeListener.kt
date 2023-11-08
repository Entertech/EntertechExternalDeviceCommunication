package cn.entertech.communication.api

import cn.entertech.communication.bean.ConnectStatus

interface IConnectStatusChangeListener {

    fun connectSuccess()

    fun connectFail()

    fun disConnectSuccess()

    fun disConnectFail()

    fun connectStatusChange(newStatus: ConnectStatus, oldStatus: ConnectStatus)
}