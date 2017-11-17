package kg.prosoft.anticorruption.service;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import kg.prosoft.anticorruption.PolitListActivity;
import kg.prosoft.anticorruption.R;

/**
 * Created by ProsoftPC on 11/17/2017.
 */

public class PoliticsAdapter extends BaseAdapter {
    private Context mContext;
    private List<DocMenu> mList;
    private LayoutInflater inflater;

    public PoliticsAdapter(Context mContext, List<DocMenu> mList) {
        this.mContext = mContext;
        this.mList = mList;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (inflater == null)
            inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null){
            convertView = inflater.inflate(R.layout.item_doc_menu, parent, false);
        }

        DocMenu docmenu = mList.get(position);
        final String title=docmenu.getTitle();
        final int id=docmenu.getId();

        TextView tv_category_title=(TextView) convertView.findViewById(R.id.id_tv_title);
        tv_category_title.setText(title);
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent intent = new Intent(mContext, PolitListActivity.class);
                intent.putExtra("title",title);
                intent.putExtra("id",id);
                mContext.startActivity(intent);
                Log.e("PolitAdapter", "open :"+title+" id: "+id);
                //Toast.makeText(context, "You Clicked "+info.getId(), Toast.LENGTH_LONG).show();
            }
        });


        return convertView;
    }
}
