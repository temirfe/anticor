package kg.prosoft.anticorruption;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import kg.prosoft.anticorruption.service.Endpoints;
import kg.prosoft.anticorruption.service.MyDbHandler;
import kg.prosoft.anticorruption.service.MyHelper;
import kg.prosoft.anticorruption.service.MyVolley;
import kg.prosoft.anticorruption.service.Page;
import kg.prosoft.anticorruption.service.SessionManager;


/**
 * A simple {@link Fragment} subclass.
 */
public class AboutFragment extends Fragment {

    TextView tv_title,tv_text;

    SessionManager session;
    public SQLiteDatabase db;
    public MyDbHandler dbHandler;
    MyHelper helper;
    String TAG="AboutFrag";
    Context context;
    Activity activity;
    ProgressBar pbar;
    ArrayList<Page> infoList;

    public AboutFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v= inflater.inflate(R.layout.fragment_about, container, false);
        activity=getActivity();
        context=activity.getApplicationContext();
        session = new SessionManager(context);
        dbHandler = new MyDbHandler(context);
        db = dbHandler.getWritableDatabase();
        helper = new MyHelper(activity, dbHandler, db, session);
        infoList=new ArrayList<>();

        tv_title=(TextView)v.findViewById(R.id.id_tv_title);
        tv_text=(TextView)v.findViewById(R.id.id_tv_text);
        pbar=(ProgressBar)v.findViewById(R.id.id_pbar);
        new PageTask().execute();
        return v;
    }


    public static CharSequence trimTrailingWhitespace(CharSequence source) {

        if(source == null)
            return "";

        int i = source.length();

        // loop back to the first non-whitespace character
        while(--i >= 0 && Character.isWhitespace(source.charAt(i))) {
        }

        return source.subSequence(0, i+1);
    }

    private class PageTask extends AsyncTask<Void, Void, ArrayList<Page>> {
        protected ArrayList<Page> doInBackground(Void... params) {
            if(dbHandler==null){dbHandler = new MyDbHandler(context);}
            if(db==null || !db.isOpen()){db = dbHandler.getWritableDatabase();}

            return dbHandler.getPageContents(db);
        }
        protected void onPostExecute(ArrayList<Page> theList) {
            if(theList.size()>0){
                pbar.setVisibility(View.GONE);
                for(int i=0; i<theList.size(); i++){
                    Page page=theList.get(i);
                    String desc=page.getDescription();
                    String text = page.getText();
                    if(desc.equals("about")){
                        tv_title.setText(page.getTitle());

                        CharSequence html_text;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                            html_text= Html.fromHtml(text,Html.FROM_HTML_MODE_LEGACY);
                        } else {
                            html_text=Html.fromHtml(text);
                        }
                        tv_text.setText(trimTrailingWhitespace(html_text));
                    }
                }
                Log.e(TAG, "page data has been taken from DB");
                checkPageDepend();
            }
            else{
                Log.e(TAG, "no content in db, requesting server");
                requestPages(); //requesting server
            }
        }
    }

    public void requestPages(){
        String uri = Endpoints.PAGES;
        Response.Listener<JSONArray> listener = new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray jsonArray) {
                pbar.setVisibility(View.GONE);
                Log.e(TAG, "reqPage response: " + jsonArray);
                try{
                    for(int s=0; s < jsonArray.length(); s++){
                        JSONObject jsonObject = jsonArray.getJSONObject(s);
                        int id=jsonObject.getInt("id");
                        String title=jsonObject.getString("title");
                        String text=jsonObject.getString("text");
                        String desc=jsonObject.getString("description");
                        Page page =new Page(id,title, text, desc);
                        infoList.add(page);
                        if(desc.equals("about")){
                            tv_title.setText(page.getTitle());

                            CharSequence html_text;
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                                html_text= Html.fromHtml(text,Html.FROM_HTML_MODE_LEGACY);
                            } else {
                                html_text=Html.fromHtml(text);
                            }
                            tv_text.setText(trimTrailingWhitespace(html_text));
                        }

                    }
                    helper.addPageList(infoList);
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

    public void checkPageDepend(){
        String uri = Endpoints.PAGE_DEPEND;
        StringRequest volReq = new StringRequest(Request.Method.GET, uri,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        String depend=session.getPageDepend();
                        response=response.replace("\"","");
                        Log.e(TAG, "depend: "+depend+" response: "+response);
                        if(!response.equals(depend)){
                            //new maxId is different, that mean category table has been altered. send new request.
                            requestPages(); //requesting server
                            session.setPageDepend(response);
                        }
                        else{ Log.e(TAG, "depend matches");}
                    }
                }, null);

        MyVolley.getInstance(context).addToRequestQueue(volReq);
    }

}
