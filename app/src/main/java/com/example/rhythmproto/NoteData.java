package com.example.rhythmproto;

import android.os.Parcel;
import android.os.Parcelable;

public class NoteData implements Parcelable {
    public int x;
    public int time;

    public NoteData(int x, int time) {
        this.x = x;
        this.time = time;
    }

    protected NoteData(Parcel in) {
        x = in.readInt();
        time = in.readInt();
    }

    public static final Creator<NoteData> CREATOR = new Creator<NoteData>() {
        @Override
        public NoteData createFromParcel(Parcel in) {
            return new NoteData(in);
        }

        @Override
        public NoteData[] newArray(int size) {
            return new NoteData[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(x);
        dest.writeInt(time);
    }
}
