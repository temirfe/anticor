package kg.prosoft.anticorruption.service;

/**
 * Created by ProsoftPC on 9/29/2017.
 */

import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.model.Marker;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import kg.prosoft.anticorruption.R;

/** Demonstrates customizing the info window and/or its contents. */
public class CustomInfoWindowAdapter implements InfoWindowAdapter {

    // These are both viewgroups containing an ImageView with id "badge" and two TextViews with id
    // "title" and "snippet".

    private final View mContents;

    public CustomInfoWindowAdapter(LayoutInflater inflater) {
        mContents = inflater.inflate(R.layout.custom_info_contents, null);
    }

    @Override
    public View getInfoWindow(Marker marker) {
        render(marker, mContents);
        return mContents;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return(null);
    }

    private void render(Marker marker, View view) {

        String title = marker.getTitle();
        TextView titleUi = ((TextView) view.findViewById(R.id.title));
        if (title != null) {
            titleUi.setText(title);
        } else {
            titleUi.setText("");
        }

        String snippet = marker.getSnippet();
        TextView snippetUi = ((TextView) view.findViewById(R.id.snippet));
        if (snippet != null) {
            snippetUi.setText(snippet);
        } else {
            snippetUi.setText("");
        }
    }
}