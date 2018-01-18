package com.shuyi.chenxiao.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;


import com.shuyi.chenxiao.R;
import java.util.ArrayList;

/**
 * Author：半世晨晓i
 * Time：2018/1/16
 * Email：shuyint@aliyin.com
 * Name：Adapter
 * Describe: GirdView Adapter
 */
public class MyAdapter extends BaseAdapter {

    private Context mContext;

    private LayoutInflater inflater;

    private ArrayList<Bitmap> bitmap_list = new ArrayList<>();


    private class ViewHolder {
        ImageView iv_img;

    }

    public MyAdapter(Context mContext, ArrayList<Bitmap> bitmap_list) {

        this.mContext = mContext;
        this.bitmap_list = bitmap_list;
        this.inflater = LayoutInflater.from(mContext);
    }

    @Override
    public int getCount() {
        //写死长度，可以根据需要自己修改
        //Demo最多展示三张
        return bitmap_list.size() == 3 ? 3 : (bitmap_list.size() + 1);
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_defect_addimg, parent, false);
            holder = new ViewHolder();
            holder.iv_img = (ImageView) convertView.findViewById(R.id.default_add_img);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        //最多展示三张图片，
        //控制添加按钮的展示和隐藏 就是+图片
        if (position < bitmap_list.size()) {
            holder.iv_img.setImageBitmap(bitmap_list.get(position));
        } else {
            holder.iv_img.setImageBitmap(BitmapFactory.decodeResource(mContext.getResources(),R.mipmap.m_addimg_1x));
        }
        return convertView;
    }
}
