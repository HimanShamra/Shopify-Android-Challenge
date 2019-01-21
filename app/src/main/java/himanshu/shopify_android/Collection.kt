package himanshu.shopify_android

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by Himanshu on 2019-01-17.
 *
 */
open class Collection(val name: String = "", val body: String = "", val image: String = "", val ID:Long=0) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readLong())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(body)
        parcel.writeString(image)
        parcel.writeLong(ID)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Collection> {
        override fun createFromParcel(parcel: Parcel): Collection {
            return Collection(parcel)
        }

        override fun newArray(size: Int): Array<Collection?> {
            return arrayOfNulls(size)
        }
    }
}
