package kg.prosoft.anticorruption;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
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
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.Profile;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import kg.prosoft.anticorruption.service.Endpoints;
import kg.prosoft.anticorruption.service.LocaleHelper;
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
    LoginButton fbLoginButton;
    CallbackManager callbackManager;
    TwitterLoginButton twiLoginButton;

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

        //--- facebook login start --//
        fbLoginButton = (LoginButton) findViewById(R.id.fb_login_button);
        fbLoginButton.setReadPermissions("email");
        // Other app specific specialization

        // Callback registration
        callbackManager = CallbackManager.Factory.create();
        fbLoginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // App code
                AccessToken fb_access_token=loginResult.getAccessToken();
                Set fb_granted_perm_set=loginResult.getRecentlyGrantedPermissions();
                Set fb_denied_perm_set=loginResult.getRecentlyDeniedPermissions();

                Log.e("FbSuc","at: "+fb_access_token);
                Log.e("FbSuc","gp: "+fb_granted_perm_set);
                Log.e("FbSuc","dp: "+fb_denied_perm_set);
                Log.e("FbSuc","uid: "+fb_access_token.getUserId());
            }

            @Override
            public void onCancel() {
                Log.e("FBonCancel","yep");
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
                Log.e("FBonError",exception+"");
            }
        });

        //later
        Log.e("LoginAct","fb token:"+AccessToken.getCurrentAccessToken());
        Log.e("LoginAct","fb profile:"+Profile.getCurrentProfile());
        //--- facebook login end --//

        //twitter login start//
        twiLoginButton = (TwitterLoginButton) findViewById(R.id.twi_login_button);
        twiLoginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                // Do something with result, which provides a TwitterSession for making API calls
                Log.e("twiSuccess",result+"");
                TwitterSession twiSession = result.data;
                Log.e("twiSuccess","user_id "+twiSession.getUserId());
                Log.e("twiSuccess","user_name "+twiSession.getUserName());

                //request email
                TwitterAuthClient twiAuthClient = new TwitterAuthClient();
                twiAuthClient.requestEmail(twiSession, new Callback<String>() {
                    @Override
                    public void success(Result<String> result) {
                        // Do something with the result, which provides the email address
                        String resString=result.data;
                        Log.e("twiEmailSuccess","res "+resString);
                    }

                    @Override
                    public void failure(TwitterException exception) {
                        // Do something on failure
                        Log.e("twiEmailFail",exception.toString());
                    }
                });
            }

            @Override
            public void failure(TwitterException exception) {
                // Do something on failure
                Log.e("twiFailure",exception+"");
            }
        });

        //later
        TwitterSession twiSession = TwitterCore.getInstance().getSessionManager().getActiveSession();
        if(twiSession!=null){
            TwitterAuthToken twiAuthToken = twiSession.getAuthToken();
            String twiToken = twiAuthToken.token;
            String twiSecret = twiAuthToken.secret;
            Log.e("LoginAct","twi token:"+twiToken);
            Log.e("LoginAct","twi secret:"+twiSecret);
        }
        //--- twitter login end --//


    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
        twiLoginButton.onActivityResult(requestCode, resultCode, data);
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


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        String lang=session.getLanguage();
        if(lang.isEmpty()){lang="ky";}
        LocaleHelper.setLocale(context, lang);
    }

    /*@Override
    protected void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);
    }*/
}
