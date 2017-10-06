package kg.prosoft.anticorruption.service;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by ProsoftPC on 9/27/2017.
 */

public class News {
    private int id, category_id, views, row_id;
    private String title,description,text,date,image;

    public News() {}
    public News(int id, String title, String description, String text, String date, String image, int category_id, int views) {
        this.id = id;
        this.title = title;
        this.text = text;
        this.date=date;
        this.description=description;
        this.image=image;
        this.category_id=category_id;
        this.views=views;
    }

    public void setRowId(int in) {row_id=in;}
    public int getRowId() {return row_id;}

    public void setId(int in) {id=in;}
    public int getId() {return id;}

    public void setTitle(String s) {title=s;}
    public String getTitle() {return title;}

    public void setText(String s) {text=s;}
    public String getText() {
        text=text.trim();
        return text;
    }

    public void setImage(String s) {image=s;}
    public String getImage() {
        return image;
    }

    public void setCategoryId(int in) {category_id=in;}
    public int getCategoryId() {
        return category_id;
    }

    public void setDescription(String s) {description=s;}
    public String getDescription() {
        if (description.length() > 155) {
            description = description.substring(0, 155) + "...";
        }
        description=description.trim();
        return description;
    }

    public void setDate(String s) {date=s;}
    public String getRawDate(){return date;}
    public String getDate() {
        //long timeNow = System.currentTimeMillis();
        //long timeThen;
        Locale locale = new Locale("ru");
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",locale);
        try{
            Date dateObj = formatter.parse(date);
            SimpleDateFormat fmt = new SimpleDateFormat("dd.MM.yyyy HH:mm",locale);
            return fmt.format(dateObj);
        }
        catch (ParseException e) {
            e.printStackTrace();
        }



        /*try {
            if(date==null){
                timeThen=timeNow;
            }
            else{
                Date dateObj = formatter.parse(date);
                timeThen=dateObj.getTime();
            }
            return DateUtils.getRelativeTimeSpanString(timeThen, timeNow,
                    DateUtils.SECOND_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE)
                    .toString();
        } catch (ParseException e) {
            e.printStackTrace();
        }*/
        return "getDate() error";
    }

    public void setViews(int in) {views=in;}
    public int getViews() {return views;}
}
