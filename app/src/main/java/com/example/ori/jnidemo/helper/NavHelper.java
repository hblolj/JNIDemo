package com.example.ori.jnidemo.helper;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.SparseArray;


/**
 * 完成对 Fragment 的调度与重用问题
 */
public class NavHelper<T> {

    private final FragmentManager fragmentManager;

    private final Integer containerId;

    private final Context context;

    private OnTabChangedListener<T> listener;

    /**
     * 当前选中的 Tab
     */
    private Tab<T> currentTab;

    private final SparseArray<Tab<T>> tabs = new SparseArray<>();

    public NavHelper(Context context, FragmentManager fragmentManager, Integer containerId, OnTabChangedListener<T> listener) {
        this.fragmentManager = fragmentManager;
        this.containerId = containerId;
        this.context = context;
        this.listener = listener;
    }

    /**
     * 执行点击菜单操作
     * @param menuId 菜单的 Id
     * @return 是否能够处理该次点击
     */
    public Boolean performClickMenu(int menuId){

        Tab<T> tab = tabs.get(menuId);

        if (tab != null){
            doSelect(tab);
            return true;
        }

        return false;
    }

    /**
     * 进行真实的 Tab 选择操作
     * @param tab
     */
    private void doSelect(Tab<T> tab) {

        Tab<T> oldTab = null;
        if (currentTab != null){
            oldTab = currentTab;
            if (oldTab == tab){
                // 如果说当前的 Tab 就是点击的 Tab，不做处理，也可以做二次点击刷新等其他处理
                notifyReSelect(tab);
                return;
            }
        }

        // 赋值并调用切换方法
        currentTab = tab;
        doTabChanged(currentTab, oldTab);

    }

    private void doTabChanged(Tab<T> newTab, Tab<T> oldTab) {

        FragmentTransaction ft = fragmentManager.beginTransaction();
        if (oldTab != null){
            if (oldTab.fragment != null){
                // 从界面移除，但是还在 Fragment 的缓存控件中
                ft.detach(oldTab.fragment);
            }
        }

        if(newTab != null){
            if (newTab.fragment == null){
                // 首次新建、缓存
                newTab.fragment = Fragment.instantiate(context, newTab.clx.getName(), null);
                // 提交到 FragmentManager
                ft.add(containerId, newTab.fragment, newTab.clx.getName());
            }else {
                // 从 FragmentManager 的缓存控件中从新加载到界面中
                ft.attach(newTab.fragment);
            }
        }

        // 提交事务
        ft.commit();
        // 通知回调
        notifyTabSelect(newTab, oldTab);
    }

    /**
     * 回调监听器
     * @param newTab
     * @param oldTab
     */
    private void notifyTabSelect(Tab<T> newTab, Tab<T> oldTab) {
        if (listener != null){
            listener.onTabChanged(newTab, oldTab);
        }
    }

    private void notifyReSelect(Tab<T> tab) {
        // TODO: 2018/9/27 二次点击所做的操作
    }

    /**
     * 添加对应的菜单 Id
     * @param menuId
     * @param tab
     */
    public NavHelper<T> add(int menuId, Tab<T> tab){
        tabs.put(menuId, tab);
        return this;
    }

    /**
     * 获取当前显示的 Tab
     * @return
     */
    public Tab<T> getCurrentTab(){
        return currentTab;
    }

    /**
     * 我们的所有 Tab 的基础属性
     * @param <T>
     */
    public static class Tab<T>{

        public Class<?> clx;
        // 额外字段
        public T extra;
        // 内部缓存对应的 Fragment
        Fragment fragment;

        public Fragment getFragment() {
            return fragment;
        }

        public Tab(Class<?> clx, T extra) {
            this.clx = clx;
            this.extra = extra;
        }
    }

    /**
     * 定义事件处理完成后的回调接口
     * @param <T>
     */
    public interface OnTabChangedListener<T>{

        void onTabChanged(Tab<T> newTab, Tab<T> oldTab);
    }
}
