package kg.prosoft.anticorruption;

import android.content.Context;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;

import kg.prosoft.anticorruption.service.ReportsTabAdapter;

public class TabActivity extends AppCompatActivity implements
        OnMapReadyCallback {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    public MapReportsFragment mapFrag;
    public ListReportsFragment listFrag;
    public NewsFragment newsFrag;
    MapView mMapView;
    OnMapReadyCallback mcback;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.

        context=getApplicationContext();

        mapFrag = new MapReportsFragment();
        listFrag = new ListReportsFragment();
        newsFrag = new NewsFragment();

        // Set up the ViewPager with the sections adapter.
        ViewPager mViewPager = (ViewPager) findViewById(R.id.container);

        ReportsTabAdapter adapter = new ReportsTabAdapter(getSupportFragmentManager());

        adapter.addFragment(newsFrag, getResources().getString(R.string.news));
        adapter.addFragment(listFrag, getResources().getString(R.string.reports));
        adapter.addFragment(mapFrag, getResources().getString(R.string.map));
        mViewPager.setAdapter(adapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        initMapSettings(savedInstanceState);

    }

    //map initialization is needed so that when you navigate to map tab it opens without lags
    public void initMapSettings(final Bundle savedInstanceState){
        mMapView = (MapView) findViewById(R.id.mapView);
        mcback=this;
        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        mMapView.onCreate(savedInstanceState);
                        mMapView.onResume(); // needed to get the map to display immediately
                        try {
                            MapsInitializer.initialize(context);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        mMapView.getMapAsync(mcback);
                    }
                },
                1000);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
    }
}