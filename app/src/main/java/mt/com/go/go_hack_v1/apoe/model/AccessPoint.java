package mt.com.go.go_hack_v1.apoe.model;

public class AccessPoint {

    private GridPoint currentGridPoint;
    private float antennaGain = 3;
    private float transmitPower = 0.4f;

    public AccessPoint(GridPoint currentGridPoint) {
        this.currentGridPoint = currentGridPoint;
    }

    public float getAntennaGain() {
        return antennaGain;
    }

    public float getTransmitPower() {
        return transmitPower;
    }

    public GridPoint getCurrentGridPoint() {
        return currentGridPoint;
    }

    public void setCurrentGridPoint(GridPoint gridPoint) {
        this.currentGridPoint = gridPoint;
    }

}
