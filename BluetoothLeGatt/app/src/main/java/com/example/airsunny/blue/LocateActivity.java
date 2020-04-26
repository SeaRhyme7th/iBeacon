package com.example.airsunny.blue;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.util.Log;

import com.example.airsunny.blue.entity.DataEvent;
import com.example.airsunny.blue.network.EventCode;
import com.example.airsunny.blue.network.NetworkUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.ArrayList;

import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.example.airsunny.blue.iBeaconClass.fromScanData;

public class LocateActivity extends SwipeBackActivity {

    //使用SwipeBackActivity达到右划返回的功能
    private SwipeBackLayout mSwipeBackLayout;
    ImageButton uploadButton;
    ImageButton findFriendsButton;
    LinearLayout imageviewlayout;
    public static LocateActivity lActivity;
    private LeDeviceListAdapter mLeDeviceListAdapter;
    private ListView lv;
    private Toast toast;
    private BluetoothAdapter mBluetoothAdapter;
    //是否开始扫描
    private boolean mScanning;
    //Handler:线程间数据传递
    private Handler mHandler;
    private Handler oHandler;
    private Handler findFriendHandler;
    private Handler uploadHandler;
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 100000000;
    //handler标识
    private static final int UPDATEPIC = 100;
    private static final int FINDFRIENDPIC = 300;
    private static final int FINDFRIENDHANDLE = 400;
    private static final int UPLOADHANDLE = 500;
    //flag:用来判断是否显示推送消息
    private boolean flag = true;
    //收集ibeacon设备的信息
    ArrayList<LeDeviceListAdapter.Ble_BluetoothDevice> deviceDistance;
    //画位置图
    Draw view;
    //findfriend的弹出框
    private AlertDialog.Builder builder;
    //friend位置坐标
    private String[] strPosition;
    //判断是否Draw添加view:首次添加后不能再次添加
    boolean a = true;
    //是否存在此好友
    boolean friendExistFlag = true;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locate);
        EventBus.getDefault().register(this);
        lActivity = this;
        //上传按钮
        addUploadButton();
        //寻找朋友按钮（查看朋友的位置）
        addFindFriendsButton();
        //增加点击事件监听，点击弹出图例说明
        listenImage();
        //右划返回
        rightSlideReturn();
        //主线程与子线程交互的handler的初始化
        initHandler();
        //检测是否支持ibeacon、打开蓝牙
        getBlueTooth();
    }

    private void initHandler() {
        //handler用来在主线程和子线程间传递数据
        mHandler = new Handler();
        //主线程中定义Handler
        oHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case UPDATEPIC:
                        if (view.getFlagStr().equals("me")) {
                            updatePicture(msg.arg1, msg.arg2);
                        } else {
                            if(friendExistFlag){
                                updateFriend(strPosition);
                            }
                        }
                        break;
                    case FINDFRIENDPIC:
                        strPosition = (String[]) msg.obj;
                        Log.e("obj",strPosition[0]);
                        if(!(strPosition[0].equals("friend"))){
                            view.setFlagStr("friend");
                            updateFriend(strPosition);
                            friendExistFlag = true;
                        }else{
                            friendExistFlag = false;
                        }
                        break;
                    default:
                        break;
                }
            }
        };
    }

    private void updateFriend(String[] str) {
        int me_x = 0, me_y = 0;
        str[0] = str[0].substring(0, 7);
        str[1] = str[1].substring(0, 7);
        view.setFriendX(Integer.parseInt(str[0]));
        view.setFriendY(Integer.parseInt(str[1]));
        view.setMinimumHeight(200);
        view.setMinimumWidth(300);
        deviceDistance = mLeDeviceListAdapter.getDevice();
        if (deviceDistance.size() > 1) {
            //确定两个距离各自对应的设备,arg1对应E-Beacon_0EC0B6,arg2对应E-Beacon_E4C5E0
            if (deviceDistance.get(0).bluetoothDevice.getName().equals("E-Beacon_0EC0B6")) {
                me_x = (int) (deviceDistance.get(0).distance * 1000);
                me_y = (int) (deviceDistance.get(1).distance * 1000);
            } else {
                me_x = (int) (deviceDistance.get(1).distance * 1000);
                me_y = (int) (deviceDistance.get(0).distance * 1000);
            }
        }
        view.setMyX(me_x);
        view.setMyY(me_y);

        //通知view组件重绘
        view.invalidate();
        if (a) {
            //第一次加载时添加视图，之后不需要
            imageviewlayout.addView(view);
            a = false;
        }
    }

    //更新位置图
    private void updatePicture(float x, float y) {
        //画位置图
        view.setFlagStr("me");
        view.setMyX(x);
        view.setMyY(y);
        view.setMinimumHeight(200);
        view.setMinimumWidth(300);
        //通知view组件重绘
        view.invalidate();
        if (a) {
            //第一次加载时添加视图，之后不需要
            imageviewlayout.addView(view);
            a = false;
        }
    }

    private void rightSlideReturn() {
        mSwipeBackLayout = getSwipeBackLayout();
        mSwipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);
    }

    private void getBlueTooth() {
        //检查是否支持ble，不支持就退出app
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(LocateActivity.this,"BLE is not supported.",Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        //获取蓝牙适配
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        //检查是否支持蓝牙
        if (mBluetoothAdapter == null) {
            Toast.makeText(LocateActivity.this,"Bluetooth not supported.",Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        //开启蓝牙
        mBluetoothAdapter.enable();
    }

    //子线程发消息，通知Handler完成UI更新
    OkHttpClient mOkHttpClient = new OkHttpClient();

    private void addUploadButton() {
        uploadButton = (ImageButton) findViewById(R.id.uploadButton);
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final LinearLayout layout = new LinearLayout(LocateActivity.this);
                final EditText YourId = new EditText(LocateActivity.this);
                YourId.setHint("your name");
                layout.addView(YourId);
                YourId.setWidth(800);
                layout.setPadding(50, 0, 50, 0);
                builder = new AlertDialog.Builder(LocateActivity.this);
                builder.setView(null);
                builder.setView(layout);
                builder.setTitle("Upload");
                builder.setMessage("Are you sure to share your position with your friends?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String yourName = YourId.getText().toString();
                        if (!"".equals(yourName)) {
                            deviceDistance = mLeDeviceListAdapter.getDevice();
                            int dis_1 = 0, dis_2 = 0;
                            if (deviceDistance.size() > 1) {
                                //确定两个距离各自对应的设备,arg1对应E-Beacon_0EC0B6,arg2对应E-Beacon_E4C5E0
                                if ("E-Beacon_0EC0B6".equals(deviceDistance.get(0).bluetoothDevice.getName())) {
                                    dis_1 = (int) (deviceDistance.get(0).distance * 1000);
                                    dis_2 = (int) (deviceDistance.get(1).distance * 1000);
                                } else {
                                    dis_1 = (int) (deviceDistance.get(1).distance * 1000);
                                    dis_2 = (int) (deviceDistance.get(0).distance * 1000);
                                }
                            }
                            NetworkUtil.getInstance().uploadPosition(yourName, String.valueOf(dis_1), String.valueOf(dis_2));
                        }
                    }
                });
                builder.setNegativeButton("No", null);
                builder.create().show();
            }
        });
    }

    private void addFindFriendsButton() {
        findFriendsButton = (ImageButton) findViewById(R.id.findFriendsButton);
        findFriendsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater factory = LayoutInflater.from(LocateActivity.this);//提示框
                final View view = factory.inflate(R.layout.alertdialog_findfriends, null);//这里必须是final的
                final EditText yourId = (EditText) view.findViewById(R.id.findFriendsYourName);//获得输入框对象
                final EditText FriendId = (EditText) view.findViewById(R.id.findFriendsFriendName);//获得输入框对象
                builder = new AlertDialog.Builder(LocateActivity.this);
                builder.setView(null);
                builder.setView(view);
                builder.setTitle("Find");
                builder.setMessage("Get your friends' position from the server.");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String yourName = yourId.getText().toString();
                        String friendName = FriendId.getText().toString();
                        if (!yourName.equals("") && (!friendName.equals(""))) {
                            String[] str = new String[2];
                            str[0] = yourName;
                            str[1] = friendName;
                            NetworkUtil.getInstance().getFriendPosition(yourName, friendName);
                            findFriendHandler.obtainMessage(FINDFRIENDHANDLE, str).sendToTarget();
                        }
                    }
                });
                builder.setNegativeButton("No", null);
                builder.create().show();
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(DataEvent event) {
        switch (event.getEventCode()) {
            case EventCode.GET_FRIEND_POSITION:
                if (event.isSuccess()) {
                    String result = (String) event.getResult();
                    if("friend is not existed".equals(result)){
                        strPosition = result.split(" ");
                        Log.e("obj",strPosition[0]);
                        if(!("friend".equals(strPosition[0]))){
                            view.setFlagStr("friend");
                            updateFriend(strPosition);
                            friendExistFlag = true;
                        }else{
                            friendExistFlag = false;
                        }
                        Toast.makeText(LocateActivity.this,"无此好友位置数据",Toast.LENGTH_SHORT).show();
                    }else{
                        strPosition = result.split("&");
                        Log.e("obj",strPosition[0]);
                        if(!("friend".equals(strPosition[0]))){
                            view.setFlagStr("friend");
                            updateFriend(strPosition);
                            friendExistFlag = true;
                        }else{
                            friendExistFlag = false;
                        }
                        Toast.makeText(LocateActivity.this,"获取好友位置成功",Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LocateActivity.this,"获取好友位置失败",Toast.LENGTH_SHORT).show();
                }
                break;
            case EventCode.UPLOAD_POSITION:
                if (event.isSuccess()) {
                    Toast.makeText(LocateActivity.this,"上传位置成功",Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(LocateActivity.this,"上传位置失败",Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
    }

    private void listenImage() {
        imageviewlayout = (LinearLayout) findViewById(R.id.imageviewlayout);
        imageviewlayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast ilToast;
                //Toast.makeText(LocateActivity.this,"Goto interiorLayout page",Toast.LENGTH_SHORT).show();
                ilToast = Toast.makeText(getApplicationContext(), "Legend", Toast.LENGTH_SHORT);
                ilToast.setGravity(Gravity.CENTER, 0, 0);
                //LinearLayout toastView = (LinearLayout) toast.getView();
                RelativeLayout layout = (RelativeLayout) getLayoutInflater().inflate(R.layout.activity_interior_help, null);
                ilToast.setView(layout);
                //ImageView imageCodeProject = new ImageView(getApplicationContext());
                //imageCodeProject.setImageResource(R.drawable.help_locate);
                //toastView.addView(imageCodeProject, 0);
                ilToast.show();
            }
        });
    }

    //app恢复运行
    @Override
    protected void onResume() {
        super.onResume();
        lActivity = this;
        view = new Draw(LocateActivity.this);
        lv = (ListView) findViewById(R.id.locateListView);
        mLeDeviceListAdapter = new LeDeviceListAdapter(this);
        lv.setAdapter(mLeDeviceListAdapter);
        scanLeDevice(true);
    }

    //app暂停
    @Override
    protected void onPause() {
        lActivity = null;
        //停止扫描ble设备
        scanLeDevice(false);
        //页面ListView内容清空
        mLeDeviceListAdapter.clear();
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        lActivity = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
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
                            iBeaconClass.iBeacon ibeacon = fromScanData(device, rssi, scanRecord);
                            if (ibeacon != null) {
                                LeDeviceListAdapter leDeviceListAdapter = new LeDeviceListAdapter(LocateActivity.this);
                                LeDeviceListAdapter.Ble_BluetoothDevice bleBluetoothDevice = leDeviceListAdapter.new Ble_BluetoothDevice(
                                        device, rssi, ibeacon.txPower, iBeaconClass.calculateAccuracy(rssi), ibeacon.major, ibeacon.minor, ibeacon.proximityUuid
                                );
                                // return 的值
                                int chufa = mLeDeviceListAdapter.addDevice(bleBluetoothDevice);
                                mLeDeviceListAdapter.notifyDataSetChanged();
                                //根据chufa的值发送信息
                                if (flag && chufa > (-75)) {
                                    Intent intent = new Intent(LocateActivity.this, NotificationService.class);
                                    startService(intent);
                                    Log.e("flag",chufa+"");
                                    flag = false;
                                }
                                if (chufa < -75) {
                                    flag = true;
                                }
                            }
                            deviceDistance = mLeDeviceListAdapter.getDevice();
                            if (deviceDistance.size() > 1) {
                                Message msg = new Message();
                                //确定两个距离各自对应的设备,arg1对应E-Beacon_0EC0B6,arg2对应E-Beacon_E4C5E0
                                if ("E-Beacon_0EC0B6".equals(deviceDistance.get(0).bluetoothDevice.getName())) {
                                    msg.arg1 = (int) (deviceDistance.get(0).distance * 1000);
                                    msg.arg2 = (int) (deviceDistance.get(1).distance * 1000);
                                } else {
                                    msg.arg1 = (int) (deviceDistance.get(1).distance * 1000);
                                    msg.arg2 = (int) (deviceDistance.get(0).distance * 1000);
                                }
                                msg.what = UPDATEPIC;
                                oHandler.sendMessage(msg);
                            }
                        }
                    });
                }
            };
}
