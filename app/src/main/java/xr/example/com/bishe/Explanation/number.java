package xr.example.com.bishe.Explanation;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import xr.example.com.bishe.R;
import xr.example.com.bishe.Welcome.MessActivity;

/**
 * Created by Administrator on 2018/4/2.
 */

public class number extends Activity {
    MessActivity numsend;
    TextView jifen;
    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle saveInstanced) {
        super.onCreate(saveInstanced);
        setActionBar();//设置带有返回键的标题
        setContentView(R.layout.activity_number);
        jifen=(TextView)findViewById(R.id.jifen);
        int jifen1=numsend.jifen;
        StringBuilder sb=new StringBuilder();
        sb.append(jifen1);
        jifen.setText(sb);
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
