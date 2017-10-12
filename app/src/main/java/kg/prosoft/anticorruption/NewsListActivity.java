package kg.prosoft.anticorruption;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import kg.prosoft.anticorruption.service.Endpoints;
import kg.prosoft.anticorruption.service.MyDbHandler;
import kg.prosoft.anticorruption.service.MyVolley;
import kg.prosoft.anticorruption.service.News;
import kg.prosoft.anticorruption.service.NewsAdapter;
import kg.prosoft.anticorruption.service.Vocabulary;

public class NewsListActivity extends BaseActivity {

    ListView listView;
    NewsAdapter adapter;
    ArrayList<News> newsList;
    private int page=1, current_page=1, total_pages;
    Uri.Builder uriB;
    ProgressBar pb;
    ProgressDialog progress;
    Button btn_reload;
    LinearLayout ll_reload;
    //ImageView iv_thumb;
    String TAG="NewsList";
    List<String> spinOptions;
    ArrayAdapter<String> dataAdapter;
    private HashMap<String, Integer> idMap;
    int ctg_id=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        listView = (ListView) findViewById(R.id.id_lv_news);
        listView.setOnScrollListener(onScrollDo);
        ll_reload=(LinearLayout)findViewById(R.id.id_ll_reload);
        btn_reload=(Button)findViewById(R.id.id_btn_reload);
        btn_reload.setOnClickListener(reloadClickListener);
        //iv_thumb=(ImageView)layout.findViewById(R.id.id_iv_thumb);
        idMap= new HashMap<>();

        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(onSpinSelect);
        // Spinner Drop down elements
        spinOptions = new ArrayList<>();
        spinOptions.add(getResources().getString(R.string.all_news));
        // Creating adapter for spinner
        dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, spinOptions);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);

        pb = (ProgressBar) findViewById(R.id.progressBar1);

        newsList = new ArrayList<>();
        adapter = new NewsAdapter(context,newsList);
        listView.setAdapter(adapter);

        new NewsTask().execute();
        new VocabularyTask().execute();

        listView.setOnItemClickListener(itemClickListener);
    }

    AdapterView.OnItemSelectedListener onSpinSelect = new AdapterView.OnItemSelectedListener(){
        public void onItemSelected(AdapterView<?> parent, View view,
                                   int pos, long id) {
            String selected=parent.getItemAtPosition(pos).toString();
            ((TextView) parent.getChildAt(0)).setTextColor(ContextCompat.getColor(context, R.color.white));
            int selected_id=0;
            if(pos!=0){selected_id=idMap.get(selected);}
            if(selected_id!=ctg_id){
                ctg_id=selected_id;
                Uri.Builder builder = new Uri.Builder();
                builder.scheme(Endpoints.SCHEME).authority(Endpoints.AUTHORITY).appendPath(Endpoints.API).appendPath("news");
                if(ctg_id!=0){
                    builder.appendQueryParameter("category_id", ""+ctg_id);
                }
                populateList(1,builder,true,false);
            }
        }

        public void onNothingSelected(AdapterView<?> parent) {}
    };

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
            News item =newsList.get(position);
            Intent intent = new Intent(context, NewsViewActivity.class);
            intent.putExtra("id",item.getId());
            intent.putExtra("title",item.getTitle());
            intent.putExtra("desc",item.getDescription());
            intent.putExtra("text",item.getText());
            intent.putExtra("date",item.getDate());
            intent.putExtra("image",item.getImage());
            intent.putExtra("cat_id",item.getCategoryId());
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
            uriB.scheme(Endpoints.SCHEME).authority(Endpoints.AUTHORITY).appendPath(Endpoints.API).appendPath("news");
        }
        Uri.Builder otherBuilder = Uri.parse(uriB.build().toString()).buildUpon();

        otherBuilder.appendQueryParameter("page", Integer.toString(page));

        String uri = otherBuilder.build().toString();

        Response.Listener<JSONArray> listener = new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try{
                    Log.i("RESPONSE", "keldi");
                    if(progress!=null){progress.dismiss();}
                    if(applyNewFilter || newlist){newsList.clear();}
                    int leng=response.length();
                    if(leng>0){
                        for(int i=0; i < leng; i++){
                            JSONObject jsonObject = response.getJSONObject(i);
                            int id = jsonObject.getInt("id");
                            String title=jsonObject.getString("title");
                            String description=jsonObject.getString("description");
                            String text=jsonObject.getString("text");
                            String date=jsonObject.getString("date");
                            String image=jsonObject.getString("img");
                            int category_id=jsonObject.getInt("category_id");
                            int views=jsonObject.getInt("views");

                            News news = new News(id, title, description, text, date, image, category_id,views);
                            newsList.add(news);
                        }
                        if(page==1 && newlist){
                            helper.doClearNewsTask();
                            helper.addNewsList(newsList);
                        }
                    }
                    else{
                        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                        builder.setMessage(R.string.no_result).setNegativeButton(R.string.close,null).create().show();
                    }


                }catch(JSONException e){e.printStackTrace();}

                if(applyNewFilter || newlist){
                    adapter = new NewsAdapter(context,newsList);
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
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    private class NewsTask extends AsyncTask<Void, Void, ArrayList<News>> {
        protected ArrayList<News> doInBackground(Void... params) {
            if(dbHandler==null){dbHandler = new MyDbHandler(context); Log.e(TAG, "NewsTask dbhandler was null");}
            if(db==null || !db.isOpen()){db = dbHandler.getWritableDatabase(); Log.e(TAG, "NewsTask db was null or not open");}

            return dbHandler.getNewsContents(db);
        }
        protected void onPostExecute(ArrayList<News> theList) {
            if(theList.size()>0){
                pb.setVisibility(ProgressBar.INVISIBLE);
                for (News news : theList) {
                    newsList.add(news);
                }
                adapter.notifyDataSetChanged();
                Log.e(TAG, "news data has been taken from DB");
                checkNewsDepend(); //also check if data has been altered on server side
            }
            else{
                Log.e("NewsTask", "no content in db, requesting server");
                populateList(1,null,false, true); //requesting server
            }
            //will load new data from server anyway
        }
    }

    public void checkNewsDepend(){
        String uri = Endpoints.NEWS_DEPEND;
        StringRequest volReq = new StringRequest(Request.Method.GET, uri,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        String depend=session.getNewsDepend();
                        response=response.replace("\"","");
                        Log.e(TAG, "depend: "+depend+" response: "+response);
                        if(!response.equals(depend)){
                            //new maxId is different, that mean category table has been altered. send new request.
                            populateList(1,null,false, true); //requesting server
                            session.setNewsDepend(response);
                        }
                        else{ Log.e(TAG, "depend matches");}
                    }
                }, null);

        MyVolley.getInstance(context).addToRequestQueue(volReq);
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
                    if(voc.getKey().equals("news_category")){
                        int id=voc.getId();
                        String value=voc.getValue();
                        idMap.put(value,id);
                        spinOptions.add(value);
                    }
                }
                dataAdapter.notifyDataSetChanged();
                Log.e(TAG, "voc data has been taken from DB");
            }
            else{
                Log.e("VocTask", "no content in db, requesting server");
                requestVocabularies(); //requesting server
            }
        }
    }

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
                        if(key.equals("news_category")){
                            spinOptions.add(value);
                            idMap.put(value,id);
                        }
                    }
                    dataAdapter.notifyDataSetChanged();
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
