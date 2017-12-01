
package cordova.plugin.scan.ScanPlugin;


import android.content.Context;
import android.content.Intent;

import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaInterface;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;

import cordova.plugin.scan.erweima.sweepcode.activity.ActivityScanerCode;


public class ScanPlugin extends CordovaPlugin  {
    public ScanPlugin() {
    }

    CallbackContext callback, cbContext;
    int TYPE_CITY = 1, TYPE_EWM = 2;
    //插件初始化方法
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
    }

    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
        callback = callbackContext;
        if ("scan".equals(action)) {
            Intent intent = new Intent();
            intent.setClass(cordova.getActivity(), ActivityScanerCode.class);
            this.cordova.startActivityForResult(this, intent, TYPE_EWM);
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        try {
             if (requestCode == TYPE_EWM) {
                String resultString = intent.getStringExtra("resultString");
                if (resultString.equals("")) {
                    callback.error("");
                } else {
                    callback.success(resultString);
                }
            }
        } catch (Exception e) {
        }
    }
}
