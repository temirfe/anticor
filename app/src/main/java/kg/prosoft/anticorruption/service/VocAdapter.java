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
    private ArrayList<Vocabulary> vocList;
    private LayoutInflater inflater;
    private int vocSelected;

    public VocAdapter() {}
    public VocAdapter(Context mContext, ArrayList<Vocabulary> mList, int selected) {
        this.mContext = mContext;
        this.vocList = mList;
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
        Vocabulary voc = vocList.get(position);
        boolean hasChildren = voc.getHasChildren();
        if(hasChildren) return voc.getParent() != 0;
        else{return true;}
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (inflater == null)
            inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null){
            convertView = inflater.inflate( R.layout.vocabulary_item, parent, false);
        }

        Vocabulary voc = vocList.get(position);
        String title=voc.getValue();
        boolean hasChildren = voc.getHasChildren();
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
        if(hasChildren){
            int par = voc.getParent();
            if(par==0){
                convertView.setBackgroundColor(Color.RED);
                tv_category_title.setTextColor(Color.WHITE);
            }
            else{
                convertView.setBackgroundColor(Color.WHITE);
                tv_category_title.setTextColor(Color.BLACK);
            }
        }

        return convertView;
    }

}
