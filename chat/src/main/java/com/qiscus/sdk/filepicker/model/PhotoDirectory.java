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

package com.qiscus.sdk.filepicker.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.qiscus.sdk.filepicker.FilePickerConst;

import java.util.ArrayList;
import java.util.List;

public class PhotoDirectory extends BaseFile implements Parcelable {
    private String bucketId;
    private String coverPath;
    private String name;
    private long dateAdded;
    private List<Media> medias = new ArrayList<>();

    public PhotoDirectory() {
        super();
    }

    public PhotoDirectory(int id, String name, String path) {
        super(id, name, path);
    }

    protected PhotoDirectory(Parcel in) {
        bucketId = in.readString();
        coverPath = in.readString();
        name = in.readString();
        dateAdded = in.readLong();
    }

    public static final Creator<PhotoDirectory> CREATOR = new Creator<PhotoDirectory>() {
        @Override
        public PhotoDirectory createFromParcel(Parcel in) {
            return new PhotoDirectory(in);
        }

        @Override
        public PhotoDirectory[] newArray(int size) {
            return new PhotoDirectory[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PhotoDirectory)) return false;

        PhotoDirectory directory = (PhotoDirectory) o;

        boolean hasId = !TextUtils.isEmpty(bucketId);
        boolean otherHasId = !TextUtils.isEmpty(directory.bucketId);

        return hasId && otherHasId && TextUtils.equals(bucketId, directory.bucketId) && TextUtils.equals(name, directory.name);

    }

    @Override
    public int hashCode() {
        if (TextUtils.isEmpty(bucketId)) {
            return TextUtils.isEmpty(name) ? 0 : name.hashCode();

        }

        int result = bucketId.hashCode();

        if (TextUtils.isEmpty(name)) {
            return result;
        }

        result = 31 * result + name.hashCode();
        return result;
    }

    public String getCoverPath() {
        return coverPath;
    }

    public void setCoverPath(String coverPath) {
        this.coverPath = coverPath;
    }

    public long getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(long dateAdded) {
        this.dateAdded = dateAdded;
    }

    public List<Media> getMedias() {
        return medias;
    }

    public void setMedias(List<Media> medias) {
        this.medias = medias;
    }

    public List<String> getPhotoPaths() {
        List<String> paths = new ArrayList<>(medias.size());
        for (Media media : medias) {
            paths.add(media.getPath());
        }
        return paths;
    }

    public void addPhoto(int id, String name, String path, int mediaType) {
        medias.add(new Media(id, name, path, mediaType));
    }

    public void addPhoto(Media media) {
        medias.add(media);
    }

    public void addPhotos(List<Media> photosList) {
        medias.addAll(photosList);
    }

    public String getBucketId() {
        return bucketId.equals(FilePickerConst.ALL_PHOTOS_BUCKET_ID) ? null : bucketId;
    }

    public void setBucketId(String bucketId) {
        this.bucketId = bucketId;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(bucketId);
        parcel.writeString(coverPath);
        parcel.writeString(name);
        parcel.writeLong(dateAdded);
    }
}
