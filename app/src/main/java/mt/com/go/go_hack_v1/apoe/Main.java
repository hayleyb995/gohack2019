package mt.com.go.go_hack_v1.apoe;

import android.graphics.PointF;

import mt.com.go.go_hack_v1.apoe.model.plan.Material;
import mt.com.go.go_hack_v1.apoe.model.plan.Point;
import mt.com.go.go_hack_v1.apoe.model.plan.UiWall;
import mt.com.go.go_hack_v1.apoe.model.plan.Wall;

public class Main {

    public static void main(String[] params) {
        System.out.println("hello");

        new OptimizationEngine(getTestingUIWalls()).getOptimalSolution(getTestingUIWalls());
    }

    private static Wall[] getTestingUIWalls() {

        Wall wall1 = new UiWall(new PointF(5, 5), new PointF(15, 5), Material.CONCRETE, 50);

        Wall wall2 = new UiWall(new PointF(15, 5), new PointF(15, 15), Material.CONCRETE, 50);

        Wall wall3 = new UiWall(new PointF(15, 15), new PointF(5, 15), Material.CONCRETE, 50);

        Wall wall4 = new UiWall(new PointF(5, 15), new PointF(5, 5), Material.CONCRETE, 50);

        Wall wall5 = new UiWall(new PointF(8, 10), new PointF(8, 5), Material.CONCRETE, 50);

        Wall walls[] = new UiWall[4];
        walls[0] = wall1;
        walls[1] = wall2;
        walls[2] = wall3;
        walls[3] = wall4;
        walls[4] = wall5;

        return walls;
    }
}
