package mt.com.go.go_hack_v1.apoe.model.plan;

public class GridWall extends Wall {

    private GridPoint gridPointStart;
    private GridPoint gridPointEnd;

    public GridWall(GridPoint gridPointStart, GridPoint gridPointEnd, Material material, int thickness) {
        super(material, thickness);

        this.gridPointStart = gridPointStart;
        this.gridPointEnd = gridPointEnd;
    }

    public GridPoint getGridPointStart() {
        return gridPointStart;
    }
    
    public GridPoint getGridPointEnd() {
        return gridPointEnd;
    }
    
    

}
