package mt.com.go.go_hack_v1.apoe.model.grid;

public class GridCell {

    private GridPoint gridPoint;
    private boolean usable;

    public GridCell(GridPoint gridPoint, boolean usable) {
        this.gridPoint = gridPoint;
        this.usable = usable;
    }

    public GridPoint getGridPoint() {
        return gridPoint;
    }

    public boolean isUsable() {
        return usable;
    }

    public void setToUsable() {
        usable = true;
    }

    public void setToUnUsable() {
        usable = false;
    }

    @Override
    public String toString() {
        return "GridCell{" +
                "gridPoint=" + gridPoint +
                '}';
    }
}