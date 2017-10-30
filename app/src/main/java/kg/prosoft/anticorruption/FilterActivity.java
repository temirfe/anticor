package kg.prosoft.anticorruption;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.NetworkResponse;
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
import kg.prosoft.anticorruption.service.Endpoints;
import kg.prosoft.anticorruption.service.MyDbHandler;
import kg.prosoft.anticorruption.service.MyVolley;
import kg.prosoft.anticorruption.service.SectorDialog;
import kg.prosoft.anticorruption.service.Vocabulary;

public class FilterActivity extends BaseActivity implements SectorDialog.SectorDialogListener {
    String TAG="FilterAct";
    Button id_btn_apply;
    int DIALOG_SECTOR=0;
    int DIALOG_CITY=1;
    int DIALOG_AUTHORITY=2;
    int DIALOG_TYPE=3;
    int ACTIVE_DIALOG=0;
    private ArrayList<Vocabulary> vocList;
    private ArrayList<Authority> authList;
    private HashMap<Integer, String> titleMap, titleMapAuth;
    private HashMap<Integer, Vocabulary> parentMap,childMap;
    private LinkedHashMap<Integer, Authority> parentMapAuth,childMapAuth;
    private HashMap<Integer, HashMap<Integer, Vocabulary>> parentChildMap;
    private HashMap<Integer, HashMap<Integer, Authority>> parentChildMapAuth;
    TextView tv_sector, tv_city, tv_authority, tv_type,tv_user;
    ImageView iv_clear_city,iv_clear_sector,iv_clear_authority,iv_clear_type, iv_clear_search,iv_clear_user;
    EditText et_search;
    Intent received_intent;
    String query, username;
    int selected_sector_id,selected_city_id,selected_authority_id,selected_type_id, selected_user_id;
    boolean empty=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);
        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        activity=this;

        parentMap=new HashMap<>();
        childMap= new HashMap<>();
        titleMap= new HashMap<>();
        parentChildMap=new HashMap<>();

        parentMapAuth=new LinkedHashMap<>();
        childMapAuth= new LinkedHashMap<>();
        titleMapAuth= new HashMap<>();
        parentChildMapAuth=new HashMap<>();

        tv_sector=(TextView)findViewById(R.id.id_tv_sector);
        tv_city=(TextView)findViewById(R.id.id_tv_city);
        tv_authority=(TextView)findViewById(R.id.id_tv_authority);
        tv_type=(TextView)findViewById(R.id.id_tv_type);
        tv_type=(TextView)findViewById(R.id.id_tv_type);
        tv_user=(TextView)findViewById(R.id.id_tv_user);
        id_btn_apply=(Button)findViewById(R.id.id_btn_apply);
        iv_clear_city=(ImageView)findViewById(R.id.id_iv_clear_city);
        iv_clear_sector=(ImageView)findViewById(R.id.id_iv_clear_sector);
        iv_clear_authority=(ImageView)findViewById(R.id.id_iv_clear_authority);
        iv_clear_type=(ImageView)findViewById(R.id.id_iv_clear_type);
        iv_clear_search=(ImageView)findViewById(R.id.id_iv_clear_search);
        iv_clear_user=(ImageView)findViewById(R.id.id_iv_clear_user);
        et_search=(EditText)findViewById(R.id.id_et_search);

        received_intent=getIntent();
        query=received_intent.getStringExtra("query");
        username=received_intent.getStringExtra("username");
        selected_sector_id=received_intent.getIntExtra("sector_id",0);
        selected_authority_id=received_intent.getIntExtra("authority_id",0);
        selected_type_id=received_intent.getIntExtra("type_id",0);
        selected_city_id=received_intent.getIntExtra("city_id",0);
        selected_user_id=received_intent.getIntExtra("user_id",0);

        if(selected_user_id!=0){
            String user=getResources().getString(R.string.user);
            user=user+": "+username;
            tv_user.setText(user);
            tv_user.setVisibility(View.VISIBLE);
            iv_clear_user.setVisibility(View.VISIBLE);
        }

        et_search.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {}

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                if(count==1 && start==0 && before==0){
                    //first letter typed
                    iv_clear_search.setVisibility(View.VISIBLE);
                    id_btn_apply.setVisibility(View.VISIBLE);
                }
                else if(count==0 && start==0 && before==1){
                    //last letter removed
                    iv_clear_search.setVisibility(View.GONE);
                }
            }
        });

        new AuthorityTask().execute();
        new VocabularyTask().execute();

        restoreFilter();
    }


    public void clearUser(View v){
        selected_user_id=0;
        tv_user.setVisibility(View.GONE);
        v.setVisibility(View.GONE);
        id_btn_apply.setVisibility(View.VISIBLE);
    }

    public void clearSearch(View v){
        et_search.setText("");
        v.setVisibility(View.GONE);
    }

    public void sectorClick(View v){
        ACTIVE_DIALOG=DIALOG_SECTOR;
        prepareVocList("report_category", false);
        Bundle args = new Bundle();
        args.putParcelableArrayList("list",vocList);
        args.putInt("selected",selected_sector_id);
        args.putString("title",getResources().getString(R.string.select_sector));

        SectorDialog sdialog = new SectorDialog();
        sdialog.setArguments(args);
        sdialog.show(getFragmentManager(),"sector");
    }
    public void clearSector(View v){
        selected_sector_id=0;
        tv_sector.setText(R.string.select_sector);
        v.setVisibility(View.GONE);
    }

    public void authorityClick(View v){
        Bundle args = new Bundle();
        args.putParcelableArrayList("list",authList);
        args.putInt("selected",selected_authority_id);
        args.putString("title",getResources().getString(R.string.select_authority));
        args.putString("type","authority");
        ACTIVE_DIALOG=DIALOG_AUTHORITY;

        SectorDialog sdialog = new SectorDialog();
        sdialog.setArguments(args);
        sdialog.show(getFragmentManager(),"authorityDialog");
    }
    public void clearAuthority(View v){
        selected_authority_id=0;
        tv_authority.setText(R.string.select_authority);
        v.setVisibility(View.GONE);
    }

    public void cityClick(View v){
        ACTIVE_DIALOG=DIALOG_CITY;
        prepareVocList("city", true);
        //Log.e("THE LIST",vocList+"");
        Bundle args = new Bundle();
        args.putParcelableArrayList("list",vocList);
        args.putInt("selected",selected_city_id);
        args.putString("title",getResources().getString(R.string.select_city));

        SectorDialog sdialog = new SectorDialog();
        sdialog.setArguments(args);
        sdialog.show(getFragmentManager(),"cityDialog");
    }
    public void clearCity(View v){
        selected_city_id=0;
        tv_city.setText(R.string.select_city);
        v.setVisibility(View.GONE);
    }

    public void typeClick(View v){
        ACTIVE_DIALOG=DIALOG_TYPE;
        prepareVocList("report_type", false);
        Bundle args = new Bundle();
        args.putParcelableArrayList("list",vocList);
        args.putInt("selected",selected_type_id);
        args.putString("title",getResources().getString(R.string.select_type));

        SectorDialog sdialog = new SectorDialog();
        sdialog.setArguments(args);
        sdialog.show(getFragmentManager(),"typeDialog");
    }
    public void clearType(View v){
        selected_type_id=0;
        tv_type.setText(R.string.select_type);
        v.setVisibility(View.GONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.findItem(R.id.action_search).setVisible(false);
        menu.add(0,2,0,R.string.reset).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
            case 2:
                reset();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void reset(){
        id_btn_apply.setVisibility(View.VISIBLE);
        et_search.setText("");
        findViewById(R.id.id_rl_filter).requestFocus(); //so that et_search looses focus

        selected_sector_id=0;
        tv_sector.setText(R.string.select_sector);
        selected_authority_id=0;
        tv_authority.setText(R.string.select_authority);
        selected_city_id=0;
        tv_city.setText(R.string.select_city);
        selected_type_id=0;
        tv_type.setText(R.string.select_type);
        selected_user_id=0;

        iv_clear_sector.setVisibility(View.GONE);
        iv_clear_authority.setVisibility(View.GONE);
        iv_clear_city.setVisibility(View.GONE);
        iv_clear_type.setVisibility(View.GONE);
        iv_clear_search.setVisibility(View.GONE);
        tv_user.setVisibility(View.GONE);
        iv_clear_user.setVisibility(View.GONE);
    }

    public void applyFilter(View v){
        query=et_search.getText().toString();
        boolean emptyFilter=true;
        Intent intent= new Intent();
        intent.putExtra("query", query);
        intent.putExtra("sector_id", selected_sector_id);
        intent.putExtra("authority_id", selected_authority_id);
        intent.putExtra("type_id", selected_type_id);
        intent.putExtra("city_id", selected_city_id);
        intent.putExtra("user_id", selected_user_id);
        if(query!=null && !query.isEmpty() || selected_sector_id!=0 || selected_authority_id!=0
                || selected_type_id!=0 || selected_city_id!=0 || selected_user_id!=0){emptyFilter=false;}
        intent.putExtra("empty", emptyFilter);
        setResult(RESULT_OK, intent);
        finish();
    }

    //restore filter when reopened
    public void restoreFilter(){
        if(query!=null && !query.isEmpty()){
            et_search.setText(query);
            iv_clear_search.setVisibility(View.VISIBLE);
            empty=false;
        }
        if(selected_sector_id!=0){
            String sector=titleMap.get(selected_sector_id);
            if(sector!=null){
                tv_sector.setText(sector);
                iv_clear_sector.setVisibility(View.VISIBLE);
            }
            empty=false;
        }
        if(selected_authority_id!=0){
            String title=titleMapAuth.get(selected_authority_id);
            if(title!=null){
                tv_authority.setText(title);
                iv_clear_authority.setVisibility(View.VISIBLE);
            }
            empty=false;
        }
        if(selected_type_id!=0){
            String title=titleMap.get(selected_type_id);
            if(title!=null){
                tv_type.setText(title);
                iv_clear_type.setVisibility(View.VISIBLE);
            }
            empty=false;
        }
        if(selected_city_id!=0){
            String title=titleMap.get(selected_city_id);
            if(title!=null){
                tv_city.setText(title);
                iv_clear_city.setVisibility(View.VISIBLE);
            }
            empty=false;
        }
        if(!empty){id_btn_apply.setVisibility(View.VISIBLE);}
    }

    /** Vocabulary **/
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
                        titleMap.put(id,value);
                        if(parent==0){
                            parentMap.put(id,voc);
                        }
                        else{
                            childMap=parentChildMap.get(parent);
                            if(childMap== null) {
                                childMap=new HashMap<>();
                            }
                            childMap.put(id,voc);
                            parentChildMap.put(parent,childMap);
                        }
                    }
                    restoreFilter();
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
    public void prepareVocList(String type, boolean hasChildren){
        vocList=new ArrayList<>();
        HashMap<Integer,Vocabulary> cMap;
        TreeMap<Integer,Integer> parentTreeMap=new TreeMap<>();
        TreeMap<Integer,Integer> childTreeMap=new TreeMap<>();
        int i=1;
        for (Map.Entry<Integer, Vocabulary> entry : parentMap.entrySet())
        {
            Vocabulary voc=entry.getValue();
            if(type.equals(voc.getKey())){
                int id=entry.getKey();
                int order=voc.getOrder();
                order=(order*1000)+i;
                parentTreeMap.put(order,id);
                cMap=parentChildMap.get(id);
                if(cMap!=null){
                    for (Map.Entry<Integer, Vocabulary> childEntry : cMap.entrySet())
                    {
                        Vocabulary childVoc=childEntry.getValue();
                        if(type.equals(childVoc.getKey())){
                            int childId=childEntry.getKey();
                            int childOrder=childVoc.getOrder();
                            childOrder=(childOrder*1000)+i;
                            //Log.e("ChildId",childId+" order "+childOrder);
                            childTreeMap.put(childOrder,childId);
                            i++;
                        }
                    }
                }
                i++;
            }
        }
        //Log.e("ParentTree",parentTreeMap.toString());
        //Log.e("ChildTree",childTreeMap.toString());
        for (Map.Entry<Integer, Integer> entry : parentTreeMap.entrySet())
        {
            int id=entry.getValue();
            Vocabulary voc=parentMap.get(id);
            if(hasChildren){voc.setHasChildren(true);}
            //Log.e("ParentVoc",voc.getValue());
            vocList.add(voc);
            cMap=parentChildMap.get(id);
            if(cMap!=null){
                for (Map.Entry<Integer, Integer> childEntry : childTreeMap.entrySet())
                {
                    int cid=childEntry.getValue();
                    //Log.e("CID",cid+"");
                    Vocabulary childVoc=cMap.get(cid);
                    if(childVoc!=null){
                        //Log.e("ChildVoc",childVoc.getValue());
                        if(hasChildren){childVoc.setHasChildren(true);}
                        vocList.add(childVoc);
                    }
                }
            }
        }
    }
    private class VocabularyTask extends AsyncTask<Void, Void, List<Vocabulary>> {
        protected List<Vocabulary> doInBackground(Void... params) {
            if(dbHandler==null){dbHandler = new MyDbHandler(context); Log.e(TAG, "VocabularyTask dbhandler was null");}
            if(db==null || !db.isOpen()){db = dbHandler.getWritableDatabase(); Log.e(TAG, "VocabularyTask db was null or not open");}

            return dbHandler.getVocContents(db);
        }
        protected void onPostExecute(List<Vocabulary> theList) {
            if(theList.size()>0){
                for (Vocabulary voc : theList) {
                    int id=voc.getId();
                    String value=voc.getValue();
                    int parent=voc.getParent();
                    titleMap.put(id,value);
                    if(parent==0){
                        parentMap.put(id,voc);
                    }
                    else{
                        childMap=parentChildMap.get(parent);
                        if(childMap== null) {
                            childMap=new HashMap<>();
                        }
                        childMap.put(id,voc);
                        parentChildMap.put(parent,childMap);
                    }
                }
                restoreFilter();
                Log.e(TAG, "voc data has been taken from DB");
            }
            else{
                Log.e("VocTask", "no content in db, requesting server");
                requestVocabularies(); //requesting server
            }
        }
    }


    /** Authority **/
    public void requestAuthority(){
        String uri = Endpoints.AUTHORITIES;

        Response.Listener<JSONArray> listener = new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try{
                    Log.i("AUTH RESPONSE", "keldi");
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

                            Authority authForDialog = new Authority(id, title, image, parent_id);
                            Authority authForDb = new Authority(id, title, text, image, parent_id,rating,comments,reports);
                            helper.insertAuthority(authForDb);
                            //authList.add(authority);
                            titleMapAuth.put(id,title);
                            if(parent_id==0){
                                parentMapAuth.put(id,authForDialog);
                            }
                            else{
                                childMapAuth=(LinkedHashMap<Integer, Authority>)parentChildMapAuth.get(parent_id);
                                if(childMapAuth== null) {
                                    childMapAuth=new LinkedHashMap<>();
                                }
                                childMapAuth.put(id,authForDialog);
                                parentChildMapAuth.put(parent_id,childMapAuth);
                            }
                        }
                        restoreFilter();
                    }
                    else{
                        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                        builder.setMessage(R.string.no_result).setNegativeButton(R.string.close,null).create().show();
                    }

                }catch(JSONException e){e.printStackTrace();}
                prepareAuthList();
            }
        };
        Response.ErrorListener errorListener =new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse networkResponse = error.networkResponse;
                Log.e(TAG, "Volley error: " + error.getMessage() + ", code: " + networkResponse);
            }
        };

        JsonArrayRequest volReq = new JsonArrayRequest(Request.Method.GET, uri, null, listener,errorListener);
        MyVolley.getInstance(context).addToRequestQueue(volReq);
    }
    public void prepareAuthList(){
        authList=new ArrayList<>();
        HashMap<Integer,Authority> cMap;
        TreeMap<Integer,Integer> parentTreeMap=new TreeMap<>();
        TreeMap<Integer,Integer> childTreeMap=new TreeMap<>();
        int i=1;
        for (Map.Entry<Integer, Authority> entry : parentMapAuth.entrySet())
        {
            int id=entry.getKey();
            int order=0; //you can put order number here
            order=(order*1000)+i;
            parentTreeMap.put(order,id);
            cMap=parentChildMapAuth.get(id);
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
            Authority voc=parentMapAuth.get(id);
            //Log.e("ParentVoc",voc.getValue());
            authList.add(voc);
            cMap=parentChildMapAuth.get(id);
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
                    String title=authority.getTitle();
                    titleMapAuth.put(id,title);
                    if(parent_id==0){
                        parentMapAuth.put(id,authority);
                    }
                    else{
                        childMapAuth=(LinkedHashMap<Integer, Authority>)parentChildMapAuth.get(parent_id);
                        if(childMapAuth== null) {
                            childMapAuth=new LinkedHashMap<>();
                        }
                        childMapAuth.put(id,authority);
                        parentChildMapAuth.put(parent_id,childMapAuth);
                    }
                }
                prepareAuthList();
                restoreFilter();
                Log.e(TAG, "auth data has been taken from DB");
            }
            else{
                Log.e("AuthorityTask", "no content in db, requesting server");
                requestAuthority(); //requesting server
            }
        }
    }

    // The dialog fragment receives a reference to this Activity through the
    // Fragment.onAttach() callback, which it uses to call the following methods
    // defined by the SectorDialog.SectorDialogListener interface
    @Override
    public void onDialogSelectClick(int id) {
        id_btn_apply.setVisibility(View.VISIBLE);
        if(ACTIVE_DIALOG==DIALOG_SECTOR){
            String sector=titleMap.get(id);
            selected_sector_id=id;
            tv_sector.setText(sector);
            iv_clear_sector.setVisibility(View.VISIBLE);
        }
        else if(ACTIVE_DIALOG==DIALOG_CITY){
            String title=titleMap.get(id);
            selected_city_id=id;
            tv_city.setText(title);
            iv_clear_city.setVisibility(View.VISIBLE);
        }
        else if(ACTIVE_DIALOG==DIALOG_AUTHORITY){
            String title=titleMapAuth.get(id);
            selected_authority_id=id;
            tv_authority.setText(title);
            iv_clear_authority.setVisibility(View.VISIBLE);
        }
        else if(ACTIVE_DIALOG==DIALOG_TYPE){
            String title=titleMap.get(id);
            selected_type_id=id;
            tv_type.setText(title);
            iv_clear_type.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
