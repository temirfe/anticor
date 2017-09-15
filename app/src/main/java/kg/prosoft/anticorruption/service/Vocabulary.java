package kg.prosoft.anticorruption.service;

import android.os.Parcelable;

/**
 * Created by ProsoftPC on 9/14/2017.
 */

public class Vocabulary{
    int voc_id,voc_order,voc_parent;
    String voc_key, voc_value;

    public Vocabulary(){}
    public Vocabulary(int id, String value){
        voc_id=id;
        voc_value=value;
    }
    public Vocabulary(int id, String key, String value){
        voc_id=id;
        voc_key=key;
        voc_value=value;
    }

    public int getId(){
        return voc_id;
    }
    public void setId(int id){
        voc_id = id;
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
}
