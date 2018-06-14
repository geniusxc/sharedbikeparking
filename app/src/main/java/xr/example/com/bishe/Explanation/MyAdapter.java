package xr.example.com.bishe.Explanation;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import xr.example.com.bishe.R;


public class MyAdapter extends BaseAdapter{//继承BaseAdapter的自定义的MyAdapter
   Context context;
    List<Map<String,Object>> list;
    LayoutInflater inflater1;

    public Map<Integer,Boolean>checkedMap;//保存checkbox是否被选中的状态
    public Map<Integer,Integer>colorMap;//保存textview中文字的状态
    public Map<Integer,Integer>visibleMap;//保存checkbox的可见性

    // alt+insert(fn), 插入代码
    public MyAdapter(List<Map<String,Object>>list, Context context) {
        super();
        this.inflater1 = LayoutInflater.from(context);
        this.list = list;
        this.context=context;
        checkedMap = new HashMap<Integer,Boolean>();
        colorMap = new HashMap<Integer, Integer>();
        visibleMap = new HashMap<Integer, Integer>();
        for(int i=0;i<list.size();i++) {//初始化checkedMap,colorMap,visibleMap功能
            checkedMap.put(i, false);
            colorMap.put(i, Color.WHITE);
            visibleMap.put(i,CheckBox.INVISIBLE);
        }

    }


    @Override
    public int getCount() {
        return list.size(); //返回多少个
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);// 返回一个map
    }
    @Override
    public long getItemId(int position) {
        return position;
    }//返回单击项目的位置

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = inflater1.inflate(R.layout.item,null);
        TextView num = (TextView) view.findViewById(R.id.tv_num);
        TextView mess = (TextView) view.findViewById(R.id.tv_mess);
        TextView date=(TextView)view.findViewById(R.id.tv_date);

        CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkbox);
        checkBox.setVisibility(visibleMap.get(position));
        checkBox.setChecked(checkedMap.get(position));

        Map map = list.get(position);
        num.setText((String) map.get("num"));
        mess.setText((String)map.get("mess"));
        long result=(long)map.get("date");
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd\nHH时mm分");
        String res=sdf.format(result);
        date.setText(res);
        return view;
    }
}
