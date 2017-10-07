package kg.prosoft.anticorruption;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import kg.prosoft.anticorruption.service.Endpoints;
import kg.prosoft.anticorruption.service.MyVolley;
import kg.prosoft.anticorruption.service.CustomInfoWindowAdapter;
import kg.prosoft.anticorruption.service.Report;
import kg.prosoft.anticorruption.service.ReportMapItem;


/**
 * A simple {@link Fragment} subclass.
 */
public class MapReportsFragment extends Fragment implements
        OnMapReadyCallback, ClusterManager.OnClusterItemInfoWindowClickListener<ReportMapItem>{
    private GoogleMap mMap;
    MapView mMapView;
    Context context;
    Activity activity;
    Uri.Builder uriB;
    public int user_id;
    SparseArray<Report> reportSparse;
    private ClusterManager<ReportMapItem> mClusterManager;

    public MapReportsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView=  inflater.inflate(R.layout.fragment_map_reports, container, false);
        activity=getActivity();
        context=activity.getApplicationContext();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        //mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_list_markers);
        /*if (Build.VERSION.SDK_INT < 21) {
            mapFragment = (SupportMapFragment) getActivity().getSupportFragmentManager()
                    .findFragmentById(R.id.map_list_markers);
        } else {
            mapFragment = (SupportMapFragment) getChildFragmentManager()
                    .findFragmentById(R.id.map_list_markers);
        }*/
        mMapView = (MapView) rootView.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume(); // needed to get the map to display immediately
        mMapView.getMapAsync(this);

        reportSparse=new SparseArray<>();

        return rootView;
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

        LatLngBounds KG = new LatLngBounds(new LatLng(39.567429,69.318), new LatLng(43.125856,80));
        LatLng center=KG.getCenter();
        //LatLng bishkek = new LatLng(42.8742589,74.6131682);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(center,6));
        mClusterManager = new ClusterManager<>(context, mMap);
        mClusterManager.setRenderer(new CustomRenderer<>(getActivity(), mMap, mClusterManager));
        mMap.setOnCameraIdleListener(mClusterManager);
        mMap.setOnMarkerClickListener(mClusterManager);

        mMap.setInfoWindowAdapter(mClusterManager.getMarkerManager());
        mMap.setOnInfoWindowClickListener(mClusterManager); //added
        mClusterManager.setOnClusterItemInfoWindowClickListener(this); //added

        mClusterManager
                .setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<ReportMapItem>() {
                    @Override
                    public boolean onClusterItemClick(ReportMapItem item) {
                        return false;
                    }
                });
        mClusterManager.getMarkerCollection().setOnInfoWindowAdapter(new CustomInfoWindowAdapter(activity.getLayoutInflater()));

        populateMap();
    }

    @Override
    public void onClusterItemInfoWindowClick(ReportMapItem myItem) {
        int report_id=myItem.getReportId();
        Log.e("ClusterInfo",report_id+"");
        Report report = reportSparse.get(report_id);
        Intent intent = new Intent(activity, ReportViewActivity.class);
        intent.putExtra("id",report_id);
        intent.putExtra("title",report.getTitle());
        intent.putExtra("text",report.getText());
        intent.putExtra("date",report.getDate());
        intent.putExtra("lat",report.getLat());
        intent.putExtra("lng",report.getLng());
        startActivity(intent);
    }

    public void populateMap(){
        uriB = new Uri.Builder();
        uriB.scheme(Endpoints.SCHEME).authority(Endpoints.AUTHORITY).appendPath("api").appendPath("reports");

        if(user_id!=0)//when MainActivity launched by AccountActivity bc of "show my incidents"
        {
            uriB.appendQueryParameter("user_id", ""+user_id);
        }
        //Log.i("MAP USER ID", ""+user_id);

        String uri = uriB.build().toString();

        Response.Listener<JSONArray> listener = new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try{
                    mMap.clear();
                    int leng=response.length();
                    if(leng>0){
                        for(int i=0; i < leng; i++){
                            JSONObject jsonObject = response.getJSONObject(i);
                            int id = jsonObject.getInt("id");
                            String title=jsonObject.getString("title");
                            String text=jsonObject.getString("text");
                            String date=jsonObject.getString("date");
                            //int category_id=jsonObject.getInt("category_id");
                            double lat=jsonObject.getDouble("lat");
                            double lng=jsonObject.getDouble("lon");
                            reportSparse.put(id,new Report(id,title,text,date,lat,lng));
                            mClusterManager.addItem(new ReportMapItem(lat, lng, title,id));

                            /*myMarker=mMap.addMarker(
                                    new MarkerOptions()
                                            .position(new LatLng(lat,lng))
                                            .title(title)
                            );
                            myMarker.setTag(id);*/

                        }
                        mClusterManager.cluster();
                    }
                    else{
                        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                        builder.setMessage(R.string.no_result).setNegativeButton(R.string.close,null).create().show();
                    }


                }catch(JSONException e){e.printStackTrace();}
            }
        };
        Response.ErrorListener errorListener =new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        };

        JsonArrayRequest volReq = new JsonArrayRequest(Request.Method.GET, uri, null, listener,errorListener);
        MyVolley.getInstance(context).addToRequestQueue(volReq);
    }

    /*@Override
    public void onInfoWindowClick(Marker marker) {
        Integer markId = (Integer) marker.getTag();
        Log.e("MapReport",markId+"");
        //Intent intent = new Intent(activity, ReportViewActivity.class);
        //intent.putExtra("id",markId);
    }*/

    private class CustomRenderer<T extends ClusterItem> extends DefaultClusterRenderer<T> {
        private CustomRenderer(Context context, GoogleMap map, ClusterManager<T> clusterManager) {
            super(context, map, clusterManager);
        }

        @Override
        protected boolean shouldRenderAsCluster(Cluster<T> cluster) {
            //start clustering if at least 2 items overlap
            return cluster.getSize() > 1;
        }
    }

}
