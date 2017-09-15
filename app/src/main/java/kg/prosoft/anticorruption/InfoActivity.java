package kg.prosoft.anticorruption;

import android.app.ListActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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

import kg.prosoft.anticorruption.service.Endpoints;
import kg.prosoft.anticorruption.service.Info;
import kg.prosoft.anticorruption.service.InfoAdapter;
import kg.prosoft.anticorruption.service.MyVolley;

public class InfoActivity extends AppCompatActivity {

    ListView listView;
    ArrayList<Info> infoList;
    InfoAdapter listAdapter;
    String TAG="InfoActivity";
    ProgressBar pbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(R.string.info);
        }
        pbar=(ProgressBar)findViewById(R.id.id_pbar);
        infoList=new ArrayList<>();

        listView = (ListView) findViewById(R.id.listView);
        listAdapter = new InfoAdapter(this, infoList);
        listView.setAdapter(listAdapter);
        requestPages();
    }


    public void requestPages(){
        String uri = Endpoints.PAGES;
        Response.Listener<JSONArray> listener = new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray jsonArray) {

                Log.e(TAG, "response: " + jsonArray);
                try{
                    for(int s=0; s < jsonArray.length(); s++){
                        JSONObject bannerObject = jsonArray.getJSONObject(s);
                        int id=bannerObject.getInt("id");
                        String title=bannerObject.getString("title");
                        String text=bannerObject.getString("text");
                        infoList.add(new Info(id,title,text));
                    }
                    listAdapter.notifyDataSetChanged();
                    pbar.setVisibility(View.GONE);
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

        MyVolley.getInstance(getApplicationContext()).addToRequestQueue(volReq);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                //NavUtils.navigateUpFromSameTask(this);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
