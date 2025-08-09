package com.quicksave.network;

import com.quicksave.model.VideoItem;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {
    @GET("video")
    Call<List<VideoItem>> getVideos(@Query("url") String videoUrl);

    @FormUrlEncoded
    @POST("video/fetch")
    Call<List<VideoItem>> fetchVideos(@Field("url") String videoUrl);
}
