package kg.prosoft.anticorruption.service;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ProsoftPC on 9/14/2017.
 */

public class Vocabulary implements Parcelable{
    int row_id,voc_id,voc_order,voc_parent;
    String voc_key, voc_value;
    boolean voc_has_children;

    public Vocabulary(){}

    public Vocabulary(int id, String key, String value, int parent, int order, boolean hasChildren){
        voc_id=id;
        voc_key=key;
        voc_value=value;
        voc_parent=parent;
        voc_order=order;
        voc_has_children=hasChildren;
    }

    public int getId(){
        return voc_id;
    }
    public void setId(int id){
        voc_id = id;
    }
    public int getRowId(){
        return row_id;
    }
    public void setRowId(int id){
        row_id = id;
    }

    public String getKey(){
        return voc_key;
    }
    public void setKey(String title){
        voc_key = title;
    }

    public String getValue(){
        return voc_value;
    }
    public void setValue(String title){
        voc_value = title;
    }
    public int getParent(){
        return voc_parent;
    }
    public void setParent(int id){
        voc_parent = id;
    }
    public int getOrder(){
        return voc_order;
    }
    public void setOrder(int id){
        voc_order = id;
    }
    public void setHasChildren(boolean hasChildren){voc_has_children=hasChildren;}
    public boolean getHasChildren(){return voc_has_children;}

    //parcelable methods
    @Override
    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(voc_key);
        parcel.writeString(voc_value);
        parcel.writeInt(voc_id);
        parcel.writeInt(voc_parent);
        parcel.writeInt(voc_order);
    }

    public static final Parcelable.Creator<Vocabulary> CREATOR
            = new Parcelable.Creator<Vocabulary>() {
        public Vocabulary createFromParcel(Parcel in) {
            return new Vocabulary(in);
        }

        public Vocabulary[] newArray(int size) {
            return new Vocabulary[size];
        }
    };

    private Vocabulary(Parcel parcel) {
        voc_key = parcel.readString();
        voc_value = parcel.readString();
        voc_id = parcel.readInt();
        voc_parent = parcel.readInt();
        voc_order = parcel.readInt();
    }
}
