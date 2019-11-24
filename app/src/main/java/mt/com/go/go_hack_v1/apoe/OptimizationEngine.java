package mt.com.go.go_hack_v1.apoe;

import mt.com.go.go_hack_v1.apoe.engineering.PathLossModel;
import mt.com.go.go_hack_v1.apoe.model.*;
import mt.com.go.go_hack_v1.apoe.model.grid.GridPoint;
import mt.com.go.go_hack_v1.apoe.model.grid.Grid;
import mt.com.go.go_hack_v1.apoe.model.grid.Gridster;
import mt.com.go.go_hack_v1.apoe.model.plan.GridWall;
import mt.com.go.go_hack_v1.apoe.model.plan.UiWall;
import mt.com.go.go_hack_v1.apoe.model.plan.Wall;
import mt.com.go.go_hack_v1.apoe.model.recommendation.EmptyRecommendation;
import mt.com.go.go_hack_v1.apoe.model.recommendation.Recommendation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class OptimizationEngine implements Runnable {

    private static final int MAX_STEPS = 100;
    private static final int MAX_ACCESS_POINTS = 5;
    private static final float AVERAGE_DECIBEL_THRESHOLD = -40;
    private static final int GRID_CELL_SIZE = 20; //This is in cm
    private static final float UI_SCALE_FACTOR = 0.02f;

    private Wall[] walls;
    public Recommendation recommendation;

    public OptimizationEngine(Wall[] walls) {
        this.walls = walls;
    }

    public Recommendation getOptimalSolution(Wall[] uiWalls) {
        Wall[] gridWalls = convertToGridWalls(uiWalls);

        PathLossModel pathLossModel = new PathLossModel(GRID_CELL_SIZE);
        PathLossModel.PathLossModelCache pathLossHeatMap = pathLossModel.generateCache(gridWalls);
        Grid usabilityGrid = new Gridster(GRID_CELL_SIZE).generateUsabilityGrid(gridWalls);

        for (int i = 0; i < pathLossHeatMap.cache.length; i++) {
            for (int j = 0; j < pathLossHeatMap.cache[0].length; j++) {
                System.out.print(pathLossHeatMap.cache[i][j] + ", ");
            }
            System.out.println();
        }

        int accessPointCount = 0;

        do {
            accessPointCount++;

            AccessPoint[] accessPoints = randomlyPlaceAccessPoints(usabilityGrid, accessPointCount);
            int step = 0;

            while (step < MAX_STEPS) {
                double[][] signalStrengthHeatMap = pathLossModel.generateHeatMap(pathLossHeatMap, accessPoints, false);

                GridPoint gridPoint = getMostAttractiveGridPoint(usabilityGrid, signalStrengthHeatMap);
                AccessPoint accessPoint = getBestAccessPointToMove(signalStrengthHeatMap, gridPoint, accessPoints);

                accessPoint.moveTowards(signalStrengthHeatMap.length, signalStrengthHeatMap[0].length, gridPoint);

                System.out.println(step);

                for (int i = 0; i < signalStrengthHeatMap.length; i++) {
                    for (int j = 0; j < signalStrengthHeatMap[0].length; j++) {
                        System.out.print(signalStrengthHeatMap[i][j] + ", ");
                    }
                    System.out.println();
                }

                if (getAreaCoverage(usabilityGrid, signalStrengthHeatMap) >= AVERAGE_DECIBEL_THRESHOLD) {
                    System.out.println("Found a solution!!!");
                    return new Recommendation(accessPoints, signalStrengthHeatMap);
                }

                step++;
            }
        } while (accessPointCount < MAX_ACCESS_POINTS);

        return new EmptyRecommendation();
    }

    private Wall[] convertToGridWalls(Wall[] walls) {
        if (walls == null) {
            return new Wall[0];
        }

        Wall[] gridWalls = new GridWall[walls.length];

        for (int i = 0; i < walls.length; i++) {
            Wall wall = walls[i];

            if (wall instanceof UiWall) {
                UiWall uiWall = (UiWall) wall;

                GridPoint startGridPoint = new GridPoint(
                        (int) ((uiWall.getStart().y * 100 * UI_SCALE_FACTOR) / GRID_CELL_SIZE),
                        (int) ((uiWall.getStart().x * 100) * UI_SCALE_FACTOR / GRID_CELL_SIZE));

                GridPoint endGridPoint = new GridPoint(
                        (int) ((uiWall.getEnd().y * 100) * UI_SCALE_FACTOR / GRID_CELL_SIZE),
                        (int) ((uiWall.getEnd().x * 100) * UI_SCALE_FACTOR / GRID_CELL_SIZE));

                gridWalls[i] = new GridWall(startGridPoint, endGridPoint, uiWall.getMaterial(), uiWall.getThickness());
            } else {
                gridWalls[i] = wall;
            }
        }

        return gridWalls;
    }

    private AccessPoint[] randomlyPlaceAccessPoints(Grid usabilityGrid, int accessPointCount) {
        List<AccessPoint> accessPoints = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < accessPointCount; i++) {
            while (true) { //Pray to God
                int x = random.nextInt(usabilityGrid.getRows());
                int y = random.nextInt(usabilityGrid.getColumns());

                if (usabilityGrid.getGridCells()[x][y].isUsable()) {
                    accessPoints.add(new AccessPoint(new GridPoint(x, y)));
                    break;
                }
            }
        }

        return accessPoints.toArray(new AccessPoint[0]);
    }

    private GridPoint getMostAttractiveGridPoint(Grid usabilityGrid, double[][] signalStrengthHeatMap) {
        double lowestDecibel = Double.MAX_VALUE;
        GridPoint gridPoint = new GridPoint(0, 0);

        for (int i = 0; i < signalStrengthHeatMap.length; i++) {
            for (int j = 0; j < signalStrengthHeatMap[0].length; j++) {
                if (usabilityGrid.getGridCells()[i][j].isUsable() && signalStrengthHeatMap[i][j] < lowestDecibel) {
                    gridPoint.setRow(i).setColumn(j);
                    lowestDecibel = signalStrengthHeatMap[i][j];
                }
            }
        }


        return gridPoint;
    }

    private double getAreaCoverage(Grid usabilityGrid, double[][] signalStrengthHeatMap) {
        float sum = 0;
        int usableGridCells = 0;

        for (int i = 0; i < signalStrengthHeatMap.length; i++) {
            for (int j = 0; j < signalStrengthHeatMap[0].length; j++) {
                if (usabilityGrid.getGridCells()[i][j].isUsable()) {
                    sum += signalStrengthHeatMap[i][j];
                    usableGridCells++;
                }
            }
        }

        float average = sum / usableGridCells;

        return average;
    }

    private AccessPoint getBestAccessPointToMove(double[][] signalStrengthMap, GridPoint
            gridPoint, AccessPoint[] accessPoints) {
        if (accessPoints.length == 0) {
            return null;
        }

        float lowestSum = Float.MAX_VALUE;
        AccessPoint bestAccessPoint = accessPoints[0];

        for (AccessPoint accessPoint : accessPoints) {
            List<GridPoint> gridPoints = Grid.findLine(
                    accessPoint.getGridPoint().getColumn(),
                    accessPoint.getGridPoint().getRow(),
                    gridPoint.getColumn(),
                    gridPoint.getRow());

            float currentSum = 0;
            for (GridPoint gp : gridPoints) {
                currentSum += signalStrengthMap[gp.getColumn()][gp.getRow()];
            }

            if (currentSum < lowestSum) {
                bestAccessPoint = accessPoint;
                lowestSum = currentSum;
            }
        }

        return bestAccessPoint;
    }


    @Override
    public void run() {
        this.recommendation = getOptimalSolution(this.walls);
    }
}