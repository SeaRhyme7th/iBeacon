/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.airsunny.blue;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.widget.ListView;
import android.widget.Toast;

import com.example.airsunny.blue.iBeaconClass.iBeacon;

import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

import static com.example.airsunny.blue.iBeaconClass.fromScanData;

/**
 * Activity for scanning and displaying available Bluetooth LE devices.
 */
public class DeviceScanActivity extends SwipeBackActivity {

    //使用SwipeBackActivity达到右划返回的功能
    private SwipeBackLayout mSwipeBackLayout;
    public static DeviceScanActivity dActivity;
    private LeDeviceListAdapter mLeDeviceListAdapter;
    private ListView lv;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler;
    private static final long SCAN_PERIOD = 100000000;
    //flag:用来判断是否显示推送消息
    private boolean flag = true;
    private Toast toast;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        //保存当前信息
        super.onCreate(savedInstanceState);
        dActivity = this;
        rightSlideBack();
        initHandler();
        openBlueTooth();
    }

    //右划返回
    private void rightSlideBack() {
        mSwipeBackLayout = getSwipeBackLayout();
        mSwipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);
    }

    private void initHandler() {
        //handler用来在主线程和子线程间传递数据
        mHandler = new Handler();
    }

    // 对toast进行一个简单的封装
    public void toastShow(CharSequence message) {
        if (null == toast) {
            toast = Toast.makeText(DeviceScanActivity.this, message,
                    Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
        } else {
            toast.setText(message);
        }
        toast.show();
    }

    private void openBlueTooth() {
        //检查是否支持ble，不支持就退出app
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            toastShow("BLE is not supported");
            finish();
        }
        //获取蓝牙适配
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        //检查是否支持蓝牙
        if (mBluetoothAdapter == null) {
            toastShow("Bluetooth not supported.");
            finish();
            return;
        }
        //开启蓝牙
        mBluetoothAdapter.enable();
    }

    //获取listview
    public ListView getListView() {
        return lv;
    }

    //app恢复运行
    @Override
    protected void onResume() {
        super.onResume();
        dActivity = this;
        //页面显示适配器：用于更新页面信息
        setContentView(R.layout.listmain_device);
        lv = (ListView) findViewById(R.id.device_listView);
        mLeDeviceListAdapter = new LeDeviceListAdapter(this);
        lv.setAdapter(mLeDeviceListAdapter);
        scanLeDevice(true);
    }

    //app暂停
    @Override
    protected void onPause() {
        super.onPause();
        dActivity = null;
        if (toast != null) {
            toast.cancel();
        }
        //停止扫描ble设备
        scanLeDevice(false);
        //页面ListView内容清空
        mLeDeviceListAdapter.clear();
    }

    @Override
    protected void onStop() {
        dActivity = null;
        if (toast != null) {
            toast.cancel();
        }
        super.onStop();
    }

    //扫描ble设备
    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            //设置一个定时器：SCAN_PERIOD时间后启动子线程:停止扫描ble设备
            mHandler.postDelayed(new Runnable() {
                //子线程内
                @Override
                public void run() {
                    //扫描标志位设为false
                    mScanning = false;
                    //停止扫描
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    //菜单更新
                    invalidateOptionsMenu();
                }
            }, SCAN_PERIOD);
            //扫描标志位设为true
            mScanning = true;
            //开始扫描
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            //扫描标志位设为false
            mScanning = false;
            //停止扫描ble设备
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
        //更新菜单
        invalidateOptionsMenu();
    }

    // Device scan callback.
    //扫描设备的回调函数
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //过滤Ibeacon以外的蓝牙设备
                            iBeacon ibeacon = fromScanData(device, rssi, scanRecord);
                            if (ibeacon != null) {
                                LeDeviceListAdapter leDeviceListAdapter = new LeDeviceListAdapter(DeviceScanActivity.this);
                                LeDeviceListAdapter.Ble_BluetoothDevice ble_bluetoothDevice = leDeviceListAdapter.new Ble_BluetoothDevice(
                                        device, rssi, ibeacon.txPower, iBeaconClass.calculateAccuracy(rssi), ibeacon.major, ibeacon.minor, ibeacon.proximityUuid
                                );
                                // return 的值
                                int chufa = mLeDeviceListAdapter.addDevice(ble_bluetoothDevice);
                                mLeDeviceListAdapter.notifyDataSetChanged();
                                //根据chufa的值发送信息
                                if (flag && chufa > (-75)) {
                                    Intent intent = new Intent(DeviceScanActivity.this, NotificationService.class);
                                    startService(intent);
                                    flag = false;
                                }
                                if (chufa < -75) {
                                    flag = true;
                                }
                            }
                        }
                    });
                }
            };
}