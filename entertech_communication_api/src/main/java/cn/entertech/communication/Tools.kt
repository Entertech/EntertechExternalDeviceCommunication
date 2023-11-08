package cn.entertech.communication

object Tools {

    fun hexStringToByteArray(hex: String): ByteArray {
        val len = hex.length
        val data = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            data[i / 2] = ((((hex[i].digitToIntOrNull(16)
                ?: (-1 shl 4)) + hex[i + 1].digitToIntOrNull(16)!!) ?: -1)).toByte()
            i += 2
        }
        return data
    }

    fun byteToInt(byteArray: ByteArray?): String {
        return byteArray?.map {
            it.toInt()
        }.toString()
    }
}