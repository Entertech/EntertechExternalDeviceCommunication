package cn.entertech.device

import android.content.Context
import android.util.Log
import java.io.File
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.Date


class FileSaveTools {
    private val sim by lazy {
        SimpleDateFormat("yyyy年MM月dd日HH:mm:ss:SSS")
    }
    private var printWriter: PrintWriter? = null
    private var file: File? = null
    private var isFirst = true
//    val stringBuild1 = StringBuilder()
//    val stringBuild2 = StringBuilder()


    fun appendData(context: Context, data: ByteArray) {
        if (file == null) {
            createFile(context)
        }
        data.forEach { byte ->
//            val dataHex = Integer.toHexString(byte.toInt() and 0xff)
            val dataHex = byte.toInt() and 0xff
//            stringBuild1.append(dataHex).append(", ")
            if (isFirst) {
//                    stringBuild2.append("0, ")
                printWriter?.print(dataHex)
                isFirst = false
            } else {
//                    stringBuild2.append("0, ")
                printWriter?.append(dataHex.toString())
            }
//                stringBuild2.append("0, ")
//            stringBuild2.append(dataHex.toString())
//            stringBuild2.append(", ")
            printWriter?.append(", ")
            printWriter?.flush()
//        Log.d("FileSaveTools ", "stringBuild1: ${stringBuild1}, ")
//        Log.d("FileSaveTools ", "stringBuild2: ${stringBuild2}, ")
        }
    }

    fun finishAppendData() {
        file = null
        printWriter?.flush()
        printWriter = null
        isFirst = true
    }

    private fun createFile(context: Context) {
        file = File(
            getSaveFileDirectory(context),
            sim.format(Date(System.currentTimeMillis())) + ".txt"
        )
        file?.apply {
            Log.d("FileSaveTools ", "createFile: ${this.absolutePath}, ")
            if (!exists()) {
                parentFile?.mkdirs()
            }
            createNewFile()
            printWriter = PrintWriter(this)
        }

    }

    private fun getSaveFileDirectory(context: Context): File {
        /*   val files = getExternalFilesDirs(context, Environment.MEDIA_MOUNTED)
           val fileSize = files.size
           val file = if (files.isNotEmpty()) {
               files[fileSize - 1]
           } else {
               context.getExternalFilesDir("") ?: context.filesDir
           }*/
        return File(
            context.getExternalFilesDir("") ?: context.filesDir,
            "serialPortData"
        )
    }
}