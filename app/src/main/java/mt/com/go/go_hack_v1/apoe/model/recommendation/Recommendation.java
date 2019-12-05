package mt.com.go.go_hack_v1.apoe.model.recommendation;

import mt.com.go.go_hack_v1.apoe.model.AccessPoint;

public class Recommendation {

    private AccessPoint[] accessPoints;
    private double[][] signalStrengthHeatMap;

    public Recommendation(AccessPoint[] accessPoints, double[][] signalStrengthHeatMap) {
        this.accessPoints = accessPoints;
        this.signalStrengthHeatMap = signalStrengthHeatMap;
    }

    public AccessPoint[] getAccessPoints() {
        return accessPoints;
    }

    public double[][] getSignalStrengthHeatMap() {
        return signalStrengthHeatMap;
    }

    public void setAccessPoints(AccessPoint[] accessPoints) {
        this.accessPoints = accessPoints;
    }

    public void setSignalStrengthHeatMap(double[][] signalStrengthHeatMap) {
        this.signalStrengthHeatMap = signalStrengthHeatMap;
    }
}
