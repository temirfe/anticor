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
 * Created by ProsoftPC on 10/2/2017.
 */

public class AuthorityAdapter extends BaseAdapter {
    private Context mContext;
    private List<Authority> authList;
    private LayoutInflater inflater;

    public AuthorityAdapter(Context mContext, List<Authority> authList) {
        this.mContext = mContext;
        this.authList = authList;
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
    public View getView(int position, View convertView, ViewGroup parent) {

        if (inflater == null)
            inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null)
            convertView = inflater.inflate(R.layout.item_authority_row,null);

        Authority authority = authList.get(position);
        String title=authority.getTitle();
        final SpannableStringBuilder boldTitle = new SpannableStringBuilder(title);
        boldTitle.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, title.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        TextView tv_title=(TextView) convertView.findViewById(R.id.id_tv_title);
        tv_title.setText(boldTitle);
        TextView tv_text=(TextView) convertView.findViewById(R.id.id_tv_text);

        convertView.setTag(authority.getId());
        return convertView;
    }
}
