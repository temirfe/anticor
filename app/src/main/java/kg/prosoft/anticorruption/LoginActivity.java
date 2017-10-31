package kg.prosoft.anticorruption;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
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
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
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

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import kg.prosoft.anticorruption.service.Endpoints;
import kg.prosoft.anticorruption.service.LocaleHelper;
import kg.prosoft.anticorruption.service.MyVolley;
import kg.prosoft.anticorruption.service.SessionManager;
import ru.ok.android.sdk.Odnoklassniki;
import ru.ok.android.sdk.OkListener;
import ru.ok.android.sdk.OkAuthListener;
import ru.ok.android.sdk.OkRequestMode;
import ru.ok.android.sdk.util.OkAuthType;
import ru.ok.android.sdk.util.OkDevice;
import ru.ok.android.sdk.util.OkScope;

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener{

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
    Context context;
    LoginButton fbLoginButton;
    CallbackManager callbackManager;
    TwitterLoginButton twiLoginButton;
    GoogleApiClient mGoogleApiClient;
    int RC_SIGN_IN=4433;
    private ProgressDialog mProgressDialog;
    String TAG="LoginAct";

    protected Odnoklassniki odnoklassniki;
    protected static final String OK_APP_ID = "1256974336";
    protected static final String OK_APP_KEY = "CBAJOMOLEBABABABA";
    protected static final String OK_REDIRECT_URL = "okauth://ok1256974336";

    String provider, provider_data, puid, provider_email, provider_username,provider_name;
    Button btn_fb, btn_twi;

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

        TextView tv_register = (TextView)findViewById(R.id.id_tv_register);
        tv_register.setOnClickListener(onClickGoToRegister);
        TextView tv_forgot = (TextView)findViewById(R.id.id_tv_forgot);
        tv_forgot.setOnClickListener(onClickForgot);
        btn_fb=(Button)findViewById(R.id.id_btn_fb);
        btn_twi=(Button)findViewById(R.id.id_btn_twi);
        btn_fb.setOnClickListener(fbLoginClick);
        btn_twi.setOnClickListener(twiLoginClick);

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
                String fb_user_id=fb_access_token.getUserId();

                Log.e("FbSuc","at: "+fb_access_token);
                Log.e("FbSuc","uid: "+fb_access_token.getUserId());

                Profile profile = Profile.getCurrentProfile();
                final String fbName = profile.getName();
                final Uri fb_link = profile.getLinkUri();
                Log.e("FbSuc","name: "+fbName +" uri: "+fb_link);

                GraphRequest request = GraphRequest.newMeRequest(
                        loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                Log.v("LoginActivity", response.toString());
                                try {
                                    // Application code
                                    String email = object.getString("email");
                                    String name = object.getString("name");
                                    String id = object.getString("id");
                                    Log.e("FbGraph","email: "+email +" uri: "+name+" id:"+id);

                                    puid=id;
                                    provider_name=fbName;
                                    provider="facebook";
                                    provider_data=response.toString()+" "+fbName+" "+fb_link;
                                    provider_email=email;
                                    provider_username="";
                                    if(puid!=null && !puid.isEmpty()){
                                        postSocial(provider, provider_data, puid, provider_email, provider_username,provider_name);
                                    }

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,email,link");
                request.setParameters(parameters);
                request.executeAsync();
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
        //Log.e("LoginAct","fb token:"+AccessToken.getCurrentAccessToken());
        //Log.e("LoginAct","fb profile:"+Profile.getCurrentProfile());
        //--- facebook login end --//

        //twitter login start//
        twiLoginButton = (TwitterLoginButton) findViewById(R.id.twi_login_button);
        twiLoginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                // Do something with result, which provides a TwitterSession for making API calls
                Log.e("twiSuccess",result+"");
                TwitterSession twiSession = result.data;
                final long id=twiSession.getUserId();
                final String username=twiSession.getUserName();
                Log.e("twiSuccess","user_id "+id);
                Log.e("twiSuccess","user_name "+username);

                //request email
                TwitterAuthClient twiAuthClient = new TwitterAuthClient();
                twiAuthClient.requestEmail(twiSession, new Callback<String>() {
                    @Override
                    public void success(Result<String> result) {
                        // Do something with the result, which provides the email address
                        String email=result.data;
                        Log.e("twiEmailSuccess","res "+email);
                        puid=Long.toString(id);
                        provider_name="";
                        provider="twitter";
                        provider_data="username: "+username+" email: "+email;
                        provider_email=email;
                        provider_username=username;
                        if(puid!=null && !puid.isEmpty()){
                            Log.e("twi",provider_data+" id:"+id);
                            postSocial(provider, provider_data, puid, provider_email, provider_username,provider_name);
                        }
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
        /*TwitterSession twiSession = TwitterCore.getInstance().getSessionManager().getActiveSession();
        if(twiSession!=null){
            TwitterAuthToken twiAuthToken = twiSession.getAuthToken();
            String twiToken = twiAuthToken.token;
            String twiSecret = twiAuthToken.secret;
            Log.e("LoginAct","twi token:"+twiToken);
            Log.e("LoginAct","twi secret:"+twiSecret);
        }*/
        //--- twitter login end --//

        //--- google login start --//
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        // Set the dimensions of the sign-in button.
        SignInButton signInButton = (SignInButton) findViewById(R.id.google_login_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);

        signInButton.setOnClickListener(googleLoginClick);
        //--- google login end --//

        //--- ok login start --//
        Button okLoginButton = (Button) findViewById(R.id.ok_login_button);
        okLoginButton.setOnClickListener(new LoginClickListener(OkAuthType.WEBVIEW_OAUTH));
        odnoklassniki = Odnoklassniki.createInstance(this, OK_APP_ID, OK_APP_KEY);
        /*odnoklassniki.checkValidTokens(new OkListener() {
            @Override
            public void onSuccess(JSONObject json) {
                Log.e("OkSuccess","token valid "+json);
            }

            @Override
            public void onError(String error) {
                Log.e("OkError",String.format("%s: %s", getString(R.string.error), error));
            }
        });*/
        //--- ok login end --//
    }

    //my custom button click performs on facebook button click which is hidden
    //i needed custom buttons because i want them to have same size
    View.OnClickListener fbLoginClick = new View.OnClickListener(){
        @Override
        public void onClick(View view) {
            fbLoginButton.performClick();
        }
    };

    //the same with twi
    View.OnClickListener twiLoginClick = new View.OnClickListener(){
        @Override
        public void onClick(View view) {
            twiLoginButton.performClick();
        }
    };

    View.OnClickListener googleLoginClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
            startActivityForResult(signInIntent, RC_SIGN_IN);
        }
    };

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d("LogInAct", "onConnectionFailed:" + connectionResult);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
        twiLoginButton.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleGoogleSignInResult(result);
        }
        if (Odnoklassniki.getInstance().isActivityRequestOAuth(requestCode)) {
            Odnoklassniki.getInstance().onAuthActivityResult(requestCode, resultCode, data, getAuthListener());
        } else if (Odnoklassniki.getInstance().isActivityRequestViral(requestCode)) {
            Odnoklassniki.getInstance().onActivityResultResult(requestCode, resultCode, data, getToastListener());
        }
    }

    private void testIfInstallationSourceIsOK() {
        Map<String, String> args = Collections.singletonMap("adv_id", OkDevice.getAdvertisingId(LoginActivity.this));
        odnoklassniki.requestAsync("sdk.getInstallSource", args, EnumSet.of(OkRequestMode.UNSIGNED), new OkListener() {
            @Override
            public void onSuccess(JSONObject json) {
                try {
                    int result = Integer.parseInt(json.optString("result"));
                    String msg=result > 0 ?
                            "application installation caused by OK app (" + result + ")" :
                            "application is not caused by OK app (" + result + ")";
                    Log.e("okTest",msg);
                } catch (NumberFormatException e) {
                    Log.e("okTestCatch","invalid value while getting install source " + json);
                }
            }

            @Override
            public void onError(String error) {
                Log.e("okTestError","error while getting install source " + error);
            }
        });
    }

    /**
     * Creates a listener that displays result as a toast message
     */
    @NonNull
    private OkListener getToastListener() {
        return new OkListener() {
            @Override
            public void onSuccess(final JSONObject json) {
                Log.e("OkToastSuccess",json.toString());
                getOkUser();
            }

            @Override
            public void onError(String error) {
                Log.e("OkToastError",String.format("%s: %s", getString(R.string.error), error));
            }
        };
    }

    private void getOkUser(){
        odnoklassniki.requestAsync("users.getCurrentUser", null, null, new OkListener() {
            @Override
            public void onSuccess(JSONObject json) {
                try {
                    if(json.has("uid")){puid=json.getString("uid");}
                    provider_name="";
                    if(json.has("name")){provider_name=json.getString("name");}
                    provider="odnoklassniki";
                    provider_data=json.toString();
                    provider_email="";
                    provider_username="";
                    if(puid!=null && !puid.isEmpty()){
                        postSocial(provider, provider_data, puid, provider_email, provider_username,provider_name);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Log.e("getOkUser success",json.toString());
            }

            @Override
            public void onError(String error) {
                Log.e("getOkUser error","Get current user failed: " + error);
            }
        });
    }

    /**
     * Creates a listener that is run on OAUTH authorization completion
     */
    @NonNull
    private OkAuthListener getAuthListener() {
        return new OkAuthListener() {
            @Override
            public void onSuccess(final JSONObject json) {
                try {
                    Log.e("OkOnSuccess",json.toString());
                    Log.e("OkOnSuccess",String.format("access_token: %s", json.getString("access_token")));
                    //testIfInstallationSourceIsOK();
                    getOkUser();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //showForm();
            }

            @Override
            public void onError(String error) {
                Log.e("AuthError",String.format("%s: %s", getString(R.string.error), error));
            }

            @Override
            public void onCancel(String error) {
                        Log.e("AuthCancel",error+"");
            }
        };
    }

    protected class LoginClickListener implements View.OnClickListener {
        private OkAuthType authType;

        public LoginClickListener(OkAuthType authType) {
            this.authType = authType;
        }

        @Override
        public void onClick(final View view) {
            odnoklassniki.requestAuthorization(LoginActivity.this, OK_REDIRECT_URL, authType, OkScope.VALUABLE_ACCESS);
        }
    }

    private void handleGoogleSignInResult(GoogleSignInResult result) {
        Log.d("LoginAct", "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            String name=acct.getDisplayName();
            String email=acct.getEmail();
            Uri photo=acct.getPhotoUrl();
            String id=acct.getId();
            Log.e("googSuccess","name: "+name);
            Log.e("googSuccess","email: "+email);
            Log.e("googSuccess","id: "+id);
            Log.e("googSuccess","token id: "+acct.getIdToken());

            puid=id;
            provider_name=name;
            provider="google";
            provider_data="name: "+name+" email: "+email+" photo:"+photo;
            provider_email=email;
            provider_username="";
            if(puid!=null && !puid.isEmpty()){
                postSocial(provider, provider_data, puid, provider_email, provider_username,provider_name);
            }
            //updateUI(true);
        } else {
            // Signed out, show unauthenticated UI.
            //updateUI(false);
        }
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
            showProgressDialog();
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

    public void requestLogin(final String username_or_email, final String password){

        Response.Listener<String> responseListener = new Response.Listener<String>(){
            @Override
            public void onResponse(String response) {
                Log.i("LOGIN ACT", "response:"+response);
                try {
                    if(response.contains("not_username")){
                        hideProgressDialog();
                        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                        builder.setMessage(R.string.user_not_found).setNegativeButton(R.string.close,null).create().show();
                    }
                    else if(response.contains("not_password")){
                        hideProgressDialog();
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

                            hideProgressDialog();
                            /*Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.putExtra("from","login");
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);*/
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

    public void postSocial(final String provider, final String provider_data, final String puid,
                           final String provider_email, final String provider_username, final String provider_name){
        showProgressDialog();
        Response.Listener<String> responseListener = new Response.Listener<String>(){
            @Override
            public void onResponse(String response) {
                Log.e("Social", "response:"+response);
                try {
                    if(response.contains("ask_email")){
                        askEmail();
                    }
                    else if(response.contains("error")){
                        hideProgressDialog();
                        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                        builder.setMessage(R.string.login_fail).setNegativeButton(R.string.close,null).create().show();
                    }
                    else{
                        hideProgressDialog();
                        JSONObject jsonResponse = new JSONObject(response);
                        int user_id = jsonResponse.getInt("id");
                        if(user_id!=0){
                            String name = jsonResponse.getString("username");
                            String email = jsonResponse.getString("email");
                            String access_token = jsonResponse.getString("auth_key");
                            Log.i("LOGIN ACT", "auth_key:"+access_token+" user_id:"+user_id);

                            session.createLoginSession(name,email, user_id, access_token);

                            /*Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.putExtra("from","login");
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);*/
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

        String url = Endpoints.SOCIAL;
        StringRequest loginRequest = new StringRequest(Request.Method.POST, url,responseListener, errorListener){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                params.put("puid",puid);
                params.put("provider",provider);
                params.put("data",provider_data);
                params.put("email",provider_email);
                params.put("username",provider_username);
                params.put("name",provider_name);

                return params;
            }
        };
        MyVolley.getInstance(this).addToRequestQueue(loginRequest);
    }

    public void askEmail(){
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        final EditText edittext = new EditText(LoginActivity.this);
        builder.setTitle(R.string.prompt_email);
        builder.setMessage(R.string.enter_email_to_complete);
        builder.setView(edittext);
        builder.setPositiveButton(R.string.submit, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String email = edittext.getText().toString();
                if(isEmailValid(email)){
                    provider_email=email;
                    postSocial(provider, provider_data, puid, provider_email, provider_username,provider_name);
                }
                else{
                    askEmail();
                }
                Log.e(TAG,"alert email "+email);
            }
        });
        builder.setNegativeButton(R.string.close,new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                hideProgressDialog();
            }
        });
        builder.show();
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
    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        hideProgressDialog();
    }

    //later if needed
    @Override
    public void onStart() {
        super.onStart();

        /*OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if (opr.isDone()) {
            // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
            // and the GoogleSignInResult will be available instantly.
            Log.d(TAG, "Got gooogle cached sign-in");
            GoogleSignInResult result = opr.get();
            //handleGoogleSignInResult(result);
        } else {
            // If the user has not previously signed in on this device or the sign-in has expired,
            // this asynchronous branch will attempt to sign in the user silently.  Cross-device
            // single sign-on will occur in this branch.
            showProgressDialog();
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(GoogleSignInResult googleSignInResult) {
                    hideProgressDialog();
                    //handleGoogleSignInResult(googleSignInResult);
                }
            });
        }*/
    }

    private void signOutGoogle() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        //updateUI(false);
                    }
                });
    }

    private void revokeAccessGoogle() {
        Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        //updateUI(false);
                    }
                });
    }

}
