package kg.prosoft.anticorruption.service;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Created by ProsoftPC on 9/29/2017.
 */

public class ReportMapItem implements ClusterItem {
    private final LatLng mPosition;
    private String mTitle;
    private String mSnippet;
    private int report_id, type_id;

    public ReportMapItem(double lat, double lng) {
        mPosition = new LatLng(lat, lng);
    }

    public ReportMapItem(double lat, double lng, String title, String snippet) {
        mPosition = new LatLng(lat, lng);
        mTitle = title;
        mSnippet = snippet;
    }

    public ReportMapItem(double lat, double lng, String title, int reportId, int typeId) {
        mPosition = new LatLng(lat, lng);
        mTitle = title;
        report_id=reportId;
        type_id=typeId;
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    @Override
    public String getTitle() {
        return mTitle;
    }

    @Override
    public String getSnippet() {
        return mSnippet;
    }

    public int getReportId() {
        return report_id;
    }

    public int getTypeId() {
        return type_id;
    }
}
