package kg.prosoft.anticorruption.service;

/**
 * Created by ProsoftPC on 11/1/2017.
 */

public class Page {
    private int id;
    private String title,text, description;

    public Page() {}
    public Page(int id, String title, String text, String description) {
        this.id = id;
        this.title = title;
        this.text = text;
        this.description = description;
    }

    public void setId(int in) {id=in;}
    public int getId() {return id;}

    public void setTitle(String s) {title=s;}
    public String getTitle() {return title;}

    public void setDescription(String s) {description=s;}
    public String getDescription() {return description;}

    public void setText(String s) {text=s;}
    public String getText() {text=text.trim();return text;}

}
