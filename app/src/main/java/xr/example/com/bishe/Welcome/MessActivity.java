package xr.example.com.bishe.Welcome;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.util.Date;

import xr.example.com.bishe.BaiduMap.bai_dist;
import xr.example.com.bishe.BaiduMap.bai_route;
import xr.example.com.bishe.R;




public class MessActivity extends Activity implements View.OnClickListener  {
      EditText et_showlat,et_showlon,timepassed,numgeted;
      EditText xml_showdist;
      Button btn_transform;
      String showlat,showlon;
      int days;
      public static int jifen=0;
    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActionBar();//设置带有返回键的标题
        setContentView(R.layout.activity_mess);

        et_showlat = (EditText) findViewById(R.id.et_showlat);
        et_showlon = (EditText) findViewById(R.id.et_showlon);
        timepassed=(EditText)findViewById(R.id.timepassed);
        xml_showdist=(EditText)findViewById(R.id.xml_showdist);
        numgeted=(EditText)findViewById(R.id.numgeted);

        String date= getIntent().getStringExtra("date");//短信时间信息
        long date1=Long.parseLong(date);
        long enddate = new Date().getTime();
        days=getIntervalDays(date1,enddate);
        StringBuilder sb=new StringBuilder();
        sb.append(days+"天");
        timepassed.setText(sb);

        numgeted();//用户可获得的积分

        String mess = getIntent().getStringExtra("mess");//经纬度信息
        et_showlat.setText(mess.substring(3, 11));//提取出纬度信息
        et_showlon.setText(mess.substring(18, 27));//
        showlon=  et_showlon.getText().toString();//保证可以自定义经度
        showlat=  et_showlat.getText().toString();//保证可以自定义纬度
       btn_transform = (Button) findViewById(R.id.btn_transform);
       btn_transform.setOnClickListener(this);
    }

    private void setActionBar() {
        android.app.ActionBar actionBar=getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);//显示返回键
        actionBar.setDisplayShowHomeEnabled(false);//取消logo
        actionBar.setTitle("返回");//设置返回字样
     }

    public int getIntervalDays(long starttime,long endtime) {//求出两个Long时间之间的间隔天数；
        long  interday=endtime-starttime;
        return (int)(interday/(1000*60*60*24));
    }

    public void numgeted() {
        StringBuilder sb1=new StringBuilder();
        if (days > 10 && days <= 20) {
            sb1.append("积分+"+20);

            numgeted.setText(sb1);
        }
        else if (days > 20 && days <= 25) {
            sb1.append("积分+"+35);
            numgeted.setText(sb1);

        }
        else if (days > 25 && days <= 30) {
            sb1.append("积分+"+45);
            numgeted.setText(sb1);

        }
        else if (days > 30 && days <= 35) {
            sb1.append("积分+"+50);
            numgeted.setText(sb1);

        } else if (days > 35) {
            days += 15;
            sb1.append("积分+" + days);
            numgeted.setText(sb1);

        } else {
            sb1.append("此车不能为你带来积分");
            numgeted.setText(sb1);
        }

    }

    public void onClick(View v) {

                    if (showlat.length() == 0 || showlon.length() == 0)//如果输入框为空则跳出提醒对话框
                    {
                        AlertDialog.Builder dialog = new AlertDialog.Builder(MessActivity.this);
                        dialog.setTitle("错误");
                        dialog.setIcon(getResources().getDrawable(R.drawable.ic_warning));
                        dialog.setMessage("请输入数据：经度,纬度");
                        dialog.setCancelable(false);//按返回键不能退出；默认为true
                        dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Field field = null;//控制点击按钮不消失问题
                                try {
                                    field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                                    field.setAccessible(true);
                                    field.set(dialog, false);
                                    dialog.dismiss();//让dialog从屏幕上消失
                                } catch (NoSuchFieldException e) {
                                    e.printStackTrace();
                                } catch (IllegalAccessException e) {
                                    e.printStackTrace();
                                }
                                //获取按钮对象
                                Button PositiveButton = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                                //对按钮进行操作
                                PositiveButton.setVisibility(View.GONE);
                            }
                            });
                        dialog.setNegativeButton("取 消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                onDestroy();//finish:用于结束一个Activity的生命周期，onDestory()方法作用则是在一个Activity对象被销毁之前，释放所占用的资源，
                                //finish会调用到onDestory方法
                            }
                        });
                        dialog.show();
                    }
                    else {
                        sendRequest();
                    }
                   }//单击将 经纬度转换为地址

    public void sendRequest() {//开启百度地图页面
                 new Thread(new Runnable() {
              @Override
              public void run() {
                  try {
                      Intent intent = new Intent(MessActivity.this, bai_route.class);
                      intent.putExtra("jingd",showlon);
                      intent.putExtra("weid", showlat);
                      startActivity(intent);
                  }
                  catch (Exception e)
                  {
                      e.printStackTrace();
                  }
              }
          }).start();
      }

    public void manual_measure(View view) {
        Intent it=new Intent(MessActivity.this,bai_dist.class);
        it.putExtra("jingd",showlon);
        it.putExtra("weid", showlat);
        startActivityForResult(it,2);
    }
   protected  void onActivityResult(int requestCode,int resultCode,Intent it) {
        double str_showdist=it.getDoubleExtra("xml_showdist",bai_dist.show_dist_value);

     if (resultCode == -1) {
           xml_showdist.setText((int)str_showdist+"米");
      }

       if(str_showdist>20) {
           Toast.makeText(this, "此车为野车，积分有效",Toast.LENGTH_LONG).show();
           String str_nownum= numgeted.getText().toString().substring(3,5);
           double do_nownum=Double.parseDouble(str_nownum);
           jifen+=do_nownum;
       }
       else
       {
           Toast.makeText(this, "此车不为野车，积分无效", Toast.LENGTH_LONG).show();
       }

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




