package kg.prosoft.anticorruption.service;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.github.chrisbanes.photoview.PhotoView;

import java.util.ArrayList;

import kg.prosoft.anticorruption.R;

/**
 * Created by ProsoftPC on 10/9/2017.
 */

public class SliderPagerAdapter extends PagerAdapter {
    private LayoutInflater layoutInflater;
    Activity activity;
    ArrayList<String> image_arraylist;

    public SliderPagerAdapter(Activity activity, ArrayList<String> image_arraylist) {
        this.activity = activity;
        this.image_arraylist = image_arraylist;
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        layoutInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view;
        view = layoutInflater.inflate(R.layout.item_gallery, container, false);
        PhotoView im_slider = (PhotoView) view.findViewById(R.id.photo_view);
        GlideApp.with(activity.getApplicationContext())
                .load(image_arraylist.get(position))
                //.placeholder(R.drawable.placeholder)
                //.dontAnimate()
                .into(im_slider);
        container.addView(view);

        return view;
    }

    @Override
    public int getCount() {
        int size=0;
        if(image_arraylist!=null){size=image_arraylist.size();}
        return size;
    }

    @Override
    public boolean isViewFromObject(View view, Object obj) {
        return view == obj;
    }


    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        View view = (View) object;
        container.removeView(view);
    }
}

