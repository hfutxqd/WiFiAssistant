package xyz.imxqd.zuiwifi;

import android.text.TextUtils;
import android.util.Base64;

import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by imxqd on 2016/7/5.
 * 用于解析结果
 */
public class ResultParser {
    public static final int WIFICIPHER_NOPASS = 1;
    public static final int WIFICIPHER_WEP = 2;
    public static final int WIFICIPHER_WPA = 3;

    String ssid, password;
    int type = WIFICIPHER_NOPASS;

    public ResultParser(IntentResult result) {
        if(result.getContents().startsWith("WIFI:")) {
            String str = result.getContents().replace("WIFI:", "");
            String[] tmp = str.split(";");
            for(String i : tmp) {
                if (i.startsWith("T:")) {
                    String t = i.replace("T:", "");
                    if(t.equals("WPA")) {
                        type = WIFICIPHER_WPA;
                    } else if (t.equals("WEP")) {
                        type = WIFICIPHER_WEP;
                    } else if (t.equals("nopass")) {
                        type = WIFICIPHER_NOPASS;
                    }
                } else if(i.startsWith("S:")) {
                    ssid = i.replace("S:", "");
                    if(ssid.startsWith("\"") && ssid.endsWith("\"")) {
                        ssid = ssid.substring(1, ssid.length() - 1);
                    }
                } else if(i.startsWith("P:")) {
                    password = i.replace("P:", "");
                    if(password.startsWith("\"") && password.endsWith("\"")) {
                        password = password.substring(1, password.length() -1);
                    }
                }
            }
        } else {
            try {
                JSONObject object = new JSONObject(result.getContents());
                ssid = object.getString("name").replace("\"", "");
                int t = object.getInt("type");
                if( t == 2) {
                    type = WIFICIPHER_WPA;
                } else if ( t == 3) {
                    type = WIFICIPHER_WEP;
                } else {
                    type = WIFICIPHER_NOPASS;
                }
                password = object.getString("password");
                if(password != null) {
                    password = new String(Base64.decode(password, Base64.DEFAULT))
                            .replace("\"", "");
                    if(TextUtils.isEmpty(password)) {
                        type = WIFICIPHER_NOPASS;
                    }
                } else {
                    type = WIFICIPHER_NOPASS;
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public String getSsid() {
        return ssid;
    }

    public int getType() {
        return type;
    }

    public String getTypeString() {
        switch (type) {
            case WIFICIPHER_NOPASS:
                return "开放";
            case WIFICIPHER_WEP:
                return "WEP";
            case WIFICIPHER_WPA:
                return "WPA/WPA2";
            default:
                return "WPA/WPA2";
        }
    }

    public String getPassword() {
        return password;
    }
}
