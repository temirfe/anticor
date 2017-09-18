package kg.prosoft.anticorruption;

import android.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import kg.prosoft.anticorruption.service.Endpoints;
import kg.prosoft.anticorruption.service.MyVolley;
import kg.prosoft.anticorruption.service.SectorDialog;
import kg.prosoft.anticorruption.service.Vocabulary;

public class AddReportActivity extends BaseActivity implements SectorDialog.SectorDialogListener {

    private ArrayList<Vocabulary> vocList;
    private HashMap<Integer, String> titleMap, titleMapAuthority;
    private HashMap<Integer, Vocabulary> parentMap,childMap, parentMapAuthority, childMapAuthority;
    private HashMap<Integer, HashMap<Integer, Vocabulary>> parentChildMap, parentChildMapAuthority;
    TextView tv_sector, tv_city, tv_authority, tv_type;
    LinearLayout ll_sector;
    int selected_sector_id=0;
    int selected_city_id=0;
    int selected_authority_id=0;
    int selected_type_id=0;
    int DIALOG_SECTOR=0;
    int DIALOG_CITY=1;
    int DIALOG_AUTHORITY=2;
    int DIALOG_TYPE=3;
    int ACTIVE_DIALOG=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_report);

        parentMap=new HashMap<>();
        parentMapAuthority=new HashMap<>();
        childMap= new HashMap<>();
        childMapAuthority= new HashMap<>();
        titleMap= new HashMap<>();
        titleMapAuthority= new HashMap<>();
        parentChildMap=new HashMap<>();
        parentChildMapAuthority=new HashMap<>();
        requestVocabularies();
        tv_sector=(TextView)findViewById(R.id.id_tv_sector);
        tv_city=(TextView)findViewById(R.id.id_tv_city);
        tv_authority=(TextView)findViewById(R.id.id_tv_authority);
        tv_type=(TextView)findViewById(R.id.id_tv_type);
        ll_sector=(LinearLayout)findViewById(R.id.id_ll_sector);
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

    public void authorityClick(View v){
        prepareVocList("report_structure", true);
        Bundle args = new Bundle();
        args.putParcelableArrayList("list",vocList);
        args.putInt("selected",selected_authority_id);
        args.putString("title",getResources().getString(R.string.select_authority));
        ACTIVE_DIALOG=DIALOG_AUTHORITY;

        SectorDialog sdialog = new SectorDialog();
        sdialog.setArguments(args);
        sdialog.show(getFragmentManager(),"authorityDialog");
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

    // The dialog fragment receives a reference to this Activity through the
    // Fragment.onAttach() callback, which it uses to call the following methods
    // defined by the SectorDialog.SectorDialogListener interface
    @Override
    public void onDialogSelectClick(int id) {
        Log.e("CLICK RECEIVE",id+"");
        if(ACTIVE_DIALOG==DIALOG_SECTOR){
            String sector=titleMap.get(id);
            selected_sector_id=id;
            tv_sector.setText(sector);
            Log.e("CLICK RECEIVE TO","SECTOR");
        }
        else if(ACTIVE_DIALOG==DIALOG_CITY){
            Log.e("CLICK RECEIVE TO","city");
            String title=titleMap.get(id);
            selected_city_id=id;
            tv_city.setText(title);
        }
        else if(ACTIVE_DIALOG==DIALOG_AUTHORITY){
            Log.e("CLICK RECEIVE TO","authority");
            String title=titleMap.get(id);
            selected_authority_id=id;
            tv_authority.setText(title);
        }
        else if(ACTIVE_DIALOG==DIALOG_TYPE){
            Log.e("CLICK RECEIVE TO","type");
            String title=titleMap.get(id);
            selected_type_id=id;
            tv_type.setText(title);
        }
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

    public void requestVocabularies(){
        String uri = Endpoints.VOCABULARIES;
        Response.Listener<JSONArray> listener = new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray jsonArray) {

                Log.e(TAG, "response: " + jsonArray);
                try{
                    for(int s=0; s < jsonArray.length(); s++){
                        JSONObject jsonObject = jsonArray.getJSONObject(s);
                        int id=jsonObject.getInt("id");
                        String key=jsonObject.getString("key");
                        String value=jsonObject.getString("value");
                        int parent=jsonObject.getInt("parent");
                        int order=jsonObject.getInt("ordered_id");
                        titleMap.put(id,value);
                        if(parent==0){
                            parentMap.put(id,new Vocabulary(id,key,value,parent,order,false));
                        }
                        else{
                            childMap=parentChildMap.get(parent);
                            if(childMap== null) {
                                childMap=new HashMap<>();
                            }
                            childMap.put(id,new Vocabulary(id,key,value,parent,order,false));
                            parentChildMap.put(parent,childMap);
                        }
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

        MyVolley.getInstance(appContext).addToRequestQueue(volReq);
    }

    public void requestAuthorities(){
        String uri = Endpoints.AUTHORITIES;
        Response.Listener<JSONArray> listener = new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray jsonArray) {

                Log.e(TAG, "response: " + jsonArray);
                try{
                    for(int s=0; s < jsonArray.length(); s++){
                        JSONObject jsonObject = jsonArray.getJSONObject(s);
                        int id=jsonObject.getInt("id");
                        String value=jsonObject.getString("title");
                        int parent=jsonObject.getInt("category_id");
                        titleMap.put(id,value);
                        if(parent==0){
                            parentMap.put(id,new Vocabulary(id,"authority",value,parent,0,false));
                        }
                        else{
                            childMap=parentChildMap.get(parent);
                            if(childMap== null) {
                                childMap=new HashMap<>();
                            }
                            childMap.put(id,new Vocabulary(id,"authority",value,parent,0,false));
                            parentChildMap.put(parent,childMap);
                        }
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

        MyVolley.getInstance(appContext).addToRequestQueue(volReq);
    }
}
