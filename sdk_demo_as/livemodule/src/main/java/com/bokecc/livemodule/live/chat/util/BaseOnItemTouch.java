package com.bokecc.livemodule.live.chat.util;

import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class BaseOnItemTouch extends RecyclerView.SimpleOnItemTouchListener {

    private static final String TAG = BaseOnItemTouch.class.getSimpleName();

    private GestureDetectorCompat mGestureDetectorCompat;
    private ITouchListener mITouchListener;

    private RecyclerView mRecyclerView;
    private View mItemView = null, mPreView = null;

    public BaseOnItemTouch(RecyclerView recyclerView, ITouchListener ITouchListener) {
        mRecyclerView = recyclerView;
        mITouchListener = ITouchListener;
        mGestureDetectorCompat = new GestureDetectorCompat(mRecyclerView.getContext(),
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public void onLongPress(MotionEvent e) {
                        View itemView = mRecyclerView.findChildViewUnder(e.getX(),
                                e.getY());
                        if (itemView != null && mITouchListener != null) {
                            mITouchListener.onLongPress(mRecyclerView.getChildViewHolder(itemView));
                        }
                    }

                    @Override
                    public boolean onSingleTapUp(MotionEvent e) {
                        View itemView = mRecyclerView.findChildViewUnder(e.getX(),
                                e.getY());
                        if (itemView != null && mITouchListener != null) {
                            mITouchListener.onClick(mRecyclerView.getChildViewHolder(itemView));
                        }
                        mPreView = itemView;
                        return true;
                    }

                });
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        if (e.getAction() == MotionEvent.ACTION_UP) {
            if (mItemView != null && mITouchListener != null) {
                mITouchListener.onTouchUp(
                        mRecyclerView.getChildViewHolder(mItemView));
                mItemView = null;
            }
        }
        if (e.getAction() == MotionEvent.ACTION_DOWN) {
            mItemView = mRecyclerView.findChildViewUnder(e.getX(),
                    e.getY());
            if (mItemView != null && mITouchListener != null) {
                mITouchListener.onTouchDown(
                        mRecyclerView.getChildViewHolder(mItemView));
            }
        }
        mGestureDetectorCompat.onTouchEvent(e);
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        mGestureDetectorCompat.onTouchEvent(e);
    }
}

