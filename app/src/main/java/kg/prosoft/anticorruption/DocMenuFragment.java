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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import kg.prosoft.anticorruption.service.DocMenu;
import kg.prosoft.anticorruption.service.DocMenuAdapter;
import kg.prosoft.anticorruption.service.Endpoints;
import kg.prosoft.anticorruption.service.MyDbHandler;
import kg.prosoft.anticorruption.service.MyHelper;
import kg.prosoft.anticorruption.service.MyVolley;
import kg.prosoft.anticorruption.service.SessionManager;
import kg.prosoft.anticorruption.service.Vocabulary;


/**
 * A simple {@link Fragment} subclass.
 */
public class DocMenuFragment extends Fragment {

    ListView listView;
    ArrayList<DocMenu> infoList;
    DocMenuAdapter listAdapter;
    Context context;
    Activity activity;

    SessionManager session;
    public SQLiteDatabase db;
    public MyDbHandler dbHandler;
    MyHelper helper;
    String TAG="DocMenuFrag";
    ProgressBar pbar;

    public DocMenuFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_doc_menu, container, false);
        activity=getActivity();
        context=activity.getApplicationContext();
        session = new SessionManager(context);
        dbHandler = new MyDbHandler(context);
        db = dbHandler.getWritableDatabase();
        helper = new MyHelper(activity, dbHandler, db, session);
        pbar=(ProgressBar)view.findViewById(R.id.id_pbar);

        infoList=new ArrayList<>();

        listView = (ListView) view.findViewById(R.id.listView);
        listAdapter = new DocMenuAdapter(getContext(), infoList);
        listView.setAdapter(listAdapter);

        new VocabularyTask().execute();

        return view;
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
                    if(voc.getKey().equals("document_category")){
                        int id=voc.getId();
                        String value=voc.getValue();
                        infoList.add(new DocMenu(id,value));
                    }
                }
                pbar.setVisibility(View.GONE);
                listAdapter.notifyDataSetChanged();
                Log.e(TAG, "voc data has been taken from DB");
            }
            else{
                Log.e(TAG, "no content in db, requesting server");
                requestVocabularies(); //requesting server
            }
        }
    }

    public void requestVocabularies(){
        String uri = Endpoints.VOCABULARIES;
        Response.Listener<JSONArray> listener = new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray jsonArray) {
                pbar.setVisibility(View.GONE);
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
                        if(key.equals("document_category")){
                            infoList.add(new DocMenu(id,value));
                        }
                    }
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

}
