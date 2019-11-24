package mt.com.go.go_hack_v1.apoe.model;

import mt.com.go.go_hack_v1.apoe.model.grid.Movement;
import mt.com.go.go_hack_v1.apoe.model.grid.GridPoint;

public class AccessPoint implements Movement {

    private static final float TOP_LEFT_RADIANS = (float) ((5 * Math.PI) / 4);
    private static final float TOP_RIGHT_RADIANS = (float) ((7 * Math.PI) / 4);
    private static final float BOTTOM_RIGHT_RADIANS = (float) (Math.PI / 4);
    private static final float BOTTOM_LEFT_RADIANS = (float) ((3 * Math.PI) / 4);

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

    public void moveTowards(int rowCount, int columnCount, GridPoint attractiveGridPoint) {
        float deltaRow = this.currentGridPoint.getRow() - attractiveGridPoint.getColumn();
        float deltaColumn = this.currentGridPoint.getColumn() - attractiveGridPoint.getRow();

        float absDeltaRow = Math.abs(deltaRow);
        float absDeltaColumn = Math.abs(deltaColumn);

        if( absDeltaColumn >= absDeltaRow && deltaColumn >= 0 ) {
            moveLeft(this.currentGridPoint);
        } else if(absDeltaColumn >= absDeltaRow && deltaColumn <= 0) {
            moveRight(columnCount, this.currentGridPoint);
        } else if(absDeltaRow >= absDeltaColumn && deltaRow >= 0) {
            moveUp(this.currentGridPoint);
        } else if(absDeltaRow >= absDeltaColumn && deltaRow <= 0) {
            moveDown(rowCount, this.currentGridPoint);
        }
    }

}
