package kg.prosoft.anticorruption.service;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import kg.prosoft.anticorruption.R;

/**
 * Created by ProsoftPC on 10/27/2017.
 */

public class CommentAdapter extends BaseAdapter {
    private Context mContext;
    private List<Comment> CommentList;
    private LayoutInflater inflater;

    public CommentAdapter(Context mContext, List<Comment> CommentList) {
        this.mContext = mContext;
        this.CommentList = CommentList;
    }

    @Override
    public int getCount() {
        return CommentList.size();
    }

    @Override
    public Object getItem(int position) {
        return CommentList.get(position);
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
            convertView = inflater.inflate(R.layout.item_comment_row,null);

        Comment Comment = CommentList.get(position);
        String title=Comment.getTitle();
        String model=Comment.getModel();
        String model_title=Comment.getModelTitle();
        /*final SpannableStringBuilder boldTitle = new SpannableStringBuilder(title);
        boldTitle.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, title.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);*/

        TextView tv_title=(TextView) convertView.findViewById(R.id.id_tv_title);
        tv_title.setText(title);

        TextView tv_model=(TextView) convertView.findViewById(R.id.id_tv_model);
        if(model.equals("report")){model=mContext.getResources().getString(R.string.report)+":";}
        else if(model.equals("news")){model=mContext.getResources().getString(R.string.news)+":";}
        else if(model.equals("authority")){model=mContext.getResources().getString(R.string.authority)+":";}
        tv_model.setText(model);

        TextView tv_model_title=(TextView) convertView.findViewById(R.id.id_tv_model_title);
        tv_model_title.setText(model_title);

        TextView dateTv=(TextView) convertView.findViewById(R.id.textView_date);
        dateTv.setText(Comment.getDate());

        /*if(position==getCount()-1){
            Log.i("END", "Reached");
        }*/

        return convertView;
    }
}
