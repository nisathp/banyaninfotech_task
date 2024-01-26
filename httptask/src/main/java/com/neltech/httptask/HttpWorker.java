package com.neltech.httptask;

import android.content.Context;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.ListenableWorker;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;

public class HttpWorker extends Worker {


    public HttpWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @androidx.annotation.NonNull
    @Override
    public Result doWork() {
        OkHttpClient client =new OkHttpClient();
        Request request=new Request.Builder()
                .url(getInputData().getString("url"))
                .build();

        try (Response response = client.newCall(request).execute()) {
            assert response.body() != null;

          /* return ListenableWorker.Result.success(new Data.Builder()
                    .putString("output",response.body().string()).build());*/
           return Result.success(new Data.Builder()
                    .putString("output",response.body().string()).build());
        } catch (IOException e) {
           return ListenableWorker.Result.failure();
        }

//return ListenableWorker.Result.success();
    }
}
