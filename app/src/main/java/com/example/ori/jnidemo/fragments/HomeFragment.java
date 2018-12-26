package com.example.ori.jnidemo.fragments;


import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.example.ori.jnidemo.R;
import com.example.ori.jnidemo.bean.CategoryItem;
import com.example.ori.jnidemo.dapter.CategoryAdapter;
import com.example.ori.jnidemo.utils.ToastHelper;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * 首页 Fragment
 */
public class HomeFragment extends com.example.ori.jnidemo.base.Fragment {

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
        items.add(new CategoryItem("金属", "0.60元/公斤"));
        items.add(new CategoryItem("塑料", "0.70元/公斤"));
        items.add(new CategoryItem("纺织物", "0.20元/公斤"));
        items.add(new CategoryItem("饮料瓶", "0.04元/个"));
        items.add(new CategoryItem("纸类", "0.70元/公斤"));
        items.add(new CategoryItem("玻璃", ""));
        items.add(new CategoryItem("有害垃圾", ""));

        gvCategory.setNumColumns(4);
        gvCategory.setHorizontalSpacing(50);
        gvCategory.setVerticalSpacing(40);
        gvCategory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ToastHelper.showShortMessage(getActivity(), items.get(position).getCategoryName());
            }
        });

        mCategoryAdapter = new CategoryAdapter(getActivity(), items);
        gvCategory.setAdapter(mCategoryAdapter);
    }
}
