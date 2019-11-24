package mt.com.go.go_hack_v1.apoe.model.grid;

public interface Movement {

    default void moveUp(GridPoint gridPoint) {
        int row = gridPoint.getRow() <= 0 ? 0 : gridPoint.getRow() - 1;
        gridPoint.setRow(row);
    }

    default void moveDown(int rowCount, GridPoint gridPoint) {
        int row = gridPoint.getRow() > rowCount ? gridPoint.getRow() : gridPoint.getRow() + 1;
        gridPoint.setRow(row);
    }

    default void moveRight(int columnCount, GridPoint currentPosition) {
        int column = currentPosition.getColumn() >= columnCount - 1 ? currentPosition.getColumn() - 1 : currentPosition.getColumn() + 1;
        currentPosition.setColumn(column);
    }

    default void moveLeft(GridPoint currentPosition) {
        int column = currentPosition.getColumn() <= 0 ? 0 : currentPosition.getColumn() - 1;
        currentPosition.setColumn(column);
    }

}