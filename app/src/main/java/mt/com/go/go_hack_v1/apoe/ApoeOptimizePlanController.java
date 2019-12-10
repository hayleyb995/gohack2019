package mt.com.go.go_hack_v1.apoe;


import android.app.Activity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import mt.com.go.go_hack_v1.DrawingImageView;
import mt.com.go.go_hack_v1.R;
import mt.com.go.go_hack_v1.STATE;
import mt.com.go.go_hack_v1.apoe.model.plan.UiWall;
import mt.com.go.go_hack_v1.apoe.model.recommendation.Recommendation;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class ApoeOptimizePlanController implements Callback<Recommendation> {

    private final DrawingImageView view;
    private final Activity mainActivity;

    public ApoeOptimizePlanController(Activity activity, DrawingImageView view) {
        this.mainActivity = activity;
        this.view = view;
    }

    public void start(UiWall[] uiWalls) {
        final OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .readTimeout(60, TimeUnit.SECONDS)
                .connectTimeout(10, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://130.255.77.253:13337")
                .addConverterFactory(JacksonConverterFactory.create())
                .client(okHttpClient)
                .build();

        ApoeService apoeService = retrofit.create(ApoeService.class);
        Call<Recommendation> call = apoeService.optimizePlan(uiWalls);
        call.enqueue(this);


        RelativeLayout layout = (RelativeLayout)(mainActivity.findViewById(R.id.lo_header));
        layout.setVisibility(View.GONE);

        ProgressBar spinner = (ProgressBar)(mainActivity.findViewById(R.id.progressBar1));
        spinner.setIndeterminate(true);
        spinner.getIndeterminateDrawable().setColorFilter(0xFF008577, android.graphics.PorterDuff.Mode.MULTIPLY);
        spinner.setVisibility(View.VISIBLE);
    }

    @Override
    public void onResponse(Call<Recommendation> call, Response<Recommendation> response) {
        ProgressBar spinner = (ProgressBar)(mainActivity.findViewById(R.id.progressBar1));
        spinner.setVisibility(View.GONE);




        Recommendation recommendation = response.body();

        view.setHeatMapGlobal(recommendation.getSignalStrengthHeatMap());
        view.setAps(Arrays.asList(recommendation.getAccessPoints()));

        view.setCurrentState(STATE.READY);
        view.invalidate();
    }

    @Override
    public void onFailure(Call<Recommendation> call, Throwable t) {
        RelativeLayout layout = (RelativeLayout)(mainActivity.findViewById(R.id.lo_header));
        layout.setVisibility(View.VISIBLE);

        ProgressBar spinner = (ProgressBar)(mainActivity.findViewById(R.id.progressBar1));
        spinner.setVisibility(View.GONE);
        t.printStackTrace();
    }
}
