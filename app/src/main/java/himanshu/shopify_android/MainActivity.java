package himanshu.shopify_android;

import android.content.Intent;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements FetchServiceResultReceiver.Receiver{


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        FetchServiceResultReceiver fetchServiceCallback = new FetchServiceResultReceiver(new Handler());
        fetchServiceCallback.setReceiver(this);

        Intent intent = new Intent(MainActivity.this, FetchService.class);
        intent.putExtra("URL","https://shopicruit.myshopify.com/admin/custom_collections.json?page=1&access_token=c32313df0d0ef512ca64d5b336a0d7c6");
        intent.putExtra("callback",fetchServiceCallback);

        this.startService(intent);
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        if (resultCode==1){
            String data = resultData.getString("FETCH_DATA");
            Log.d("JSON",data);
            try {
                ArrayList<Collection> collectionsList = new ArrayList<>();
                JSONObject jsonData = new JSONObject(data);
                JSONArray collections = jsonData.getJSONArray("custom_collections");

                for(int i=0;i<collections.length();i++){
                    JSONObject jsonCollection = collections.getJSONObject(i);

                    collectionsList.add(new Collection(jsonCollection.getString("title"),
                            jsonCollection.getString("body_html"),
                            jsonCollection.getJSONObject("image").getString("src"),
                            (Long)jsonCollection.get("id")));



                    Log.d("JSON_DATA",collections.get(i).toString());
                }
                RecyclerView collectionsRecyclerView = (RecyclerView)findViewById(R.id.collections_list);
                collectionsRecyclerView .setLayoutManager(new LinearLayoutManager(this));
                collectionsRecyclerView .setHasFixedSize(true);
                collectionsRecyclerView .setAdapter(new ScrollingAdapter(collectionsList));
            }catch(JSONException jException){
                Log.d("JSON",jException.getMessage());
                //TODO:UPDATE UI ACCORDINGLY
            }
        }
    }
}
