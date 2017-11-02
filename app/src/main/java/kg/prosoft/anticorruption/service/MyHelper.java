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

    public void checkVocDepend(){
        String uri = Endpoints.VOC_DEPEND;
        StringRequest volReq = new StringRequest(Request.Method.GET, uri,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        String depend=session.getVocabularyDepend();
                        response=response.replace("\"","");
                        Log.e(TAG, "voc depend: "+depend+" response: "+response);
                        if(!response.equals(depend)){
                            //new maxId is different, that mean category table has been altered. send new request.
                            requestVocabularies();
                            session.setVocabularyDepend(response);
                        }
                        else{ Log.e(TAG, "voc depend matches");}
                        session.setVocabularyDependChecked(true);
                    }
                }, null);

        MyVolley.getInstance(context).addToRequestQueue(volReq);
    }
    public void addVocabulary(Vocabulary voc){
        new AddVocTask().execute(voc);
    }

    private class AddVocTask extends AsyncTask<Vocabulary, Void, Void> {
        protected Void doInBackground(Vocabulary... params) {
            if(dbHandler==null){dbHandler = new MyDbHandler(context);}
            if(db==null || !db.isOpen()){db = dbHandler.getWritableDatabase();}
            dbHandler.addVocItem(params[0], db);
            return null;
        }
    }

    public void requestVocabularies(){
        String uri = Endpoints.VOCABULARIES;
        Response.Listener<JSONArray> listener = new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray jsonArray) {
                Log.e(TAG, "voc response: " + jsonArray);
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
    public void checkAuthDepend(){
        String uri = Endpoints.AUTH_DEPEND;
        StringRequest volReq = new StringRequest(Request.Method.GET, uri,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        String depend=session.getAuthorityDepend();
                        response=response.replace("\"","");
                        Log.e(TAG, "auth depend: "+depend+" response:"+response);
                        if(!response.equals(depend)){
                            //new maxId is different, that mean category table has been altered. send new request.
                            requestAuthority();
                            session.setAuthorityDepend(response);
                        }
                        session.setAuthorityDependChecked(true);
                    }
                }, null);

        MyVolley.getInstance(context).addToRequestQueue(volReq);
    }

    public void insertAuthority(Authority voc){
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
                Log.e(TAG, "auth response: " + jsonArray);
                try{
                    doClearAuthTask();
                    for(int s=0; s < jsonArray.length(); s++){
                        JSONObject jsonObject = jsonArray.getJSONObject(s);
                        int id = jsonObject.getInt("id");
                        String title=jsonObject.getString("title");
                        String text=jsonObject.getString("text");
                        String image=jsonObject.getString("img");
                        int parent_id=jsonObject.getInt("parent_id");
                        int rating=jsonObject.getInt("rating");
                        int comments=jsonObject.getInt("comments");
                        int reports=jsonObject.getInt("reports");

                        Authority authority = new Authority(id, title, text, image, parent_id,rating,comments,reports);
                        insertAuthority(authority);
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

    /** News **/
    public void addNewsList(ArrayList<News> list){
        doClearNewsTask();//delete previous ones and add new ones
        for(int i=0; i<list.size();i++){
            new AddNewsTask().execute(list.get(i));
        }
    }

    private class AddNewsTask extends AsyncTask<News, Void, Void> {
        protected Void doInBackground(News... params) {
            if(dbHandler==null){dbHandler = new MyDbHandler(context);}
            if(db==null || !db.isOpen()){db = dbHandler.getWritableDatabase();}
            dbHandler.addNewsItem(params[0], db);
            return null;
        }
    }

    public void doClearNewsTask(){
        new ClearNewsTask().execute();
    }

    private class ClearNewsTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {
            if(dbHandler==null){dbHandler = new MyDbHandler(context);}
            if(db==null || !db.isOpen()){db = dbHandler.getWritableDatabase();}

            dbHandler.clearNews(db);

            return null;
        }
    }

    /** Report **/
    public void addReportList(ArrayList<Report> list){
        doClearReportTask();//delete previous ones and add new ones
        for(int i=0; i<list.size();i++){
            new AddReportTask().execute(list.get(i));
        }
    }

    private class AddReportTask extends AsyncTask<Report, Void, Void> {
        protected Void doInBackground(Report... params) {
            if(dbHandler==null){dbHandler = new MyDbHandler(context);}
            if(db==null || !db.isOpen()){db = dbHandler.getWritableDatabase();}
            dbHandler.addReportItem(params[0], db);
            return null;
        }
    }

    public void doClearReportTask(){
        new ClearReportTask().execute();
    }

    private class ClearReportTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {
            if(dbHandler==null){dbHandler = new MyDbHandler(context);}
            if(db==null || !db.isOpen()){db = dbHandler.getWritableDatabase();}

            dbHandler.clearReport(db);

            return null;
        }
    }


    /** Page **/
    public void addPageList(ArrayList<Page> list){
        doClearPageTask();//delete previous ones and add new ones
        for(int i=0; i<list.size();i++){
            new AddPageTask().execute(list.get(i));
        }
    }

    private class AddPageTask extends AsyncTask<Page, Void, Void> {
        protected Void doInBackground(Page... params) {
            if(dbHandler==null){dbHandler = new MyDbHandler(context);}
            if(db==null || !db.isOpen()){db = dbHandler.getWritableDatabase();}
            dbHandler.addPageItem(params[0], db);
            return null;
        }
    }

    public void doClearPageTask(){
        new ClearPageTask().execute();
    }

    private class ClearPageTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {
            if(dbHandler==null){dbHandler = new MyDbHandler(context);}
            if(db==null || !db.isOpen()){db = dbHandler.getWritableDatabase();}

            dbHandler.clearPage(db);

            return null;
        }
    }

    //clear db


    public void doClearDbTask(){
        new ClearDbTask().execute();
    }

    private class ClearDbTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {
            if(dbHandler==null){dbHandler = new MyDbHandler(context);}
            if(db==null || !db.isOpen()){db = dbHandler.getWritableDatabase();}

            dbHandler.clearDb(db);

            return null;
        }
    }

}
