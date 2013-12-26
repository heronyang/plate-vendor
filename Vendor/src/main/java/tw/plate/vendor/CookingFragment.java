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
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class CookingFragment extends Fragment {

    private View v;
    private ListViewCustomAdapter listViewCustomAdapter;
    private GridViewCustomAdapter gridviewCustomAdapter;

    List<PlateVendorService.OrderSingle> orders;
    List<PlateVendorService.OrderSingle> orders_cooking;

    //================================================================================
    // Adapters
    //================================================================================
    private class ListViewCustomAdapter extends BaseAdapter {
        LayoutInflater inflater;

        public class ViewHolder{
            TextView tv_listrow_cooking;
            TextView tv_slip_number;
            TextView tv_phone;
            TextView tv_totalPrice;

        }
        public ListViewCustomAdapter(Context context){
            inflater = LayoutInflater.from(context);

        }
        @Override
        public boolean isEnabled(int position) {
            return false;
        }
        public int getCount(){
            if (orders_cooking == null || orders_cooking.isEmpty())  return 0;
            return orders_cooking.size();
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
                viewHolder.tv_slip_number = (TextView) convertview.findViewById(R.id.tv_slip_number);
                viewHolder.tv_phone = (TextView) convertview.findViewById(R.id.tv_phone);
                viewHolder.tv_totalPrice = (TextView) convertview.findViewById(R.id.tv_total_price);

                Button bt_cancel = (Button)convertview.findViewById(R.id.cancel_order_button);
                Button bt_finish = (Button)convertview.findViewById(R.id.finish_order_button);

                bt_cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // finishOrder
                        int order_key = orders_cooking.get(arg0).order.id;
                        doubleConfirmCancel(order_key, getActivity());
                    }
                });

                bt_finish.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // finishOrder
                        int order_key = orders_cooking.get(arg0).order.id;
                        doubleConfirmFinish(order_key, getActivity());
                    }
                });

                convertview.setTag(viewHolder);
            }
            else {
                viewHolder=(ViewHolder)convertview.getTag();
            }
            // set values
            String output = "";
            PlateVendorService.OrderV1 o = orders_cooking.get(arg0).order;
            //output += ("time : " + o.mtime + "\n");
            //output += ("ph : " + orders_cooking.get(arg0).user.username + "\n");

            List<PlateVendorService.OrderItemV1> order_items = orders_cooking.get(arg0).order_items;
            int totalPrice = 0;
            for (PlateVendorService.OrderItemV1 oi : order_items) {
                output += oi.meal.meal_name + " * " + oi.amount + "\n";
                totalPrice += oi.meal.meal_price * oi.amount;
            }
            //output += "total : " + totalPrice;

            viewHolder.tv_totalPrice.setText(totalPrice+" 元");
            viewHolder.tv_phone.setText(orders_cooking.get(arg0).user.username +"");
            viewHolder.tv_slip_number.setText((o.pos_slip_number%100)+"");
            viewHolder.tv_listrow_cooking.setText(output);

            return convertview;
        }

    }

    private class GridViewCustomAdapter extends BaseAdapter {
        LayoutInflater inflater;

        public class ViewHolder{
            TextView tv_number_slip;
        }
        public GridViewCustomAdapter(Context context){
            inflater = LayoutInflater.from(context);

        }
        @Override
        public boolean isEnabled(int position) {
            return false;
        }
        public int getCount(){
            if (orders_cooking == null || orders_cooking.isEmpty())  return 0;
            return orders_cooking.size();
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
                convertview = inflater.inflate(R.layout.gridview_number_slip, null);

                viewHolder=new ViewHolder();
                viewHolder.tv_number_slip = (TextView) convertview.findViewById(R.id.tv_number_slip);

                convertview.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.d(Constants.LOG_TAG, "clicked");
                        //
                    }
                });

                convertview.setTag(viewHolder);
            }
            else {
                viewHolder=(ViewHolder)convertview.getTag();
            }

            // set values
            int ns = orders_cooking.get(arg0).order.pos_slip_number%100;
            viewHolder.tv_number_slip.setText("" + ns);

            return convertview;
        }

    }

    //================================================================================
    // Layout Updates
    //================================================================================
    public void cookingListUpdate() {
        // should wait for the data is grabbed
        Log.d(Constants.LOG_TAG, "Cooking: cooking list update start");
        PlateOrderManager plateOrderManager = MainActivity.plateOrderManager;
        orders = plateOrderManager.orders;

        orders_cooking = new ArrayList<PlateVendorService.OrderSingle>();
        for (PlateVendorService.OrderSingle os : orders) {
            if (os.order.status == Constants.ORDER_STATE.ORDER_STATUS_INIT_COOKING.ordinal()) {
                orders_cooking.add(os);
            }
        }
        listViewUpdate();
        gridViewUpdate();
    }

    public void listViewUpdate() {
        listViewCustomAdapter.notifyDataSetChanged();
    }

    public void gridViewUpdate() {
        gridviewCustomAdapter.notifyDataSetChanged();
    }


    //================================================================================
    // Overrides
    //================================================================================
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

    //================================================================================
    // Constructor
    //================================================================================
    public static CookingFragment newInstance() {
        CookingFragment cookingFragment = new CookingFragment();
        return cookingFragment;
    }

    public static CookingFragment newInstance(String param1, String param2) {
        CookingFragment fragment = new CookingFragment();
        return fragment;
    }

    public CookingFragment() {
        // Required empty public constructor
    }

    //================================================================================
    // Tool Function
    //================================================================================
    private void doubleConfirmFinish(final int order_key, Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(getString(R.string.double_confirm_finish_message))
                .setTitle(getString(R.string.double_confirm_finish_title));
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Log.d(Constants.LOG_TAG, "finishing order id >> " + order_key);
                PlateOrderManager plateOrderManager = MainActivity.plateOrderManager;
                plateOrderManager.finish(order_key, getActivity());
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

    private void doubleConfirmCancel(final int order_key, Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(getString(R.string.double_confirm_cancel_message))
                .setTitle(getString(R.string.double_confirm_cancel_title));
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Log.d(Constants.LOG_TAG, "cancel order id >> " + order_key);
                PlateOrderManager plateOrderManager = MainActivity.plateOrderManager;
                plateOrderManager.cancel(order_key, getActivity());
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
}
