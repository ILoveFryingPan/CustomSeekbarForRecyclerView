package com.example.customseekbarforrecyclerview;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class FirstActivity extends AppCompatActivity implements View.OnClickListener {

    private int dip15;
    private int dip50;

    private LinearLayout rootLayout;
    private View rlBtn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(createView());

        dip15 = (int) getResources().getDimension(R.dimen.dip_15);
        dip50 = (int) getResources().getDimension(R.dimen.dip_50);

        rlBtn = createBtnView("自定义滚动条");
    }

    private View createView() {
        ScrollView scrollView = new ScrollView(this);
        scrollView.setBackgroundColor(0xffffffff);
        scrollView.setFillViewport(true);

        rootLayout = new LinearLayout(this);
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        rootLayout.setGravity(Gravity.CENTER);
        ViewGroup.LayoutParams rootLayoutLP = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        scrollView.addView(rootLayout, rootLayoutLP);

        return scrollView;
    }

    private View createBtnView(CharSequence content) {
        Button btn = new Button(this);
        btn.setTextSize(15);
        btn.setTextColor(0xff333333);
        btn.setGravity(Gravity.CENTER);
        btn.setText(content);
        btn.setOnClickListener(this);
        LinearLayout.LayoutParams btnLP = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dip50);
        btnLP.topMargin = dip15;
        btnLP.leftMargin = dip15;
        btnLP.rightMargin = dip15;
        rootLayout.addView(btn, btnLP);

        return btn;
    }

    @Override
    public void onClick(View v) {
        if (v.equals(rlBtn)) {
            startActivity(new Intent(this, MainActivity.class));
        }
    }
}
