package tw.plate.vendor;

import android.app.Application;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by heron on 1/24/14.
 */
public class PlateVendor extends Application {
    int max_number_slip = 0;

    boolean networkErrorFreezed = false;

    List<ClosedReason> closedReasons;

    public PlateVendor() {
        closedReasons = new ArrayList<ClosedReason>();
    }
}
