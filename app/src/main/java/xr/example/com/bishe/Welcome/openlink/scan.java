package xr.example.com.bishe.Welcome.openlink;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import xr.example.com.bishe.Explanation.moreinfo;
import xr.example.com.bishe.Explanation.number;
import xr.example.com.bishe.R;
import xr.example.com.bishe.Welcome.CaptureActivity;
import xr.example.com.bishe.Welcome.EncodingUtils;

/**
 * Created by Administrator on 2018/4/1.
 */

public class scan extends Activity {
    private TextView mTextView;
    private EditText mEditText;
    private ImageView mImageView;
    private CheckBox mCheckBox;
    String pkg="com.tencent.mm";
    String cls="com.tencent.mm.ui.LauncherUI";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActionBar();
        setContentView(R.layout.scan);
        initView();
    }
    private void initView() {
        mTextView= (TextView) this.findViewById(R.id.tv_showResult);
        mEditText= (EditText) this.findViewById(R.id.et_text);
        mImageView= (ImageView) this.findViewById(R.id.img_show);
        mCheckBox= (CheckBox) this.findViewById(R.id.logo);
    }
    //扫描二维码
     //https://cli.im/text?2dd0d2b267ea882d797f03abf5b97d88二维码生成网站
    public void btn_scan(View view) {
        startActivityForResult(new Intent(this, CaptureActivity.class),0);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode==RESULT_OK){
            Bundle bundle = data.getExtras();
            if (bundle != null) {
                String result=bundle.getString("result");
                mTextView.setText(result);
            }
        }
    }
    //生成二维码 可以设置Logo
    public void make_QRCode(View view) {
        String input = mEditText.getText().toString();
        if (input.equals("")){
            Toast.makeText(this,"输入不能为空", Toast.LENGTH_SHORT).show();
        }else{
            Bitmap qrCode = EncodingUtils.createQRCode(input, 700, 700,
                    mCheckBox.isChecked()? BitmapFactory.decodeResource(getResources(),R.mipmap.ic_lanlonextract):null);//CheckBox选中就设置Logo
            mImageView.setImageBitmap(qrCode);
        }
    }
    private void setActionBar() {
        android.app.ActionBar actionBar=getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);//显示返回键
        actionBar.setDisplayShowHomeEnabled(false);//取消logo
        actionBar.setTitle("返回");//设置返回字样
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {//创建菜单方式一
        //   SubMenu subMenu = menu.addSubMenu("操作");
        menu.add(0, 1, Menu.NONE, "更多内容");
        menu.add(0, 2, Menu.NONE, "积分");
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return false;
            case 1:
                Intent it=new Intent(this,moreinfo.class);
                startActivity(it);
                break;
            case 2:
                Intent it2=new Intent(this,number.class);
                startActivity(it2);
                break;
        }
        return true;
    }
    public void open_link(View v) {
        switch (v.getId()) {
            case R.id.weichat_send://打开手机自身的应用
                ComponentName componentName=new ComponentName(pkg,cls);
                Intent it2=new Intent();
                it2.setComponent(componentName);
                it2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(it2);
        }
    }
}
