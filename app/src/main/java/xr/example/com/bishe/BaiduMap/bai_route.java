package xr.example.com.bishe.BaiduMap;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.RouteLine;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.search.route.BikingRoutePlanOption;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.IndoorRouteResult;
import com.baidu.mapapi.search.route.MassTransitRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteLine;
import com.baidu.mapapi.search.route.WalkingRoutePlanOption;
import com.baidu.mapapi.search.route.WalkingRouteResult;

import xr.example.com.bishe.Explanation.about;
import xr.example.com.bishe.Explanation.use;
import xr.example.com.bishe.R;

public class bai_route extends Activity implements OnGetGeoCoderResultListener,BaiduMap.OnMapClickListener
,OnGetRoutePlanResultListener{

    private LocationClient mLocationClient;
    private MyLocationListener mLocationListener;
    private  MyLocationConfiguration.LocationMode mCurrentMode;//??????????
    RoutePlanSearch mrouteSearch = null;
    GeoCoder mGeoSearch;
    BaiduMap baiduMap = null;
    MapView mapView = null;
    EditText edt_start_pos, edt_end_pos;
    Button btn_walkroute,btn_bikeroute,next_node, last_node;
    TextView strInfo;
    String city = "合肥市";
    LatLng myCurrentposition;
    private Marker mMarker;

    double cur_lon;
    double cur_lat;//当前城市的经纬度

    int nodeIndex = -1;
    RouteLine route = null;
    OverlayManager routeOverlay = null;
    boolean useDefaultIcon = false;
    private TextView popupText = null;//相比较dialog函数多了通过参数view，with,height来设置显示位置的功能

    LatLng nodeLocation = null;//节点位置
    String nodeTitle = null;

    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActionBar();//设置带有返回键的标题
        mLocationClient = new LocationClient(getApplicationContext());
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.bai_route);

        mapView = (MapView) findViewById(R.id.bmapView);
        baiduMap = mapView.getMap();
        baiduMap.setMyLocationEnabled(true);
        baiduMap.setOnMapClickListener(this);

        edt_start_pos = (EditText) findViewById(R.id.edt_start_pos);
        edt_end_pos = (EditText) findViewById(R.id.edt_end_pos);
        btn_walkroute = (Button) findViewById(R.id.btn_walkroute);
        btn_bikeroute=(Button)findViewById(R.id.btn_bikeroute);
        strInfo=(TextView)findViewById(R.id.strInfo);
        next_node = (Button) findViewById(R.id.next_node);
        last_node = (Button) findViewById(R.id.last_node);

         Intent intent = getIntent();
         String aa = intent.getStringExtra("jingd");
         cur_lon= Double.parseDouble(aa) ;//??????????
         String bb = intent.getStringExtra("weid");
         cur_lat = Double.parseDouble(bb) ;//?????????????????????

        LatLng terminal_point = new LatLng(cur_lat,cur_lon);
        BitmapDescriptor bdB = BitmapDescriptorFactory
                .fromResource(R.drawable.ic_end_pos);
        OverlayOptions ooP = new MarkerOptions().position(terminal_point).icon(bdB);
        mMarker = (Marker) (baiduMap.addOverlay(ooP));
        mGeoSearch=GeoCoder.newInstance();
        mGeoSearch.setOnGetGeoCodeResultListener(this);
        mrouteSearch = RoutePlanSearch.newInstance();
        mrouteSearch.setOnGetRoutePlanResultListener(this);
        intloc();
}
    private void setActionBar() {
        android.app.ActionBar actionBar=getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);//显示返回键
        actionBar.setDisplayShowHomeEnabled(false);//取消logo
        actionBar.setTitle("返回");//设置返回字样
    }

    public  void searchButtonProcess(View v) {

        switch (v.getId()) {
            case R.id.btn_walkroute:
                route=null;
                baiduMap.clear();
                if(edt_start_pos.getText().toString().trim().length()==0&&edt_end_pos.getText().toString().trim().length()==0) {//默认从定位处开始,以传递过来的车辆经纬度为终点；为默认的操作
                    PlanNode startNode = PlanNode.withLocation(myCurrentposition);
                    PlanNode endNode= PlanNode.withLocation(new LatLng(cur_lat,cur_lon));// 可以传输单片机数据；
                    mrouteSearch.walkingSearch((new WalkingRoutePlanOption()).from(startNode).to(endNode));//步行线路规划
                }
                else if(edt_end_pos.getText().toString().trim().length()!=0){
                    PlanNode startNode = PlanNode.withLocation(myCurrentposition);// 当前位置为起点
                    PlanNode endNode = PlanNode.withLocation(new LatLng(cur_lat, cur_lon));//传输单片机数据；

                 //   mGeoSearch.geocode(new GeoCodeOption().city(city).address(edt_end_pos.getText().toString()));
                    mrouteSearch.walkingSearch((new WalkingRoutePlanOption()).from(startNode).to(endNode));//步行线路规划
                }
                else return;
                break;
            case R.id.btn_bikeroute:
                route=null;//
                baiduMap.clear();
                if(edt_start_pos.getText().toString().trim().length()==0&&edt_end_pos.getText().toString().trim().length()==0) {//默认从定位处开始,以传递过来的车辆经纬度为终点；为默认的操作
                    PlanNode startNode = PlanNode.withLocation(myCurrentposition);
                    PlanNode endNode= PlanNode.withLocation(new LatLng(cur_lat,cur_lon));// 可以传输单片机数据；
                    mrouteSearch.bikingSearch((new BikingRoutePlanOption()).from(startNode).to(endNode));//骑行线路规划
                }
                else if(edt_end_pos.getText().toString().trim().length()!=0){
                    PlanNode startNode = PlanNode.withLocation(myCurrentposition);// 当前位置为起点
                    PlanNode endNode = PlanNode.withLocation(new LatLng(cur_lat, cur_lon));//传输单片机数据；
                  //  mGeoSearch.geocode(new GeoCodeOption().city(city).address(edt_start_pos.getText().toString()));
                    //   mGeoSearch.geocode(new GeoCodeOption().city(city).address(edt_end_pos.getText().toString()));
                    mrouteSearch.bikingSearch((new BikingRoutePlanOption()).from(startNode).to(endNode));//骑行线路规划
                }
                else return;
                break;

            case R.id.next_node:
                if (route == null || route.getAllStep() == null) {
                    return;
                }
                if (nodeIndex == -1 && v.getId() == R.id.last_node) {
                    return;
                }
                if (nodeIndex < route.getAllStep().size() - 1) {
                    nodeIndex++;
                }
                Object step1 = route.getAllStep().get(nodeIndex);
                if (step1 instanceof WalkingRouteLine.WalkingStep) {
                    nodeLocation = ((WalkingRouteLine.WalkingStep) step1).getEntrance().getLocation();
                    nodeTitle = ((WalkingRouteLine.WalkingStep) step1).getInstructions();
                }
                baiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(nodeLocation));
                // show popup
                popupText = new TextView(bai_route.this);
             //   popupText.setBackgroundResource(R.mipmap.ic_lanlonextract);
                popupText.setTextColor(0xFF000000);
                popupText.setText(nodeTitle);
                baiduMap.showInfoWindow(new InfoWindow(popupText, nodeLocation, 0));
                break;
            case R.id.last_node:
                if (route == null || route.getAllStep() == null) {
                    return;
                }
                if (nodeIndex == -1 && v.getId() == R.id.last_node) {
                    return;
                }
                if (nodeIndex > 0) {
                    nodeIndex--;
                }
                Object step2 = route.getAllStep().get(nodeIndex);
                if (step2 instanceof WalkingRouteLine.WalkingStep) {
                    nodeLocation = ((WalkingRouteLine.WalkingStep) step2).getEntrance().getLocation();
                    nodeTitle = ((WalkingRouteLine.WalkingStep) step2).getInstructions();
                }
                if (nodeLocation == null || nodeTitle ==null) {
                    return;
                }
                baiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(nodeLocation));

                popupText = new TextView(bai_route.this);
                popupText.setBackgroundResource(R.drawable.ic_showroute);
                popupText.setTextColor(0xFF000000);
                popupText.setText(nodeTitle);
                baiduMap.showInfoWindow(new InfoWindow(popupText, nodeLocation, 0));
                break;
            default:break;

        }

        }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onGetWalkingRouteResult(WalkingRouteResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(bai_route.this,"抱歉，未找到步行线路",Toast.LENGTH_SHORT).show();
        }
        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            return;
        }
        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
            nodeIndex=-1;
            route=result.getRouteLines().get(0);
            WalkingRouteOverlay overlay =new MyWalkingRouteOverlay(baiduMap);
           baiduMap.setOnMarkerClickListener(overlay);
           routeOverlay=overlay;
           overlay.setData(result.getRouteLines().get(0));
           overlay.addToMap();
           overlay.zoomToSpan();
        }
    }
    @Override
    public void onGetBikingRouteResult(BikingRouteResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(bai_route.this,"抱歉，未找到骑行线路",Toast.LENGTH_SHORT).show();
        }
        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            return;
        }
        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
            nodeIndex=-1;
            route=result.getRouteLines().get(0);
            BikingRouteOverlay overlay =new MyBikingRouteOverlay(baiduMap);
            baiduMap.setOnMarkerClickListener(overlay);
            routeOverlay=overlay;
            overlay.setData(result.getRouteLines().get(0));
            overlay.addToMap();
            overlay.zoomToSpan();
        }
    }
    @Override
    public void onGetGeoCodeResult(GeoCodeResult result) {//由于取消了地址编码功能，该函数暂时未用到
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(bai_route.this, "抱歉，未能找到结果2", Toast.LENGTH_SHORT).show();
            return;
        }
        baiduMap.clear();
    }
    @Override
    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {//  if (TextUtils.isEmpty(reverseGeoCodeResult.getAddress()))
              if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(bai_route.this, "抱歉，未能找到您所单击的地址", Toast.LENGTH_LONG).show();
            return;
        }
        if (null != mMarker) {
            mMarker.remove();
        }
        baiduMap.clear();
        baiduMap.addOverlay(new MarkerOptions().position(result.getLocation())
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_navigation)));
        baiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(result.getLocation()));
        Toast.makeText(bai_route.this, result.getAddress(),Toast.LENGTH_LONG).show();
        String showinfo=String.format("纬度：%.5f 经度：%.5f", result.getLocation().latitude, result.getLocation().longitude);
        strInfo.setText(showinfo);
        edt_end_pos.setText(result.getAddress());//单击的终点位置
        cur_lat= result.getLocation().latitude;//单击的纬度
        cur_lon= result.getLocation().longitude;//单击的经度
        //用第二种表达形式显示addOverlay;
        LatLng from = new LatLng( result.getLocation().latitude,
                result.getLocation().longitude);
        BitmapDescriptor bdB = BitmapDescriptorFactory
                .fromResource(R.drawable.ic_navigation);
        OverlayOptions ooP = new MarkerOptions().position(from).icon(bdB);
        mMarker = (Marker) (baiduMap.addOverlay(ooP));
        //   MapStatus mMapStatus = new MapStatus.Builder().target(from)
        //    .build();

    }
    @Override

    public void onGetTransitRouteResult(TransitRouteResult transitRouteResult) {

    }

    @Override
    public void onGetMassTransitRouteResult(MassTransitRouteResult massTransitRouteResult) {

    }

    @Override
    public void onGetDrivingRouteResult(DrivingRouteResult drivingRouteResult) {

    }

    @Override
    public void onGetIndoorRouteResult(IndoorRouteResult indoorRouteResult) {

    }

    private class MyWalkingRouteOverlay extends WalkingRouteOverlay {
        public MyWalkingRouteOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }
        @Override
        public BitmapDescriptor getStartMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher);
            }
            return null;
        }

        @Override
        public BitmapDescriptor getTerminalMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher);
            }
            return null;
    }
    }

    private class MyBikingRouteOverlay extends BikingRouteOverlay {
        public MyBikingRouteOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }
        @Override
        public BitmapDescriptor getStartMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher);
            }
            return null;
        }

        @Override
        public BitmapDescriptor getTerminalMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher);
            }
            return null;
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

    private void intloc () {

            mLocationClient = new LocationClient(this);//是否可用getApplicationContext取代this；????????
            mLocationListener = new MyLocationListener();
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
        mGeoSearch.destroy();
    }

    public class MyLocationListener implements BDLocationListener{
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            baiduMap.setMyLocationEnabled(true);
            MyLocationData locData = new MyLocationData.Builder().
                    accuracy(bdLocation.getRadius()).direction(100).latitude(bdLocation.getLatitude())
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.instruction_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_item://打开使用说明页面
                Intent intent = new Intent(bai_route.this, use.class);
                startActivity(intent);
                break;
            case R.id.about://打开关于百度软件的页面
                Intent intent2 = new Intent(bai_route.this, about.class);
                startActivity(intent2);
                break;
            case R.id.txv_setting://打开设置页面
                Intent intent3=new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent3);
                break;
            case android.R.id.home:
                this.finish();
                return false;
        }
        return true;
    }

}