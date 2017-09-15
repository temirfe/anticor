package kg.prosoft.anticorruption.service;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import kg.prosoft.anticorruption.InfoActivity;
import kg.prosoft.anticorruption.R;
import kg.prosoft.anticorruption.WebViewActivity;


/**
 * Created by ProsoftPC on 8/28/2017.
 */

public class InfoAdapter extends BaseAdapter {
    List<Info> infoList;
    Context context;
    String TAG = "InfoAdapter";
    private static LayoutInflater inflater=null;
    public InfoAdapter(InfoActivity mainActivity, ArrayList<Info> info) {
        // TODO Auto-generated constructor stub
        infoList=info;
        context=mainActivity;
        inflater = ( LayoutInflater )context.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return infoList.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    public class Holder
    {
        TextView tv;
    }
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        Holder holder=new Holder();
        View rowView;
        rowView = inflater.inflate(R.layout.info_row, null);

        final Info info = infoList.get(position);
        final String title=info.getTitle();
        final String text=info.getText();
        Log.e(TAG, "title :"+title+" textsize: "+text.length());

        holder.tv=(TextView) rowView.findViewById(R.id.id_tv_info);
        holder.tv.setText(title);
        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                Intent intent = new Intent(context, WebViewActivity.class);
                Log.e(TAG, "open :"+title+" textsize: "+text.length());
                intent.putExtra("title",title);
                intent.putExtra("text",text);
                context.startActivity(intent);
                //Toast.makeText(context, "You Clicked "+info.getId(), Toast.LENGTH_LONG).show();
            }
        });
        return rowView;
    }

}