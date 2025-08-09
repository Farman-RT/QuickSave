package com.quicksave.adapter;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.quicksave.R;
import com.quicksave.model.VideoItem;

import java.util.List;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {

    private List<VideoItem> videos;
    private Context context;

    public VideoAdapter(Context context, List<VideoItem> videos) {
        this.context = context;
        this.videos = videos;
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        VideoItem item = videos.get(position);
        holder.title.setText(item.getTitle());
        Glide.with(context)
                .load(item.getThumbnail())
                .placeholder(R.drawable.ic_placeholder)
                .into(holder.thumbnail);

        holder.progress.setVisibility(View.GONE);

        holder.deleteBtn.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Delete")
                    .setMessage("Delete this item from list?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        videos.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, videos.size());
                    })
                    .setNegativeButton("No", null)
                    .show();
        });

        holder.downloadBtn.setOnClickListener(v -> {
            int seconds = computeAdSeconds(item.getQuality());
            showAdThenDownload(item.getDownloadUrl(), seconds, holder);
        });
    }

    private int computeAdSeconds(String quality) {
        if (quality == null) return 5;
        try {
            String q = quality.toLowerCase().replace("p", "").trim();
            int qInt = Integer.parseInt(q);
            if (qInt <= 720) return 5;
            if (qInt <= 1080) return 10;
            if (qInt <= 2000) return 15;
            if (qInt <= 4000) return 30;
        } catch (Exception e) {
            return 5;
        }
        return 5;
    }

    private void showAdThenDownload(String url, int seconds, VideoViewHolder holder) {
        View adView = LayoutInflater.from(context).inflate(R.layout.ad_fullscreen_placeholder, null);
        final androidx.appcompat.app.AlertDialog adDialog = new androidx.appcompat.app.AlertDialog.Builder(context)
                .setView(adView)
                .setCancelable(false)
                .create();

        TextView countdown = adView.findViewById(R.id.ad_countdown_text);
        Button skipBtn = adView.findViewById(R.id.ad_skip_btn);
        skipBtn.setVisibility(View.GONE);

        final int[] remaining = {seconds};
        countdown.setText("Ad: " + remaining[0] + "s");

        adDialog.show();

        final android.os.Handler handler = new android.os.Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                remaining[0]--;
                if (remaining[0] <= 0) {
                    adDialog.dismiss();
                    startDownload(url, holder);
                } else {
                    countdown.setText("Ad: " + remaining[0] + "s");
                    handler.postDelayed(this, 1000);
                }
            }
        };
        handler.postDelayed(runnable, 1000);
    }

    private void startDownload(String url, VideoViewHolder holder) {
        try {
            holder.progress.setVisibility(View.VISIBLE);
            DownloadManager dm = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            Uri uri = Uri.parse(url);
            DownloadManager.Request request = new DownloadManager.Request(uri);
            request.setAllowedOverMetered(true);
            request.setAllowedOverRoaming(false);
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "QuickSave_" + System.currentTimeMillis() + ".mp4");
            long enqueueId = dm.enqueue(request);

            new Thread(() -> {
                boolean downloading = true;
                while (downloading) {
                    DownloadManager.Query q = new DownloadManager.Query();
                    q.setFilterById(enqueueId);
                    Cursor c = dm.query(q);
                    if (c != null && c.moveToFirst()) {
                        int status = c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS));
                        if (status == DownloadManager.STATUS_SUCCESSFUL || status == DownloadManager.STATUS_FAILED) {
                            downloading = false;
                        }
                    } else {
                        downloading = false;
                    }
                    try { Thread.sleep(500); } catch (InterruptedException ignored) {}
                }
                ((android.app.Activity)context).runOnUiThread(() -> holder.progress.setVisibility(View.GONE));
            }).start();

        } catch (Exception e) {
            holder.progress.setVisibility(View.GONE);
            Toast.makeText(context, "Network error", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() {
        return videos.size();
    }

    static class VideoViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnail;
        TextView title;
        Button downloadBtn, deleteBtn;
        ProgressBar progress;

        VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnail = itemView.findViewById(R.id.video_thumb);
            title = itemView.findViewById(R.id.video_title);
            downloadBtn = itemView.findViewById(R.id.btn_download);
            deleteBtn = itemView.findViewById(R.id.btn_delete);
            progress = itemView.findViewById(R.id.item_progress);
        }
    }
}
