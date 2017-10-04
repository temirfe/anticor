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
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

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
    private Activity activity;
    public int product_id;
    private SessionManager session;

    public MyHelper(Activity actvty){
        context = actvty.getApplicationContext();
        activity = actvty;
        session = new SessionManager(context);
    }

    public MyHelper(Activity actvty,MyDbHandler dbHandler,SQLiteDatabase db, SessionManager session){
        context = actvty.getApplicationContext();
        activity = actvty;
        this.dbHandler=dbHandler;
        this.db=db;
        this.session = session;
    }

    public void addVocabulary(Vocabulary voc){
        new AddVocTask().execute(voc);
    }
    public void addVocabularyList(SparseArray<Vocabulary> vocs){
        doClearVocTask();//delete previous ones and add new ones
        for(int i=0; i<vocs.size();i++){
            new AddVocTask().execute(vocs.get(i));
        }
    }

    private class AddVocTask extends AsyncTask<Vocabulary, Void, Void> {
        protected Void doInBackground(Vocabulary... params) {
            if(dbHandler==null){dbHandler = new MyDbHandler(context);}
            if(db==null || !db.isOpen()){db = dbHandler.getWritableDatabase();}
            dbHandler.addVocItem(params[0], db);
            return null;
        }
    }

    public void doVocabularyTask(){
        new VocabularyTask().execute();
    }

    private class VocabularyTask extends AsyncTask<Void, Void, List<Vocabulary>> {
        protected List<Vocabulary> doInBackground(Void... params) {
            if(dbHandler==null){dbHandler = new MyDbHandler(context); Log.e(TAG, "VocabularyTask dbhandler was null");}
            if(db==null || !db.isOpen()){db = dbHandler.getWritableDatabase(); Log.e(TAG, "VocabularyTask db was null or not open");}

            return dbHandler.getVocContents(db);
        }
        protected void onPostExecute(List<Vocabulary> vocList) {
            if(vocList.size()>0){
                for (Vocabulary voc : vocList) {
                    String log = "rowId: "+voc.getRowId()+", Id: " + voc.getId() + ", key: " + voc.getKey()+ ", value: " + voc.getValue();
                    // Writing Contacts to log
                    //Log.e("Contents: ", log);
                }
            }
            else{
                Log.e("VocabularyTask", "no content aa");
                requestVocabularies();
            }
        }
    }

    public void requestVocabularies(){
        String uri = Endpoints.VOCABULARIES;
        Response.Listener<JSONArray> listener = new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray jsonArray) {
                Log.e(TAG, "response: " + jsonArray);
                try{
                    doClearVocTask();
                    for(int s=0; s < jsonArray.length(); s++){
                        JSONObject jsonObject = jsonArray.getJSONObject(s);
                        int id=jsonObject.getInt("id");
                        String key=jsonObject.getString("key");
                        String value=jsonObject.getString("value");
                        int parent=jsonObject.getInt("parent");
                        int order=jsonObject.getInt("ordered_id");
                        Vocabulary voc=new Vocabulary(id,key,value,parent,order,false);
                        addVocabulary(voc);
                    }
                    //helper.addVocabulary(vocList);
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

    public void doClearVocTask(){
        new ClearVocTask().execute();
    }

    private class ClearVocTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {
            if(dbHandler==null){dbHandler = new MyDbHandler(context);}
            if(db==null || !db.isOpen()){db = dbHandler.getWritableDatabase();}

            dbHandler.clearVocabulary(db);

            return null;
        }
    }

    public void doReadVocTask(){
        new ReadVocTask().execute();
    }

    private class ReadVocTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {
            if(dbHandler==null){dbHandler = new MyDbHandler(context); Log.e(TAG, "ReadTask dbhandler was null");}
            if(db==null || !db.isOpen()){db = dbHandler.getWritableDatabase(); Log.e(TAG, "ReadTask db was null or not open");}

            List<Vocabulary> vocItems = dbHandler.getVocContents(db);
            if(vocItems.size()>0){
                for (Vocabulary voc : vocItems) {
                    String log = "rowId: "+voc.getRowId()+", Id: " + voc.getId() + ", key: " + voc.getKey()+ ", value: " + voc.getValue();
                    // Writing Contacts to log
                    Log.e("Contents: ", log);
                }
            }
            else{
                Log.e("ReadVocTask", "no content");
            }

            return null;
        }
    }

    //Authority
    public void getAuthDepend(){
        String uri = Endpoints.DEPEND;
        StringRequest volReqe = new StringRequest(Request.Method.GET, uri,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        String depend=session.getAuthorityDepend();
                        response=response.replace("\"","");
                        Log.e(TAG, "depend: "+depend+" response:"+response);
                        if(!response.equals(depend)){
                            //new maxId is different, that mean category table has been altered. send new request.
                            requestAuthority();
                            session.setAuthorityDepend(response);
                        }
                        session.setAuthorityDependChecked(true);
                    }
                }, null);
        Response.Listener<JSONArray> list = new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray jsonArray) {
                Log.e(TAG, "response: " + jsonArray);
                try{
                    for(int i=0; i < jsonArray.length(); i++){
                        JSONObject productObj = jsonArray.getJSONObject(i);
                        String name=productObj.getString("table_name");
                        String update=productObj.getString("last_update");
                    }
                }catch(JSONException e){e.printStackTrace();}
            }
        };
        JsonArrayRequest volReq = new JsonArrayRequest(Request.Method.GET, uri, null, list,null);
        MyVolley.getInstance(context).addToRequestQueue(volReq);


    }

    public void doAuthorityTask(){
        new AuthorityTask().execute();
    }

    private class AuthorityTask extends AsyncTask<Void, Void, List<Authority>> {
        protected List<Authority> doInBackground(Void... params) {
            if(dbHandler==null){dbHandler = new MyDbHandler(context); Log.e(TAG, "AuthorityTask dbhandler was null");}
            if(db==null || !db.isOpen()){db = dbHandler.getWritableDatabase(); Log.e(TAG, "AuthorityTask db was null or not open");}

            return dbHandler.getAuthContents(db);
        }
        protected void onPostExecute(List<Authority> theList) {
            if(theList.size()>0){
                for (Authority voc : theList) {
                    String log = "rowId: "+voc.getRowId()+", Id: " + voc.getId() + ", title: " + voc.getTitle();
                    // Writing Contacts to log
                    //Log.e("Contents: ", log);
                }
            }
            else{
                Log.e("AuthorityTask", "no content aa");
                requestAuthority();
            }
        }
    }

    public void addAuthority(Authority voc){
        new AddAuthTask().execute(voc);
    }

    private class AddAuthTask extends AsyncTask<Authority, Void, Void> {
        protected Void doInBackground(Authority... params) {
            if(dbHandler==null){dbHandler = new MyDbHandler(context);}
            if(db==null || !db.isOpen()){db = dbHandler.getWritableDatabase();}
            dbHandler.addAuthItem(params[0], db);
            return null;
        }
    }

    public void requestAuthority(){
        String uri = Endpoints.AUTHORITIES;
        Response.Listener<JSONArray> listener = new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray jsonArray) {
                Log.e(TAG, "response: " + jsonArray);
                try{
                    doClearAuthTask();
                    for(int s=0; s < jsonArray.length(); s++){
                        JSONObject jsonObject = jsonArray.getJSONObject(s);
                        int id = jsonObject.getInt("id");
                        String title=jsonObject.getString("title");
                        String text=jsonObject.getString("text");
                        String image=jsonObject.getString("img");
                        int parent_id=jsonObject.getInt("category_id");

                        Authority authority = new Authority(id, title, text, image, parent_id);
                        addAuthority(authority);
                    }
                    //helper.addVocabulary(vocList);
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

    public void doClearAuthTask(){
        new ClearAuthTask().execute();
    }

    private class ClearAuthTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {
            if(dbHandler==null){dbHandler = new MyDbHandler(context);}
            if(db==null || !db.isOpen()){db = dbHandler.getWritableDatabase();}

            dbHandler.clearAuthority(db);

            return null;
        }
    }

}
