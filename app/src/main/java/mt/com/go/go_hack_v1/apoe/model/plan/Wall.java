package mt.com.go.go_hack_v1.apoe.model.plan;

public abstract class Wall {

    private Material material;
    private int thickness;

    protected Wall(Material material, int thickness) {
        this.material = material;
        this.thickness = thickness;
    }

    public Material getMaterial() {
        return material;
    }

    public int getThickness() {
        return thickness;
    }

}
