package kg.prosoft.anticorruption;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import kg.prosoft.anticorruption.service.Endpoints;

public class DocViewActivity extends AppCompatActivity {
    TextView tv_title, tv_date, tv_text,tv_category;
    int id, cat_id;
    String TAG ="DocViewAc";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doc_view);
        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        tv_category=(TextView)findViewById(R.id.id_tv_category);
        tv_title=(TextView)findViewById(R.id.id_tv_title);
        tv_date=(TextView)findViewById(R.id.id_tv_date);
        tv_text=(TextView)findViewById(R.id.id_tv_text);

        Intent intent = getIntent();
        id=intent.getIntExtra("id",0);
        cat_id=intent.getIntExtra("cat_id",0);
        String title=intent.getStringExtra("title");
        String text=intent.getStringExtra("text");
        String date=intent.getStringExtra("date");
        String ctg_title=intent.getStringExtra("cat_title");

        tv_title.setText(title);
        tv_date.setText(date);
        tv_category.setText(ctg_title);
        CharSequence html_text;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            html_text= Html.fromHtml(text,Html.FROM_HTML_MODE_LEGACY);
        } else {
            html_text=Html.fromHtml(text);
        }
        tv_text.setText(trimTrailingWhitespace(html_text));
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

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
