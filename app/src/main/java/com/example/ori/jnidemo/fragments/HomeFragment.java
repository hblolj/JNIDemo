package com.example.ori.jnidemo.fragments;


import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.example.ori.jnidemo.R;
import com.example.ori.jnidemo.bean.CategoryItem;
import com.example.ori.jnidemo.bean.FragmentMessageEvent;
import com.example.ori.jnidemo.dapter.CategoryAdapter;
import com.example.ori.jnidemo.enums.CategoryEnum;
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

    private static void show(FragmentManager fragmentManager){
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.fragment_home;
    }

    @Override
    protected void initData() {
        items.add(new CategoryItem(CategoryEnum.METAL_REGENERANT));
        items.add(new CategoryItem(CategoryEnum.PLASTIC_REGENERANT));
        items.add(new CategoryItem(CategoryEnum.TEXTILE_REGENERANT));
        items.add(new CategoryItem(CategoryEnum.PLASTIC_BOTTLE__REGENERANT));
        items.add(new CategoryItem(CategoryEnum.PAPER_REGENERANT));
        items.add(new CategoryItem(CategoryEnum.GLASS_REGENERANT));
        items.add(new CategoryItem(CategoryEnum.HARMFUL_WASTE));

        gvCategory.setNumColumns(4);
        gvCategory.setHorizontalSpacing(50);
        gvCategory.setVerticalSpacing(40);
        gvCategory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ToastHelper.showShortMessage(getActivity(), items.get(position).getCategoryName());
                EventBus.getDefault().post(new FragmentMessageEvent(FragmentMessageEvent.SWITCH_FRAGMENT, RecycleFragment.FRAGMENT_ID, items.get(position)));
            }
        });

        mCategoryAdapter = new CategoryAdapter(getActivity(), items);
        gvCategory.setAdapter(mCategoryAdapter);
    }

    @Override
    public void setData(Object data) {

    }
}
