package kg.prosoft.anticorruption.service;

/**
 * Created by ProsoftPC on 8/28/2017.
 */

public class Info {
    String title, text;
    int id;

    // constructor
    public Info(int component_id, String title, String text){
        this.title = title;
        this.text = text;
        this.id = component_id;
    }

    public String getText(){return text;}
    public String getTitle(){return title;}
    public int getId(){return id;}
}
