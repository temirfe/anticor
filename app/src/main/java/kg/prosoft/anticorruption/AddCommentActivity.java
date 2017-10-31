package kg.prosoft.anticorruption;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.text.format.Formatter;
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
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.safetynet.SafetyNet;
import com.google.android.gms.safetynet.SafetyNetApi;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.UnsupportedEncodingException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

import kg.prosoft.anticorruption.service.Endpoints;
import kg.prosoft.anticorruption.service.MyVolley;
import kg.prosoft.anticorruption.service.SessionManager;

public class AddCommentActivity extends AppCompatActivity {
    EditText et_name, et_email, et_comment;
    String model, name, email, comment;
    int id;
    Activity activity;
    SessionManager session;
    boolean saveCredentials;
    String TAG="AddComment";

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
        comment = et_comment.getText().toString();
        if(comment.length()>0){
            name = et_name.getText().toString();
            if(name.length()>0){
                boolean okToSend=true;
                email = et_email.getText().toString();
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
                    doCaptcha();
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

    public void doCaptcha(){
        SafetyNet.getClient(this).verifyWithRecaptcha("6Lc3jTYUAAAAADtTzPDTmL6eJciK4Sv87Rz4Tl-2")
                .addOnSuccessListener(this,
                        new OnSuccessListener<SafetyNetApi.RecaptchaTokenResponse>() {
                            @Override
                            public void onSuccess(SafetyNetApi.RecaptchaTokenResponse response) {
                                // Indicates communication with reCAPTCHA service was
                                // successful.
                                String userResponseToken = response.getTokenResult();
                                if (!userResponseToken.isEmpty()) {
                                    // Validate the user response token using the
                                    // reCAPTCHA siteverify API.
                                    handleSiteVerify(userResponseToken);
                                }
                            }
                        })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (e instanceof ApiException) {
                            // An error occurred when communicating with the
                            // reCAPTCHA service. Refer to the status code to
                            // handle the error appropriately.
                            ApiException apiException = (ApiException) e;
                            int statusCode = apiException.getStatusCode();
                            Log.d(TAG, "Error: " + CommonStatusCodes
                                    .getStatusCodeString(statusCode));
                        } else {
                            // A different, unknown type of error occurred.
                            Log.d(TAG, "Error: " + e.getMessage());
                        }
                    }
                });
    }

    public void handleSiteVerify(final String token){
        final String ip=getLocalIpAddress();
        Log.e(TAG,"captcha "+token);
        Log.e(TAG,"ip "+ip);

        String url= "https://www.google.com/recaptcha/api/siteverify";
        Response.Listener<String> listener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e(TAG, "String resp: "+response);
                try {
                    JSONObject obj = new JSONObject(response);
                    try{
                        boolean success=obj.getBoolean("success");
                        if(success){
                            sendComment(name,email,comment);
                        }

                    }catch(JSONException e){e.printStackTrace();}

                } catch (Throwable t) {
                    Log.e("siteVerify", "Could not parse malformed JSON: \"" + response + "\"");
                }
            }
        };

        StringRequest req = new StringRequest(Request.Method.POST, url, listener, null){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                params.put("secret","6Lc3jTYUAAAAACjBfmZHeX5gKzKLoT7o9E7fq7pc");
                params.put("response",token);
                params.put("remoteip",ip);

                return params;
            }
        };
        MyVolley.getInstance(activity).addToRequestQueue(req);
    }

    public String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
            //
        }
        return null;
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
