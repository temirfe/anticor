package kg.prosoft.anticorruption;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import kg.prosoft.anticorruption.service.Endpoints;
import kg.prosoft.anticorruption.service.MyDbHandler;
import kg.prosoft.anticorruption.service.MyHelper;
import kg.prosoft.anticorruption.service.MyVolley;
import kg.prosoft.anticorruption.service.News;
import kg.prosoft.anticorruption.service.NewsAdapter;
import kg.prosoft.anticorruption.service.SessionManager;


/**
 * A simple {@link Fragment} subclass.
 */
public class NewsFragment extends Fragment {
    ListView listView;
    NewsAdapter adapter;
    ArrayList<News> newsList;
    Context context;
    Activity activity;
    private int page=1;
    private int current_page=1;
    private int total_pages;
    Uri.Builder uriB;
    ProgressBar pb;
    ProgressDialog progress;
    Button btn_reload;
    LinearLayout ll_reload;
    //ImageView iv_thumb;

    SessionManager session;
    public SQLiteDatabase db;
    public MyDbHandler dbHandler;
    MyHelper helper;
    String TAG="NewsFrag";

    public NewsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View layout=inflater.inflate(R.layout.fragment_news, container, false);
        activity=getActivity();
        context=activity.getApplicationContext();
        session = new SessionManager(context);
        dbHandler = new MyDbHandler(context);
        db = dbHandler.getWritableDatabase();
        helper = new MyHelper(activity, dbHandler, db, session);

        listView = (ListView) layout.findViewById(R.id.id_lv_news);
        listView.setOnScrollListener(onScrollDo);
        ll_reload=(LinearLayout)layout.findViewById(R.id.id_ll_reload);
        btn_reload=(Button)layout.findViewById(R.id.id_btn_reload);
        btn_reload.setOnClickListener(reloadClickListener);
        //iv_thumb=(ImageView)layout.findViewById(R.id.id_iv_thumb);

        pb = (ProgressBar) layout.findViewById(R.id.progressBar1);

        newsList = new ArrayList<>();
        adapter = new NewsAdapter(context,newsList);
        listView.setAdapter(adapter);

        new NewsTask().execute();

        listView.setOnItemClickListener(itemClickListener);

        return layout;
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
                    progress = new ProgressDialog(getActivity());
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
            progress = new ProgressDialog(getActivity());
            progress.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
            progress.setMessage(getResources().getString(R.string.loading));
            progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
            progress.show();
        }

        uriB=urlB;

        if(uriB==null){
            uriB = new Uri.Builder();
            uriB.scheme(Endpoints.SCHEME).authority(Endpoints.AUTHORITY).appendPath("api").appendPath("news");
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
                        if(page==1){
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

}
