package kg.prosoft.anticorruption;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import kg.prosoft.anticorruption.service.Endpoints;
import kg.prosoft.anticorruption.service.MyVolley;


public class AccountActivity extends BaseActivity {
    //LinearLayout ll_logout;
    EditText et_username;
    TextView tv_username, tv_edit, tv_apply, tv_report_count, tv_comment_count,
            tv_reports_open, tv_comments_open;
    int user_id;
    String auth_key;
    ProgressBar pb_report, pb_comment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(R.string.myAccount);
        }

        Intent gotIntent=getIntent();
        user_id=gotIntent.getIntExtra("user_id",0);
        if(user_id==session.getUserId()){
            auth_key=session.getAccessToken();
        }
        //Log.e(TAG, "got id:"+user_id);

        //ll_logout = (LinearLayout) findViewById(R.id.id_ll_logout);
        //ll_logout.setOnClickListener(onClickLogout);
        pb_report=(ProgressBar)findViewById(R.id.id_pb_report);
        pb_comment=(ProgressBar)findViewById(R.id.id_pb_comment);
        et_username=(EditText)findViewById(R.id.id_et_username);
        tv_username=(TextView) findViewById(R.id.id_tv_username);
        tv_edit=(TextView) findViewById(R.id.id_tv_edit);
        tv_apply=(TextView) findViewById(R.id.id_tv_apply);
        tv_report_count=(TextView) findViewById(R.id.id_tv_report_count);
        tv_comment_count=(TextView) findViewById(R.id.id_tv_comment_count);
        et_username.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    Log.e(TAG,"t: "+et_username.getText());
                    return true;
                }
                return false;
            }
        });
        tv_reports_open=(TextView) findViewById(R.id.id_tv_reports_open);
        tv_comments_open=(TextView) findViewById(R.id.id_tv_comments_open);
        requestUser();
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

    public void editUsername(View v){
        et_username.setVisibility(View.VISIBLE);
        tv_apply.setVisibility(View.VISIBLE);
        et_username.requestFocus();
        et_username.setSelection(et_username.getText().length());
        //et_username.setImeOptions(EditorInfo.IME_ACTION_DONE);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(et_username, InputMethodManager.SHOW_IMPLICIT);

        tv_username.setVisibility(View.GONE);
        tv_edit.setVisibility(View.GONE);
    }

    public void applyUsername(View v){
    }
    public void openReports(View v){
    }
    public void openComments(View v){
        Intent intent = new Intent(context, CommentActivity.class);
        intent.putExtra("user_id",user_id);
        startActivity(intent);
    }

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

    public void requestUser(){
        String auth="";
        if(auth_key!=null){auth="?auth_key="+auth_key;}
        String uri = Endpoints.USERS+"/"+user_id+auth;
        Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                try{
                    Log.e(TAG,"resp: "+jsonObject);
                    int reports=jsonObject.getInt("reports");
                    int comments=jsonObject.getInt("comments");
                    tv_report_count.setVisibility(View.VISIBLE);
                    pb_report.setVisibility(View.GONE);
                    tv_comment_count.setVisibility(View.VISIBLE);
                    pb_comment.setVisibility(View.GONE);
                    if(reports>0){
                        tv_report_count.setText(Integer.toString(reports));
                        tv_reports_open.setVisibility(View.VISIBLE);
                    }
                    if(comments>0){
                        tv_comment_count.setText(Integer.toString(comments));
                        tv_comments_open.setVisibility(View.VISIBLE);
                    }

                }catch(JSONException e){e.printStackTrace();}
            }
        };

        JsonObjectRequest volReq = new JsonObjectRequest(Request.Method.GET, uri, null, listener,null);


        MyVolley.getInstance(this).addToRequestQueue(volReq);
    }
}
