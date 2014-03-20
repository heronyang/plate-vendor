package tw.plate.vendor;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.client.Response;
import retrofit.converter.GsonConverter;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Query;

/**
 * Created by heron on 12/16/13.
 */

public class PlateVendorService {

    public class User {
        public int id;
        public String username;
        public String first_name;
        public String last_name;
        public String email;
    }

    public class Meal {
        public int meal_price;
        public String meal_name;
        public int meal_id;
    }

    public class OrderV1 {
        /* FIXME: ctime, mtime should be in Data format */
        public Date ctime;
        public Date mtime;
        public int id;                  // new in vendor API, but not in client app
        public int pos_slip_number;
        public int status;
    }

    public class OrderItemV1 {
        public int amount;
        public Meal meal;
    }

    public class OrderVendorResponse {
        public List<OrderSingle> orders;
    }

    public class OrderSingle {
        public OrderV1 order;
        public List<OrderItemV1> order_items;
        public User user;
    }

    public class VendorListResponse {
        public List<String> vendor_usernames;
    }

    public class RestStatusResponse {
        public int status;
        public boolean is_open;
        public String closed_reason;
    }

    public class ClosedReasonResponse {
        public List<ClosedReason> closed_reasons;
    }

    public class ClosedReason {
        public String msg;
        public int id;
    }

    interface PlateTWAPI1 {
        @FormUrlEncoded
        @POST("/1/login")
        void login(@Field("username") String username,
                   @Field("password") String password,
                   Callback<Response> cb);

        @POST("/1/order_vendor")
        void order_vendor(Callback<OrderVendorResponse> cb);

        @FormUrlEncoded
        @POST("/1/finish")
        void finish(@Field("order_key") int order_key,
                    Callback<Response> cb);

        @FormUrlEncoded
        @POST("/1/pickup")
        void pickup(@Field("order_key") int order_key,
                    Callback<Response> cb);

        @FormUrlEncoded
        @POST("/1/cancel")
        void cancel(@Field("order_key") int order_key,
                    Callback<Response> cb);

        @GET("/1/restaurant_status")
        void get_restaurant_status(Callback<RestStatusResponse> cb);

        @FormUrlEncoded
        @POST("/1/restaurant_status")
        void post_restaurant_status(@Field("status") int status,
                                    Callback<Response> cb);

        @GET("/1/vendor_list")
        void vendor_list(Callback<VendorListResponse> cb);

        @GET("/1/closed_reason")
        void get_closed_reason(Callback<ClosedReasonResponse> cb);

        @FormUrlEncoded
        @POST("/1/closed_reason")
        void post_closed_reason(@Field("closed_reason") int closed_reason_id, Callback<Response> cb);
    }

    private static RestAdapter restAdapterV1;
    private static PlateTWAPI1 plateTWV1;

    private static class DateTimeDeserializer implements JsonDeserializer<Date> {
        private SimpleDateFormat sdf;

        public DateTimeDeserializer() {
			/* http://stackoverflow.com/questions/7910734/gsonbuilder-setdateformat-for-2011-10-26t202959-0700
			 * http://developer.android.com/reference/java/text/SimpleDateFormat.html
			 * http://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html
			 * */
            String fmt;
            //FIXME: server : "yyyy-MM-dd'T'HH:mmZ", local : "yyyy-MM-dd'T'HH:mm:ss.SSSSSSZZZZZ";
            if (System.getProperty("java.runtime.name").equals("Android Runtime")) {
                if (Constants.DEBUG_MODE) {
                    fmt = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSZZZZZ";
                } else {
                    fmt = "yyyy-MM-dd'T'HH:mmZ";
                }
            } else {
                fmt = "yyyy-MM-dd'T'HH:mmZ";
            }
            sdf = new SimpleDateFormat(fmt, Locale.US);
        }

        public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext ctx)
                throws JsonParseException {
            try {
                String s = json.getAsJsonPrimitive().getAsString();
                String dateWithoutMicros = s.substring(0, s.length() - 9) + s.substring(s.length() - 6);
                return sdf.parse(dateWithoutMicros);
            } catch (ParseException e) {
                throw new JsonParseException(e.getMessage());
            }
        }
    }

    public static PlateTWAPI1 getAPI1(String url) {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Date.class, new DateTimeDeserializer())
                .create();
        restAdapterV1 = new RestAdapter.Builder()
                .setServer(url)
                .setConverter(new GsonConverter(gson))
                .build();

        plateTWV1 = restAdapterV1.create(PlateTWAPI1.class);
        return plateTWV1;
    }
}


