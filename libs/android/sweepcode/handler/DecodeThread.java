package cordova.plugin.scan.erweima.sweepcode.handler;

import android.os.Handler;
import android.os.Looper;
import java.util.concurrent.CountDownLatch;
import cordova.plugin.scan.erweima.sweepcode.activity.ActivityScanerCode;
/**
 *
 * 描述: 解码线程
 */
final class DecodeThread extends Thread {

    private final CountDownLatch handlerInitLatch;
    ActivityScanerCode activity;
    private Handler handler;

	DecodeThread(ActivityScanerCode activity) {
		this.activity = activity;
		handlerInitLatch = new CountDownLatch(1);
	}


  Handler getHandler() {
		try {
			handlerInitLatch.await();
		} catch (InterruptedException ie) {
			// continue?
		}
		return handler;
	}

	@Override
	public void run() {
		Looper.prepare();
		handler = new DecodeHandler(activity);
		handlerInitLatch.countDown();
		Looper.loop();
	}

}
