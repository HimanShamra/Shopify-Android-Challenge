package himanshu.shopify_android;


import android.content.Intent;
import android.graphics.Paint;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


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
        TextView collectionBody = findViewById(R.id.product_body);
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

                //ListView productListView = findViewById(R.id.product_list_view);
                //productListView.setAdapter(new ProductsListAdapter(productList));

                ExpandableListView exList = (ExpandableListView) findViewById(R.id.expandingList);
                ProductsListExAdapter exAdpt = new ProductsListExAdapter(productList);
                exList.setAdapter(exAdpt);

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
                ArrayList<Pair<String,Integer>> variants = new ArrayList<>();
                JSONArray jsonVariants = jsonProduct.getJSONArray(getResources().getString(R.string.jsonVariants));
                int totalInventory = 0;

                for (int j = 0; j < jsonVariants.length(); j++){
                    JSONObject cVariant = jsonVariants.getJSONObject(j);
                    totalInventory += cVariant.getInt(getResources().getString(R.string.jsonInventoryQuantity));
                    variants.add(new Pair(cVariant.getString(getResources().getString(R.string.jsonName)),
                            cVariant.getInt(getResources().getString(R.string.jsonInventoryQuantity))));
                }
                products.add(new Product(
                        jsonProduct.getString(getResources().getString(R.string.jsonName)),
                        jsonProduct.getString(getResources().getString(R.string.jsonBody)),
                        jsonProduct.getJSONObject(getResources().getString(R.string.jsonImage)).
                                getString(getResources().getString(R.string.jsonSrc)),
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
     * An Adapter for the ExpandableListView
     */
    class ProductsListExAdapter extends BaseExpandableListAdapter{

        ArrayList<Product> products;
        public ProductsListExAdapter(ArrayList<Product> products){
            this.products=products;
        }

        @Override
        public View getGroupView(int index, boolean isExpanded, View view, ViewGroup viewGroup) {
            View cView = view;
            Product selectedProduct = products.get(index);

            if(cView==null){
                cView = getLayoutInflater().inflate(R.layout.product_list_element,null);
            }
            TextView name = cView.findViewById(R.id.product_name);
            TextView body = cView.findViewById(R.id.product_body);
            TextView stock = cView.findViewById(R.id.product_inventory);
            ImageView image = cView.findViewById(R.id.product_image);

            name.setText(selectedProduct.getName());
            body.setText(selectedProduct.getBody());
            stock.setText(String.format(getString(R.string.in_stock_message),
                    String.valueOf(selectedProduct.getTotalInventory())));
            Picasso.get().load(selectedProduct.getImage()).into(image);
            return cView;
        }

        @Override
        public View getChildView(int index, int index1, boolean isExpanded, View view,
                                 ViewGroup viewGroup) {
            View cView = view;
            String selectedVariant = products.get(index).getVariants().get(index1).first;
            int stock = products.get(index).getVariants().get(index1).second;
            if(cView==null){
                cView = getLayoutInflater().inflate(R.layout.product_child_list_element,null);
            }
            TextView variantName = cView.findViewById(R.id.variant_name);
            TextView inventory = cView.findViewById(R.id.variant_inventory);

            variantName.setText(selectedVariant);
            inventory.setText(String.format(getResources().getString(R.string.in_stock_message),
                    String.valueOf(stock)));
            return cView;
        }

        /**
         * Boilerplate code below
         */

        @Override
        public int getGroupCount() {return products.size();}

        @Override
        public int getChildrenCount(int i) {return products.get(i).getVariants().size();}

        @Override
        public Object getGroup(int i) {return products.get(i);}

        @Override
        public Object getChild(int i, int j) {return products.get(i).getVariants().get(j);}

        @Override
        public long getGroupId(int i) {return 0;}

        @Override
        public long getChildId(int i, int i1) {return 0;}

        @Override
        public boolean hasStableIds() {return false;}

        @Override
        public boolean isChildSelectable(int i, int i1) {
            return false;
        }
    }
}
