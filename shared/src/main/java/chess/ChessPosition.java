package chess;

/**
 * Represents a single square position on a chess board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPosition {
    private final int col;
    private final int row;

    public ChessPosition(int row, int col) {
        this.col = col;
        this.row = row;
    }

    /**
     * @return which row this position is in
     * 1 codes for the bottom row
     */
    public int getRow() { return row; }

    /**
     * @return which column this position is in
     * 1 codes for the left row
     */
    public int getColumn() { return col; }

    @Override
    public String toString() { return "(row: " + row + ", col: "+ col + ")"; }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof ChessPosition comp)) {
            return false;
        }
        return this.row == comp.row && this.col == comp.col;
    }

//    @Override
//    public int hashcode() {
//        return 5 * col + row;
//    }
}
