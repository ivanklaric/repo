package com.protohackers.prime;

import org.json.JSONException;
import org.json.JSONObject;

public class RequestResponseObjectManipulation {
    public static boolean isValidRequest(String jsonString) {
        try {
            var jsonObj = new JSONObject(jsonString);
            if (!jsonObj.has("method")) {
                return false;
            }
            if (!jsonObj.getString("method").equals("isPrime")) {
                return false;
            }
            if (!jsonObj.has("number")) {
                return false;
            }
            if (jsonObj.get("number") instanceof String) {
                return false;
            }
            jsonObj.getNumber("number");
        } catch (JSONException jsonExc) {
            // invalid JSON or getNumber didn't return a number
            return false;
        }
        return true;
    }

    public static Number getNumberFromRequest(String jsonString) {
        if (!isValidRequest(jsonString)) {
            return null;
        }
        return new JSONObject(jsonString).getNumber("number");
    }

    public static String createMalformedResponse() {
        return "malformed.\n";
    }

    public static String createResponse(boolean isPrime) {
        return new JSONObject().put("method", "isPrime").put("prime", isPrime).toString()+"\n";
    }
}
