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

package com.qiscus.sdk.filepicker.adapter;

import android.support.v7.widget.RecyclerView;

import com.qiscus.sdk.filepicker.model.BaseFile;

import java.util.ArrayList;
import java.util.List;

public abstract class SelectableAdapter<V extends RecyclerView.ViewHolder, T extends BaseFile>
        extends RecyclerView.Adapter<V> implements Selectable<T> {

    private List<T> items;
    protected List<T> selectedPhotos;

    public SelectableAdapter(List<T> items, List<String> selectedPaths) {
        this.items = items;
        selectedPhotos = new ArrayList<>();

        addPathsToSelections(selectedPaths);
    }

    private void addPathsToSelections(List<String> selectedPaths) {
        if (selectedPaths == null) {
            return;
        }

        for (int i = 0; i < items.size(); i++) {
            for (int j = 0; j < selectedPaths.size(); j++) {
                if (items.get(i).getPath().equals(selectedPaths.get(j))) {
                    selectedPhotos.add(items.get(i));
                }
            }
        }
    }


    /**
     * Indicates if the item at position where is selected
     *
     * @param photo Media of the item to check
     * @return true if the item is selected, false otherwise
     */
    @Override
    public boolean isSelected(T photo) {
        return selectedPhotos.contains(photo);
    }


    /**
     * Toggle the selection status of the item at a given position
     *
     * @param photo Media of the item to toggle the selection status for
     */
    @Override
    public void toggleSelection(T photo) {
        if (selectedPhotos.contains(photo)) {
            selectedPhotos.remove(photo);
        } else {
            selectedPhotos.add(photo);
        }
    }

    /**
     * Clear the selection status for all items
     */
    @Override
    public void clearSelection() {
        selectedPhotos.clear();
    }

    @Override
    public int getSelectedItemCount() {
        return selectedPhotos.size();
    }

    public void setData(List<T> items) {
        this.items = items;
    }

    public List<T> getItems() {
        return items;
    }

}