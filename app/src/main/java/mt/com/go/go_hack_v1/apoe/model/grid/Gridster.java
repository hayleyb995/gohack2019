package mt.com.go.go_hack_v1.apoe.model.grid;

import java.util.List;
import java.util.Stack;

import mt.com.go.go_hack_v1.apoe.model.plan.GridWall;
import mt.com.go.go_hack_v1.apoe.model.plan.Wall;

public class Gridster {

    private final int _resolution;

    public Gridster(int resolution) {
        _resolution = resolution;
    }

    public Grid generateUsabilityGrid(Wall[] walls) {
        Grid grid = convertWallsToGridCells(walls);
        GridCell [][] gridCells = grid.getGridCells();

        printWallCellGrid(gridCells);

        Stack<GridCell> stack = new Stack<>();
        stack.add(gridCells[0][0]);

        while(!stack.isEmpty()) {
            GridCell gridCell = stack.pop();
            if(gridCell.isVisited()){
                continue;
            }
            gridCell.setVisited(true);
            gridCell.setUsable(false);

            int row = gridCell.getGridPoint().getRow();
            int column = gridCell.getGridPoint().getColumn();

            if(grid.isNotOutOfBounds(row, column + 1) && gridCells[row][column + 1].isNotAWall()){
                stack.push(gridCells[row][column + 1]);
            }
            if(grid.isNotOutOfBounds(row + 1, column + 1) && gridCells[row + 1][column + 1].isNotAWall()) {
                stack.push(gridCells[row + 1][column + 1]);
            }
            if(grid.isNotOutOfBounds(row + 1, column) && gridCells[row + 1][column].isNotAWall()) {
                stack.push(gridCells[row + 1][column]);
            }
            if(grid.isNotOutOfBounds(row + 1, column - 1) && gridCells[row + 1][column - 1].isNotAWall()) {
                stack.push(gridCells[row + 1][column - 1]);
            }
            if (grid.isNotOutOfBounds(row, column - 1) && gridCells[row][column - 1].isNotAWall()) {
                stack.push(gridCells[row][column - 1]);
            }
            if (grid.isNotOutOfBounds(row - 1, column - 1) && gridCells[row - 1][column - 1].isNotAWall()){
                stack.push(gridCells[row - 1][column - 1]);
            }
            if (grid.isNotOutOfBounds(row - 1, column) && gridCells[row - 1][column].isNotAWall()) {
                stack.push(gridCells[row - 1][column]);
            }
            if (grid.isNotOutOfBounds(row - 1, column + 1) && gridCells[row - 1][column + 1].isNotAWall()) {
                stack.push(gridCells[row - 1][column + 1]);
            }
        }

        printUsabilityCellGrid(gridCells);

        return grid;
    }

    private Grid convertWallsToGridCells(Wall[] walls) {
        GridPoint dimensions = getGridDimensions(walls);
        GridCell[][] gridCells = new GridCell[dimensions.getRow()][dimensions.getColumn()];

        for (int i = 0; i < gridCells.length; i++) {
            for (int j = 0; j < gridCells[0].length; j++) {
                gridCells[i][j] = new GridCell(new GridPoint(i, j));
            }
        }

        Grid grid = new Grid(gridCells);

        for (Wall wall : walls) {
            GridWall gridWall = (GridWall) wall;

            List<GridPoint> gridPoints = Grid.findLine(
                    gridWall.getGridPointStart().getColumn(),
                    gridWall.getGridPointStart().getRow(),
                    gridWall.getGridPointEnd().getColumn(),
                    gridWall.getGridPointEnd().getRow());
            gridPoints.parallelStream()
                    .forEach(gridPoint -> grid.getGridCells()[gridPoint.getRow()][gridPoint.getColumn()].setWall(true).setUsable(false));
        }

        return grid;
    }

    public void printWallCellGrid(GridCell[][] gridCells) {
        int rows = gridCells.length;
        int columns = gridCells[0].length;

        for(int i=0; i < rows; i++ ) {
            for(int k=0; k < columns; k++ ) {
                if(gridCells[i][k].isNotAWall()) {
                    System.out.print('0');
                } else {
                    System.out.print('1');
                }
            }
            System.out.println();
        }
        System.out.println();
    }

    public void printUsabilityCellGrid(GridCell[][] gridCells) {
        int rows = gridCells.length;
        int columns = gridCells[0].length;

        for(int i=0; i < rows; i++ ) {
            for(int k=0; k < columns; k++ ) {
                if(gridCells[i][k].isUsable()) {
                    System.out.print('1');
                } else {
                    System.out.print('0');
                }
            }
            System.out.println();
        }

        System.out.println();
    }

    public GridPoint getGridDimensions(Wall walls[]){
        float min_x = Float.MAX_VALUE;
        float min_y = Float.MAX_VALUE;
        float max_x = Float.MIN_VALUE;
        float max_y = Float.MIN_VALUE;

        for (Wall wall : walls) {
            GridWall gridWall = (GridWall) wall;

            float x1 = gridWall.getGridPointStart().getColumn();
            float y1 = gridWall.getGridPointStart().getRow();

            float x2 = gridWall.getGridPointEnd().getColumn();
            float y2 = gridWall.getGridPointEnd().getRow();

            if (x1 < min_x){ min_x = x1; }
            if (y1 < min_y){ min_y = y1; }
            if (x2 < min_x){ min_x = x2; }
            if (y2 < min_y){ min_y = y2; }

            if (x1 > max_x){ max_x = x1; }
            if (y1 > max_y){ max_y = y1; }
            if (x2 > max_x){ max_x = x2; }
            if (y2 > max_y){ max_y = y2; }
        }

        int dim_x = (int) Math.ceil((max_x+min_x));
        int dim_y = (int) Math.ceil((max_y+min_y));

        return new GridPoint(dim_y, dim_x);
    }

}
