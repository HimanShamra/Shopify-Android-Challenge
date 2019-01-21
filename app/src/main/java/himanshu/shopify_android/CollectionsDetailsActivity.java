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
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class CollectionsDetailsActivity extends AppCompatActivity implements FetchServiceResultReceiver.Receiver{


    private static final int PRODUCTS_IDS_FETCH=1;
    private static final int PRODUCTS_INFO_FETCH=2;
    private ArrayList<Product> productList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collections_details);
        TextView collectionName = findViewById(R.id.collection_name);
        TextView collectionBody = findViewById(R.id.collection_body);
        ImageView collectionImage = findViewById(R.id.collection_image);
        Collection currentCollection = getIntent().getParcelableExtra("COLLECTION");
        String productsList = String.format(getResources().getString(R.string.product_ids),Long.toString(currentCollection.getID()));
        collectionName.setText(currentCollection.getName());
        collectionBody.setText(currentCollection.getBody());

        Picasso.get().load(currentCollection.getImage()).into(collectionImage);
        startFetchService(productsList,PRODUCTS_IDS_FETCH);
    }

    private void startFetchService(String url,int fetchCallNumber){
        FetchServiceResultReceiver fetchServiceCallback = new FetchServiceResultReceiver(new Handler());
        fetchServiceCallback.setReceiver(this);

        Intent intent = new Intent(CollectionsDetailsActivity.this, FetchService.class);
        intent.putExtra("URL",url);
        intent.putExtra("CALL_NUM",fetchCallNumber);
        intent.putExtra("callback",fetchServiceCallback);
        startService(intent);
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        String data = resultData.getString("FETCH_DATA");
        if(resultCode==PRODUCTS_IDS_FETCH){
            try {
                JSONObject jsonData = new JSONObject(data);
                JSONArray collects = jsonData.getJSONArray("collects");

                StringBuffer urlBuffer = new StringBuffer();

                for(int i=0;i<collects.length();i++){
                    JSONObject jsonCollection = collects.getJSONObject(i);
                    urlBuffer.append(jsonCollection.getLong("product_id"));
                    urlBuffer.append(",");
                }

                //TODO: CLEAN UP YOUR STRINGS
                String URL = String.format(getResources().getString(R.string.product_url),urlBuffer.toString());
                Log.d("PRODUCTDETAILS","URL: "+URL);
                startFetchService(URL,PRODUCTS_INFO_FETCH);
            }catch(JSONException jException){
                //TODO: ERROR HANDLING
            }
        }else if (resultCode==PRODUCTS_INFO_FETCH){
            try{
                JSONObject jsonData = new JSONObject(data);
                productList = prepareProductList(jsonData.getJSONArray("products"));

                ListView productListView = findViewById(R.id.product_list_view);
                productListView.setAdapter(new ProductsListAdapter(productList));

            }catch (JSONException jException){

            }
        }else if (resultCode==0){
            //TODO: ERROR HANDLING
        }
    }

    private ArrayList<Product> prepareProductList(JSONArray productData){
        ArrayList<Product> products = new ArrayList<>();
        try {
            for (int i = 0; i < productData.length(); i++) {
                JSONObject jsonProduct = productData.getJSONObject(i);
                ArrayList<String> variants = new ArrayList<>();
                JSONArray jsonVariants = jsonProduct.getJSONArray("variants");
                int totalInventory=0;
                for (int j = 0; j < jsonVariants.length(); j++){
                    JSONObject cVariant = jsonVariants.getJSONObject(j);
                    totalInventory+=cVariant.getInt("inventory_quantity");
                    variants.add(cVariant.getString("title"));
                }
                products.add(new Product(
                        jsonProduct.getString("title"),
                        jsonProduct.getString("body_html"),
                        jsonProduct.getJSONObject("image").getString("src"),
                        jsonProduct.getLong("id"),
                        totalInventory,
                        variants));
            }
        }catch(JSONException jException){
            Log.d("Product-Test",jException.getMessage());
        }
        return  products;
    }

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
            if(view==null){
                view=getLayoutInflater().inflate(R.layout.product_list_element,null);
                viewHolder=new ViewHolder();
                viewHolder.productNameView =view.findViewById(R.id.product_name);
                viewHolder.productBodyView = view.findViewById(R.id.product_body);
                viewHolder.productInventoryView = view.findViewById(R.id.product_inventory);
                viewHolder.productImageView = view.findViewById(R.id.product_image);
                view.setTag(viewHolder);
            }else{
                viewHolder=(ViewHolder) view.getTag();
            }
            viewHolder.productNameView.setText(selectedProduct.getName());
            viewHolder.productBodyView.setText(selectedProduct.getBody());
            viewHolder.productInventoryView.setText(String.format("%s in stock",String.valueOf(selectedProduct.getTotalInventory())));
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
