package cn.entertech.device

import android.content.Context
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
    private var index = 0
    private var isFirst = true

    fun appendData(context: Context, data: ByteArray) {
        if (file == null) {
            createFile(context)
        }
        data.forEach { byte ->
            if (index % 20 == 0) {
                if (isFirst) {
                    printWriter?.print("0, ")
                    isFirst = false
                } else {
                    printWriter?.append("0, ")
                }
                ++index
                printWriter?.append("0, ")
            } else {
                printWriter?.append((byte.toInt() and 0xff).toString())
                printWriter?.append(", ")
            }
            ++index
        }
        printWriter?.flush()
    }

    fun finishAppendData() {
        file = null
        printWriter?.flush()
        printWriter = null
        index = 0
        isFirst = false
    }

    private fun createFile(context: Context) {
        file = File(
            getSaveFileDirectory(context),
            sim.format(Date(System.currentTimeMillis())) + ".txt"
        )
        file?.apply {
            if (!exists()) {
                parentFile?.mkdirs()
            }
            createNewFile()
            printWriter = PrintWriter(this)
        }

    }

    private fun getSaveFileDirectory(context: Context): File {
        return File(
            context.getExternalFilesDir("") ?: context.filesDir,
            "serialPortData"
        )
    }
}