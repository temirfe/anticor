package kg.prosoft.anticorruption.service;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by ProsoftPC on 10/18/2017.
 */

public class Analytics {
    private int id, author_id;
    private String title,text,date,author_name;

    public Analytics(int id, String title, String text, String date, String author_name, int author_id) {
        this.id = id;
        this.title = title;
        this.text = text;
        this.date=date;
        this.author_name=author_name;
        this.author_id=author_id;
    }

    public void setId(int in) {id=in;}
    public int getId() {return id;}

    public void setAuthorId(int in) {author_id=in;}
    public int getAuthorId() {return author_id;}

    public void setTitle(String s) {title=s;}
    public String getTitle() {return title;}

    public void setText(String s) {text=s;}
    public String getText() {
        text=text.trim();
        return text;
    }

    public void setAuthorName(String s) {author_name=s;}
    public String getAuthorName() {
        return author_name;
    }

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
