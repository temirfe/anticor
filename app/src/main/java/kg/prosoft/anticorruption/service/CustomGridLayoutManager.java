package kg.prosoft.anticorruption.service;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;

/**
 * Created by Temirbek on 8/26/2017.
 */

public class CustomGridLayoutManager extends LinearLayoutManager {
    private boolean isScrollEnabled = true;

    public CustomGridLayoutManager(Context context) {
        super(context);
    }
    public CustomGridLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context);
        super.setOrientation(orientation);
        super.setReverseLayout(reverseLayout);
        super.setAutoMeasureEnabled(true);
    }

    public void setScrollEnabled(boolean flag) {
        this.isScrollEnabled = flag;
    }

    /*@Override
    public boolean canScrollVertically() {
        return isScrollEnabled && super.canScrollVertically();
    }*/

    @Override
    public boolean canScrollHorizontally() {
        return isScrollEnabled && super.canScrollHorizontally();
    }
}
