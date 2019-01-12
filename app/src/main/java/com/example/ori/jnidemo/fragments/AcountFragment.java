package com.example.ori.jnidemo.fragments;


import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.ori.jnidemo.HomeActivity;
import com.example.ori.jnidemo.R;
import com.example.ori.jnidemo.base.Fragment;
import com.example.ori.jnidemo.bean.CategoryItem;
import com.example.ori.jnidemo.bean.FragmentMessageEvent;
import com.example.ori.jnidemo.enums.CategoryEnum;

import org.greenrobot.eventbus.EventBus;

import java.math.BigDecimal;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 结算 Fragment
 */
public class AcountFragment extends Fragment {

    public static final Integer FRAGMENT_ID = 3;

    @BindView(R.id.tv_list)
    TextView tvList;

    @BindView(R.id.tv_money)
    TextView tvMoney;

    @BindView(R.id.btn_finish_recycle)
    Button btnFinishRecycle;

    @BindView(R.id.btn_continue_recycle)
    Button btnContinueRecycle;

    @BindView(R.id.tv_count_down_time)
    TextView tvCountDownTime;

    @BindView(R.id.count_down_time)
    LinearLayout llCountDownTime;

    private CategoryItem mCurrentItem;

    private CountDownTimer countDownTimer;

    public AcountFragment() {
        // Required empty public constructor
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.fragment_acount;
    }

    @Override
    protected void initData() {
        super.initData();

        String s;
        BigDecimal t;

        if (CategoryEnum.PLASTIC_BOTTLE_REGENERANT.getId().equals(mCurrentItem.getItemId())){
            // 瓶子
            s = mCurrentItem.getCategoryName() + HomeActivity.CURRENT_RECYCLE_PLASTIC_BOTTLE_NUM + mCurrentItem.getUnit();
            t = mCurrentItem.getdPrice().multiply(new BigDecimal(HomeActivity.CURRENT_RECYCLE_PLASTIC_BOTTLE_NUM));
        }else {
            // 金属、纸类 做一下减法
            BigDecimal validWeigh = HomeActivity.getValidWeigh(mCurrentItem);
            s = mCurrentItem.getCategoryName() + validWeigh + mCurrentItem.getUnit();
            t = mCurrentItem.getdPrice().multiply(validWeigh);
        }
        tvList.setText(s);

        String sm = "获得环保金" + t + "元";
        tvMoney.setText(sm);
        tvCountDownTime.setVisibility(View.VISIBLE);
        llCountDownTime.setVisibility(View.VISIBLE);

        countDownTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                String s = String.valueOf(millisUntilFinished / 1000) + "秒";
                tvCountDownTime.setText(s);
            }

            @Override
            public void onFinish() {
                // 倒计时结束，仍未作出选择，默认选择结束投递
                finishRecycle();
            }
        };
        countDownTimer.start();
        HomeActivity.CURRENT_RECYCLE_PLASTIC_BOTTLE_NUM = 0;
    }

    @Override
    protected void initWidget(View root) {
        super.initWidget(root);
    }

    @Override
    public void setData(Object data) {
        mCurrentItem = (CategoryItem) data;
    }

    @OnClick(R.id.btn_finish_recycle)
    public void finishRecycle(){
        // 结束投递
        countDownTimer.cancel();
        tvCountDownTime.setVisibility(View.GONE);
        llCountDownTime.setVisibility(View.INVISIBLE);
        EventBus.getDefault().post(new FragmentMessageEvent(FragmentMessageEvent.SWITCH_FRAGMENT, HomeFragment.FRAGMENT_ID, null));
    }

    @OnClick(R.id.btn_continue_recycle)
    public void continueRecycle(){
        // 继续投递
        countDownTimer.cancel();
        tvCountDownTime.setVisibility(View.GONE);
        llCountDownTime.setVisibility(View.INVISIBLE);
        // 跳转到 HomeFragment
        EventBus.getDefault().post(new FragmentMessageEvent(FragmentMessageEvent.SWITCH_FRAGMENT, HomeFragment.FRAGMENT_ID, null));
    }

}
