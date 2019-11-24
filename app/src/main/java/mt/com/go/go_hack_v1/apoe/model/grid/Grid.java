package mt.com.go.go_hack_v1.apoe.model.grid;

import java.util.ArrayList;
import java.util.List;

public class Grid {

    private GridCell[][] gridCells;

    public Grid(GridCell[][] gridCells) {
        this.gridCells = gridCells;
    }

    public GridCell[][] getGridCells() {
        return gridCells;
    }

    public int getRows() {
        return gridCells.length;
    }

    public int getColumns() {
        return getRows() == 0 ? 0 : gridCells[0].length;
    }

    public boolean isNotOutOfBounds(int row, int column) {
        return row >= 0 && column >= 0 && row < getRows() && column < getColumns();
    }

    public static List<GridPoint> findLine(int x0, int y0, int x1, int y1) {
        List<GridPoint> line = new ArrayList<>();

        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);

        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;

        int err = dx - dy;
        int e2;

        while (true) {
            line.add(new GridPoint(y0, x0));

            if (x0 == x1 && y0 == y1)
                break;

            e2 = 2 * err;
            if (e2 > -dy) {
                err = err - dy;
                x0 = x0 + sx;
            }

            if (e2 < dx) {
                err = err + dx;
                y0 = y0 + sy;
            }
        }
        return line;
    }
}