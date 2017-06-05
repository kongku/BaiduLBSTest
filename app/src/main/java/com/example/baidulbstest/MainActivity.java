package com.example.baidulbstest;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.Poi;


import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private final int UPDATE_TEXT = 1;
    public LocationClient mLocationClient;
    private BDLocationListener myListener = new MyLocationListener();
    private TextView positionTextView;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_TEXT:
                    positionTextView.setText(((StringBuffer) msg.obj).toString());
                    break;
                default:
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        positionTextView = (TextView) findViewById(R.id.position_text_view);

        List<String> permissionList = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!permissionList.isEmpty()) {
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this, permissions, 1);
        } else {
            //LocationClient类必须在主线程中声明，需要Context类型的参数。
            //Context需要是全进程有效的Context,推荐用getApplicationConext获取全进程有效的Context
            mLocationClient = new LocationClient(getApplicationContext());
            mLocationClient.registerLocationListener(myListener);
            initLocation();
            //requestLocation();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopRequestLocation();
    }


    private void requestLocation() {
        if (mLocationClient != null && !mLocationClient.isStarted()) {
            //定位是运行在service中，但是实际的定位获取，是在不同的子线程中去获取，所以一个app里面，可以同时有多个LocationClient在获取定位数据，
            //注意，每一个LocationClient获取的数据会影响其他的LocationClient，例如其中一个的扫描间隔是1秒，另外一个设置的是3秒，两个LocationClient都处于start状态下，
            //那么设置为3秒的也会变成1秒，因为定位获取数据后，都会触发监听器，从而影响到所有开启的LocationClient，
            //还有数据，一个LocationClient设置获取地址信息，另外一个没有设置，但是一样可以获取到信息
            mLocationClient.start();
        }
    }

    private void stopRequestLocation() {
        if (mLocationClient != null && mLocationClient.isStarted()) {
            mLocationClient.stop();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this, "你必须统一所有权限才能使用本程序", Toast.LENGTH_SHORT).show();
//                            finish();
                            return;
                        }
                    }
                    requestLocation();
                } else {
                    Toast.makeText(this, "发生未知错误", Toast.LENGTH_SHORT).show();
//                    finish();
                }
                break;
            default:
        }
    }

    public void startLocationBtn(View view) {
            requestLocation();
    }

    public void stopLocationBtn(View view) {
        stopRequestLocation();
    }

    public void gotoMapView(View view) {
        Intent intent =new Intent(MainActivity.this,MapViewActivity.class);
        startActivity(intent);
    }

    class MyLocationListener implements BDLocationListener {
        private int num = 0;

        @Override
        public void onReceiveLocation(BDLocation location) {

            //获取定位结果
            StringBuffer sb = new StringBuffer(256);
            sb.append("线程id："+Thread.currentThread().getId()+"\n");
            sb.append("第" + ++num + "定位：\n");

            sb.append("time : ");
            sb.append(location.getTime());    //获取定位时间

            sb.append("\nerror code : ");
            sb.append(location.getLocType());    //获取类型类型

            sb.append("\nlatitude : ");
            sb.append(location.getLatitude());    //获取纬度信息

            sb.append("\nlontitude : ");
            sb.append(location.getLongitude());    //获取经度信息

            sb.append("\nradius : ");
            sb.append(location.getRadius());    //获取定位精准度

            if (location.getLocType() == BDLocation.TypeGpsLocation) {

                // GPS定位结果
                sb.append("\nspeed : ");
                sb.append(location.getSpeed());    // 单位：公里每小时

                sb.append("\nsatellite : ");
                sb.append(location.getSatelliteNumber());    //获取卫星数

                sb.append("\nheight : ");
                sb.append(location.getAltitude());    //获取海拔高度信息，单位米

                sb.append("\ndirection : ");
                sb.append(location.getDirection());    //获取方向信息，单位度

                sb.append("\naddr : ");
                sb.append(location.getAddrStr());    //获取地址信息

                sb.append("\ndescribe : ");
                sb.append("gps定位成功");

            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {

                // 网络定位结果
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());    //获取地址信息
                //location.getAddrStr()可以分解为下面的细分信息：
//                location.getCountry();//国家
//                location.getProvince();//省
//                location.getCity();//市
//                location.getDistrict();//区
//                location.getStreet();//街道

                sb.append("\noperationers : ");
                sb.append(location.getOperators());    //获取运营商信息

                sb.append("\ndescribe : ");
                sb.append("网络定位成功");

            } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {

                // 离线定位结果
                sb.append("\ndescribe : ");
                sb.append("离线定位成功，离线定位结果也是有效的");

            } else if (location.getLocType() == BDLocation.TypeServerError) {

                sb.append("\ndescribe : ");
                sb.append("服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");

            } else if (location.getLocType() == BDLocation.TypeNetWorkException) {

                sb.append("\ndescribe : ");
                sb.append("网络不通导致定位失败，请检查网络是否通畅");

            } else if (location.getLocType() == BDLocation.TypeCriteriaException) {

                sb.append("\ndescribe : ");
                sb.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");

            }

            sb.append("\nlocationdescribe : ");
            sb.append(location.getLocationDescribe());    //位置语义化信息

            List<Poi> list = location.getPoiList();    // POI数据，注意，在网络不通的时候，返回null
            if (list != null) {
                sb.append("\npoilist size = : ");
                sb.append(list.size());
                for (Poi p : list) {
                    sb.append("\npoi= : ");
                    sb.append(p.getId() + " " + p.getName() + " " + p.getRank());
                }
            }

            Message msg = new Message();
            msg.what = UPDATE_TEXT;
            msg.obj = sb;
            handler.sendMessage(msg);
        }

        @Override
        public void onConnectHotSpotMessage(String s, int i) {

        }
    }

    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        //可选，默认高精度，设置定位模式，高精度(使用网络与GPS相结合)Hight_Accuracy，低功耗(仅适用网络)Battery_Saving，仅设备(仅GPS)Device_Sensors

        option.setCoorType("bd09ll");
        //可选，默认gcj02，设置返回的定位结果坐标系

        int span = 1000;
        option.setScanSpan(span);
        //可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的

//        option.setIsNeedAddress(true);
        //可选，设置是否需要地址信息，默认不需要
        //true后，就可以调用BDLocation的getAddrStr()获取地址，还有细分国家省市区街道的信息

        option.setOpenGps(true);
        //可选，默认false,设置是否使用gps

        option.setLocationNotify(true);
        //可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果

        option.setIsNeedLocationDescribe(true);
        //可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”

        option.setIsNeedLocationPoiList(true);
        //可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到

        option.setIgnoreKillProcess(false);
        //可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死

//        option.SetIgnoreCacheException(false);
        //可选，默认false，设置是否收集CRASH信息，默认收集

//        option.setEnableSimulateGps(false);
        //可选，默认false，设置是否需要过滤GPS仿真结果，默认需要

        mLocationClient.setLocOption(option);
    }
}
