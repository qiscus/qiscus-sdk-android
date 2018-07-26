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

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.webkit.MimeTypeMap;

import com.qiscus.sdk.chat.core.QiscusCore;
import com.qiscus.sdk.chat.core.R;
import com.qiscus.sdk.chat.core.data.local.QiscusCacheManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class QiscusImageUtil {

    public static final String IMAGE_PATH = QiscusCore.getAppsName() + File.separator +
            QiscusCore.getAppsName() + " Images";

    private QiscusImageUtil() {

    }

    public static Bitmap getScaledBitmap(Uri imageUri) {
        String filePath = QiscusFileUtil.getRealPathFromURI(imageUri);
        Bitmap scaledBitmap = null;

        BitmapFactory.Options options = new BitmapFactory.Options();

        //by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
        //you try the use the bitmap here, you will get null.
        options.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(filePath, options);

        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;

        if (actualWidth < 0 || actualHeight < 0) {
            Bitmap bitmap2 = BitmapFactory.decodeFile(filePath);
            actualWidth = bitmap2.getWidth();
            actualHeight = bitmap2.getHeight();
        }

        //max Height and width values of the compressed image is taken as 1440x900
        float maxHeight = QiscusCore.getChatConfig().getQiscusImageCompressionConfig().getMaxHeight();
        float maxWidth = QiscusCore.getChatConfig().getQiscusImageCompressionConfig().getMaxWidth();
        float imgRatio = actualWidth / actualHeight;
        float maxRatio = maxWidth / maxHeight;

        //width and height values are set maintaining the aspect ratio of the image
        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight;
                actualWidth = (int) (imgRatio * actualWidth);
                actualHeight = (int) maxHeight;
            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth;
                actualHeight = (int) (imgRatio * actualHeight);
                actualWidth = (int) maxWidth;
            } else {
                actualHeight = (int) maxHeight;
                actualWidth = (int) maxWidth;

            }
        }

        //setting inSampleSize value allows to load a scaled down version of the original image
        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);

        //inJustDecodeBounds set to false to load the actual bitmap
        options.inJustDecodeBounds = false;

        //this options allow android to claim the bitmap memory if it runs low on memory
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inTempStorage = new byte[16 * 1024];

        try {
            //load the bitmap from its path
            bmp = BitmapFactory.decodeFile(filePath, options);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();

        }
        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
        }

        float ratioX = actualWidth / (float) options.outWidth;
        float ratioY = actualHeight / (float) options.outHeight;
        float middleX = actualWidth / 2.0f;
        float middleY = actualHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

        //check the rotation of the image and display it properly
        ExifInterface exif;
        try {
            exif = new ExifInterface(filePath);

            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
            } else if (orientation == 3) {
                matrix.postRotate(180);
            } else if (orientation == 8) {
                matrix.postRotate(270);
            }
            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0,
                    scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix,
                    true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return scaledBitmap;
    }

    public static File compressImage(File imageFile) {

        FileOutputStream out = null;
        String filename = QiscusFileUtil.generateFilePath(imageFile.getName(), ".jpg");
        try {
            out = new FileOutputStream(filename);

            //write the compressed bitmap at the destination specified by filename.
            QiscusImageUtil.getScaledBitmap(Uri.fromFile(imageFile)).compress(Bitmap.CompressFormat.JPEG,
                    QiscusCore.getChatConfig().getQiscusImageCompressionConfig().getQuality(), out);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException ignored) {
                //Do nothing
            }
        }

        File compressedImage = new File(filename);
        QiscusFileUtil.notifySystem(compressedImage);

        return compressedImage;
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        final float totalPixels = width * height;
        final float totalReqPixelsCap = reqWidth * reqHeight * 2;

        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++;
        }

        return inSampleSize;
    }

    public static boolean isImage(File file) {
        return isImage(file.getPath());
    }

    public static boolean isImage(String fileName) {
        String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(QiscusFileUtil.getExtension(fileName));
        if (type == null) {
            return false;
        } else if (type.contains("image")) {
            return true;
        }

        return false;
    }

    public static void addImageToGallery(File picture) {
        QiscusFileUtil.notifySystem(picture);
    }

    public static void showImageFolderAppInGallery() {
        File nomedia = new File(Environment.getExternalStorageDirectory().getPath(),
                QiscusImageUtil.IMAGE_PATH + File.separator +
                        QiscusCore.getApps().getString(R.string.qiscus_nomedia));
        if (nomedia.exists()) {
            nomedia.delete();
            //rescan media gallery for updating deleted .nomedia file
            QiscusFileUtil.notifySystem(nomedia);
        }
    }

    public static void hideImageFolderAppInGallery() {
        File nomedia = new File(Environment.getExternalStorageDirectory().getPath(),
                QiscusImageUtil.IMAGE_PATH + File.separator +
                        QiscusCore.getApps().getString(R.string.qiscus_nomedia));

        if (!nomedia.getParentFile().exists()) {
            nomedia.getParentFile().mkdirs();
        }

        if (!nomedia.exists()) {
            try {
                if (nomedia.createNewFile()) {
                    QiscusFileUtil.notifySystem(nomedia);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(new Date());
        String imageFileName = "JPEG-" + timeStamp + "-";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        QiscusCacheManager.getInstance().cacheLastImagePath("file:" + image.getAbsolutePath());
        return image;
    }

    public static Bitmap getCircularBitmap(Bitmap bm) {
        int size = 192;

        Bitmap bitmap = ThumbnailUtils.extractThumbnail(bm, size, size);

        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(output);

        final int color = 0xffff0000;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setFilterBitmap(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawOval(rectF, paint);

        paint.setColor(Color.BLUE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth((float) 4);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    public static String generateBlurryThumbnailUrl(String imageUrl) {
        return generateBlurryThumbnailUrl(imageUrl, 320, 320, 300);
    }

    public static String generateBlurryThumbnailUrl(String imageUrl, int width, int height, int blur) {
        if (imageUrl == null) {
            return null;
        }

        int i = imageUrl.indexOf("upload/");
        if (i > 0) {
            i += 7;
            String blurryImageUrl = imageUrl.substring(0, i);
            blurryImageUrl += "w_" + width + ",h_" + height + ",c_limit,e_blur:" + blur + "/";
            String file = imageUrl.substring(i);
            i = file.lastIndexOf('.');
            if (i > 0) {
                file = file.substring(0, i);
            }
            return blurryImageUrl + file + ".jpg";
        }
        return imageUrl;
    }
}
