package tw.plate.vendor;

import android.app.Application;

import java.util.ArrayList;
import java.util.List;
import org.acra.*;
import org.acra.annotation.*;

/**
 * Created by heron on 1/24/14.
 */

@ReportsCrashes(
        formKey = "",
        formUri = "http://dev.plate.tw:5984/acra-plate-vendor/_design/acra-storage/_update/report",
        reportType = org.acra.sender.HttpSender.Type.JSON,
        httpMethod = org.acra.sender.HttpSender.Method.PUT,
        formUriBasicAuthLogin="plate-android",
        formUriBasicAuthPassword="1234",
        mode = ReportingInteractionMode.TOAST,
        forceCloseDialogAfterToast = false, // optional, default false
        resToastText = R.string.crash_toast_text
     )
public class PlateVendor extends Application {
    int max_number_slip = 0;

    boolean networkErrorFreezed = false;

    List<ClosedReason> closedReasons;

    public PlateVendor() {
        closedReasons = new ArrayList<ClosedReason>();
    }

    @Override
    public void onCreate() {
        // The following line triggers the initialization of ACRA
        super.onCreate();
        ACRA.init(this);
    }
}
