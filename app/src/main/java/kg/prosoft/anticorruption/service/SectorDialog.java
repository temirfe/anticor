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
            Log.e("ATTACH","worked");
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(context.toString()
                    + " must implement NoticeDialogListener");
        }
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Activity activity=getActivity();
        VocAdapter myAdapter=new VocAdapter();
        final List<Vocabulary> vocList=new ArrayList<>();
        HashMap<Integer,String> mMap;
        Bundle b = getArguments();
        if(b.getSerializable("hashmap") != null) {
            mMap = (HashMap<Integer, String>) b.getSerializable("hashmap");
            int selected=b.getInt("selected");
            for (Map.Entry<Integer, String> entry : mMap.entrySet())
            {
                vocList.add(new Vocabulary(entry.getKey(), entry.getValue()));
            }
            myAdapter  = new VocAdapter(activity, vocList, selected);
        }

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.select_sector)
                .setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                }).setAdapter(myAdapter, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                        Vocabulary voc=vocList.get(which);
                        int id= voc.getId();
                        Log.e("DIALOG",which+" "+id);
                        mListener.onDialogSelectClick(id);
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
