package kg.prosoft.anticorruption;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.text.Html;
import android.util.Log;
import android.widget.TextView;


public class AnalyticsViewActivity extends AppCompatActivity {
    TextView tv_title, tv_date, tv_text, tv_author;
    int id, author_id;
    String TAG ="AnalViewAc";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics_view);

        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        tv_title=(TextView)findViewById(R.id.id_tv_title);
        tv_date=(TextView)findViewById(R.id.id_tv_date);
        tv_text=(TextView)findViewById(R.id.id_tv_text);
        tv_author=(TextView)findViewById(R.id.id_tv_author);

        Intent intent = getIntent();
        id=intent.getIntExtra("id",0);
        String title=intent.getStringExtra("title");
        //String description=intent.getStringExtra("desc");
        String text=intent.getStringExtra("text");
        String date=intent.getStringExtra("date");
        String author_name=intent.getStringExtra("author_name");
        author_id=intent.getIntExtra("author_id",0);

        tv_title.setText(title);
        tv_date.setText(date);
        tv_author.setText(author_name);
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

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
