package kg.prosoft.anticorruption.service;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import kg.prosoft.anticorruption.R;

/**
 * Created by ProsoftPC on 9/27/2017.
 */

public class NewsAdapter extends BaseAdapter {
    private Context mContext;
    private List<News> newsList;
    private LayoutInflater inflater;
    int range=0;

    public NewsAdapter(Context mContext, List<News> newsList) {
        this.mContext = mContext;
        this.newsList = newsList;
    }

    @Override
    public int getCount() {
        return newsList.size();
    }

    @Override
    public Object getItem(int position) {
        return newsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (inflater == null)
            inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null)
            convertView = inflater.inflate(R.layout.item_news_row,null);

        News news = newsList.get(position);
        String title=news.getTitle();
        final SpannableStringBuilder boldTitle = new SpannableStringBuilder(title);
        boldTitle.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, title.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        TextView tv_title=(TextView) convertView.findViewById(R.id.id_tv_title);
        tv_title.setText(boldTitle);
        TextView tv_text=(TextView) convertView.findViewById(R.id.id_tv_text);
        tv_text.setText(news.getDescription());
        TextView dateTv=(TextView) convertView.findViewById(R.id.textView_date);
        dateTv.setText(news.getDate());

        convertView.setTag(news.getId());

        /*if(position==getCount()-1){
            Log.i("END", "Reached");
        }*/

        return convertView;
    }
}
