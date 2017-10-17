package kg.prosoft.anticorruption;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;

import java.util.ArrayList;

import kg.prosoft.anticorruption.service.DocMenu;
import kg.prosoft.anticorruption.service.DocMenuAdapter;


/**
 * A simple {@link Fragment} subclass.
 */
public class MapMenuFragment extends Fragment {

    ListView listView ;
    ArrayList<DocMenu> infoList;
    DocMenuAdapter listAdapter;
    public MapMenuFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_map_menu, container, false);

        // Get ListView object from xml
        listView = (ListView) view.findViewById(R.id.listView);


        infoList=new ArrayList<>();
        infoList.add(new DocMenu(1,getResources().getString(R.string.corruption_map)));
        infoList.add(new DocMenu(2,getResources().getString(R.string.corruption_report)));
        infoList.add(new DocMenu(3,getResources().getString(R.string.corruption_scheme_report)));
        infoList.add(new DocMenu(4,getResources().getString(R.string.wanna_know)));
        infoList.add(new DocMenu(5,getResources().getString(R.string.all_applications)));
        infoList.add(new DocMenu(6,getResources().getString(R.string.corruption_rating)));

        listView = (ListView) view.findViewById(R.id.listView);
        listAdapter = new DocMenuAdapter(getActivity(), infoList, 1);
        // Assign adapter to ListView
        listView.setAdapter(listAdapter);
        return view;
    }

}
