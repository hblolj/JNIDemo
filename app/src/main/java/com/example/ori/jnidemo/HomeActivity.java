package com.example.ori.jnidemo;

import android.widget.FrameLayout;

import com.example.ori.jnidemo.base.Activity;

import butterknife.BindView;

public class HomeActivity extends Activity {

    @BindView(R.id.lay_container)
    FrameLayout mContainer;

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_home;
    }

    @Override
    protected void initWindows() {
        super.initWindows();
    }

    @Override
    protected void initData() {
        super.initData();
    }
}
