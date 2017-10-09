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
import java.util.HashMap;

import kg.prosoft.anticorruption.service.Endpoints;
import kg.prosoft.anticorruption.service.MyDbHandler;
import kg.prosoft.anticorruption.service.MyHelper;
import kg.prosoft.anticorruption.service.MyVolley;
import kg.prosoft.anticorruption.service.Report;
import kg.prosoft.anticorruption.service.ReportAdapter;
import kg.prosoft.anticorruption.service.SessionManager;


/**
 * A simple {@link Fragment} subclass.
 */
public class ListReportsFragment extends Fragment {

    ListView listView;
    ReportAdapter adapter;
    ArrayList<Report> reportList;
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

    SessionManager session;
    public SQLiteDatabase db;
    public MyDbHandler dbHandler;
    MyHelper helper;
    String TAG="ReportFrag";

    public ListReportsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View layout=inflater.inflate(R.layout.fragment_list_reports, container, false);
        activity=getActivity();
        context=activity.getApplicationContext();
        session = new SessionManager(context);
        dbHandler = new MyDbHandler(context);
        db = dbHandler.getWritableDatabase();
        helper = new MyHelper(activity, dbHandler, db, session);

        listView = (ListView) layout.findViewById(R.id.id_lv_reports);
        listView.setOnScrollListener(onScrollDo);
        ll_reload=(LinearLayout)layout.findViewById(R.id.id_ll_reload);
        btn_reload=(Button)layout.findViewById(R.id.id_btn_reload);
        btn_reload.setOnClickListener(reloadClickListener);
        //iv_thumb=(ImageView)layout.findViewById(R.id.id_iv_thumb);

        pb = (ProgressBar) layout.findViewById(R.id.progressBar1);

        reportList = new ArrayList<>();
        adapter = new ReportAdapter(context,reportList);
        listView.setAdapter(adapter);

        new ReportTask().execute();

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
            Report item =reportList.get(position);
            Intent intent = new Intent(context, ReportViewActivity.class);
            intent.putExtra("id",item.getId());
            intent.putExtra("title",item.getTitle());
            intent.putExtra("text",item.getText());
            intent.putExtra("date",item.getDate());
            intent.putExtra("lat",item.getLat());
            intent.putExtra("lng",item.getLng());
            intent.putExtra("city",item.getCityTitle());
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
            uriB.scheme(Endpoints.SCHEME).authority(Endpoints.AUTHORITY).appendPath("api").appendPath("reports");
        }
        Uri.Builder otherBuilder = Uri.parse(uriB.build().toString()).buildUpon();

        otherBuilder.appendQueryParameter("page", Integer.toString(page));

        String uri = otherBuilder.build().toString();

        Response.Listener<JSONArray> listener = new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try{
                    Log.i("RESPONSE", "reports keldi");
                    if(progress!=null){progress.dismiss();}
                    if(applyNewFilter || newlist){reportList.clear();}
                    int leng=response.length();
                    if(leng>0){
                        for(int i=0; i < leng; i++){
                            JSONObject jsonObject = response.getJSONObject(i);
                            int id = jsonObject.getInt("id");
                            String title=jsonObject.getString("title");
                            String text=jsonObject.getString("text");
                            String date=jsonObject.getString("date");
                            String city=jsonObject.getString("city_title");
                            int category_id=jsonObject.getInt("category_id");
                            double lat=jsonObject.getDouble("lat");
                            double lng=jsonObject.getDouble("lon");

                            Report report = new Report(id, title, text, date, lat, lng);
                            report.setCityTitle(city);
                            reportList.add(report);
                        }
                        if(page==1){
                            helper.doClearReportTask();
                            helper.addReportList(reportList);
                        }
                    }
                    else{
                        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                        builder.setMessage(R.string.no_result).setNegativeButton(R.string.close,null).create().show();
                    }


                }catch(JSONException e){e.printStackTrace();}

                if(applyNewFilter || newlist){
                    adapter = new ReportAdapter(context,reportList);
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

    private class ReportTask extends AsyncTask<Void, Void, ArrayList<Report>> {
        protected ArrayList<Report> doInBackground(Void... params) {
            if(dbHandler==null){dbHandler = new MyDbHandler(context); Log.e(TAG, "ReportTask dbhandler was null");}
            if(db==null || !db.isOpen()){db = dbHandler.getWritableDatabase(); Log.e(TAG, "ReportTask db was null or not open");}

            return dbHandler.getReportContents(db);
        }
        protected void onPostExecute(ArrayList<Report> theList) {
            if(theList.size()>0){
                pb.setVisibility(ProgressBar.INVISIBLE);
                for (Report report : theList) {
                    reportList.add(report);
                }
                adapter.notifyDataSetChanged();
                Log.e(TAG, "report data has been taken from DB");
                checkReportDepend(); //also check if data has been altered on server side
            }
            else{
                Log.e("ReportTask", "no content in db, requesting server");
                populateList(1,null,false, true); //requesting server
            }
            //will load new data from server anyway
        }
    }

    public void checkReportDepend(){
        String uri = Endpoints.REPORT_DEPEND;
        StringRequest volReq = new StringRequest(Request.Method.GET, uri,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        String depend=session.getReportDepend();
                        response=response.replace("\"","");
                        Log.e(TAG, "depend: "+depend+" response: "+response);
                        if(!response.equals(depend)){
                            //new maxId is different, that mean category table has been altered. send new request.
                            populateList(1,null,false, true); //requesting server
                            session.setReportDepend(response);
                        }
                        else{ Log.e(TAG, "depend matches");}
                    }
                }, null);

        MyVolley.getInstance(context).addToRequestQueue(volReq);
    }
}
