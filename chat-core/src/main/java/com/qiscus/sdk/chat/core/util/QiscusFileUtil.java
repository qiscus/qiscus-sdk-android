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

package com.qiscus.sdk.chat.core.util;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class QiscusFileUtil {

    public static final String FILES_PATH = QiscusConst.getAppsName() + File.separator + "Files";
    public static final String IMAGE_PATH = QiscusConst.getAppsName() + File.separator +
            QiscusConst.getAppsName() + " Images";
    private static final int EOF = -1;
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

    private QiscusFileUtil() {

    }

    public static File from(Uri uri) throws IOException {
        if (uri != null) {
            InputStream inputStream = QiscusConst.getApps().getContentResolver().openInputStream(uri);
            String fileName = getFileName(uri);
            String[] splitName = splitFileName(fileName);
            File tempFile = File.createTempFile(splitName[0], splitName[1]);
            tempFile = rename(tempFile, fileName);
            tempFile.deleteOnExit();
            FileOutputStream out = null;
            try {
                out = new FileOutputStream(tempFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            if (inputStream != null) {
                copy(inputStream, out);
                inputStream.close();
            }

            if (out != null) {
                out.close();
            }
            return tempFile;
        } else {
            throw new IOException("File Uri is Null, Please check your implementation");
        }
    }

    public static File from(InputStream inputStream, String fileName) throws IOException {
        File file = new File(generateFilePath(fileName));
        file = rename(file, fileName);
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        copy(inputStream, out);

        if (out != null) {
            out.close();
        }
        return file;
    }

    public static String[] splitFileName(String fileName) {
        String name = fileName;
        String extension = "";
        int i = fileName.lastIndexOf('.');
        if (i != -1) {
            name = fileName.substring(0, i);
            extension = fileName.substring(i);
        }

        return new String[]{name, extension};
    }

    public static String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = QiscusConst.getApps().getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf(File.separator);
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    public static String getRealPathFromURI(Uri contentUri) {
        Cursor cursor = QiscusConst.getApps().getContentResolver().query(contentUri, null, null, null, null);
        if (cursor == null) {
            return contentUri.getPath();
        } else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            String realPath = cursor.getString(index);
            cursor.close();
            return realPath;
        }
    }

    public static File saveFile(File file) {
        String path = generateFilePath(file.getName());
        File newFile = new File(path);
        try {
            FileInputStream in = new FileInputStream(file);
            FileOutputStream out = new FileOutputStream(newFile);
            copy(in, out);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return newFile;
    }

    public static String generateFilePath(String fileName) {
        String[] fileNameSplit = splitFileName(fileName);
        int androidVersion = Build.VERSION.SDK_INT;
        if (androidVersion >= 30) {
            return generateFilePath(fileName, fileNameSplit[1], getEnvironment(fileName));
        } else {
            return generateFilePath(fileName, fileNameSplit[1]);
        }

    }

    //api >=30
    public static String generateFilePath(String fileName, String extension, String environment) {
        File file = new File(Environment.getExternalStoragePublicDirectory(environment),
                isImage(fileName) ? IMAGE_PATH : FILES_PATH);

        if (!file.exists()) {
            file.mkdirs();
        }

        int index = 0;
        String directory = file.getAbsolutePath() + File.separator;
        String[] fileNameSplit = splitFileName(fileName);
        while (true) {
            File newFile;
            if (index == 0) {
                newFile = new File(directory + fileNameSplit[0] + extension);
            } else {
                newFile = new File(directory + fileNameSplit[0] + "-" + index + extension);
            }
            if (!newFile.exists()) {
                return newFile.getAbsolutePath();
            }
            index++;
        }
    }


    public static String generateFilePath(String fileName, String extension) {
        File file = new File(Environment.getExternalStorageDirectory().getPath(),
                isImage(fileName) ? IMAGE_PATH : FILES_PATH);

        if (!file.exists()) {
            file.mkdirs();
        }

        int index = 0;
        String directory = file.getAbsolutePath() + File.separator;
        String[] fileNameSplit = splitFileName(fileName);
        while (true) {
            File newFile;
            if (index == 0) {
                newFile = new File(directory + fileNameSplit[0] + extension);
            } else {
                newFile = new File(directory + fileNameSplit[0] + "-" + index + extension);
            }
            if (!newFile.exists()) {
                return newFile.getAbsolutePath();
            }
            index++;
        }
    }

    public static boolean isImage(String fileName) {
        String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(getExtension(fileName));
        if (type == null) {
            return false;
        } else if (type.contains("image")) {
            return true;
        }

        return false;
    }

    public static String getEnvironment(String fileName) {
        String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(getExtension(fileName));
        if (type == null) {
            return Environment.DIRECTORY_DOWNLOADS;
        } else if (type.contains("image")) {
            return Environment.DIRECTORY_PICTURES;
        } else if (type.contains("video")) {
            return Environment.DIRECTORY_MOVIES;
        } else if (type.contains("audio")) {
            return Environment.DIRECTORY_MUSIC;
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                return Environment.DIRECTORY_DOCUMENTS;
            } else {
                return Environment.DIRECTORY_DOWNLOADS;
            }
        }
    }



    public static File rename(File file, String newName) {
        File newFile = new File(file.getParent(), newName);
        if (!newFile.equals(file)) {
            if (newFile.exists()) {
                newFile.delete();
            }
            file.renameTo(newFile);
        }
        return newFile;
    }

    public static boolean isContains(String path) {
        File file = new File(path);
        return file.exists();
    }

    public static String getExtension(File file) {
        return getExtension(file.getPath());
    }

    public static String getExtension(String fileName) {
        int lastDotPosition = fileName.lastIndexOf('.');
        String ext = fileName.substring(lastDotPosition + 1);
        ext = ext.replace("_", "");
        return ext.trim().toLowerCase();
    }

    private static int copy(InputStream input, OutputStream output) throws IOException {
        long count = copyLarge(input, output);
        if (count > Integer.MAX_VALUE) {
            return -1;
        }
        return (int) count;
    }

    private static long copyLarge(InputStream input, OutputStream output) throws IOException {
        return copyLarge(input, output, new byte[DEFAULT_BUFFER_SIZE]);
    }

    private static long copyLarge(InputStream input, OutputStream output, byte[] buffer) throws IOException {
        long count = 0;
        int n;
        while (EOF != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

    public static String createTimestampFileName(String extension) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(new Date());
        return timeStamp + "." + extension;
    }

    public static void notifySystem(File file) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(Uri.fromFile(file));
        QiscusConst.getApps().sendBroadcast(mediaScanIntent);
    }

    public static String getThumbnailURL(String url) {
        return getThumbnailURL(url, 320, 320, 0);
    }

    public static String getBlurryThumbnailURL(String url) {
        return getThumbnailURL(url, 320, 320, 300);
    }

    private static String getThumbnailURL(String url, int width, int height, int blur) {
        if (url == null) {
            return null;
        }

        int i = url.indexOf("upload/");
        if (i > 0) {
            i += 7;
            String thumbnailUrl = url.substring(0, i);

            if (blur == 300) {
                thumbnailUrl += "w_" + width + ",h_" + height + ",c_limit,e_blur:" + blur + "/";
            } else {
                thumbnailUrl += "w_" + width + ",h_" + height + ",c_limit" + "/";
            }
            String file = url.substring(i);
            i = file.lastIndexOf('.');
            if (i > 0) {
                file = file.substring(0, i);
            }
            return thumbnailUrl + file + ".png";
        }
        return url;
    }
}
