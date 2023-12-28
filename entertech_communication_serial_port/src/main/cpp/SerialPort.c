/*
 * Copyright 2009-2011 Cedric Priscal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include <termios.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <string.h>
#include <jni.h>
#include <stdlib.h>

#include "SerialPort.h"

#include "android/log.h"

static const char *TAG = "serial_port";
#define LOGI(fmt, args...) __android_log_print(ANDROID_LOG_INFO,  TAG, fmt, ##args)
#define LOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, TAG, fmt, ##args)
#define LOGE(fmt, args...) __android_log_print(ANDROID_LOG_ERROR, TAG, fmt, ##args)
#define LOOPBACK	0x8000
#define MESSAGE		"Test"
#define MESSAGE_SIZE	sizeof(MESSAGE)
static speed_t getBaudrate(jint baudrate)
{
	switch(baudrate) {
		case 0: return B0;
		case 50: return B50;
		case 75: return B75;
		case 110: return B110;
		case 134: return B134;
		case 150: return B150;
		case 200: return B200;
		case 300: return B300;
		case 600: return B600;
		case 1200: return B1200;
		case 1800: return B1800;
		case 2400: return B2400;
		case 4800: return B4800;
		case 9600: return B9600;
		case 19200: return B19200;
		case 38400: return B38400;
		case 57600: return B57600;
		case 115200: return B115200;
		case 230400: return B230400;
		case 460800: return B460800;
		case 500000: return B500000;
		case 576000: return B576000;
		case 921600: return B921600;
		case 1000000: return B1000000;
		case 1152000: return B1152000;
		case 1500000: return B1500000;
		case 2000000: return B2000000;
		case 2500000: return B2500000;
		case 3000000: return B3000000;
		case 3500000: return B3500000;
		case 4000000: return B4000000;
		default: return -1;
	}
}

/*
 * Class:     android_serialport_SerialPort
 * Method:    open
 * Signature: (Ljava/lang/String;II)Ljava/io/FileDescriptor;
 */
JNIEXPORT jobject JNICALL Java_android_serialport_SerialPort_open
		(JNIEnv *env, jclass thiz, jstring path, jint baudrate, jint stopBits, jint dataBits,
		 jint parity, jint flowCon, jint flags) {
	int fd;
	speed_t speed;
	jobject mFileDescriptor;

	/* Check arguments */
	{
		speed = getBaudrate(baudrate);
		if (speed == -1) {
			/* TODO: throw an exception */
			LOGE("Invalid baudrate");
			return NULL;
		}
	}

	/* Opening device */
	{
		jboolean iscopy;
		const char *path_utf = (*env)->GetStringUTFChars(env, path, &iscopy);
		LOGD("Opening serial port %s with flags 0x%x", path_utf, O_RDWR | flags);
		fd = open(path_utf, O_RDWR | flags);
		LOGD("open() fd = %d", fd);
		(*env)->ReleaseStringUTFChars(env, path, path_utf);
		if (fd == -1) {
			/* Throw an exception */
			LOGE("Cannot open port");
			/* TODO: throw an exception */
			return NULL;
		}
	}

	/* Configure device */
	{
		struct termios cfg;
		LOGD("Configuring serial port");
		if (tcgetattr(fd, &cfg)) {
			LOGE("tcgetattr() failed");
			close(fd);
			/* TODO: throw an exception */
			return NULL;
		}

		cfmakeraw(&cfg);
		cfsetispeed(&cfg, speed);
		cfsetospeed(&cfg, speed);

		cfg.c_cflag &= ~CSIZE;
		switch (dataBits) {
			case 5:
				cfg.c_cflag |= CS5;    //使用5位数据位
				break;
			case 6:
				cfg.c_cflag |= CS6;    //使用6位数据位
				break;
			case 7:
				cfg.c_cflag |= CS7;    //使用7位数据位
				break;
			case 8:
				cfg.c_cflag |= CS8;    //使用8位数据位
				break;
			default:
				cfg.c_cflag |= CS8;
				break;
		}

		switch (parity) {
			case 0:
				cfg.c_cflag &= ~PARENB;    //无奇偶校验
				break;
			case 1:
				cfg.c_cflag |= (PARODD | PARENB);   //奇校验
				break;
			case 2:
				cfg.c_iflag &= ~(IGNPAR | PARMRK); // 偶校验
				cfg.c_iflag |= INPCK;
				cfg.c_cflag |= PARENB;
				cfg.c_cflag &= ~PARODD;
				break;
			default:
				cfg.c_cflag &= ~PARENB;
				break;
		}

		switch (stopBits) {
			case 1:
				cfg.c_cflag &= ~CSTOPB;    //1位停止位
				break;
			case 2:
				cfg.c_cflag |= CSTOPB;    //2位停止位
				break;
			default:
				break;
		}

		// hardware flow control
		switch (flowCon) {
			case 0:
				cfg.c_cflag &= ~CRTSCTS;    //不使用流控
				break;
			case 1:
				cfg.c_cflag |= CRTSCTS;    //硬件流控
				break;
			case 2:
				cfg.c_cflag |= IXON | IXOFF | IXANY;    //软件流控
				break;
			default:
				cfg.c_cflag &= ~CRTSCTS;
				break;
		}


		if (tcsetattr(fd, TCSANOW, &cfg)) {
			LOGE("tcsetattr() failed");
			close(fd);
			/* TODO: throw an exception */
			return NULL;
		}
	}

	/* Create a corresponding file descriptor */
	{
		jclass cFileDescriptor = (*env)->FindClass(env, "java/io/FileDescriptor");
		jmethodID iFileDescriptor = (*env)->GetMethodID(env, cFileDescriptor, "<init>", "()V");
		jfieldID descriptorID = (*env)->GetFieldID(env, cFileDescriptor, "descriptor", "I");
		mFileDescriptor = (*env)->NewObject(env, cFileDescriptor, iFileDescriptor);
		(*env)->SetIntField(env, mFileDescriptor, descriptorID, (jint) fd);
	}

	return mFileDescriptor;
}

/*
 * Class:     cedric_serial_SerialPort
 * Method:    close
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_android_serialport_SerialPort_close
		(JNIEnv *env, jobject thiz) {
	jclass SerialPortClass = (*env)->GetObjectClass(env, thiz);
	jclass FileDescriptorClass = (*env)->FindClass(env, "java/io/FileDescriptor");

	jfieldID mFdID = (*env)->GetFieldID(env, SerialPortClass, "mFd", "Ljava/io/FileDescriptor;");
	jfieldID descriptorID = (*env)->GetFieldID(env, FileDescriptorClass, "descriptor", "I");

	jobject mFd = (*env)->GetObjectField(env, thiz, mFdID);
	jint descriptor = (*env)->GetIntField(env, mFd, descriptorID);

	LOGD("close(fd = %d)", descriptor);
	close(descriptor);
}

int uart_file1;
char buf[5];
unsigned int line_val;
struct termios mxc, old;
char **argv;

JNIEXPORT void JNICALL
Java_android_serialport_SerialPort_init(JNIEnv *env, jobject thiz) {
	LOGD("init");

	jstring argString[2] = {"stty_demo","/dev/ttyHS1"};
    argv= (char **) argString;
    LOGD("\n---- Running < %s > test ----\n\n",argv[0]);

	/* Open the specified UART device */
	if ((uart_file1 = open(*++argv, O_RDWR)) == -1) {
		LOGD("Error opening %s\n", *argv);
		exit(1);
	} else {
		LOGD("%s opened\n", *argv);
	}
	LOGD("init333");

	tcgetattr(uart_file1, &old);
	cfsetispeed(&old, B115200);
	cfsetospeed(&old, B115200);
	mxc = old;
	mxc.c_lflag &= ~(ICANON | ECHO | ISIG);
	tcsetattr(uart_file1, TCSANOW, &mxc);
	LOGD("Attributes set\n");

	line_val = LOOPBACK;
	ioctl(uart_file1, TIOCMSET, &line_val);
	LOGD("Test: IOCTL Set\n");

	tcflush(uart_file1, TCIOFLUSH);

	write(uart_file1, MESSAGE, MESSAGE_SIZE);
	LOGD("Data Written= %s\n", MESSAGE);

	sleep(1);
	memset(buf, 0, MESSAGE_SIZE);
	int retval = 0;
	int retries = 5;
	while (retries-- && retval < 5)
		retval += read(uart_file1, buf + retval, MESSAGE_SIZE - retval);
	LOGD("Data Read back= %s\n", buf);
	sleep(2);
	LOGD("ioctl TIOCMBIC start\n");
	ioctl(uart_file1, TIOCMBIC, &line_val);
	LOGD("tcsetattr TCSAFLUSH start\n");
	retval = tcsetattr(uart_file1, TCSAFLUSH, &old);
	LOGD("close file start\n");
	close(uart_file1);

	if (memcmp(buf, MESSAGE, MESSAGE_SIZE)) {
		LOGD("Data read back %s is different than data sent %s\n",
			 buf, MESSAGE);
		LOGD("\n---- Test < %s > end ----\n\n",argv[0]);
		return;
	}
	LOGD("Data read back is same with sent\n" );
	LOGD("\n---- Test < %s > end ----\n\n",argv[0]);

}


JNIEXPORT void JNICALL
Java_android_serialport_SerialPort_initRead(JNIEnv *env, jclass thiz) {
	LOGD("Data Read start");
	int retval = 0;
	int retries = 5;
	while (retries-- && retval < 5)
		retval += read(uart_file1, buf + retval, MESSAGE_SIZE - retval);
	LOGD("Data Read back= %s\n", buf);
	sleep(2);

	ioctl(uart_file1, TIOCMBIC, &line_val);

	retval = tcsetattr(uart_file1, TCSAFLUSH, &old);

	close(uart_file1);

	if (memcmp(buf, MESSAGE, MESSAGE_SIZE)) {
		LOGD("Data read back %s is different than data sent %s\n",
			 buf, MESSAGE);
		LOGD("\n---- Test < %s > end ----\n\n",argv[0]);
		return;
	}
	LOGD("Data read back is same with sent\n" );
	LOGD("\n---- Test < %s > end ----\n\n",argv[0]);

}

JNIEXPORT void JNICALL
Java_android_serialport_SerialPort_initWrite(JNIEnv *env, jclass clazz) {
	LOGD("initWrite");

	jstring argString[2] = {"stty_demo","/dev/ttyHS1"};
	argv= (char **) argString;
	LOGD("\n---- Running < %s > test ----\n\n",argv[0]);

	/* Open the specified UART device */
	if ((uart_file1 = open(*++argv, O_RDWR)) == -1) {
		LOGD("Error opening %s\n", *argv);
		exit(1);
	} else {
		LOGD("%s opened\n", *argv);
	}
	LOGD("init333");

	tcgetattr(uart_file1, &old);
	mxc = old;
	mxc.c_lflag &= ~(ICANON | ECHO | ISIG);
	tcsetattr(uart_file1, TCSANOW, &mxc);
	LOGD("Attributes set\n");

	line_val = LOOPBACK;
	ioctl(uart_file1, TIOCMSET, &line_val);
	LOGD("Test: IOCTL Set\n");

	tcflush(uart_file1, TCIOFLUSH);

	write(uart_file1, MESSAGE, MESSAGE_SIZE);
	LOGD("Data Written= %s\n", MESSAGE);
}