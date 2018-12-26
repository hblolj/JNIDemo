package com.example.ori.jnidemo.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import java.util.List;

import butterknife.ButterKnife;

public abstract class Activity extends AppCompatActivity {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 在界面未初始化之前调用的初始化窗口
        initWindows();

        if (initArgs(getIntent().getExtras())){
            setContentView(getContentLayoutId());
            initWidget();
            initData();
        }else {
            finish();
        }
    }

    /**
     * 初始化窗口
     */
    protected void initWindows(){

    }

    /**
     * 初始化相关参数，
     * @param bundle
     * @return 如果参数正确，返回<code>true</code>。错误，返回<code>false</code>
     */
    protected Boolean initArgs(Bundle bundle){
        return true;
    }

    /**
     * 得到当前界面的资源文件 Id
     * @return
     */
    protected abstract int getContentLayoutId();

    /**
     * 初始化控件
     */
    protected void initWidget(){
        ButterKnife.bind(this);
    }

    /**
     * 初始化数据
     */
    protected void initData(){

    }

    /**
     * 当点击界面导航返回时，Finish 当前界面
     * @return
     */
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {

        List<android.support.v4.app.Fragment> fragments = getSupportFragmentManager().getFragments();
        if (fragments != null && fragments.size() > 0){
            for (android.support.v4.app.Fragment f : fragments){
                if (f instanceof Fragment){
                    if (((com.example.ori.jnidemo.base.Fragment)f).onBackPressed()){
                        return;
                    }
                }
            }
        }

        super.onBackPressed();

        finish();
    }
}
