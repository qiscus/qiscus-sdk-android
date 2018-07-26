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

/**
 * Created on : August 14, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public class QiscusContact implements Parcelable {
    private String name;
    private String value;
    private String type;

    public QiscusContact(String name, String value, String type) {
        this.name = name;
        this.value = value;
        this.type = type;
    }

    protected QiscusContact(Parcel in) {
        name = in.readString();
        value = in.readString();
        type = in.readString();
    }

    public static final Creator<QiscusContact> CREATOR = new Creator<QiscusContact>() {
        @Override
        public QiscusContact createFromParcel(Parcel in) {
            return new QiscusContact(in);
        }

        @Override
        public QiscusContact[] newArray(int size) {
            return new QiscusContact[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "QiscusContact{" +
                "name='" + name + '\'' +
                ", value='" + value + '\'' +
                ", type='" + type + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return hashCode();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(value);
        dest.writeString(type);
    }
}
