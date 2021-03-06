package kg.prosoft.anticorruption;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;

import kg.prosoft.anticorruption.service.Endpoints;
import kg.prosoft.anticorruption.service.GlideApp;

public class EducationViewActivity extends BaseActivity {
    TextView tv_title, tv_date, tv_text;
    ImageView iv_image;
    int id;
    String TAG ="EduViewAc";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_education_view);
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
        Log.e(TAG, "image: "+image);

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
                    .load(Endpoints.EDU_IMG+id+"/"+image)
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.findItem(R.id.action_search).setVisible(false);
        return true;
    }

}
