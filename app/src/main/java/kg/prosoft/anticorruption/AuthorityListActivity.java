package kg.prosoft.anticorruption;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import kg.prosoft.anticorruption.service.Authority;
import kg.prosoft.anticorruption.service.AuthorityAdapter;
import kg.prosoft.anticorruption.service.Endpoints;
import kg.prosoft.anticorruption.service.MyDbHandler;
import kg.prosoft.anticorruption.service.MyHelper;
import kg.prosoft.anticorruption.service.MyVolley;
import kg.prosoft.anticorruption.service.SessionManager;

public class AuthorityListActivity extends AppCompatActivity {

    String TAG="AuthListAct";
    ListView listView;
    AuthorityAdapter adapter;
    List<Authority> authList;
    private LinkedHashMap<Integer, Authority> parentMap,childMap;
    private HashMap<Integer, HashMap<Integer, Authority>> parentChildMap;
    Context context;
    Activity activity;
    ProgressBar pb;
    Button btn_reload;
    LinearLayout ll_reload;
    //ImageView iv_thumb;
    SessionManager session;
    public SQLiteDatabase db;
    public MyDbHandler dbHandler;
    MyHelper helper;
    String lang;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authority_list);
        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(R.string.corruption_rating);
        }

        activity=this;
        context=getApplicationContext();
        session = new SessionManager(context);
        dbHandler = new MyDbHandler(context);
        db = dbHandler.getWritableDatabase();
        helper = new MyHelper(activity, dbHandler, db, session);
        lang=session.getLanguage();
        if(lang.isEmpty()){lang="ky";}

        listView = (ListView) findViewById(R.id.id_lv_authorities);
        ll_reload=(LinearLayout)findViewById(R.id.id_ll_reload);
        btn_reload=(Button)findViewById(R.id.id_btn_reload);
        btn_reload.setOnClickListener(reloadClickListener);
        //iv_thumb=(ImageView)layout.findViewById(R.id.id_iv_thumb);

        pb = (ProgressBar)findViewById(R.id.progressBar1);

        authList = new ArrayList<>();
        adapter = new AuthorityAdapter(context,authList);
        listView.setAdapter(adapter);
        parentMap=new LinkedHashMap<>();
        childMap= new LinkedHashMap<>();
        parentChildMap=new HashMap<>();
        new AuthorityTask().execute();

        listView.setOnItemClickListener(itemClickListener);
    }

    View.OnClickListener reloadClickListener = new View.OnClickListener(){
        public void onClick(View v){
            populateList();
            pb.setVisibility(ProgressBar.VISIBLE);
            ll_reload.setVisibility(View.GONE);
        }
    };

    AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener(){
        public void onItemClick(AdapterView<?> listView,
                                View itemView,
                                int position,
                                long id) {
            Authority item =authList.get(position);
            Intent intent = new Intent(context, AuthorityViewActivity.class);
            intent.putExtra("id",item.getId());
            intent.putExtra("title",item.getTitle());
            intent.putExtra("text",item.getText());
            intent.putExtra("image",item.getImage());
            intent.putExtra("parent_id",item.getParentId());
            intent.putExtra("rating",item.getRating());
            startActivity(intent);
        }
    };

    public void checkAuthDepend(){
        String uri = Endpoints.AUTH_DEPEND;
        StringRequest volReq = new StringRequest(Request.Method.GET, uri,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        String depend=session.getAuthorityDepend();
                        response=response.replace("\"","");
                        Log.e(TAG, "depend: "+depend+" response: "+response);
                        if(!response.equals(depend)){
                            //new maxId is different, that mean category table has been altered. send new request.
                            populateList();
                            session.setAuthorityDepend(response);
                        }
                        else{ Log.e(TAG, "depend matches");}
                        session.setAuthorityDependChecked(true);
                    }
                }, null);

        MyVolley.getInstance(context).addToRequestQueue(volReq);
    }

    public void populateList(){
        pb.setVisibility(ProgressBar.VISIBLE);
        String uri = Endpoints.AUTHORITIES+"?lang="+lang;

        Response.Listener<JSONArray> listener = new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try{
                    Log.i("RESPONSE", "keldi");
                    int leng=response.length();
                    if(leng>0){
                        helper.doClearAuthTask();
                        for(int i=0; i < leng; i++){
                            JSONObject jsonObject = response.getJSONObject(i);
                            int id = jsonObject.getInt("id");
                            String title=jsonObject.getString("title");
                            String text=jsonObject.getString("text");
                            String image=jsonObject.getString("img");
                            int parent_id=jsonObject.getInt("parent_id");
                            int rating=jsonObject.getInt("rating");
                            int comments=jsonObject.getInt("comments");
                            int reports=jsonObject.getInt("reports");

                            Authority authority = new Authority(id, title, text, image, parent_id, rating, comments, reports);
                            helper.insertAuthority(authority);
                            //authList.add(authority);

                            if(parent_id==0){
                                parentMap.put(id,authority);
                            }
                            else{
                                childMap=(LinkedHashMap<Integer, Authority>)parentChildMap.get(parent_id);
                                if(childMap== null) {
                                    childMap=new LinkedHashMap<>();
                                }
                                childMap.put(id,authority);
                                parentChildMap.put(parent_id,childMap);
                            }
                        }
                    }
                    else{
                        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                        builder.setMessage(R.string.no_result).setNegativeButton(R.string.close,null).create().show();
                    }


                }catch(JSONException e){e.printStackTrace();}
                prepareAuthList();
                adapter.notifyDataSetChanged();
                pb.setVisibility(ProgressBar.GONE);
                ll_reload.setVisibility(View.GONE);
            }
        };
        Response.ErrorListener errorListener =new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                pb.setVisibility(ProgressBar.INVISIBLE);
                ll_reload.setVisibility(View.VISIBLE);
            }
        };

        JsonArrayRequest volReq = new JsonArrayRequest(Request.Method.GET, uri, null, listener,errorListener);


        MyVolley.getInstance(context).addToRequestQueue(volReq);
    }

    public void prepareAuthList(){
        authList.clear();
        HashMap<Integer,Authority> cMap;
        TreeMap<Integer,Integer> parentTreeMap=new TreeMap<>();
        TreeMap<Integer,Integer> childTreeMap=new TreeMap<>();
        int i=1;
        for (Map.Entry<Integer, Authority> entry : parentMap.entrySet())
        {
            int id=entry.getKey();
            int order=0; //you can put order number here
            order=(order*1000)+i;
            parentTreeMap.put(order,id);
            cMap=parentChildMap.get(id);
            if(cMap!=null){
                for (Map.Entry<Integer, Authority> childEntry : cMap.entrySet())
                {
                    int childId=childEntry.getKey();
                    int childOrder=0;
                    childOrder=(childOrder*1000)+i;
                    //Log.e("ChildId",childId+" order "+childOrder);
                    childTreeMap.put(childOrder,childId);
                    i++;
                }
            }
            i++;
        }
        //Log.e("ParentTree",parentTreeMap.toString());
        //Log.e("ChildTree",childTreeMap.toString());
        for (Map.Entry<Integer, Integer> entry : parentTreeMap.entrySet())
        {
            int id=entry.getValue();
            Authority voc=parentMap.get(id);
            //Log.e("ParentVoc",voc.getValue());
            authList.add(voc);
            cMap=parentChildMap.get(id);
            if(cMap!=null){
                for (Map.Entry<Integer, Integer> childEntry : childTreeMap.entrySet())
                {
                    int cid=childEntry.getValue();
                    //Log.e("CID",cid+"");
                    Authority childVoc=cMap.get(cid);
                    if(childVoc!=null){
                        //Log.e("ChildVoc",childVoc.getValue());
                        authList.add(childVoc);
                    }
                }
            }
        }
    }

    private class AuthorityTask extends AsyncTask<Void, Void, List<Authority>> {
        protected List<Authority> doInBackground(Void... params) {
            if(dbHandler==null){dbHandler = new MyDbHandler(context); Log.e(TAG, "AuthorityTask dbhandler was null");}
            if(db==null || !db.isOpen()){db = dbHandler.getWritableDatabase(); Log.e(TAG, "AuthorityTask db was null or not open");}

            return dbHandler.getAuthContents(db);
        }
        protected void onPostExecute(List<Authority> theList) {
            if(theList.size()>0){
                for (Authority authority : theList) {
                    int id=authority.getId();
                    int parent_id=authority.getParentId();
                    if(parent_id==0){
                        parentMap.put(id,authority);
                    }
                    else{
                        //Log.e(TAG, authority.getCommentCount()+" "+authority.getReportCount());
                        childMap=(LinkedHashMap<Integer, Authority>)parentChildMap.get(parent_id);
                        if(childMap== null) {
                            childMap=new LinkedHashMap<>();
                        }
                        childMap.put(id,authority);
                        parentChildMap.put(parent_id,childMap);
                    }
                }
                prepareAuthList();
                adapter.notifyDataSetChanged();
                Log.e(TAG, "data has been taken from DB");
                checkAuthDepend(); //also check if data has been altered on server side
            }
            else{
                Log.e("AuthorityTask", "no content in db, requesting server");
                populateList(); //requesting server
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //session.clear();
        if(db!=null && db.isOpen()){db.close();}
        RequestQueue queue = MyVolley.getInstance(context).getRequestQueue();
        queue.cancelAll(context);
    }
}
