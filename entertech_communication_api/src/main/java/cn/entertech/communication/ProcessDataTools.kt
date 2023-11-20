package cn.entertech.communication

import cn.entertech.communication.log.ExternalDeviceCommunicateLog

/**
 * VR串口数据结构
 * | 包头  | 包长度  | 心率数据 | 脱落检测数据 | 第一个数据（左通道） | 第二个数据（右通道） | 第三个数据（左通道） | 第四个数据（右通道） | ........  | 第9个数据 （左通道） | 第10个数据 （右通道） | 校验位（单字节对比校验） | 包尾  |
 * | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
 * | 3字节 | 1字节 | 1字节 | 1字节 | 3个字节 | 3个字节 | 3个字节 | 3字节 | ........ | 3个字节 | 3个字节 | 1字节 | 3字节 |
 * | 0xBB-0xBB-0xBB  | 0x28 | 0x00(心率数据为0) | 0x00(0为佩戴正常，非0为脱落) | 00-01-02  | 03-04-05 | 06-07-08 | 09-0A-0B  | ........  | 00-01-02 | 00-01-02 | 0x77 | 0xEE-0xEE-0xEE  |
 *
 * */
object ProcessDataTools {
    private const val TAG = "ProcessDataTools"

    /**
     * 包头位 3字节
     * */
    private const val VR_SERIAL_PORT_DATA_PCK_START = "BB"

    /**
     * 校验位
     * */
    private const val VR_SERIAL_PORT_DATA_PCK_CHECK = "77"

    /**
     * 包尾位 3字节
     * */
    private const val VR_SERIAL_PORT_DATA_PCK_END = "EE"

    /**
     * 包头位 对应的int
     * */
    private val vrSerialPortDataPckHeadInt by lazy {
        Integer.parseInt(VR_SERIAL_PORT_DATA_PCK_START, 16)
    }


    private val vrSerialPortDataPckHeadByte by lazy {
        vrSerialPortDataPckHeadInt.toByte()
    }

    /**
     * 校验位 对应的int
     * */
    private val vrSerialPortDataPckCheckInt by lazy {
        Integer.parseInt(VR_SERIAL_PORT_DATA_PCK_CHECK, 16)
    }

    private val vrSerialPortDataPckCheckByte by lazy {
        vrSerialPortDataPckCheckInt.toByte()
    }

    /**
     * 包尾位 对应的int
     * */
    private val vrSerialPortDataPckEndInt by lazy {
        Integer.parseInt(VR_SERIAL_PORT_DATA_PCK_END, 16)
    }

    private val vrSerialPortDataPckEndByte by lazy {
        vrSerialPortDataPckEndInt.toByte()
    }

    /**
     * 是否已经开始了
     * */
    private var start = false


    private const val DATA_COUNT = 10

    /**
     * [mask] 每次移动的
     * */
    private const val MASK_STEP = 1
    private const val MASK_DEFAULT = -1

    /**
     * 包头起始位
     * */
    private const val MASK_VR_SERIAL_PORT_DATA_HEAD_START = 0

    /*
    * 包头结束位
    * */
    private const val MASK_VR_SERIAL_PORT_DATA_HEAD_END = MASK_VR_SERIAL_PORT_DATA_HEAD_START + 2

    /**
     * 数据总长度位
     * */
    private const val MASK_VR_SERIAL_PORT_DATA_LENGTH = MASK_VR_SERIAL_PORT_DATA_HEAD_END + 1


    /**
     * 心率数据
     * */
    private const val MASK_VR_SERIAL_PORT_DATA_HR = MASK_VR_SERIAL_PORT_DATA_LENGTH + 1


    /**
     * 脱落检测数据
     * */
    private const val MASK_VR_SERIAL_PORT_DATA_CHECK_CONTACT = MASK_VR_SERIAL_PORT_DATA_HR + 1

    /**
     * 数据位开头
     * */
    private const val MASK_VR_SERIAL_PORT_DATA_START = MASK_VR_SERIAL_PORT_DATA_CHECK_CONTACT + 1

    /**
     * 数据位末尾
     * */
    private const val MASK_VR_SERIAL_PORT_DATA_END =
        MASK_VR_SERIAL_PORT_DATA_START + 2 + (DATA_COUNT - 1) * 3

    /**
     * 校验位
     * */
    private const val MASK_VR_SERIAL_PORT_DATA_CHECK = MASK_VR_SERIAL_PORT_DATA_END + 1

    /**
     * 包尾起始位
     * */
    private const val MASK_VR_SERIAL_PORT_DATA_TAIL_START =
        MASK_VR_SERIAL_PORT_DATA_CHECK + 1

    /**
     * 包尾结束位
     * */
    private const val MASK_VR_SERIAL_PORT_DATA_TAIL_END = MASK_VR_SERIAL_PORT_DATA_TAIL_START + 2

    private var mask = MASK_DEFAULT


    private var data = ByteArray(DATA_COUNT * 3)

    /**
     * 先检验前3个是否是包头 3
     * 记录包长度 1
     * 心率数据 1字节
     * 脱落标志 1
     * 数据 10
     * 校验位 1
     * 3个包尾位 3
     *
     * 在当前方法帧中，byteInt所在的位置要早与mask标记的位置，也就是byteInt先入帧，然后mask被赋予byteInt所在的位置
     * 目前设定：心率&脱落标志与整体数据结构无关
     * */
    fun process(
        byteInt: Byte,
        contactListeners: List<((Int) -> Unit)?>,
        bioAndAffectDataListeners: List<((ByteArray) -> Unit)?>,
        heartRateListeners: List<((Int) -> Unit)>?,
        finish: (() -> Unit)? = null
    ) {
        if (byteInt == vrSerialPortDataPckHeadByte) {
            if (!start) {
                mask = MASK_VR_SERIAL_PORT_DATA_HEAD_START
                start = true
                return
            }
        }
        if (!start) {
            mask = MASK_DEFAULT
            ExternalDeviceCommunicateLog.d(TAG, "还未开始，数据无效")
            return
        }
        mask += MASK_STEP


        when (mask) {
            MASK_VR_SERIAL_PORT_DATA_HEAD_END -> {
                if (byteInt != vrSerialPortDataPckHeadByte) {
                    ExternalDeviceCommunicateLog.e(
                        TAG,
                        "包头校验 出错 byteInt $byteInt is not $vrSerialPortDataPckHeadInt "
                    )
                    reset()
                    return
                }
            }

            MASK_VR_SERIAL_PORT_DATA_HR -> {
                //心率数据
                heartRateListeners?.forEach {
                    it.invoke(byteInt.toInt())
                }
            }

            MASK_VR_SERIAL_PORT_DATA_CHECK_CONTACT -> {
                //脱落检测数据 0为佩戴正常，非0为脱落
                contactListeners?.forEach {
                    it?.invoke(byteInt.toInt())
                }
            }

            in MASK_VR_SERIAL_PORT_DATA_START..MASK_VR_SERIAL_PORT_DATA_END -> {
                //数据
                data[mask - MASK_VR_SERIAL_PORT_DATA_START] = byteInt
            }

            MASK_VR_SERIAL_PORT_DATA_CHECK -> {
                //校验位
                if (byteInt != vrSerialPortDataPckCheckByte) {
                    ExternalDeviceCommunicateLog.e(
                        TAG,
                        "校验位 出错 byteInt $byteInt is not $vrSerialPortDataPckCheckInt "
                    )
                    reset()
                    return
                }
            }

            in MASK_VR_SERIAL_PORT_DATA_TAIL_START..MASK_VR_SERIAL_PORT_DATA_TAIL_END -> {
                //包尾
                if (byteInt != vrSerialPortDataPckEndByte) {
                    ExternalDeviceCommunicateLog.e(
                        TAG,
                        "包尾校验 出错 byteInt $byteInt is not $vrSerialPortDataPckEndInt "
                    )
                    reset()
                    return
                }
                if (mask == MASK_VR_SERIAL_PORT_DATA_TAIL_END) {
                    finish?.invoke()
                    bioAndAffectDataListeners.forEach {
                        it?.invoke(data)
                    }
                    reset()
                }
            }
        }
    }

    private fun reset() {
        data = ByteArray(DATA_COUNT * 3)
        start = false
        mask = MASK_DEFAULT
    }


    /**
     * 只移除无用数据：包头，校验位，包尾
     * */
    fun dealSingleData(
        byteInt: Int,
        headCache: MutableList<Int>,
        endCache: MutableList<Int>,
        appendData: (Int) -> Unit,
        appendDataList: (List<Int>) -> Unit
    ) {
        //检查包头,连续三个BB，说明是包头，不添加
        if (byteInt == vrSerialPortDataPckHeadInt) {
            //怀疑是包头
            headCache.add(byteInt)
            if (headCache.size == 3) {
                //说明是包头 舍弃
                headCache.clear()
            }
            return
        }

        if (headCache.isNotEmpty()) {
            appendDataList(headCache)
            headCache.clear()
        }
        //检查包尾,校验位开始
        if (byteInt == vrSerialPortDataPckCheckInt) {
            //若当前尾部缓存不为空，说明不是包尾
            if (endCache.isNotEmpty()) {
                appendDataList(endCache)
                endCache.clear()
            }
            //怀疑该位是校验位
            endCache.add(byteInt)
            return
        } else {
            //可能是包尾位
            if (byteInt == vrSerialPortDataPckEndInt) {
                if (endCache.isNotEmpty()) {
                    endCache.add(byteInt)
                    //说明是包尾
                    if (endCache.size == 4) {
                        endCache.clear()
                    }
                    return
                }
            }
        }
        if (endCache.isNotEmpty()) {
            appendDataList(endCache)
            endCache.clear()
        }
        appendData(byteInt)
    }


}