package kg.prosoft.anticorruption.service;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kg.prosoft.anticorruption.R;

/**
 * Created by ProsoftPC on 9/14/2017.
 */

public class SectorDialog extends DialogFragment {

    /* The activity that creates an instance of this dialog fragment must
    * implement this interface in order to receive event callbacks.
    * Each method passes the DialogFragment in case the host needs to query it. */
    public interface SectorDialogListener {
        //public void onDialogPositiveClick(DialogFragment dialog);
        //public void onDialogNegativeClick(DialogFragment dialog);
        void onDialogSelectClick(int id);
    }

    // Use this instance of the interface to deliver action events
    SectorDialogListener mListener;

    // Override the Fragment.onAttach() method to instantiate the SectorDialogListener
    @TargetApi(23)
    @Override public void onAttach(Context context) {
        //This method avoid to call super.onAttach(context) if I'm not using api 23 or more
        super.onAttach(context);
        onAttachToContext(context);
    }

    /*
     * Deprecated on API 23
     * Use onAttachToContext instead
     */
    @SuppressWarnings("deprecation")
    @Override public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (Build.VERSION.SDK_INT < 23) {
            onAttachToContext(activity);
        }
    }

    /*
     * This method will be called from one of the two previous method
     */
    protected void onAttachToContext(Context context) {
        try {
            mListener = (SectorDialogListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(context.toString()
                    + " must implement NoticeDialogListener");
        }
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Activity activity=getActivity();
        final Bundle b = getArguments();
        final int selected=b.getInt("selected");
        String title=b.getString("title");
        final String type=b.getString("type");
        BaseAdapter myAdapter;
        if(type!=null && type.equals("authority")){
            ArrayList<Authority> authList=b.getParcelableArrayList("list");
            myAdapter  = new AuthDialogAdapter(activity, authList, selected);
        }
        else{
            ArrayList<Vocabulary> vocList=b.getParcelableArrayList("list");
            myAdapter  = new VocAdapter(activity, vocList, selected);
        }
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(title)
                .setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                }).setAdapter(myAdapter, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        int id;
                        if(type!=null && type.equals("authority")){
                            ArrayList<Authority> authList=b.getParcelableArrayList("list");
                            Authority auth=authList.get(which);
                            id= auth.getId();
                        }
                        else{
                            ArrayList<Vocabulary> vocList=b.getParcelableArrayList("list");
                            Vocabulary voc=vocList.get(which);
                            id= voc.getId();
                        }
                        // The 'which' argument contains the index position
                        // of the selected item
                        Log.e("DIALOG",which+" "+id);
                        mListener.onDialogSelectClick(id);
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
