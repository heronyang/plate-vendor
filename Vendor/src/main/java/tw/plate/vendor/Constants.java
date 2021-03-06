package tw.plate.vendor;

import android.support.v7.appcompat.*;
import android.support.v7.appcompat.R;

/**
 * Created by heron on 12/14/13.
 */
final public class Constants {

    //public static final boolean DEBUG_MODE = false;
    public static final boolean DEBUG_MODE = true;

    // SERVER URI
    //public static final String API_URI_PREFIX = "http://10.0.2.2:8080"; // Developing Mode (Emulator)
    //public static final String API_URI_PREFIX = "http://192.168.0.121:8080"; // Heron's Local Developing Mode
    public static final String API_URI_PREFIX = "http://140.113.68.101:8080"; // Aje's Local Developing Mode
    //public static final String API_URI_PREFIX = "http://192.168.0.192:8080"; // Heron's Local Developing Mode
    //public static final String API_URI_PREFIX = "http://106.187.89.91:8080"; // Heron's Local Developing Mode
    //public static final String API_URI_PREFIX = "http://api-dev.plate.tw:8080"; // Heron's Local Developing Mode
    public static final String API_URI_PREFIX = DEBUG_MODE?"http://192.168.0.121:8080":"https://api.plate.tw"; // Release Mode
    //
    public static final String LOG_TAG = "PlateVendorLog";

    // account
    public static final String SP_ACCOUNT_FILENAME = "account";
    public static final String SP_TAG_PASSWORD = "VENDOR_PASSWORD";
    public static final String SP_TAG_USERNAME = "VENDOR_USERNAME";

    // password for vendor
    public static final String USERNAME = "v1";
    public static final String VENDOR_PASSWORD = "platerocks";
    public static final String SWITCH_USER_PASSWORD = "etalp";


    // states
    public static enum ORDER_STATE {
        ORDER_STATUS_INIT_COOKING,
        ORDER_STATUS_FINISHED,
        ORDER_STATUS_PICKED_UP,
        ORDER_STATUS_REJECTED,
        ORDER_STATUS_ABANDONED,
        ORDER_STATUS_RESCUED
    }

    // status for rest
    public static enum Status {
        RESTAURANT_STATUS_FOLLOW_OPEN_RULES,
        RESTAURANT_STATUS_MANUAL_OPEN,
        RESTAURANT_STATUS_MANUAL_CLOSE,
        RESTAURANT_STATUS_UNLISTED
    }


    public static int   REFRESH_INT = 1000;
}
