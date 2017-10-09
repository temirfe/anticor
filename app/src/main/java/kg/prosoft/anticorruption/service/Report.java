package kg.prosoft.anticorruption.service;

import android.text.format.DateUtils;
import android.text.format.Time;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by ProsoftPC on 9/27/2017.
 */

public class Report {
    private int id,user_id, authority_id, category_id,type_id, city_id, row_id;
    private double lat, lng;
    private String title,text,description,date,city_title;

    public Report() {}
    public Report(int id, String title, String text, String date, double lat, double lng) {
        this.id = id;
        this.title = title;
        this.text = text;
        this.date=date;
        this.lat=lat;
        this.lng=lng;
    }

    public void setRowId(int in) {row_id=in;}

    public void setId(int in) {id=in;}
    public int getId() {
        return id;
    }

    public void setUserId(int id) { this.user_id=id;}
    public int getUserId() {return id;}

    public void setAuthorityId(int id) { this.authority_id=id;}
    public int getAuthorityId() {return authority_id;}

    public void setCategoryId(int id) { this.category_id=id;}
    public int getCategoryId() {return category_id;}

    public void setTypeId(int id) { this.type_id=id;}
    public int getTypeId() {return type_id;}

    public void setCityId(int id) { this.city_id=id;}
    public int getCityId() {return city_id;}

    public void setLat(double d) {lat=d;}
    public double getLat() {return lat;}

    public void setLng(double d) {lng=d;}
    public double getLng() {return lng;}

    public void setTitle(String s) {title=s;}
    public String getTitle() {
        return title;
    }

    public void setText(String s) {text=s;}
    public String getText() {
        return text;
    }

    public void setDescription(String s) {description=s;}
    public String getDescription() {
        description=text;
        if (description.length() > 125) {
            description = description.substring(0, 125) + "...";
        }
        description=description.trim();

        return description;
    }

    public void setCityTitle(String s) {city_title=s;}
    public String getCityTitle() {return city_title;}

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

        /*Date now = new Date();
        long timeThen;

        try {
            if(date==null){
                timeThen=now.getTime();
            }
            else{
                Date dateObj = formatter.parse(date);
                timeThen=dateObj.getTime();
            }
            return DateUtils.getRelativeTimeSpanString(timeThen, now.getTime(),
                    DateUtils.SECOND_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE)
                    .toString();
        } catch (ParseException e) {
            e.printStackTrace();
        }*/
        return "getDate() error";
    }
    public String getRawDate(){return date;}
}
