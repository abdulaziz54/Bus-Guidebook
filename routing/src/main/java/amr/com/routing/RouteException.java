package amr.com.routing;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class RouteException extends Exception {
    private static final String TAG = "RouteException";

    private String statusCode;
    private String message;

    public RouteException(JSONObject json){
        if(json == null){
            statusCode = "";
            message = "Parsing error";
            return;
        }
        try {
            String KEY_STATUS = "status";
            statusCode = json.getString(KEY_STATUS);
            String KEY_MESSAGE = "error_message";
            message = json.getString(KEY_MESSAGE);
        } catch (JSONException e) {
            Log.e(TAG, "JSONException while parsing RouteException argument. Msg: " + e.getMessage());
        }
    }

    public RouteException(String msg){
        message = msg;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public String getStatusCode() {
        return statusCode;
    }
}