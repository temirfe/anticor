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
 * Created by ProsoftPC on 9/14/2017.
 */

public class VocAdapter extends BaseAdapter {

    private Context mContext;
    private List<Vocabulary> vocList;
    private LayoutInflater inflater;
    private int vocSelected;

    public VocAdapter() {}
    public VocAdapter(Context mContext, List<Vocabulary> mCategoriesList, int selected) {
        this.mContext = mContext;
        this.vocList = mCategoriesList;
        vocSelected=selected;
    }

    @Override
    public int getCount() {
        return vocList.size();
    }

    @Override
    public Object getItem(int position) {
        return vocList.get(position);
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
            convertView = inflater.inflate( R.layout.vocabulary_item, parent, false);
        }

        if(type==0){
            Vocabulary voc = vocList.get(position);
            String title=voc.getValue();
            int id=voc.getId();
            ImageView arrow=(ImageView)convertView.findViewById(R.id.id_iv_arrow);
            if(id==vocSelected){
                arrow.setVisibility(View.VISIBLE);
            }
            else{
                arrow.setVisibility(View.GONE);
            }

            TextView tv_category_title=(TextView) convertView.findViewById(R.id.id_tv_category_title);
            tv_category_title.setText(title);
        }

        return convertView;
    }

}
