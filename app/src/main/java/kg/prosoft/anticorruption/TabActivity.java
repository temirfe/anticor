package kg.prosoft.anticorruption;

import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;

import kg.prosoft.anticorruption.service.ReportsTabAdapter;

public class TabActivity extends AppCompatActivity {

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.

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

    }
}