package com.example.ori.jnidemo.fragments;


import android.util.Log;
import android.view.View;

import com.example.ori.jnidemo.R;
import com.example.ori.jnidemo.bean.CategoryItem;

/**
 * 回收 Fragment
 */
public class RecycleFragment extends com.example.ori.jnidemo.base.Fragment {

    private static final String TAG = RecycleFragment.class.getSimpleName();

    public static final Integer FRAGMENT_ID = 2;

    private CategoryItem mCurrentItem;

    public RecycleFragment() {
        // Required empty public constructor
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.fragment_recycle;
    }

    @Override
    protected void initWidget(View root) {
        super.initWidget(root);
    }

    @Override
    protected void initData() {
        super.initData();
        Log.d(TAG, "initData: " + mCurrentItem);
        // TODO: 2018/12/27 需要知道当前回收的什么类别的回收物，然后采取对应的业务逻辑进行运行
        // TODO: 2018/12/27 塑料瓶回收流程独一份 
        // TODO: 2018/12/27 其他回收物流程暂为一份
    }

    @Override
    public void setData(Object data) {
        CategoryItem item = (CategoryItem) data;
        mCurrentItem = item;
        Log.d(TAG, "setData: " + mCurrentItem);
    }
}
