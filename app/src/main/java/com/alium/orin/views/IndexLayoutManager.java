package com.alium.orin.views;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.alium.orin.R;
import com.alium.orin.util.PhonographColorUtil;
import com.kabouzeid.appthemehelper.ThemeStore;
import com.kabouzeid.appthemehelper.util.ColorUtil;
import com.kabouzeid.appthemehelper.util.MaterialValueHelper;
import com.kabouzeid.appthemehelper.util.ToolbarContentTintHelper;

import static com.alium.orin.util.PhonographColorUtil.CIRCLE_TRANSPARENCY;


/**
 * Created by edgar on 5/31/15.
 */
public class IndexLayoutManager extends FrameLayout {

    private TextView stickyIndex;
    private RecyclerView indexList;
    private Activity activity;
    private RecyclerView.OnScrollListener mScrollListener;

    public IndexLayoutManager(Context context) {
        super(context);
    }

    public IndexLayoutManager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public IndexLayoutManager(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public IndexLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        LayoutInflater.from(getContext()).inflate(R.layout.internal_index_layout, this, true);
        mScrollListener = getListener();
        init();
    }

    public void attach(RecyclerView pRecyclerView, Activity activity) {
        indexList = pRecyclerView;
        this.activity = activity;
        setMargins(indexList, 0, 24, 0, 0);
        pRecyclerView.addOnScrollListener(mScrollListener);
        update(pRecyclerView, 0, 0);
    }

    public static void setMargins(View v, int l, int t, int r, int b) {
        if (v.getLayoutParams() instanceof MarginLayoutParams) {
            MarginLayoutParams p = (MarginLayoutParams) v.getLayoutParams();
            p.setMargins(l, t, r, b);
            v.requestLayout();
        }
    }


    @NonNull
    private RecyclerView.OnScrollListener getListener() {
        return new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                update(recyclerView, dx, dy);
            }
        };
    }

    public void dettach(RecyclerView pRecyclerView) {
        pRecyclerView.removeOnScrollListener(mScrollListener);
    }

    public void refresh() {
        update(indexList, 0, 0);
    }

    private void init() {
        stickyIndex = (TextView) findViewById(R.id.section_title);
    }

    private Boolean isHeader(TextView prev, TextView act) {
        if (isSameChar(prev.getText().charAt(0), act.getText().charAt(0))) {
            return Boolean.FALSE;
        } else {
            return Boolean.TRUE;
        }
    }

    private Boolean isSameChar(char prev, char curr) {
        if (Character.toLowerCase(prev) == Character.toLowerCase(curr)) {
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }

    private void updatePosBasedOnReferenceList(RecyclerView referenceRv) {
        View firstVisibleView = referenceRv.getChildAt(0);
        int actual = referenceRv.getChildPosition(firstVisibleView);
        ((LinearLayoutManager) indexList.getLayoutManager()).scrollToPositionWithOffset(actual, firstVisibleView.getTop() + 0);
    }

    public void update(RecyclerView referenceList, float dx, float dy) {
        if (indexList != null && indexList.getChildCount() > 2) {
            show();
            updatePosBasedOnReferenceList(referenceList);

            View firstVisibleView = indexList.getChildAt(0);
            View secondVisibleView = indexList.getChildAt(1);


            TextView firstRowIndex = (TextView) firstVisibleView.findViewById(R.id.section_title);
            TextView secondRowIndex = (TextView) secondVisibleView.findViewById(R.id.section_title);

            int visibleRange = indexList.getChildCount();
            int actual = indexList.getChildPosition(firstVisibleView);
            int next = actual + 1;
            int last = actual + visibleRange;

            Drawable drawable = activity.getResources().getDrawable(R.drawable.circle_white_bg);
            int backGroundColor = ToolbarContentTintHelper.toolbarTitleColor(activity, ThemeStore.primaryColor(activity));
            stickyIndex.setTextColor(MaterialValueHelper.getPrimaryTextColor(activity, ColorUtil.isColorLight(backGroundColor)));
            drawable.setColorFilter(backGroundColor, PorterDuff.Mode.SRC_IN);
            stickyIndex.setBackground(drawable);

            // RESET STICKY LETTER INDEX
            stickyIndex.setText(String.valueOf(getIndexContext(firstRowIndex)).toUpperCase());
            stickyIndex.setVisibility(TextView.VISIBLE);
            ViewCompat.setAlpha(firstRowIndex, 1);

            if (dy > 0) {
                // USER SCROLLING DOWN THE RecyclerView
                if (next <= last) {
                    if (isHeader(firstRowIndex, secondRowIndex)) {
                        stickyIndex.setVisibility(TextView.INVISIBLE);
                        firstRowIndex.setVisibility(TextView.VISIBLE);
                        ViewCompat.setAlpha(firstRowIndex, (1 - (Math.abs(ViewCompat.getY(firstVisibleView)) / firstRowIndex.getHeight())));
                        secondRowIndex.setVisibility(TextView.VISIBLE);
                    } else {
                        firstRowIndex.setVisibility(TextView.INVISIBLE);
                        stickyIndex.setVisibility(TextView.VISIBLE);
                    }
                }
            } else if (dy < 0) {
                // USER IS SCROLLING UP THE RecyclerVIew
                if (next <= last) {
                    // RESET FIRST ROW STATE
                    firstRowIndex.setVisibility(TextView.INVISIBLE);
                    if ((isHeader(firstRowIndex, secondRowIndex) || (getIndexContext(firstRowIndex) != getIndexContext(secondRowIndex))) && isHeader(firstRowIndex, secondRowIndex)) {
                        stickyIndex.setVisibility(TextView.INVISIBLE);
                        firstRowIndex.setVisibility(TextView.VISIBLE);
                        ViewCompat.setAlpha(firstRowIndex, 1 - (Math.abs(ViewCompat.getY(firstVisibleView) / firstRowIndex.getHeight())));
                        secondRowIndex.setVisibility(TextView.VISIBLE);
                    } else {
                        secondRowIndex.setVisibility(TextView.INVISIBLE);
                    }
                }
            }

            if (stickyIndex.getVisibility() == TextView.VISIBLE) {
                firstRowIndex.setVisibility(TextView.INVISIBLE);
            }
        } else {
            hide();
        }
    }

    private Bitmap bitMapFromText(String text, float textSize, int textColor) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextSize(textSize);
        paint.setColor(textColor);
        paint.setTextAlign(Paint.Align.LEFT);
        float baseLine = -paint.ascent();
        int width = (int) (paint.measureText(text) + 0.0f);
        int height = (int) (baseLine + paint.measureText(text) + 0.0f);

        Bitmap image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(image);
        canvas.drawText(text, 0, baseLine, paint);
        return image;
    }


    private char getIndexContext(TextView index) {
        if (Character.isDigit(index.getText().charAt(0))) {
            return '#';
        } else {
            return index.getText().charAt(0);
        }
    }

    public void show() {
        stickyIndex.setVisibility(View.VISIBLE);
    }

    public void hide() {
        stickyIndex.setVisibility(View.GONE);
    }
}