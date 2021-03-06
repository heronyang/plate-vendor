package tw.plate.vendor;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class FinishFragment extends Fragment {

    private View v;
    private ListViewCustomAdapter listViewCustomAdapter;
    private GridViewCustomAdapter gridviewCustomAdapter;

    List<PlateVendorService.OrderSingle> orders;
    List<PlateVendorService.OrderSingle> orders_finish;

    Tool tool;

    //================================================================================
    // Adapters
    //================================================================================
    private class ListViewCustomAdapter extends BaseAdapter {
        LayoutInflater inflater;

        public class ViewHolder{
            TextView tv_listrow_finish;
        }
        public ListViewCustomAdapter(Context context){
            inflater = LayoutInflater.from(context);

        }
        @Override
        public boolean isEnabled(int position) {
            return false;
        }
        public int getCount(){
            if (orders_finish == null || orders_finish.isEmpty())  return 0;
            return orders_finish.size();
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
                convertview = inflater.inflate(R.layout.listrow_finish, null);

                viewHolder=new ViewHolder();
                viewHolder.tv_listrow_finish = (TextView) convertview.findViewById(R.id.tv_listrow_finish);

                convertview.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.d(Constants.LOG_TAG, "clicked");

                        // pickup order
                        int order_key = orders_finish.get(arg0).order.id;
                        Log.d(Constants.LOG_TAG, "picking up order id >> " + order_key);
                        PlateOrderManager plateOrderManager = MainActivity.plateOrderManager;
                        plateOrderManager.pickup(order_key, getActivity());
                    }
                });

                convertview.setTag(viewHolder);
            }
            else {
                viewHolder=(ViewHolder)convertview.getTag();
            }
            // set values
            String output = "";
            PlateVendorService.OrderV1 o = orders_finish.get(arg0).order;
            output += ("time : " + o.mtime + "\n");
            output += ("ns : " + tool.formattedNS(o.pos_slip_number) + "\n");
            output += ("ph : " + orders_finish.get(arg0).user.username + "\n");

            List<PlateVendorService.OrderItemV1> order_items = orders_finish.get(arg0).order_items;
            int totalPrice = 0;
            for (PlateVendorService.OrderItemV1 oi : order_items) {
                output += oi.meal.meal_name + " * " + oi.amount + "\n";
                totalPrice += oi.meal.meal_price * oi.amount;
            }
            output += "total : " + totalPrice;


            viewHolder.tv_listrow_finish.setText(output);

            return convertview;
        }

    }

    private class GridViewCustomAdapter extends BaseAdapter {
        LayoutInflater inflater;

        public class ViewHolder{
            TextView tv_number_slip_large;
        }
        public GridViewCustomAdapter(Context context){
            inflater = LayoutInflater.from(context);

        }
        @Override
        public boolean isEnabled(int position) {
            return false;
        }
        public int getCount(){
            if (orders_finish == null || orders_finish.isEmpty())  return 0;
            return orders_finish.size();
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
                convertview = inflater.inflate(R.layout.gridview_number_slip_large, null);

                viewHolder=new ViewHolder();
                viewHolder.tv_number_slip_large = (TextView) convertview.findViewById(R.id.tv_number_slip_large);


                convertview.setTag(viewHolder);
            }
            else {
                viewHolder=(ViewHolder)convertview.getTag();
            }

            convertview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(Constants.LOG_TAG, "clicked");

                    // disable the button first
                    removeNumberslipAndFreeze(arg0);

                    //
                    PlateVendorService.OrderSingle orderSingle = orders_finish.get(arg0);
                    int order_key = orders_finish.get(arg0).order.id;
                    Log.d(Constants.LOG_TAG, "picking up order id >> " + order_key + ", arg0 >>" + arg0);
                    doubleConfirmPick(order_key, orderSingle, getActivity());
                }
            });

            // set values
            int ns = orders_finish.get(arg0).order.pos_slip_number;
            viewHolder.tv_number_slip_large.setText(""+ tool.formattedNS(ns));

            return convertview;
        }

    }

    //================================================================================
    // Layout Updates
    //================================================================================
    public void finishListUpdate() {
        // should wait for the data is grabbed
        Log.d(Constants.LOG_TAG, "Finish: finish list update start");
        PlateOrderManager plateOrderManager = MainActivity.plateOrderManager;
        orders = plateOrderManager.orders;

        orders_finish = new ArrayList<PlateVendorService.OrderSingle>();
        for (PlateVendorService.OrderSingle os : orders) {
            if (os.order.status == Constants.ORDER_STATE.ORDER_STATUS_FINISHED.ordinal()) {
                orders_finish.add(os);
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
        // set all enable
        enableListedFinishNumberslipAll();
    }

    //================================================================================
    // Overrides
    //================================================================================
    @Override
    public void onCreate(Bundle savedInstanceState) {
        tool = new Tool();
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_finish, container, false);

        ListView lv = (ListView) v.findViewById(R.id.lv_finish);
        listViewCustomAdapter = new ListViewCustomAdapter(this.getActivity());
        lv.setAdapter(listViewCustomAdapter);

        GridView gv = (GridView) v.findViewById(R.id.gv_finish);
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
    public static FinishFragment newInstance() {
        FinishFragment finishFragment = new FinishFragment();
        return finishFragment;
    }

    public static FinishFragment newInstance(String param1, String param2) {
        FinishFragment fragment = new FinishFragment();
        return fragment;
    }

    public FinishFragment() {
        // Required empty public constructor
    }


    //================================================================================
    // Tool Function
    //================================================================================
    private void doubleConfirmPick(final int order_key, PlateVendorService.OrderSingle orderSingle, Activity activity) {
        String order_content = "";
        order_content += "電話號碼：" + orderSingle.user.username + "\n";

        DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date ctime = orderSingle.order.ctime;
        order_content += "訂餐時間：" + df.format(ctime) + "\n\n";

        int totalPrice = 0;
        for (PlateVendorService.OrderItemV1 oi : orderSingle.order_items) {
            order_content += oi.meal.meal_name + " * " + oi.amount + "\n";
            totalPrice += (oi.meal.meal_price * oi.amount);
        }
        order_content += "\n總計：" + totalPrice + "NTD\n";

        final Dialog dialog = new Dialog(activity);
        dialog.setContentView(R.layout.dialog_custom);
        dialog.setCanceledOnTouchOutside(false);

        String formattedNS = tool.formattedNS(orderSingle.order.pos_slip_number);
        dialog.setTitle("號碼牌" + formattedNS + " : " + getString(R.string.double_confirm_pick_title));

        TextView message = (TextView) dialog.findViewById(R.id.tv_dialog);
        message.setText(getString(R.string.double_confirm_pick_message) + "\n" + order_content);

        Button okButton = (Button) dialog.findViewById(R.id.btn_dialog_continue);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(Constants.LOG_TAG, "cancel order id >> " + order_key);
                PlateOrderManager plateOrderManager = MainActivity.plateOrderManager;
                plateOrderManager.pickup(order_key, getActivity());
                ((MainActivity)getActivity()).timerStart();
                dialog.dismiss();
            }
        });
        Button cancelButton = (Button) dialog.findViewById(R.id.btn_dialog_back);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity)getActivity()).timerStart();
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void removeNumberslipAndFreeze(int index) {
        ((MainActivity)getActivity()).timerStop();
        disableListedFinishNumberslip(index);
    }

    private void disableListedFinishNumberslip(int index) {
        GridView gv = (GridView) v.findViewById(R.id.gv_finish);
        index -= gv.getFirstVisiblePosition();
        if (index < 0 || index >= gv.getChildCount()) {
            Log.d(Constants.LOG_TAG, "Unable to get view for desired position, because it's not being displayed on screen.");
            return;
        }

        View v = gv.getChildAt(index);
        if (v == null) {
            throw new NullPointerException("child not found in grid view");
        }

        v.setEnabled(false);
        v.setClickable(false);
        //v.setVisibility(View.GONE);
        //v.setBackgroundColor(Color.RED);
    }

    private void enableListedFinishNumberslipAll() {
        GridView gv = (GridView) v.findViewById(R.id.gv_finish);
        final int size = gv.getChildCount();
        for (int i=0 ; i<size ; i++){
            View v = gv.getChildAt(i);
            if (v == null) {
                throw new NullPointerException("child not found in grid view");
            }

            v.setEnabled(true);
            v.setClickable(true);
            //v.setVisibility(View.VISIBLE);
            //v.setBackgroundColor(0xFF000000);
        }
    }
}
