package kg.prosoft.anticorruption.service;

import android.app.Activity;
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

import kg.prosoft.anticorruption.AddReportActivity;
import kg.prosoft.anticorruption.AuthorityListActivity;
import kg.prosoft.anticorruption.DocListActivity;
import kg.prosoft.anticorruption.MainActivity;
import kg.prosoft.anticorruption.R;

/**
 * Created by ProsoftPC on 10/16/2017.
 */

public class DocMenuAdapter extends BaseAdapter {
    private Activity activity;
    private Context mContext;
    private List<DocMenu> mList;
    private LayoutInflater inflater;
    private int type=0; //0 - doc menu, 1-map menu

    public DocMenuAdapter(Context mContext, List<DocMenu> mList) {
        this.mContext = mContext;
        this.mList = mList;
    }
    public DocMenuAdapter(Activity actv, List<DocMenu> mList, int type) {
        this.activity=actv;
        this.mContext = actv.getApplicationContext();
        this.mList = mList;
        this.type=type;
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
                if(type==0){
                    Intent intent = new Intent(mContext, DocListActivity.class);
                    intent.putExtra("title",title);
                    intent.putExtra("id",id);
                    mContext.startActivity(intent);
                }
                else{
                    MainActivity mainActivity=(MainActivity)activity;
                    Intent intent = new Intent(mContext, AddReportActivity.class);
                    switch (id) {
                        case 1:
                            mainActivity.showReportFrag(2);
                            break;
                        case 2:
                            activity.startActivity(intent);
                            break;
                        case 3:
                            intent.putExtra("type_id",137); //коррупционная схема
                            activity.startActivity(intent);
                            break;
                        case 4:
                            intent.putExtra("type_id",138); //интересно знать
                            activity.startActivity(intent);
                            break;
                        case 5:
                            mainActivity.showReportFrag(1);
                            break;
                        case 6:
                            Intent intent2 = new Intent(mContext, AuthorityListActivity.class);
                            activity.startActivity(intent2);
                            break;
                    }
                }
                Log.e("DMAdapter", "open :"+title+" id: "+id);
                //Toast.makeText(context, "You Clicked "+info.getId(), Toast.LENGTH_LONG).show();
            }
        });


        return convertView;
    }
}
