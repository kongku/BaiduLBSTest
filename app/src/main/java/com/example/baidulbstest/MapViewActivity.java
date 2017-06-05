package com.example.baidulbstest;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

public class MapViewActivity extends AppCompatActivity {
    private final int UPDATE_TEXT = 1;
    private com.baidu.mapapi.map.MapView mapView;
    private TextView llTextView;
    private BaiduMap baiduMap;
    private boolean isFirstLocate = true;
    public LocationClient mLocationClient;
    private BDLocationListener myListener = new MyLocationListener();
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_TEXT:
                    llTextView.setText(((StringBuffer) msg.obj).toString());
                    break;
                default:
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());//一定要在setContentView()前执行初始化，不然会出错
        setContentView(R.layout.activity_map_view);
        mapView = (com.baidu.mapapi.map.MapView) findViewById(R.id.bmapView);
        llTextView = (TextView) findViewById(R.id.ll_text_view);
        //从ui中的MapView中获取Map实例，从而控制这个MapView的地图信息
        baiduMap = mapView.getMap();

        //开启标识定位所在位置的功能
        //用了定位显示功能，记得退出的时候要关闭掉
        baiduMap.setMyLocationEnabled(true);

        //Context需要是全进程有效的Context,推荐用getApplicationConext获取全进程有效的Context
        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(myListener);

        initLocation();//初始化参数设置

        mLocationClient.start();//注意，这里start()之后，定位就会一直开着，会比较费电好资源，直你调用stop()，这为了演示，没有自动去关闭，等到销毁活动才关闭
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocationClient.stop();
        baiduMap.setMyLocationEnabled(false);
        mapView.onDestroy();
    }

    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        //可选，默认高精度，设置定位模式，高精度(使用网络与GPS相结合)Hight_Accuracy，低功耗(仅适用网络)Battery_Saving，仅设备(仅GPS)Device_Sensors

        option.setCoorType("bd09ll");
        //可选，默认gcj02，设置返回的定位结果坐标系，bd09ll专门用在百度地图上的坐标系

        int span = 3000;
        option.setScanSpan(span);
        //可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的

//        option.setIsNeedAddress(true);
        //可选，设置是否需要地址信息，默认不需要
        //true后，就可以调用BDLocation的getAddrStr()获取地址，还有细分国家省市区街道的信息

        //注意，这里不设为true，获取到的定位不准确，有偏移
        option.setOpenGps(true);
        //可选，默认false,设置是否使用gps

//        option.setLocationNotify(true);
        //可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果

//        option.setIsNeedLocationDescribe(true);
        //可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”

//        option.setIsNeedLocationPoiList(true);
        //可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到

        //// TODO: 2017-06-01 0001 测试true的时候
//        option.setIgnoreKillProcess(false);
        //可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死

//        option.SetIgnoreCacheException(false);
        //可选，默认false，设置是否收集CRASH信息，默认收集

//        option.setEnableSimulateGps(false);
        //可选，默认false，设置是否需要过滤GPS仿真结果，默认需要

        mLocationClient.setLocOption(option);
    }

    private class MyLocationListener implements BDLocationListener {
        private int num = 0;

        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            Log.d("map", "onReceiveLocation: ");
            if (bdLocation.getLocType() == BDLocation.TypeGpsLocation || bdLocation.getLocType() == BDLocation.TypeNetWorkLocation) {
                //一般的app，只需要知道用户所在城市，通常在收到定位信息反馈后，只要定位信息有效，就可以停止定位了
                //mLocationClient.stop();
                //像导航类型的软件，需要长时间获取定位信息,实时显示用户所在的位置

                navigateTo(bdLocation);
            }
            StringBuffer sb = new StringBuffer(256);
            sb.append("线程id：" + Thread.currentThread().getId() + "\n");
            sb.append("第" + ++num + "定位：");
            sb.append("\nlatitude : ");
            sb.append(bdLocation.getLatitude());    //获取纬度信息
            sb.append("\nlontitude : ");
            sb.append(bdLocation.getLongitude());    //获取经度信息
            sb.append("\nlocationdescribe : ");
            sb.append(bdLocation.getLocationDescribe());    //位置语义化信息
            //定位获取，是在service的子线程中执行的，因此需要调用异步执行来更新UI
            //使用runOnUiThread也行，这只是对异步消息处理机制的借口封装
            Message msg = new Message();
            msg.what = UPDATE_TEXT;
            msg.obj = sb;
            handler.sendMessage(msg);
        }

        @Override
        public void onConnectHotSpotMessage(String s, int i) {

        }
    }

    private void navigateTo(BDLocation bdLocation) {
        if (isFirstLocate) {
            LatLng ll = new LatLng(bdLocation.getLatitude(), bdLocation.getLongitude());
            MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(ll);
            baiduMap.animateMapStatus(update);
            update = MapStatusUpdateFactory.zoomTo(19f);
            baiduMap.animateMapStatus(update);
            isFirstLocate = false;
        }
        MyLocationData.Builder builder=new MyLocationData.Builder();
        builder.latitude(bdLocation.getLatitude());
        builder.longitude(bdLocation.getLongitude());
        MyLocationData myLocationData=builder.build();
        baiduMap.setMyLocationData(myLocationData);
    }
}
