package cn.entertech.communication

import cn.entertech.communication.api.IDataAdapter

class AppendDataAdapter : IDataAdapter<ByteArray> {

    companion object {
        private const val ADAPTER_BYTE_SIZE = 20
    }

    private var index = 0
    private var adapterByteArray = ByteArray(ADAPTER_BYTE_SIZE)

    @Synchronized
    override fun dataAdapter(originData: ByteArray, newDataCallback: (ByteArray) -> Unit) {
        originData.forEach { byte ->
            //协议一开始 添加两个序号字节
            if (index % ADAPTER_BYTE_SIZE == 0) {
                adapterByteArray[0] = 0
                ++index
                adapterByteArray[1] = 0
                ++index
            }
            adapterByteArray[index % ADAPTER_BYTE_SIZE] = byte
            ++index
            if (index % ADAPTER_BYTE_SIZE == 0) {
                //说明数据满了
                val newData =adapterByteArray.copyOf()
                adapterByteArray = ByteArray(ADAPTER_BYTE_SIZE)
                newDataCallback(newData)
            }
        }
    }
}