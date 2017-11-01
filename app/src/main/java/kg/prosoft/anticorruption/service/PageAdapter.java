package kg.prosoft.anticorruption.service;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import kg.prosoft.anticorruption.AddReportActivity;
import kg.prosoft.anticorruption.AuthorityListActivity;
import kg.prosoft.anticorruption.DocListActivity;
import kg.prosoft.anticorruption.MainActivity;
import kg.prosoft.anticorruption.PageViewActivity;
import kg.prosoft.anticorruption.R;

/**
 * Created by ProsoftPC on 11/1/2017.
 */

public class PageAdapter extends BaseAdapter {
    private Activity activity;
    private Context mContext;
    private List<Page> mList;
    private LayoutInflater inflater;
    private int type=0; //0 - doc menu, 1-map menu

    public PageAdapter(Context mContext, List<Page> mList) {
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
            convertView = inflater.inflate(R.layout.item_page, parent, false);
        }

        Page Page = mList.get(position);
        final String title=Page.getTitle();
        final String text=Page.getText();
        final int id=Page.getId();

        TextView tv_title=(TextView) convertView.findViewById(R.id.id_tv_title);
        tv_title.setText(title);
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent intent = new Intent(mContext, PageViewActivity.class);
                intent.putExtra("title",title);
                intent.putExtra("text",text);
                intent.putExtra("id",id);
                mContext.startActivity(intent);
                //Toast.makeText(context, "You Clicked "+info.getId(), Toast.LENGTH_LONG).show();
            }
        });


        return convertView;
    }
}
