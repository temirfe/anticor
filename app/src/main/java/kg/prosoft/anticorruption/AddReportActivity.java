package kg.prosoft.anticorruption;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import kg.prosoft.anticorruption.service.Endpoints;
import kg.prosoft.anticorruption.service.Item;
import kg.prosoft.anticorruption.service.MyImageHelper;
import kg.prosoft.anticorruption.service.MyVolley;
import kg.prosoft.anticorruption.service.SectorDialog;
import kg.prosoft.anticorruption.service.Vocabulary;

public class AddReportActivity extends BaseActivity implements SectorDialog.SectorDialogListener, FrameMapFragment.ParentFrag {

    private ArrayList<Vocabulary> vocList;
    public ArrayList<String> selectedImages;
    private HashMap<Integer, String> titleMap;
    private HashMap<Integer, Vocabulary> parentMap,childMap;
    private HashMap<Integer, HashMap<Integer, Vocabulary>> parentChildMap;
    TextView tv_sector, tv_city, tv_authority, tv_type, tv_lat, tv_lng;
    LinearLayout ll_sector, ll_user,ll_add_photo,ll_images;
    CheckBox chb_anonym;
    EditText et_name, et_email, et_contact;
    int selected_sector_id=0;
    int selected_city_id=0;
    int selected_authority_id=0;
    int selected_type_id=0;
    int DIALOG_SECTOR=0;
    int DIALOG_CITY=1;
    int DIALOG_AUTHORITY=2;
    int DIALOG_TYPE=3;
    int ACTIVE_DIALOG=0;
    public double lat;
    public double lng;
    public RelativeLayout rl_map;
    boolean initialStart=true;
    int marker_city_id=0;

    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    public static final int MEDIA_TYPE_IMAGE = 1;
    // directory name to store captured images and videos
    private static final String IMAGE_DIRECTORY_NAME = "Camera";
    private Uri fileUri; // file url to store image/video
    String TAG ="AddReportActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_report);

        parentMap=new HashMap<>();
        childMap= new HashMap<>();
        titleMap= new HashMap<>();
        parentChildMap=new HashMap<>();
        requestVocabularies();
        tv_sector=(TextView)findViewById(R.id.id_tv_sector);
        tv_city=(TextView)findViewById(R.id.id_tv_city);
        tv_authority=(TextView)findViewById(R.id.id_tv_authority);
        tv_type=(TextView)findViewById(R.id.id_tv_type);
        ll_sector=(LinearLayout)findViewById(R.id.id_ll_sector);
        ll_user=(LinearLayout)findViewById(R.id.id_ll_user);
        chb_anonym=(CheckBox)findViewById(R.id.id_chb_anonym);
        et_name=(EditText)findViewById(R.id.id_et_name);
        et_email=(EditText)findViewById(R.id.id_et_email);
        et_contact=(EditText)findViewById(R.id.id_et_contact);
        tv_lat=(TextView)findViewById(R.id.id_tv_lat);
        tv_lng=(TextView)findViewById(R.id.id_tv_lng);
        ll_images=(LinearLayout)findViewById(R.id.id_ll_images);
        ll_add_photo=(LinearLayout)findViewById(R.id.id_ll_add_photo);
        ll_add_photo.setOnClickListener(addPhotoClick);
        selectedImages=new ArrayList<>();
    }

    View.OnClickListener addPhotoClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            showImageUploadSelect();
        }
    };

    public void sectorClick(View v){
        ACTIVE_DIALOG=DIALOG_SECTOR;
        prepareVocList("report_category", false);
        Bundle args = new Bundle();
        args.putParcelableArrayList("list",vocList);
        args.putInt("selected",selected_sector_id);
        args.putString("title",getResources().getString(R.string.select_sector));

        SectorDialog sdialog = new SectorDialog();
        sdialog.setArguments(args);
        sdialog.show(getFragmentManager(),"sector");
    }

    public void authorityClick(View v){
        prepareVocList("report_structure", true);
        Bundle args = new Bundle();
        args.putParcelableArrayList("list",vocList);
        args.putInt("selected",selected_authority_id);
        args.putString("title",getResources().getString(R.string.select_authority));
        ACTIVE_DIALOG=DIALOG_AUTHORITY;

        SectorDialog sdialog = new SectorDialog();
        sdialog.setArguments(args);
        sdialog.show(getFragmentManager(),"authorityDialog");
    }

    public void cityClick(View v){
        ACTIVE_DIALOG=DIALOG_CITY;
        prepareVocList("city", true);
        //Log.e("THE LIST",vocList+"");
        Bundle args = new Bundle();
        args.putParcelableArrayList("list",vocList);
        args.putInt("selected",selected_city_id);
        args.putString("title",getResources().getString(R.string.select_city));

        SectorDialog sdialog = new SectorDialog();
        sdialog.setArguments(args);
        sdialog.show(getFragmentManager(),"cityDialog");
    }

    public void typeClick(View v){
        ACTIVE_DIALOG=DIALOG_TYPE;
        prepareVocList("report_type", false);
        Bundle args = new Bundle();
        args.putParcelableArrayList("list",vocList);
        args.putInt("selected",selected_type_id);
        args.putString("title",getResources().getString(R.string.select_type));

        SectorDialog sdialog = new SectorDialog();
        sdialog.setArguments(args);
        sdialog.show(getFragmentManager(),"typeDialog");
    }

    public void anonymCheck(View v){
        if(chb_anonym.isChecked()){
            ll_user.setVisibility(View.GONE);
        }
        else{
            ll_user.setVisibility(View.VISIBLE);
        }
    }

    // The dialog fragment receives a reference to this Activity through the
    // Fragment.onAttach() callback, which it uses to call the following methods
    // defined by the SectorDialog.SectorDialogListener interface
    @Override
    public void onDialogSelectClick(int id) {
        Log.e("CLICK RECEIVE",id+"");
        if(ACTIVE_DIALOG==DIALOG_SECTOR){
            String sector=titleMap.get(id);
            selected_sector_id=id;
            tv_sector.setText(sector);
            Log.e("CLICK RECEIVE TO","SECTOR");
        }
        else if(ACTIVE_DIALOG==DIALOG_CITY){
            Log.e("CLICK RECEIVE TO","city");
            String title=titleMap.get(id);
            selected_city_id=id;
            tv_city.setText(title);
            showMapFrame();
        }
        else if(ACTIVE_DIALOG==DIALOG_AUTHORITY){
            Log.e("CLICK RECEIVE TO","authority");
            String title=titleMap.get(id);
            selected_authority_id=id;
            tv_authority.setText(title);
        }
        else if(ACTIVE_DIALOG==DIALOG_TYPE){
            Log.e("CLICK RECEIVE TO","type");
            String title=titleMap.get(id);
            selected_type_id=id;
            tv_type.setText(title);
        }
    }

    public void prepareVocList(String type, boolean hasChildren){
        vocList=new ArrayList<>();
        HashMap<Integer,Vocabulary> cMap;
        TreeMap<Integer,Integer> parentTreeMap=new TreeMap<>();
        TreeMap<Integer,Integer> childTreeMap=new TreeMap<>();
        int i=1;
        for (Map.Entry<Integer, Vocabulary> entry : parentMap.entrySet())
        {
            Vocabulary voc=entry.getValue();
            if(type.equals(voc.getKey())){
                int id=entry.getKey();
                int order=voc.getOrder();
                order=(order*1000)+i;
                parentTreeMap.put(order,id);
                cMap=parentChildMap.get(id);
                if(cMap!=null){
                    for (Map.Entry<Integer, Vocabulary> childEntry : cMap.entrySet())
                    {
                        Vocabulary childVoc=childEntry.getValue();
                        if(type.equals(childVoc.getKey())){
                            int childId=childEntry.getKey();
                            int childOrder=childVoc.getOrder();
                            childOrder=(childOrder*1000)+i;
                            //Log.e("ChildId",childId+" order "+childOrder);
                            childTreeMap.put(childOrder,childId);
                            i++;
                        }
                    }
                }
                i++;
            }
        }
        //Log.e("ParentTree",parentTreeMap.toString());
        //Log.e("ChildTree",childTreeMap.toString());
        for (Map.Entry<Integer, Integer> entry : parentTreeMap.entrySet())
        {
            int id=entry.getValue();
            Vocabulary voc=parentMap.get(id);
            if(hasChildren){voc.setHasChildren(true);}
            //Log.e("ParentVoc",voc.getValue());
            vocList.add(voc);
            cMap=parentChildMap.get(id);
            if(cMap!=null){
                for (Map.Entry<Integer, Integer> childEntry : childTreeMap.entrySet())
                {
                    int cid=childEntry.getValue();
                    //Log.e("CID",cid+"");
                    Vocabulary childVoc=cMap.get(cid);
                    if(childVoc!=null){
                        //Log.e("ChildVoc",childVoc.getValue());
                        if(hasChildren){childVoc.setHasChildren(true);}
                        vocList.add(childVoc);
                    }
                }
            }
        }
    }

    public void requestVocabularies(){
        String uri = Endpoints.VOCABULARIES;
        Response.Listener<JSONArray> listener = new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray jsonArray) {

                Log.e(TAG, "response: " + jsonArray);
                try{
                    for(int s=0; s < jsonArray.length(); s++){
                        JSONObject jsonObject = jsonArray.getJSONObject(s);
                        int id=jsonObject.getInt("id");
                        String key=jsonObject.getString("key");
                        String value=jsonObject.getString("value");
                        int parent=jsonObject.getInt("parent");
                        int order=jsonObject.getInt("ordered_id");
                        titleMap.put(id,value);
                        if(parent==0){
                            parentMap.put(id,new Vocabulary(id,key,value,parent,order,false));
                        }
                        else{
                            childMap=parentChildMap.get(parent);
                            if(childMap== null) {
                                childMap=new HashMap<>();
                            }
                            childMap.put(id,new Vocabulary(id,key,value,parent,order,false));
                            parentChildMap.put(parent,childMap);
                        }
                    }
                    //helper.addVocabulary(vocList);
                }catch(JSONException e){e.printStackTrace();}
            }
        };

        Response.ErrorListener errListener=new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse networkResponse = error.networkResponse;
                Log.e(TAG, "Volley error: " + error.getMessage() + ", code: " + networkResponse);
            }
        };

        JsonArrayRequest volReq = new JsonArrayRequest(Request.Method.GET, uri, null, listener,errListener);

        MyVolley.getInstance(appContext).addToRequestQueue(volReq);
    }

    //** image methods ** //
    public void showImageUploadSelect(){

        final Item[] items = {
                new Item(getResources().getString(R.string.camera), android.R.drawable.ic_menu_camera),
                new Item(getResources().getString(R.string.gallery), android.R.drawable.ic_menu_gallery),
        };

        ListAdapter adapter = new ArrayAdapter<Item>(
                this,
                android.R.layout.select_dialog_item,
                android.R.id.text1,
                items){
            public View getView(int position, View convertView, ViewGroup parent) {
                //Use super class to create the View
                View v = super.getView(position, convertView, parent);
                TextView tv = (TextView)v.findViewById(android.R.id.text1);

                //Put the image on the TextView
                tv.setCompoundDrawablesWithIntrinsicBounds(items[position].icon, 0, 0, 0);

                //Add margin between image and text (support various screen densities)
                int dp5 = (int) (5 * getResources().getDisplayMetrics().density + 0.5f);
                tv.setCompoundDrawablePadding(dp5);

                return v;
            }
        };

        new AlertDialog.Builder(this)
                .setTitle(R.string.add_image)
                .setAdapter(adapter, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        if(item==0){
                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);

                            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

                            // start the image capture Intent
                            startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
                        }
                        else if(item==1){
                            Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            startActivityForResult(i, 101);
                        }
                    }
                }).show();
    }

    public void previewImagePath(String path){
        try {
            Bitmap bitmap = MyImageHelper.decodeSampledBitmapFromPath(path, 400, 400);
            previewImage(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void previewImage(Bitmap bitmap){
        if(selectedImages.size()<10){
            Log.e(TAG,selectedImages.size()+"");
            selectedImages.add(getStringImage(bitmap));
            ImageView iv = new ImageView(this);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(80, 80);
            iv.setLayoutParams(layoutParams);
            iv.setImageBitmap(bitmap);
            ll_images.setPadding(0,10,0,10);
            ll_images.addView(iv);
        }
        else{
            Toast.makeText(this, R.string.upload_limit, Toast.LENGTH_LONG).show();
        }
    }

    public String getStringImage(Bitmap bmp){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }

    public Uri getOutputMediaFileUri(int type) {
        isStoragePermissionGranted();
        return Uri.fromFile(getOutputMediaFile(type));
    }
    private static File getOutputMediaFile(int type) {

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        File mediaFile;

        // External sdcard location
        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                IMAGE_DIRECTORY_NAME);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(IMAGE_DIRECTORY_NAME, "Oops! Failed create "
                        + IMAGE_DIRECTORY_NAME + " directory");
                //------------ for internal storage, not done yet
                //http://stackoverflow.com/questions/31678146/saving-image-taken-from-camera-into-internal-storage
                    /*mediaFile = new File(
                            Environment
                                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                            "IMG_" + timeStamp + ".jpg");
                    return mediaFile;*/
                //------------
                return null;
            }
        }

        // Create a media file name
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "IMG_" + timeStamp + ".jpg");
        }else {
            return null;
        }

        return mediaFile;
    }

    public void isStoragePermissionGranted() {
        //http://stackoverflow.com/questions/3853472/creating-a-directory-in-sdcard-fails/38694026
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(appContext,android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }
    }

    //**map methods **//
    public void showMapFrame(){
        FrameMapFragment fmfragment=new FrameMapFragment();
        Bundle bundle = new Bundle();
        bundle.putDouble("lat", lat);
        bundle.putDouble("lng", lng);
        if(selected_city_id!=0){
            bundle.putInt("city_id",selected_city_id);
        }
        fmfragment.setArguments(bundle);
        putFragment(fmfragment);

        rl_map=(RelativeLayout)findViewById(R.id.id_rl_add_map);
        Button button = new Button(this);
        button.getBackground().setAlpha(0);
        button.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        rl_map.addView(button);

        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), SetLocationActivity.class);
                intent.putExtra("lat",lat);
                intent.putExtra("lng",lng);
                intent.putExtra("city_id",selected_city_id);
                intent.putExtra("previous_city_id",marker_city_id);
                startActivityForResult(intent,240);
            }
        });
    }

    protected void putFragment(FrameMapFragment frag){
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.id_fl_add_map, frag, "FrameMap");
        //ft.addToBackStack(null);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.commit();
    }

    @Override
    public void setParent()
    {
        FragmentManager fragmentManager = getFragmentManager();
        FrameMapFragment nestFrag = (FrameMapFragment)fragmentManager.findFragmentByTag("FrameMap");
        //Tag of your fragment which you should use when you add

        if(nestFrag != null)
        {
            // your some other frag need to provide some data back based on views.
            lat = nestFrag.mylat;
            lng = nestFrag.mylng;
            if(lat!=0.0){
                //Log.e("mylat good",""+lat);
                tv_lat.setText(Double.toString(lat));
                tv_lng.setText(Double.toString(lng));
            }
            else{
                Log.e("mylat bad",""+lat);
            }
            // it can be a string, or int, or some custom java object.
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==240 && resultCode==RESULT_OK){ //get map location
            lat=data.getDoubleExtra("new_lat",0);
            lng=data.getDoubleExtra("new_lng",0);
            marker_city_id=data.getIntExtra("marked_city",0);
            String new_lat_str=Double.toString(lat);
            String new_lng_str=Double.toString(lng);
            tv_lat.setText(new_lat_str);
            tv_lng.setText(new_lng_str);
            initialStart=false;
            //Log.e("RESULT", "lat:"+new_lat_str+" lng:"+new_lng_str);
        }
        else if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE && resultCode == RESULT_OK) {
            String path=fileUri.getPath();
            //Log.e("PATH OF FILE",path);
            previewImagePath(path);
        }
        else if(requestCode==101 && resultCode==RESULT_OK){ //get image from gallery
            //get file path
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            String filePath;
            if(data.getClipData()!=null){
                //Log.e(TAG,"clipData: "+data.getClipData().toString());
                ClipData mClipData = data.getClipData();
                for (int i = 0; i < mClipData.getItemCount(); i++) {
                    ClipData.Item item = mClipData.getItemAt(i);
                    Uri uri = item.getUri();
                    // Get the cursor
                    Cursor cursor = getContentResolver().query(uri, filePathColumn, null, null, null);
                    // Move to first row
                    assert cursor != null;
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    filePath = cursor.getString(columnIndex);
                    cursor.close();
                    //Log.e(TAG,"filePath: "+filePath);
                    previewImagePath(filePath);
                }
                //Uri selectedImageURI = data.getData();
                //input = getContentResolver().openInputStream(selectedImageURI);
            }
            else if(data.getData()!=null){
                //Log.e(TAG,"clipData is null ");
                //Log.e(TAG,"getData: "+data.getData().toString());
                Uri selectedImage = data.getData();
                Cursor cursor = appContext.getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                assert cursor != null;
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                filePath = cursor.getString(columnIndex);
                cursor.close();
                //Log.e(TAG,"filePath: "+filePath);
                if(filePath==null){
                    try {
                        InputStream input = appContext.getContentResolver().openInputStream(selectedImage);
                        Bitmap mSelectedPhotoBmp = BitmapFactory.decodeStream(input);
                        //Log.e(TAG,"Bitmap: "+mSelectedPhotoBmp);
                        previewImage(mSelectedPhotoBmp);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                else{
                    previewImagePath(filePath);
                }
            }
            else{
                //Log.e(TAG,"getData is null");
            }
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        if(initialStart){
            new android.os.Handler().postDelayed(
                    new Runnable() {
                        public void run() {
                            showMapFrame();
                        }
                    },
                    3000);
        }
        else{
            showMapFrame();
        }

    }
}
