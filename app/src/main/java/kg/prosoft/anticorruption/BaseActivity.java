package kg.prosoft.anticorruption;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.RequestQueue;

import kg.prosoft.anticorruption.service.Endpoints;
import kg.prosoft.anticorruption.service.MyDbHandler;
import kg.prosoft.anticorruption.service.MyHelper;
import kg.prosoft.anticorruption.service.MyVolley;
import kg.prosoft.anticorruption.service.SessionManager;

public class BaseActivity extends AppCompatActivity {

    String TAG = "BASE ACTIVITY";

    public Context thisContext;
    public Context appContext;
    public SQLiteDatabase db;
    public MyDbHandler dbHandler;
    MyHelper helper;
    SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        thisContext=this;
        appContext=getApplicationContext();

        session = new SessionManager(appContext);

        dbHandler = new MyDbHandler(appContext);
        db = dbHandler.getWritableDatabase();
        helper = new MyHelper(thisContext, dbHandler, db, session);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        //change search icon color to white
        Drawable searchIcon = menu.findItem(R.id.action_search).getIcon();
        if(searchIcon != null) {
            searchIcon.mutate();
            int color;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                color =thisContext.getResources().getColor(android.R.color.white, thisContext.getTheme());
            }else {
                color =thisContext.getResources().getColor(android.R.color.white);
            }
            searchIcon.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        }

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchManager searchManager = (SearchManager) BaseActivity.this.getSystemService(Context.SEARCH_SERVICE);

        SearchView searchView = null;
        if (searchItem != null) {
            searchView = (SearchView) searchItem.getActionView();
        }
        if (searchView != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(BaseActivity.this.getComponentName()));
            searchView.setQueryHint(getResources().getString(R.string.search));
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        Log.e(TAG,"menu item "+id);

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }/*
        if (id == R.id.action_search) {
            Log.e(TAG,"search clicked");
            return true;
        }*/
        /*
        if (id == R.id.action_cart) {
            Log.e(TAG,"cart menu clicked");
            return true;
        }*/

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //session.clear();
        session.setDependChecked(false);
        session.setLookupDependChecked(false);
        if(db!=null && db.isOpen()){db.close();}
        RequestQueue queue = MyVolley.getInstance(appContext).getRequestQueue();
        queue.cancelAll(appContext);
    }
}
