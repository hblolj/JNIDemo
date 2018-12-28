package com.example.ori.jnidemo.fragments;


import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.ori.jnidemo.HomeActivity;
import com.example.ori.jnidemo.R;
import com.example.ori.jnidemo.base.Fragment;
import com.example.ori.jnidemo.bean.FragmentMessageEvent;

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
    }

    @Override
    protected void initWidget(View root) {
        super.initWidget(root);
        String s = "饮料瓶" + HomeActivity.CURRENT_RECYCLE_PLASTIC_BOTTLE_NUM + "个";
        tvList.setText(s);
        BigDecimal m = new BigDecimal(HomeActivity.CURRENT_RECYCLE_PLASTIC_BOTTLE_NUM * 0.04);
        String sm = "获得环保金" + m + "元";
        tvMoney.setText(sm);
        tvCountDownTime.setVisibility(View.VISIBLE);
    }

    @Override
    public void setData(Object data) {

    }

    @OnClick(R.id.btn_finish_recycle)
    public void finishRecycle(){
        // 结束投递
        countDownTimer.cancel();
        tvCountDownTime.setVisibility(View.GONE);
        // 跳转到 总结算页面(所有种类总收益)
    }

    @OnClick(R.id.btn_continue_recycle)
    public void continueRecycle(){
        // 继续投递
        countDownTimer.cancel();
        tvCountDownTime.setVisibility(View.GONE);
        // 跳转到 HomeFragment
        EventBus.getDefault().post(new FragmentMessageEvent(FragmentMessageEvent.SWITCH_FRAGMENT, HomeFragment.FRAGMENT_ID, ""));
    }

}
