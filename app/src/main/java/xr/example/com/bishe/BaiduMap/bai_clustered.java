package xr.example.com.bishe.BaiduMap;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;

import java.util.List;

import xr.example.com.bishe.Algorism.point;
import xr.example.com.bishe.R;

/**
 * Created by Administrator on 2018/3/31.
 */

public class bai_clustered extends Activity {
    List<point> receivelist1;//接收点类
    point[] pointcore;
    private Marker mMarker;

    private  MyLocationListener1 mLocationListener;
    private  MyLocationConfiguration.LocationMode mCurrentMode;
    private LocationClient mLocationClient;
    MapView mapView = null;
    BaiduMap baiduMap;
    LatLng myCurrentposition;
    int num1=0;

  //  TextView textView;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         setActionBar();//设置带有返回键的标题
        mLocationClient = new LocationClient(getApplicationContext());
        SDKInitializer.initialize(getApplicationContext());//软件开发包初始化；
        setContentView(R.layout.bai_clustered);
        mapView = (MapView)findViewById(R.id.bmapView);
        baiduMap = mapView.getMap();
        baiduMap.setMyLocationEnabled(true);

      //  textView=(TextView)findViewById(R.id.textView);
        receivelist1 = (List<point>) this.getIntent().getSerializableExtra("mapshow");
        num1=receivelist1.size();
        pointcore=new point[num1];
        int i=0;
        for (point p : receivelist1) {
            pointcore[i] = new point(p.x, p.y);//将点类转换为矩阵
            i += 1;
        }
        intloc ();
       addmarker();
    }


     public void addmarker() {
         for(int i=0;i<num1;i++)
         {
            LatLng terminal_point;
            terminal_point = new LatLng(pointcore[i].x,pointcore[i].y);
            BitmapDescriptor bdB = BitmapDescriptorFactory
                    .fromResource(R.drawable.ic_mapshow);
            OverlayOptions ooP = new MarkerOptions().position(terminal_point).icon(bdB);
            mMarker = (Marker) (baiduMap.addOverlay(ooP));
      }
     }
    private void intloc () {
        mLocationClient = new LocationClient(this);//是否可用getApplicationContext取代this；????????
        mLocationListener = new  MyLocationListener1();
        mLocationClient.registerLocationListener(mLocationListener);
        initLocation();
    }
    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        int span=10000;
        option.setScanSpan(span);
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        // option.setLocationNotify(true);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false);//可选，默认false，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认杀死
        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤gps仿真结果，默认需要
        mLocationClient.setLocOption(option);
        mLocationClient.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        mLocationClient.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        mLocationClient.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        mLocationClient.stop();

    }
    public class MyLocationListener1 implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            baiduMap.setMyLocationEnabled(true);
            MyLocationData locData = new MyLocationData.Builder().
                     latitude(bdLocation.getLatitude())
                    .longitude(bdLocation.getLongitude()).build();//生成定位数据
            baiduMap.setMyLocationData(locData);
            // 设置定位图层的配置（定位模式，是否允许方向信息，用户自定义定位图标）
            MyLocationConfiguration config = new MyLocationConfiguration(mCurrentMode, true, null);  //第三个参数是位置图片没有就默认
            baiduMap.setMyLocationConfigeration(config);

            myCurrentposition = new LatLng(bdLocation.getLatitude(), bdLocation.getLongitude());//以我的位置为中心
            baiduMap.animateMapStatus(MapStatusUpdateFactory.newLatLng(myCurrentposition));

        }
    }
    private void setActionBar() {
        android.app.ActionBar actionBar=getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);//显示返回键
        actionBar.setDisplayShowHomeEnabled(false);//取消logo
        actionBar.setTitle("返回");//设置返回字样
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return false;
        }
        return true;
    }

}
