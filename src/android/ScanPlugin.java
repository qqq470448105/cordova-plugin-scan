
package com.qilong.cordova.interactionnative;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;


import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.qilong.citylist.CityListActivity;
import com.qilong.erweima.MipcaActivityCapture;
import com.qilong.http.URLManage;
import com.qilong.map.LocationActivity;
import com.qilong.map.NearbyActivity;
import com.qilong.qilongtool.ImageUtils;
import com.qilong.qilongtool.MD5Util;
import com.qilong.qilongtool.WebViewActivity;

import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaInterface;

import org.apache.cordova.QlCordovaApplication;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;


public class InteractionNative extends CordovaPlugin implements BDLocationListener {
  public InteractionNative() {
  }

  SharedPreferences preferences;
  SharedPreferences.Editor editor;
  Context context;
  CallbackContext callback, cbContext;
  int TYPE_CITY = 1, TYPE_EWM = 2, TYPE_ORDER = 3, TYPE_KUICKPAY = 4, TYPE_MAP = 5;
  private Bitmap bitmap;

  //插件初始化方法
  public void initialize(CordovaInterface cordova, CordovaWebView webView) {
    super.initialize(cordova, webView);
    context = QlCordovaApplication.getAppContext();
    preferences = context.getSharedPreferences("qilong_data", 0);
    editor = preferences.edit();
  }

  public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
    callback = callbackContext;
    if ("goToNativeView".equals(action)) {
      JSONObject data = args.getJSONObject(0);
    } else if ("getRootRouting".equals(action)) {
      String path = preferences.getString("rootRoutingPath", null);
      if (path == null || path.equals("")) {
        callbackContext.error("ERR_SENT_FAILED");
      } else {
        callbackContext.success(path);
      }
      editor.putString("rootRoutingPath", "");
      editor.commit();
    } else if ("getAppKey".equals(action)) {
      String appkey = preferences.getString("AppKey", "");
      if (appkey == null || appkey.equals("")) {
        callbackContext.error("ERR_SENT_FAILED");
      } else {
        callbackContext.success(appkey);
      }
    } else if ("setNativeInfo".equals(action)) {
      JSONObject data = args.getJSONObject(0);
      editor.putString(data.getString("key"), data.getString("value"));
      editor.commit();
      callbackContext.success("");
    } else if ("getNativeInfo".equals(action)) {
      JSONObject data = args.getJSONObject(0);
      callbackContext.success(preferences.getString(data.getString("key"), ""));
    } else if ("againGetAppKey".equals(action)) {
      RequestParams params = new RequestParams();
      params.put("client_type", "android");
      URLManage.showGet("key/getkey", params, new JsonHttpResponseHandler() {
        @Override
        public void onSuccess(JSONObject response) {
          super.onSuccess(response);
          try {
            if (response.getInt("code") != 100) {
              editor.putString("AppKey", "");
              editor.commit();
              callbackContext.error("ERR_SENT_FAILED");
              return;
            }

            JSONObject data = response.getJSONObject("data");
            editor.putString("AppKey", data.getString("key"));
            editor.commit();
            callbackContext.success("");
          } catch (Exception e) {
            // TODO: handle exception
          }
        }

        @Override
        public void onFailure(Throwable e, JSONObject errorResponse) {
          super.onFailure(e, errorResponse);
          editor.putString("AppKey", "");
          editor.commit();
          callbackContext.error("ERR_SENT_FAILED");
        }

      });
    } else if ("modalNativeView".equals(action)) {//城市选择
      JSONObject data = args.getJSONObject(0);
      if (data.getString("modalViewName").equals("CityViewController")) {
        Intent intent = new Intent();
        intent.setClass(cordova.getActivity(), CityListActivity.class);
        intent.putExtra("cityname", data.getString("currentCityName"));
        intent.putExtra("cityid", data.getString("currentCityId"));
        this.cordova.startActivityForResult(this, intent, TYPE_CITY);

      }
    } else if ("pushInsetWebView".equals(action)) {
      JSONObject data = args.getJSONObject(0);
      final JSONObject param = data.getJSONObject("params");
      if (data.getString("type").equals("blank")) {
        //纯webview
        Intent intent = new Intent();
        intent.setClass(cordova.getActivity(), WebViewActivity.class);
        intent.putExtra("url", param.getString("url"));
        intent.putExtra("title", param.getString("viewTitle"));
        this.cordova.startActivityForResult(this, intent, 0);
      } else if (data.getString("type").equals("order")) {
        //跳银行页面
        Intent intent = new Intent();
        intent.setClass(cordova.getActivity(), WebViewActivity.class);
        intent.putExtra("url", param.getString("url"));
        intent.putExtra("title", param.getString("viewTitle"));
        this.cordova.startActivityForResult(this, intent, TYPE_ORDER);
      } else if (data.getString("type").equals("quickPay")) {
        //银联快捷支付
        try {
          String token = MD5Util.getMD5String("CARDNUMBER=" + param.getString("cardno") + "&CITY_ID="
            + param.getString("city_id")
            + "&CLIENT_TYPE=android&ORDERID="
            + param.getString("orderId")
            + "&USER_TOKEN="
            + param.getString("user_token")
            + "&USERID="
            + param.getString("userid") + "&" + preferences.getString("AppKey", ""));
          RequestParams params = new RequestParams();
          params.put("api_token", token);
          params.put("city_id", param.getString("city_id"));
          params.put("client_type", "android");
          params.put("user_token", param.getString("user_token"));
          params.put("userid", param.getString("userid"));
          params.put("orderid", param.getString("orderId"));
          params.put("cardNumber", param.getString("cardno"));
          String url = "http://api.qilong.com/pay/toBindUnionpayFastCard?" + params;
          Intent intent = new Intent();
          intent.setClass(cordova.getActivity(), WebViewActivity.class);
          intent.putExtra("url", url);
          intent.putExtra("title", param.getString("viewTitle"));
          this.cordova.startActivityForResult(this, intent, TYPE_KUICKPAY);
        } catch (Exception e) {
        }
      } else if (data.getString("type").equals("bindingNewCard")) {
        //绑卡页面
        Intent intent = new Intent();
        intent.setClass(cordova.getActivity(), WebViewActivity.class);
        intent.putExtra("url", param.getString("url"));
        intent.putExtra("title", "验证支付");
        this.cordova.startActivityForResult(this, intent, TYPE_ORDER);
      } else if (data.getString("type").equals("bindingCard")) {

        try {
          RequestParams params = new RequestParams();
          RequestParams params2 = new RequestParams();
          Iterator iterator = param.keys();
          while (iterator.hasNext()) {
            String key = (String) iterator.next();
            String value = param.getString(key);
            params.put(key.toUpperCase(), value);
            params2.put(key, value);
          }

          String[] strings = params.toString().split("&");
          Arrays.sort(strings, String.CASE_INSENSITIVE_ORDER);
          String str = "";
          for (int i = 0; i < strings.length; i++) {
            str += strings[i].trim() + "&";
          }
          String token = MD5Util.getMD5String(str.trim() + preferences.getString("AppKey", ""));
          params2.put("api_token", token);
          String url = "http://api.qilong.com/card/gorecharge?" + params2;
          Intent intent = new Intent();
          intent.setClass(cordova.getActivity(), WebViewActivity.class);
          intent.putExtra("url", url);
          intent.putExtra("title", "验证支付");
          this.cordova.startActivityForResult(this, intent, TYPE_KUICKPAY);

        } catch (Exception e) {
        }
      }
    } else if ("scan".equals(action)) {
      //二维码
      Intent intent = new Intent();
      intent.setClass(cordova.getActivity(), MipcaActivityCapture.class);
      this.cordova.startActivityForResult(this, intent, TYPE_EWM);
    } else if ("uploadCommentImage".equals(action)) {
      //评论图片
      JSONObject data = args.getJSONObject(0);
      String url = data.getString("url");
      JSONObject param = data.getJSONObject("parameters");

      try {
        RequestParams params = new RequestParams();
        RequestParams params2 = new RequestParams();
        Iterator iterator = param.keys();
        while (iterator.hasNext()) {
          String key = (String) iterator.next();
          String value = param.getString(key);
          params.put(key.toUpperCase(), value);
          params2.put(key, value);
        }

        String[] strings = params.toString().split("&");
        Arrays.sort(strings, String.CASE_INSENSITIVE_ORDER);
        String str = "";
        for (int i = 0; i < strings.length; i++) {
          str += strings[i].trim() + "&";
        }
        String token = MD5Util.getMD5String(str.trim() + preferences.getString("AppKey", ""));
        params2.put("api_token", token);
        JSONArray imgs = data.getJSONArray("imgs");
        try {
          for (int i = 0; i < imgs.length(); i++) {
            bitmap = ImageUtils.compBitmap(MediaStore.Images.Media.getBitmap(context.getContentResolver(), Uri.parse(imgs.getString(i))));
            File dFile = ImageUtils.saveMyBitmap(i + "temp_photo.jpg", bitmap);
            params2.put(i + "temp_photo.jpg", dFile);
          }
        } catch (Exception e) {

        }
        //post请求
        URLManage.showPost(url, params2, new JsonHttpResponseHandler() {
          @Override
          public void onSuccess(JSONObject response) {
            super.onSuccess(response);

            try {
              callbackContext.success(response);
            } catch (Exception e) {
              // TODO: handle exception
            }
          }

          @Override
          public void onFailure(Throwable e, JSONObject errorResponse) {
            super.onFailure(e, errorResponse);
            callbackContext.error("");
          }

        });
      } catch (Exception e) {
        // TODO: handle exception
      }
    } else if ("uploadImage".equals(action)) {
      //图片上传
      JSONObject data = args.getJSONObject(0);
      //将字符串转换成jsonObject对象
      JSONObject userInfo = new JSONObject(preferences.getString("userInfo", ""));
      JSONObject cityInfo = new JSONObject(preferences.getString("cityInfo", ""));
      String token = MD5Util.getMD5String("CITY_ID="
        + cityInfo.getString("areaid") + "&CLIENT_TYPE=android&NO=2&USER_TOKEN=" + userInfo.getString("user_token") + "&USERID=" +
        userInfo.getString("userid") + "&" + preferences.getString("AppKey", ""));
      RequestParams params = new RequestParams();
      params.put("api_token", token);
      params.put("city_id", cityInfo.getString("areaid"));
      params.put("client_type", "android");
      params.put("userid", userInfo.getString("userid"));
      params.put("user_token", userInfo.getString("user_token"));
      params.put("no", "2");
      try {

        bitmap = ImageUtils.compBitmap(MediaStore.Images.Media.getBitmap(context.getContentResolver(), Uri.parse(data.getString("url"))));
        File dFile = ImageUtils.saveMyBitmap("temp_photo.jpg", bitmap);
        params.put("temp_photo.jpg", dFile);
      } catch (Exception e) {

      }
      URLManage.showPost("persondata/saveuserinfo", params, new JsonHttpResponseHandler() {
        @Override
        public void onSuccess(JSONObject response) {
          super.onSuccess(response);
          try {
            callbackContext.success(response);
          } catch (Exception e) {
            // TODO: handle exception
          }
        }

        @Override
        public void onFailure(Throwable e, JSONObject errorResponse) {
          super.onFailure(e, errorResponse);
          callbackContext.error("");
        }

      });
    } else if ("backNativeView".equals(action)) {
      Intent i = new Intent("backNativeView");
      context.sendBroadcast(i);
    } else if ("getLocation".equals(action)) {
      //定位
      cbContext = callbackContext;
      location_client = new LocationClient(QlCordovaApplication.getAppContext());
      LocationClientOption location_option = new LocationClientOption();
      location_option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);// 设置定位模式
      location_option.setCoorType("bd09ll");// 返回的定位结果是百度经纬度，默认值gcj02
      location_option.setScanSpan(60 * 1000);// 设置发起定位请求的间隔时间为5000ms
      location_option.setIsNeedAddress(true);
      location_client.setLocOption(location_option);
      location_client.registerLocationListener(this);
      location_client.requestLocation();
      location_client.start();

    } else if ("pushBDMapView".equals(action)) {
      JSONObject data = args.getJSONObject(0);
      //地图
      if (data.getString("mapType").equals("1")) {
        Intent intent = new Intent();
        intent.setClass(cordova.getActivity(), NearbyActivity.class);
        this.cordova.startActivityForResult(this, intent, TYPE_MAP);
      } else if (data.getString("mapType").equals("2")) {
        JSONObject js = data.getJSONObject("data");
        Intent mIntent = new Intent(cordova.getActivity(), LocationActivity.class);
        mIntent.putExtra("shopid", js.getString("shopid"));
        mIntent.putExtra("lat", data.getString("lat"));
        mIntent.putExtra("lng", data.getString("long"));
        mIntent.putExtra("shopname", js.getString("shopTitle"));
        mIntent.putExtra("address", js.getString("address"));
        mIntent.putExtra("tel", "");
        this.cordova.startActivityForResult(this, mIntent, TYPE_MAP);
      }

    } else if ("postData".equals(action)) {
      JSONObject data = args.getJSONObject(0);
      String url = data.getString("url");
      JSONObject param = data.getJSONObject("param");

      try {
        RequestParams params = new RequestParams();
        RequestParams params2 = new RequestParams();
        Iterator iterator = param.keys();
        while (iterator.hasNext()) {
          String key = (String) iterator.next();
          String value = param.getString(key);
          Log.v("postData","key ="+key+"    value = "+value);
          params.put(key.toUpperCase(), value);
          params2.put(key, value);
        }

        String[] strings = params.toString().split("&");
        Arrays.sort(strings, String.CASE_INSENSITIVE_ORDER);
        String str = "";
        for (int i = 0; i < strings.length; i++) {
          str += strings[i].trim() + "&";
        }
        String token = MD5Util.getMD5String(str.trim() + preferences.getString("AppKey", ""));
        params2.put("api_token", token);
        //post请求
        URLManage.showPost(url, params2, new JsonHttpResponseHandler() {
          @Override
          public void onSuccess(JSONObject response) {
            super.onSuccess(response);

            try {
              callbackContext.success(response);
              Log.v("postData","response = "+response.toString());
            } catch (Exception e) {
              // TODO: handle exception
            }
          }

          @Override
          public void onFailure(Throwable e, JSONObject errorResponse) {
            super.onFailure(e, errorResponse);
            callbackContext.error("");
          }

        });
      } catch (Exception e) {
        // TODO: handle exception
      }

    } else if ("getData".equals(action)) {
      JSONObject data = args.getJSONObject(0);
      String url = data.getString("url");
      JSONObject param = data.getJSONObject("param");

      try {
        RequestParams params = new RequestParams();
        RequestParams params2 = new RequestParams();
        Iterator iterator = param.keys();
        while (iterator.hasNext()) {
          String key = (String) iterator.next();
          String value = param.getString(key);
          Log.v("getData","key ="+key+"    value = "+value);
          params.put(key.toUpperCase(), value);
          params2.put(key, value);
        }
        Log.v("getData","params "+params.toString());
        String[] strings = params.toString().split("&");
        Arrays.sort(strings, String.CASE_INSENSITIVE_ORDER);
        String str = "";
        for (int i = 0; i < strings.length; i++) {
          str += strings[i].trim() + "&";
        }
        Log.v("getData","str ="+str);
        String token = MD5Util.getMD5String(str.trim() + preferences.getString("AppKey", ""));
        params2.put("api_token", token);
        //get请求
        URLManage.showGet(url, params2, new JsonHttpResponseHandler() {
          @Override
          public void onSuccess(JSONObject response) {
            super.onSuccess(response);
            try {
              callbackContext.success(response);
              Log.v("getData","response = "+response.toString());
            } catch (Exception e) {
              // TODO: handle exception
            }
          }

          @Override
          public void onFailure(Throwable e, JSONObject errorResponse) {
            super.onFailure(e, errorResponse);
            callbackContext.error("");
          }

        });
      } catch (Exception e) {
        // TODO: handle exception
      }

    } else {
      return false;
    }
    return true;
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent intent) {
    super.onActivityResult(requestCode, resultCode, intent);
    try {
      if (requestCode == TYPE_CITY) {//城市选择
        String cityid = intent.getStringExtra("cityid");
        String cityname = intent.getStringExtra("cityname");
        String str = "{\"areaid\":\"" + cityid + "\",\"areaname\":\"" + cityname + "\"}";
        callback.success(str);
      } else if (requestCode == TYPE_EWM) {//二维码
        String resultString = intent.getStringExtra("resultString");
        if (resultString.equals("")) {
          callback.error("");
        } else {
          callback.success(resultString);
        }
      } else if (requestCode == TYPE_ORDER) {//银行支付回调
        switch (resultCode) {
          case 0:
            callback.error("取消支付");
            return;
          case 3:
            boolean ret = intent.getBooleanExtra("ret", false);
            if (ret) {
              callback.success(intent.getStringExtra("msg"));
            } else {
              callback.error("");
            }
            return;
        }
      } else if (requestCode == TYPE_KUICKPAY) {//快捷支付回调
        switch (resultCode) {
          case 0:
            callback.error("取消支付");
            return;
          case 3:
            boolean ret = intent.getBooleanExtra("ret", false);
            if (ret) {
              callback.success(intent.getStringExtra("msg"));
            } else {
              callback.error("");
            }
            return;
        }
      }
    } catch (Exception e) {

    }
  }

  //定位
  private LocationClient location_client;

  //  @Override
  public void onReceiveLocation(BDLocation bdLocation) {
    if (null != bdLocation && bdLocation.getLocType() != BDLocation.TypeServerError) {
      if(Double.MIN_VALUE == bdLocation.getLatitude() && Double.MIN_VALUE == bdLocation.getLongitude()){
        cbContext.error("");
      }else{
        String str = "{\"lat\":\"" + bdLocation.getLatitude() + "\",\"long\":\"" + bdLocation.getLongitude() + "\"}";
        cbContext.success(str);
      }
    } else {
      cbContext.error("");
    }
  }
}
