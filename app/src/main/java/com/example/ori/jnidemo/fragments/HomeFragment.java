package com.example.ori.jnidemo.fragments;


import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.example.ori.jnidemo.R;
import com.example.ori.jnidemo.bean.CategoryItem;
import com.example.ori.jnidemo.bean.FragmentMessageEvent;
import com.example.ori.jnidemo.dapter.CategoryAdapter;
import com.example.ori.jnidemo.enums.CategoryEnum;
import com.example.ori.jnidemo.utils.LanguageUtil;
import com.example.ori.jnidemo.utils.ToastHelper;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * 首页 Fragment
 */
public class HomeFragment extends com.example.ori.jnidemo.base.Fragment {

    public static final Integer FRAGMENT_ID = 1;

    @BindView(R.id.gv_category)
    GridView gvCategory;

    private CategoryAdapter mCategoryAdapter;

    private List<CategoryItem> items = new ArrayList<>();

    public HomeFragment() {
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.fragment_home;
    }

    @Override
    protected void initData() {
        gvCategory.setNumColumns(4);
        gvCategory.setHorizontalSpacing(50);
        gvCategory.setVerticalSpacing(40);
        gvCategory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CategoryItem item = items.get(position);
                if (CategoryEnum.PLASTIC_BOTTLE_REGENERANT.getId().equals(item.getItemId())){
                    EventBus.getDefault().post(new FragmentMessageEvent(FragmentMessageEvent.SWITCH_FRAGMENT, RecycleFragment.FRAGMENT_ID, items.get(position)));
                }else {
                    // 中英文适配
                    if (LanguageUtil.isChinese()){
                        ToastHelper.showLongMessage(getActivity(), item.getCategoryName() + "副箱尚未安装！请选择其他类型进行回收！");
                    }else {
                        ToastHelper.showLongMessage(getActivity(), item.getCategoryName() + "The sub-box has not been installed! Please choose another type for recycling!");
                    }
                }
            }
        });

        mCategoryAdapter = new CategoryAdapter(getActivity(), items);
        gvCategory.setAdapter(mCategoryAdapter);
    }

    @Override
    public void setData(Object data) {
        if (data != null){
            items = (List<CategoryItem>) data;
        }
    }
}
