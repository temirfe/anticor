package kg.prosoft.anticorruption;


import android.app.Activity;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;

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
import java.util.List;

import kg.prosoft.anticorruption.service.Endpoints;
import kg.prosoft.anticorruption.service.MyDbHandler;
import kg.prosoft.anticorruption.service.MyHelper;
import kg.prosoft.anticorruption.service.MyVolley;
import kg.prosoft.anticorruption.service.Page;
import kg.prosoft.anticorruption.service.PageAdapter;
import kg.prosoft.anticorruption.service.SessionManager;


/**
 * A simple {@link Fragment} subclass.
 */
public class FightFragment extends Fragment {
    ListView listView;
    ArrayList<Page> infoList;
    ArrayList<Page> fightList;
    PageAdapter listAdapter;
    Context context;
    Activity activity;

    SessionManager session;
    public SQLiteDatabase db;
    public MyDbHandler dbHandler;
    MyHelper helper;
    String TAG="FightFrag";
    ProgressBar pbar;

    public FightFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_fight, container, false);
        activity=getActivity();
        context=activity.getApplicationContext();
        session = new SessionManager(context);
        dbHandler = new MyDbHandler(context);
        db = dbHandler.getWritableDatabase();
        helper = new MyHelper(activity, dbHandler, db, session);
        pbar=(ProgressBar)view.findViewById(R.id.id_pbar);

        infoList=new ArrayList<>();
        fightList=new ArrayList<>();

        listView = (ListView) view.findViewById(R.id.listView);
        listAdapter = new PageAdapter(getContext(), fightList);
        listView.setAdapter(listAdapter);

        new PageTask().execute();

        return view;
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
                filterPages(theList);
                listAdapter.notifyDataSetChanged();
                Log.e(TAG, "page data has been taken from DB");
                checkPageDepend();
            }
            else{
                Log.e(TAG, "no content in db, requesting server");
                requestPages(); //requesting server
            }
        }
    }

    public void filterPages(ArrayList<Page> theList){
        for(int i=0; i<theList.size(); i++){
            Page page=theList.get(i);
            String desc=page.getDescription();
            if(desc.equals("fight")){
                fightList.add(page);
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
                    infoList.clear();
                    for(int s=0; s < jsonArray.length(); s++){
                        JSONObject jsonObject = jsonArray.getJSONObject(s);
                        int id=jsonObject.getInt("id");
                        String title=jsonObject.getString("title");
                        String text=jsonObject.getString("text");
                        String desc=jsonObject.getString("description");
                        Page page =new Page(id,title, text, desc);
                        infoList.add(page);
                        if(desc.equals("fight")){
                            fightList.add(page);
                        }

                    }
                    helper.addPageList(infoList);
                    listAdapter.notifyDataSetChanged();
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
