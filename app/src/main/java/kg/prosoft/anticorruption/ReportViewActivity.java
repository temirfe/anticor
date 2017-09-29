package kg.prosoft.anticorruption;

import android.app.Fragment;
import android.app.FragmentTransaction;
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
import java.util.Date;
import java.util.Locale;

import kg.prosoft.anticorruption.service.Endpoints;
import kg.prosoft.anticorruption.service.GlideApp;
import kg.prosoft.anticorruption.service.MyVolley;

public class ReportViewActivity extends AppCompatActivity {
    TextView tv_title, tv_date, tv_text, tv_zero_comment;
    public double lat,lng;
    //ImageView iv_image;
    public RelativeLayout rl_map;
    public LinearLayout ll_comments;
    int id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_view);

        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        tv_title=(TextView)findViewById(R.id.id_tv_title);
        tv_date=(TextView)findViewById(R.id.id_tv_date);
        tv_text=(TextView)findViewById(R.id.id_tv_text);
        tv_zero_comment=(TextView)findViewById(R.id.id_tv_comments_zero);
        //iv_image=(ImageView)findViewById(R.id.id_iv_img);
        ll_comments=(LinearLayout) findViewById(R.id.id_ll_comments);
        rl_map=(RelativeLayout)findViewById(R.id.id_rl_map);

        Intent intent = getIntent();
        id=intent.getIntExtra("id",0);
        int cat_id=intent.getIntExtra("cat_id",0);
        String title=intent.getStringExtra("title");
        String description=intent.getStringExtra("desc");
        String text=intent.getStringExtra("text");
        String date=intent.getStringExtra("date");
        lat=intent.getDoubleExtra("lat",0);
        lng=intent.getDoubleExtra("lng",0);

        tv_title.setText(title);
        tv_date.setText(date);

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

        //although we have info from intent, we make a request to get comments and
        //other info like authority title, etc.
        requestReport(id);
    }


    public void requestReport(final int id){
        String uri = Endpoints.REPORTS+"/"+id;

        Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
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


                    JSONObject authority = jsonObject.getJSONObject("authority");
                    String authority_title=authority.getString("title");
                    Log.e("AUTH",authority_title);
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

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
