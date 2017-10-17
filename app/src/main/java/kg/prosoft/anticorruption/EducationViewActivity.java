package kg.prosoft.anticorruption;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import kg.prosoft.anticorruption.service.Document;
import kg.prosoft.anticorruption.service.DocumentAdapter;
import kg.prosoft.anticorruption.service.Education;
import kg.prosoft.anticorruption.service.EducationAdapter;
import kg.prosoft.anticorruption.service.Endpoints;
import kg.prosoft.anticorruption.service.GlideApp;
import kg.prosoft.anticorruption.service.MyDbHandler;
import kg.prosoft.anticorruption.service.MyVolley;
import kg.prosoft.anticorruption.service.Vocabulary;

public class EducationViewActivity extends BaseActivity {
    TextView tv_title, tv_date, tv_text, tv_zero_comment,tv_category;
    ImageView iv_image;
    public LinearLayout ll_comments;
    int id, cat_id;
    String TAG ="NewsViewAc";

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

        Intent intent = getIntent();
        id=intent.getIntExtra("id",0);
        String title=intent.getStringExtra("title");
        //String description=intent.getStringExtra("desc");
        String text=intent.getStringExtra("text");
        String date=intent.getStringExtra("date");
        String image=intent.getStringExtra("image");

        tv_title.setText(title);
        tv_date.setText(date);
        CharSequence html_text;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            html_text= Html.fromHtml(text,Html.FROM_HTML_MODE_LEGACY);
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

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}
