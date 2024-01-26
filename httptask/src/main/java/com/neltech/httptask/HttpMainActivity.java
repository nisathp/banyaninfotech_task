package com.neltech.httptask;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;



import android.os.Bundle;

import android.util.Log;

import com.google.gson.Gson;
import com.neltech.httptask.databinding.ActivityHttpMainBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class HttpMainActivity extends AppCompatActivity {
    public static final String API_KEY="4d7f32fcdaa440c8957002be5a281290";
    public final MediatorLiveData<WorkInfo> liveWorkStatus = new MediatorLiveData<>();
    private ActivityHttpMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHttpMainBinding.inflate(getLayoutInflater());
        binding.httpcall.setOnClickListener((view)->{
            doTheDownload();
        });
        setContentView(binding.getRoot());

    }

    public void doTheDownload() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build();
        OneTimeWorkRequest downloadWork =
                new OneTimeWorkRequest.Builder(HttpWorker.class)
                        .setConstraints(constraints)
                        .setInputData(new Data.Builder()
                                .putString("url",
                                        "https://www.boredapi.com/api/activity")

                                .build())
                        .addTag("download")
                        .build();
        WorkManager.getInstance(this).enqueue(downloadWork);

        final LiveData<WorkInfo> liveOpStatus =
                WorkManager.getInstance(this).getWorkInfoByIdLiveData(downloadWork.getId());


        liveOpStatus.observe((LifecycleOwner) this, new Observer<WorkInfo>() {
            @Override
            public void onChanged(@Nullable WorkInfo workInfo) {
                if (workInfo != null) {

                    Log.d("TAG###", "onChanged: "+workInfo.getState().name());
                    Log.d("TAG###", "onChanged: "+workInfo.getOutputData().getString("output"));
                    binding.httpcall.setText(workInfo.getState().name());
                   if (workInfo.getOutputData().getString("output")!=null){
                       try {
                          // JSONObject jsonObject=new JSONObject(Objects.requireNonNull(workInfo.getOutputData().getString("output")));
                           Response response=new Gson().fromJson(Objects.requireNonNull(workInfo.getOutputData().getString("output")),Response.class);
                           binding.dataStatus.setText("My Activity: "+response.getActivity()+"\n");
                           binding.dataStatus.append("For what: "+response.getType()+"\n");
                           binding.dataStatus.append("How many members: "+response.getParticipants()+"\n");
                           binding.dataStatus.append("Per member price: "+response.getPrice()+"\n");
                       } catch (Exception e) {
                           throw new RuntimeException(e);
                       }
                   }


                }

            }
        });
    }

}