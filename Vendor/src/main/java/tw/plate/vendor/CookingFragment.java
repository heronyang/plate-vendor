package tw.plate.vendor;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
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
import java.util.concurrent.CompletionService;

public class CookingFragment extends Fragment {

    private View v;
    private ListViewCustomAdapter listViewCustomAdapter;
    private GridViewCustomAdapter gridviewCustomAdapter;

    List<PlateVendorService.OrderSingle> orders;
    List<PlateVendorService.OrderSingle> orders_cooking;

    Tool tool;

    boolean inFreezeMode = false;

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
            TextView tv_datetime;

            Button cancel_order_button;
            Button finish_order_button;
        }
        public ListViewCustomAdapter(Context context){
            inflater = LayoutInflater.from(context);

        }

        @Override
        public boolean areAllItemsEnabled () {
            return false;
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
                viewHolder.tv_datetime = (TextView) convertview.findViewById(R.id.tv_datetime);

                viewHolder.cancel_order_button = (Button) convertview.findViewById(R.id.cancel_order_button);
                viewHolder.finish_order_button = (Button) convertview.findViewById(R.id.finish_order_button);

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
            Button bt_cancel = (Button)convertview.findViewById(R.id.cancel_order_button);
            Button bt_finish = (Button)convertview.findViewById(R.id.finish_order_button);

            bt_cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // disable this listrow
                    removeListRowAndFreeze(arg0);
                    // finishOrder
                    int order_key = orders_cooking.get(arg0).order.id;
                    doubleConfirmCancel(order_key, getActivity());
                }
            });

            bt_finish.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // disable this listrow
                    removeListRowAndFreeze(arg0);
                    // finishOrder
                    int order_key = orders_cooking.get(arg0).order.id;
                    doubleConfirmFinish(order_key, getActivity());
                }
            });


            List<PlateVendorService.OrderItemV1> order_items = orders_cooking.get(arg0).order_items;
            int totalPrice = 0;
            for (PlateVendorService.OrderItemV1 oi : order_items) {
                output += oi.meal.meal_name + " * " + oi.amount + "\n";
                totalPrice += oi.meal.meal_price * oi.amount;
            }
            //output += "total : " + totalPrice;

            viewHolder.tv_totalPrice.setText(totalPrice+" 元");
            viewHolder.tv_slip_number.setText(tool.formattedNS(o.pos_slip_number)+"");
            viewHolder.tv_listrow_cooking.setText(output);


            DateFormat df = new SimpleDateFormat("HH:mm:ss");
            Date ctime = orders_cooking.get(arg0).order.ctime;
            SpannableString spanStringDatetime = new SpannableString(df.format(ctime));
            /*spanString.setSpan(new UnderlineSpan(), 0, spanString.length(), 0);
            spanString.setSpan(new StyleSpan(Typeface.BOLD), 0, spanString.length(), 0);*/
            spanStringDatetime.setSpan(new StyleSpan(Typeface.ITALIC), 0, spanStringDatetime.length(), 0);
            spanStringDatetime.setSpan(new UnderlineSpan(), 0, spanStringDatetime.length(), 0);
            viewHolder.tv_datetime.setText(spanStringDatetime);

            String phoneNumber = orders_cooking.get(arg0).user.username;
            SpannableString spanStringPhone = new SpannableString(formatPhoneNumber(phoneNumber));
            spanStringPhone.setSpan(new StyleSpan(Typeface.ITALIC), 0, spanStringPhone.length(), 0);
            viewHolder.tv_phone.setText(spanStringPhone);

            /*
            viewHolder.cancel_order_button.setEnabled(false);
            viewHolder.finish_order_button.setEnabled(false);
            */

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
            int ns = orders_cooking.get(arg0).order.pos_slip_number;
            viewHolder.tv_number_slip.setText("" + tool.formattedNS(ns));

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
        if (!inFreezeMode) {
            listViewUpdate();
            gridViewUpdate();
        }
    }

    public void listViewUpdate() {
        listViewCustomAdapter.notifyDataSetChanged();
    }

    public void gridViewUpdate() {
        gridviewCustomAdapter.notifyDataSetChanged();
        enableOrderListRowAll();
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
        final Dialog dialog = new Dialog(activity);
        dialog.setContentView(R.layout.dialog_custom);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setTitle(getString(R.string.double_confirm_finish_title));

        TextView message = (TextView) dialog.findViewById(R.id.tv_dialog);
        message.setText(getString(R.string.double_confirm_finish_message));

        Button okButton = (Button) dialog.findViewById(R.id.btn_dialog_continue);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(Constants.LOG_TAG, "finishing order id >> " + order_key);
                PlateOrderManager plateOrderManager = MainActivity.plateOrderManager;
                plateOrderManager.finish(order_key, getActivity());
                ((MainActivity)getActivity()).timerStart();
                inFreezeMode = false;
                dialog.dismiss();
            }
        });
        Button cancelButton = (Button) dialog.findViewById(R.id.btn_dialog_back);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity)getActivity()).timerStart();
                inFreezeMode = false;
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void doubleConfirmCancel(final int order_key, Activity activity) {
        final Dialog dialog = new Dialog(activity);
        dialog.setContentView(R.layout.dialog_custom);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setTitle(getString(R.string.double_confirm_cancel_title));

        TextView message = (TextView) dialog.findViewById(R.id.tv_dialog);
        message.setText(getString(R.string.double_confirm_cancel_message));

        Button okButton = (Button) dialog.findViewById(R.id.btn_dialog_continue);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(Constants.LOG_TAG, "cancel order id >> " + order_key);
                PlateOrderManager plateOrderManager = MainActivity.plateOrderManager;
                plateOrderManager.cancel(order_key, getActivity());
                ((MainActivity)getActivity()).timerStart();
                inFreezeMode = false;
                dialog.dismiss();
            }
        });
        Button cancelButton = (Button) dialog.findViewById(R.id.btn_dialog_back);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity)getActivity()).timerStart();
                inFreezeMode = false;
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    /* Tool for UI */
    private void removeListRowAndFreeze(int index) {
        inFreezeMode = true;
        ((MainActivity)getActivity()).timerStop();
        disableOrderListRow(index);
    }

    private void disableOrderListRow(int index) {
        //View listRow = listViewCustomAdapter.getView(index, null, null);
        Log.d(Constants.LOG_TAG, "index >> " + index);

        ListView lv = (ListView) v.findViewById(R.id.lv_cooking);
        index -= lv.getFirstVisiblePosition();
        if (index < 0 || index >= lv.getChildCount()) {
            Log.d(Constants.LOG_TAG, "Unable to get view for desired position, because it's not being displayed on screen.");
            return;
        }
        View listRow = lv.getChildAt(index);
        if (listRow == null) {
            throw new NullPointerException("child not found in listview");
        }
        listRow.setEnabled(false);  // FIXME: not working for customAdapter, use visible = GONE instead
        listRow.setVisibility(View.GONE);
    }

    private void enableOrderListRowAll(){
        ListView lv = (ListView) v.findViewById(R.id.lv_cooking);
        final int size = lv.getChildCount();
        for (int i=0 ; i<size ; i++){
            View listRow = lv.getChildAt(i);
            if (listRow == null) {
                throw new NullPointerException("child not found in list view");
            }
            listRow.setEnabled(true);
            listRow.setVisibility(View.VISIBLE);

            /*
            View v = listViewCustomAdapter.getView(i, null, null);
            //View v = lv.getChildAt(i);
            if (v == null) {
                throw new NullPointerException("child not found in list view");
            }
            ListViewCustomAdapter.ViewHolder viewHolder=(ListViewCustomAdapter.ViewHolder)v.getTag();
            viewHolder.cancel_order_button.setEnabled(false);
            viewHolder.finish_order_button.setEnabled(false);

            //v.setClickable(false);
            if (v instanceof ViewGroup){
                ViewGroup group = (ViewGroup)v;
                for ( int idx = 0 ; idx < group.getChildCount() ; idx++ ) {
                    View child = group.getChildAt(idx);
                    if (child == null) {
                        throw new NullPointerException("child not found in group view");
                    }
                    child.setEnabled(false);
                    child.setClickable(false);
                }
                Log.d(Constants.LOG_TAG, "is viewGroup");
            }
            */
        }
    }

    /* Formatting */
    private String formatPhoneNumber(String pn) {
        if (pn.length() != 10)  return pn;

        String formattedPn = pn.substring(0, 4) + "-" + pn.substring(4, 7) + "-" + pn.substring(7, 10);

        return formattedPn;
    }
}

