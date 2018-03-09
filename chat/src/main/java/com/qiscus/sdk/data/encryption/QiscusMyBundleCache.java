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

package com.qiscus.sdk.data.encryption;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.support.annotation.RestrictTo;
import android.text.TextUtils;
import android.util.Base64;

import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.data.encryption.core.Bundle;
import com.qiscus.sdk.data.encryption.core.BundlePrivate;
import com.qiscus.sdk.data.encryption.core.BundlePublic;
import com.qiscus.sdk.data.encryption.core.HashId;
import com.qiscus.sdk.data.encryption.core.SesameSenderDevice;

/**
 * Created on : March 07, 2018
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public enum QiscusMyBundleCache {
    INSTANCE;

    private final SharedPreferences sharedPreferences;
    private String deviceId;

    QiscusMyBundleCache() {
        sharedPreferences = Qiscus.getApps().getSharedPreferences("e2e_bundle.cache", Context.MODE_PRIVATE);
    }

    public static QiscusMyBundleCache getInstance() {
        return INSTANCE;
    }

    /**
     * @return 64 unique character per device
     */
    private String computeDeviceId() {
        StringBuilder deviceId = new StringBuilder("android_");
        deviceId.append(Settings.Secure.getString(Qiscus.getApps().getContentResolver(), Settings.Secure.ANDROID_ID))
                .append('_').append(Qiscus.getApps().getPackageName());
        if (deviceId.length() > 64) {
            deviceId = new StringBuilder(deviceId.substring(0, 64));
        } else {
            deviceId.append('_');
            char ch = 'a';
            while (deviceId.length() < 64) {
                deviceId.append(ch);
                ch++;
                if (ch > 'z') {
                    ch = 'a';
                }
            }
        }
        return deviceId.toString();
    }

    private void saveDeviceId(String deviceId) {
        this.deviceId = deviceId;
        sharedPreferences.edit().putString("device_id", deviceId).apply();
    }

    public String getDeviceId() {
        if (TextUtils.isEmpty(deviceId)) {
            deviceId = sharedPreferences.getString("device_id", "");
            if (TextUtils.isEmpty(deviceId)) {
                saveDeviceId(computeDeviceId());
            }
        }
        return deviceId;
    }

    public void saveSenderDevice(SesameSenderDevice sesameSenderDevice) {
        byte[] bundlePrivate = sesameSenderDevice.getBundle().bundlePrivate.encode();
        String bundlePrivateStr = Base64.encodeToString(bundlePrivate, Base64.DEFAULT);
        sharedPreferences.edit().putString("bundle_private", bundlePrivateStr).apply();

        byte[] bundlePublic = sesameSenderDevice.getBundle().bundlePublic.encode();
        String bundlePublicStr = Base64.encodeToString(bundlePublic, Base64.DEFAULT);
        sharedPreferences.edit().putString("bundle_public", bundlePublicStr).apply();
    }

    public SesameSenderDevice getSenderDevice() {
        try {
            String bundlePrivateStr = sharedPreferences.getString("bundle_private", "");
            BundlePrivate bundlePrivate = BundlePrivate.decode(Base64.decode(bundlePrivateStr, Base64.DEFAULT));

            String bundlePublicStr = sharedPreferences.getString("bundle_public", "");
            BundlePublic bundlePublic = BundlePublic.decode(Base64.decode(bundlePublicStr, Base64.DEFAULT));

            Bundle bundle = new Bundle(bundlePrivate, bundlePublic);
            return new SesameSenderDevice(new HashId(getDeviceId().getBytes()), Qiscus.getQiscusAccount().getEmail(), bundle);
        } catch (Exception e) {
            return null;
        }
    }

    public void clearData() {
        deviceId = null;
        sharedPreferences.edit().clear().apply();
    }
}
