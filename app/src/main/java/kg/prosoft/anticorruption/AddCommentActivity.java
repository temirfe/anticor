package kg.prosoft.anticorruption;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import kg.prosoft.anticorruption.service.Endpoints;
import kg.prosoft.anticorruption.service.MyVolley;
import kg.prosoft.anticorruption.service.SessionManager;

public class AddCommentActivity extends AppCompatActivity {
    EditText et_name, et_email, et_comment;
    String model;
    int id;
    Activity activity;
    SessionManager session;
    boolean saveCredentials;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_comment);

        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(R.string.add_comment);
        }
        activity=this;
        session = new SessionManager(getApplicationContext());
        saveCredentials=false;

        et_name=(EditText)findViewById(R.id.id_et_name);
        et_email=(EditText)findViewById(R.id.id_et_email);
        et_comment=(EditText)findViewById(R.id.id_et_comment);
        et_comment.setImeOptions(EditorInfo.IME_ACTION_DONE);
        et_comment.setRawInputType(InputType.TYPE_CLASS_TEXT);

        Intent intent = getIntent();
        id=intent.getIntExtra("id",0);
        model=intent.getStringExtra("model");

        Log.e("AddComm",id+" "+model);

        String name=session.getName();
        String email=session.getEmail();
        if(name==null || name.isEmpty()){
            name=session.getUserName();
        }
        //user_id=session.getUserId();
        if(session.isLoggedIn()){
            if(name.length()>0){
                et_name.setText(name);
                et_comment.requestFocus();
            }
            if(email.length()>0){
                et_email.setText(email);
                et_email.setVisibility(View.GONE);
                et_comment.requestFocus();
            }
        }
        else if(name.length()>0 && email.length()>0){
            et_name.setText(name);
            et_email.setText(email);
            et_comment.requestFocus();
        }
        else {saveCredentials=true;}
    }

    public void submitComment(View v){
        String comment = et_comment.getText().toString();
        if(comment.length()>0){
            String name = et_name.getText().toString();
            if(name.length()>0){
                boolean okToSend=true;
                String email = et_email.getText().toString();
                if(email.length()>0){
                    if(!isEmailValid(email)){
                        et_email.setError(getResources().getString(R.string.error_invalid_email));
                        okToSend=false;
                    }
                }
                if(okToSend){
                    if(saveCredentials){
                        session.saveNameEmail(name,email);
                    }
                    sendComment(name,email,comment);
                }
            }
            else {
                et_name.setError(getResources().getString(R.string.required));
            }
        }
        else {
            et_comment.setError(getResources().getString(R.string.required));
        }
    }

    public void sendComment(final String name, final String email, final String comment){
        final ProgressDialog progress = new ProgressDialog(this);
        progress.setTitle(getResources().getString(R.string.sending));
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
        progress.show();

        String url= Endpoints.COMMENTS;
        Response.Listener<String> listener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progress.dismiss();
                try {
                    JSONObject obj = new JSONObject(response);
                    try{
                        int id = obj.getInt("id");
                        if(id!=0){
                            finish();
                            Toast.makeText(activity, R.string.comment_moder, Toast.LENGTH_LONG).show();
                        }

                    }catch(JSONException e){e.printStackTrace();}

                } catch (Throwable t) {
                    Log.e("submitComment", "Could not parse malformed JSON: \"" + response + "\"");
                }
            }
        };

        Response.ErrorListener errorResp =new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error) {
                progress.dismiss();
                // As of f605da3 the following should work
                NetworkResponse response = error.networkResponse;
                if (error instanceof ServerError && response != null) {
                    try {
                        String res = new String(response.data,
                                HttpHeaderParser.parseCharset(response.headers, "utf-8"));
                        Object json = new JSONTokener(res).nextValue();
                        if (json instanceof JSONObject){
                            JSONObject err = new JSONObject(res);
                            Log.i("RESPONSE err 1", err.toString());
                        }
                        else if (json instanceof JSONArray){
                            JSONArray err = new JSONArray(res);
                            Log.i("RESPONSE err 1", err.toString());
                        }
                        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                        builder.setMessage(R.string.app_error).setNegativeButton(R.string.close,null).create().show();
                    } catch (UnsupportedEncodingException e1) {
                        // Couldn't properly decode data to string
                        e1.printStackTrace();
                        Log.i("RESPONSE err 2", "here");
                    } catch (JSONException e2) {
                        // returned data is not JSONObject?
                        e2.printStackTrace();
                        Log.i("RESPONSE err 3", "here");
                    }
                }
            }
        };

        StringRequest req = new StringRequest(Request.Method.POST, url, listener, errorResp){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                params.put("name",name);
                params.put("email",email);
                params.put("text",comment);
                params.put("user_id",Integer.toString(session.getUserId()));
                //if(user_id!=0){params.put("user_id",Integer.toString(user_id));}
                String to_id="x";
                if(model.equals("report")){to_id="report_id";}
                else if(model.equals("news")){to_id="news_id";}
                else if(model.equals("authority")){to_id="category_id";}
                params.put(to_id,Integer.toString(id));

                return params;
            }
        };
        req.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        MyVolley.getInstance(activity).addToRequestQueue(req);
    }


    private boolean isEmailValid(String email) {
        boolean valid=true;
        if(!email.contains("@")){
            valid=false;
        }
        if(!email.contains(".")){
            valid=false;
        }
        return valid;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //session.clear();
        RequestQueue queue = MyVolley.getInstance(activity).getRequestQueue();
        queue.cancelAll(activity);
    }
}
