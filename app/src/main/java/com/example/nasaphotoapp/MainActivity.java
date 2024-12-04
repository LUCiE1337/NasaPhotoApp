package com.example.nasaphotoapp;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import retrofit2.*;
import retrofit2.converter.gson.GsonConverterFactory;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;



public class MainActivity extends AppCompatActivity {
    private static final String NASA_API_KEY = "qsvrNl0DW6V6UxUW0lNqiXs1kRia1Agymlnd00Xo";
    private static final String BASE_URL = "https://api.nasa.gov/";

    private TextView tvTitle, tvDescription, tvDaysAgo;
    private ImageView ivPhoto;
    private Button btnPickDate;

    private Retrofit retrofit;
    private NasaApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvTitle = findViewById(R.id.tvTitle);
        tvDescription = findViewById(R.id.tvDescription);
        ivPhoto = findViewById(R.id.ivPhoto);
        tvDaysAgo = findViewById(R.id.tvDaysAgo);
        btnPickDate = findViewById(R.id.btnPickDate);

        // Konfiguracja Retrofit
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(NasaApiService.class);

        // Pobierz zdjęcie dnia
        fetchPhotoOfTheDay(getCurrentDate());

        // Wybór daty
        btnPickDate.setOnClickListener(v -> openDatePicker());
    }

    private void fetchPhotoOfTheDay(String date) {
        apiService.getPhotoOfTheDay(NASA_API_KEY, date).enqueue(new Callback<NasaPhoto>() {
            @Override
            public void onResponse(Call<NasaPhoto> call, Response<NasaPhoto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    NasaPhoto photo = response.body();

                    tvTitle.setText(photo.getTitle());
                    tvDescription.setText(photo.getExplanation());
                    Glide.with(MainActivity.this).load(photo.getUrl()).into(ivPhoto);

                    // Oblicz liczbę dni od daty publikacji
                    calculateDaysAgo(photo.getDate());
                } else {
                    Toast.makeText(MainActivity.this, "Error fetching data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<NasaPhoto> call, Throwable t) {
                Toast.makeText(MainActivity.this, "API error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    String selectedDate = String.format(Locale.US, "%d-%02d-%02d", year, month + 1, dayOfMonth);
                    fetchPhotoOfTheDay(selectedDate);
                },
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void calculateDaysAgo(String date) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Date publicationDate = sdf.parse(date);
            long diff = new Date().getTime() - publicationDate.getTime();
            long daysAgo = diff / (1000 * 60 * 60 * 24);
            tvDaysAgo.setText("Days since publication: " + daysAgo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        return sdf.format(new Date());
    }
}
