package kg.prosoft.anticorruption.service;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ProsoftPC on 10/2/2017.
 */

public class Authority implements Parcelable {
    private int id, parent_id, rating, row_id, comment_count, report_count;
    private String title,text,image;

    public Authority(){}
    public Authority(int id, String title, String image, int par_id) {
        this.id = id;
        this.title = title;
        this.image=image;
        this.parent_id=par_id;
    }
    public Authority(int id, String title, String text, String image, int par_id, int rating, int comments, int reports) {
        this.id = id;
        this.title = title;
        this.text = text;
        this.image=image;
        this.parent_id=par_id;
        this.rating=rating;
        comment_count=comments;
        report_count=reports;
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

    public int getCommentCount() {return comment_count;}
    public void setCommentCount(int c) {comment_count=c;}

    public int getReportCount() {return report_count;}
    public void setReportCount(int c) {report_count=c;}

    public int getRowId(){
        return row_id;
    }
    public void setRowId(int id) {row_id = id;}

    //parcelable methods
    @Override
    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(title);
        parcel.writeString(image);
        parcel.writeInt(id);
        parcel.writeInt(parent_id);
    }

    public static final Parcelable.Creator<Authority> CREATOR
            = new Parcelable.Creator<Authority>() {
        public Authority createFromParcel(Parcel in) {
            return new Authority(in);
        }

        public Authority[] newArray(int size) {
            return new Authority[size];
        }
    };

    private Authority(Parcel parcel) {
        title = parcel.readString();
        image = parcel.readString();
        id = parcel.readInt();
        parent_id = parcel.readInt();
    }

}
