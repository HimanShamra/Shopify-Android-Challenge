package himanshu.shopify_android;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * helper methods.
 */
public class FetchService extends IntentService {

    public FetchService() {
        super("FetchService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            
            String url = intent.getStringExtra(getResources().getString(R.string.url));
            OkHttpClient httpClient = new OkHttpClient();
            Request fetchRequest = new Request.Builder().url(url).build();
            
            try {
                Response response = httpClient.newCall(fetchRequest).execute();
                ResultReceiver receiver = intent.getParcelableExtra(getResources().getString(R.string.callback));

                String data = response.body().string();
                Bundle bundle = new Bundle();
                bundle.putString(getResources().getString(R.string.fetchedData),data);
                receiver.send(intent.getIntExtra(getResources().getString(R.string.callNum),1),bundle);
                Log.d("FetchService",data);
                
            }catch(IOException|NullPointerException IOE){
                Log.d("FetchService","Failed Request\n" + IOE.getMessage());
            }
        }
    }
}
