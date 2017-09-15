package kg.prosoft.anticorruption.service;

import android.content.Context;
import android.util.Log;
import android.util.SparseArray;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by ProsoftPC on 1/30/2017.
 */

public class CategoriesCache implements Serializable {
    //private static final long serialVersionUID = 1L;

    public String title;
    public ArrayList<Categories> mCategoriesList;
    HashMap<Integer,ArrayList<Categories>> ctgMap;

    public CategoriesCache() {}
    /*public CategoriesCache(ArrayList<Categories> saveList) {
        mCategoriesList = saveList;
    }*/
    public CategoriesCache(HashMap<Integer,ArrayList<Categories>> saveMap) {
        ctgMap = saveMap;
    }

    public boolean saveObject(CategoriesCache obj, Context c) {
        final File suspend_f = new File(c.getCacheDir(), "zamCtgs");
        Log.e("CACHE", "trying to save");

        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        boolean keep = true;

        try {
            fos = new FileOutputStream(suspend_f);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(obj);
            Log.e("CACHE", "write");
        } catch (Exception e) {
            e.printStackTrace();
            keep = false;
        } finally {
            try {
                if (oos != null) oos.close();
                if (fos != null) fos.close();
                if (!keep) suspend_f.delete();
            } catch (Exception e) { /* do nothing */ }
        }

        return keep;
    }

    public CategoriesCache getObject(Context c) {
        final File suspend_f = new File(c.getCacheDir(), "zamCtgs");

        CategoriesCache simpleClass = null;
        FileInputStream fis = null;
        ObjectInputStream is = null;

        try {
            fis = new FileInputStream(suspend_f);
            is = new ObjectInputStream(fis);
            simpleClass = (CategoriesCache) is.readObject();
        } catch (Exception e) {
            String val = e.getMessage();
        } finally {
            try {
                if (fis != null) fis.close();
                if (is != null) is.close();
            } catch (Exception e) {
            }
        }

        return simpleClass;
    }

    /*public ArrayList<Categories> getCategories(){
        return mCategoriesList;
    }*/
    public HashMap<Integer,ArrayList<Categories>> getCategories(){
        return ctgMap;
    }
}