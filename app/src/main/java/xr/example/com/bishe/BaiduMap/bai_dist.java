package xr.example.com.bishe.BaiduMap;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;

import xr.example.com.bishe.R;

/**
 * Created by Administrator on 2018/4/24.
 */

public class bai_dist extends Activity implements OnGetGeoCoderResultListener,BaiduMap.OnMapClickListener
        {

    private Marker mMarker;
    GeoCoder mGeoSearch;
    private  MyLocationListener mLocationListener;
    private  MyLocationConfiguration.LocationMode mCurrentMode;
    private LocationClient mLocationClient;
    MapView mapView = null;
    BaiduMap baiduMap;
    LatLng myCurrentposition;

    double lat,lon,lat1,lon1;
  public static double show_dist_value=0;
    TextView show_dist;
            @SuppressLint("WrongConstant")
            @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mLocationClient = new LocationClient(getApplicationContext());
        SDKInitializer.initialize(getApplicationContext());//软件开发包初始化；
        setContentView(R.layout.bai_clustered);
        mapView = (MapView)findViewById(R.id.bmapView);
        baiduMap = mapView.getMap();
        baiduMap.setMyLocationEnabled(true);
        baiduMap.setOnMapClickListener(this);

         String aa=getIntent().getStringExtra("jingd");
         lon = Double.parseDouble(aa);
         String bb=getIntent().getStringExtra("weid");
         lat=Double.parseDouble(bb);

         LatLng from = new LatLng(lat,lon);
         BitmapDescriptor bdB = BitmapDescriptorFactory
                        .fromResource(R.drawable.ic_navigation);
         OverlayOptions ooP = new MarkerOptions().position(from).icon(bdB);
         mMarker = (Marker) (baiduMap.addOverlay(ooP));

         mGeoSearch=GeoCoder.newInstance();
         mGeoSearch.setOnGetGeoCodeResultListener(this);
         show_dist=(TextView)findViewById(R.id.show_dist);

         intloc ();
}

    private void intloc () {
        mLocationClient = new LocationClient(this);//是否可用getApplicationContext取代this；????????
        mLocationListener = new bai_dist.MyLocationListener();
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
        @Override
        public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(bai_dist.this, "抱歉，未能找到您所单击的地址", Toast.LENGTH_LONG).show();
            return;
        }
        if (null != mMarker)
         {
            mMarker.remove();
         }
         baiduMap.clear();
         Toast.makeText(bai_dist.this, result.getAddress(), Toast.LENGTH_LONG).show();
         lat1= result.getLocation().latitude;//单击的纬度
         lon1= result.getLocation().longitude;//单击的经度

        LatLng from = new LatLng(lat1,lon1);
        BitmapDescriptor bdB = BitmapDescriptorFactory
                .fromResource(R.drawable.ic_navigation);
        OverlayOptions ooP = new MarkerOptions().position(from).icon(bdB);
        mMarker = (Marker) (baiduMap.addOverlay(ooP));
    }


    public void measure_dist(View view)
    {
        switch(view.getId()) {

            case R.id.measure_dist:
                show_dist_value = Distance(lat1, lon1, lat, lon);
                show_dist.setText(show_dist_value + "米");
                break;

            case R.id.return_dist:
               Intent it2=new Intent();
               it2.putExtra("xml_showdist",show_dist_value);
               bai_dist.this.setResult(RESULT_OK,it2);
               bai_dist.this.finish();
               break;
        }
    }

    public Double Distance(double lat1, double lon1,double lat2, double lon2) {
        Double R=6370996.81;  //地球的半径

        Double x = (lon2 - lon1)*Math.PI*R*Math.cos(((lat1+lat2)/2)*Math.PI/180)/180;
        Double y = (lat2 - lat1)*Math.PI*R/180;
        Double distance = Math.hypot(x, y);   //得到两点之间的直线距离(本质勾股定理)
        return   distance;
    }

    @Override
    public void onGetGeoCodeResult(GeoCodeResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(bai_dist.this, "抱歉，未能找到结果2", Toast.LENGTH_SHORT).show();
            return;
        }
        baiduMap.clear();
    }
            public class MyLocationListener implements  BDLocationListener {
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

    @Override
    public void onMapClick(LatLng point) {//单击地图方法的实现
        mGeoSearch.reverseGeoCode(new ReverseGeoCodeOption().location(point));
    }

    @Override
    public boolean onMapPoiClick(MapPoi mapPoi) {
        return false;
    }

}