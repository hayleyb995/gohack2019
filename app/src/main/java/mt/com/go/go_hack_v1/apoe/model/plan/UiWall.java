package mt.com.go.go_hack_v1.apoe.model.plan;

import android.graphics.PointF;

import java.io.Serializable;

public class UiWall extends Wall implements Serializable {

    private PointF start;
    private PointF end;

    public UiWall(PointF start, PointF end, Material material, int thickness) {
        super(material, thickness);

        this.start = start;
        this.end = end;
    }

    public PointF getStart() {
        return start;
    }

    public PointF getEnd() {
        return end;
    }

}
