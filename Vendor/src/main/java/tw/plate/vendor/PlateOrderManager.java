package tw.plate.vendor;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.util.Log;

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

    PlateVendorService.PlateTWAPI1 plateTWV1;
    List<PlateVendorService.OrderSingle> orders;

    Activity mContext;

    // callback
    public interface PlateOrderManagerCallback {
        void orderUpdated();
        void loginCompleted();
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

    public void pick(int order_key, final Activity activity) {
        plateTWV1.pick(order_key, new Callback<Response>() {
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
}
