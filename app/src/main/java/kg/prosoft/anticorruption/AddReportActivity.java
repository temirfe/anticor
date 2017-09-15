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

import kg.prosoft.anticorruption.service.Endpoints;
import kg.prosoft.anticorruption.service.MyVolley;
import kg.prosoft.anticorruption.service.SectorDialog;
import kg.prosoft.anticorruption.service.Vocabulary;

public class AddReportActivity extends BaseActivity implements SectorDialog.SectorDialogListener {

    private SparseArray<Vocabulary> vocList;
    private HashMap<Integer,String> sectorMap, cityMap;
    TextView tv_sector, tv_city;
    LinearLayout ll_sector;
    int selected_sector_id=0;
    int selected_city_id=0;
    int DIALOG_SECTOR=0;
    int DIALOG_CITY=1;
    int ACTIVE_DIALOG=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_report);

        vocList=new SparseArray<>();
        sectorMap=new HashMap<>();
        cityMap=new HashMap<>();
        requestVocabularies();
        tv_sector=(TextView)findViewById(R.id.id_tv_sector);
        tv_city=(TextView)findViewById(R.id.id_tv_city);
        ll_sector=(LinearLayout)findViewById(R.id.id_ll_sector);
    }

    public void sectorClick(View v){
        Bundle args = new Bundle();
        args.putSerializable("hashmap",sectorMap);
        args.putInt("selected",selected_sector_id);
        ACTIVE_DIALOG=DIALOG_SECTOR;

        SectorDialog sdialog = new SectorDialog();
        sdialog.setArguments(args);
        sdialog.show(getFragmentManager(),"sector");
    }

    public void authorityClick(View v){
        Bundle args = new Bundle();
        args.putSerializable("hashmap",cityMap);
        args.putInt("selected",selected_city_id);
        /*ACTIVE_DIALOG=DIALOG_CITY;

        SectorDialog sdialog = new SectorDialog();
        sdialog.setArguments(args);
        sdialog.show(getFragmentManager(),"cityDialog");*/
    }

    public void cityClick(View v){
        Bundle args = new Bundle();
        args.putSerializable("hashmap",cityMap);
        args.putInt("selected",selected_city_id);
        ACTIVE_DIALOG=DIALOG_CITY;

        SectorDialog sdialog = new SectorDialog();
        sdialog.setArguments(args);
        sdialog.show(getFragmentManager(),"cityDialog");
    }

    // The dialog fragment receives a reference to this Activity through the
    // Fragment.onAttach() callback, which it uses to call the following methods
    // defined by the SectorDialog.SectorDialogListener interface
    @Override
    public void onDialogSelectClick(int id) {
        Log.e("CLICK RECEIVE",id+"");
        if(ACTIVE_DIALOG==DIALOG_SECTOR){
            String sector=sectorMap.get(id);
            selected_sector_id=id;
            tv_sector.setText(sector);
            Log.e("CLICK RECEIVE TO","SECTOR");
        }
        else if(ACTIVE_DIALOG==DIALOG_CITY){
            Log.e("CLICK RECEIVE TO","city");
            String title=cityMap.get(id);
            selected_city_id=id;
            tv_city.setText(title);
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
                        vocList.put(s,new Vocabulary(id,key,value));
                        if(key.equals("report_category")){
                            sectorMap.put(id,value);
                        }
                        else if(key.equals("city")){
                            cityMap.put(id,value);
                        }
                        //Log.e(TAG, "img: " + img);
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
