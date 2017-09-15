package kg.prosoft.anticorruption.service;

/**
 * Created by Temirbek on 8/26/2017.
 */

public class Lookup {
    int _id;
    int _lookup_id;
    String _title, _text;

    // Empty constructor
    public Lookup(){

    }
    // constructor
    public Lookup(int id, int lookup_id, String title, String text){
        this._id = id;
        this._lookup_id = lookup_id;
        this._title = title;
        this._text = text;
    }
    // constructor
    public Lookup(int lookup_id, String title, String text){
        this._lookup_id = lookup_id;
        this._title = title;
        this._text = text;
    }

    // getting ID
    public int getID(){
        return this._id;
    }

    // setting id
    public void setID(int id){
        this._id = id;
    }

    public int getLookupId(){
        return this._lookup_id;
    }
    public void setLookupId(int id){
        this._lookup_id = id;
    }

    public String getTitle(){
        return this._title;
    }
    public void setTitle(String title){
        this._title = title;
    }

    public String getText(){
        return this._text;
    }
    public void setText(String text){
        this._text = text;
    }
}
