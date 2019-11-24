package mt.com.go.go_hack_v1;

import android.graphics.PointF;

import java.util.ArrayList;
import java.util.List;

import mt.com.go.go_hack_v1.apoe.model.plan.Material;

public class PolyLine {
    private List<PointF> coordinates = new ArrayList<>();
    private int thickness;
    private Material material;

    public PolyLine(PointF startingPoint, PointF endPoint, int thickness, Material material) {
        coordinates.add(startingPoint);
        coordinates.add(endPoint);
        this.thickness = thickness;
        this.material = material;
    }

    public List<PointF> getCoordinates() {
        return this.coordinates;
    }

    public int getThickness() {
        return this.thickness;
    }

    public Material getMaterial() {
        return this.material;
    }
}



