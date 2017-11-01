package kg.prosoft.anticorruption;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;

public class PageViewActivity extends AppCompatActivity {
    TextView tv_title,tv_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        setContentView(R.layout.activity_page_view);
        Intent gotIntent=getIntent();
        String title=gotIntent.getStringExtra("title");
        String text=gotIntent.getStringExtra("text");
        tv_title=(TextView)findViewById(R.id.id_tv_title);
        tv_text=(TextView)findViewById(R.id.id_tv_text);
        tv_title.setText(title);

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
