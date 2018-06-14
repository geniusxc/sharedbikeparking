package xr.example.com.bishe.Welcome;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import xr.example.com.bishe.Algorism.Kcluster;
import xr.example.com.bishe.Algorism.point;
import xr.example.com.bishe.Explanation.MyAdapter;
import xr.example.com.bishe.R;
import xr.example.com.bishe.Welcome.openlink.open_link;
import xr.example.com.bishe.Welcome.openlink.scan;

public class MainActivity extends Activity implements View.OnClickListener {
    MyAdapter adapter;
    Button btn_compute;
    ListView listView;
    List<Map<String, Object>> list;//存储信息的集合
    List<Map<String,Object>> checked;//被选中的列表中的TextView的字符串
    private boolean isMultiple=false;//默认没有多选
    private Uri SMS_INBOX = Uri.parse("content://sms/");
    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        android.app.ActionBar actionBar=getActionBar();
        actionBar.setCustomView(R.layout.actionbar_title);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        setContentView(R.layout.listview);
        listView = (ListView) findViewById(R.id.lv_main);
        btn_compute = (Button) findViewById(R.id.btn_compute);
        btn_compute.setOnClickListener(this);
        list = new ArrayList<Map<String,Object>>();
        getSmsFromPhone();//得到手机中的经纬度信息
        adapter = new MyAdapter(list,this);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?>parent, View view, int i, long l) {
                if (isMultiple) {//如果处于多选状态
                    CheckBox checkBox = (CheckBox)view.findViewById(R.id.checkbox);
                    TextView textView = (TextView) view.findViewById(R.id.tv_mess);//只显示短信内容的颜色变化
                    if (checkBox.isChecked())
                    {
                        checkBox.setChecked(false);//设置所选项选择框为未选中状态
                        adapter.checkedMap.put(i,false);//存储所选项未选中的状态
                        textView.setTextColor(Color.BLACK);//设置所选项文本的颜色为黑色
                        adapter.colorMap.put(i, Color.WHITE);//存储所选项文本的颜色为白色,既是将其消息的经纬度消失
                        Map<String,Object>map=(Map<String, Object>) parent.getItemAtPosition(i);
                    //    int index = checked.indexOf(textView.getText() + "");
                        checked.remove(map);//将备份checked的map列表去除map消息
                    }
                    else        //与上效果刚好相反
                    {
                        checkBox.setChecked(true);
                        adapter.checkedMap.put(i, true);
                        textView.setTextColor(Color.BLACK);//使消息内容暂时不发生变化
                        adapter.colorMap.put(i, Color.RED);
                        Map<String,Object>map=(Map<String, Object>) parent.getItemAtPosition(i);
                        checked.add(map);
                    }
                }
                else {      //处于打开MessActivity的状态
                    Intent intent = new Intent();
                     intent.setClass(MainActivity.this, MessActivity.class);
                    Map<String, Object> map =
                            (Map<String, Object>) parent.getItemAtPosition(i);
                    intent.putExtra("date",  map.get("date").toString());
                    intent.putExtra("mess",  map.get("mess").toString());
                    startActivity(intent);
                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {//创建菜单方式一
     //   SubMenu subMenu = menu.addSubMenu("操作");
        menu.add(0,0,Menu.NONE,"除2操作");
        menu.add(0, 1, Menu.NONE, "多选");
        menu.add(0, 2, Menu.NONE, "删除");
        menu.add(0, 3, Menu.NONE, "全选");
        menu.add(0, 4, Menu.NONE, "取消全选");
        menu.add(0,5,Menu.NONE,"野车显示");
        SubMenu Su=menu.addSubMenu(0,6,Menu.NONE,"循环分析");
        Su.add(1, 7, Menu.NONE,"7天");
        Su.add(1,8,0,"20天");
        Su.add(1,9,0,"30天");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 0)
        {//进行除2操作
            isMultiple = true;
            checked = new ArrayList<Map<String, Object>>();
            int index = adapter.getCount();
            long today = new Date().getTime();
            for (int i = 0; i < index; i++)
            {
                Map map = list.get(i);
                long date = (long) map.get("date");
                String mess_i = (String) map.get("mess");
                int days = getIntervalDays(date, today);
                if (days <= 8)
                {
                    for (int j = i + 1; j < index; j++)
                    {
                        Map map_j=list.get(j);
                        String mess_j = (String) map_j.get("mess");

                        if (mess_j.equals(mess_i))
                        {
                            adapter.checkedMap.put(i, true);
                            adapter.colorMap.put(i, Color.RED);
                            adapter.visibleMap.put(i, CheckBox.VISIBLE);
                            adapter.notifyDataSetChanged();
                            Map<String, Object> map_ij = list.get(i);
                            checked.add(map_ij);
                        }
                    }
                }
            }
        }

        if(item.getItemId()==1)//菜单处于多选状态
        {
            isMultiple = true;
            checked = new ArrayList<Map<String,Object>>();
            int index = adapter.getCount();
            for(int i=0;i<index;i++)
            {
                adapter.visibleMap.put(i, CheckBox.VISIBLE);
            }
            adapter.notifyDataSetChanged();
        }
        else if(item.getItemId()==2)//菜单处于删除状态
        {
            for(Map<String,Object> text : checked)
            {
                list.remove(text);
            }
            isMultiple = false;
            adapter = new MyAdapter(list,MainActivity.this);
            listView.setAdapter(adapter);
        }
        else if(item.getItemId()==3)//菜单处于全选状态
        {
            isMultiple = true;
            checked = new ArrayList<Map<String,Object>>();
            int index = adapter.getCount();
            for(int i=0;i<index;i++)
            {
                adapter.checkedMap.put(i, true);
                adapter.colorMap.put(i, Color.RED);
                adapter.visibleMap.put(i, CheckBox.VISIBLE);
                View view1 = adapter.getView(i, null, null);
                TextView textView = (TextView) view1.findViewById(R.id.tv_mess);
                textView.setTextColor(Color.BLACK);
                CheckBox checkBox = (CheckBox) view1.findViewById(R.id.checkbox);
                checkBox.setVisibility(CheckBox.VISIBLE);
                adapter.notifyDataSetChanged();
                Map<String,Object> map = list.get(i);
                checked.add(map);
            }
        }
        else if(item.getItemId()==4)//菜单处于取消全选状态
        {
            isMultiple = false;
            checked=null;//??????
            int index = adapter.getCount();
            for(int i=0;i<index;i++)
            {
                adapter.checkedMap.put(i, false);
                adapter.colorMap.put(i, Color.WHITE);
                adapter.visibleMap.put(i, CheckBox.INVISIBLE);
                View view1 = adapter.getView(i, null, null);
               // TextView textView = (TextView) view1.findViewById(R.id.tv_mess);
               // textView.setTextColor(Color.WHITE);
                CheckBox checkBox = (CheckBox) view1.findViewById(R.id.checkbox);
                checkBox.setVisibility(CheckBox.INVISIBLE);
                adapter.notifyDataSetChanged();
            }
        } else if (item.getItemId() == 5) {//野车显示
            isMultiple = true;
            checked = new ArrayList<Map<String,Object>>();
            long today = new Date().getTime();
            int index = adapter.getCount();
            for(int i=0;i<index;i++) {
                Map map=list.get(i);
                long  date =(long)map.get("date");
                String mess_i=(String)map.get("mess");
                int days=getIntervalDays(date,today);
                if (days <= 8) {
                    for(int j=0;j<index;j++) {
                        Map map_j=list.get(j);
                        String mess_j=(String)map_j.get("mess");
                        long date_j=(long)map_j.get("date");
                        int days_j=getIntervalDays(date_j,today);
                        if (mess_i.equals(mess_j)&&days_j>8) {
                            adapter.checkedMap.put(i, true);
                            adapter.colorMap.put(i, Color.RED);
                            adapter.visibleMap.put(i, CheckBox.VISIBLE);
                            adapter.notifyDataSetChanged();
                            Map<String,Object> map_ij = list.get(i);
                            checked.add(map_ij);
                        }
                    }
                }

            }
        } else if (item.getItemId() == 6) {
            }//循环分析
            else if (item.getItemId() == 7) {//菜单删除7天以上的数据
                isMultiple = false;
                checked = new ArrayList<Map<String, Object>>();
                long today = new Date().getTime();
                int index = adapter.getCount();
                for (int i = 0; i < index; i++) {
                    // View view=adapter.getView(i,null,null);
                    // TextView textView=(TextView) view.findViewById(R.id.tv_date);
                    Map map = list.get(i);
                    long date = (long) map.get("date");
                    int interdays = getIntervalDays(date, today);
                    if (interdays > 7) {//以7天为限//？？？？？？？？不足的地方应该是没有考虑距离因素。
                        checked.add(map);
                    }
                }
                for (Map<String, Object> text : checked) {
                    list.remove(text);
                }
                checked = null;
                adapter = new MyAdapter(list, MainActivity.this);
                listView.setAdapter(adapter);

            } else if (item.getItemId() == 8) {//菜单删除20天以上的数据
                isMultiple = false;
                checked = new ArrayList<Map<String, Object>>();
                long today = new Date().getTime();
                int index = adapter.getCount();
                for (int i = 0; i < index; i++) {
                    // View view=adapter.getView(i,null,null);
                    // TextView textView=(TextView) view.findViewById(R.id.tv_date);
                    Map map = list.get(i);
                    long date = (long) map.get("date");
                    int interdays = getIntervalDays(date, today);
                    if (interdays > 20) {//以7天为限//？？？？？？？？不足的地方应该是没有考虑距离因素。
                        checked.add(map);
                    }
                }
                for (Map<String, Object> text : checked) {
                    list.remove(text);
                }
                checked = null;
                adapter = new MyAdapter(list, MainActivity.this);
                listView.setAdapter(adapter);
            } else if (item.getItemId() == 9) {//菜单删除30天以上的数据
                isMultiple = false;
                checked = new ArrayList<Map<String, Object>>();
                long today = new Date().getTime();
                int index = adapter.getCount();
                for (int i = 0; i < index; i++) {
                    // View view=adapter.getView(i,null,null);
                    // TextView textView=(TextView) view.findViewById(R.id.tv_date);
                    Map map = list.get(i);
                    long date = (long) map.get("date");
                    int interdays = getIntervalDays(date, today);
                    if (interdays > 30) {//以7天为限//？？？？？？？？不足的地方应该是没有考虑距离因素。
                        checked.add(map);
                    }
                }
                for (Map<String, Object> text : checked) {
                    list.remove(text);
                }
                checked = null;
                adapter = new MyAdapter(list, MainActivity.this);
                listView.setAdapter(adapter);
            }
       
        return super.onOptionsItemSelected(item);
    }

    public int getIntervalDays(long starttime,long endtime) {//求出两个Long时间之间的间隔天数；
        long  interday=endtime-starttime;
        return (int)(interday/(1000*60*60*24));
    }

    public void getSmsFromPhone() {//收集短信的信息
        ContentResolver cr = getContentResolver();
        String[] projection = new String[]{"_id", "address", "person",
                "body", "date", "type"};
        Cursor cur = cr.query(SMS_INBOX, projection, null, null, "date desc");
        if (null == cur) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("num", "110");
            map.put("mess", "读取短信出错！");
            list.add(map);
            return;
        }
        while (cur.moveToNext()) {
                String number = cur.getString(cur.getColumnIndex("address"));
                String body = cur.getString(cur.getColumnIndex("body"));
                long  startdate=cur.getLong(cur.getColumnIndex("date"));
                Pattern pattern = Pattern.compile("纬度:[0-9]{2,3}+(.[0-9]{8}) 经度:[0-9]{2,3}+(.[0-9]{8})");//正则表达式
                Matcher matcher = pattern.matcher(body);
               if (matcher.find()){
                  String res = matcher.group();
                 Map<String, Object> map = new HashMap<String, Object>();
                 map.put("num", number);//手机号码显示
                 map.put("date",startdate);//手机短信接收的时间显示
                 map.put("mess", res);//手机短信的内容显示
                 list.add(map);
            }
        }
    }



    @Override
    public void onClick(View view) {//单击聚类确定

        int index = adapter.getCount();
        boolean ischoosed=false;
        List<point>sendlist=new ArrayList<point>();
        for(int i=0;i<index;i++)//通过一个for循环将已经选中的项中的有用经纬度信息提取到sendlist的point类中
        {
             if(adapter.checkedMap.get(i)) //为手机收到错误数据时手动取消选择做准备//对错误数据可以通过MessActivity.java进一步处理
           {
                ischoosed=true;
                View view1 = adapter.getView(i, null, null);
                TextView textView = (TextView) view1.findViewById(R.id.tv_mess);
                String lat = textView.getText().toString().substring(3,11);
                String lon = textView.getText().toString().substring(18,27);
                double flo_lat = Double.parseDouble(lat);
                double flo_lon = Double.parseDouble(lon);
                sendlist.add(new point(flo_lat, flo_lon));
             }
        }

        if(ischoosed)
       {
            Bundle bundle = new Bundle();
            bundle.putSerializable("point", (Serializable)sendlist); //利用bundle传输sendlist类
            Intent it = new Intent(MainActivity.this, Kcluster.class);
            it.putExtras(bundle);
            startActivity(it);
        }
       else {
           Toast.makeText(MainActivity.this, "请先选择要处理的数据", Toast.LENGTH_LONG).show();
          }
    }
    public void btn_clickredbill(View v) {//单击红包按钮，可以选择支付宝接口或扫码加入微信群
        switch (v.getId()) {

            case R.id.scan_redbill:
                Intent it2=new Intent();
                it2.setClass(this,scan.class);
                startActivity(it2);
        }
    }
    public void open_link(View v) {
        switch (v.getId()) {
            case R.id.http_send:
                Intent it = new Intent(this, open_link.class);
                startActivity(it);
                break;
        }
    }
}

