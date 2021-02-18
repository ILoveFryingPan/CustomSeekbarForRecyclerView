package com.example.customseekbarforrecyclerview;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.seekbarrecycler.ScrollbarRecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private int dip15;
    private int dip50;

    private boolean hasFocus = false;

    private ScrollbarRecyclerView srl;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initData();
//        initBar();
    }

    private void initView() {
        srl = findViewById(R.id.seekbar_rl);
    }

    private void initData() {

        dip15 = (int) getResources().getDimension(R.dimen.dip_15);
        dip50 = (int) getResources().getDimension(R.dimen.dip_50);

        srl.setLayoutManager(new LinearLayoutManager(this));
        List<String> itemList = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            itemList.add("数据：第" + i + "条");
        }
        srl.setAdapter(new ListAdapter(itemList));
    }

    private void initBar() {
        srl.setScrollbarBarWidth(dip15);
        srl.setScrollbarBGWidth(dip15);
        srl.setScrollbarWidth(dip15);
        srl.setScrollbarBGLength(dip50 * 3);
        srl.setScrollbarBGShape(R.drawable.back_corner_solid00ffff);
        //上面的几行代码是对默认自定义滚动条的属性设置，如果使用ScrollbarView接口创建自定义滚动条，则上面的代码无效
        srl.setSpaceByParent(dip15);
        srl.setSpaceByList(dip50);
        srl.setScrollbarBarMinLength(dip15 * 2);
        srl.setIsFloatBar(1);
        srl.setLengthVariable(ScrollbarRecyclerView.LENGTH_VARIABLE_FALSE);
        srl.setScrollbarBarLength(dip15);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && !this.hasFocus) {
            this.hasFocus = hasFocus;
            //为了在自定义控件中能够准确计算宽高，第一次默认在这里调用
            srl.createScrollBar();
        }
    }

    private class ListAdapter extends RecyclerView.Adapter<ListAdapter.ListViewHolder> {

        private List<String> itemList;

        public ListAdapter(List<String> itemList) {
            this.itemList = itemList;
        }

        @NonNull
        @Override
        public ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ListViewHolder(createItemView());
        }

        @Override
        public void onBindViewHolder(@NonNull ListViewHolder holder, int position) {
            holder.itemText.setText(itemList.get(position));
        }

        @Override
        public int getItemCount() {
            return null == itemList? 0 : itemList.size();
        }

        public class ListViewHolder extends RecyclerView.ViewHolder {

            private final TextView itemText;

            public ListViewHolder(@NonNull View itemView) {
                super(itemView);
                itemText = (TextView) ((RelativeLayout) itemView).getChildAt(0);
            }
        }

        private View createItemView() {
            RelativeLayout itemLayout = new RelativeLayout(MainActivity.this);
            ViewGroup.LayoutParams itemLayoutLP = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dip50);
            itemLayout.setLayoutParams(itemLayoutLP);

            TextView itemText = new TextView(MainActivity.this);
            itemText.setTextSize(15);
            itemText.setTextColor(0xff333333);
            itemText.setGravity(Gravity.CENTER_VERTICAL);
            RelativeLayout.LayoutParams itemTextLP = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            itemTextLP.leftMargin = dip15;
            itemLayout.addView(itemText, itemTextLP);

            View line = new View(MainActivity.this);
            line.setBackgroundColor(0xffe0e0e0);
            RelativeLayout.LayoutParams lineLP = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1);
            itemLayout.addView(line, lineLP);

            return itemLayout;
        }
    }
}
