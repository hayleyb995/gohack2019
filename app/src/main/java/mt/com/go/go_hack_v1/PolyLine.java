package mt.com.go.go_hack_v1;

import java.util.ArrayList;
import java.util.List;

public class PolyLine {
    private List<Coordinate> coordinates = new ArrayList<>();
    private int thickness;
    private Material material;

    public PolyLine(Coordinate startingPoint, Coordinate endPoint, int thickness, Material material) {
        coordinates.add(startingPoint);
        coordinates.add(endPoint);
        this.thickness = thickness;
        this.material = material;
    }

    public enum Material {
        CONCRETE,
        LIMESTONE,
        WOOD,
        GLASS
    }
}



