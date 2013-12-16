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

    /*
    public class Restaurant {
        public int location;
        public String name;
        public int rest_id;
    }
    */

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
        public String ctime;
        public String mtime;
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
        @POST("/1/pick")
        void pick(@Field("order_key") int order_key,
                    Callback<Response> cb);
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
            if (System.getProperty("java.runtime.name").equals("Android Runtime")) {
                fmt = "yyyy-MM-dd'T'HH:mm:ssZZZZZ";
            } else {
                fmt = "yyyy-MM-dd'T'HH:mm:ssX";
            }
            sdf = new SimpleDateFormat(fmt, Locale.US);
        }

        public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext ctx)
                throws JsonParseException {
            try {
                return sdf.parse(json.getAsJsonPrimitive().getAsString());
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


