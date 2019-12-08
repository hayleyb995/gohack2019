package mt.com.go.go_hack_v1.apoe.model;

import mt.com.go.go_hack_v1.apoe.model.grid.GridPoint;

public class AccessPoint {

    private GridPoint currentGridPoint;
    private float antennaGain;
    private float transmitPower;

    public float getAntennaGain() {
        return antennaGain;
    }

    public float getTransmitPower() {
        return transmitPower;
    }

    public GridPoint getCurrentGridPoint() {
        return currentGridPoint;
    }

    public void setCurrentGridPoint(GridPoint currentGridPoint) {
        this.currentGridPoint = currentGridPoint;
    }

    public void setAntennaGain(float antennaGain) {
        this.antennaGain = antennaGain;
    }

    public void setTransmitPower(float transmitPower) {
        this.transmitPower = transmitPower;
    }
}
