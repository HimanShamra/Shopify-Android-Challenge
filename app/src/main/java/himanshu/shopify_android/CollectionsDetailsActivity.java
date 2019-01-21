package himanshu.shopify_android;


import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


/**
 * CollectionDetailsActivity for the Collections Details page
 */

public class CollectionsDetailsActivity extends AppCompatActivity implements FetchServiceResultReceiver.Receiver{

    //Call Numbers for each time the FetchService is called
    private static final int PRODUCTS_IDS_FETCH = 1;
    private static final int PRODUCTS_INFO_FETCH = 2;
    private ArrayList<Product> productList;

    /**
     * onCreate initializes the FetchService to find Product IDs and the Card at the top of the screen
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setTitle(getString(R.string.collectionDetailPage));;
        setContentView(R.layout.activity_collections_details);

        TextView collectionName = findViewById(R.id.collection_name);
        TextView collectionBody = findViewById(R.id.collection_body);
        ImageView collectionImage = findViewById(R.id.collection_image);

        Collection currentCollection = getIntent().getParcelableExtra(getResources().getString(R.string.collection));
        String productsList = String.format(getResources().getString(R.string.product_ids), Long.toString(currentCollection.getID()));
        startFetchService(productsList, PRODUCTS_IDS_FETCH);

        collectionName.setText(currentCollection.getName());
        collectionBody.setText(currentCollection.getBody());
        Picasso.get().load(currentCollection.getImage()).into(collectionImage);
    }

    /**
     * Function to call the IntentService
     *
     * url: url of the location to fetch data from
     * fetchCallNumber: call number for the FetchService
     */
    private void startFetchService(String url,int fetchCallNumber){

        FetchServiceResultReceiver fetchServiceCallback = new FetchServiceResultReceiver(new Handler());
        fetchServiceCallback.setReceiver(this);

        Intent intent = new Intent(CollectionsDetailsActivity.this, FetchService.class);
        intent.putExtra(getResources().getString(R.string.url), url);
        intent.putExtra(getResources().getString(R.string.callNum), fetchCallNumber);
        intent.putExtra(getResources().getString(R.string.callback), fetchServiceCallback);
        startService(intent);
    }

    /**
     * Called when data is received from FetchService
     *
     * resultCode: corresponds to whatever call number was provided in the Intent when the service was started, 1 by default.
     *              resultCode==0: a failure in fetching.
     *              resultCode==1: product IDs were fetched and will be used to call the FetchService again to find product data
     *              resultCode==2: product data was fetched and will be parsed to create an array of Products for ListView
     *
     * resultData: data that is received, null if resultCode ==0
     */
    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {

        String data = resultData.getString(getResources().getString(R.string.fetchedData));

        if(resultCode == PRODUCTS_IDS_FETCH){
            try {
                JSONObject jsonData = new JSONObject(data);
                JSONArray collects = jsonData.getJSONArray(getResources().getString(R.string.jsonCollects));
                StringBuffer urlBuffer = new StringBuffer();

                for(int i = 0; i < collects.length(); i++){
                    JSONObject jsonCollection = collects.getJSONObject(i);
                    urlBuffer.append(jsonCollection.getLong(getResources().getString(R.string.jsonProductID)));
                    urlBuffer.append(",");
                }

                String URL = String.format(getResources().getString(R.string.product_url),urlBuffer.toString());
                startFetchService(URL, PRODUCTS_INFO_FETCH);

            }catch(JSONException jException){
                Toast.makeText(this, "Couldn't translate JSON\n" + jException.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }
        }else if (resultCode == PRODUCTS_INFO_FETCH){
            try{
                JSONObject jsonData = new JSONObject(data);
                productList = prepareProductList(jsonData.getJSONArray(getResources().getString(R.string.jsonProducts)));

                ListView productListView = findViewById(R.id.product_list_view);
                productListView.setAdapter(new ProductsListAdapter(productList));

                ProgressBar progressSpinner = findViewById(R.id.progress);
                progressSpinner.setVisibility(View.GONE);

            }catch (JSONException jException){
                Toast.makeText(this, "Couldn't translate JSON\n" + jException.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }
        }else if (resultCode == 0){
            Toast.makeText(this, "An unexpected error has occurred", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Creates an ArrayList of Product given a JSON array which contains the data
     */
    private ArrayList<Product> prepareProductList(JSONArray productData){
        ArrayList<Product> products = new ArrayList<>();
        try {
            for (int i = 0; i < productData.length(); i++) {
                JSONObject jsonProduct = productData.getJSONObject(i);
                ArrayList<String> variants = new ArrayList<>();
                JSONArray jsonVariants = jsonProduct.getJSONArray(getResources().getString(R.string.jsonVariants));
                int totalInventory = 0;

                for (int j = 0; j < jsonVariants.length(); j++){
                    JSONObject cVariant = jsonVariants.getJSONObject(j);
                    totalInventory += cVariant.getInt(getResources().getString(R.string.jsonInventoryQuantity));
                    variants.add(cVariant.getString(getResources().getString(R.string.jsonName)));
                }
                products.add(new Product(
                        jsonProduct.getString(getResources().getString(R.string.jsonName)),
                        jsonProduct.getString(getResources().getString(R.string.jsonBody)),
                        jsonProduct.getJSONObject(getResources().getString(R.string.jsonImage)).getString(getResources().getString(R.string.jsonSrc)),
                        jsonProduct.getLong(getResources().getString(R.string.jsonID)),
                        totalInventory,
                        variants));
            }
        }catch(JSONException jException){
            Toast.makeText(this, "Couldn't translate JSON\n" + jException.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }
        return  products;
    }

    /**
     * Simple implementation of BaseAdapter for Product listing
     */
    public class ProductsListAdapter extends BaseAdapter {
        ArrayList<Product> products;
        public ProductsListAdapter(ArrayList<Product> products){
            this.products = products;
        }

        @Override
        public int getCount() {
            return products.size();
        }

        @Override
        public Object getItem(int index) {
            return products.get(index);
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

            Product selectedProduct = products.get(index);
            if(view == null){
                view = getLayoutInflater().inflate(R.layout.product_list_element,null);
                viewHolder = new ViewHolder();
                viewHolder.productNameView =view.findViewById(R.id.product_name);
                viewHolder.productBodyView = view.findViewById(R.id.collection_body);
                viewHolder.productInventoryView = view.findViewById(R.id.product_inventory);
                viewHolder.productImageView = view.findViewById(R.id.collection_image);
                view.setTag(viewHolder);
            }else{
                viewHolder = (ViewHolder) view.getTag();
            }
            viewHolder.productNameView.setText(selectedProduct.getName());
            viewHolder.productBodyView.setText(selectedProduct.getBody());
            viewHolder.productInventoryView.setText(String.format(getString(R.string.in_stock_message), String.valueOf(selectedProduct.getTotalInventory())));
            Picasso.get().load(selectedProduct.getImage()).into(viewHolder.productImageView);
            return view;
        }

        class ViewHolder {
            TextView productNameView;
            TextView productBodyView;
            TextView productInventoryView;
            ImageView productImageView;
        }
    }
}
