package kg.prosoft.anticorruption.service;

/**
 * Created by ProsoftPC on 10/2/2017.
 */

public class Authority {
    private int id, parent_id, rating, row_id;
    private String title,text,image;

    public Authority(){}
    public Authority(int id, String title, String text, String image, int par_id) {
        this.id = id;
        this.title = title;
        this.text = text;
        this.image=image;
        this.parent_id=par_id;
    }

    public void setId(int id) {this.id=id;}
    public int getId() {
        return id;
    }
    public String getTitle() {return title;}
    public void setTitle(String str) {title=str;}

    public String getText() {
        text=text.trim();
        return text;
    }
    public void setText(String str) {text=str;}

    public void setImage(String str) {image=str;}
    public String getImage() {
        return image;
    }

    public void setParentId(int id) {this.parent_id=id;}
    public int getParentId() {
        return parent_id;
    }

    public int getRating() {
        return rating;
    }
    public void setRating(int r) {
        rating=r;
    }

    public int getRowId(){
        return row_id;
    }
    public void setRowId(int id) {row_id = id;}

}
