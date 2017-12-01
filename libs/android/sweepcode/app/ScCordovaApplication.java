package cordova.plugin.scan.app;

import android.app.Application;
import android.content.Context;

import org.apache.cordova.CordovaWebView;

/**
 * Administrator
 * Created by tc on 2017/11/30 0030.
 * 类名(ClassName)：
 * 作用(Effect)：
 * 备注(Remarks)：
 */

public class ScCordovaApplication extends Application {
  private static Context context;
  protected static CordovaWebView appView;

  @Override
  public void onCreate() {
    super.onCreate();
    ScCordovaApplication.context = getApplicationContext();
    ScCordovaApplication.appView = getAppView();
  }

  @Override
  public void onTerminate() {
    super.onTerminate();
  }

  public static Context getAppContext() {
    return ScCordovaApplication.context;
  }

  public static CordovaWebView getAppView() {
    return ScCordovaApplication.appView;
  }
}
