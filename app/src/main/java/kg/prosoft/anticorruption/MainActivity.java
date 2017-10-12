package kg.prosoft.anticorruption;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.MapsInitializer;

import java.util.ArrayList;

import kg.prosoft.anticorruption.service.Endpoints;
import kg.prosoft.anticorruption.service.ReportsTabAdapter;

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener, ListReportsFragment.OnCompleteListener {

    private String TAG = MainActivity.class.getSimpleName();
    LinearLayout ll_logo;
    RelativeLayout rl_login, rl_account;
    TextView tv_name, tv_logo;
    Button btn_login;
    Button btn_register;
    public ListReportsFragment listFrag;
    public MapReportsFragment mapFrag;
    String filter_query;
    int filter_authority_id,filter_sector_id, filter_type_id, filter_city_id;
    static int FILTER_FLAG=123;
    Intent gotIntent;
    Menu myMenu;
    boolean showFilterBadge=false, showFilteredReport=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
                Intent intent = new Intent(MainActivity.this, AddReportActivity.class);
                startActivity(intent);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View header = navigationView.getHeaderView(0);

        rl_login = (RelativeLayout) header.findViewById(R.id.id_rl_login);
        rl_account = (RelativeLayout) header.findViewById(R.id.id_rl_account);
        ll_logo = (LinearLayout) header.findViewById(R.id.id_ll_logo);

        btn_login = (Button) header.findViewById(R.id.id_btn_login);
        btn_register = (Button) header.findViewById(R.id.id_btn_register);
        tv_name = (TextView) header.findViewById(R.id.id_tv_name);
        tv_logo = (TextView) header.findViewById(R.id.id_tv_logo);

        Menu nav_Menu = navigationView.getMenu();

        if(session.isLoggedIn()){
            Log.e(TAG,"is logged in "+session.getUserName());
            tv_name.setText(session.getUserName());
            rl_account.setOnClickListener(onClickName);
            rl_login.setVisibility(View.GONE);
        }
        else{
            rl_login.setVisibility(View.VISIBLE);
            btn_login.setOnClickListener(onClickLogin);
            btn_register.setOnClickListener(onClickRegister);
            rl_account.setVisibility(View.GONE);
            nav_Menu.findItem(R.id.nav_logout).setVisible(false);
        }

        gotIntent=getIntent();
        showFilteredReport=gotIntent.getBooleanExtra("showReport",false);
        if(showFilteredReport){
            showFilterBadge=true;
            showReportFrag();
            //Log.e("MainAct","showReport fired");
        }
    }

    public void onComplete() {
        // After the fragment completes, it calls this callback.
        // setup the rest of your layout now
        //Log.e("MainAct","onComplete fired "+showFilteredReport);
        if(showFilteredReport){
            applyFilter(gotIntent);
        }
    }

    View.OnClickListener onClickName = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(MainActivity.this, AccountActivity.class);
            startActivity(intent);
        }
    };

    View.OnClickListener onClickLogin = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent loginIntent = new Intent(MainActivity.this,LoginActivity.class);
            startActivity(loginIntent);
        }
    };

    View.OnClickListener onClickRegister = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent regIntent = new Intent(MainActivity.this,RegisterActivity.class);
            startActivity(regIntent);
        }
    };

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            showReportFrag();
        } else if (id == R.id.nav_gallery) {
            Intent intent= new Intent(MainActivity.this, NewsListActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_slideshow) {
            Intent intent= new Intent(MainActivity.this, TabActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_manage) {
            Intent intent= new Intent(MainActivity.this, AuthorityListActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        } else if (id == R.id.nav_logout) {
            session.logoutUser();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void showReportFrag(){
        if(myMenu!=null){
            myMenu.findItem(R.id.action_filter).setVisible(true);
            myMenu.findItem(R.id.action_search).setVisible(false);
        }
        // Set up the ViewPager with the sections adapter.
        ViewPager mViewPager = (ViewPager) findViewById(R.id.id_vp_main);
        mViewPager.setVisibility(View.VISIBLE);

        listFrag = new ListReportsFragment();
        mapFrag = new MapReportsFragment();
        if(!showFilteredReport){
            Bundle bundle = new Bundle();
            bundle.putBoolean("populate",true);
            listFrag.setArguments(bundle);
            mapFrag.setArguments(bundle);
        }
        //listFrag.setArguments(gotIntent.getExtras());
        //mapFrag.setArguments(gotIntent.getExtras());
        ReportsTabAdapter adapter = new ReportsTabAdapter(getSupportFragmentManager());

        adapter.addFragment(listFrag, getResources().getString(R.string.reports));
        adapter.addFragment(mapFrag, getResources().getString(R.string.map));
        mViewPager.setAdapter(adapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
        tabLayout.setVisibility(View.VISIBLE);

        /*if (findViewById(R.id.fragment_container) != null) {
            // Add the fragment to the 'fragment_container' FrameLayout
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, listFrag).commit();
        }*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        myMenu=menu;

        MenuItem item = menu.findItem(R.id.action_filter_badge);
        MenuItemCompat.setActionView(item, R.layout.filter_menu_badge);
        RelativeLayout rl_filter = (RelativeLayout) MenuItemCompat.getActionView(item);

        TextView filterBadge = (TextView) rl_filter.findViewById(R.id.id_tv_filter_menu);
        filterBadge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFilter();
            }
        });
        if(showFilterBadge){
            filterMenuToggle();
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Log.i("ActRes","came here");
        if (data == null) { Log.e("MainAct"," onActRes data is null");  return;}
        if(requestCode==FILTER_FLAG){
            applyFilter(data);
        }
    }

    public void applyFilter(Intent data){
        //Log.e("MainAct"," onActRes filterflag fired");
        if(myMenu!=null){
            boolean empty=data.getBooleanExtra("empty",true);
            boolean clean=true, badged=false;
            if(!empty){
                clean=false; badged=true;
            }
            myMenu.findItem(R.id.action_filter).setVisible(clean);
            myMenu.findItem(R.id.action_filter_badge).setVisible(badged);
        }

        filter_query=data.getStringExtra("query");
        filter_sector_id=data.getIntExtra("sector_id",0);
        filter_authority_id=data.getIntExtra("authority_id",0);
        filter_type_id=data.getIntExtra("type_id",0);
        filter_city_id=data.getIntExtra("city_id",0);

        Uri.Builder builder = new Uri.Builder();
        builder.scheme(Endpoints.SCHEME).authority(Endpoints.AUTHORITY).appendPath(Endpoints.API).appendPath("reports");
        if(filter_query!=null && !filter_query.isEmpty()){
            builder.appendQueryParameter("text", filter_query);
        }
        if(filter_sector_id!=0){
            builder.appendQueryParameter("category_id", Integer.toString(filter_sector_id));
        }
        if(filter_authority_id!=0){
            builder.appendQueryParameter("authority_id", Integer.toString(filter_authority_id));
        }
        if(filter_type_id!=0){
            builder.appendQueryParameter("type_id", Integer.toString(filter_type_id));
        }
        if(filter_city_id!=0){
            builder.appendQueryParameter("city_id", Integer.toString(filter_city_id));
        }
        if(listFrag!=null){listFrag.populateList(1,builder,true,false);}
        if(mapFrag!=null){
            //Log.e("MainAct"," populateMap fired");
            mapFrag.populateMap(builder);}
    }

    public void filterMenuToggle(){
        //myMenu.findItem(R.id.action_filter).setVisible(clean);
        myMenu.findItem(R.id.action_filter_badge).setVisible(true);
        myMenu.findItem(R.id.action_search).setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_filter) {
            openFilter();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void openFilter(){
        Intent filter_intent=new Intent(context, FilterActivity.class);
        filter_intent.putExtra("query",filter_query);
        filter_intent.putExtra("sector_id",filter_sector_id);
        filter_intent.putExtra("authority_id",filter_authority_id);
        filter_intent.putExtra("type_id",filter_type_id);
        filter_intent.putExtra("city_id",filter_city_id);
        startActivityForResult(filter_intent, FILTER_FLAG);
    }
}
