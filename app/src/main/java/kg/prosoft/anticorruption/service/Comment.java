package kg.prosoft.anticorruption.service;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by ProsoftPC on 10/27/2017.
 */

public class Comment {
    private int id, model_id;
    private String title,date,model, model_title;

    public Comment(int id, String title,String date, int model_id, String model, String model_title) {
        this.id = id;
        this.title = title;
        this.date=date;
        this.model_id=model_id;
        this.model=model;
        this.model_title=model_title;
    }

    public void setId(int in) {id=in;}
    public int getId() {return id;}

    public void setModelId(int in) {model_id=in;}
    public int getModelId() {return model_id;}

    public void setTitle(String s) {title=s;}
    public String getTitle() {
        if (title.length() > 155) {
            title = title.substring(0, 155) + "...";
        }
        title=title.trim();
        return title;
    }
    public void setModel(String s) {model=s;}
    public String getModel() { return model;}

    public void setModelTitle(String s) {model_title=s;}
    public String getModelTitle() { return model_title;}

    public void setDate(String s) {date=s;}
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
