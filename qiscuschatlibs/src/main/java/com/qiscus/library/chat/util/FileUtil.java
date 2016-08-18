package com.qiscus.library.chat.util;

import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.qiscus.library.chat.Qiscus;
import com.qiscus.library.chat.R;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileUtil {
    public static final String FILES_PATH = AndroidUtilities.getString(R.string.app_name) + File.separator + "Files";

    public static File from(Uri uri) throws IOException {
        InputStream inputStream = Qiscus.getApps().getContentResolver().openInputStream(uri);
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
            IOUtils.copy(inputStream, out);
            inputStream.close();
        }

        if (out != null) {
            out.close();
        }
        return tempFile;
    }

    public static File from(InputStream inputStream, String fileName, int topicId) throws
            IOException {
        File file = new File(generateFilePath(fileName, topicId));
        file = rename(file, fileName);
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        IOUtils.copy(inputStream, out);

        if (out != null) {
            out.close();
        }
        return file;
    }

    public static String[] splitFileName(String fileName) {
        String name = fileName;
        String extension = "";
        int i = fileName.lastIndexOf(".");
        if (i != -1) {
            name = fileName.substring(0, i);
            extension = fileName.substring(i);
        }

        return new String[]{name, extension};
    }

    public static String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = Qiscus.getApps().getContentResolver().query(uri, null, null, null, null);
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
        Cursor cursor = Qiscus.getApps().getContentResolver().query(contentUri, null, null, null, null);
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

    public static File saveFile(File file, int topicId) {
        String path = generateFilePath(Uri.fromFile(file), topicId);
        File newFile = new File(path);
        try {
            FileInputStream in = new FileInputStream(file);
            FileOutputStream out = new FileOutputStream(newFile);
            IOUtils.copy(in, out);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return newFile;
    }

    private static String generateFilePath(Uri uri, int topicId) {
        File file = new File(Environment.getExternalStorageDirectory().getPath(),
                ImageUtil.isImage(uri.getPath()) ? ImageUtil.IMAGE_PATH : FILES_PATH);

        if (!file.exists()) {
            file.mkdirs();
        }

        return file.getAbsolutePath() + File.separator + addTopicToFileName(getFileName(uri), topicId);
    }

    public static String generateFilePath(String fileName, int topicId) {
        File file = new File(Environment.getExternalStorageDirectory().getPath(),
                ImageUtil.isImage(fileName) ? ImageUtil.IMAGE_PATH : FILES_PATH);

        if (!file.exists()) {
            file.mkdirs();
        }

        return file.getAbsolutePath() + File.separator + addTopicToFileName(fileName, topicId);
    }

    public static String addTopicToFileName(String fileName, int topicId) {
        int existedTopicId = getTopicFromFileName(fileName);
        if (existedTopicId == -1) {
            String[] fileNameSplit = splitFileName(fileName);
            return fileNameSplit[0] + "_topic_" + topicId + "_topic" + fileNameSplit[1];
        } else if (existedTopicId != topicId) {
            return replaceTopicInFileName(fileName, topicId);
        }

        return fileName;
    }

    private static String replaceTopicInFileName(String fileName, int topicId) {
        String[] fileNameSplit = splitFileName(fileName);
        int startTopicIndex = fileNameSplit[0].indexOf("_topic_");
        return fileNameSplit[0].substring(0, startTopicIndex) + "_topic_" + topicId + "_topic" + fileNameSplit[1];
    }

    public static int getTopicFromFileName(String fileName) {
        int startTopicIndex = fileName.indexOf("topic_");
        int lastTopicIndex = fileName.lastIndexOf("_topic");
        if (startTopicIndex >= 0 && lastTopicIndex >= 0) {
            try {
                return Integer.parseInt(fileName.substring(startTopicIndex + 6, lastTopicIndex));
            } catch (Exception e) {
                return -2;
            }
        }
        return -1;
    }

    public static File rename(File file, String newName) {
        File newFile = new File(file.getParent(), newName);
        if (!newFile.equals(file)) {
            if (newFile.exists()) {
                if (newFile.delete()) {
                    Log.d("FileUtil", "Delete old " + newName + " file");
                }
            }
            if (file.renameTo(newFile)) {
                Log.d("FileUtil", "Rename file to " + newName);
            }
        }
        return newFile;
    }

    public static boolean isContains(int topicId, String fileName) {
        File file = new File(Environment.getExternalStorageDirectory().getPath(), FILES_PATH + topicId + File.separator + fileName);
        return file.exists();
    }

    public static boolean isVideo(File file) {
        String path = file.getPath();
        int lastDotPosition = path.lastIndexOf(".");
        String ext = path.substring(lastDotPosition + 1);
        ext = ext.replace("_", "");
        ext = ext.toLowerCase();
        String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);
        if (type == null) {
            return false;
        } else if (type.contains("video")) {
            return true;
        }

        return false;
    }

    public static String getExtension(File file) {
        return getExtension(file.getPath());
    }

    public static String getExtension(String fileName) {
        int lastDotPosition = fileName.lastIndexOf(".");
        String ext = fileName.substring(lastDotPosition + 1);
        ext = ext.replace("_", "");
        return ext.trim().toLowerCase();
    }
}
