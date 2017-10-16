package kg.prosoft.anticorruption.service;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import kg.prosoft.anticorruption.R;

/**
 * Created by ProsoftPC on 10/16/2017.
 */

public class DocMenuAdapter extends BaseAdapter {
    private Context mContext;
    private List<DocMenu> mList;
    private LayoutInflater inflater;

    public DocMenuAdapter(Context mContext, List<DocMenu> mList) {
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

                //Intent intent = new Intent(context, WebViewActivity.class);
                Log.e("DMAdapter", "open :"+title+" id: "+id);
                /*intent.putExtra("title",title);
                intent.putExtra("text",text);
                context.startActivity(intent);*/
                //Toast.makeText(context, "You Clicked "+info.getId(), Toast.LENGTH_LONG).show();
            }
        });

        return convertView;
    }
}
