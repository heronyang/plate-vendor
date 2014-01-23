package tw.plate.vendor;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.util.Log;

import java.net.ContentHandler;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.util.List;
import java.util.concurrent.locks.LockSupport;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by heron on 12/16/13.
 */
public class PlateOrderManager{

    String password, username;
    Constants.Status status = Constants.Status.RESTAURANT_STATUS_CLOSE; // default

    PlateVendorService.PlateTWAPI1 plateTWV1;
    List<PlateVendorService.OrderSingle> orders;

    Activity mContext;

    // callback
    public interface PlateOrderManagerCallback {
        void orderUpdated();
        void loginCompleted();
        void statusUpdate();
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
                Log.d(Constants.LOG_TAG, "Update: Success");
                callerActivity.orderUpdated();
            }
            @Override
            public void failure(RetrofitError error) {
                // redirect to login page
                Log.d(Constants.LOG_TAG, "Update: Error : " + error.getMessage());
            }
        });
    }

    public boolean isEmpty() {
        return orders.isEmpty();
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
            username = "v1";
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

    //private static PlateOrderManager instance;

    public PlateOrderManager(Activity _mContext) {
        mContext = _mContext;
        accountSetup();
    }


    public void finish(int order_key, final Activity activity) {
        plateTWV1.finish(order_key, new Callback<Response>() {
            @Override
            public void success(Response r, Response response) {
                Log.d(Constants.LOG_TAG, "Finish: Success!");
            }

            @Override
            public void failure(RetrofitError error) {
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
                Log.d(Constants.LOG_TAG, "Finish: Error : " + error.getMessage());
                String title = activity.getString(R.string.popup_network_error_title);
                String message = activity.getString(R.string.popup_network_error_message);
                popupMessage(title, message, activity);
            }
        });
    }

    // ======= UI Stuff =======

    private void popupMessage(String title, String message, Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(message)
                .setTitle(title);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });

        AlertDialog dialog = builder.create();
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
                // select login vendor
                Log.d(Constants.LOG_TAG, "VendorList: Error : " + error.getMessage());
            }
        });
    }

    public void setBusy(final Activity activity) {
        plateTWV1 = PlateVendorService.getAPI1(Constants.API_URI_PREFIX);
        plateTWV1.set_busy(new Callback<Response>() {
            @Override
            public void success(Response response, Response response2) {
                String title = activity.getString(R.string.popup_set_title);
                String message = activity.getString(R.string.popup_set_to_busy_message);
                popupMessage(title, message, activity);
            }

            @Override
            public void failure(RetrofitError error) {
                String title = activity.getString(R.string.popup_network_error_title);
                String message = activity.getString(R.string.popup_network_error_message);
                popupMessage(title, message, activity);
            }
        });

    }

    public void setNotBusy(final Activity activity) {
        plateTWV1 = PlateVendorService.getAPI1(Constants.API_URI_PREFIX);
        plateTWV1.set_not_busy(new Callback<Response>() {
            @Override
            public void success(Response response, Response response2) {
                String title = activity.getString(R.string.popup_set_title);
                String message = activity.getString(R.string.popup_set_to_not_busy_message);
                popupMessage(title, message, activity);
            }

            @Override
            public void failure(RetrofitError error) {
                String title = activity.getString(R.string.popup_network_error_title);
                String message = activity.getString(R.string.popup_network_error_message);
                popupMessage(title, message, activity);
            }
        });

    }

    public void updateRestStatus(final Activity activity) {
        plateTWV1 = PlateVendorService.getAPI1(Constants.API_URI_PREFIX);
        plateTWV1.get_rest_status(new Callback<PlateVendorService.RestStatusResponse>() {
            @Override
            public void success(PlateVendorService.RestStatusResponse restStatusResponse, Response response) {
                Log.d(Constants.LOG_TAG, "status >> " + restStatusResponse.status);
                status = Constants.Status.values()[restStatusResponse.status];
                callerActivity = (PlateOrderManagerCallback)activity;
                callerActivity.statusUpdate();
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d(Constants.LOG_TAG, "status update failed");
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
