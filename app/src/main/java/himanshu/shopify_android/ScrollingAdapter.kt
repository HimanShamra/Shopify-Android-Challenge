package himanshu.shopify_android

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import org.w3c.dom.Text

/**
 * Created by Himanshu on 2019-01-18.
 *
 */
class ScrollingAdapter(private var listData: ArrayList<Collection>): RecyclerView.Adapter<ScrollingAdapter.ViewHolder>() {
    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        holder?.bindListItems(listData[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        val mainView:View

        mainView = LayoutInflater.from(parent?.context)
                .inflate(R.layout.list_element, parent, false) as View

        return ViewHolder(mainView)

    }

    override fun getItemCount() = listData.size

    class ViewHolder(val view:View):RecyclerView.ViewHolder(view){

        fun bindListItems(collection: Collection){
            val name = view.findViewById<TextView>(R.id.name) as TextView
            name.text=collection.name
        }
    }
}