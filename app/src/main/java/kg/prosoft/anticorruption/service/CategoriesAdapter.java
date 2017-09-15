package kg.prosoft.anticorruption.service;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import kg.prosoft.anticorruption.R;

/**
 * Created by ProsoftPC on 7/17/2017.
 */

public class CategoriesAdapter extends BaseAdapter {

    private Context mContext;
    private List<Categories> mCategoriesList;
    private LayoutInflater inflater;
    //private ArrayList<Integer> selectedCtgs;

    public CategoriesAdapter(Context mContext, List<Categories> mCategoriesList) {
        this.mContext = mContext;
        this.mCategoriesList = mCategoriesList;
        //selectedCtgs=new ArrayList<>();
    }

    @Override
    public int getCount() {
        return mCategoriesList.size();
    }

    @Override
    public Object getItem(int position) {
        return mCategoriesList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        Categories Categories = mCategoriesList.get(position);
        String separator=Categories.getSeparator();
        int type=0;
        if(separator!=null && separator.equals("separator")){type=1;}
        //Log.e("TYPEEE", "id "+Categories.getId()+" type "+type);
        return type;
    }

    @Override
    public boolean isEnabled(int position) {
        // A separator cannot be clicked !
        return getItemViewType(position) != 1;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int type = getItemViewType(position);

        if (inflater == null)
            inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null){
            convertView = inflater.inflate(
                    type == 1 ? R.layout.separator_list_item : R.layout.category_item, parent, false);
        }

        if(type==0){
            Categories Categories = mCategoriesList.get(position);
            String title=Categories.getTitle();
            boolean isChild=Categories.getIsChild();
            boolean hasChild=Categories.getHasChild();
            ImageView arrow=(ImageView)convertView.findViewById(R.id.id_iv_arrow);
            if(hasChild){
                arrow.setVisibility(View.VISIBLE);
            }
            else{
                arrow.setVisibility(View.GONE);
            }
            //String image=Categories.getImage();
            //String image_name=image.split("\\.")[0];
            //int category_id=Categories.getId();
           /* ArrayList<Categories>childrenList=Categories.getChildren();
            LinearLayout ll_children=(LinearLayout) convertView.findViewById(R.id.id_ll_children);

            if(childrenList!=null && !childrenList.isEmpty()){
                for (Categories childCatObject: childrenList) {
                    TextView valueTV = new TextView(mContext);
                    valueTV.setText(childCatObject.getTitle());
                    ll_children.addView(valueTV);
                }

            }*/

            TextView tv_category_title=(TextView) convertView.findViewById(R.id.id_tv_category_title);
            tv_category_title.setText(title);
            if(isChild){
                tv_category_title.setTextColor(ContextCompat.getColor(mContext, R.color.gray5));
                tv_category_title.setPadding(20, 0, 0, 20);
            }else{
                tv_category_title.setTextColor(Color.BLACK);
                tv_category_title.setPadding(0, 15, 0, 15);
            }

            //ImageView imgv_category=(ImageView) convertView.findViewById(R.id.id_imgv_category);
            //URI imgsrc=new URI("http://map.oshcity.kg/media/uploads/");
            //int img_id = convertView.getResources().getIdentifier("kg.prosoft.oshmapreport:drawable/" + image_name, null, null);
            //imgv_category.setImageResource(img_id);

        /*if(position==getCount()-1){
            Log.i("END", "Reached");
        }*/
        }

        return convertView;
    }


    /*ArrayList<Integer> getSelectedCtgs(){
        return selectedCtgs;
    }*/

}
