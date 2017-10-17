package kg.prosoft.anticorruption.service;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by ProsoftPC on 10/17/2017.
 */

public class Document {
    private int id, category_id;
    private String title,text,date, ctg_title;

    //used for documents
    public Document(int id, String title, String text, String date, int category_id) {
        this.id = id;
        this.title = title;
        this.text = text;
        this.date=date;
        this.category_id=category_id;
    }

    public void setId(int in) {id=in;}
    public int getId() {return id;}

    public void setTitle(String s) {title=s;}
    public String getTitle() {return title;}

    public void setCtgTitle(String s) {ctg_title=s;}
    public String getCtgTitle() {return ctg_title;}

    public void setText(String s) {text=s;}
    public String getText() {
        text=text.trim();
        return text;
    }

    public void setCategoryId(int in) {category_id=in;}
    public int getCategoryId() {
        return category_id;
    }

    public void setDate(String s) {date=s;}
    public String getDate() {
        //long timeNow = System.currentTimeMillis();
        //long timeThen;
        Locale locale = new Locale("ru");
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd",locale);
        try{
            Date dateObj = formatter.parse(date);
            SimpleDateFormat fmt = new SimpleDateFormat("dd.MM.yyyy",locale);
            return fmt.format(dateObj);
        }
        catch (ParseException e) {
            e.printStackTrace();
        }
        return "getDate() error";
    }
}