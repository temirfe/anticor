package kg.prosoft.anticorruption;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.MapsInitializer;

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private String TAG = MainActivity.class.getSimpleName();
    LinearLayout ll_logo;
    RelativeLayout rl_login, rl_account;
    TextView tv_name, tv_logo;
    Button btn_login;
    Button btn_register;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
                Intent intent = new Intent(MainActivity.this, AddReportActivity.class);
                startActivity(intent);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View header = navigationView.getHeaderView(0);

        rl_login = (RelativeLayout) header.findViewById(R.id.id_rl_login);
        rl_account = (RelativeLayout) header.findViewById(R.id.id_rl_account);
        ll_logo = (LinearLayout) header.findViewById(R.id.id_ll_logo);

        btn_login = (Button) header.findViewById(R.id.id_btn_login);
        btn_register = (Button) header.findViewById(R.id.id_btn_register);
        tv_name = (TextView) header.findViewById(R.id.id_tv_name);
        tv_logo = (TextView) header.findViewById(R.id.id_tv_logo);

        Menu nav_Menu = navigationView.getMenu();

        if(session.isLoggedIn()){
            tv_name.setText(session.getName());
            rl_account.setOnClickListener(onClickName);
            rl_login.setVisibility(View.GONE);
        }
        else{
            rl_login.setVisibility(View.VISIBLE);
            btn_login.setOnClickListener(onClickLogin);
            btn_register.setOnClickListener(onClickRegister);
            rl_account.setVisibility(View.GONE);
            nav_Menu.findItem(R.id.nav_logout).setVisible(false);
        }
    }

    View.OnClickListener onClickName = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(MainActivity.this, AccountActivity.class);
            startActivity(intent);
        }
    };

    View.OnClickListener onClickLogin = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent loginIntent = new Intent(MainActivity.this,LoginActivity.class);
            startActivity(loginIntent);
        }
    };

    View.OnClickListener onClickRegister = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent regIntent = new Intent(MainActivity.this,RegisterActivity.class);
            startActivity(regIntent);
        }
    };

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {
            Intent intent= new Intent(MainActivity.this, MapsActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_slideshow) {
            Intent intent= new Intent(MainActivity.this, TabActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_manage) {
            Intent intent= new Intent(MainActivity.this, AuthorityListActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        } else if (id == R.id.nav_logout) {
            session.logoutUser();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
