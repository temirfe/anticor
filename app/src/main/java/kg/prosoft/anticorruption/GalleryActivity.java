package kg.prosoft.anticorruption;

import android.content.Intent;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;

import kg.prosoft.anticorruption.service.SliderPagerAdapter;

public class GalleryActivity extends AppCompatActivity {

    private ViewPager vp_slider;
    private LinearLayout ll_dots;
    SliderPagerAdapter sliderPagerAdapter;
    ArrayList<String> slider_image_list;
    private ImageView[] dots;
    int page_position = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        getSupportActionBar().hide();
        Intent intent=getIntent();
        page_position=intent.getIntExtra("pos",0);
        slider_image_list=intent.getStringArrayListExtra("imglist");

        // method for initialisation
        init();

        // method for adding indicators
        addBottomDots(page_position);
    }

    private void init() {

        vp_slider = (ViewPager) findViewById(R.id.vp_slider);
        ll_dots = (LinearLayout) findViewById(R.id.ll_dots);

        sliderPagerAdapter = new SliderPagerAdapter(GalleryActivity.this, slider_image_list);
        vp_slider.setAdapter(sliderPagerAdapter);

        vp_slider.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                addBottomDots(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });
        if(page_position!=0)
            vp_slider.setCurrentItem(page_position);
    }

    public void onClickClose(View v){
        finish();
    }

    private void addBottomDots(int currentPage) {
        int size=slider_image_list.size();
        if(size>1){
            //dots = new TextView[size];
            dots = new ImageView[size];

            ll_dots.removeAllViews();
            for (int i = 0; i < dots.length; i++) {
                dots[i] = new ImageView(this);
                dots[i].setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.gray_dot,null));

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

                params.setMargins(4, 0, 4, 0);
                ll_dots.addView(dots[i],params);
            }

            if (dots.length > 0)
                dots[currentPage].setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.red_dot,null));
        }
    }
}
