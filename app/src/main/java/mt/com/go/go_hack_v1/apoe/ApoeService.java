package mt.com.go.go_hack_v1.apoe;

import mt.com.go.go_hack_v1.apoe.model.plan.UiWall;
import mt.com.go.go_hack_v1.apoe.model.recommendation.Recommendation;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApoeService {

    @POST("plan/optimize")
    Call<Recommendation> optimizePlan(@Body UiWall[] uiWalls);

}
