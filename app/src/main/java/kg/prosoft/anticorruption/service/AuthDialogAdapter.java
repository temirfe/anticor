package kg.prosoft.anticorruption.service;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import kg.prosoft.anticorruption.R;

/**
 * Created by ProsoftPC on 10/5/2017.
 */

public class AuthDialogAdapter extends BaseAdapter {
    private Context mContext;
    private List<Authority> authList;
    private LayoutInflater inflater;
    private int selected_id=0;

    public AuthDialogAdapter(Context mContext, List<Authority> authList, int selected) {
        this.mContext = mContext;
        this.authList = authList;
        selected_id=selected;
    }

    @Override
    public int getCount() {
        return authList.size();
    }

    @Override
    public Object getItem(int position) {
        return authList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean isEnabled(int position) {
        Authority voc = authList.get(position);
        if(voc.getParentId()==0) {return false;}
        else{return true;}
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (inflater == null)
            inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null)
            convertView = inflater.inflate(R.layout.vocabulary_item,null);

        Authority authority = authList.get(position);
        String title=authority.getTitle();

        TextView tv_title=(TextView) convertView.findViewById(R.id.id_tv_category_title);
        tv_title.setText(title);
        ImageView arrow=(ImageView)convertView.findViewById(R.id.id_iv_arrow);
        int id=authority.getId();
        if(id==selected_id){
            arrow.setVisibility(View.VISIBLE);
        }
        else{
            arrow.setVisibility(View.GONE);
        }

        convertView.setTag(authority.getId());
        int par = authority.getParentId();
        if(par==0){
            convertView.setBackgroundColor(Color.RED);
            tv_title.setTextColor(Color.WHITE);
        }
        else{
            convertView.setBackgroundColor(Color.WHITE);
            tv_title.setTextColor(Color.BLACK);
        }
        return convertView;
    }
}
