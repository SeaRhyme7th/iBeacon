package com.example.air_sunny.blue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import android.util.Log;
import android.widget.Toast;

public class LeDeviceListAdapter extends BaseAdapter {

    public class Ble_BluetoothDevice {
        BluetoothDevice bluetoothDevice;
        int rssi;
        int txpower;
        double distance;
        //major：相当于群组号，同一个组里Beacon有相同的Major
        int major;
        //minor：相当于识别群组里单个的Beacon
        int minor;
        String proximityUuid;

        public Ble_BluetoothDevice(BluetoothDevice device, int rssi, int txpower, double distance,
                                   int major, int minor, String proximityUuid) {
            this.bluetoothDevice = device;
            this.rssi = rssi;
            this.txpower = txpower;
            this.distance = distance;
            this.major = major;
            this.minor = minor;
            this.proximityUuid = proximityUuid;
        }
    }


    // Adapter for holding devices found through scanning.

    private ArrayList<Ble_BluetoothDevice> mLeDevices;
    private LayoutInflater mInflator;
    private Activity mContext;
    // 这个嵌套的ArrayList 用于保存各个设备近期保存的rssi值，以便对测试的值进行平滑处理
    private ArrayList<ArrayList<Integer>> device_arr = new ArrayList<ArrayList<Integer>>();
    //点击事件要用
    private View viewMain;
    //存储测量数据
    private String[] sMeasure;
    private boolean flagMeasure;
    boolean flagWrite;
    Intent intent_le;
    private int[] devicename;
    private final static int IBEACON_DATACOUNT = 200;

    public LeDeviceListAdapter(Activity c) {
        super();
        mContext = c;
        mLeDevices = new ArrayList<>();
        mInflator = mContext.getLayoutInflater();
        //测量数据用到
        sMeasure = new String[2];
        sMeasure[0] = "";
        sMeasure[1] = "";
        flagMeasure = false;
        devicename = new int[2];
        devicename[0] = 0;
        devicename[1] = 0;
    }

//    //获取手机某目录下所有文件的路径
//    public void getAllFiles(File root) {
//
//        File files[] = root.listFiles();
//        if (files != null) {
//            for (File f : files) {
//                if (f.isDirectory()) {
//                    getAllFiles(f);
//                } else {
//                    Log.e("measure", "file:" + f);
//                }
//            }
//        }
//    }

//    public boolean MysaveFile(String filePath, String content) {
//        //保存文件
//        File file = new File(filePath);
//        Log.e("measure", "path:" + file.getPath());
//
//        try {
//            OutputStream outstream = new FileOutputStream(file);
//            OutputStreamWriter out = new OutputStreamWriter(outstream);
//            out.write(content);
//            out.close();
//            Log.e("measure", "write data success");
//            return true;
//        } catch (java.io.IOException e) {
//            Log.e("measure", "write data failed");
//            e.printStackTrace();
//            return false;
//        }
//    }

//    public boolean writeData(String str1, String str2) {
//        //保存数据到文件，类似txt以数据流写入存储
//        String filePath1 = "/sdcard/" + "E-Beacon_0EC0B6" + System.currentTimeMillis() + ".txt";
//        String filePath2 = "/sdcard/" + "E-Beacon_E4C5E0" + System.currentTimeMillis() + ".txt";
//        String content1 = str1;
//        String content2 = str2;
//        if (MysaveFile(filePath1, content1) && MysaveFile(filePath2, content2)) {
//            return true;
//        }
//        return false;
//    }

    public ArrayList<Ble_BluetoothDevice> getDevice() {
        return mLeDevices;
    }

    public int addDevice(Ble_BluetoothDevice device) {

        for (int i = 0; i < mLeDevices.size(); i++) {
            if (mLeDevices.get(i).bluetoothDevice.getAddress().equals(device.bluetoothDevice.getAddress())) {
                mLeDevices.get(i).rssi = smooth(i, device.rssi);
                mLeDevices.get(i).txpower = device.txpower;
                mLeDevices.get(i).distance = iBeaconClass.calculateAccuracy(mLeDevices.get(i).rssi);
                mLeDevices.get(i).major = device.major;
                mLeDevices.get(i).minor = device.minor;
                mLeDevices.get(i).proximityUuid = device.proximityUuid;
//                //测数据写入文件
//                if (devicename[0] < IBEACON_DATACOUNT || devicename[1] < IBEACON_DATACOUNT) {
//                    if (devicename[0] < IBEACON_DATACOUNT && mLeDevices.get(i).bluetoothDevice.getName().equals("E-Beacon_0EC0B6")) {
//                        Log.e("measure", "measure:E-Beacon_0EC0B6:" + (devicename[0] + 1));
//                        devicename[0]++;
//                        sMeasure[0] += " " + mLeDevices.get(i).rssi;
//                    }
//                    if (devicename[1] < IBEACON_DATACOUNT && mLeDevices.get(i).bluetoothDevice.getName().equals("E-Beacon_E4C5E0")) {
//                        Log.e("measure", "measure:E-Beacon_E4C5E0:" + (devicename[1] + 1));
//                        devicename[1]++;
//                        sMeasure[1] += " " + mLeDevices.get(i).rssi;
//                    }
//                }
//                if (flagMeasure == false && devicename[0] == IBEACON_DATACOUNT && devicename[1] == IBEACON_DATACOUNT) {
//                    flagWrite = writeData(sMeasure[0], sMeasure[1]);
//                    if ((devicename[1] == IBEACON_DATACOUNT) && (devicename[2] == IBEACON_DATACOUNT)) {
//                        flagMeasure = true;
//                    }
//                    if (flagWrite) {
//                        if (DeviceScanActivity.dActivity != null) {
//                            Toast.makeText(DeviceScanActivity.dActivity, "数据成功写入txt文件", Toast.LENGTH_LONG).show();
//                            intent_le = new Intent(DeviceScanActivity.dActivity, MainActivity.class);
//                            DeviceScanActivity.dActivity.startActivity(intent_le);
//                        }
//                        if (LocateActivity.lActivity != null) {
//                            Toast.makeText(LocateActivity.lActivity, "数据成功写入txt文件", Toast.LENGTH_LONG).show();
//                            intent_le = new Intent(LocateActivity.lActivity, MainActivity.class);
//                            LocateActivity.lActivity.startActivity(intent_le);
//                        }
//                    } else {
//                        if (DeviceScanActivity.dActivity != null) {
//                            Toast.makeText(DeviceScanActivity.dActivity, "数据写入txt文件失败！！！！", Toast.LENGTH_LONG).show();
//                            intent_le = new Intent(DeviceScanActivity.dActivity, MainActivity.class);
//                            DeviceScanActivity.dActivity.startActivity(intent_le);
//                        }
//                        if (LocateActivity.lActivity != null) {
//                            Toast.makeText(LocateActivity.lActivity, "数据写入txt文件失败！！！！", Toast.LENGTH_LONG).show();
//                            intent_le = new Intent(LocateActivity.lActivity, MainActivity.class);
//                            LocateActivity.lActivity.startActivity(intent_le);
//                        }
//                    }
//
//                }
                return mLeDevices.get(i).rssi;
            }
        }

        mLeDevices.add(device);
        //新增加一个设备，则添加一个ArrayList
        device_arr.add(new ArrayList<Integer>());
        return device.rssi;
    }

    /**
     * 用于平滑处理的函数，主要用于处理device_arr中的arraylist
     *
     * @param i 第几个设备   rssi 当前的device.rssi
     * @return 返回平滑处理的rssi
     */
    public int smooth(int i, int rssi) {
        //最近的7个数取平均数(去掉最大值以及最小值),
        int num = 0, min = rssi, max = rssi;
        int total = rssi;
        int len = device_arr.get(i).size();
        if (len >= 7) {
            device_arr.get(i).remove(0);
            len--;
        }
        for (int j = 0; j < len; j++) {
            num = device_arr.get(i).get(j);
            if (num > max) {
                max = num;
            }
            if (num < min) {
                min = num;
            }
            total += num;
        }
        //将total设置为rssi的值并且把add操作置后可以减少一次加法
        device_arr.get(i).add(rssi);
        len++;
        if (len == 7) {
            total = total - max - min;
            len = len - 2;
        }
        return total / len;
    }

    public void clear() {
        mLeDevices.clear();
    }

    @Override
    public int getCount() {
        return mLeDevices.size();
    }

    @Override
    public Object getItem(int i) {
        return mLeDevices.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        // General ListView optimization code.
        if (view == null) {
            if (DeviceScanActivity.dActivity != null) {
                view = mInflator.inflate(R.layout.listitem_device, null);
                viewHolder = new ViewHolder();
                viewHolder.deviceAddress = (TextView) view
                        .findViewById(R.id.device_address);
                viewHolder.deviceName = (TextView) view
                        .findViewById(R.id.device_name);
                viewHolder.deviceRSSI = (TextView) view.findViewById(R.id.device_rssi);
                viewHolder.deviceTxPower = (TextView) view.findViewById(R.id.device_tx_power);
                viewHolder.deviceDistance = (TextView) view.findViewById(R.id.device_distance);
                viewHolder.deviceMajor = (TextView) view.findViewById(R.id.device_major);
                viewHolder.deviceMinor = (TextView) view.findViewById(R.id.device_minor);
                viewHolder.deviceProximityUuid = (TextView) view.findViewById(R.id.device_proximityUuid);
                view.setTag(viewHolder);
            } else {
                //locateActivity对应的adapter
                view = mInflator.inflate(R.layout.listitem_locate, null);
                viewHolder = new ViewHolder();
                viewHolder.locateDetail = (TextView) view.findViewById(R.id.locate_listTv);
                view.setTag(viewHolder);
            }

        } else {
            if (DeviceScanActivity.dActivity != null) {
                //点击显示或隐藏详细信息
                viewMain = view;
                final ListView devicelv = DeviceScanActivity.dActivity.getListView();
                if (devicelv != null) {
                    devicelv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            LinearLayout layout = (LinearLayout) view.findViewById(R.id.device_detailLayout);
                            if (layout.getVisibility() == View.VISIBLE) {
                                layout.setVisibility(View.GONE);
                            } else {
                                layout.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                }
                viewHolder = (ViewHolder) view.getTag();
            } else {
                //locateActivity
                viewHolder = (ViewHolder) view.getTag();
            }

        }

        Ble_BluetoothDevice device = mLeDevices.get(i);
        if (DeviceScanActivity.dActivity != null) {
            final String deviceName = device.bluetoothDevice.getName();
            if (deviceName != null && deviceName.length() > 0)
                viewHolder.deviceName.setText("Device Name:" + deviceName);
            else
                viewHolder.deviceName.setText("Device Name:Unknown device");
            viewHolder.deviceAddress.setText("Bluetooth Address:" + device.bluetoothDevice.getAddress());
            viewHolder.deviceRSSI.setText("rssi:" + String.valueOf(device.rssi));
            viewHolder.deviceTxPower.setText("TxPower:" + String.valueOf(device.txpower));
            DecimalFormat df_device = new DecimalFormat("0.0");
            viewHolder.deviceDistance.setText("Distance:" + df_device.format(device.distance) + "m");
            viewHolder.deviceMajor.setText("Major:" + String.valueOf(device.major));
            viewHolder.deviceMinor.setText("Minor:" + String.valueOf(device.minor));
            viewHolder.deviceProximityUuid.setText("Uuid:" + device.proximityUuid);

            return view;
        } else {
            final String deviceName = device.bluetoothDevice.getName();
            DecimalFormat df_locate = new DecimalFormat("0.0");
            if (deviceName != null && deviceName.length() > 0) {
                viewHolder.locateDetail.setText("Apart from " + deviceName + " " + df_locate.format(device.distance) + " m");
            } else {
                viewHolder.locateDetail.setText("Apart from Unknown device " + df_locate.format(device.distance) + " m");
            }
            return view;
        }

    }

    class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
        TextView deviceRSSI;
        TextView deviceTxPower;
        TextView deviceDistance;
        TextView deviceMajor;
        TextView deviceMinor;
        TextView deviceProximityUuid;

        //locate
        TextView locateDetail;
    }
}
