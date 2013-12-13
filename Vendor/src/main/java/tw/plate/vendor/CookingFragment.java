package tw.plate.vendor;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;

public class CookingFragment extends Fragment {

    private View v;
    private ListViewCustomAdapter listViewCustomAdapter;
    private GridViewCustomAdapter gridviewCustomAdapter;

    private class ListViewCustomAdapter extends BaseAdapter {
        LayoutInflater inflater;

        public class ViewHolder{
            TextView tv_listrow_cooking;
        }
        public ListViewCustomAdapter(Context context){
            inflater = LayoutInflater.from(context);

        }
        @Override
        public boolean isEnabled(int position) {
            return false;
        }
        public int getCount(){
            return 15;
        }
        public Object getItem(int position){
            return "test";
        }
        public long getItemId(int position){
            return position;
        }
        public View getView(final int arg0, View convertview, ViewGroup arg2) {
            ViewHolder viewHolder = null;
            if(convertview == null) {
                convertview = inflater.inflate(R.layout.listrow_cooking, null);

                viewHolder=new ViewHolder();
                viewHolder.tv_listrow_cooking = (TextView) convertview.findViewById(R.id.tv_listrow_cooking);

                convertview.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.d(Constants.LOG_TAG, "clicked");
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                        builder.setMessage(R.string.confirm_finish_warning_message)
                                .setTitle(R.string.confirm_finish_warning_title);

                        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User clicked OK button
                                Log.d(Constants.LOG_TAG, "send request to server here");
                            }
                        });

                        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // If cancel, do nothing
                            }
                        });

                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                });

                convertview.setTag(viewHolder);
            }
            else {
                viewHolder=(ViewHolder)convertview.getTag();
            }
            // set values
            //
            viewHolder.tv_listrow_cooking.setText("test" + arg0);

            return convertview;
        }

    }

    private class GridViewCustomAdapter extends BaseAdapter {
        LayoutInflater inflater;

        public class ViewHolder{
            TextView tv_listrow_cooking;
        }
        public GridViewCustomAdapter(Context context){
            inflater = LayoutInflater.from(context);

        }
        @Override
        public boolean isEnabled(int position) {
            return false;
        }
        public int getCount(){
            return 200;
        }
        public Object getItem(int position){
            return "test";
        }
        public long getItemId(int position){
            return position;
        }
        public View getView(final int arg0, View convertview, ViewGroup arg2) {
            ViewHolder viewHolder = null;
            if(convertview == null) {
                convertview = inflater.inflate(R.layout.listrow_cooking, null);

                viewHolder=new ViewHolder();
                viewHolder.tv_listrow_cooking = (TextView) convertview.findViewById(R.id.tv_listrow_cooking);

                convertview.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.d(Constants.LOG_TAG, "clicked");
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                        builder.setMessage(R.string.confirm_finish_warning_message)
                                .setTitle(R.string.confirm_finish_warning_title);

                        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User clicked OK button
                                Log.d(Constants.LOG_TAG, "send request to server here");
                            }
                        });

                        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // If cancel, do nothing
                            }
                        });

                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                });

                convertview.setTag(viewHolder);
            }
            else {
                viewHolder=(ViewHolder)convertview.getTag();
            }
            // set values
            //
            viewHolder.tv_listrow_cooking.setText("p" + arg0);

            return convertview;
        }

    }

    public static CookingFragment newInstance(String param1, String param2) {
        CookingFragment fragment = new CookingFragment();
        return fragment;
    }
    public CookingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_cooking, container, false);

        ListView lv = (ListView) v.findViewById(R.id.lv_cooking);
        listViewCustomAdapter = new ListViewCustomAdapter(this.getActivity());
        lv.setAdapter(listViewCustomAdapter);

        GridView gv = (GridView) v.findViewById(R.id.gv_cooking);
        gridviewCustomAdapter = new GridViewCustomAdapter(this.getActivity());
        gv.setAdapter(gridviewCustomAdapter);

        return v;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public static CookingFragment newInstance() {
        CookingFragment cookingFragment = new CookingFragment();
        return cookingFragment;
    }
}
