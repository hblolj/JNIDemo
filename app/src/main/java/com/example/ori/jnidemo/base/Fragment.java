package com.example.ori.jnidemo.base;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import butterknife.Unbinder;

public abstract class Fragment extends android.support.v4.app.Fragment{

    protected View mRoot;

    private Unbinder mRootUnBind;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // 初始化参数
        initArgs(getArguments());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if (mRoot == null){
            int layoutId = getContentLayoutId();
            // 初始化当前的根布局，但是不在创建时就添加到 Container 里面
            View root = inflater.inflate(layoutId, container, false);
            initWidget(root);
            mRoot = root;
        }else {
            if (mRoot.getParent() != null){
                // 把当前 Root 从起父控件移除
                ((ViewGroup) mRoot.getParent()).removeView(mRoot);
            }
        }

        return mRoot;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);
        // 当 View 创建完成后，初始化数据
        initData();
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
     * 得到当前界面资源文件 Id
     * @return
     */
    protected abstract int getContentLayoutId();

    /**
     * 初始化控件
     * @param root
     */
    protected void initWidget(View root){
        mRootUnBind = ButterKnife.bind(this, root);
    }

    /**
     * 初始化数据
     */
    protected void initData(){

    }

    /**
     * 返回按键出发时调用
     * @return 返回 true 表示我已处理返回逻辑，Activity 不用自己 finish。
     * 返回 false 表示我没有处理逻辑，Activity 走自己的逻辑
     */
    public Boolean onBackPressed(){
        return false;
    }
}
