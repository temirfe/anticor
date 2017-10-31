package kg.prosoft.anticorruption;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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

import kg.prosoft.anticorruption.service.Endpoints;
import kg.prosoft.anticorruption.service.MyVolley;
import kg.prosoft.anticorruption.service.SessionManager;

public class RegisterActivity extends AppCompatActivity {
    EditText etRegName, etEmail, etPassword, etPassword2;
    SessionManager session;
    Context context;
    String email, name, password;
    String TAG="RegAct";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        context=this;
        session = new SessionManager(getApplicationContext());

        etRegName=(EditText)findViewById(R.id.id_reg_name);
        etEmail=(EditText)findViewById(R.id.id_reg_email);
        etPassword=(EditText)findViewById(R.id.id_reg_password);
        etPassword2=(EditText)findViewById(R.id.id_reg_password_repeat);

        Button btn_register=(Button)findViewById(R.id.register_button);
        btn_register.setOnClickListener(onClickRegister);
    }

    View.OnClickListener onClickRegister = new View.OnClickListener(){
        public void onClick(View v) {

            boolean allGood=true;
            View focusView = null;
            name=etRegName.getText().toString();
            email=etEmail.getText().toString();
            password=etPassword.getText().toString();
            final String password2=etPassword2.getText().toString();

            if(name.trim().equals("")){
                etRegName.setError(getResources().getString(R.string.required));
                focusView=etRegName;
                allGood=false;
            }
            if(email.trim().equals("")){
                etEmail.setError(getResources().getString(R.string.required));
                focusView=etEmail;
                allGood=false;
            } else if (!isEmailValid(email)) {
                etEmail.setError(getString(R.string.error_invalid_email));
                focusView=etEmail;
                allGood=false;
            }
            // Check for a valid password, if the user entered one.
            if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
                focusView=etPassword;
                etPassword.setError(getString(R.string.error_invalid_password));
                allGood=false;
            }
            // Check for a valid password, if the user entered one.
            if (!password.equals(password2)) {
                focusView=etPassword;
                etPassword2.setError(getString(R.string.error_password_match));
                allGood=false;
            }

            if(allGood)
            {
                doCaptcha();
            }
            else{
                focusView.requestFocus();
            }
        }
    };

    public void requestRegister(){
        final ProgressDialog progress = new ProgressDialog(this);
        progress.setTitle(getResources().getString(R.string.register));
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
        progress.show();
        String url= Endpoints.USERS;
        Response.Listener<String> listener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progress.dismiss();
                try {
                    Object json = new JSONTokener(response).nextValue();
                    if (json instanceof JSONObject){
                        JSONObject obj = new JSONObject(response);
                        try{
                            int id = obj.getInt("id");
                            //Log.i("RESPONSE id", " "+id);
                            if(id!=0){
                                String name = obj.getString("username");
                                String access_token = obj.getString("auth_key");
                                session.createLoginSession(name,email, id, access_token);
                                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                intent.putExtra("from","login");
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            }

                        }catch(JSONException e){e.printStackTrace();}
                    }

                } catch (Throwable t) {
                    Log.e("My App", "Could not parse malformed JSON: \"" + response + "\"");
                }
            }
        };

        Response.ErrorListener errorResp =new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // As of f605da3 the following should work
                NetworkResponse response = error.networkResponse;
                if (error instanceof ServerError && response != null) {
                    try {
                        String res = new String(response.data,
                                HttpHeaderParser.parseCharset(response.headers, "utf-8"));
                        // Now you can use any deserializer to make sense of data
                        JSONArray arr = new JSONArray(res);
                        JSONObject errObj = arr.getJSONObject(0);
                        if(errObj.getString("field").equals("email")){
                            AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                            builder.setMessage(R.string.email_taken).setNegativeButton(R.string.close,null).create().show();
                        }
                        else
                        {
                            AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                            builder.setMessage(R.string.app_error).setNegativeButton(R.string.close,null).create().show();
                        }
                        progress.dismiss();

                        //Log.i("RESPONSE err 1", arr.toString());
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
                params.put("username",name);
                params.put("email",email);
                params.put("password",password);

                return params;
            }
        };
        MyVolley.getInstance(this).addToRequestQueue(req);
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
                            requestRegister();
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
        MyVolley.getInstance(this).addToRequestQueue(req);
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
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        //session.clear();
        RequestQueue queue = MyVolley.getInstance(this).getRequestQueue();
        queue.cancelAll(this);
    }
}
