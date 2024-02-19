package cn.entertech.serialport;

import junit.framework.TestCase;

public class SerialDataUtilsTest extends TestCase {

    public void testHexToByteArr() {
        String s="03";
        System.out.println(SerialDataUtils.removeOx(s));
    }
}