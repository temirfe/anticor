package kg.prosoft.anticorruption.service;

/**
 * Created by ProsoftPC on 10/16/2017.
 */

public class DocMenu {
    int id;
    String title;
    public DocMenu(int id, String title){
        this.id=id;
        this.title=title;
    }

    public void setId(int id) {this.id=id;}
    public int getId() {
        return id;
    }
    public String getTitle() {return title;}
    public void setTitle(String str) {title=str;}
}
