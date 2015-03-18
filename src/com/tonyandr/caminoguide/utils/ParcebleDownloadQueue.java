package com.tonyandr.caminoguide.utils;

/**
 * Created by Tony on 04-Mar-15.
 */

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author Shriram Shri Shrikumar * * A basic object that can be parcelled to * transfer between objects *
 */
public class ParcebleDownloadQueue implements Parcelable {
    private String strValue;
    private Long longValue;

    /**
     * Standard basic constructor for non-parcel * object creation
     */

    public ParcebleDownloadQueue() { ;
    };



    /**
     * * Constructor to use when re-constructing object * from a parcel * * @param in a parcel from which to read this object
     */
    public ParcebleDownloadQueue(Parcel in) {
        readFromParcel(in);
    }

    /**
     * standard getter * * @return strValue
     */
    public String getStrValue() {
        return strValue;
    }

    /**
     * Standard setter * * @param strValue
     */
    public void setStrValue(String strValue) {
        this.strValue = strValue;
    }

    /**
     * standard getter * * @return
     */
    public Long getLongValue() {
        return longValue;
    }

    /**
     * Standard setter * * @param intValue
     */
    public void setLongValue(Long longValue) {
        this.longValue = longValue;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // We just need to write each field into the
        // parcel. When we read from parcel, they
        // will come back in the same order
        dest.writeString(strValue);
        dest.writeLong(longValue);
    }

    /**
     * * Called from the constructor to create this * object from a parcel. * * @param in parcel from which to re-create object
     */
    private void readFromParcel(Parcel in) {
        // We just need to read back each
        // field in the order that it was
        // written to the parcel
        strValue = in.readString();
        longValue = in.readLong();
    }

    /**
     * * This field is needed for Android to be able to * create new objects, individually or as arrays. * * This also means that you can use use the default * constructor to create the object and use another * method to hyrdate it as necessary. * * I just find it easier to use the constructor. * It makes sense for the way my brain thinks ;-) *
     */
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public ParcebleDownloadQueue createFromParcel(Parcel in) {
            return new ParcebleDownloadQueue(in);
        }

        public ParcebleDownloadQueue[] newArray(int size) {
            return new ParcebleDownloadQueue[size];
        }
    };
}