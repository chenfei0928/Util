package io.github.chenfei0928.view.viewHelp;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

/**
 * RecyclerView的点击监听器
 *
 * @author MrFeng
 * @date 2016/4/7
 * @see <a href="http://stackoverflow.com/questions/24471109/recyclerview-onclick">原博客</a>
 */
public class RecyclerItemClickListener implements RecyclerView.OnItemTouchListener {
    private final OnItemClickListener mClick;
    private final OnItemLongClickListener mLongClick;
    private final GestureDetector mGestureDetector;

    public RecyclerItemClickListener(Context context, RecyclerView view,
                                     OnItemClickListener listener, OnItemLongClickListener longClick) {
        mClick = listener;
        mLongClick = longClick;
        mGestureDetector = new GestureDetector(context.getApplicationContext(),
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onSingleTapUp(MotionEvent e) {
                        return true;
                    }

                    @Override
                    public void onLongPress(MotionEvent e) {
                        View childView = view.findChildViewUnder(e.getX(), e.getY());

                        if (childView != null && mLongClick != null) {
                            mLongClick.onItemLongClick(childView, view.getChildAdapterPosition(childView));
                        }
                    }
                });
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView view, MotionEvent e) {
        View childView = view.findChildViewUnder(e.getX(), e.getY());
        if (childView != null && mClick != null && mGestureDetector.onTouchEvent(e)) {
            mClick.onItemClick(childView, view.getChildAdapterPosition(childView));
            return true;
        }
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView view, MotionEvent motionEvent) {
    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(View view, int position);
    }
}