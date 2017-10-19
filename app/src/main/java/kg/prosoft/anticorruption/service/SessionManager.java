package kg.prosoft.anticorruption.service;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import kg.prosoft.anticorruption.LoginActivity;
import kg.prosoft.anticorruption.MainActivity;
//import kg.prosoft.anticorruption.model.User;

/**
 * Created by ProsoftPC on 4/18/2017.
 */

public class SessionManager {

    private String TAG = SessionManager.class.getSimpleName();

    // Shared Preferences
    SharedPreferences pref;

    // Editor for Shared preferences
    SharedPreferences.Editor editor;

    // Context
    Context _context;

    // Shared pref mode
    int PRIVATE_MODE = 0;

    // Sharedpref file name
    private static final String PREF_NAME = "fcm_chat";

    // All Shared Preferences Keys
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_NOTIFICATIONS = "notifications";
    public static final String KEY_TEST= "test";
    public static final String KEY_CTG_DEPEND= "ctg_depend";
    public static final String KEY_AUTHORITY_DEPEND= "authority_depend";
    public static final String KEY_VOCABULARY_DEPEND= "authority_depend";
    public static final String KEY_NEWS_DEPEND= "news_depend";
    public static final String KEY_REPORT_DEPEND= "report_depend";
    public static final String ACCESS_TOKEN = "access_token";
    public static final String KEY_PHONE = "phone";
    public static final String KEY_ADMIN_PHONE = "admin_phone";
    public static final String KEY_ADDRESS = "address";
    public static final String KEY_CONTACT = "contact";
    public static final String KEY_NAME = "name";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_CITY_ID= "city_id";
    public static final String KEY_LANGUAGE= "language";

    // All Shared Preferences Keys
    private static final String IS_LOGIN = "IsLoggedIn";
    public static final String dependChecked ="dependChecked";
    public static final String authorityDependChecked ="authorityDependChecked";
    public static final String vocabularyDependChecked ="vocabularyDependChecked";

    private static final String ContactSaved = "IsContactSaved";

    // Constructor
    public SessionManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }


    /*public void storeUser(User user) {
        editor.putString(KEY_USER_ID, user.getId());
        editor.putString(KEY_USER_NAME, user.getName());
        editor.putString(KEY_USER_EMAIL, user.getEmail());
        editor.commit();

        Log.e(TAG, "User is stored in shared preferences. " + user.getName() + ", " + user.getEmail());
    }*/

    public String getRegId() {
        return pref.getString("regId","");
    }

    public void storeFCMid(String token) {
        //Log.e(TAG, "TOKEN IS STORED: " + token);
        editor.putString("regId", token);
        editor.commit();
    }

    /*public User getUser() {
        if (pref.getString(KEY_USER_ID, null) != null) {
            String id, name, email;
            id = pref.getString(KEY_USER_ID, null);
            name = pref.getString(KEY_USER_NAME, null);
            email = pref.getString(KEY_USER_EMAIL, null);

            User user = new User(id, name, email);
            return user;
        }
        return null;
    }*/

    public void addNotification(String notification) {

        // get old notifications
        String oldNotifications = getNotifications();

        if (oldNotifications != null) {
            oldNotifications += "|" + notification;
        } else {
            oldNotifications = notification;
        }

        editor.putString(KEY_NOTIFICATIONS, oldNotifications);
        editor.commit();
    }

    public String getNotifications() {
        return pref.getString(KEY_NOTIFICATIONS, null);
    }

    public void clear() {
        editor.clear();
        editor.commit();
    }

    public void createTestSession(String text){
        //editor.putBoolean(itrue, true);

        // Storing name in pref
        editor.putString(KEY_TEST, text);

        // commit changes
        editor.commit();
    }

    public String getTest() {
        return pref.getString(KEY_TEST,"");
    }

    public void setDepend(String text){
        //editor.putBoolean(itrue, true);

        // Storing name in pref
        editor.putString(KEY_CTG_DEPEND, text);

        // commit changes
        editor.commit();
    }

    public String getDepend() {
        return pref.getString(KEY_CTG_DEPEND,"");
    }

    public boolean isDependChecked(){
        return pref.getBoolean(dependChecked, false);
    }
    public void setDependChecked(boolean check){
        editor.putBoolean(dependChecked, check);
        editor.commit();
    }

    /** News depend **/
    public void setNewsDepend(String text){
        editor.putString(KEY_NEWS_DEPEND, text);
        editor.commit();
    }
    public String getNewsDepend() {
        return pref.getString(KEY_NEWS_DEPEND,"");
    }

    /** Report depend **/
    public void setReportDepend(String text){
        editor.putString(KEY_REPORT_DEPEND, text);
        editor.commit();
    }
    public String getReportDepend() {
        return pref.getString(KEY_REPORT_DEPEND,"");
    }

    /**Authority depend**/
    public void setAuthorityDepend(String text){
        editor.putString(KEY_AUTHORITY_DEPEND, text);
        editor.commit();
    }
    public String getAuthorityDepend() {
        return pref.getString(KEY_AUTHORITY_DEPEND,"");
    }

    public boolean isAuthorityDependChecked(){
        return pref.getBoolean(authorityDependChecked, false);
    }
    public void setAuthorityDependChecked(boolean check){
        editor.putBoolean(authorityDependChecked, check);
        editor.commit();
    }

    /**Vocabulary depend**/
    public void setVocabularyDepend(String text){
        editor.putString(KEY_VOCABULARY_DEPEND, text);
        editor.commit();
    }
    public String getVocabularyDepend() {
        return pref.getString(KEY_VOCABULARY_DEPEND,"");
    }

    public boolean isVocabularyDependChecked(){
        return pref.getBoolean(vocabularyDependChecked, false);
    }
    public void setVocabularyDependChecked(boolean check){
        editor.putBoolean(vocabularyDependChecked, check);
        editor.commit();
    }

    /**
     * Create login session
     * */
    public void createLoginSession(String name, String email, int user_id, String access_token){
        // Storing login value as TRUE
        editor.putBoolean(IS_LOGIN, true);

        // Storing name in pref
        editor.putString(KEY_USER_NAME, name);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putInt(KEY_USER_ID, user_id);
        editor.putString(ACCESS_TOKEN, access_token);

        // commit changes
        editor.commit();
        Log.e("SESSION","login session created");
    }

    /**
     * Check login method wil check user login status
     * If false it will redirect user to login page
     * Else won't do anything
     * */
    public void checkLogin(){
        // Check login status
        if(!this.isLoggedIn()){
            pleaseLogin();
        }

    }

    public void pleaseLogin(){
        // user is not logged in redirect him to Login Activity
        Intent i = new Intent(_context, LoginActivity.class);
        // Closing all the Activities
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // Staring Login Activity
        _context.startActivity(i);
    }

    /**
     * Create login session
     * */
    public void createContactSession(String name, String email, String contact){
        editor.putBoolean(ContactSaved, true);

        // Storing name in pref
        editor.putString(KEY_NAME, name);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_CONTACT, contact);

        // commit changes
        editor.commit();
    }

    /**
     * Create login session
     * */
    public void saveNameEmail(String name, String email){
        // Storing name in pref
        editor.putString(KEY_NAME, name);
        editor.putString(KEY_EMAIL, email);

        // commit changes
        editor.commit();
    }

    /**
     * Clear session details
     * */
    public void logoutUser(){
        // Clearing all data from Shared Preferences
        clear();

        // After logout redirect user to Loing Activity
        Intent i = new Intent(_context, MainActivity.class);
        // Closing all the Activities
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // Staring Login Activity
        _context.startActivity(i);
    }

    /**
     * Quick check for login
     * **/
    // Get Login State
    public boolean isLoggedIn(){
        return pref.getBoolean(IS_LOGIN, false);
    }

    public int getUserId() {
        return pref.getInt(KEY_USER_ID,0);
    }

    public String getAccessToken() {
        return pref.getString(ACCESS_TOKEN,"");
    }
    public String getUserEmail() {
        return pref.getString(KEY_USER_EMAIL,"");
    }
    public String getEmail() {
        return pref.getString(KEY_EMAIL,"");
    }
    public void setContact(String contact) {
        editor.putString(KEY_CONTACT,contact);
        editor.commit();
    }
    public String getContact() {return pref.getString(KEY_CONTACT,"");}

    public String getUserName() {
        return pref.getString(KEY_USER_NAME,"");
    }
    public String getName() {
        return pref.getString(KEY_NAME,"");
    }

    public void setPhone(String phone) {
        editor.putString(KEY_PHONE,phone);
        editor.commit();
    }
    public void setEmail(String email) {
        editor.putString(KEY_USER_EMAIL,email);
        editor.commit();
    }
    public String getPhone() {
        return pref.getString(KEY_PHONE,"");
    }
    public void setAddress(String address) {
        editor.putString(KEY_ADDRESS,address);
        editor.commit();
    }
    public String getAddress() {
        return pref.getString(KEY_ADDRESS,"");
    }

    public void setAdminPhone(String phone) {editor.putString(KEY_ADMIN_PHONE,phone);editor.commit();}
    public String getAdminPhone() {
        return pref.getString(KEY_ADMIN_PHONE,"");
    }

    public void setLanguage(String s) {editor.putString(KEY_LANGUAGE,s);editor.commit();}
    public String getLanguage() {
        return pref.getString(KEY_LANGUAGE,"");
    }

    public void setCityId(int id) {editor.putInt(KEY_CITY_ID,id);editor.commit();}
    public int getCityId() {
        return pref.getInt(KEY_CITY_ID,0);
    }
}
