package com.quicksave;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.quicksave.adapter.VideoAdapter;
import com.quicksave.model.VideoItem;
import com.quicksave.network.ApiClient;
import com.quicksave.network.ApiService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private EditText urlInput;
    private Button fetchBtn;
    private RecyclerView recyclerView;
    private VideoAdapter adapter;
    private List<VideoItem> videoList = new ArrayList<>();
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        urlInput = findViewById(R.id.url_input);
        fetchBtn = findViewById(R.id.fetch_btn);
        recyclerView = findViewById(R.id.recycler_view);
        progressBar = findViewById(R.id.main_progress);

        adapter = new VideoAdapter(this, videoList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        fetchBtn.setOnClickListener(v -> {
            String url = urlInput.getText().toString().trim();
            if (url.isEmpty()) {
                Toast.makeText(this, "Enter a video URL", Toast.LENGTH_SHORT).show();
                return;
            }
            fetchVideosFromApi(url);
        });

        findViewById(R.id.about_link).setOnClickListener(v -> startActivity(new Intent(this, AboutActivity.class)));
    }

    private void fetchVideosFromApi(String url) {
        progressBar.setVisibility(View.VISIBLE);
        ApiService service = ApiClient.getClient().create(ApiService.class);
        Call<List<VideoItem>> call = service.getVideos(url);
        call.enqueue(new Callback<List<VideoItem>>() {
            @Override
            public void onResponse(Call<List<VideoItem>> call, Response<List<VideoItem>> response) {
                progressBar.setVisibility(View.GONE);
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(MainActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                    return;
                }
                videoList.clear();
                videoList.addAll(response.body());
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<List<VideoItem>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
