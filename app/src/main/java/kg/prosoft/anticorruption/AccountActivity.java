package kg.prosoft.anticorruption;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioGroup;

import kg.prosoft.anticorruption.service.LocaleHelper;
import kg.prosoft.anticorruption.service.MyHelper;

public class AccountActivity extends BaseActivity {
    LinearLayout ll_logout;
    LinearLayout ll_orders;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(R.string.myAccount);
        }

        ll_logout = (LinearLayout) findViewById(R.id.id_ll_logout);
        ll_logout.setOnClickListener(onClickLogout);
        ll_orders = (LinearLayout) findViewById(R.id.id_ll_orders);
        ll_orders.setOnClickListener(onClickOrders);
    }

    View.OnClickListener onClickLogout = new View.OnClickListener(){
        @Override
        public void onClick(View view) {
            session.logoutUser();
            finish();
        }
    };

    View.OnClickListener onClickOrders = new View.OnClickListener(){
        @Override
        public void onClick(View view) {
            //Intent intent = new Intent(thisContext, MyordersActivity.class);
            //intent.putExtra("user_id",session.getUserId());
            //startActivity(intent);
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
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
