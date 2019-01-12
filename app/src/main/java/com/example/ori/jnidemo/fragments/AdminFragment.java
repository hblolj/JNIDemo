package com.example.ori.jnidemo.fragments;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.example.ori.jnidemo.R;
import com.example.ori.jnidemo.bean.CategoryItem;
import com.example.ori.jnidemo.dapter.AdminCategoryAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * 管理员界面
 */
public class AdminFragment extends com.example.ori.jnidemo.base.Fragment {

    public static final Integer FRAGMENT_ID = 4;

    @BindView(R.id.rl_admin)
    RecyclerView rlCategory;

    private List<CategoryItem> datas = new ArrayList<>();

    private AdminCategoryAdapter adapter;

    public AdminFragment() {
        // Required empty public constructor
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.fragment_admin;
    }

    @Override
    protected void initData() {
        super.initData();
        adapter = new AdminCategoryAdapter(datas, getActivity());
        rlCategory.setLayoutManager(new LinearLayoutManager(getContext()));
        rlCategory.setAdapter(adapter);
    }

    @Override
    public void setData(Object data) {
        if (data != null){
            datas = (List<CategoryItem>) data;
        }
    }

}
