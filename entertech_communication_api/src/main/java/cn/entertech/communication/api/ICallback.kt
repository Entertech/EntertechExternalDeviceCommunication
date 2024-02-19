package cn.entertech.communication.api

interface ICallback<T, R> {
    fun success(t: T)
    fun fail(r: R)
}