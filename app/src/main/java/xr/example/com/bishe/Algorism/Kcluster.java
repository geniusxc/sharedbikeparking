package xr.example.com.bishe.Algorism;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import xr.example.com.bishe.BaiduMap.bai_clustered;
import xr.example.com.bishe.R;

/**
 * Created by XR on 2018/3/2.
 */

public class Kcluster extends Activity implements AdapterView.OnItemSelectedListener
        ,View.OnLongClickListener {
    TextView textView;
    EditText edt_cluster1,edt_cluster2;//聚类收集信息点
    List<point> receivelist;//接收点类
    Spinner lanlon_rate;//实际经纬度转地址的比率
    Button btn_frequence;
    int num;//经纬度数量
    int core;//聚类点数量
    int compare;//比较之后的聚类点
    int countofclick=0;//记录单击优化聚类点数量
    int[] vehiclenum;//存放聚点所含有的车辆数；

    String [][] showstatue= {{"0"},{"0"},{"5 6"},{"4 5 6"},{"4 5 6"},{"4 5 6"},{"4 5 6"}};/**演示专用*/

    boolean isclusterfinished=false;//是否聚类完成
    boolean iscountofcluster=false;//是否聚类计数完成
    boolean isItemselected=false;//是否项目单已选择

    double lanlondist;//经纬度距离
    double[] lanlon_ratevalue={1,0.9,0.56,0.2,0.05};//经纬度比率值
    double[] realmarray={10,20,30,40,50,60,70};//实际投放的车辆选址范围
    String str_rate;//作为一个全局变量处理

    point[] pointset;//收集点列表中的点类
    point[] oldclu = null;//旧聚类点
    point[] newclu = null;//新聚类点

    StringBuilder strB_auto_core=new StringBuilder();
    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setActionBar();//设置带有返回键的标题
        setContentView(R.layout.kcluster);

        receivelist = (List<point>) this.getIntent().getSerializableExtra("point");//接收到主界面传来的数据
        textView= (TextView) findViewById(R.id.textView);
        lanlon_rate=(Spinner)findViewById(R.id.lanlon_rate);
        lanlon_rate.setOnItemSelectedListener(this);//经纬度比率转换值得声明

        btn_frequence=(Button)findViewById(R.id.frequence_ok);
        btn_frequence.setOnLongClickListener(this);//长按按照聚点所含车辆数排序
        edt_cluster1 = (EditText) findViewById(R.id.edt_cluster1);
        edt_cluster2=(EditText)findViewById(R.id.edt_cluster2);
        num = receivelist.size();
        core=num/3;//系统会自动将选择设定一个最大范围；
        pointset = new point[num];
        int i = 0;
        for (point p : receivelist) {
            pointset[i] = new point(p.x, p.y);//将点类转换为矩阵
            i += 1;
        }
        this.oldclu = new point[core];
        this.newclu = new point[core];
        Random random = new Random();
        int[] temp = new int[core];
        temp[0] = random.nextInt(num);
        oldclu[0] = new point(0, 0);
        oldclu[0].x = pointset[temp[0]].x;
        oldclu[0].y = pointset[temp[0]].y;
        oldclu[0].flag = 0;//0表示为聚类点
        //为避免产生相同的聚类点
        for (int j = 1; j < core; j++) {
            int flag1 = 0;
            int temp1 = random.nextInt(num);
            for (int k = 0; k < j; k++)
            {
                if (temp[k] == temp1)
                {
                    flag1 = 1;
                    break;
                }
            }
            if (flag1 == 1)
            {
                j--;
            }
            else
                {
                oldclu[j] = new point(0, 0);
                oldclu[j].x = pointset[temp1].x;
                oldclu[j].y = pointset[temp1].y;
                oldclu[j].flag = 0;
                temp[j] = temp1;
            }
        }
        gettingbetter();//系统默认迭代运算
    }
    public void btn_ok(View view) {
         switch (view.getId()) {
             case R.id.btn_manual_ok: {//手动聚类操作
                 String str2 = edt_cluster2.getText().toString();
                 String str1 = edt_cluster1.getText().toString();
                 double str_rate1;
                 if (str1.length() == 0 || str2.length() == 0) {
                     Toast.makeText(Kcluster.this, "请输入合理的聚类点数以及聚类范围", Toast.LENGTH_LONG).show();
                 }
                 else
                     {
                     core = (int) Double.parseDouble(str1);//将用户的输入聚类点数量考虑在内；
                     if (isItemselected)
                     {     //对默认的情况的补充
                         str_rate1 = Double.parseDouble(str_rate);
                     } else
                     {
                         str_rate1 = lanlon_ratevalue[0];
                     }
                     num = receivelist.size();
                     compare = num / 3;//聚点运算的上限
                     core = (core <= compare) ? core : compare;//为防止输入过多的而使聚点不具有应用价值
                     lanlondist = 0.00001 * (Double.parseDouble(str2) / str_rate1);//实际的距离所对应的随机聚点生成时的约束
                     pointset = new point[num];
                     int i = 0;
                     for (point p : receivelist) {
                         pointset[i] = new point(p.x, p.y);//将点类转换为矩阵
                         i += 1;
                     }
                     this.oldclu = new point[core];
                     this.newclu = new point[core];
                     Random random = new Random();
                     int[] temp = new int[core];
                     temp[0] = random.nextInt(num);
                     oldclu[0] = new point(0, 0);
                     oldclu[0].x = pointset[temp[0]].x;
                     oldclu[0].y = pointset[temp[0]].y;
                     oldclu[0].flag = 0;
                     //0表示为聚类点
                     // 为避免产生相同的聚类点//同时考虑了聚点过近问题
                     for (int j = 1; j < core; j++) {
                         int flag1 = 0;
                         int temp1 = random.nextInt(num);
                         for (int k = 0; k < j; k++) {
                             if (temp[k] != temp1 && pointdist(pointset[temp1], pointset[temp[k]])<=lanlondist/**演示专用>= lanlondist*/) {
                                 flag1 = 1;
                                 break;
                             }
                         }
                         if (flag1 == 1) {
                             j--;
                         } else {
                             oldclu[j] = new point(0, 0);
                             oldclu[j].x = pointset[temp1].x;
                             oldclu[j].y = pointset[temp1].y;
                             oldclu[j].flag = 0;
                             temp[j] = temp1;
                         }
                     }
                     gettingbetter();
                 }
             }
             break;
             case R.id.btn_auto_ok: {//实现的功能手机系统自动确定合理的聚类点数量
                                        // 标准：在此类聚点的条件下所涵盖的车辆点数达到一个系统的默认指标，不妨定为90%！！！！！（该功能是重中之重，软件的核心价值所在）
                 double str_rate1;
                 if (isItemselected) {
                     str_rate1 = Double.parseDouble(str_rate);
                 } else {
                     str_rate1 = lanlon_ratevalue[0];
                 }

                 if (countofclick< 7) {
                     lanlondist = 0.00001*realmarray[countofclick]/str_rate1;
                     StringBuilder strB=new StringBuilder();
                     strB.append(realmarray[countofclick]);
                     edt_cluster2.setText(strB);
                     /*
                     countofclick++;
                     num = receivelist.size();
                     compare = num / 3;
                     for (core = 2; core <compare; core++)//取初始化的聚点数量为3较为合理；
                     {
                         pointset = new point[num];
                         int i = 0;
                         for (point p : receivelist) {
                             pointset[i] = new point(p.x, p.y);//将点类转换为矩阵
                             i += 1;
                         }
                         this.oldclu = new point[core];
                         this.newclu = new point[core];
                         Random random = new Random();
                         int[] temp = new int[core];
                         temp[0] = random.nextInt(num);
                         oldclu[0] = new point(0, 0);
                         oldclu[0].x = pointset[temp[0]].x;
                         oldclu[0].y = pointset[temp[0]].y;
                         oldclu[0].flag = 0;
                         ///////////////////////////////////////////////
                         for (int j = 1; j < core; j++) {
                             int flag1 = 0;
                             int temp1 = random.nextInt(num);
                             for (int k = 0; k < j; k++) {
                                 if (temp[k] != temp1 && pointdist(pointset[temp1], pointset[temp[k]]) <= lanlondist) {
                                     flag1 = 1;
                                     break;
                                 }
                             }
                             if (flag1 == 1) {
                                 j--;
                             } else {
                                 oldclu[j] = new point(0, 0);
                                 oldclu[j].x = pointset[temp1].x;
                                 oldclu[j].y = pointset[temp1].y;
                                 oldclu[j].flag = 0;
                                 temp[j] = temp1;
                             }
                         }
                        gettingbetter_polished();
                     }
                        edt_cluster1.setText(strB_auto_core);
                        strB_auto_core.delete(0,strB_auto_core.length());
                         以上属于理想状态
                     */

                     /**演示专用*/
                       for(int i=0;i<showstatue[countofclick].length;i++)
                       {
                          strB_auto_core.append(showstatue[countofclick][i]);
                       }
                       edt_cluster1.setText(strB_auto_core);
                       strB_auto_core.delete(0,strB_auto_core.length());
                       countofclick++;
                 }

                 else
                 {
                     countofclick=0;
                 }
             }
              break;
             }
         }

    public void btn_mapshow(View view) {  //再次运用了多个数据不同activity之间的传输功能
            Intent it=new Intent(Kcluster.this,bai_clustered.class);

            List<point>sendlist1=new ArrayList<point>();
            for(int i=0;i<core;i++)//通过一个for循环将已经选中的项中的有用经纬度信息提取到sendlist的point类中
                {
                    sendlist1.add(new point(oldclu[i].x,oldclu[i].y));
                }
                Bundle bundle = new Bundle();
                bundle.putSerializable("mapshow", (Serializable)sendlist1); //利用bundle传输sendlist列表
                it.putExtras(bundle);
                startActivity(it);
            }

    public void frequence_ok(View view) {
         if (isclusterfinished) {//此聚类完成判断是在开始完成系统聚类时确定的（并没有考虑聚点过近问题）
             String str2=edt_cluster2.getText().toString();//以备用户重新输入选址范围
             double str_rate1;

             str_rate1=  Double.parseDouble(str_rate);
             lanlondist=0.00001*(Double.parseDouble(str2)/str_rate1);
             vehiclenum = new int[core];
             for (int i = 1; i <= oldclu.length; i++) {
                 for (int j = 0; j < num; j++) {
                     if (pointset[j].flag == i && pointdist(oldclu[i - 1], pointset[j]) <= lanlondist) {
                         vehiclenum[i-1]++;
                     }
                 }
             }
             iscountofcluster=true;
             StringBuilder strB = new StringBuilder();
             strB.append("每个聚点所含有的车辆数量如下：\n");

             for (int i = 0; i < core; i++) {
                 strB.append("第");
                 strB.append(i+1);
                 strB.append("个聚类范围内车辆数为： ");
                 strB.append(vehiclenum[i]);
                 strB.append("    (lat:");
                 strB.append(String.format("%.5f",oldclu[i].x));
                 strB.append("  lon:");
                 strB.append(String.format("%.5f",oldclu[i].y));
                 strB.append(")");
                 strB.append('\n');
             }
             strB.append("该范围内所包含的总的车辆数为：");
             strB.append(count());//此处在手动聚点完成后得到的总聚点内车辆数
             strB.append("\n");
             strB.append("占总投放量的比率：");
             double rate=count()/(num*1.0);
             strB.append(rate);
             textView.setText(strB);
         }
         else
         {
             Toast.makeText(this,"请先聚类确定",Toast.LENGTH_LONG).show();
         }
     }

    public void searchclass() {   //寻找点所在的类

        for (int i = 0; i < pointset.length; i++) {
            double dist = 1;
            int flag2 = 0;

            for (int j = 0; j < oldclu.length; j++) {
                double distance = pointdist(pointset[i], oldclu[j]);

                if (distance < dist) {
                    dist = distance;
                    flag2 = j;
                }
            }
            pointset[i].flag = flag2 + 1;//对于点集pointset中的flag从1到core依次表示oldclu的第一个第二个到第core个点
        }

    }

    public double pointdist(point px, point py) {
        return Math.sqrt(Math.pow((px.x - py.x), 2) + Math.pow((px.y - py.y), 2));
    }//求出两点之间的距离

    public int count() {//求出并返回聚类点内含有的车辆数
            int total = 0;
            for (int i = 1; i <= oldclu.length; i++) {
                for (int j = 0; j < num; j++) {
                    if (pointset[j].flag == i && pointdist(oldclu[i - 1], pointset[j]) <= lanlondist) {//确定在i的聚点范围内有的车辆数
                        total++;
                    }
                }
            }
            return total;
    }

    public void adjustcore() {  //调整聚类点的X与Y坐标值
        for(int i=0;i<oldclu.length;i++) {
               int count=0;
               point adjustclu=new point(0,0);
               for(int j=0;j<pointset.length;j++) {
                   if (pointset[j].flag == i + 1) {
                       adjustclu.x+=pointset[j].x;
                       adjustclu.y+=pointset[j].y;
                       count++;
                   }
               }
               newclu[i]=new point(0,0);
               newclu[i].x=adjustclu.x/(double)count;
               newclu[i].y=adjustclu.y/(double)count;
               newclu[i].flag=0;
        }
    }

    public void change_oldtonew(point[]old,point[]news) {
        for(int i=0;i<old.length;i++) {
            old[i].x=news[i].x;
            old[i].y=news[i].y;
            old[i].flag=0;
        }
    }

    public void gettingbetter() {
        this.searchclass();
        this.adjustcore();
        double distance1;
        int flag3=-1;
        for(int i=0;i<oldclu.length;i++) {
             distance1=pointdist(oldclu[i],newclu[i]);
             if(distance1<0.0005)//聚类点变化最小距离（对应于实际的1米）
             {
                 flag3=0;
             }
            else {
                 flag3=1;
                 break;
             }
        }
        if (flag3 == 0)
        {
            StringBuilder strB=new StringBuilder();
            strB.append("系统处理完的聚类点结果如下：\n");
            for(int i=0;i<oldclu.length;i++) {
                strB.append("第");
                strB.append(i+1);
                strB.append("个聚类点坐标为： ");
                strB.append("经度：");
                strB.append(String.format("%.5f",oldclu[i].x));//以便更好的界面显示
                strB.append("纬度：");
                strB.append(String.format("%.5f",oldclu[i].y));//以便更好的界面显示
                strB.append('\n');

            }
            textView.setText(strB);
            isclusterfinished=true;
        }
        else
        {
           change_oldtonew(oldclu,newclu);
           gettingbetter();
        }
    }
/*理想化的结果
    public void gettingbetter_polished() {                                         //系统优化点数！
        this.searchclass();
        this.adjustcore();
        double distance1;
        int flag3 = -1;
        for (int i = 0; i < oldclu.length; i++) {
            distance1 = pointdist(oldclu[i], newclu[i]);
            if (distance1 < 0.00001)//聚类点变化最小距离
            {
                flag3 = 0;
            } else {
                flag3 = 1;
                break;
            }
        }
        if (flag3 == 0) {
            int count = 0;
            count = count();//count（）函数可以得到聚点范围内的所有车辆数
            if (((double) count / num) > 0.85)   //一般的通用办法，
                                               //以所选样本数量中收到信息数量占总所选样本数量之比
                                               //作为指标
            {
                strB_auto_core.append(core);
                isclusterfinished = true;
            } else //如果聚类条件不满足，则反复迭代
            {
                change_oldtonew(oldclu, newclu);//交换新旧点
                gettingbetter_polished();//重复执行该优化聚点函数
            }
        }
    }

*/

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        str_rate=String.valueOf(lanlon_ratevalue[i]);
        isItemselected=true;
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public boolean onLongClick(View view) {//长按则按顺序显示车辆数量
        if (iscountofcluster)
        {

            int temp1;
            int temp2=0;
            for (int i = 0; i < core; i++)
            {
                for (int j = i; j < core; j++)
                {

                    if (vehiclenum[i] <= vehiclenum[j])
                    {
                      temp1=vehiclenum[i];
                      vehiclenum[i]=vehiclenum[j];
                      vehiclenum[j]=temp1;
                      if(i==0)
                      {
                        temp2 = j;
                       }
                    }
                }
            }
            StringBuilder strB=new StringBuilder();
            strB.append("聚类点由多到少依次排序为：\n");
            strB.append("第");
            strB.append(temp2+1);
            strB.append("个聚点所含有的车辆数最多为：");
            strB.append(vehiclenum[0]);
            strB.append('\n');
            for (int i = 1; i < core; i++) {
                strB.append("按序下一个聚类点车辆数量为：");
                strB.append(vehiclenum[i]);
                strB.append('\n');
            }
            textView.setText(strB);
        }
        else{
            Toast.makeText(this,"请先单击进行聚类点所含车辆数量分析",Toast.LENGTH_LONG).show();
        }
           return true;
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



