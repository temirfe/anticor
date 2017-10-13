package kg.prosoft.anticorruption;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import kg.prosoft.anticorruption.service.Endpoints;
import kg.prosoft.anticorruption.service.MyVolley;
import kg.prosoft.anticorruption.service.SessionManager;

public class LoginActivity extends AppCompatActivity {

    /**
     * save credential in session
     */
    SessionManager session;


    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */

    // UI references.
    private AutoCompleteTextView mUsernameOrEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        context=this;
        session = new SessionManager(getApplicationContext());

        // Set up the login form.
        mUsernameOrEmailView = (AutoCompleteTextView) findViewById(R.id.username_or_email);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        TextView tv_register = (TextView)findViewById(R.id.id_tv_register);
        tv_register.setOnClickListener(onClickGoToRegister);
        TextView tv_forgot = (TextView)findViewById(R.id.id_tv_forgot);
        tv_forgot.setOnClickListener(onClickForgot);
    }

    View.OnClickListener onClickGoToRegister = new View.OnClickListener(){
        public void onClick(View v) {
            Intent regint=new Intent(context, RegisterActivity.class);
            startActivity(regint);
        }
    };

    View.OnClickListener onClickForgot = new View.OnClickListener(){
        public void onClick(View v) {
            Intent regint=new Intent(context, RegisterActivity.class);
            startActivity(regint);
        }
    };

    /**
     * Attempts to sign in the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {

        // Reset errors.
        mUsernameOrEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String username_or_email = mUsernameOrEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(username_or_email)) {
            mUsernameOrEmailView.setError(getString(R.string.error_field_required));
            focusView = mUsernameOrEmailView;
            cancel = true;
        }/* else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }*/

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            requestLogin(username_or_email, password);
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

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    public void requestLogin(final String username_or_email, final String password){

        Response.Listener<String> responseListener = new Response.Listener<String>(){
            @Override
            public void onResponse(String response) {
                Log.i("LOGIN ACT", "response:"+response);
                try {
                    if(response.contains("not_username")){
                        showProgress(false);
                        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                        builder.setMessage(R.string.user_not_found).setNegativeButton(R.string.close,null).create().show();
                    }
                    else if(response.contains("not_password")){
                        showProgress(false);
                        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                        builder.setMessage(R.string.password_error).setNegativeButton(R.string.close,null).create().show();
                    }
                    else{
                        JSONObject jsonResponse = new JSONObject(response);
                        int user_id = jsonResponse.getInt("id");
                        if(user_id!=0){
                            String name = jsonResponse.getString("username");
                            String email = jsonResponse.getString("email");
                            String access_token = jsonResponse.getString("auth_key");
                            Log.i("LOGIN ACT", "auth_key:"+access_token+" user_id:"+user_id);

                            session.createLoginSession(name,email, user_id, access_token);

                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.putExtra("from","login");
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        }
                        else{
                            AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                            builder.setMessage(R.string.login_fail).setNegativeButton(R.string.close,null).create().show();
                        }
                    }

                } catch(JSONException e){
                    e.printStackTrace();
                }
            }
        };

        Response.ErrorListener errorListener =new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Log.e("Response", error.toString());
                Toast.makeText(getApplicationContext(), "Произошла ошибка, перезагрузите приложение", Toast.LENGTH_LONG).show();
            }
        };

        String url = Endpoints.LOGIN;
        StringRequest loginRequest = new StringRequest(Request.Method.POST, url,responseListener, errorListener){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                params.put("login",username_or_email);
                params.put("password",password);

                return params;
            }
        };
        MyVolley.getInstance(this).addToRequestQueue(loginRequest);
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
