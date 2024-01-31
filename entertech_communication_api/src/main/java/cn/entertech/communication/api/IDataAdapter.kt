package cn.entertech.communication.api

interface IDataAdapter<T> {

    fun dataAdapter(originData: T, newDataCallback: (T) -> Unit)
}