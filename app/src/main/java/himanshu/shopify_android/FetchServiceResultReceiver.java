package himanshu.shopify_android;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

/**
 * Created by Himanshu Sharma on 2019-01-17.
 * A simple ResultReceiver implementation, Allows for communication between the FetchService IntentService and Activities
 */

public class FetchServiceResultReceiver extends ResultReceiver {
    private Receiver mReceiver;

    public FetchServiceResultReceiver(Handler handler) {
        super(handler);
    }

    public interface Receiver {
        public void onReceiveResult(int resultCode, Bundle resultData);

    }

    public void setReceiver(Receiver receiver) {
        mReceiver = receiver;
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {

        if (mReceiver != null) {
            mReceiver.onReceiveResult(resultCode, resultData);
        }
    }

}
