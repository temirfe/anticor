package kg.prosoft.anticorruption;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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
    TextView tv_title, tv_text, tv_zero_comment, tv_report_count, tv_report_link,
            tv_login, tv_rating_rate, tv_rating_count,tv_dialog_rate;
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
    Button btn_rate;
    String votes, lang;
    ProgressBar pb_rating, pb_count;

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
        lang=session.getLanguage();
        if(lang.isEmpty()){lang="ky";}
        tv_title=(TextView)findViewById(R.id.id_tv_title);
        tv_text=(TextView)findViewById(R.id.id_tv_text);
        iv_image=(ImageView)findViewById(R.id.id_iv_img);
        tv_zero_comment=(TextView)findViewById(R.id.id_tv_comments_zero);
        tv_report_count=(TextView)findViewById(R.id.id_tv_report_count);
        tv_report_link=(TextView)findViewById(R.id.id_tv_report_link);
        tv_login=(TextView)findViewById(R.id.id_tv_login);
        tv_rating_rate=(TextView)findViewById(R.id.id_tv_rating_rate);
        tv_rating_count=(TextView)findViewById(R.id.id_tv_rating_count);
        ll_comments=(LinearLayout) findViewById(R.id.id_ll_comments);
        rl_pb=(RelativeLayout)findViewById(R.id.id_rl_pb);
        btn_rate=(Button)findViewById(R.id.id_btn_rate);
        if(session.isLoggedIn()){btn_rate.setVisibility(View.VISIBLE);}
        else{tv_login.setVisibility(View.VISIBLE);}
        pb_rating=(ProgressBar)findViewById(R.id.id_pb_rating);
        pb_count=(ProgressBar)findViewById(R.id.id_pb_count);

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
            /*String ratingDesc=getRatingDesc(rating);
            String rating_text=rating+ratingDesc;
            tv_rating_rate.setText(rating_text);*/

            if(!image.isEmpty()){
                GlideApp.with(this)
                        .load(Endpoints.AUTHORITY_IMG+"/"+image)        // optional
                        .into(iv_image);
            }

            ratingBar = (RatingBar) findViewById(R.id.id_rating);
            ratingBar.setOnTouchListener(showRatingDialog);
            if(rating>0){
                ratingBar.setRating(rating);
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
            Log.e("Session", "auth_key "+session.getAccessToken());
        }
    }
    public void requestAuthority(final int id){
        String uri = Endpoints.AUTHORITIES+"/"+id+"?lang="+lang;

        Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                rl_pb.setVisibility(View.GONE);
                pb_rating.setVisibility(View.GONE);
                pb_count.setVisibility(View.GONE);
                try{
                    if(jsonObject.has("comments")){
                        JSONArray comments = jsonObject.getJSONArray("comments");
                        for(int i=0; i < comments.length(); i++){
                            JSONObject comObj = comments.getJSONObject(i);
                            String com_name=comObj.getString("name");
                            String com_text=comObj.getString("text");
                            String com_date=comObj.getString("date");
                            int com_user_id=comObj.getInt("user_id");
                            showCommentItem(com_user_id, com_name,com_text,com_date);
                        }
                        if(comments.length()==0){tv_zero_comment.setVisibility(View.VISIBLE);}
                    }
                    int rep_count=jsonObject.getInt("reports");
                    if(rep_count>0){
                        tv_report_count.setText(Integer.toString(rep_count));
                        tv_report_link.setVisibility(View.VISIBLE);
                    }
                    votes=jsonObject.getString("votes");
                    tv_rating_count.setText(votes);
                    tv_rating_count.setVisibility(View.VISIBLE);

                    rating=jsonObject.getInt("rating");
                    String ratingDesc=getRatingDesc(rating);
                    String rating_text=rating+ratingDesc;
                    tv_rating_rate.setText(rating_text);
                    tv_rating_rate.setVisibility(View.VISIBLE);
                    if(rating>0){
                        ratingBar.setRating(rating);
                    }
                    if(loadAll){
                        String title=jsonObject.getString("title");
                        String text=jsonObject.getString("text");
                        String image=jsonObject.getString("img");
                        tv_title.setText(title);
                        tv_text.setText(text);

                        if(!image.isEmpty()){
                            GlideApp.with(activity)
                                    .load(Endpoints.AUTHORITY_IMG+"/"+image)        // optional
                                    .into(iv_image);
                        }
                    }

                }catch(JSONException e){e.printStackTrace();}
            }
        };

        JsonObjectRequest volReq = new JsonObjectRequest(Request.Method.GET, uri, null, listener,null);


        MyVolley.getInstance(context).addToRequestQueue(volReq);
    }

    public void showCommentItem(int user_id, String name, String comment, String cdate){
        TextView nameTv=new TextView(this);
        nameTv.setPadding(0,5,10,5);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            nameTv.setTextColor(getResources().getColorStateList(R.color.gray_link, context.getTheme()));
        } else {
            nameTv.setTextColor(getResources().getColorStateList(R.color.gray_link));
        }
        nameTv.setTag(user_id);
        nameTv.setOnClickListener(clickCommentUser);
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

    View.OnClickListener clickCommentUser= new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int id=(int)view.getTag();
            TextView tv_name=(TextView)view;
            String name=tv_name.getText().toString();
            Intent intent = new Intent(AuthorityViewActivity.this, AccountActivity.class);
            intent.putExtra("user_id",id);
            intent.putExtra("username",name);
            Log.e("NewsView","id:"+id+" name:"+name);
            startActivity(intent);
        }
    };

    public String getRatingDesc(int rate){
        String desc="";
        switch(rate){
            case 1:desc=" ("+getResources().getString(R.string.rate1)+")"; break;
            case 2:desc=" ("+getResources().getString(R.string.rate2)+")"; break;
            case 3:desc=" ("+getResources().getString(R.string.rate3)+")"; break;
            case 4:desc=" ("+getResources().getString(R.string.rate4)+")"; break;
            case 5:desc=" ("+getResources().getString(R.string.rate5)+")"; break;
            case 6:desc=" ("+getResources().getString(R.string.rate6)+")"; break;
            case 7:desc=" ("+getResources().getString(R.string.rate7)+")"; break;
            case 8:desc=" ("+getResources().getString(R.string.rate8)+")"; break;
            case 9:desc=" ("+getResources().getString(R.string.rate9)+")"; break;
            case 10:desc=" ("+getResources().getString(R.string.rate10)+")"; break;
        }
        return desc;
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
                openDialog();
            }
            return true;
        }
    };

    public void onClickOpenDialog(View v){
        openDialog();
    }
    public void onClickLogin(View v){
        Intent intent = new Intent(AuthorityViewActivity.this, LoginActivity.class);
        startActivity(intent);
    }

    public void openDialog(){
        final AlertDialog.Builder ratingdialog = new AlertDialog.Builder(activity);

        TextView title = new TextView(activity);
        title.setText(R.string.rate_authority);
        title.setPadding(10, 10, 10, 10);
        title.setGravity(Gravity.CENTER);
        title.setTextSize(20);
        //ratingdialog.setTitle(R.string.rate_authority);
        ratingdialog.setCustomTitle(title);

        View linearlayout = getLayoutInflater().inflate(R.layout.rating_dialog, null);
        ratingdialog.setView(linearlayout);

        final RatingBar rateMe = (RatingBar)linearlayout.findViewById(R.id.ratingbar);
        tv_dialog_rate=(TextView)linearlayout.findViewById(R.id.id_tv_dialog_rate);
        rateMe.setOnRatingBarChangeListener(changeListener);
        if(user_rating>0){
            Log.e("Dialog","userRate: "+user_rating);
            String ratingDesc=getRatingDesc(user_rating);
            String rating_text=user_rating+ratingDesc;
            tv_dialog_rate.setText(rating_text);
            rateMe.setRating(user_rating);
        }

        ratingdialog.setPositiveButton(R.string.done_send,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        float user_rate=rateMe.getRating();
                        sendRating(id,user_rate);
                        dialog.dismiss();
                    }
                })

                .setNegativeButton(R.string.cancel_close,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        ratingdialog.create();
        ratingdialog.show();
    }

    RatingBar.OnRatingBarChangeListener changeListener= new RatingBar.OnRatingBarChangeListener() {
        @Override
        public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
            int drate=(int)v;
            String ratingDesc=getRatingDesc(drate);
            String rating_text=drate+ratingDesc;
            tv_dialog_rate.setText(rating_text);
        }
    };

    public void sendRating(final int authority_id, final float value){
        tv_rating_rate.setVisibility(View.INVISIBLE);
        tv_rating_count.setVisibility(View.GONE);
        pb_rating.setVisibility(View.VISIBLE);
        pb_count.setVisibility(View.VISIBLE);

        Log.e("Send","auth_id:"+authority_id+" value:"+value);
        String url= Endpoints.AUTHORITY_RATE;
        Response.Listener<String> listener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject obj = new JSONObject(response);
                    try{
                        if(obj.has("new_rating")){
                            int new_rating = obj.getInt("new_rating");
                            Log.e("Send","new "+new_rating);
                            ratingBar.setRating(new_rating);
                            String ratingDesc=getRatingDesc(new_rating);
                            String rating_text=new_rating+ratingDesc;
                            tv_rating_rate.setText(rating_text);
                            if(user_rating==0){ //if user hasn't voted then increase vote count
                                user_rating=new_rating;
                                int voteCount=0;
                                if(votes!=null && !votes.isEmpty()){
                                    voteCount=Integer.parseInt(votes);
                                }
                                voteCount++;
                                tv_rating_count.setText(Integer.toString(voteCount));
                            }

                            tv_rating_rate.setVisibility(View.VISIBLE);
                            tv_rating_count.setVisibility(View.VISIBLE);
                            pb_rating.setVisibility(View.GONE);
                            pb_count.setVisibility(View.GONE);
                        }
                        if(obj.has("msg")){
                            String msg = obj.getString("msg");
                            Log.e("Send","msg "+msg);
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
        Log.e("USERRATE", "request send");
        String uri = Endpoints.AUTHORITY_USER_RATE+"?authority_id="+id;

        Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                Log.e("USERRATE", "response: " + jsonObject);
                try{
                    if(jsonObject.has("rate")){
                        Log.e("USERRATE", "user_rating: " + jsonObject.getInt("rate"));
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
