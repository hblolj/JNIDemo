package com.example.ori.jnidemo.dapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ori.jnidemo.R;
import com.example.ori.jnidemo.bean.CategoryItem;

import java.util.List;

/**
 * @author: hblolj
 * @date: 2018/12/25 19:45
 * @description:
 */
public class CategoryAdapter extends BaseAdapter{

    private Context context;

    private List<CategoryItem> datas;

    public CategoryAdapter() {
    }

    public CategoryAdapter(Context context, List<CategoryItem> datas) {
        this.context = context;
        this.datas = datas;
    }

    @Override
    public int getCount() {
        return datas.size();
    }

    @Override
    public Object getItem(int position) {
        return datas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        if (convertView == null){
            convertView = View.inflate(context, R.layout.category_grid_view_item_layout, null);
            holder = new ViewHolder();
            holder.name = convertView.findViewById(R.id.tv_name);
            holder.price = convertView.findViewById(R.id.tv_price);
            convertView.setTag(holder);
        }else {
            holder = (ViewHolder) convertView.getTag();
        }

        CategoryItem item = datas.get(position);
        if (item != null){
            holder.name.setText(item.getCategoryName());
            holder.price.setText(item.getUnitPrice());
        }

        return convertView;
    }

    public class ViewHolder{
        ImageView icon;
        TextView name;
        TextView price;
    }
}
