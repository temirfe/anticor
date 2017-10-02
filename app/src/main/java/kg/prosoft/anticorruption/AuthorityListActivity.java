package kg.prosoft.anticorruption;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import kg.prosoft.anticorruption.service.Authority;
import kg.prosoft.anticorruption.service.AuthorityAdapter;
import kg.prosoft.anticorruption.service.Endpoints;
import kg.prosoft.anticorruption.service.MyVolley;

public class AuthorityListActivity extends AppCompatActivity {

    ListView listView;
    AuthorityAdapter adapter;
    List<Authority> authList;
    Context context;
    Activity activity;
    ProgressBar pb;
    Button btn_reload;
    LinearLayout ll_reload;
    ImageView iv_thumb;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authority_list);

        context=getApplicationContext();

        listView = (ListView) findViewById(R.id.id_lv_authorities);
        ll_reload=(LinearLayout)findViewById(R.id.id_ll_reload);
        btn_reload=(Button)findViewById(R.id.id_btn_reload);
        btn_reload.setOnClickListener(reloadClickListener);
        //iv_thumb=(ImageView)layout.findViewById(R.id.id_iv_thumb);

        pb = (ProgressBar)findViewById(R.id.progressBar1);

        authList = new ArrayList<>();
        adapter = new AuthorityAdapter(context,authList);
        listView.setAdapter(adapter);

        populateList();

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
            intent.putExtra("cat_id",item.getParentId());
            startActivity(intent);
        }
    };


    public void populateList(){

        String uri = Endpoints.AUTHORITIES;

        Response.Listener<JSONArray> listener = new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try{
                    Log.i("RESPONSE", "keldi");
                    int leng=response.length();
                    if(leng>0){
                        for(int i=0; i < leng; i++){
                            JSONObject jsonObject = response.getJSONObject(i);
                            int id = jsonObject.getInt("id");
                            String title=jsonObject.getString("title");
                            String text=jsonObject.getString("text");
                            String image=jsonObject.getString("img");
                            int parent_id=jsonObject.getInt("category_id");

                            Authority news = new Authority(id, title, text, image, parent_id);
                            authList.add(news);
                        }
                    }
                    else{
                        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                        builder.setMessage(R.string.no_result).setNegativeButton(R.string.close,null).create().show();
                    }


                }catch(JSONException e){e.printStackTrace();}

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
}
