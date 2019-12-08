package mt.com.go.go_hack_v1.apoe;


import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import mt.com.go.go_hack_v1.DrawingImageView;
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

    public ApoeOptimizePlanController(DrawingImageView view) {
        this.view = view;
    }

    public void start(UiWall[] uiWalls) {
        final OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .readTimeout(60, TimeUnit.SECONDS)
                .connectTimeout(10, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://77.71.136.203:13337")
                .addConverterFactory(JacksonConverterFactory.create())
                .client(okHttpClient)
                .build();

        ApoeService apoeService = retrofit.create(ApoeService.class);
        Call<Recommendation> call = apoeService.optimizePlan(uiWalls);
        call.enqueue(this);
    }

    @Override
    public void onResponse(Call<Recommendation> call, Response<Recommendation> response) {
        Recommendation recommendation = response.body();

        view.setHeatMapGlobal(recommendation.getSignalStrengthHeatMap());
        view.setAps(Arrays.asList(recommendation.getAccessPoints()));

        view.setCurrentState(STATE.READY);
        view.invalidate();
    }

    @Override
    public void onFailure(Call<Recommendation> call, Throwable t) {
        t.printStackTrace();
    }
}
