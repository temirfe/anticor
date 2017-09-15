package kg.prosoft.anticorruption.service;

import android.app.Activity;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * Created by ProsoftPC on 8/14/2017.
 */

public class MyHelper {
    private SQLiteDatabase db;
    private MyDbHandler dbHandler;
    private String TAG = "MYHELPER";
    private Context context;
    private Context activity;
    public int product_id;
    private SessionManager session;

    public MyHelper(Context ctx, Context actvty){
        context = ctx;
        activity = actvty;
        session = new SessionManager(context);
    }

    public MyHelper(Context actvty,MyDbHandler dbHandler,SQLiteDatabase db, SessionManager session){
        context = actvty.getApplicationContext();
        activity = actvty;
        this.dbHandler=dbHandler;
        this.db=db;
        this.session = session;
    }


    public void addLookup(SparseArray<Lookup> lookups){
        doClearLookupTask();//delete previous ones and add new ones
        for(int i=0; i<lookups.size();i++){
            new AddLookupTask().execute(lookups.get(i));
        }
    }

    private class AddLookupTask extends AsyncTask<Lookup, Void, Void> {
        protected Void doInBackground(Lookup... params) {
            if(dbHandler==null){dbHandler = new MyDbHandler(context);}
            if(db==null || !db.isOpen()){db = dbHandler.getWritableDatabase();}
            dbHandler.addLookupItem(params[0], db);
            return null;
        }
    }

    public void doClearLookupTask(){
        new CleaLookupTask().execute();
    }
    private class CleaLookupTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {
            if(dbHandler==null){dbHandler = new MyDbHandler(context);}
            if(db==null || !db.isOpen()){db = dbHandler.getWritableDatabase();}

            dbHandler.clearLookup(db);

            return null;
        }
    }
}
