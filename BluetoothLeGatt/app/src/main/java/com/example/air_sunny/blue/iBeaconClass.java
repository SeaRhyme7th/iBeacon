package com.example.air_sunny.blue;

/**
 * Created by jerry123 on 2016/10/22.
 */

import android.bluetooth.BluetoothDevice;

import static java.lang.Math.pow;

import android.util.Log;

/**
 * 在iBbeaconClass类中对其进行数据的解析处理:
 * 详细的参考git仓库：https://github.com/AltBeacon/android-beacon-library
 */

public class iBeaconClass {

    static public class iBeacon {
        public String name;
        //major：相当于群组号，同一个组里Beacon有相同的Major
        public int major;
        //minor：相当于识别群组里单个的Beacon
        public int minor;
        //proximityUuid：厂商识别号
        public String proximityUuid;
        public String bluetoothAddress;
        //txPower:距离设备1米测得的信号强度值
        public int txPower;
        // RSSI的值作为对远程蓝牙设备的报告; 0代表没有蓝牙设备;
        public int rssi;
    }

    //对硬件获取到的蓝牙信息进行处理
    public static iBeacon fromScanData(BluetoothDevice device, int rssi, byte[] scanData) {

        int startByte = 2;
        boolean patternFound = false;
        while (startByte <= 5) {
            if (((int) scanData[startByte + 2] & 0xff) == 0x02 &&
                    ((int) scanData[startByte + 3] & 0xff) == 0x15) {
                // yes!  This is an iBeacon
                patternFound = true;
                break;
            } else if (((int) scanData[startByte] & 0xff) == 0x2d &&
                    ((int) scanData[startByte + 1] & 0xff) == 0x24 &&
                    ((int) scanData[startByte + 2] & 0xff) == 0xbf &&
                    ((int) scanData[startByte + 3] & 0xff) == 0x16) {
                iBeacon iBeacon = new iBeacon();
                iBeacon.major = 0;
                iBeacon.minor = 0;
                iBeacon.proximityUuid = "00000000-0000-0000-0000-000000000000";
                iBeacon.txPower = -55;
                return iBeacon;
            } else if (((int) scanData[startByte] & 0xff) == 0xad &&
                    ((int) scanData[startByte + 1] & 0xff) == 0x77 &&
                    ((int) scanData[startByte + 2] & 0xff) == 0x00 &&
                    ((int) scanData[startByte + 3] & 0xff) == 0xc6) {

                iBeacon iBeacon = new iBeacon();
                iBeacon.major = 0;
                iBeacon.minor = 0;
                iBeacon.proximityUuid = "00000000-0000-0000-0000-000000000000";
                iBeacon.txPower = -55;
                return iBeacon;
            }
            startByte++;
        }


        if (!patternFound) {
            // This is not an iBeacon
            return null;
        }

        iBeacon iBeacon = new iBeacon();

        iBeacon.major = (scanData[startByte + 20] & 0xff) * 0x100 + (scanData[startByte + 21] & 0xff);
        iBeacon.minor = (scanData[startByte + 22] & 0xff) * 0x100 + (scanData[startByte + 23] & 0xff);
        iBeacon.txPower = (int) scanData[startByte + 24]; // this one is signed
        iBeacon.rssi = rssi;

        // AirLocate:
        // 02 01 1a 1a ff 4c 00 02 15  # Apple's fixed iBeacon advertising prefix
        // e2 c5 6d b5 df fb 48 d2 b0 60 d0 f5 a7 10 96 e0 # iBeacon profile uuid
        // 00 00 # major
        // 00 00 # minor
        // c5 # The 2's complement of the calibrated Tx Power

        // Estimote:
        // 02 01 1a 11 07 2d 24 bf 16
        // 394b31ba3f486415ab376e5c0f09457374696d6f7465426561636f6e00000000000000000000000000000000000000000000000000

        byte[] proximityUuidBytes = new byte[16];
        System.arraycopy(scanData, startByte + 4, proximityUuidBytes, 0, 16);
        String hexString = bytesToHexString(proximityUuidBytes);
        iBeacon.proximityUuid = hexString.substring(0, 8) + "-" + hexString.substring(8, 12) + "-" + hexString.substring(12, 16) + "-" + hexString.substring(16, 20) + "-" + hexString.substring(20, 32);

        if (device != null) {
            iBeacon.bluetoothAddress = device.getAddress();
            iBeacon.name = device.getName();
        }

        return iBeacon;
    }

    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (byte aSrc : src) {
            int v = aSrc & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    //计算距离：不同的手机还需要修改系数
    public static double calculateAccuracy(int rssi) {
        if (rssi >= 0) {
            return -1.0; // if we cannot determine accuracy, return -1.
        }
        //1-9米所得数据测得方程
//        //distance = a*exp(b*rssi):卡尔曼滤波
//        double txPower = -62.42;
//        double ratio = rssi * 1.0 / txPower;
//        if (ratio < 1.0) {
//            double distance = 3.798 * pow(ratio, 0.8103) - 2.742;
//            if (distance < 0)
//                return 0;
//            return 3.798 * pow(ratio, 0.8103) - 2.742;
//        } else {
//            return 0.03361 * pow(ratio, 12.28) + 0.896;
//        }
//        //distance = a*exp(b*rssi):中值滤波
//        double txPower = -61.7591;
//        double ratio = rssi * 1.0 / txPower;
//        if (ratio < 1.0) {
//            double distance = 3.798 * pow(ratio, 0.8103) - 2.742;
//            if (distance < 0)
//                return 0;
//            return distance;
//        } else {
//            if(ratio < 1.3665){
//                double distance = 0.06405 * pow(ratio, 11.81) +0.9369;
//                Log.e("1-3.5",ratio+":"+String.valueOf(distance));
//                if (distance < 0)
//                    return 0;
//                return distance;
//            }else{
//                double distance = 0.2891 * pow(ratio, 7.654) -0.2529;
//                Log.e("3.5-",ratio+":"+String.valueOf(distance));
//                if (distance < 0)
//                    return 0;
//                return distance;
//            }
//
//        }
//        //distance = a*exp(b*rssi):高斯滤波
//        double txPower = -61.9447;
//        double ratio = rssi * 1.0 / txPower;
//        if (ratio < 1.0) {
//            double distance = 3.798 * pow(ratio, 0.8103) - 2.742;
//            if (distance < 0)
//                return 0;
//            return 3.798 * pow(ratio, 0.8103) - 2.742;
//        } else {
//            return 0.03418 * pow(ratio, 12.24) -0.8913;
//        }

//        //1-5米数据所得方程
//        double txPower = -56.6451;
//        double ratio = rssi * 1.0 / txPower;
//        if (ratio < 1.0) {
//            double distance = 3.798 * pow(ratio, 0.8103) - 2.742;
//            if (distance < 0)
//                return 0;
//            return distance;
//        } else {
//            double distance = 10.71 * pow(ratio, 1.942) -9.6;
//            Log.e("3.5-",ratio+":"+String.valueOf(distance));
//            if (distance < 0)
//                return 0;
//            return distance;
//        }

        //1-7米数据所得方程
        //中值滤波（去部分较大较小值）
        double txPower = -56.8497;
        double ratio = rssi * 1.0 / txPower;
        if (ratio < 1.0) {
            double distance = 1.003 * pow(ratio, 4.798) + 0.02281;
            if (distance < 0)
                return 0;
            return distance;
        } else {
            double distance = 7.233 * pow(ratio, 2.797) - 6.099;
            if (distance < 0)
                return 0;
            return distance;
        }
    }
}