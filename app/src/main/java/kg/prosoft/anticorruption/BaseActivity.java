package kg.prosoft.anticorruption;

import android.app.Activity;
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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.RequestQueue;

import org.w3c.dom.Text;

import kg.prosoft.anticorruption.service.MyDbHandler;
import kg.prosoft.anticorruption.service.MyHelper;
import kg.prosoft.anticorruption.service.MyVolley;
import kg.prosoft.anticorruption.service.SessionManager;

public class BaseActivity extends AppCompatActivity {

    String TAG = "BASE ACTIVITY";

    public Activity activity;
    public Context context;
    public SQLiteDatabase db;
    public MyDbHandler dbHandler;
    MyHelper helper;
    SessionManager session;
    SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity=this;
        context=getApplicationContext();

        session = new SessionManager(context);

        dbHandler = new MyDbHandler(context);
        db = dbHandler.getWritableDatabase();
        helper = new MyHelper(activity, dbHandler, db, session);

        if(!session.isVocabularyDependChecked()){
            helper.checkVocDepend();
        }
        else{
            Log.e(TAG,"VocDepend was checked");
        }
        if(!session.isAuthorityDependChecked()){
            helper.checkAuthDepend();
        }
        else{
            Log.e(TAG,"AuthDepend was checked");
        }
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
                color =activity.getResources().getColor(android.R.color.white, activity.getTheme());
            }else {
                color =activity.getResources().getColor(android.R.color.white);
            }
            searchIcon.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        }

        MenuItem searchItem = menu.findItem(R.id.action_search);
        //SearchManager searchManager = (SearchManager) BaseActivity.this.getSystemService(Context.SEARCH_SERVICE);

        if (searchItem != null) {
            searchView = (SearchView) searchItem.getActionView();
            //searchView.setSearchableInfo(searchManager.getSearchableInfo(BaseActivity.this.getComponentName()));
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //session.clear();
        session.setDependChecked(false);
        session.setAuthorityDependChecked(false);
        if(db!=null && db.isOpen()){db.close();}
        RequestQueue queue = MyVolley.getInstance(context).getRequestQueue();
        queue.cancelAll(context);
    }
}
