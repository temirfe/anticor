package kg.prosoft.anticorruption.service;

/**
 * Created by ProsoftPC on 10/2/2017.
 */

public class Authority {
    private int id, parent_id, rating;
    private String title,text,image;

    public Authority(int id, String title, String text, String image, int par_id) {
        this.id = id;
        this.title = title;
        this.text = text;
        this.image=image;
        this.parent_id=par_id;
    }

    public int getId() {
        return id;
    }
    public String getTitle() {
        return title;
    }

    public String getText() {
        text=text.trim();
        return text;
    }
    public String getImage() {
        return image;
    }
    public int getParentId() {
        return parent_id;
    }
    public int getRating() {
        return rating;
    }
    public void setRating(int r) {
        rating=r;
    }
}
