package kg.prosoft.anticorruption;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import kg.prosoft.anticorruption.service.Authority;
import kg.prosoft.anticorruption.service.Comment;
import kg.prosoft.anticorruption.service.CommentAdapter;
import kg.prosoft.anticorruption.service.Endpoints;
import kg.prosoft.anticorruption.service.MyDbHandler;
import kg.prosoft.anticorruption.service.MyVolley;
import kg.prosoft.anticorruption.service.Vocabulary;

public class CommentActivity extends BaseActivity {

    ListView listView;
    CommentAdapter adapter;
    ArrayList<Comment> newsList;
    private int page=1, current_page=1, total_pages;
    Uri.Builder uriB;
    ProgressBar pb;
    ProgressDialog progress;
    Button btn_reload;
    LinearLayout ll_reload;
    String TAG="CommentList";
    int user_id;
    String auth_key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);
        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(R.string.comments);
        }

        listView = (ListView) findViewById(R.id.id_lv_comments);
        listView.setOnScrollListener(onScrollDo);
        ll_reload=(LinearLayout)findViewById(R.id.id_ll_reload);
        btn_reload=(Button)findViewById(R.id.id_btn_reload);
        btn_reload.setOnClickListener(reloadClickListener);

        pb = (ProgressBar) findViewById(R.id.progressBar1);

        newsList = new ArrayList<>();
        adapter = new CommentAdapter(context,newsList);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(itemClickListener);
        Intent gotIntent=getIntent();
        user_id=gotIntent.getIntExtra("user_id",0);
        if(user_id==session.getUserId()){
            auth_key=session.getAccessToken();
            Log.e(TAG, "token: "+auth_key);
        }

        populateList(page, null, false, false);
    }

    View.OnClickListener reloadClickListener = new View.OnClickListener(){
        public void onClick(View v){
            populateList(page, null,false, false);
            pb.setVisibility(ProgressBar.VISIBLE);
            ll_reload.setVisibility(View.GONE);
        }
    };

    AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener(){
        public void onItemClick(AdapterView<?> listView,
                                View itemView,
                                int position,
                                long id) {
            Comment comment =newsList.get(position);
            String model=comment.getModel();
            int model_id=comment.getModelId();
            Intent intent;
            if(model.equals("news")){
                intent = new Intent(context, NewsViewActivity.class);
                intent.putExtra("id",model_id);
                startActivity(intent);
            }
            else if(model.equals("report")){
                intent = new Intent(context, ReportViewActivity.class);
                intent.putExtra("id",model_id);
                startActivity(intent);
            }
        }
    };

    AbsListView.OnScrollListener onScrollDo = new AbsListView.OnScrollListener() {
        private int currentVisibleItemCount;
        private int currentScrollState;
        private int currentFirstVisibleItem;
        private int totalItem;


        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            this.currentScrollState = scrollState;
            this.isScrollCompleted();
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,
                             int visibleItemCount, int totalItemCount) {
            this.currentFirstVisibleItem = firstVisibleItem;
            this.currentVisibleItemCount = visibleItemCount;
            this.totalItem = totalItemCount;
        }

        private void isScrollCompleted() {

            int threshold=totalItem-(currentFirstVisibleItem+currentVisibleItemCount);

            if(threshold<=2 && this.currentScrollState == SCROLL_STATE_IDLE){
                if(current_page<total_pages){
                    //Log.i("Threshold reached", "loading next. current:"+current_page+" total:"+total_pages);
                    int next_page=current_page+1;
                    populateList(next_page, uriB, false, false);
                }
            }

            if (totalItem - currentFirstVisibleItem == currentVisibleItemCount
                    && this.currentScrollState == SCROLL_STATE_IDLE) {
                Log.e("END of Current", "reached current:"+current_page+" total:"+total_pages);
                if(current_page<total_pages){
                    progress = new ProgressDialog(activity);
                    progress.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
                    progress.setMessage(getResources().getString(R.string.loading));
                    progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
                    progress.show();
                }
            }
        }
    };

    public void populateList(final int page,Uri.Builder urlB, final boolean applyNewFilter, final boolean newlist){
        if(applyNewFilter){
            progress = new ProgressDialog(activity);
            progress.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
            progress.setMessage(getResources().getString(R.string.loading));
            progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
            progress.show();
        }

        uriB=urlB;

        if(uriB==null){
            uriB = new Uri.Builder();
            uriB.scheme(Endpoints.SCHEME).authority(Endpoints.AUTHORITY).appendPath(Endpoints.API).appendPath("comments");
        }
        Uri.Builder otherBuilder = Uri.parse(uriB.build().toString()).buildUpon();

        otherBuilder.appendQueryParameter("user_id", Integer.toString(user_id));
        if(auth_key!=null && !auth_key.isEmpty()){
            otherBuilder.appendQueryParameter("auth_key", auth_key);
        }
        otherBuilder.appendQueryParameter("page", Integer.toString(page));

        String uri = otherBuilder.build().toString();

        Response.Listener<JSONArray> listener = new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try{
                    Log.e("RESPONSE", "keldi"+response);
                    if(progress!=null){progress.dismiss();}
                    if(applyNewFilter || newlist){newsList.clear();}
                    int leng=response.length();
                    if(leng>0){
                        for(int i=0; i < leng; i++){
                            JSONObject jsonObject = response.getJSONObject(i);
                            int id = jsonObject.getInt("id");
                            String text=jsonObject.getString("text");
                            String date=jsonObject.getString("date");
                            int model_id=jsonObject.getInt("model_id");
                            String model=jsonObject.getString("model");
                            String model_title=jsonObject.getString("model_title");

                            Comment news = new Comment(id, text, date, model_id, model, model_title);
                            newsList.add(news);
                        }
                    }
                    else{
                        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                        builder.setMessage(R.string.no_result).setNegativeButton(R.string.close,null).create().show();
                    }


                }catch(JSONException e){e.printStackTrace();}

                if(applyNewFilter){
                    adapter = new CommentAdapter(context,newsList);
                    listView.setAdapter(adapter);
                }
                else{
                    adapter.notifyDataSetChanged();
                }
                pb.setVisibility(ProgressBar.INVISIBLE);
                ll_reload.setVisibility(View.GONE);
            }
        };
        Response.ErrorListener errorListener =new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG,"onError");
                if(progress!=null){progress.dismiss();}
                pb.setVisibility(ProgressBar.INVISIBLE);
                ll_reload.setVisibility(View.VISIBLE);
            }
        };

        JsonArrayRequest volReq = new JsonArrayRequest(Request.Method.GET, uri, null, listener,errorListener){
            @Override
            protected Response<JSONArray> parseNetworkResponse(NetworkResponse response) {
                try {
                    current_page=Integer.parseInt(response.headers.get("X-Pagination-Current-Page"));
                    total_pages=Integer.parseInt(response.headers.get("X-Pagination-Page-Count"));
                    String jsonString = new String(response.data,
                            HttpHeaderParser.parseCharset(response.headers, PROTOCOL_CHARSET));
                    return Response.success(new JSONArray(jsonString),
                            HttpHeaderParser.parseCacheHeaders(response));
                } catch (UnsupportedEncodingException e) {
                    return Response.error(new ParseError(e));
                } catch (JSONException je) {
                    return Response.error(new ParseError(je));
                }
            }
        };

        MyVolley.getInstance(context).addToRequestQueue(volReq);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
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
}
