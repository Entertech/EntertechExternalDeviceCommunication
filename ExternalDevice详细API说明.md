# 详细API说明

### 外设-串口管理类

**方法说明**

该类集成了外设-串口的所有操作

**示例代码**

```kotlin
val manage = BaseExternalDeviceCommunicationManage.getManage(ExternalDeviceType.SERIAL_PORT)
```

### 设备连接

**方法说明**

连接串口设备

**示例代码**

```kotlin
  manage?.connectDevice(this, {
            Log.d(TAG, "connectDevice success")
        }) { errorCode, errorMsg ->
            Log.e(TAG, "errorCode: $errorCode  errorMsg: $errorMsg")
        }

```


**参数说明**

| 参数                    | 类型                           | 说明        |
| --------------------- | ---------------------------- | --------- |
| successConnect        | ((String) -> Unit)?          | 连接成功回调    |
| failure               | ((String) -> Unit)           | 连接失败回调    |
| connectionBleStrategy | ConnectionBleStrategy        | 连接类型    ｜ |
| filter                | (String?,String?) -> Boolean | 过滤逻辑    ｜ |


### 设备断开

**方法说明**

断开与设备的连接

**示例代码**

```kotlin
manage?.disConnectDevice()
```

### 获取设备连接状态

**方法说明**

获取当前设备连接状态

**示例代码**

```kotlin
val isConnected = manage?.isConnected()
```

**返回值说明**

| 参数          | 类型      | 说明                    |
| ----------- | ------- | --------------------- |
| isConnected | Boolean | 设备已连接为true，未连接为false。 |

### 设置监听接口

**监听接口生命周期需要管理，不需要监听了，请调用remove**
#### 添加原始脑波监听

**方法说明**

添加原始脑波监听，通过该监听可从硬件中获取原始脑波数据

**示例代码**

```kotlin
  var rawDataListener = fun(data:ByteArray){
        Logger.d(Arrays.toString(data))
  }
  manage?.addRawDataListener(rawDataListener)
```

**参数说明**

| 参数            | 类型                | 说明         |
| --------------- | ------------------- | ------------ |
| rawDataListener | （ByteArray）->Unit | 原始脑波回调 |

> **原始脑波数据说明**
>
> 从脑波回调中返回的原始脑波数据是一个长度为30的字节数组，其中脑波数据分左右两个通道，
> 依次为：左通道、左通道、左通道、右通道、右通道、右通道、左通道、左通道、左通道、右通道、右通道、右通道、左通道、左通道、左通道、右通道、右通道、右通道。。。。
>
> **正常数据示例**
>
> [0, -94, 21, -36, 125, 21, -12, -75, 22, 8, 61, 22, 10, -72, 22, 15, -19,20,10,8]
>
> **异常数据示例（未检测到脑波数据）**
>
> [-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,-1,-1,-1]

#### 移除原始脑波监听

**方法说明**

如果不想受到脑波数据，移除监听即可

**示例代码**

```kotlin
manage?.removeRawDataListener(rawDataListener)
```

**参数说明**

| 参数            | 类型                | 说明         |
| --------------- | ------------------- | ------------ |
| rawDataListener | （ByteArray）->Unit | 原始脑波回调 |


#### 添加心率监听

**方法说明**

添加心率监听，通过该监听可从硬件中获取心率数据

**示例代码**

```kotlin
var heartRateListener = fun(heartRate: Int) {
    Logger.d("heart rate data is " + heartRate)
}
manage?.addHeartRateListener(heartRateListener)
```

**参数说明**

| 参数                | 类型          | 说明       |
| ----------------- | ----------- | -------- |
| heartRateListener | （Int）->Unit | 心率数据获取回调 |

#### 移除心率监听

**方法说明**

如果不想收到心率，移除监听即可

**示例代码**

```kotlin
manage?.removeHeartRateListener(heartRateListener)
```

**参数说明**

| 参数                | 类型          | 说明     |
| ----------------- | ----------- | ------ |
| heartRateListener | （Int）->Unit | 心率数据回调 |

#### 添加佩戴信号监听

**方法说明**

添加该监听，可实时获取设备佩戴质量

**代码示例**

```kotlin
contactListener = fun(state: Int) {
   Logger.d("Whether the wearing contact is good:"+ state == 0);
}
manage?.addContactListener(contactListener)
    
```

**参数说明**

| 参数              | 类型          | 说明                      |
| --------------- | ----------- | ----------------------- |
| contactListener | （Int）->Unit | 佩戴信号回调。0:接触良好，其他值：未正常佩戴 |

#### 移除佩戴信号监听

**方法说明**

移除该监听，则不会受到佩戴信号

**代码示例**

```kotlin
manage?.removeContactListener(contactListener)
```

**参数说明**

| 参数              | 类型          | 说明     |
| --------------- | ----------- | ------ |
| contactListener | （Int）->Unit | 佩戴信号回调 |


**参数说明**

| 参数                     | 类型              | 说明     |
| ---------------------- | --------------- | ------ |
| batteryVoltageListener | （Double）-> Unit | 电池电压回调 |




#### 开始脑波和心率数据同时采集

**方法说明**

开始心率数据采集，调用这个接口开始同时采集脑波和心率数据

**示例代码**

```kotlin
biomoduleBleManager.startHeartAndBrainCollection()
```

#### 停止脑波和心率数据采集

**方法说明**

停止采集，调用该方法停止采集脑波和心率数据

**示例代码**

```kotlin
biomoduleBleManager.stopHeartAndBrainCollection()
```

