package kg.prosoft.anticorruption;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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
import java.util.List;
import java.util.Locale;

import kg.prosoft.anticorruption.service.Endpoints;
import kg.prosoft.anticorruption.service.GlideApp;
import kg.prosoft.anticorruption.service.MyDbHandler;
import kg.prosoft.anticorruption.service.MyVolley;
import kg.prosoft.anticorruption.service.Vocabulary;

public class NewsViewActivity extends BaseActivity {
    TextView tv_title, tv_date, tv_text, tv_zero_comment,tv_category;
    ImageView iv_image;
    public LinearLayout ll_comments;
    int id, cat_id;
    String TAG ="NewsViewAc";
    boolean loadAll;
    ProgressBar pb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_view);
        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        pb=(ProgressBar)findViewById(R.id.progressBar1);
        tv_category=(TextView)findViewById(R.id.id_tv_category);
        //tv_category.setOnClickListener(onCtgClick);
        tv_title=(TextView)findViewById(R.id.id_tv_title);
        tv_date=(TextView)findViewById(R.id.id_tv_date);
        tv_text=(TextView)findViewById(R.id.id_tv_text);
        iv_image=(ImageView)findViewById(R.id.id_iv_img);
        tv_zero_comment=(TextView)findViewById(R.id.id_tv_comments_zero);
        ll_comments=(LinearLayout) findViewById(R.id.id_ll_comments);

        Intent intent = getIntent();
        id=intent.getIntExtra("id",0);
        if(intent.hasExtra("title")) //if opened from commentActivity then intent has only id
        {
            String title=intent.getStringExtra("title");
            cat_id=intent.getIntExtra("cat_id",0);
            //String description=intent.getStringExtra("desc");
            String text=intent.getStringExtra("text");
            String date=intent.getStringExtra("date");
            String image=intent.getStringExtra("image");

            tv_title.setText(title);
            tv_date.setText(date);
            CharSequence html_text;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                html_text=Html.fromHtml(text,Html.FROM_HTML_MODE_LEGACY);
            } else {
                html_text=Html.fromHtml(text);
            }
            tv_text.setText(trimTrailingWhitespace(html_text));

            if(!image.isEmpty()){
                GlideApp.with(this)
                        .load(Endpoints.NEWS_IMG+id+"/"+image)
                        .placeholder(R.drawable.placeholder) // optional
                        .dontAnimate()
                        .into(iv_image);
            }

            new VocabularyTask().execute();
            loadAll=false;
        }
        else{
            loadAll=true;
            pb.setVisibility(View.VISIBLE);
        }

        requestNews(id);
    }

    public static CharSequence trimTrailingWhitespace(CharSequence source) {

        if(source == null)
            return "";

        int i = source.length();

        // loop back to the first non-whitespace character
        while(--i >= 0 && Character.isWhitespace(source.charAt(i))) {
        }

        return source.subSequence(0, i+1);
    }

    /*View.OnClickListener onCtgClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.e("CTG",cat_id+"");
            Intent intent= new Intent();
            intent.putExtra("news_ctg_id", cat_id);
            setResult(RESULT_OK, intent);
            finish();
        }
    };*/

    public void requestNews(final int id){
        String uri = Endpoints.NEWS+"/"+id;

        Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                pb.setVisibility(View.GONE);
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

                    JSONObject ctgObj = jsonObject.getJSONObject("category");
                    tv_category.setText(ctgObj.getString("value"));

                    if(loadAll){
                        String title=jsonObject.getString("title");
                        tv_title.setText(title);
                        String text=jsonObject.getString("text");
                        CharSequence html_text;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                            html_text=Html.fromHtml(text,Html.FROM_HTML_MODE_LEGACY);
                        } else {
                            html_text=Html.fromHtml(text);
                        }
                        tv_text.setText(trimTrailingWhitespace(html_text));
                        cat_id=jsonObject.getInt("id");
                        String date=jsonObject.getString("date");
                        date=getDate(date);
                        tv_date.setText(date);
                        String image=jsonObject.getString("image");
                        if(!image.isEmpty()){
                            GlideApp.with(context)
                                    .load(Endpoints.NEWS_IMG+id+"/"+image)
                                    .placeholder(R.drawable.placeholder) // optional
                                    .dontAnimate()
                                    .into(iv_image);
                        }
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

    private class VocabularyTask extends AsyncTask<Void, Void, List<Vocabulary>> {
        protected List<Vocabulary> doInBackground(Void... params) {
            if(dbHandler==null){dbHandler = new MyDbHandler(context); Log.e(TAG, "VocabularyTask dbhandler was null");}
            if(db==null || !db.isOpen()){db = dbHandler.getWritableDatabase(); Log.e(TAG, "VocabularyTask db was null or not open");}

            return dbHandler.getVocContents(db);
        }
        protected void onPostExecute(List<Vocabulary> theList) {
            if(theList.size()>0){
                for (Vocabulary voc : theList) {
                    int vid=voc.getId();
                    String value=voc.getValue();
                    if(vid==cat_id){
                        tv_category.setText(value);
                    }
                }
                Log.e(TAG, "ctg has been taken from DB");
            }
            else{
                Log.e("VocabularyTask", "no content in db");
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
