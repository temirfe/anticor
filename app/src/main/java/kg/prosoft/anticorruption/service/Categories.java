package kg.prosoft.anticorruption.service;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by ProsoftPC on 7/17/2017.
 */

public class Categories implements Serializable {
    private int id;
    //private int parent;
    private String title;
    private String separator=null;
    //private String title_ky;
    //private String image;
    private boolean hasChild;
    private boolean isChild;
    private boolean openAllChildren;
    //private ArrayList<Categories>  children;

    public Categories(String separator) {
        this.separator = separator;
    }

    public Categories(int id, String title, boolean hasChild, boolean isChild, boolean openAllChildren) {
        this.id = id;
        this.title = title;
        this.isChild = isChild;
        this.hasChild = hasChild;
        this.openAllChildren = openAllChildren;
    }
    /*public Categories(int id, String title, boolean isChild) {
        this.id = id;
        this.title = title;
        this.isChild = isChild;
    }*/

    /*public Categories(int id, String title, ArrayList<Categories> children) {
        this.id = id;
        //this.image = image;
        this.title = title;
        this.children=children;
    }*/

    public int getId() {
        return id;
    }
    public String getTitle() {
        return title;
    }
    public String getSeparator() {
        return separator;
    }
    /*public String getTitleKy() {
        return title_ky;
    }*/
    public boolean getIsChild() {
        return isChild;
    }
    public boolean getHasChild() {
        return hasChild;
    }
    public boolean getOpenAllChildren() {
        return openAllChildren;
    }

    /*public String getImage() {
        return image;
    }*/
    /*public ArrayList<Categories> getChildren() {
        return children;
    }*/

}