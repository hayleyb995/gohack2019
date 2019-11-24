package mt.com.go.go_hack_v1;

public class BoundingBox {
    float minX = 0;
    float maxX = 0;
    float minY = 0;
    float maxY = 0;

    public BoundingBox(float minX, float maxX, float minY, float maxY) {
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
    }

    public float getMinX() {
        return minX;
    }

    public float getMaxX() {
        return maxX;
    }

    public float getMinY() {
        return minY;
    }

    public float getMaxY() {
        return maxY;
    }
}
