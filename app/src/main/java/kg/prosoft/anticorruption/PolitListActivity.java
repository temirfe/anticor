package kg.prosoft.anticorruption;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
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

import kg.prosoft.anticorruption.service.Document;
import kg.prosoft.anticorruption.service.DocumentAdapter;
import kg.prosoft.anticorruption.service.Endpoints;
import kg.prosoft.anticorruption.service.MyVolley;

public class PolitListActivity extends BaseActivity {


    ListView listView;
    DocumentAdapter adapter;
    ArrayList<Document> newsList;
    private int page=1, current_page=1, total_pages;
    Uri.Builder uriB;
    ProgressBar pb;
    ProgressDialog progress;
    Button btn_reload;
    LinearLayout ll_reload;
    String TAG="PolitList";
    int ctg_id=0;
    String ctg_title, query, lang;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_polit_list);

        Intent intent = getIntent();
        ctg_id=intent.getIntExtra("id",0);
        ctg_title=intent.getStringExtra("title");
        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            if(ctg_title!=null){
                getSupportActionBar().setTitle(ctg_title);
            }
        }

        lang=session.getLanguage();
        if(lang.isEmpty()){lang="ky";}

        listView = (ListView) findViewById(R.id.id_lv_news);
        listView.setOnScrollListener(onScrollDo);
        ll_reload=(LinearLayout)findViewById(R.id.id_ll_reload);
        btn_reload=(Button)findViewById(R.id.id_btn_reload);
        btn_reload.setOnClickListener(reloadClickListener);


        pb = (ProgressBar) findViewById(R.id.progressBar1);

        newsList = new ArrayList<>();
        adapter = new DocumentAdapter(context,newsList);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(itemClickListener);

        query=intent.getStringExtra("query");
        if(query!=null){
            Uri.Builder builder = new Uri.Builder();
            builder.scheme(Endpoints.SCHEME).authority(Endpoints.AUTHORITY).appendPath(Endpoints.API).appendPath("politics");
            builder.appendQueryParameter("text", query);
            populateList(page,builder,false,false);
        }
        else{
            populateList(page,null,false,false);
        }
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
            Document item =newsList.get(position);
            Intent intent = new Intent(context, DocViewActivity.class);
            intent.putExtra("id",item.getId());
            intent.putExtra("title",item.getTitle());
            intent.putExtra("text",item.getText());
            intent.putExtra("date",item.getDate());
            intent.putExtra("cat_id",item.getCategoryId());
            intent.putExtra("cat_title",item.getCtgTitle());
            startActivity(intent);
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
            uriB.scheme(Endpoints.SCHEME).authority(Endpoints.AUTHORITY).appendPath(Endpoints.API).appendPath("politics");
        }
        Uri.Builder otherBuilder = Uri.parse(uriB.build().toString()).buildUpon();

        otherBuilder.appendQueryParameter("page", Integer.toString(page));
        if(ctg_id!=0){
            otherBuilder.appendQueryParameter("category_id", Integer.toString(ctg_id));
        }
        otherBuilder.appendQueryParameter("lang", lang);

        String uri = otherBuilder.build().toString();

        Response.Listener<JSONArray> listener = new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try{
                    Log.e("RESPONSE", "keldi");
                    if(progress!=null){progress.dismiss();}
                    if(applyNewFilter || newlist){newsList.clear();}
                    int leng=response.length();
                    if(leng>0){
                        for(int i=0; i < leng; i++){
                            JSONObject jsonObject = response.getJSONObject(i);
                            int id = jsonObject.getInt("id");
                            String title=jsonObject.getString("title");
                            String text=jsonObject.getString("text");
                            String date=jsonObject.getString("date");
                            int category_id=jsonObject.getInt("category_id");

                            Document news = new Document(id, title, text, date, category_id);
                            news.setCtgTitle(ctg_title);
                            newsList.add(news);
                        }
                    }
                    else{
                        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                        builder.setMessage(R.string.no_result).setNegativeButton(R.string.close,null).create().show();
                    }


                }catch(JSONException e){e.printStackTrace();}

                if(applyNewFilter || newlist){
                    adapter = new DocumentAdapter(context,newsList);
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
        super.onCreateOptionsMenu(menu);

        //search
        if(query!=null){
            searchView.post(new Runnable() {
                @Override
                public void run() {
                    MenuItemCompat.expandActionView(searchItem);
                    searchView.setQuery(query, false);
                }
            });
        }
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Uri.Builder builder = new Uri.Builder();
                builder.scheme(Endpoints.SCHEME).authority(Endpoints.AUTHORITY).appendPath(Endpoints.API).appendPath("politics");
                builder.appendQueryParameter("text", query);
                searchView.clearFocus();
                populateList(1,builder,true,false);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

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
