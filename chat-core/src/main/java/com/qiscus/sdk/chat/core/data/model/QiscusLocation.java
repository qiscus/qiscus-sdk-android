/*
 * Copyright (c) 2016 Qiscus.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.qiscus.sdk.chat.core.data.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

/**
 * Created on : August 16, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public class QiscusLocation implements Parcelable {
    public static final Creator<QiscusLocation> CREATOR = new Creator<QiscusLocation>() {
        @Override
        public QiscusLocation createFromParcel(Parcel in) {
            return new QiscusLocation(in);
        }

        @Override
        public QiscusLocation[] newArray(int size) {
            return new QiscusLocation[size];
        }
    };
    private String name;
    private String address;
    private double latitude;
    private double longitude;
    private String mapUrl;
    private String thumbnailUrl;

    public QiscusLocation() {

    }

    protected QiscusLocation(Parcel in) {
        name = in.readString();
        address = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
        mapUrl = in.readString();
        thumbnailUrl = in.readString();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getMapUrl() {
        if (TextUtils.isEmpty(mapUrl)) {
            generateMapUrl();
        }
        return mapUrl;
    }

    private void generateMapUrl() {
        mapUrl = "http://maps.google.com/?q=" + latitude + "," + longitude;
    }

    public String getThumbnailUrl() {
        if (TextUtils.isEmpty(thumbnailUrl)) {
            generateThumbnail();
        }
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public void generateThumbnail() {
        thumbnailUrl = "http://maps.google.com/maps/api/staticmap?center="
                + latitude + "," + longitude + "&zoom=17&size=512x300&sensor=false";
    }

    @Override
    public int describeContents() {
        return hashCode();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(address);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeString(mapUrl);
        dest.writeString(thumbnailUrl);
    }

    @Override
    public String toString() {
        return "QiscusLocation{" +
                "name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", mapUrl='" + getMapUrl() + '\'' +
                ", thumbnailUrl='" + getThumbnailUrl() + '\'' +
                '}';
    }
}
