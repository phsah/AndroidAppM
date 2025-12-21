package com.example.justdoit;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.justdoit.config.Config;
import com.example.justdoit.dto.zadachi.ZadachaItemDTO;
import com.example.justdoit.network.RetrofitClient;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddTaskActivity extends BaseActivity {

    private EditText titleInput;
    private ImageView imagePreview;
    private Uri selectedImageUri;

    private final ActivityResultLauncher<String> imagePicker =
            registerForActivityResult(
                    new ActivityResultContracts.GetContent(),
                    uri -> {
                        if (uri != null) {
                            selectedImageUri = uri;
                            imagePreview.setImageURI(uri);
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        titleInput = findViewById(R.id.taskTitleInput);
        imagePreview = findViewById(R.id.taskImagePreview);

        findViewById(R.id.chooseImageButton)
                .setOnClickListener(v -> imagePicker.launch("image/*"));

        String url = Config.IMAGES_URL+"default.jpg";
        Glide.with(this)
                .load(url)
                .apply(new RequestOptions().override(300))
                .into(imagePreview);
    }

    public void onSaveClick(View view) {
        String title = titleInput.getText().toString().trim();

        if (title.isEmpty()) {
            toast("Введіть назву задачі");
            return;
        }
        if (selectedImageUri == null) {
            toast("Додайте зображення");
            return;
        }

        uploadTask(title, selectedImageUri);
    }

    private void uploadTask(String title, Uri imageUri) {
    try {
        String mimeType = getContentResolver().getType(imageUri);
        if (mimeType == null) mimeType = "image/jpeg";

        RequestBody titlePart =
                RequestBody.create(title, MultipartBody.FORM);

        InputStream inputStream =
                getContentResolver().openInputStream(imageUri);

        byte[] imageBytes = readBytes(inputStream);

        RequestBody imageBody =
                RequestBody.create(imageBytes, MediaType.parse(mimeType));

        MultipartBody.Part imagePart =
                MultipartBody.Part.createFormData(
                        "image",
                        "image.jpg",
                        imageBody
                );

        RetrofitClient.getInstance()
                .getZadachiApi()
                .create(titlePart, imagePart)
                .enqueue(new Callback<ZadachaItemDTO>() {
                    @Override
                    public void onResponse(Call<ZadachaItemDTO> call,
                                           Response<ZadachaItemDTO> response) {
                        if (response.isSuccessful()) {
                            toast("Задача створена");
                            goToMainActivity();
                        } else {
                            toast("Server error: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<ZadachaItemDTO> call, Throwable t) {
                        Log.e("AddTaskActivity", "Upload failed", t);
                        toast("Помилка: " + t.getMessage());
                    }
                });

    } catch (Exception e) {
        e.printStackTrace();
        toast("Помилка читання зображення");
    }
}

private byte[] readBytes(InputStream inputStream) throws IOException {
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    int nRead;
    byte[] data = new byte[4096];
    while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
        buffer.write(data, 0, nRead);
    }
    return buffer.toByteArray();
}


    private String getImagePath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);

        if (cursor != null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String imagePath = cursor.getString(column_index);
            cursor.close();
            return imagePath;
        }

        return null;
    }
    private void toast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
}
