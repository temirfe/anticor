package kg.prosoft.anticorruption.service;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import kg.prosoft.anticorruption.R;

/**
 * Created by ProsoftPC on 10/17/2017.
 */

public class EducationAdapter extends BaseAdapter {
    private Context mContext;
    private List<Education> newsList;
    private LayoutInflater inflater;

    public EducationAdapter(Context mContext, List<Education> newsList) {
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

        Education news = newsList.get(position);
        String title=news.getTitle();
        /*final SpannableStringBuilder boldTitle = new SpannableStringBuilder(title);
        boldTitle.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, title.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);*/

        TextView tv_title=(TextView) convertView.findViewById(R.id.id_tv_title);
        tv_title.setText(title);
        /*TextView tv_text=(TextView) convertView.findViewById(R.id.id_tv_text);
        tv_text.setText(news.getDescription());*/
        TextView dateTv=(TextView) convertView.findViewById(R.id.textView_date);
        dateTv.setText(news.getDate());
        ImageView iv_thumb=(ImageView)convertView.findViewById(R.id.id_iv_thumb);
        String img=news.getImage();
        if(img!=null && !img.isEmpty()){
            GlideApp.with(mContext)
                    .load(Endpoints.NEWS_IMG+news.getId()+"/"+img)
                    .placeholder(R.drawable.placeholder) // optional
                    .dontAnimate()
                    .into(iv_thumb);
        }
        else{
            GlideApp.with(mContext).clear(iv_thumb);
            iv_thumb.setImageDrawable(null);
        }

        convertView.setTag(news.getId());

        /*if(position==getCount()-1){
            Log.i("END", "Reached");
        }*/

        return convertView;
    }
}