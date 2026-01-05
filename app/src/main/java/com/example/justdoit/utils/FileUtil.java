package com.example.justdoit.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

public class FileUtil {

    public static String getFileName(Context context, Uri uri) {
        String name = null;

        if ("content".equals(uri.getScheme())) {
            try (Cursor cursor = context.getContentResolver()
                    .query(uri, null, null, null, null)) {

                if (cursor != null && cursor.moveToFirst()) {
                    int index =
                            cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index >= 0) {
                        name = cursor.getString(index);
                    }
                }
            }
        }

        if (name == null) {
            name = "image_" + System.currentTimeMillis() + ".jpg";
        }

        return name;
    }

    public static MultipartBody.Part createImagePart(
            Context context,
            Uri uri,
            String partName,
            String fileName
    ) {
        try (InputStream is = context.getContentResolver().openInputStream(uri);
             ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {

            byte[] data = new byte[8192];
            int n;
            while ((n = is.read(data)) != -1) {
                buffer.write(data, 0, n);
            }

            RequestBody body = RequestBody.create(
                    MediaType.parse("image/*"),
                    buffer.toByteArray()
            );

            return MultipartBody.Part.createFormData(
                    partName,
                    fileName,
                    body
            );

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
