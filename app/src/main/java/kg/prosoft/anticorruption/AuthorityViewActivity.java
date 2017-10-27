package kg.prosoft.anticorruption;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import kg.prosoft.anticorruption.service.Endpoints;
import kg.prosoft.anticorruption.service.GlideApp;
import kg.prosoft.anticorruption.service.MyVolley;
import kg.prosoft.anticorruption.service.SessionManager;

public class AuthorityViewActivity extends AppCompatActivity {
    TextView tv_title, tv_text, tv_zero_comment, tv_report_count, tv_report_link;
    RatingBar ratingBar;
    ImageView iv_image;
    Activity activity;
    Context context;
    SessionManager session;
    public LinearLayout ll_comments;
    int id;
    int rating=0;
    int user_rating=0;
    boolean loadAll;
    public RelativeLayout rl_pb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authority_view);
        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        activity=this;
        context=getApplicationContext();
        session=new SessionManager(context);
        tv_title=(TextView)findViewById(R.id.id_tv_title);
        tv_text=(TextView)findViewById(R.id.id_tv_text);
        iv_image=(ImageView)findViewById(R.id.id_iv_img);
        tv_zero_comment=(TextView)findViewById(R.id.id_tv_comments_zero);
        tv_report_count=(TextView)findViewById(R.id.id_tv_report_count);
        tv_report_link=(TextView)findViewById(R.id.id_tv_report_link);
        ll_comments=(LinearLayout) findViewById(R.id.id_ll_comments);
        rl_pb=(RelativeLayout)findViewById(R.id.id_rl_pb);

        Intent intent = getIntent();
        id=intent.getIntExtra("id",0);
        if(intent.hasExtra("title")){
            int parent_id=intent.getIntExtra("parent_id",0);
            String title=intent.getStringExtra("title");
            String text=intent.getStringExtra("text");
            String image=intent.getStringExtra("image");
            rating=intent.getIntExtra("rating",0);

            tv_title.setText(title);
            tv_text.setText(text);

            if(!image.isEmpty()){
                GlideApp.with(this)
                        .load(Endpoints.AUTHORITY_IMG+"/"+image)        // optional
                        .into(iv_image);
            }

            ratingBar = (RatingBar) findViewById(R.id.id_rating);
            ratingBar.setOnTouchListener(showRatingDialog);
            if(rating>0){
                float f_rating=(float)rating/2;
                ratingBar.setRating(f_rating);
            }
            loadAll=false;
        }
        else{
            loadAll=true;
            rl_pb.setVisibility(View.VISIBLE);
        }

        //although we have info from intent, we make a request to get comments
        requestAuthority(id);
        if(session.isLoggedIn()){
            requestUserRate();
        }
    }
    public void requestAuthority(final int id){
        String uri = Endpoints.AUTHORITIES+"/"+id;

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
                    int rep_count=jsonObject.getInt("reports");
                    if(rep_count>0){
                        tv_report_count.setText(Integer.toString(rep_count));
                        tv_report_link.setVisibility(View.VISIBLE);
                    }
                    if(loadAll){
                        String title=jsonObject.getString("title");
                        String text=jsonObject.getString("text");
                        String image=jsonObject.getString("img");
                        rating=jsonObject.getInt("rating");

                        tv_title.setText(title);
                        tv_text.setText(text);

                        if(!image.isEmpty()){
                            GlideApp.with(activity)
                                    .load(Endpoints.AUTHORITY_IMG+"/"+image)        // optional
                                    .into(iv_image);
                        }

                        ratingBar = (RatingBar) findViewById(R.id.id_rating);
                        ratingBar.setOnTouchListener(showRatingDialog);
                        if(rating>0){
                            float f_rating=(float)rating/2;
                            ratingBar.setRating(f_rating);
                        }
                    }

                }catch(JSONException e){e.printStackTrace();}
            }
        };

        JsonObjectRequest volReq = new JsonObjectRequest(Request.Method.GET, uri, null, listener,null);


        MyVolley.getInstance(context).addToRequestQueue(volReq);
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

    public void addComment(View v){
        Intent intent = new Intent(v.getContext(), AddCommentActivity.class);
        intent.putExtra("model","authority");
        intent.putExtra("id",id);
        startActivity(intent);
    }
    public void openReports(View v){
        Intent intent = new Intent(v.getContext(), MainActivity.class);
        intent.putExtra("authority_id",id);
        intent.putExtra("showReport",true);
        startActivity(intent);
    }

    View.OnTouchListener showRatingDialog = new View.OnTouchListener(){
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                session.checkLogin();
                final AlertDialog.Builder ratingdialog = new AlertDialog.Builder(activity);
                ratingdialog.setTitle(R.string.rate_authority);

                View linearlayout = getLayoutInflater().inflate(R.layout.rating_dialog, null);
                ratingdialog.setView(linearlayout);

                final RatingBar rateMe = (RatingBar)linearlayout.findViewById(R.id.ratingbar);
                if(user_rating>0){
                    float f_rating=(float)user_rating/2;
                    rateMe.setRating(f_rating);
                }

                ratingdialog.setPositiveButton(R.string.done,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                float user_rate=rateMe.getRating();
                                user_rate=user_rate*2; //since it's only 5 star system here, but 10 star in server
                                sendRating(id,user_rate);
                                dialog.dismiss();
                            }
                        })

                        .setNegativeButton(R.string.cancel,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });

                ratingdialog.create();
                ratingdialog.show();
            }
            return true;
        }
    };

    public void sendRating(final int authority_id, final float value){
        String url= Endpoints.AUTHORITY_RATE;
        Response.Listener<String> listener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject obj = new JSONObject(response);
                    try{
                        if(obj.has("new_rating")){
                            int new_rating = obj.getInt("new_rating");
                            float f_rating=new_rating/2;
                            ratingBar.setRating(f_rating);
                        }

                    }catch(JSONException e){e.printStackTrace();}

                } catch (Throwable t) {
                    Log.e("sendRating", "Could not parse malformed JSON: \"" + response + "\"");
                }
            }
        };

        StringRequest req = new StringRequest(Request.Method.POST, url, listener, null){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                params.put("id",Integer.toString(authority_id));
                params.put("value",Float.toString(value));

                return params;
            }
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer "+session.getAccessToken());
                return headers;
            }
        };
        req.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        MyVolley.getInstance(activity).addToRequestQueue(req);
    }


    public void requestUserRate(){

        String uri = Endpoints.AUTHORITY_USER_RATE+"?authority_id="+id;

        Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {

                Log.e("USERRATE", "response: " + jsonObject);
                try{
                    if(jsonObject.has("rate")){
                        user_rating=jsonObject.getInt("rate");
                    }

                }catch(JSONException e){e.printStackTrace();}
            }
        };
        JsonObjectRequest volReq = new JsonObjectRequest(Request.Method.GET, uri, null, listener,null){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer "+session.getAccessToken());
                return headers;
            }
        };

        MyVolley.getInstance(context).addToRequestQueue(volReq);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}
