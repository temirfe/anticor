package kg.prosoft.anticorruption;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.text.Html;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import kg.prosoft.anticorruption.service.Authority;
import kg.prosoft.anticorruption.service.Endpoints;
import kg.prosoft.anticorruption.service.Item;
import kg.prosoft.anticorruption.service.MyDbHandler;
import kg.prosoft.anticorruption.service.MyImageHelper;
import kg.prosoft.anticorruption.service.MyVolley;
import kg.prosoft.anticorruption.service.Page;
import kg.prosoft.anticorruption.service.SectorDialog;
import kg.prosoft.anticorruption.service.Vocabulary;

public class AddReportActivity extends BaseActivity implements SectorDialog.SectorDialogListener, FrameMapFragment.ParentFrag {

    private ArrayList<Vocabulary> vocList;
    private ArrayList<Authority> authList;
    public ArrayList<String> selectedImages;
    private HashMap<Integer, String> titleMap, titleMapAuth;
    private HashMap<Integer, Vocabulary> parentMap,childMap;
    private LinkedHashMap<Integer, Authority> parentMapAuth,childMapAuth;
    private HashMap<Integer, HashMap<Integer, Vocabulary>> parentChildMap;
    private HashMap<Integer, HashMap<Integer, Authority>> parentChildMapAuth;
    TextView tv_sector, tv_city, tv_authority, tv_type, tv_lat, tv_lng;
    LinearLayout ll_add_photo,ll_images;
    RelativeLayout ll_user;
    CheckBox chb_anonym;
    EditText et_title, et_text, et_name, et_email, et_contact;
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
    boolean initialStart=true, anonym=false;
    int marker_city_id=0;

    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    public static final int MEDIA_TYPE_IMAGE = 1;
    // directory name to store captured images and videos
    private static final String IMAGE_DIRECTORY_NAME = "Camera";
    private Uri fileUri; // file url to store image/video
    String TAG ="AddReportActivity";
    Bundle savedIS;
    Intent gotIntent;
    String anon_help="",name_help="",email_help="",contact_help="";
    private TextView tv_date;
    private TextView tv_time;
    private int year, month, day, hour, minute;
    private DatePickerDialog dateDialog;
    private TimePickerDialog timeDialog;
    private SimpleDateFormat dateFormatter, dateFormatterForm;
    private SimpleDateFormat timeFormatter;
    public Calendar calendar;
    String date_form;
    int user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_report);
        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(R.string.corruption_report);
        }
        savedIS=savedInstanceState;

        gotIntent=getIntent();

        activity=this;
        user_id=session.getUserId();
        selected_city_id=session.getCityId();

        parentMap=new HashMap<>();
        childMap= new HashMap<>();
        titleMap= new HashMap<>();
        parentChildMap=new HashMap<>();

        parentMapAuth=new LinkedHashMap<>();
        childMapAuth= new LinkedHashMap<>();
        titleMapAuth= new HashMap<>();
        parentChildMapAuth=new HashMap<>();

        new AuthorityTask().execute();
        new VocabularyTask().execute();
        checkAuthDepend();
        checkVocDepend();

        tv_sector=(TextView)findViewById(R.id.id_tv_sector);
        tv_city=(TextView)findViewById(R.id.id_tv_city);
        tv_authority=(TextView)findViewById(R.id.id_tv_authority);
        tv_type=(TextView)findViewById(R.id.id_tv_type);
        ll_user=(RelativeLayout)findViewById(R.id.id_ll_user);
        chb_anonym=(CheckBox)findViewById(R.id.id_chb_anonym);
        et_title=(EditText)findViewById(R.id.id_et_title);
        et_text=(EditText)findViewById(R.id.id_et_note);
        et_name=(EditText)findViewById(R.id.id_et_name);
        et_email=(EditText)findViewById(R.id.id_et_email);
        et_contact=(EditText)findViewById(R.id.id_et_contact);
        tv_lat=(TextView)findViewById(R.id.id_tv_lat);
        tv_lng=(TextView)findViewById(R.id.id_tv_lng);
        ll_images=(LinearLayout)findViewById(R.id.id_ll_images);
        ll_add_photo=(LinearLayout)findViewById(R.id.id_ll_add_photo);
        ll_add_photo.setOnClickListener(addPhotoClick);
        selectedImages=new ArrayList<>();
        tv_date=(TextView)findViewById(R.id.id_tv_date);
        tv_date.setOnClickListener(dateClick);
        tv_time=(TextView) findViewById(R.id.id_tv_time);
        tv_time.setOnClickListener(timeClick);

        dateFormatter = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
        dateFormatterForm = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        timeFormatter = new SimpleDateFormat("H:mm", Locale.US);

        calendar = Calendar.getInstance();
        tv_date.setText(dateFormatter.format(calendar.getTime()));
        date_form=dateFormatter.format(calendar.getTime());
        tv_time.setText(timeFormatter.format(calendar.getTime()));
        setDateTimeField();
        new PageTask().execute();
    }

    private void setDateTimeField() {
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);

        hour = calendar.get(Calendar.HOUR_OF_DAY);
        minute = calendar.get(Calendar.MINUTE);

        dateDialog = new DatePickerDialog(activity, new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker view, int yearSelected, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(yearSelected, monthOfYear, dayOfMonth);
                tv_date.setText(dateFormatter.format(newDate.getTime()));
                date_form=dateFormatterForm.format(newDate.getTime());
            }
        },year, month, day);

        timeDialog = new TimePickerDialog(activity, new TimePickerDialog.OnTimeSetListener() {
            public void onTimeSet(TimePicker view, int hourOfDay, int minuteS) {
                Calendar newTime = Calendar.getInstance();
                newTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                newTime.set(Calendar.MINUTE, minuteS);
                tv_time.setText(timeFormatter.format(newTime.getTime()));
            }
        },hour, minute, true);
    }

    View.OnClickListener dateClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            dateDialog.show();
        }
    };

    View.OnClickListener timeClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            timeDialog.show();
        }
    };

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
        Bundle args = new Bundle();
        args.putParcelableArrayList("list",authList);
        args.putInt("selected",selected_authority_id);
        args.putString("title",getResources().getString(R.string.select_authority));
        args.putString("type","authority");
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

    public void submitReport(View v){
        submitForm();
    }

    public void anonymCheck(View v){
        if(chb_anonym.isChecked()){
            ll_user.setVisibility(View.GONE);
            anonym=true;
        }
        else{
            ll_user.setVisibility(View.VISIBLE);
            anonym=false;
        }
    }

    // The dialog fragment receives a reference to this Activity through the
    // Fragment.onAttach() callback, which it uses to call the following methods
    // defined by the SectorDialog.SectorDialogListener interface
    @Override
    public void onDialogSelectClick(int id) {
        if(ACTIVE_DIALOG==DIALOG_SECTOR){
            String sector=titleMap.get(id);
            selected_sector_id=id;
            tv_sector.setText(sector);
        }
        else if(ACTIVE_DIALOG==DIALOG_CITY){
            String title=titleMap.get(id);
            selected_city_id=id;
            tv_city.setText(title);
            showMapFrame();
        }
        else if(ACTIVE_DIALOG==DIALOG_AUTHORITY){
            String title=titleMapAuth.get(id);
            selected_authority_id=id;
            tv_authority.setText(title);
        }
        else if(ACTIVE_DIALOG==DIALOG_TYPE){
            String title=titleMap.get(id);
            selected_type_id=id;
            tv_type.setText(title);
        }
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
            ll_images.setPadding(5,5,5,5);
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
            if (ContextCompat.checkSelfPermission(context,android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }
    }

    //**map methods **//
    public void showMapFrame(){
        if(savedIS==null){
            FrameMapFragment fmfragment=new FrameMapFragment();
            Bundle bundle = new Bundle();
            bundle.putDouble("lat", lat);
            bundle.putDouble("lng", lng);
            if(selected_city_id!=0){
                bundle.putInt("city_id",selected_city_id);
            }
            fmfragment.setArguments(bundle);
            putFragment(fmfragment);
        }

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
    //** **//

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
                Cursor cursor = context.getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                assert cursor != null;
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                filePath = cursor.getString(columnIndex);
                cursor.close();
                //Log.e(TAG,"filePath: "+filePath);
                if(filePath==null){
                    try {
                        InputStream input = context.getContentResolver().openInputStream(selectedImage);
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
                            if (activity != null && !activity.isFinishing()) {
                                showMapFrame();
                            }
                        }
                    },
                    3000);
        }
        else{
            showMapFrame();
        }

    }

    public void submitForm(){
        View focusView = null;
        boolean allGood=true;
        final String title = et_title.getText().toString();
        final String description = et_text.getText().toString();
        final String name = et_name.getText().toString();
        final String email = et_email.getText().toString();
        final String contact = et_contact.getText().toString();
        final String latitude = tv_lat.getText().toString();
        final String longitude = tv_lng.getText().toString();
        final String date=date_form+" "+tv_time.getText().toString();

        if(title.trim().equals("")){
            et_title.setError(getResources().getString(R.string.required));
            focusView = et_title;
            allGood=false;
        }
        if(description.trim().equals("")){
            et_text.setError(getResources().getString(R.string.required));
            focusView = et_text;
            allGood=false;
        }
        if(!anonym){
            if(contact.trim().equals("")){
                et_contact.setError(getResources().getString(R.string.required));
                focusView = et_contact;
                allGood=false;
            }
            if(name.trim().equals("")){
                et_name.setError(getResources().getString(R.string.required));
                focusView = et_name;
                allGood=false;
            }
            if(email.trim().equals("")){
                et_email.setError(getResources().getString(R.string.required));
                focusView = et_email;
                allGood=false;
            }
        }
        if(selected_authority_id==0){
            Toast.makeText(this, getResources().getString(R.string.select_authority), Toast.LENGTH_SHORT).show();
            allGood=false;
        }
        if(selected_sector_id==0){
            Toast.makeText(this, getResources().getString(R.string.select_sector), Toast.LENGTH_SHORT).show();
            allGood=false;
        }
        if(selected_city_id==0){
            Toast.makeText(this, getResources().getString(R.string.select_city), Toast.LENGTH_SHORT).show();
            allGood=false;
        }
        /*if(latitude.trim().equals("0.0")){
            Toast.makeText(this, getResources().getString(R.string.set_location), Toast.LENGTH_SHORT).show();
            allGood=false;
        }*/

        if(allGood){
            //store personal details in session
            session.createContactSession(name,email,contact);
            session.setCityId(selected_city_id);

            Log.e(TAG,"title: "+title+" text: "+description);
            if(anonym){
                Log.e(TAG,"anonumous");
            }
            else{
                Log.e(TAG,"author: "+name+" email: "+email+" cont: "+contact);
            }
            Log.e(TAG,"lat: "+latitude+" lon: "+longitude);
            Log.e(TAG,"authority_id: "+selected_authority_id+" city_id: "+selected_city_id);
            Log.e(TAG,"type_id: "+selected_type_id+" category_id: "+selected_sector_id);
            Log.e(TAG,"date: "+date);
            for (String img : selectedImages)
            {
                Log.e(TAG,"image: "+img);
            }

            final ProgressDialog progress = new ProgressDialog(this);
            progress.setTitle(getResources().getString(R.string.sending));
            progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
            progress.show();
            String url=Endpoints.REPORTS;
            Response.Listener<String> listener = new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    progress.dismiss();
                    try {
                        JSONObject obj = new JSONObject(response);
                        //Log.d("My App", obj.toString());
                        try{
                            int id = obj.getInt("id");
                            if(id!=0){
                                Intent intent = new Intent(AddReportActivity.this, ReportViewActivity.class);
                                intent.putExtra("id",id);
                                intent.putExtra("from","form");
                                intent.putExtra("title",title);
                                intent.putExtra("text",description);
                                intent.putExtra("date",date);
                                intent.putExtra("lat",Double.parseDouble(latitude));
                                intent.putExtra("lng",Double.parseDouble(longitude));
                                intent.putExtra("city",titleMap.get(selected_city_id));
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }

                        }catch(JSONException e){e.printStackTrace();}

                    } catch (Throwable t) {
                        Log.e("My App", "Could not parse malformed JSON: \"" + response + "\"");
                    }
                }
            };

            Response.ErrorListener errorResp =new Response.ErrorListener()
            {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // As of f605da3 the following should work
                    NetworkResponse response = error.networkResponse;
                    if (error instanceof ServerError && response != null) {
                        try {
                            String res = new String(response.data,
                                    HttpHeaderParser.parseCharset(response.headers, "utf-8"));
                            Object json = new JSONTokener(res).nextValue();
                            if (json instanceof JSONObject){
                                JSONObject err = new JSONObject(res);
                                //Log.i("RESPONSE err 1", err.toString());
                            }
                            else if (json instanceof JSONArray){
                                JSONArray err = new JSONArray(res);
                                //Log.i("RESPONSE err 1", err.toString());
                            }
                            progress.dismiss();
                            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                            builder.setMessage(R.string.app_error).setNegativeButton(R.string.close,null).create().show();
                        } catch (UnsupportedEncodingException e1) {
                            // Couldn't properly decode data to string
                            e1.printStackTrace();
                            //Log.i("RESPONSE err 2", "here");
                        } catch (JSONException e2) {
                            // returned data is not JSONObject?
                            e2.printStackTrace();
                            //Log.i("RESPONSE err 3", "here");
                        }
                    }
                }
            };

            StringRequest req = new StringRequest(Request.Method.POST, url, listener, errorResp){
                @Override
                protected Map<String,String> getParams(){
                    Map<String,String> params = new HashMap<String, String>();
                    params.put("title",title);
                    params.put("text",description);
                    if(anonym){
                        params.put("anonymous","1");
                    }
                    else{
                        params.put("author",name);
                        params.put("email",email);
                        params.put("contact",contact);
                    }
                    params.put("lat",latitude);
                    params.put("lon",longitude);
                    params.put("authority_id",Integer.toString(selected_authority_id));
                    params.put("city_id",Integer.toString(selected_city_id));
                    params.put("category_id",Integer.toString(selected_sector_id));
                    params.put("type_id",Integer.toString(selected_type_id));
                    params.put("date",date);
                    params.put("user_id",Integer.toString(user_id));
                    int im=1;
                    for (String img : selectedImages)
                    {
                        params.put("images["+im+"]",img);
                        im++;
                    }
                    //params.put("incident_mode","5"); //5 is android

                    /*SharedPreferences pref = context.getSharedPreferences(FirebaseConfig.SHARED_PREF, 0);
                    String phoneFirebaseId = pref.getString("regId", null);
                    if(phoneFirebaseId!=null){
                        params.put("regid",phoneFirebaseId);
                    }*/

                    //Log.e("FIRE ID", "Firebase reg id: " + phoneFirebaseId);

                    return params;
                }
            };
            req.setRetryPolicy(new DefaultRetryPolicy(
                    0,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            MyVolley.getInstance(context).addToRequestQueue(req);
        }
        else{
            if(focusView!=null){focusView.requestFocus();}}
    }

    /** Vocabulary **/
    public void checkVocDepend(){
        String uri = Endpoints.VOC_DEPEND;
        StringRequest volReq = new StringRequest(Request.Method.GET, uri,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        String depend=session.getVocabularyDepend();
                        response=response.replace("\"","");
                        Log.e(TAG, "voc depend: "+depend+" response: "+response);
                        if(!response.equals(depend)){
                            //new maxId is different, that mean category table has been altered. send new request.
                            requestVocabularies();
                            session.setVocabularyDepend(response);
                        }
                        else{ Log.e(TAG, "voc depend matches");}
                        session.setVocabularyDependChecked(true);
                    }
                }, null);

        MyVolley.getInstance(context).addToRequestQueue(volReq);
    }
    public void requestVocabularies(){
        String uri = Endpoints.VOCABULARIES;
        Response.Listener<JSONArray> listener = new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray jsonArray) {

                Log.e(TAG, "reqVoc response: " + jsonArray);
                try{
                    helper.doClearVocTask();
                    for(int s=0; s < jsonArray.length(); s++){
                        JSONObject jsonObject = jsonArray.getJSONObject(s);
                        int id=jsonObject.getInt("id");
                        String key=jsonObject.getString("key");
                        String value=jsonObject.getString("value");
                        int parent=jsonObject.getInt("parent");
                        int order=jsonObject.getInt("ordered_id");
                        Vocabulary voc =new Vocabulary(id,key,value,parent,order,false);
                        helper.addVocabulary(voc);
                        titleMap.put(id,value);
                        if(parent==0){
                            parentMap.put(id,voc);
                        }
                        else{
                            childMap=parentChildMap.get(parent);
                            if(childMap== null) {
                                childMap=new HashMap<>();
                            }
                            childMap.put(id,voc);
                            parentChildMap.put(parent,childMap);
                        }
                        populateHelp(key, value);
                    }

                    int type_id_got=gotIntent.getIntExtra("type_id",0);
                    if(type_id_got!=0){
                        String title=titleMap.get(type_id_got);
                        selected_type_id=type_id_got;
                        tv_type.setText(title);
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

        MyVolley.getInstance(context).addToRequestQueue(volReq);
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
    private class VocabularyTask extends AsyncTask<Void, Void, List<Vocabulary>> {
        protected List<Vocabulary> doInBackground(Void... params) {
            if(dbHandler==null){dbHandler = new MyDbHandler(context); Log.e(TAG, "VocabularyTask dbhandler was null");}
            if(db==null || !db.isOpen()){db = dbHandler.getWritableDatabase(); Log.e(TAG, "VocabularyTask db was null or not open");}

            return dbHandler.getVocContents(db);
        }
        protected void onPostExecute(List<Vocabulary> theList) {
            if(theList.size()>0){
                for (Vocabulary voc : theList) {
                    int id=voc.getId();
                    String value=voc.getValue();
                    String key=voc.getKey();
                    int parent=voc.getParent();
                    titleMap.put(id,value);
                    if(parent==0){
                        parentMap.put(id,voc);
                    }
                    else{
                        childMap=parentChildMap.get(parent);
                        if(childMap== null) {
                            childMap=new HashMap<>();
                        }
                        childMap.put(id,voc);
                        parentChildMap.put(parent,childMap);
                    }
                    populateHelp(key, value);
                }

                int type_id_got=gotIntent.getIntExtra("type_id",0);
                if(type_id_got!=0){
                    String title=titleMap.get(type_id_got);
                    selected_type_id=type_id_got;
                    tv_type.setText(title);
                }
                Log.e(TAG, "voc data has been taken from DB");
            }
            else{
                Log.e("VocTask", "no content in db, requesting server");
                requestVocabularies(); //requesting server
            }
        }
    }
    public void populateHelp(String key, String value){

        switch(key){
            case "lookup_anonym":anon_help=value;break;
            case "lookup_name":name_help=value;break;
            case "lookup_email":email_help=value;break;
            case "lookup_contact":contact_help=value;break;
        }
    }


    /** Authority **/
    public void checkAuthDepend(){
        String uri = Endpoints.AUTH_DEPEND;
        StringRequest volReq = new StringRequest(Request.Method.GET, uri,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        String depend=session.getAuthorityDepend();
                        response=response.replace("\"","");
                        Log.e(TAG, "auth depend: "+depend+" response: "+response);
                        if(!response.equals(depend)){
                            //new maxId is different, that mean category table has been altered. send new request.
                            requestAuthority();
                            session.setAuthorityDepend(response);
                        }
                        else{ Log.e(TAG, "auth depend matches");}
                        session.setAuthorityDependChecked(true);
                    }
                }, null);

        MyVolley.getInstance(context).addToRequestQueue(volReq);
    }
    public void requestAuthority(){
        String uri = Endpoints.AUTHORITIES;

        Response.Listener<JSONArray> listener = new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try{
                    Log.i("AUTH RESPONSE", "keldi");
                    int leng=response.length();
                    if(leng>0){
                        helper.doClearAuthTask();
                        for(int i=0; i < leng; i++){
                            JSONObject jsonObject = response.getJSONObject(i);
                            int id = jsonObject.getInt("id");
                            String title=jsonObject.getString("title");
                            String text=jsonObject.getString("text");
                            String image=jsonObject.getString("img");
                            int parent_id=jsonObject.getInt("parent_id");
                            int rating=jsonObject.getInt("rating");
                            int comments=jsonObject.getInt("comments");
                            int reports=jsonObject.getInt("reports");

                            Authority authForDialog = new Authority(id, title, image, parent_id);
                            Authority authForDb = new Authority(id, title, text, image, parent_id,rating,comments,reports);
                            helper.insertAuthority(authForDb);
                            //authList.add(authority);
                            titleMapAuth.put(id,title);
                            if(parent_id==0){
                                parentMapAuth.put(id,authForDialog);
                            }
                            else{
                                childMapAuth=(LinkedHashMap<Integer, Authority>)parentChildMapAuth.get(parent_id);
                                if(childMapAuth== null) {
                                    childMapAuth=new LinkedHashMap<>();
                                }
                                childMapAuth.put(id,authForDialog);
                                parentChildMapAuth.put(parent_id,childMapAuth);
                            }
                        }
                    }
                    else{
                        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                        builder.setMessage(R.string.no_result).setNegativeButton(R.string.close,null).create().show();
                    }

                }catch(JSONException e){e.printStackTrace();}
                prepareAuthList();
            }
        };
        Response.ErrorListener errorListener =new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse networkResponse = error.networkResponse;
                Log.e(TAG, "Volley error: " + error.getMessage() + ", code: " + networkResponse);
            }
        };

        JsonArrayRequest volReq = new JsonArrayRequest(Request.Method.GET, uri, null, listener,errorListener);
        MyVolley.getInstance(context).addToRequestQueue(volReq);
    }
    public void prepareAuthList(){
        authList=new ArrayList<>();
        HashMap<Integer,Authority> cMap;
        TreeMap<Integer,Integer> parentTreeMap=new TreeMap<>();
        TreeMap<Integer,Integer> childTreeMap=new TreeMap<>();
        int i=1;
        for (Map.Entry<Integer, Authority> entry : parentMapAuth.entrySet())
        {
            int id=entry.getKey();
            int order=0; //you can put order number here
            order=(order*1000)+i;
            parentTreeMap.put(order,id);
            cMap=parentChildMapAuth.get(id);
            if(cMap!=null){
                for (Map.Entry<Integer, Authority> childEntry : cMap.entrySet())
                {
                    int childId=childEntry.getKey();
                    int childOrder=0;
                    childOrder=(childOrder*1000)+i;
                    //Log.e("ChildId",childId+" order "+childOrder);
                    childTreeMap.put(childOrder,childId);
                    i++;
                }
            }
            i++;
        }
        //Log.e("ParentTree",parentTreeMap.toString());
        //Log.e("ChildTree",childTreeMap.toString());
        for (Map.Entry<Integer, Integer> entry : parentTreeMap.entrySet())
        {
            int id=entry.getValue();
            Authority voc=parentMapAuth.get(id);
            //Log.e("ParentVoc",voc.getValue());
            authList.add(voc);
            cMap=parentChildMapAuth.get(id);
            if(cMap!=null){
                for (Map.Entry<Integer, Integer> childEntry : childTreeMap.entrySet())
                {
                    int cid=childEntry.getValue();
                    //Log.e("CID",cid+"");
                    Authority childVoc=cMap.get(cid);
                    if(childVoc!=null){
                        //Log.e("ChildVoc",childVoc.getValue());
                        authList.add(childVoc);
                    }
                }
            }
        }
    }
    private class AuthorityTask extends AsyncTask<Void, Void, List<Authority>> {
        protected List<Authority> doInBackground(Void... params) {
            if(dbHandler==null){dbHandler = new MyDbHandler(context); Log.e(TAG, "AuthorityTask dbhandler was null");}
            if(db==null || !db.isOpen()){db = dbHandler.getWritableDatabase(); Log.e(TAG, "AuthorityTask db was null or not open");}

            return dbHandler.getAuthContents(db);
        }
        protected void onPostExecute(List<Authority> theList) {
            if(theList.size()>0){
                for (Authority authority : theList) {
                    int id=authority.getId();
                    int parent_id=authority.getParentId();
                    String title=authority.getTitle();
                    titleMapAuth.put(id,title);
                    if(parent_id==0){
                        parentMapAuth.put(id,authority);
                    }
                    else{
                        childMapAuth=(LinkedHashMap<Integer, Authority>)parentChildMapAuth.get(parent_id);
                        if(childMapAuth== null) {
                            childMapAuth=new LinkedHashMap<>();
                        }
                        childMapAuth.put(id,authority);
                        parentChildMapAuth.put(parent_id,childMapAuth);
                    }
                }
                prepareAuthList();
                Log.e(TAG, "auth data has been taken from DB");
            }
            else{
                Log.e("AuthorityTask", "no content in db, requesting server");
                requestAuthority(); //requesting server
            }
        }
    }

    /** Page **/
    public static CharSequence trimTrailingWhitespace(CharSequence source) {

        if(source == null)
            return "";

        int i = source.length();

        // loop back to the first non-whitespace character
        while(--i >= 0 && Character.isWhitespace(source.charAt(i))) {
        }

        return source.subSequence(0, i+1);
    }
    private class PageTask extends AsyncTask<Void, Void, ArrayList<Page>> {
        protected ArrayList<Page> doInBackground(Void... params) {
            if(dbHandler==null){dbHandler = new MyDbHandler(context);}
            if(db==null || !db.isOpen()){db = dbHandler.getWritableDatabase();}

            return dbHandler.getPageContents(db);
        }
        protected void onPostExecute(ArrayList<Page> theList) {
            if(theList.size()>0){
                for(int i=0; i<theList.size(); i++){
                    Page page=theList.get(i);
                    String desc=page.getDescription();
                    if(desc.equals("warning")){
                        String title=page.getTitle();
                        String text = page.getText();

                        CharSequence html_text;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                            html_text= Html.fromHtml(text,Html.FROM_HTML_MODE_LEGACY);
                        } else {
                            html_text=Html.fromHtml(text);
                        }
                        String html_text_string=trimTrailingWhitespace(html_text).toString();
                        showWarning(title, html_text_string);
                    }
                }
                Log.e(TAG, "page data has been taken from DB");
                checkPageDepend();
            }
            else{
                Log.e(TAG, "no content in db, requesting server");
                requestPages(); //requesting server
            }
        }
    }
    public void requestPages(){
        String uri = Endpoints.PAGES;
        String lang=session.getLanguage();
        if(lang.isEmpty()){lang="ky";}
        uri=uri+"?lang="+lang;
        Response.Listener<JSONArray> listener = new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray jsonArray) {
                Log.e(TAG, "reqPage response: " + jsonArray);
                ArrayList<Page> infoList=new ArrayList<>();
                try{
                    for(int s=0; s < jsonArray.length(); s++){
                        JSONObject jsonObject = jsonArray.getJSONObject(s);
                        int id=jsonObject.getInt("id");
                        String title=jsonObject.getString("title");
                        String text=jsonObject.getString("text");
                        String desc=jsonObject.getString("description");
                        Page page =new Page(id,title, text, desc);
                        infoList.add(page);
                        if(desc.equals("about")){
                            CharSequence html_text;
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                                html_text= Html.fromHtml(text,Html.FROM_HTML_MODE_LEGACY);
                            } else {
                                html_text=Html.fromHtml(text);
                            }
                            String html_text_string=trimTrailingWhitespace(html_text).toString();
                            showWarning(title, html_text_string);
                        }

                    }
                    helper.addPageList(infoList);
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

        MyVolley.getInstance(context).addToRequestQueue(volReq);
    }
    public void checkPageDepend(){
        String uri = Endpoints.PAGE_DEPEND;
        StringRequest volReq = new StringRequest(Request.Method.GET, uri,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        String depend=session.getPageDepend();
                        response=response.replace("\"","");
                        Log.e(TAG, "depend: "+depend+" response: "+response);
                        if(!response.equals(depend)){
                            //new maxId is different, that mean category table has been altered. send new request.
                            requestPages(); //requesting server
                            session.setPageDepend(response);
                        }
                        else{ Log.e(TAG, "depend matches");}
                    }
                }, null);

        MyVolley.getInstance(context).addToRequestQueue(volReq);
    }

    public void showWarning(String title, String text){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(title).setMessage(text).setNegativeButton(android.R.string.ok,null).create().show();
    }

    public void onClickEmailWarn(View v){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(email_help).setNegativeButton(R.string.close,null).create().show();
    }
    public void onClickNameHelp(View v){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(name_help).setNegativeButton(R.string.close,null).create().show();
    }
    public void onClickAnonWarn(View v){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(anon_help).setNegativeButton(R.string.close,null).create().show();
    }
    public void onClickContactHelp(View v){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(contact_help).setNegativeButton(R.string.close,null).create().show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.findItem(R.id.action_search).setVisible(false);
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //session.clear();
        if(db!=null && db.isOpen()){db.close();}
        RequestQueue queue = MyVolley.getInstance(context).getRequestQueue();
        queue.cancelAll(context);
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
}
