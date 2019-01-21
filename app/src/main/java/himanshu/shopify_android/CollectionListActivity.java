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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Activity for Collection List page, utilizes an IntentService to fetch JSON
 */
public class CollectionListActivity extends AppCompatActivity implements FetchServiceResultReceiver.Receiver{


    private ArrayList<Collection> collectionsList;

    /**
     * onCreate() starts the fetching service and initializes the ResultReceiver, both through an Intent
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTitle(getString(R.string.collectionsListPage));
        FetchServiceResultReceiver fetchServiceCallback = new FetchServiceResultReceiver(new Handler());
        fetchServiceCallback.setReceiver(this);

        Intent intent = new Intent(CollectionListActivity.this, FetchService.class);
        intent.putExtra(getResources().getString(R.string.url), getResources().getString(R.string.collections_url));
        intent.putExtra(getResources().getString(R.string.callback), fetchServiceCallback);

        this.startService(intent);
    }

    /**
     *  Runs when FetchService is complete.
     *
     *  resultCode: which service number is fetched, 1 by default. A value of 0 means a failure to fetch data
     *  resultData: contains the data that is fetched, null if resultCode ==0
     */
    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        if (resultCode == 1){
            String data = resultData.getString(getResources().getString(R.string.fetchedData));
            Log.d("JSON", data);
            try {
                collectionsList = new ArrayList<>();
                JSONObject jsonData = new JSONObject(data);
                JSONArray collections = jsonData.getJSONArray(getResources().getString(R.string.jsonCollections));

                for(int i = 0; i < collections.length(); i++){
                    JSONObject jsonCollection = collections.getJSONObject(i);

                    collectionsList.add(new Collection(jsonCollection.getString(getResources().getString(R.string.jsonName)),
                            jsonCollection.getString(getResources().getString(R.string.jsonBody)),
                            jsonCollection.getJSONObject(getResources().getString(R.string.jsonImage)).getString(getResources().getString(R.string.jsonSrc)),
                            (Long)jsonCollection.get(getResources().getString(R.string.jsonID))));

                }

                ListView collectionsListView = findViewById(R.id.collectionsListView);
                collectionsListView.setAdapter(new CollectionsListAdapter(collectionsList));
                collectionsListView.setOnItemClickListener(listClickListener);

            }catch(JSONException jException){
                Log.d("JSON", jException.getMessage());
                //TODO:UPDATE UI ACCORDINGLY
            }
        }
    }

    /**
     * An onClickListener for the elements in the list, onClick it starts the next activity and passes a Collection Object through the Intent
     */
    private AdapterView.OnItemClickListener listClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int index, long id) {
            Intent collectionDetailsIntent = new Intent(CollectionListActivity.this, CollectionsDetailsActivity.class);
            collectionDetailsIntent.putExtra(getResources().getString(R.string.collection), collectionsList.get(index));
            startActivity(collectionDetailsIntent);
        }
    };

    /**
     * A simple BaseAdapter implementation
     */
    public class CollectionsListAdapter extends BaseAdapter {
        ArrayList<Collection> collections;
        public CollectionsListAdapter(ArrayList<Collection> collections){
            this.collections = collections;
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
            Collection selectedCollection = collections.get(index);

            if(view == null){
                view = getLayoutInflater().inflate(R.layout.list_element, null);
                viewHolder = new ViewHolder();
                viewHolder.textViewKey = view.findViewById(R.id.product_name);
                viewHolder.collectionImage = view.findViewById(R.id.collection_image);
                view.setTag(viewHolder);
            }else{
                viewHolder = (ViewHolder)view.getTag();
            }
            viewHolder.textViewKey.setText(selectedCollection.getName());
            Picasso.get().load(selectedCollection.getImage()).into(viewHolder.collectionImage);
            return view;
        }

        class ViewHolder {
            TextView textViewKey;
            ImageView collectionImage;
        }
    }
}
