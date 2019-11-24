package mt.com.go.go_hack_v1.apoe.model.grid;

public class GridPoint {

    private int row;
    private int column;

    public GridPoint(int row, int column) {
        this.row = row;
        this.column = column;
    }

    public GridPoint setRow(int row) {
        this.row = row;
        return this;
    }

    public GridPoint setColumn(int column) {
        this.column = column;
        return this;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

}
