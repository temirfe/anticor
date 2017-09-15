package kg.prosoft.anticorruption;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.WebView;

public class WebViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        String text=getIntent().getStringExtra("text");
        if(text!=null){
            String title=getIntent().getStringExtra("title");
            getSupportActionBar().setTitle(title);
            setContentView(R.layout.activity_web_view);
            WebView webview = (WebView)this.findViewById(R.id.webview);
            webview.loadDataWithBaseURL("", text, "text/html", "UTF-8", "");
        }
        else{
            WebView webview = new WebView(this);
            setContentView(webview);
            String link=getIntent().getStringExtra("link");
            webview.loadUrl(link);

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                //NavUtils.navigateUpFromSameTask(this);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
