package xr.example.com.bishe.Algorism;

import java.io.Serializable;

/**
 * Created by Administrator on 2018/3/2.
 */

public class point implements Serializable {//定义point点类
    public double x=0;
    public double y=0;
    public int flag=-1;

    public double getX() {
        return x;
    }//没有用到

    public double getY() {
        return  y;
    }//没有用到

    public point(double x, double y) {
        this.x = x;
        this.y = y;
    }

}
