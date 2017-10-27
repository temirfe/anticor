package kg.prosoft.anticorruption;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import kg.prosoft.anticorruption.service.Endpoints;
import kg.prosoft.anticorruption.service.GlideApp;
import kg.prosoft.anticorruption.service.MyVolley;

public class ReportViewActivity extends AppCompatActivity {
    TextView tv_author, tv_title, tv_date, tv_text, tv_zero_comment, tv_city,tv_category,tv_authority,tv_type;
    public double lat=0,lng=0;
    public RelativeLayout rl_map, rl_pb;
    public LinearLayout ll_comments,ll_thumb_holder;
    int id,cat_id, authority_id,city_id,type_id;
    ArrayList<String> imageList = new ArrayList<>();
    Activity activity;
    Context context;
    boolean loadAll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_view);

        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        activity=this;
        context=getApplicationContext();

        tv_author=(TextView)findViewById(R.id.id_tv_author);
        tv_title=(TextView)findViewById(R.id.id_tv_title);
        tv_date=(TextView)findViewById(R.id.id_tv_date);
        tv_text=(TextView)findViewById(R.id.id_tv_text);
        tv_city=(TextView)findViewById(R.id.id_tv_city);
        tv_zero_comment=(TextView)findViewById(R.id.id_tv_comments_zero);
        tv_category=(TextView)findViewById(R.id.id_tv_category);
        tv_authority=(TextView)findViewById(R.id.id_tv_authority);
        tv_type=(TextView)findViewById(R.id.id_tv_type);
        //iv_image=(ImageView)findViewById(R.id.id_iv_img);
        ll_comments=(LinearLayout) findViewById(R.id.id_ll_comments);
        ll_thumb_holder=(LinearLayout) findViewById(R.id.id_ll_thumb_holder);
        rl_map=(RelativeLayout)findViewById(R.id.id_rl_map);
        rl_pb=(RelativeLayout)findViewById(R.id.id_rl_pb);

        Intent intent = getIntent();
        id=intent.getIntExtra("id",0);
        if(intent.hasExtra("title")) //if opened from commentActivity then intent has only id
        {
            if(intent.hasExtra("from")){
                String from=intent.getStringExtra("from");
                Log.e("RepView","from: "+from);
            }
            String title=intent.getStringExtra("title");
            String text=intent.getStringExtra("text");
            String date=intent.getStringExtra("date");
            String city_title=intent.getStringExtra("city");
            lat=intent.getDoubleExtra("lat",0);
            lng=intent.getDoubleExtra("lng",0);

            tv_title.setText(title);
            tv_date.setText(date);
            tv_city.setText(city_title);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                tv_text.setText(Html.fromHtml(text,Html.FROM_HTML_MODE_LEGACY));
            } else {
                tv_text.setText(Html.fromHtml(text));
            }


            if(lat>0 && lng>0){
                new android.os.Handler().postDelayed(
                        new Runnable() {
                            public void run() {
                                showMapFrame();
                            }
                        },
                        1000);
            }
            else{
                rl_map.setVisibility(View.GONE);
            }
            loadAll=false;
        }
        else{
            loadAll=true;
            rl_pb.setVisibility(View.VISIBLE);
        }

        //although we have info from intent, we make a request to get comments and
        //other info like authority title, etc.
        requestReport(id);
    }


    public void requestReport(final int id){
        String uri = Endpoints.REPORTS+"/"+id;

        Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                rl_pb.setVisibility(View.GONE);
                try{
                    if(jsonObject.has("comments")){
                        JSONArray comments = jsonObject.getJSONArray("comments");
                        for(int i=0; i < comments.length(); i++){
                            JSONObject comObj = comments.getJSONObject(i);
                            String com_name=comObj.getString("name");
                            String com_text=comObj.getString("text");
                            String com_date=comObj.getString("date");
                            showCommentItem(com_name,com_text,com_date);
                        }
                        if(comments.length()==0){tv_zero_comment.setVisibility(View.VISIBLE);}
                    }
                    if(jsonObject.has("author")){
                        int anonymous=0;
                        String author=jsonObject.getString("author");
                        if(jsonObject.has("anonymous")){
                            anonymous=jsonObject.getInt("anonymous");
                        }
                        if(anonymous==1 || author.length()==0){
                            author=getResources().getString(R.string.anonymous);
                        }
                        author=author+":";
                        tv_author.setVisibility(View.VISIBLE);
                        tv_author.setText(author);
                    }
                    if(jsonObject.has("authority")){
                        JSONObject authObj = jsonObject.getJSONObject("authority");
                        authority_id=authObj.getInt("id");
                        String auth_title=authObj.getString("title");
                        if(auth_title.length()!=0){
                            tv_authority.setText(auth_title);
                        }
                    }
                    if(jsonObject.has("department")){
                        JSONObject myObj = jsonObject.getJSONObject("department");
                        cat_id=myObj.getInt("id");
                        String cat_title=myObj.getString("value");
                        if(cat_title.length()!=0){
                            tv_category.setText(cat_title);
                        }
                    }
                    if(jsonObject.has("type")){
                        JSONObject myObj = jsonObject.getJSONObject("type");
                        type_id=myObj.getInt("id");
                        String type_title=myObj.getString("value");
                        if(type_title.length()!=0){
                            tv_type.setText(type_title);
                        }
                    }
                    if(jsonObject.has("city")){
                        JSONObject myObj = jsonObject.getJSONObject("city");
                        city_id=myObj.getInt("id");
                        tv_city.setText(myObj.getString("value"));
                    }
                    if(jsonObject.has("images")){
                        JSONArray images = jsonObject.getJSONArray("images");
                        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(100, 100);
                        lp.setMargins(0, 0, 10, 10);
                        for(int i=0; i < images.length(); i++){
                            String img = images.getString(i);
                            ImageView imageViewPreview = new ImageView(activity);
                            imageViewPreview.setLayoutParams(lp);

                            GlideApp.with(context)
                                    .load(Endpoints.REPORT_IMG+id+"/thumbs/"+img)
                                    .centerCrop()
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .into(imageViewPreview);

                            imageViewPreview.setOnClickListener(thumbClick);
                            imageViewPreview.setTag(i);
                            ll_thumb_holder.addView(imageViewPreview);
                            imageList.add(Endpoints.REPORT_IMG+id+"/"+img);
                        }
                    }
                    if(loadAll){
                        String title=jsonObject.getString("title");
                        String text=jsonObject.getString("text");
                        String date=jsonObject.getString("date");
                        lat=jsonObject.getDouble("lat");
                        lng=jsonObject.getDouble("lon");

                        tv_title.setText(title);
                        tv_date.setText(getDate(date));

                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                            tv_text.setText(Html.fromHtml(text,Html.FROM_HTML_MODE_LEGACY));
                        } else {
                            tv_text.setText(Html.fromHtml(text));
                        }

                        if(lat>0 && lng>0){
                            new android.os.Handler().postDelayed(
                                    new Runnable() {
                                        public void run() {
                                            showMapFrame();
                                        }
                                    },
                                    1000);
                        }
                        else{
                            rl_map.setVisibility(View.GONE);
                        }
                    }

                    /*int verified=jsonObject.getInt("incident_verified");
                    int active=jsonObject.getInt("incident_active");
                    if(active==0){
                        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        llp.setMargins(0, 0, 10, 0); // llp.setMargins(left, top, right, bottom);
                        tv_not_active.setLayoutParams(llp);
                        tv_not_active.setVisibility(View.VISIBLE);
                    }*/

                }catch(JSONException e){e.printStackTrace();}
            }
        };

        JsonObjectRequest volReq = new JsonObjectRequest(Request.Method.GET, uri, null, listener,null);


        MyVolley.getInstance(this).addToRequestQueue(volReq);
    }


    View.OnClickListener thumbClick = new View.OnClickListener(){
        public void onClick(View v){
            int tag =(Integer) v.getTag();
            Intent intent = new Intent(activity, GalleryActivity.class);
            intent.putStringArrayListExtra("imglist", imageList);
            intent.putExtra("pos",tag);
            startActivity(intent);
        }
    };

    public void showCommentItem(String name, String comment, String cdate){
        TextView nameTv=new TextView(this);
        nameTv.setText(name);
        nameTv.setTypeface(null, Typeface.BOLD);

        TextView commentTv=new TextView(this);
        commentTv.setText(comment);
        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        llp.setMargins(0, 2, 0, 2); // llp.setMargins(left, top, right, bottom);
        commentTv.setLayoutParams(llp);

        TextView dateTv=new TextView(this);
        dateTv.setText(getDate(cdate));
        dateTv.setTextSize(12);
        dateTv.setTextColor(Color.GRAY);
        LinearLayout.LayoutParams cllp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        cllp.setMargins(0, 0, 0, 10); // llp.setMargins(left, top, right, bottom);
        dateTv.setLayoutParams(cllp);

        ll_comments.addView(nameTv);
        ll_comments.addView(commentTv);
        ll_comments.addView(dateTv);
        ll_comments.setPadding(0,15,0,10);
    }

    public String getDate(String date) {
        Locale locale = new Locale("ru");
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",locale);
        try{
            Date dateObj = formatter.parse(date);
            SimpleDateFormat fmt = new SimpleDateFormat("dd.MM.yyyy HH:mm",locale);
            return fmt.format(dateObj);
        }
        catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    protected void showMapFrame(){
        FrameMapFragment fmfragment=new FrameMapFragment();
        Bundle bundle = new Bundle();
        bundle.putDouble("lat", lat);
        bundle.putDouble("lng", lng);
        Log.e("LATLNG", "lat"+lat+" lng"+lng);
        fmfragment.setArguments(bundle);
        putFragment(fmfragment);

        Button button = new Button(this);
        button.getBackground().setAlpha(0);
        button.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        rl_map.addView(button);

        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), MapsActivity.class);
                intent.putExtra("lat",lat);
                intent.putExtra("lng",lng);
                startActivity(intent);
            }
        });
    }

    protected void putFragment(Fragment frag){
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.id_fl_map, frag);
        //ft.addToBackStack(null);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.commit();
    }

    public void addComment(View v){
        Intent intent = new Intent(v.getContext(), AddCommentActivity.class);
        intent.putExtra("model","report");
        intent.putExtra("id",id);
        startActivity(intent);
    }

    public void authClicked(View v){
        openReportView("authority_id",authority_id);
    }

    public void typeClicked(View v){
        openReportView("type_id",type_id);
    }

    public void ctgClicked(View v){
        openReportView("sector_id",cat_id);
    }

    public void cityClicked(View v){
        openReportView("city_id",city_id);
    }

    public void openReportView(String key, int value){
        Intent intent=new Intent(ReportViewActivity.this,MainActivity.class);
        intent.putExtra(key,value);
        intent.putExtra("showReport",true);
        intent.putExtra("empty",false);
        startActivity(intent);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
