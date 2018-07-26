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

import java.io.File;

/**
 * Created on : August 08, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public class QiscusPhoto implements Parcelable {
    public static final Creator<QiscusPhoto> CREATOR = new Creator<QiscusPhoto>() {
        @Override
        public QiscusPhoto createFromParcel(Parcel in) {
            return new QiscusPhoto(in);
        }

        @Override
        public QiscusPhoto[] newArray(int size) {
            return new QiscusPhoto[size];
        }
    };
    private File photoFile;
    private boolean selected;

    public QiscusPhoto(File photoFile, boolean selected) {
        this.photoFile = photoFile;
        this.selected = selected;
    }

    public QiscusPhoto(File photoFile) {
        this.photoFile = photoFile;
    }

    protected QiscusPhoto(Parcel in) {
        photoFile = new File(in.readString());
        selected = in.readByte() != 0;
    }

    public File getPhotoFile() {
        return photoFile;
    }

    public void setPhotoFile(File photoFile) {
        this.photoFile = photoFile;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public int describeContents() {
        return hashCode();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(photoFile.getAbsolutePath());
        dest.writeByte((byte) (selected ? 1 : 0));
    }
}
