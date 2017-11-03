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
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import kg.prosoft.anticorruption.service.Endpoints;
import kg.prosoft.anticorruption.service.MyVolley;


public class AccountActivity extends BaseActivity {
    //LinearLayout ll_logout;
    EditText et_username;
    TextView tv_username, tv_edit, tv_apply, tv_report_count, tv_comment_count,
            tv_reports_open, tv_comments_open;
    int user_id;
    String auth_key, username;
    ProgressBar pb_report, pb_comment;
    InputMethodManager imm;
    String new_username_global;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(R.string.myAccount);
        }

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        Intent gotIntent=getIntent();
        user_id=gotIntent.getIntExtra("user_id",0);
        username= gotIntent.getStringExtra("username");
        //Log.e(TAG, "got id:"+user_id);

        //ll_logout = (LinearLayout) findViewById(R.id.id_ll_logout);
        //ll_logout.setOnClickListener(onClickLogout);
        pb_report=(ProgressBar)findViewById(R.id.id_pb_report);
        pb_comment=(ProgressBar)findViewById(R.id.id_pb_comment);
        et_username=(EditText)findViewById(R.id.id_et_username);
        tv_username=(TextView) findViewById(R.id.id_tv_username);
        tv_username.setText(username);
        et_username.setText(username);
        tv_edit=(TextView) findViewById(R.id.id_tv_edit);
        tv_apply=(TextView) findViewById(R.id.id_tv_apply);
        tv_report_count=(TextView) findViewById(R.id.id_tv_report_count);
        tv_comment_count=(TextView) findViewById(R.id.id_tv_comment_count);
        et_username.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    Log.e(TAG,"t: "+et_username.getText());
                    putRequest();
                    return true;
                }
                return false;
            }
        });
        tv_reports_open=(TextView) findViewById(R.id.id_tv_reports_open);
        tv_comments_open=(TextView) findViewById(R.id.id_tv_comments_open);

        if(user_id==session.getUserId()){
            auth_key=session.getAccessToken();
            tv_edit.setVisibility(View.VISIBLE);
        }

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
        imm.showSoftInput(et_username, InputMethodManager.SHOW_IMPLICIT);

        tv_username.setVisibility(View.GONE);
        tv_edit.setVisibility(View.GONE);
    }

    public void applyUsername(View v){
        Log.e(TAG,"applyUsername clicked");
        putRequest();
    }

    public void putRequest(){
        String uri = Endpoints.USERS+"/"+user_id;
        final String new_username=et_username.getText().toString();
        if(new_username.length()>0 && !new_username.equals(username)){
            Response.Listener<String> listener = new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try{
                        JSONObject jsonObject = new JSONObject(response);
                        if(jsonObject.has("username")){ //there has been error
                            JSONArray comments = jsonObject.getJSONArray("username");
                            String message = comments.getString(0);
                            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                            tv_username.setText(username);
                            et_username.setText(username);
                        }
                        else{
                            session.setUserName(new_username);
                            new_username_global=new_username;
                        }

                    }catch(JSONException e){e.printStackTrace();}
                }
            };

            StringRequest volReq = new StringRequest(Request.Method.PUT, uri, listener,null){
                @Override
                protected Map<String,String> getParams(){
                    Map<String,String> params = new HashMap<String, String>();
                    params.put("username",new_username);

                    return params;
                }
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer "+session.getAccessToken());
                    return headers;
                }
            };

            MyVolley.getInstance(context).addToRequestQueue(volReq);
        }
        View view = this.getCurrentFocus();
        if (view != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        tv_username.setText(new_username);
        tv_username.setVisibility(View.VISIBLE);
        tv_edit.setVisibility(View.VISIBLE);
        tv_apply.setVisibility(View.GONE);
        et_username.setVisibility(View.GONE);
    }

    public void openReports(View v){
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("user_id",user_id);
        intent.putExtra("showReport",true);
        startActivity(intent);
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
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Intent intent= new Intent();
        intent.putExtra("name", new_username_global);
        setResult(RESULT_OK, intent);
        super.onBackPressed();
        finish();
    }

    public void requestUser(){
        Log.e(TAG,"requestUser sent");
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
                    tv_comment_count.setVisibility(View.VISIBLE);
                    pb_comment.setVisibility(View.GONE);
                    pb_report.setVisibility(View.GONE);
                    tv_report_count.setText(Integer.toString(reports));
                    tv_comment_count.setText(Integer.toString(comments));
                    if(reports>0){
                        tv_reports_open.setVisibility(View.VISIBLE);
                    }
                    if(comments>0){
                        tv_comments_open.setVisibility(View.VISIBLE);
                    }

                }catch(JSONException e){e.printStackTrace();}
            }
        };

        Response.ErrorListener errListener=new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse networkResponse = error.networkResponse;
                Log.e(TAG, "Volley error: " + error.getMessage() + ", code: " + networkResponse);
                tv_report_count.setText("0");
                tv_comment_count.setText("0");
                pb_comment.setVisibility(View.GONE);
                pb_report.setVisibility(View.GONE);
                tv_report_count.setVisibility(View.VISIBLE);
                tv_comment_count.setVisibility(View.VISIBLE);
            }
        };
        JsonObjectRequest volReq = new JsonObjectRequest(Request.Method.GET, uri, null, listener,errListener);
        MyVolley.getInstance(context).addToRequestQueue(volReq);
    }
}
