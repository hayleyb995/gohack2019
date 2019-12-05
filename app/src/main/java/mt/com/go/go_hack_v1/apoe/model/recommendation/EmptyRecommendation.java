package mt.com.go.go_hack_v1.apoe.model.recommendation;


import mt.com.go.go_hack_v1.apoe.model.AccessPoint;

public class EmptyRecommendation extends Recommendation {

    public EmptyRecommendation() {
        super(new AccessPoint[0], new double[0][0]);
    }

}
