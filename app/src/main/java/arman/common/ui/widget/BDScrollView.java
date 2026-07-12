package arman.common.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.OverScroller;
import android.widget.ScrollView;
import android.util.Log;

public class BDScrollView extends ScrollView {
    private float lastX, lastY;
    private OverScroller scroller;

    public BDScrollView(Context context) {
        super(context);
        init(context);
    }

    public BDScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public BDScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        scroller = new OverScroller(context);
        setHorizontalScrollBarEnabled(true); // Enable horizontal scrollbar
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (ev.getPointerCount() > 1) return false; // Avoid multi-touch issues

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastX = ev.getX();
                lastY = ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                float deltaX = Math.abs(ev.getX() - lastX);
                float deltaY = Math.abs(ev.getY() - lastY);

                if (deltaX > 10 || deltaY > 10) {
                    return true; // Detect scrolling in any direction
                }
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_MOVE:
                int dx = (int) (lastX - ev.getX());
                int dy = (int) (lastY - ev.getY());
                scrollBy(dx, dy);
                lastX = ev.getX();
                lastY = ev.getY();
                return true;
        }
        return super.onTouchEvent(ev);
    }

    @Override
    public void fling(int velocity) {
        int velocityX = (int) (velocity * 0.75); // Adjust horizontal speed
        int velocityY = velocity; // Keep vertical speed same

        // Detect fling direction
        if (velocityX > 0) Log.d("BDScrollView", "Fling Right");
        else if (velocityX < 0) Log.d("BDScrollView", "Fling Left");
        if (velocityY > 0) Log.d("BDScrollView", "Fling Down");
        else if (velocityY < 0) Log.d("BDScrollView", "Fling Up");

        // Start scrolling in both directions
        scroller.fling(
            getScrollX(), getScrollY(),
            velocityX, velocityY,
            0, getChildAt(0).getWidth() - getWidth(),
            0, getChildAt(0).getHeight() - getHeight()
        );

        postInvalidate();
    }

    @Override
    public void computeScroll() {
        if (scroller.computeScrollOffset()) {
            scrollTo(scroller.getCurrX(), scroller.getCurrY());
            postInvalidate();
        }
    }
}