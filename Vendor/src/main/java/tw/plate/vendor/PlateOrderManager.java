package tw.plate.vendor;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by heron on 12/16/13.
 */
public class PlateOrderManager{

    String password, username;

    //
    Constants.Status status = Constants.Status.RESTAURANT_STATUS_FOLLOW_OPEN_RULES; // default
    boolean is_open = false;
    String closed_reason = "";

    //
    PlateVendorService.PlateTWAPI1 plateTWV1;
    List<PlateVendorService.OrderSingle> orders;

    Activity mContext;

    // callback
    public interface PlateOrderManagerCallback {
        void orderUpdated();
        void loginCompleted();
        void statusUpdate();
        void statusPostCompleted(boolean selectClosedReason);

        void closedReasonPostSucceed();
        void closedReasonPostFailed();

        void networkError();
    }
    private PlateOrderManagerCallback callerActivity;

    public void update(Activity activity) {
        callerActivity = (PlateOrderManagerCallback)activity;
        //
        plateTWV1 = PlateVendorService.getAPI1(Constants.API_URI_PREFIX);
        plateTWV1.order_vendor(new Callback<PlateVendorService.OrderVendorResponse>() {
            @Override
            public void success(PlateVendorService.OrderVendorResponse r, Response response) {
                orders = r.orders;
                Log.i(Constants.LOG_TAG, "Update: Success");
                callerActivity.orderUpdated();
            }
            @Override
            public void failure(RetrofitError error) {
                if (error.isNetworkError()) callerActivity.networkError();
                // redirect to login page
                Log.d(Constants.LOG_TAG, "Update: Error : " + error.getMessage());
            }
        });
    }

    public void login(Activity activity) {
        callerActivity = (PlateOrderManagerCallback)activity;
        PlateVendorService.PlateTWAPI1 plateTWV1;
        plateTWV1 = PlateVendorService.getAPI1(Constants.API_URI_PREFIX);

        plateTWV1.login(username, password, new Callback<Response>() {
            @Override public void success(Response r, Response response) {
                Log.d(Constants.LOG_TAG, "Login: Success!");
                callerActivity.loginCompleted();
            }
            @Override public void failure(RetrofitError error) {
                if (error.isNetworkError()) callerActivity.networkError();
                // select login vendor
                Log.d(Constants.LOG_TAG, "Login: Error : " + error.getMessage());
            }
        });
    }

    public void accountSetup() {
        SharedPreferences sp = mContext.getSharedPreferences(Constants.SP_ACCOUNT_FILENAME, 0);
        // check if the username/password is stored
        if (!accountInSharedPreferences()){
            // get username/password
            // NOTE: should popup a selection UI
            username = Constants.USERNAME;
            password = Constants.VENDOR_PASSWORD;

            // save back
            SharedPreferences.Editor ed = sp.edit();
            ed.clear();
            ed.putString(Constants.SP_TAG_USERNAME, username).commit();
            ed.putString(Constants.SP_TAG_PASSWORD, Constants.VENDOR_PASSWORD).commit();
        } else {
            username = sp.getString(Constants.SP_TAG_USERNAME, null);
            password = sp.getString(Constants.SP_TAG_PASSWORD, null);
        }
    }

    private boolean accountInSharedPreferences() {
        SharedPreferences sp = mContext.getSharedPreferences(Constants.SP_ACCOUNT_FILENAME, 0);
        return (sp.contains(Constants.SP_TAG_PASSWORD) && sp.contains((Constants.SP_TAG_USERNAME)));
    }

    public PlateOrderManager(Activity _mContext) {
        mContext = _mContext;
        accountSetup();
        username = Constants.USERNAME;
    }


    /* POST ORDER STATUS */
    public void finish(int order_key, final Activity activity) {
        plateTWV1.finish(order_key, new Callback<Response>() {
            @Override
            public void success(Response r, Response response) {
                Log.d(Constants.LOG_TAG, "Finish: Success!");
            }

            @Override
            public void failure(RetrofitError error) {
                if (error.isNetworkError()) callerActivity.networkError();
                Log.d(Constants.LOG_TAG, "Finish: Error : " + error.getMessage());
                String title = activity.getString(R.string.popup_network_error_title);
                String message = activity.getString(R.string.popup_network_error_message);
                popupMessage(title, message, activity);
            }
        });
    }

    public void cancel(int order_key, final Activity activity) {
        plateTWV1.cancel(order_key, new Callback<Response>() {
            @Override
            public void success(Response r, Response response) {
                Log.d(Constants.LOG_TAG, "Cancel: Success!");
            }

            @Override
            public void failure(RetrofitError error) {
                if (error.isNetworkError()) callerActivity.networkError();
                Log.d(Constants.LOG_TAG, "Cancel: Error : " + error.getMessage());
                String title = activity.getString(R.string.popup_network_error_title);
                String message = activity.getString(R.string.popup_network_error_message);
                popupMessage(title, message, activity);
            }
        });
    }

    public void pickup(int order_key, final Activity activity) {
        plateTWV1.pickup(order_key, new Callback<Response>() {
            @Override
            public void success(Response r, Response response) {
                Log.d(Constants.LOG_TAG, "Finish: Success!");
            }

            @Override
            public void failure(RetrofitError error) {
                if (error.isNetworkError()) callerActivity.networkError();
                Log.d(Constants.LOG_TAG, "Finish: Error : " + error.getMessage());
                String title = activity.getString(R.string.popup_network_error_title);
                String message = activity.getString(R.string.popup_network_error_message);
                popupMessage(title, message, activity);
            }
        });
    }

    /* UI Stuff */
    private void popupMessage(String title, String message, Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(message)
                .setTitle(title);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    public void switchUser(final Activity activity) {
        plateTWV1 = PlateVendorService.getAPI1(Constants.API_URI_PREFIX);
        plateTWV1.vendor_list(new Callback<PlateVendorService.VendorListResponse>() {
            @Override
            public void success(PlateVendorService.VendorListResponse r, Response response) {
                Log.d(Constants.LOG_TAG, "VendorList: Success!");
                List<String> vendorList = r.vendor_usernames;
                vendorListPopup(vendorList, activity);
            }

            @Override
            public void failure(RetrofitError error) {
                if (error.isNetworkError()) callerActivity.networkError();
                // select login vendor
                Log.d(Constants.LOG_TAG, "VendorList: Error : " + error.getMessage());
            }
        });
    }

    /* Vendor Status */
    public void post_restaurant_status(final int status, final Activity activity) {
        callerActivity = (PlateOrderManagerCallback)activity;

        plateTWV1 = PlateVendorService.getAPI1(Constants.API_URI_PREFIX);
        plateTWV1.post_restaurant_status(status, new Callback<Response>() {
            @Override
            public void success(Response r, Response response) {
                Log.d(Constants.LOG_TAG, "Post Status: Success!");
                boolean selectCosedReason = status == Constants.Status.RESTAURANT_STATUS_MANUAL_CLOSE.ordinal();
                callerActivity.statusPostCompleted(selectCosedReason);
            }

            @Override
            public void failure(RetrofitError error) {
                if (error.isNetworkError()) callerActivity.networkError();
                Log.d(Constants.LOG_TAG, "Post Status: Error : " + error.getMessage());
            }
        });
    }

    public void updateRestStatus(final Activity activity) {
        plateTWV1 = PlateVendorService.getAPI1(Constants.API_URI_PREFIX);
        plateTWV1.get_restaurant_status(new Callback<PlateVendorService.RestStatusResponse>() {
            @Override
            public void success(PlateVendorService.RestStatusResponse restStatusResponse, Response response) {
                Log.i(Constants.LOG_TAG, "status >> " + restStatusResponse.status);

                status = Constants.Status.values()[restStatusResponse.status];
                is_open = restStatusResponse.is_open;
                closed_reason = restStatusResponse.closed_reason;

                callerActivity = (PlateOrderManagerCallback)activity;
                callerActivity.statusUpdate();
            }

            @Override
            public void failure(RetrofitError error) {
                if (error.isNetworkError()) callerActivity.networkError();
                Log.d(Constants.LOG_TAG, "status update failed");
            }
        });
    }

    /* Closed Reasons */
    public void getClosedReason(final Activity activity) {
        plateTWV1 = PlateVendorService.getAPI1(Constants.API_URI_PREFIX);
        plateTWV1.get_closed_reason(new Callback<PlateVendorService.ClosedReasonResponse>() {
            @Override
            public void success(PlateVendorService.ClosedReasonResponse closedReasonResponse, Response response) {
                List<ClosedReason> closedReasonsApp = ((PlateVendor)activity.getApplication()).closedReasons;
                closedReasonsApp.clear();
                for (PlateVendorService.ClosedReason closedReasonGet : closedReasonResponse.closed_reasons) {
                    ClosedReason cr = new ClosedReason(closedReasonGet.msg, closedReasonGet.id);
                    closedReasonsApp.add(cr);
                    Log.i(Constants.LOG_TAG, cr.msg);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                if (error.isNetworkError()) callerActivity.networkError();
                Log.i(Constants.LOG_TAG, "get closed reason failed");
            }
        });
    }

    public void postClosedReason(int closed_reason_id, final Activity activity) {
        callerActivity = (PlateOrderManagerCallback)activity;
        plateTWV1 = PlateVendorService.getAPI1(Constants.API_URI_PREFIX);
        plateTWV1.post_closed_reason(closed_reason_id, new Callback<Response>() {
            @Override
            public void success(Response response, Response response2) {
                callerActivity.closedReasonPostSucceed();
            }

            @Override
            public void failure(RetrofitError error) {
                if (error.isNetworkError()) callerActivity.networkError();
                Log.d(Constants.LOG_TAG, "post closed reason failed" + error.getMessage());
                callerActivity.closedReasonPostFailed();
            }
        });

    }

    private void vendorListPopup(List<String> _vendorList, final Activity activity) {

        final String vendorList[] = new String[_vendorList.size()];
        _vendorList.toArray(vendorList);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(activity.getString(R.string.select_vendor_username));
        builder.setItems(vendorList, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String selectedUsername = vendorList[which];
                Log.d(Constants.LOG_TAG, "selected >>" + selectedUsername);
                ((PlateVendor)activity.getApplication()).max_number_slip = 0;
                loginUsingUsername(selectedUsername, activity);
            }
        });
        builder.show();
    }

    public void loginUsingUsername(String _username, Activity activity) {
        username = _username;
        login(activity);
    }
}
