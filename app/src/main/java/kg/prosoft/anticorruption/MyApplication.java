package kg.prosoft.anticorruption;

import android.app.Application;
import android.content.res.Configuration;
//import android.support.multidex.MultiDexApplication;

import com.twitter.sdk.android.core.Twitter;

/**
 * Created by ProsoftPC on 10/20/2017.
 */

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Twitter.initialize(this);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
}
