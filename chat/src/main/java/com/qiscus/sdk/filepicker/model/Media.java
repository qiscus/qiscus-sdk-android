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

public class Media extends BaseFile {
    private int mediaType;

    public Media(int id, String name, String path, int mediaType) {
        super(id, name, path);
        this.mediaType = mediaType;
    }

    public Media() {
        super(0, null, null);
    }

    public int getMediaType() {
        return mediaType;
    }

    public void setMediaType(int mediaType) {
        this.mediaType = mediaType;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Media && id == ((Media) o).id;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + mediaType;
        return result;
    }
}
