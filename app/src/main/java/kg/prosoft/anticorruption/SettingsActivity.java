package kg.prosoft.anticorruption;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RadioGroup;

import kg.prosoft.anticorruption.service.LocaleHelper;

public class SettingsActivity extends AppCompatActivity {
    RadioGroup radio_group_lang;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(R.string.action_settings);
        }

        radio_group_lang=(RadioGroup) findViewById(R.id.id_rgroup_lng);
        String lang= LocaleHelper.getLanguage(getApplicationContext());
        if(lang.equals("ru")){
            radio_group_lang.check(R.id.id_radio_ru);
        }
        if(lang.equals("en")){
            radio_group_lang.check(R.id.id_radio_en);
        }
        radio_group_lang.setOnCheckedChangeListener(changeLangListener);
    }


    RadioGroup.OnCheckedChangeListener changeLangListener = new RadioGroup.OnCheckedChangeListener()
    {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId)
        {
            //MainActivity mainAct=(MainActivity)context;
            switch(checkedId)
            {
                case R.id.id_radio_ru:
                    //mainAct.updateViews("ru");
                    changeLang("ru");
                    break;
                case R.id.id_radio_ky:
                    //mainAct.updateViews("ky");
                    changeLang("ky");
                    break;
                case R.id.id_radio_en:
                    //mainAct.updateViews("en");
                    changeLang("en");
                    break;
            }
        }
    };

    public void changeLang(String lang){
        Intent intent= new Intent();
        intent.putExtra("lang", lang);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
