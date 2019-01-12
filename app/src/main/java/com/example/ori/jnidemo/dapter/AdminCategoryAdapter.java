package com.example.ori.jnidemo.dapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.ori.jnidemo.HomeActivity;
import com.example.ori.jnidemo.R;
import com.example.ori.jnidemo.bean.ActionMessageEvent;
import com.example.ori.jnidemo.bean.CategoryItem;
import com.example.ori.jnidemo.constant.ComConstant;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import static com.example.ori.jnidemo.constant.ComConstant.getAddressCodeByCategoryItemId;

/**
 * @author: hblolj
 * @date: 2019/1/9 16:14
 * @description:
 */
public class AdminCategoryAdapter extends RecyclerView.Adapter<AdminCategoryAdapter.ViewHolder>{

    private List<CategoryItem> datas;

    private Context mContext;

    public AdminCategoryAdapter() {
    }

    public AdminCategoryAdapter(List<CategoryItem> datas, Context mContext) {
        this.datas = datas;
        this.mContext = mContext;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.admin_category_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final CategoryItem item = datas.get(position);
        String d = item.getCategoryName() + "\r\n" + HomeActivity.getDataByCategoryItem(item);
        holder.tvCategory.setText(d);
        holder.btnOpenDoor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 发送开门指令
                String targetSource = getAddressCodeByCategoryItemId(item.getItemId());
                EventBus.getDefault().post(new ActionMessageEvent(targetSource, ComConstant.OPEN_ADMIN_RECYCLE_ACTION_CODE, null, 1, false));
            }
        });
        holder.btnFinishRecycler.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 重量清理，返回首页
                HomeActivity.cleanDataByCategoryItem(item);
                String d = item.getCategoryName() + "\r\n" + HomeActivity.getDataByCategoryItem(item);
                holder.tvCategory.setText(d);
                String targetSource = getAddressCodeByCategoryItemId(item.getItemId());
                EventBus.getDefault().post(new ActionMessageEvent(targetSource, ComConstant.CLOSE_ADMIN_RECYCLE_ACTION_CODE, null, 1, false));
//                EventBus.getDefault().post(new FragmentMessageEvent(FragmentMessageEvent.SWITCH_FRAGMENT, HomeFragment.FRAGMENT_ID, null));
            }
        });
    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{

        TextView tvCategory;
        Button btnOpenDoor;
        Button btnFinishRecycler;

        public ViewHolder(View itemView) {
            super(itemView);
            tvCategory = (TextView) itemView.findViewById(R.id.tv_category_name);
            btnOpenDoor = (Button) itemView.findViewById(R.id.btn_open_door);
            btnFinishRecycler = (Button) itemView.findViewById(R.id.btn_finish_recycle);
        }
    }
}
