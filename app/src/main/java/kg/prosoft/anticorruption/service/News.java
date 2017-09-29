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
    private int id, category_id;
    private String title,description,text,date,image;

    public News(int id, String title, String description, String text, String date, String image, int category_id) {
        this.id = id;
        this.title = title;
        this.text = text;
        this.date=date;
        this.description=description;
        this.image=image;
        this.category_id=category_id;
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
    public int getCategoryId() {
        return category_id;
    }
    public String getDescription() {
        if (description.length() > 155) {
            description = description.substring(0, 155) + "...";
        }
        description=description.trim();
        return description;
    }

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
}
