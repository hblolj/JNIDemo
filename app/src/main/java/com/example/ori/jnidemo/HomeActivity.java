package com.example.ori.jnidemo;

import android.util.Log;
import android.widget.FrameLayout;

import com.example.ori.jnidemo.base.Activity;
import com.example.ori.jnidemo.base.Fragment;
import com.example.ori.jnidemo.bean.FragmentMessageEvent;
import com.example.ori.jnidemo.fragments.HomeFragment;
import com.example.ori.jnidemo.fragments.RecycleFragment;
import com.example.ori.jnidemo.helper.NavHelper;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;

public class HomeActivity extends Activity implements NavHelper.OnTabChangedListener<Integer> {

    private static final String TAG = HomeActivity.class.getSimpleName();

    @BindView(R.id.lay_container)
    FrameLayout mContainer;

    public NavHelper<Integer> navHelper;

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_home;
    }

    @Override
    protected void initWidget() {
        super.initWidget();
        // 初始化切换到 HomeFragment 开始、投递结束
        // 投递回收 Fragment 投递中
        // 投递成功 Fragment +1
        // 投递完成 Fragment 开关门结算
        navHelper = new NavHelper<>(this, getSupportFragmentManager(), R.id.lay_container, this);
        navHelper
                .add(HomeFragment.FRAGMENT_ID, new NavHelper.Tab<>(HomeFragment.class, HomeFragment.FRAGMENT_ID))
                .add(RecycleFragment.FRAGMENT_ID, new NavHelper.Tab<>(RecycleFragment.class, RecycleFragment.FRAGMENT_ID));
    }

    @Override
    protected void initData() {
        super.initData();
        // 初始化点击
        navHelper.performClickMenu(HomeFragment.FRAGMENT_ID);
    }

    /**
     * Fragment 之间数据交互
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(FragmentMessageEvent event){
        if (FragmentMessageEvent.SWITCH_FRAGMENT.equals(event.getMessageType())){
            navHelper.performClickMenu((Integer) event.getData());
            ((Fragment) navHelper.getCurrentTab().getFragment()).setData(event.getExtra());
        }
        Log.d(TAG, "当前 Fragment: " + navHelper.getCurrentTab().clx.getSimpleName());
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().register(this);
        }
    }

    @Override
    public void onTabChanged(NavHelper.Tab<Integer> newTab, NavHelper.Tab<Integer> oldTab) {

    }
}
