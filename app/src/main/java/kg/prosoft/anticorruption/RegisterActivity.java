package kg.prosoft.anticorruption;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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

public class RegisterActivity extends AppCompatActivity {
    EditText etRegName, etEmail, etPassword, etPassword2;
    SessionManager session;
    Context context;

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
            requestRegister();
        }
    };

    public void requestRegister(){
        boolean allGood=true;
        View focusView = null;
        final String name=etRegName.getText().toString();
        final String email=etEmail.getText().toString();
        final String password=etPassword.getText().toString();
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
        else{
            focusView.requestFocus();
        }
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
