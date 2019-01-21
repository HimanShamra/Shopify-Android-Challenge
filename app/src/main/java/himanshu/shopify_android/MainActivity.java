package himanshu.shopify_android;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements FetchServiceResultReceiver.Receiver{


    private ArrayList<Collection> collectionsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FetchServiceResultReceiver fetchServiceCallback = new FetchServiceResultReceiver(new Handler());
        fetchServiceCallback.setReceiver(this);

        Intent intent = new Intent(MainActivity.this, FetchService.class);
        intent.putExtra("URL",getResources().getString(R.string.collections_url));
        intent.putExtra("callback",fetchServiceCallback);

        this.startService(intent);
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        if (resultCode==1){
            String data = resultData.getString("FETCH_DATA");
            Log.d("JSON",data);
            try {
                collectionsList = new ArrayList<>();
                JSONObject jsonData = new JSONObject(data);
                JSONArray collections = jsonData.getJSONArray("custom_collections");

                for(int i=0;i<collections.length();i++){
                    JSONObject jsonCollection = collections.getJSONObject(i);

                    collectionsList.add(new Collection(jsonCollection.getString("title"),
                            jsonCollection.getString("body_html"),
                            jsonCollection.getJSONObject("image").getString("src"),
                            (Long)jsonCollection.get("id")));

                }

                ListView collectionsListView = findViewById(R.id.collectionsListView);
                collectionsListView.setAdapter(new CollectionsListAdapter(collectionsList));
                collectionsListView.setOnItemClickListener(listClickListener);

            }catch(JSONException jException){
                Log.d("JSON",jException.getMessage());
                //TODO:UPDATE UI ACCORDINGLY
            }
        }
    }

    private AdapterView.OnItemClickListener listClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int index, long id) {
            Intent collectionDetailsIntent = new Intent(MainActivity.this,CollectionsDetailsActivity.class);
            collectionDetailsIntent.putExtra("COLLECTION",collectionsList.get(index));
            startActivity(collectionDetailsIntent);
        }
    };

    public class CollectionsListAdapter extends BaseAdapter {
        ArrayList<Collection> collections;
        public CollectionsListAdapter(ArrayList<Collection> collections){
            this.collections =collections;
        }

        @Override
        public int getCount() {
            return collections.size();
        }

        @Override
        public Object getItem(int index) {
            return collections.get(index);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        /**
         * Uses a ViewHolder for smooth scrolling by decreasing the amount of times findViewById() is called
         */
        @Override
        public View getView(int index, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;

            if(view==null){
                view=getLayoutInflater().inflate(R.layout.list_element,null);
                viewHolder=new ViewHolder();
                viewHolder.textViewKey=view.findViewById(R.id.product_name);
                view.setTag(viewHolder);
            }else{
                viewHolder=(ViewHolder)view.getTag();
            }
            viewHolder.textViewKey.setText(collections.get(index).getName());
            return view;
        }

        class ViewHolder {
            TextView textViewKey;
        }
    }
}
