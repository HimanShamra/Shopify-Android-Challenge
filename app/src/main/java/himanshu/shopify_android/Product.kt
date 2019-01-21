package himanshu.shopify_android

/**
 * Created by Himanshu on 2019-01-19.
 * Product Data class
 */
class Product(val name: String, val body: String = "",val image: String = "", val ID:Long=0, val totalInventory:Int=0,val variants:ArrayList<String>)
