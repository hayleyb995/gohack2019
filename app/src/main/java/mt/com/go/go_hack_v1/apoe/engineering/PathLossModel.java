package mt.com.go.go_hack_v1.apoe.engineering;

import mt.com.go.go_hack_v1.apoe.model.AccessPoint;
import mt.com.go.go_hack_v1.apoe.model.grid.GridPoint;
import mt.com.go.go_hack_v1.apoe.model.grid.Gridster;
import mt.com.go.go_hack_v1.apoe.model.plan.GridWall;
import mt.com.go.go_hack_v1.apoe.model.plan.Wall;

import java.util.Arrays;

public class PathLossModel {

    private final int gridCellSize;

    public PathLossModel(int gridCellSize) {
        this.gridCellSize = gridCellSize;
    }

    public void findLine(PathLossModel.PathLossModelCache cache, int x1, int y1, int x2, int y2, float loss) {
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);

        int sx = x1 < x2 ? 1 : -1;
        int sy = y1 < y2 ? 1 : -1;

        int err = dx - dy;
        int e2;

        while (true) {
            cache.cache[x1][y1] = loss;

            if (x1 == x2 && y1 == y2)
                break;

            e2 = 2 * err;
            if (e2 > -dy) {
                err = err - dy;
                x1 = x1 + sx;
            }

            if (e2 < dx) {
                err = err + dx;
                y1 = y1 + sy;
            }
        }
    }

    public double findLoss(PathLossModel.PathLossModelCache cache, final int X1, final int Y1, final int x2, final int y2)
    {
        int x1 = X1;
        int y1 = Y1;

        if(x1 > cache.cache.length || x2 > cache.cache.length) System.out.println("Out of bound X");
        if(y1 > cache.cache[0].length || y2 > cache.cache[0].length) System.out.println("Out of bound Y");

        final int dx = Math.abs(x2 - x1);
        final int dy = Math.abs(y2 - y1);


        final int sx = x1 < x2 ? 1 : -1;
        final int sy = y1 < y2 ? 1 : -1;

        int err = dx-dy;
        int e2;
        double totalLoss = 0;

        while (true)
        {
            totalLoss += cache.cache[x1][y1];

            if (x1 == x2 && y1 == y2)
                break;

            e2 = 2 * err;
            if (e2 > -dy)
            {
                err = err - dy;
                x1 = x1 + sx;
            }

            if (e2 < dx)
            {
                err = err + dx;
                y1 = y1 + sy;
            }
        }
        return totalLoss;
    }

    double CalculateRxPower (double Distance, double WallLoss, double TransmitPower, boolean Is24GHz, double AntennaGain){

        double distance = Distance;
        double antennaGainAP = AntennaGain;
        double txPower = TransmitPower;
        double antennaGainDevice = 0;
        double wallLoss = WallLoss;
        double frequency = (Is24GHz)? 2400 : 5000;
        double gains = txPower + antennaGainDevice + antennaGainAP;
        double powerLossCoefficient = 28;

        double PathLoss = 20 * Math.log10(frequency) + powerLossCoefficient * Math.log10(distance) + wallLoss - powerLossCoefficient;

        return gains - PathLoss;

    }

    public PathLossModel.PathLossModelCache generateCache(Wall walls[]) {
        GridPoint gridPoint = new Gridster(gridCellSize).getGridDimensions(walls);

        PathLossModel.PathLossModelCache cache = new PathLossModel.PathLossModelCache(gridPoint.getRow(), gridPoint.getColumn());

        //loss is currently set as a fixed value (10)
        for (Wall wall : walls) {
            GridWall gridWall = (GridWall) wall;
            findLine(
                    cache,
                    gridWall.getGridPointStart().getRow(),
                    gridWall.getGridPointStart().getColumn(),
                    gridWall.getGridPointEnd().getRow(),
                    gridWall.getGridPointEnd().getColumn(),
                    10.0f);
        }

        return cache;
    }

    public class PathLossModelCache {

        public int dim_x;
        public int dim_y;
        public double cache[][];

        public PathLossModelCache(int Dim_x, int Dim_y) {
            dim_x = Dim_x;
            dim_y = Dim_y;

            cache = new double[dim_x][dim_y];
        }
    }

    public double[][] generateHeatMap(PathLossModel.PathLossModelCache cache, AccessPoint APs[], boolean accumulativeHeatMap){
        double[][] heatMap = new double[cache.dim_x][cache.dim_y];
        if(!accumulativeHeatMap) {
            Arrays.stream(heatMap).forEach(a -> Arrays.fill(a, Double.NEGATIVE_INFINITY));
        }
        //Array.fill(heatMap, Double.NEGATIVE_INFINITY);

        for (AccessPoint AP : APs) {
            for (int i = 0; i < cache.dim_x; i++) {
                for (int j = 0; j < cache.dim_y; j++) {
                    if (i == AP.getCurrentGridPoint().getColumn() && j == AP.getCurrentGridPoint().getRow()){
                        if (accumulativeHeatMap) {
                            heatMap[i][j] += AP.getTransmitPower();
                        }
                        else {
                            heatMap[i][j] = Math.max(AP.getTransmitPower(), heatMap[i][j]);
                        }
                        continue;
                    }
                    double distance = Math.sqrt((Math.pow((AP.getCurrentGridPoint().getRow()-i), 2))+(Math.pow(AP.getCurrentGridPoint().getColumn()-j, 2)));
                    double totalLoss = findLoss(cache, (int) (AP.getCurrentGridPoint().getRow()), (int) (AP.getCurrentGridPoint().getColumn()), (i), (j));
                    double recievedPower = Math.round(CalculateRxPower(distance, totalLoss, AP.getTransmitPower(), true, AP.getAntennaGain()));
                    if (accumulativeHeatMap) {
                        heatMap[i][j] += recievedPower;
                    }
                    else {
                        heatMap[i][j] = Math.max(recievedPower, heatMap[i][j]);
                    }
                }
            }
        }
        return heatMap;
    }

}