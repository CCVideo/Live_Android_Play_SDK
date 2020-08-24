package com.bokecc.livemodule.live.qa.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.bokecc.livemodule.R;

/**
 * 问答列表分割线
 */
public class QaListDividerItemDecoration extends RecyclerView.ItemDecoration {

    private final static int dividerHeight = 10;
    private Paint dividerPaint;

    public QaListDividerItemDecoration(Context context) {
        dividerPaint = new Paint();
        dividerPaint.setColor(context.getResources().getColor(R.color.qa_list_divider_color));
    }


    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                               @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        outRect.bottom = dividerHeight;
    }

    @Override
    public void onDraw(@NonNull Canvas c, RecyclerView parent, @NonNull RecyclerView.State state) {
        int childCount = parent.getChildCount();
        int left = parent.getPaddingLeft();
        int right = parent.getWidth() - parent.getPaddingRight();

        for (int i = 0; i < childCount - 1; i++) {
            View view = parent.getChildAt(i);
            float top = view.getBottom();
            float bottom = view.getBottom() + dividerHeight;
            c.drawRect(left, top, right, bottom, dividerPaint);
        }
    }
}

