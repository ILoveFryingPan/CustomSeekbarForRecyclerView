package com.example.seekbarrecycler;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.DrawableRes;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ScrollbarRecyclerView extends RelativeLayout {

    private RecyclerView recyclerView;

    public static final int SITE_NONE = -1;

    @IntDef({SITE_NONE, RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.ALIGN_PARENT_RIGHT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Site {
    }

    @Site
    private int scrollbarSite = SITE_NONE;                   //scrollbarbar的所在位置

    @IntDef({RecyclerView.HORIZONTAL, RecyclerView.VERTICAL})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Orientation {
    }

    @Orientation
    private int mOrientation = RecyclerView.VERTICAL;                               //列表的滚动方式

    /*
      scrollbar可以在列表的一侧, 也可以在列表的上方
      当isFloatBar是0的时候表示scrollbar在列表的一侧
      当isFloatBar是1的时候表示scrollbar悬浮在列表的上方
     */
    private int isFloatBar = 0;

    /*
        该变量表示滚动条scrollbar与列表的距离
        即滚动条在左侧，则表示滚动条右侧距离列表左侧的距离
        当isFloatBar是1的时候，因为滚动条同一层没有相对的控件，所以该变量无效
     */
    private int spaceByList = 0;

    /*
        该变量表示滚动条与父容器之间的距离
        即滚动条在左侧，则表示滚动条左侧距离父容器的距离
     */
    private int spaceByParent = 0;

    /*
        滚动条的长度，默认为RecyclerView在屏幕中的可视区域的长度
     */
    private int scrollbarLength = -1;

    /*
        滚动条的宽度，默认是5dp
     */
    private int scrollbarWidth;

    /*
        滚动条背景View的宽度，默认是5dp
     */
    private int scrollbarBGWidth;

    /*
        滚动条背景View的长度，默认与scrollbarLength一致
     */
    private int scrollbarBGLength = -1;

    /*
        滚动条滚动View的宽度，默认是5dp
     */
    private int scrollbarBarWidth;

    /*
        滚动条滚动View的长度，该长度默认通过列表的屏幕可视区域和列表的总长度计算得来
        当长度可变时，通过set设置的长度无效
     */
    private int scrollbarBarLength = -1;

    public static final int LENGTH_VARIABLE_TRUE = 1;       //滚动条长度可变
    public static final int LENGTH_VARIABLE_FALSE = 0;      //滚动条长度不可变

    @IntDef({LENGTH_VARIABLE_TRUE, LENGTH_VARIABLE_FALSE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface LengthVariable{}
    /*
        滚动条滚动view的长度是否可变，默认是1，表示可变
        值为0时，表示不可变，此时 scrollbarBarLength 必须有值，否则抛出异常
     */
    @LengthVariable
    private int lengthVariable = LENGTH_VARIABLE_TRUE;

    /*
        是滚动条可滚动View的最小长度，默认是15dp，可以修改
     */
    private int scrollbarBarMinLength;

    @DrawableRes
    private int scrollbarBGShape = R.drawable.back_corner_solidf7f7f7;

    @DrawableRes
    private int scrollbarBarShape = R.drawable.back_corner_solidff6f28;

    /*
        创建自定义scrollbar 的View的接口
     */
    private ScrollbarView scrollbarView;

    /*
        scrollbar的对象
     */
    private RelativeLayout scrollbarRootLayout;

    /*
        scrollbar的背景view
     */
    private View bgView;

    /*
        scrollbar的滚动view
     */
    private View barView;

    /*
        列表的可视区域的长度
     */
    private int extent;

    /*
        列表的总长度
        RecyclerView返回的不准确，所以滚动过程中要时刻根据返回的长度重新计算
        如果滚动条的长度不固定，则修改滚动条的长度，同时计算ratio变量的值
        如果滚动条的长度固定不变或者滚动条的长度达到最小值，则只是重新计算ratio的值
     */
    private int range;

    /*
        滚动条背景view与滚动view的长度的差值
     */
    private int barSpace;

    /*
        该变量是一个比例，是滚动条最大偏移量和列表最大偏移量的比例
     */
    private float ratio;

    public ScrollbarRecyclerView(Context context) {
        super(context);
        init(null);
    }

    public ScrollbarRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public ScrollbarRecyclerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public ScrollbarRecyclerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    private void init(AttributeSet attrs) {

        setPadding(0, 0, 0, 0);     //去除padding
        recyclerView = new RecyclerView(getContext());
        recyclerView.setHorizontalScrollBarEnabled(false);
        recyclerView.setVerticalScrollBarEnabled(false);
//        initScrollbar(recyclerView);
        RelativeLayout.LayoutParams recyclerViewLP = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        addView(recyclerView, recyclerViewLP);

        int dip5 = (int) getContext().getResources().getDimension(R.dimen.dip_5);
        int dip15 = (int) getContext().getResources().getDimension(R.dimen.dip_15);

        scrollbarWidth = dip5;
        scrollbarBGWidth = dip5;
        scrollbarBarWidth = dip5;

        scrollbarBarMinLength = dip15;

        if (null != attrs) {
            TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.ScrollbarRecyclerView);
            boolean isFloat = ta.getBoolean(R.styleable.ScrollbarRecyclerView_isFloatBar, false);
            if (isFloat) {
                isFloatBar = 1;
            } else {
                isFloatBar = 0;
            }
            spaceByList = ta.getDimensionPixelSize(R.styleable.ScrollbarRecyclerView_spaceByList, 0);
            spaceByParent = ta.getDimensionPixelSize(R.styleable.ScrollbarRecyclerView_spaceByParent, 0);
            scrollbarLength = ta.getDimensionPixelSize(R.styleable.ScrollbarRecyclerView_scrollbarLength, -1);
            scrollbarWidth = ta.getDimensionPixelSize(R.styleable.ScrollbarRecyclerView_scrollbarWidth, dip5);
            scrollbarBGWidth = ta.getDimensionPixelSize(R.styleable.ScrollbarRecyclerView_scrollbarBGWidth, dip5);
            scrollbarBGLength = ta.getDimensionPixelSize(R.styleable.ScrollbarRecyclerView_scrollbarBGLength, -1);
            scrollbarBarWidth = ta.getDimensionPixelSize(R.styleable.ScrollbarRecyclerView_scrollbarBarWidth, dip5);
            scrollbarBarLength = ta.getDimensionPixelSize(R.styleable.ScrollbarRecyclerView_scrollbarBarLength, -1);
            boolean variable = ta.getBoolean(R.styleable.ScrollbarRecyclerView_lengthVariable, true);
            if (variable) {
                lengthVariable = LENGTH_VARIABLE_TRUE;
            } else {
                lengthVariable = LENGTH_VARIABLE_FALSE;
            }
            scrollbarBarMinLength = ta.getDimensionPixelSize(R.styleable.ScrollbarRecyclerView_scrollbarBarMinLength, dip15);
            scrollbarBGShape = ta.getResourceId(R.styleable.ScrollbarRecyclerView_scrollbarBGShape, R.drawable.back_corner_solidf7f7f7);
            scrollbarBarShape = ta.getResourceId(R.styleable.ScrollbarRecyclerView_scrollbarBarShape, R.drawable.back_corner_solidff6f28);
        }
    }

    /*  该方法设置后可以让动态创建的RecyclerView显示滚动条
        在这里仅仅用于查看默认滚动条样式，作为自动以滚动条的参考
     */
    private void initScrollbar(RecyclerView mRecyclerView) {
        mRecyclerView.setVerticalScrollBarEnabled(true);
        mRecyclerView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
        try {
            @SuppressLint("PrivateApi") Method method = View.class.getDeclaredMethod("initializeScrollbars", TypedArray.class);
            try {
                method.setAccessible(true);
                method.invoke(mRecyclerView, (Object) null);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public void setScrollbarSite(@Site int scrollbarSite) {
        this.scrollbarSite = scrollbarSite;
        if (scrollbarSite == RelativeLayout.ALIGN_PARENT_BOTTOM
                || scrollbarSite == RelativeLayout.ALIGN_PARENT_TOP) {
            setmOrientation(RecyclerView.HORIZONTAL);
        } else if (scrollbarSite == RelativeLayout.ALIGN_PARENT_LEFT
                || scrollbarSite == RelativeLayout.ALIGN_PARENT_RIGHT) {
            setmOrientation(RecyclerView.VERTICAL);
        }
    }

    public void setmOrientation(@Orientation int mOrientation) {
        this.mOrientation = mOrientation;
    }

    public void setIsFloatBar(int isFloatBar) {
        this.isFloatBar = isFloatBar;
    }

    public void setSpaceByList(int spaceByList) {
        this.spaceByList = spaceByList;
    }

    public void setSpaceByParent(int spaceByParent) {
        this.spaceByParent = spaceByParent;
    }

    public void setScrollbarLength(int scrollbarLength) {
        this.scrollbarLength = scrollbarLength;
    }

    public void setScrollbarWidth(int scrollbarWidth) {
        this.scrollbarWidth = scrollbarWidth;
    }

    public void setScrollbarBGWidth(int scrollbarBGWidth) {
        this.scrollbarBGWidth = scrollbarBGWidth;
    }

    public void setScrollbarBGLength(int scrollbarBGLength) {
        this.scrollbarBGLength = scrollbarBGLength;
    }

    public void setScrollbarBarWidth(int scrollbarBarWidth) {
        this.scrollbarBarWidth = scrollbarBarWidth;
    }

    public void setScrollbarBarLength(int scrollbarBarLength) {
        this.scrollbarBarLength = scrollbarBarLength;
    }

    public void setLengthVariable(@LengthVariable int lengthVariable) {
        this.lengthVariable = lengthVariable;
    }

    public void setScrollbarBarMinLength(int scrollbarBarMinLength) {
        this.scrollbarBarMinLength = scrollbarBarMinLength;
    }

    public void setScrollbarBGShape(@DrawableRes int scrollbarBGShape) {
        this.scrollbarBGShape = scrollbarBGShape;
    }

    public void setScrollbarBarShape(@DrawableRes int scrollbarBarShape) {
        this.scrollbarBarShape = scrollbarBarShape;
    }

    public void setScrollbarView(@Nullable ScrollbarView scrollbarView) {
        this.scrollbarView = scrollbarView;
    }

    public void setLayoutManager(RecyclerView.LayoutManager layoutManager) {
        recyclerView.setLayoutManager(layoutManager);
        int site = checkDefaultSite(layoutManager);
        setScrollbarSite(site);
    }

    public void setAdapter(RecyclerView.Adapter adapter) {
        recyclerView.setAdapter(adapter);
    }

    public void addOnScrollListener(RecyclerView.OnScrollListener scrollListener) {
        recyclerView.addOnScrollListener(scrollListener);
    }

    /*
        这个方法是创建自定义的滚动条
        在调用这个方法前，需要将对应的参数都设置好，不然会抛出异常
        注意：这个方法的调用要在activity的布局都绘制完后调用，这样控件的宽高计算才会准确
        建议在方法onWindowFocusChanged里调用
     */
    public void createScrollBar() {
        if (null == recyclerView.getLayoutManager()) {
            throw new ScrollbarException("RecyclerView的layoutManager布局管理器不能为空");
        }

        int orientation = getRLOrientation(recyclerView.getLayoutManager());
        if (-1 != orientation && !checkSite(scrollbarSite, orientation)) {
            throw new ScrollbarException("RecyclerView的layoutManager布局管理器的滚动方向和设置的滚动条的方向不一致");
        }

        if (-1 == orientation && SITE_NONE == scrollbarSite) {
            throw new ScrollbarException("请先为滚动条设置要显示的位置以及列表的滚动方向");
        }

        if (-1 == orientation && !checkSiteAndOrientation()) {
            throw new ScrollbarException("RecyclerView的自定义布局管理器的滚动方向和设置的滚动条的方向不一致");
        }

        if (lengthVariable == LENGTH_VARIABLE_FALSE && null == scrollbarView && 0 >= scrollbarBarLength) {
            throw new ScrollbarException("scrollbar的滚动view的长度是不变的，请为其设置一个固定的长度，通过：setScrollbarBarLength()");
        }

        createScrollbarView();
        showScrollbarView();

        extent = 0;
        range = 0;
        barSpace = 0;
        ratio = 0;
        if (lengthVariable == LENGTH_VARIABLE_TRUE) {
            scrollbarBarLength = -1;
        }
        recyclerView.removeOnScrollListener(scrollListener);
        addOnScrollListener(scrollListener);
        scrollListener.onScrolled(recyclerView, 0, 0);
    }

    /**
     * 创建scrollbar的View
     * 在整个view里，view的id并不都是能用到，但是我都set了，反正多了不怕，就怕少了来了个空指针异常
     */
    private void createScrollbarView() {

        if (null != scrollbarView) {
            scrollbarRootLayout = scrollbarView.createScrollbarView(getContext());
            if (null == scrollbarRootLayout) {
                throw new ScrollbarException("scrollbar自定义view不能为空");
            }
            scrollbarRootLayout.setId(R.id.scroll_bar_root_layout);
            bgView = scrollbarRootLayout.findViewById(R.id.scroll_bg_view);
            barView = scrollbarRootLayout.findViewById(R.id.scroll_bar_view);
            if (null == barView) {
                throw new ScrollbarException("自定义滚动条的滚动View不能为空");
            }
            if (null != bgView) {
                //获取scrollbar背景view的长度
                LayoutParams bgViewLP = (LayoutParams) bgView.getLayoutParams();
                if (mOrientation == RecyclerView.VERTICAL)
                    scrollbarBGLength = bgViewLP.height;
                else
                    scrollbarBGLength = bgViewLP.width;
            }
            computeDefaultLength();
        } else {
            computeDefaultLength();

            if (scrollbarBGLength > scrollbarLength) {
                scrollbarBGLength = scrollbarLength;
            }

            if (scrollbarBGWidth > scrollbarWidth) {
                scrollbarBGWidth = scrollbarWidth;
            }

            if (scrollbarBarWidth > scrollbarWidth) {
                scrollbarBarWidth = scrollbarWidth;
            }

            scrollbarRootLayout = new RelativeLayout(getContext());
            scrollbarRootLayout.setId(R.id.scroll_bar_root_layout);

            bgView = new View(getContext());
            bgView.setBackgroundResource(scrollbarBGShape);
            bgView.setId(R.id.scroll_bg_view);
            RelativeLayout.LayoutParams bgViewLP;
            if (mOrientation == RecyclerView.VERTICAL) {
                bgViewLP = new LayoutParams(scrollbarBGWidth, scrollbarBGLength);
            } else {
                bgViewLP = new LayoutParams(scrollbarBGLength, scrollbarBGWidth);
            }
            bgViewLP.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
            scrollbarRootLayout.addView(bgView, bgViewLP);

            barView = new View(getContext());
            barView.setBackgroundResource(scrollbarBarShape);
            barView.setId(R.id.scroll_bar_view);
            RelativeLayout.LayoutParams barViewLP;
            if (mOrientation == RecyclerView.VERTICAL) {
                barViewLP = new LayoutParams(scrollbarBarWidth, scrollbarBarLength);
                barViewLP.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
                barViewLP.addRule(RelativeLayout.ALIGN_TOP, bgView.getId());
            } else {
                barViewLP = new LayoutParams(scrollbarBarLength, scrollbarBarWidth);
                barViewLP.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
                barViewLP.addRule(RelativeLayout.ALIGN_LEFT, bgView.getId());
            }
            scrollbarRootLayout.addView(barView, barViewLP);
        }
    }

    /**
     * 将创建的scrollbar的view显示到页面上
     */
    private void showScrollbarView() {

        for (int i = getChildCount() - 1; i >= 0; i--) {
            View child = getChildAt(i);
            if (child != recyclerView) {
                removeView(child);
            }
        }

        RelativeLayout.LayoutParams scrollbarRootLayoutLP;
        if (mOrientation == RecyclerView.VERTICAL) {
            scrollbarRootLayoutLP = new LayoutParams(scrollbarWidth, scrollbarLength);
        } else {
            scrollbarRootLayoutLP = new LayoutParams(scrollbarLength, scrollbarWidth);
        }
        scrollbarRootLayoutLP.addRule(scrollbarSite, RelativeLayout.TRUE);

        int listSite = RelativeLayout.LEFT_OF;
        switch (scrollbarSite) {
            case RelativeLayout.ALIGN_PARENT_TOP:
                scrollbarRootLayoutLP.topMargin = spaceByParent;
                scrollbarRootLayoutLP.bottomMargin = spaceByList;
                listSite = RelativeLayout.BELOW;
                break;
            case RelativeLayout.ALIGN_PARENT_BOTTOM:
                scrollbarRootLayoutLP.bottomMargin = spaceByParent;
                scrollbarRootLayoutLP.topMargin = spaceByList;
                listSite = RelativeLayout.ABOVE;
                break;
            case RelativeLayout.ALIGN_PARENT_LEFT:
                scrollbarRootLayoutLP.leftMargin = spaceByParent;
                scrollbarRootLayoutLP.rightMargin = spaceByList;
                listSite = RelativeLayout.RIGHT_OF;
                break;
            case RelativeLayout.ALIGN_PARENT_RIGHT:
                scrollbarRootLayoutLP.rightMargin = spaceByParent;
                scrollbarRootLayoutLP.leftMargin = spaceByList;
                listSite = RelativeLayout.LEFT_OF;
                break;
        }

        RelativeLayout.LayoutParams rlLP = (LayoutParams) recyclerView.getLayoutParams();
        int[] rules = rlLP.getRules();
        if (null != rules)
            for (int i = 0; i < rules.length; i++) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
                    rlLP.removeRule(rules[i]);
            }
        if (0 == isFloatBar) {
            addView(scrollbarRootLayout, 0, scrollbarRootLayoutLP);
            rlLP.addRule(listSite, scrollbarRootLayout.getId());
        } else {
            addView(scrollbarRootLayout, scrollbarRootLayoutLP);
        }
        recyclerView.setLayoutParams(rlLP);
    }

    /**
     * 计算scrollbar的各个控件的默认长度
     * 如果各个长度的值大于0，表示已经设置了该值，则不需要计算
     */
    private void computeDefaultLength() {
        if (0 >= scrollbarLength
                || 0 >= scrollbarBGLength) {
            if (mOrientation == RecyclerView.VERTICAL) {
                int height = getHeight();
                if (0 >= height) {
                    height = getMeasuredHeight();
                }
                if (0 >= height) {
                    measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
                    height = getMeasuredHeight();
                }
                if (0 >= scrollbarLength)
                    scrollbarLength = height;
                if (0 >= scrollbarBGLength)
                    scrollbarBGLength = height;
            } else {
                int width = getWidth();
                if (0 >= width) {
                    width = getMeasuredWidth();
                }
                if (0 >= width) {
                    measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
                    width = getMeasuredWidth();
                }
                if (0 >= scrollbarLength)
                    scrollbarLength = width;
                if (0 >= scrollbarBGLength)
                    scrollbarBGLength = width;
            }
        }
    }

    /*
        如果RecyclerView的布局管理器LayoutManager是自定义的
        那么就没办法判断列表的滚动方向，需要其自行设置滚动的方向
        和滚动条的位置信息
        该方法用于判断此种情况下设置的滚动方向和位置信息是否正确
     */
    private boolean checkSiteAndOrientation() {
        if ((mOrientation == RecyclerView.VERTICAL && (scrollbarSite == RelativeLayout.ALIGN_PARENT_LEFT || scrollbarSite == RelativeLayout.ALIGN_PARENT_RIGHT))
                || (mOrientation == RecyclerView.HORIZONTAL && (scrollbarSite == RelativeLayout.ALIGN_PARENT_TOP || scrollbarSite == RelativeLayout.ALIGN_PARENT_BOTTOM))) {
            return true;
        }
        return false;
    }

    /*
        如果RecyclerView使用官方的布局管理器，则通过RecyclerView获取滚动方向
        与全局的滚动条的位置信息进行校验
     */
    private boolean checkSite(int scrollbarSite, int orientation) {
        if ((orientation == RecyclerView.VERTICAL && (scrollbarSite == RelativeLayout.ALIGN_PARENT_LEFT || scrollbarSite == RelativeLayout.ALIGN_PARENT_RIGHT))
                || (orientation == RecyclerView.HORIZONTAL && (scrollbarSite == RelativeLayout.ALIGN_PARENT_TOP || scrollbarSite == RelativeLayout.ALIGN_PARENT_BOTTOM))) {
            return true;
        }
        return false;
    }

    /*
        通过RecyclerView的布局管理器判断列表的滚动方向并设置滚动条的默认位置
        该方法只在创建RecyclerView时调用一次
     */
    private int checkDefaultSite(RecyclerView.LayoutManager layoutManager) {
        int orientation = getRLOrientation(layoutManager);
        if (orientation == RecyclerView.HORIZONTAL) {
            return RelativeLayout.ALIGN_PARENT_BOTTOM;
        } else if (orientation == RecyclerView.VERTICAL) {
            return RelativeLayout.ALIGN_PARENT_RIGHT;
        }
        return SITE_NONE;
    }

    /*
        通过RecyclerView的布局管理器获取列表的滚动方向
        如果布局管理器是自定义的，则返回-1，表示获取不到
     */
    private int getRLOrientation(RecyclerView.LayoutManager layoutManager) {
        if (layoutManager instanceof LinearLayoutManager) {             //因为GridLayoutManager是LinearLayoutManager的子类，所以也走这个if分支
            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
            return linearLayoutManager.getOrientation();
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            StaggeredGridLayoutManager staggeredGridLayoutManager = (StaggeredGridLayoutManager) layoutManager;
            return staggeredGridLayoutManager.getOrientation();
        }
        return -1;
    }

    RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {

        int maxOffset;

        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            if (0 == extent || 0 == range) {
                recyclerView.post(new Runnable() {
                    @Override
                    public void run() {
                        maxOffset = initScrollInfo(mOrientation);
                        Log.d("ScrollbarRecyclerView","偏移量是：" + maxOffset);
                    }
                });
            }
            int offset;
            if (mOrientation == RecyclerView.VERTICAL) {
                offset = recyclerView.computeVerticalScrollOffset();
            } else {
                offset = recyclerView.computeHorizontalScrollOffset();
            }
            Log.d("ScrollbarRecyclerView", "offset:" + offset);
            if (offset > maxOffset) {
                if (lengthVariable == LENGTH_VARIABLE_TRUE) {
                    maxOffset = initScrollInfo(mOrientation);
                    Log.d("ScrollbarRecyclerView","修正偏移量是：" + maxOffset);
                } else {
                    maxOffset = offset;
                    ratio = barSpace * 1.0f / maxOffset;
                }
            }
            if (mOrientation == RecyclerView.VERTICAL) {
                barView.setTranslationY(offset * ratio);
            } else {
                barView.setTranslationX(offset * ratio);
            }
        }
    };

    private int initScrollInfo(int mOrientation) {
        RelativeLayout.LayoutParams barViewLP = (LayoutParams) barView.getLayoutParams();
        if (mOrientation == RecyclerView.VERTICAL) {
            extent = recyclerView.computeVerticalScrollExtent();
            range = recyclerView.computeVerticalScrollRange();
        } else {
            extent = recyclerView.computeHorizontalScrollExtent();
            range = recyclerView.computeHorizontalScrollRange();
        }
        int barLength = (int) (extent * scrollbarBGLength * 1.0f / range);
        if (lengthVariable == LENGTH_VARIABLE_TRUE) {
            if (barLength <= scrollbarBarMinLength) {
                barLength = scrollbarBarMinLength;
            }
        } else {
            if (null == scrollbarView) {
                barLength = scrollbarBarLength;
            } else {
                if (mOrientation == RecyclerView.VERTICAL) {
                    barLength = barViewLP.height;
                } else {
                    barLength = barViewLP.width;
                }
            }
        }
        if (mOrientation == RecyclerView.VERTICAL) {
            barViewLP.height = barLength;
        } else {
            barViewLP.width = barLength;
        }
        Log.d("ScrollbarRecyclerView", "scrollbarBGLength:\t" + scrollbarBGLength + "\tbarLength:" + barLength);
        barSpace = scrollbarBGLength - barLength;
        scrollbarBarLength = barLength;
        barView.setLayoutParams(barViewLP);
        int maxoffset = range - extent;
        ratio = barSpace * 1.0f / maxoffset;
        return maxoffset;
    }

    /*
        该接口用于返回滚动条的自定义View
        该自定义View，有且只能有两个子控件，不能多也不能少
     */
    public static interface ScrollbarView {
        RelativeLayout createScrollbarView(Context mContext);
    }

    /*
        该抽象类是ScrollbarView的子类，为了方便用户更好更准确地使用ScrollbarView接口，按功能分写了三个方法
     */
    public static abstract class SimpleScrollbarView implements ScrollbarView {

        @Override
        public RelativeLayout createScrollbarView(Context mContext) {
            RelativeLayout rootLayout = createScrollbarRootView(mContext);

            View bgView = createScrollbarBGView(mContext, rootLayout);
            if (null == bgView) {
                throw new ScrollbarException("滚动条子控件bgView不能为空");
            }
            bgView.setId(R.id.scroll_bg_view);

            if (null == bgView.getParent()) {
                RelativeLayout.LayoutParams bgViewLP = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                rootLayout.addView(bgView, bgViewLP);
            } else if (bgView.getParent() != rootLayout) {
                throw new ScrollbarException("滚动条子控件bgView父容器不是RootView");
            }

            View barView = createScrollbarBarView(mContext, rootLayout);
            if (null == barView) {
                throw new ScrollbarException("滚动条子控件barView不能为空");
            }
            barView.setId(R.id.scroll_bar_view);

            if (null == barView.getParent()) {
                throw new ScrollbarException("滚动条子控件barView的父容器不能为空，因为父容器中有该子控件需要的宽高参数，没有则滚动条将不能正常显示");
            } else if (barView.getParent() != rootLayout) {
                throw new ScrollbarException("滚动条子控件barView父容器不是RootView");
            }

            return rootLayout;
        }

        /**
         * @param mContext 创建View对象的上下文，为了避免与scrollbar使用的是不同的上下文，建议使用方法中的Context
         * @return 该方法返回的是滚动条scrollbar的View，要求该容器是RelativeLayout
         */
        abstract RelativeLayout createScrollbarRootView(Context mContext);

        /**
         * @param mContext 创建滚动条bgView对象的上下文
         * @param parent   是bgView的父容器
         * @return 返回的是bgView的对象，该对象是滚动条的背景，样式自定义
         */
        abstract View createScrollbarBGView(Context mContext, RelativeLayout parent);

        /**
         * @param mContext 创建滚动条barView对象的上下文
         * @param parent   是barView的父容器
         * @return 返回的是barView的对象，该对象是滚动条的滚动部分，样式自定义
         */
        abstract View createScrollbarBarView(Context mContext, RelativeLayout parent);
    }
}
