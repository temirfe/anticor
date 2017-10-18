package kg.prosoft.anticorruption;

import android.app.ActionBar;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.support.v4.content.ContextCompat;
import android.content.pm.PackageManager;
import android.Manifest;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import kg.prosoft.anticorruption.service.Cities;

public class SetLocationActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnMarkerDragListener,
        GoogleMap.OnMapClickListener {

    private GoogleMap mMap;

    // Create a LatLngBounds that includes Osh. (sw,ne)
    private LatLngBounds BISHKEK = new LatLngBounds(new LatLng(42.790932,74.5002453), new LatLng(42.92415,74.6766403));
    private LatLngBounds KG = new LatLngBounds(new LatLng(39.567429,69.318), new LatLng(43.125856,80));
    private LatLngBounds UZ1 = new LatLngBounds(new LatLng(40.3466,69.3140), new LatLng(41.1436,71.9836));
    private LatLngBounds UZ2 = new LatLngBounds(new LatLng(40.6394,72.12), new LatLng(40.8558,72.7054));
    private LatLngBounds CHINA = new LatLngBounds(new LatLng(39.3567,74), new LatLng(40.4915,80.14));
    private LatLngBounds KZ = new LatLngBounds(new LatLng(43.0318,74.751), new LatLng(43.32,80.05));
    private LatLngBounds KZ2 = new LatLngBounds(new LatLng(42.8165,69.8812), new LatLng(43.3205,73.5349));
    private LatLngBounds LOCATION_BOUND = KG;

    protected static final String TAG = "SetLocationActivity";
    public Marker myMarker;

    /**
     * Provides the entry point to Google Play services.
     */
    protected GoogleApiClient mGoogleApiClient;

    /**
     * Represents a geographical location.
     */
    protected Location mLastLocation;

    public double lat;
    public double lng;
    public boolean marker_already=false;

    public double new_lat;
    public double new_lng;
    int city_id;
    int previous_city_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(false);
        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        Intent intent=getIntent();
        lat=intent.getDoubleExtra("lat",0.0);
        lng=intent.getDoubleExtra("lng",0.0);
        city_id=intent.getIntExtra("city_id",0);
        previous_city_id=intent.getIntExtra("previous_city_id",0);

    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.ready_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                done();
                return true;
            case R.id.action_back:
                done();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void done(){
        if( lat!=0.0 && new_lat==0.0){
            new_lat=lat;
            new_lng=lng;
        }
        Intent intent= new Intent();
        intent.putExtra("new_lat", new_lat);
        intent.putExtra("new_lng", new_lng);
        intent.putExtra("marked_city", city_id);
        setResult(RESULT_OK, intent);
        finish();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng markerLoc = KG.getCenter();
        int camZoom=8;
        if(city_id>0){
            camZoom=10;
            markerLoc= Cities.getCityCoord(city_id);
        }

        //Log.e(TAG,"CITY ID "+city_id);

        if(previous_city_id==city_id && lat!=0.0 && lng!=0.0){
            markerLoc=new LatLng(lat, lng);
            myMarker=mMap.addMarker(new MarkerOptions().position(markerLoc).draggable(true));
            marker_already=true;

        }
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerLoc,camZoom));


        mMap.getUiSettings().setZoomControlsEnabled(true);
        //mMap.setLatLngBoundsForCameraTarget(BISHKEK);

        mMap.setOnMapClickListener(this);
        mMap.setOnMarkerDragListener(this);
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        // Provides a simple way of getting a device's location and is well suited for
        // applications that do not require a fine-grained location and that do not need location
        // updates. Gets the best and most recent location currently available, which may be null
        // in rare cases when a location is not available.
        if(!marker_already){
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                //mMap.setMyLocationEnabled(true);
                if (mLastLocation != null) {
                    double mylat=mLastLocation.getLatitude();
                    double mylng=mLastLocation.getLongitude();
                    Log.e(TAG, "My current loc:"+mylat+","+mylng);

                    LatLng myLocation=new LatLng(mylat, mylng);
                    if(KG.contains(myLocation)){
                        new_lat=mylat;
                        new_lng=mylng;
                        myMarker=mMap.addMarker(new MarkerOptions().position(myLocation).draggable(true));
                    }
                } else {
                    Log.e(TAG, "No location detected 187");
                }
            } else {
                // Show rationale and request permission.
                Toast.makeText(this, R.string.grant_location_permission, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.e(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.e(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    @Override
    public void onMapClick(LatLng point) {
        //mTapTextView.setText("tapped, point=" + point);
        new_lat=point.latitude;
        new_lng=point.longitude;
        LatLng myLocation=new LatLng(new_lat, new_lng);
        if(!LOCATION_BOUND.contains(myLocation) || UZ1.contains(myLocation) || UZ2.contains(myLocation)
                || KZ.contains(myLocation) || KZ.contains(myLocation) || CHINA.contains(myLocation)){
            onlyKgWarning();
        }
        else{
            if(myMarker!=null){myMarker.remove();}
            myMarker=mMap.addMarker(new MarkerOptions().position(point).draggable(true));
        }
    }


    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        LatLng newpos=marker.getPosition();
        new_lat=newpos.latitude;
        new_lng=newpos.longitude;
        Log.e("Dragged to:", ""+newpos);
        if(!LOCATION_BOUND.contains(newpos) || UZ1.contains(newpos) || UZ2.contains(newpos)
                || KZ.contains(newpos) || KZ2.contains(newpos) || CHINA.contains(newpos)){
            onlyKgWarning();
            marker.remove();
        }
    }

    public void onlyKgWarning(){
        Toast.makeText(this, R.string.only_kg, Toast.LENGTH_LONG).show();
    }
}
