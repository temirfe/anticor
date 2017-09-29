package kg.prosoft.anticorruption;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;

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

public class NewsViewActivity extends AppCompatActivity {
    TextView tv_title, tv_date, tv_text, tv_zero_comment;
    ImageView iv_image;
    public LinearLayout ll_comments;
    int id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_view);
        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        tv_title=(TextView)findViewById(R.id.id_tv_title);
        tv_date=(TextView)findViewById(R.id.id_tv_date);
        tv_text=(TextView)findViewById(R.id.id_tv_text);
        iv_image=(ImageView)findViewById(R.id.id_iv_img);
        tv_zero_comment=(TextView)findViewById(R.id.id_tv_comments_zero);
        ll_comments=(LinearLayout) findViewById(R.id.id_ll_comments);

        Intent intent = getIntent();
        id=intent.getIntExtra("id",0);
        int cat_id=intent.getIntExtra("cat_id",0);
        String title=intent.getStringExtra("title");
        String description=intent.getStringExtra("desc");
        String text=intent.getStringExtra("text");
        String date=intent.getStringExtra("date");
        String image=intent.getStringExtra("image");

        tv_title.setText(title);
        tv_date.setText(date);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            tv_text.setText(Html.fromHtml(text,Html.FROM_HTML_MODE_LEGACY));
        } else {
            tv_text.setText(Html.fromHtml(text));
        }

        if(!image.isEmpty()){
            GlideApp.with(this)
                    .load(Endpoints.NEWS_IMG+"/"+image)        // optional
                    .into(iv_image);
        }

        //although we have info from intent, we make a request to get comments
        requestNews(id);
    }

    public void requestNews(final int id){
        String uri = Endpoints.NEWS+"/"+id;

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

    public void addComment(View v){
        Intent intent = new Intent(v.getContext(), AddCommentActivity.class);
        intent.putExtra("model","news");
        intent.putExtra("id",id);
        startActivity(intent);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
