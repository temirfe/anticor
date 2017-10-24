package kg.prosoft.anticorruption;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.support.v7.widget.SearchView;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.google.android.gms.maps.MapsInitializer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import kg.prosoft.anticorruption.service.Endpoints;
import kg.prosoft.anticorruption.service.LocaleHelper;
import kg.prosoft.anticorruption.service.MyDbHandler;
import kg.prosoft.anticorruption.service.MyVolley;
import kg.prosoft.anticorruption.service.ReportsTabAdapter;
import kg.prosoft.anticorruption.service.Vocabulary;

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener, ListReportsFragment.OnCompleteListener{

    private String TAG = MainActivity.class.getSimpleName();
    LinearLayout ll_logo;
    //RelativeLayout rl_login, rl_account;
    TextView tv_name, tv_logo, tv_lang, tv_login;
    //Button btn_login;
    //Button btn_register;
    public ListReportsFragment listFrag;
    public MapReportsFragment mapFrag;
    public DocMenuFragment docMenuFrag;
    String filter_query;
    int filter_authority_id,filter_sector_id, filter_type_id, filter_city_id;
    static int FILTER_FLAG=123;
    Intent gotIntent;
    Menu myMenu;
    boolean showFilterBadge=false, showFilteredReport=false;
    Bundle savedIS;
    TabLayout tabLayout;
    ViewPager mViewPager;
    FrameLayout fragCont;
    NewsFragment newsFragment;
    List<String> spinOptions;
    ArrayAdapter<String> spinnerDataAdapter;
    Spinner spinner;
    private HashMap<String, Integer> idMap;
    FloatingActionButton fab;
    int news_ctg_id=0;
    int ACTIVE_FRAME=0, NEWS_FRAME=1, RESEARCH_FRAME=2, MAPMENU_FRAME=3, MAIN_FRAME=4, REPORT_FRAME=5;
    int PREV_FRAME=0;
    FragmentManager fm;
    NavigationView navigationView;
    int visible_tab=1;
    static int SETTINGS_FLAG=31;
    String lang="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        savedIS=savedInstanceState;

        /*if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            query = intent.getStringExtra(SearchManager.QUERY);
            Log.e(TAG, "query: " + query);
        }*/

        fab = (FloatingActionButton) findViewById(R.id.fab);
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

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        fm=getSupportFragmentManager();

        View header = navigationView.getHeaderView(0);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        mViewPager = (ViewPager) findViewById(R.id.id_vp_main);
        fragCont=(FrameLayout)findViewById(R.id.fragment_container);
        spinner = (Spinner) findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(onSpinSelect);
        idMap= new HashMap<>();
        setNewsBarSpinner();

        //rl_login = (RelativeLayout) header.findViewById(R.id.id_rl_login);
        //rl_account = (RelativeLayout) header.findViewById(R.id.id_rl_account);
        ll_logo = (LinearLayout) header.findViewById(R.id.id_ll_logo);

        //btn_login = (Button) header.findViewById(R.id.id_btn_login);
        //btn_register = (Button) header.findViewById(R.id.id_btn_register);
        tv_login = (TextView) header.findViewById(R.id.id_tv_login);
        tv_name = (TextView) header.findViewById(R.id.id_tv_name);
        tv_logo = (TextView) header.findViewById(R.id.id_tv_logo);
        tv_lang = (TextView) header.findViewById(R.id.id_tv_lang);
        lang=LocaleHelper.getLanguage(context);
        tv_lang.setText(lang.toUpperCase());
        tv_lang.setOnClickListener(onClickSettings);

        Menu nav_Menu = navigationView.getMenu();
        Log.e(TAG,"sessLang "+session.getLanguage());

        if(session.isLoggedIn()){
            Log.e(TAG,"is logged in "+session.getUserName());
            tv_name.setText(session.getUserName());
            tv_name.setOnClickListener(onClickName);
            tv_login.setVisibility(View.GONE);
        }
        else{
            tv_login.setVisibility(View.VISIBLE);
            tv_login.setOnClickListener(onClickLogin);
            //btn_register.setOnClickListener(onClickRegister);
            tv_name.setVisibility(View.GONE);
            nav_Menu.findItem(R.id.nav_logout).setVisible(false);
        }

        gotIntent=getIntent();
        showFilteredReport=gotIntent.getBooleanExtra("showReport",false);
        if(showFilteredReport){
            showFilterBadge=true;
            showReportFrag(1);
            //Log.e("MainAct","showReport fired");
        }
    }

    public void setNewsBarSpinner(){
        // Spinner Drop down elements
        spinOptions = new ArrayList<>();
        spinOptions.add(getResources().getString(R.string.all_news));
        // Creating adapter for spinner
        spinnerDataAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, spinOptions);
        spinnerDataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerDataAdapter);
    }

    public void onComplete() {
        // After the fragment completes, it calls this callback.
        // setup the rest of your layout now
        //Log.e("MainAct","onComplete fired "+showFilteredReport);
        if(showFilteredReport){
            applyFilter(gotIntent);
        }
    }

    AdapterView.OnItemSelectedListener onSpinSelect = new AdapterView.OnItemSelectedListener(){
        public void onItemSelected(AdapterView<?> parent, View view,
                                   int pos, long id) {
            String selected=parent.getItemAtPosition(pos).toString();
            int selected_id=0; boolean go=false;
            if(pos!=0){selected_id=idMap.get(selected);}
            if(pos==1 && selected_id==0){go=true;spinOptions.remove(0);} //case when search queary is added
            if(selected_id!=news_ctg_id){go=true;}
            if(go)
            {
                news_ctg_id=selected_id;
                Uri.Builder builder = new Uri.Builder();
                builder.scheme(Endpoints.SCHEME).authority(Endpoints.AUTHORITY).appendPath(Endpoints.API).appendPath("news");
                if(news_ctg_id!=0){
                    builder.appendQueryParameter("category_id", ""+news_ctg_id);
                }
                newsFragment.populateList(1,builder,true,false);
            }
        }

        public void onNothingSelected(AdapterView<?> parent) {}
    };

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
    View.OnClickListener onClickSettings = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            clickSettings();
        }
    };

    public void clickSettings(){
        Intent intent=new Intent(context, SettingsActivity.class);
        startActivityForResult(intent,SETTINGS_FLAG);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_main) {
            showMainFrag();
        }
        else if (id == R.id.nav_news) {
            showNewsFrag();
        }
        else if (id == R.id.nav_research ){
            showResearchFrag();
        }
        else if (id == R.id.nav_map ){
            showMapMenuFrag();
        }
        else if (id == R.id.nav_education ){
            Intent intent= new Intent(MainActivity.this, EducationListActivity.class);
            startActivity(intent);
        }

        else if (id == R.id.nav_analytics ){
            Intent intent= new Intent(MainActivity.this, AnalyticsListActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.nav_anticor_politics ){
        }
        else if (id == R.id.nav_about ){
        }
        else if (id == R.id.nav_contacts ){
        } else if (id == R.id.nav_logout) {
            session.logoutUser();
        } else if (id == R.id.nav_settings) {
            clickSettings();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void showMainFrag(){
        PREV_FRAME=0;
        ACTIVE_FRAME=MAIN_FRAME;
        Log.e(TAG, "showMainFrag");
        hideFragContainer();
        navigationView.setCheckedItem(R.id.nav_main);
    }

    public void showNewsFrag(){
        if(PREV_FRAME==NEWS_FRAME){PREV_FRAME=0;}
        else{PREV_FRAME=ACTIVE_FRAME;}
        ACTIVE_FRAME=NEWS_FRAME;
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        if(idMap.isEmpty()){
            new VocabularyTask().execute();
        }
        hideReportFrag();
        fragCont.setVisibility(View.VISIBLE);
        spinner.setVisibility(View.VISIBLE);
        fab.setVisibility(View.GONE);
        /*if (savedIS != null) {
            Log.e(TAG, "savedIS returns");
            return;
        }*/

        if(fm.findFragmentByTag("news") != null) {
            fm.beginTransaction().show(fm.findFragmentByTag("news")).commit();
        } else {
            newsFragment = new NewsFragment();
            fm.beginTransaction().add(R.id.fragment_container, newsFragment, "news").addToBackStack(null).commit();
        }
        if(fm.findFragmentByTag("research") != null){
            fm.beginTransaction().hide(fm.findFragmentByTag("research")).commit();
        }
        if(fm.findFragmentByTag("mapmenu") != null){
            fm.beginTransaction().hide(fm.findFragmentByTag("mapmenu")).commit();
        }
    }

    public void hideFragContainer(){
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        fragCont.setVisibility(View.GONE);
        spinner.setVisibility(View.GONE);
    }

    public void showReportFrag(int showFirst){
        visible_tab=showFirst;
        if(PREV_FRAME==REPORT_FRAME){PREV_FRAME=0;}
        else{PREV_FRAME=ACTIVE_FRAME;}
        ACTIVE_FRAME=REPORT_FRAME;
        hideFragContainer();
        if(myMenu!=null){
            myMenu.findItem(R.id.action_filter).setVisible(true);
            myMenu.findItem(R.id.action_search).setVisible(false);
        }
        // Set up the ViewPager with the sections adapter.
        mViewPager.setVisibility(View.VISIBLE);
        tabLayout.setVisibility(View.VISIBLE);
        fab.setVisibility(View.VISIBLE);

        if(listFrag==null || mapFrag==null){
            Log.e(TAG, "Report frags are creating");
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
            ReportsTabAdapter adapter = new ReportsTabAdapter(fm);

            if(showFirst==1){
                adapter.addFragment(listFrag, getResources().getString(R.string.reports));
                adapter.addFragment(mapFrag, getResources().getString(R.string.map));
            }
            else{
                adapter.addFragment(mapFrag, getResources().getString(R.string.map));
                adapter.addFragment(listFrag, getResources().getString(R.string.reports));
            }
            mViewPager.setAdapter(adapter);
            tabLayout.setupWithViewPager(mViewPager);
        }
    }
    public void hideReportFrag(){
        mViewPager.setVisibility(View.GONE);
        tabLayout.setVisibility(View.GONE);
        if(myMenu!=null){
            myMenu.findItem(R.id.action_filter).setVisible(false);
            myMenu.findItem(R.id.action_filter_badge).setVisible(false);
            myMenu.findItem(R.id.action_search).setVisible(true);
        }
    }

    public void showResearchFrag(){
        if(PREV_FRAME==RESEARCH_FRAME){PREV_FRAME=0;}
        else{PREV_FRAME=ACTIVE_FRAME;}
        ACTIVE_FRAME=RESEARCH_FRAME;
        hideFragContainer();
        hideReportFrag();
        fragCont.setVisibility(View.VISIBLE);
        fab.setVisibility(View.GONE);
        /*if (savedIS != null) {
            Log.e(TAG, "savedIS returns");
            return;
        }*/

        if(fm.findFragmentByTag("research") != null) {
            //if the fragment exists, show it.
            fm.beginTransaction().show(getSupportFragmentManager().findFragmentByTag("research")).commit();
        } else {
            //if the fragment does not exist, add it to fragment manager.
            fm.beginTransaction().add(R.id.fragment_container, new DocMenuFragment(), "research").addToBackStack(null).commit();
        }
        if(fm.findFragmentByTag("news") != null){
            //if the other fragment is visible, hide it.
            fm.beginTransaction().hide(fm.findFragmentByTag("news")).commit();
        }
        if(fm.findFragmentByTag("mapmenu") != null){
            //if the other fragment is visible, hide it.
            fm.beginTransaction().hide(fm.findFragmentByTag("mapmenu")).commit();
        }
    }

    public void showMapMenuFrag(){
        if(PREV_FRAME==MAPMENU_FRAME){PREV_FRAME=0;}
        else{PREV_FRAME=ACTIVE_FRAME;}
        ACTIVE_FRAME=MAPMENU_FRAME;
        hideFragContainer();
        hideReportFrag();
        fragCont.setVisibility(View.VISIBLE);
        fab.setVisibility(View.GONE);
        /*if (savedIS != null) {
            Log.e(TAG, "savedIS returns");
            return;
        }*/

        if(fm.findFragmentByTag("mapmenu") != null && PREV_FRAME!=0) //PREV_FRAME!=0 is needed to make sure it's not coming from showreportfrag
        {
            Log.e(TAG, "mapmenu should be shown prev: "+PREV_FRAME);
            fm.beginTransaction().show(fm.findFragmentByTag("mapmenu")).commit();
        } else {
            Log.e(TAG, "mapmenu new frag");
            fm.beginTransaction().add(R.id.fragment_container, new MapMenuFragment(), "mapmenu").addToBackStack(null).commit();
        }

        if(fm.findFragmentByTag("news") != null){
            fm.beginTransaction().hide(fm.findFragmentByTag("news")).commit();
        }
        if(fm.findFragmentByTag("research") != null){
            fm.beginTransaction().hide(fm.findFragmentByTag("research")).commit();
        }
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
            myMenu.findItem(R.id.action_filter_badge).setVisible(true);
            myMenu.findItem(R.id.action_search).setVisible(false);
        }

        //search
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if( ACTIVE_FRAME==NEWS_FRAME){
                    spinOptions.add(0, "Поиск: "+query);
                    idMap.put(getResources().getString(R.string.all_news),0);
                    spinnerDataAdapter.notifyDataSetChanged();

                    Uri.Builder builder = new Uri.Builder();
                    builder.scheme(Endpoints.SCHEME).authority(Endpoints.AUTHORITY).appendPath(Endpoints.API).appendPath("news");
                    if(news_ctg_id!=0){
                        builder.appendQueryParameter("category_id", ""+news_ctg_id);
                    }
                    builder.appendQueryParameter("text", query);
                    searchView.clearFocus();
                    newsFragment.populateList(1,builder,true,false);
                }
                else if( ACTIVE_FRAME==RESEARCH_FRAME){
                    searchView.setQuery("", false);
                    searchView.clearFocus();
                    MenuItemCompat.collapseActionView(searchItem);
                    Intent intent = new Intent(MainActivity.this, DocListActivity.class);
                    intent.putExtra("query",query);
                    startActivity(intent);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

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
        else if(requestCode==SETTINGS_FLAG){
            String lang=data.getStringExtra("lang");
            Log.e(TAG,"result "+lang);
            LocaleHelper.setLocale(context, lang);
            session.setLanguage(lang);
            Intent myIntent = getIntent();
            myIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            finish();
            startActivity(myIntent);
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

    private class VocabularyTask extends AsyncTask<Void, Void, List<Vocabulary>> {
        protected List<Vocabulary> doInBackground(Void... params) {
            if(dbHandler==null){dbHandler = new MyDbHandler(context); Log.e(TAG, "VocTask dbhandler was null");}
            if(db==null || !db.isOpen()){db = dbHandler.getWritableDatabase(); Log.e(TAG, "VocTask db was null or not open");}

            return dbHandler.getVocContents(db);
        }
        protected void onPostExecute(List<Vocabulary> theList) {
            if(theList.size()>0){
                for (Vocabulary voc : theList) {
                    if(voc.getKey().equals("news_category")){
                        int id=voc.getId();
                        String value=voc.getValue();
                        idMap.put(value,id);
                        spinOptions.add(value);
                    }
                }
                spinnerDataAdapter.notifyDataSetChanged();
                Log.e(TAG, "voc data has been taken from DB");
            }
            else{
                Log.e("VocTask", "no content in db, requesting server");
                requestVocabularies(); //requesting server
            }
        }
    }

    public void requestVocabularies(){
        String uri = Endpoints.VOCABULARIES;
        Response.Listener<JSONArray> listener = new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray jsonArray) {

                Log.e(TAG, "reqVoc response: " + jsonArray);
                try{
                    helper.doClearVocTask();
                    for(int s=0; s < jsonArray.length(); s++){
                        JSONObject jsonObject = jsonArray.getJSONObject(s);
                        int id=jsonObject.getInt("id");
                        String key=jsonObject.getString("key");
                        String value=jsonObject.getString("value");
                        int parent=jsonObject.getInt("parent");
                        int order=jsonObject.getInt("ordered_id");
                        Vocabulary voc =new Vocabulary(id,key,value,parent,order,false);
                        helper.addVocabulary(voc);
                        if(key.equals("news_category")){
                            spinOptions.add(value);
                            idMap.put(value,id);
                        }
                    }
                    spinnerDataAdapter.notifyDataSetChanged();
                }catch(JSONException e){e.printStackTrace();}
            }
        };

        Response.ErrorListener errListener=new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse networkResponse = error.networkResponse;
                Log.e(TAG, "Volley error: " + error.getMessage() + ", code: " + networkResponse);
            }
        };

        JsonArrayRequest volReq = new JsonArrayRequest(Request.Method.GET, uri, null, listener,errListener);

        MyVolley.getInstance(context).addToRequestQueue(volReq);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (fm.getBackStackEntryCount() > 0 && PREV_FRAME!=0) {
                Log.e(TAG,"nav main prev: "+PREV_FRAME +" active: "+ACTIVE_FRAME);
                fm.popBackStack();
                showPrevFrame();
            } else {
                Log.e(TAG,"superOnBack");
                showMainFrag();
                super.onBackPressed();
            }
        }
    }

    public void showPrevFrame(){
        if(PREV_FRAME==NEWS_FRAME && ACTIVE_FRAME!=NEWS_FRAME){showNewsFrag(); navigationView.setCheckedItem(R.id.nav_news);}
        else if(PREV_FRAME==RESEARCH_FRAME && ACTIVE_FRAME!=RESEARCH_FRAME){showResearchFrag(); navigationView.setCheckedItem(R.id.nav_research);}
        else if(PREV_FRAME==MAPMENU_FRAME && ACTIVE_FRAME!=MAPMENU_FRAME){
            Log.e(TAG,"Back press says to showMapMenuFrag");
            showMapMenuFrag();
            navigationView.setCheckedItem(R.id.nav_map);
        }
        else if(PREV_FRAME==REPORT_FRAME && ACTIVE_FRAME!=REPORT_FRAME){showReportFrag(visible_tab); navigationView.setCheckedItem(R.id.nav_map);}
        else {showMainFrag();}
    }

    //change language
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }
}
