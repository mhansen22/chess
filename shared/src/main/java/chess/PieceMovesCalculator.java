package chess;

import java.util.Collection;
import java.util.ArrayList;

public abstract class PieceMovesCalculator {

    public abstract Collection<ChessMove> piecesMove(ChessBoard board, ChessPosition position);

}

class BishopMovesCalculator extends PieceMovesCalculator {
    //so this inherets from piecemovescalcultor!!

    @Override
    public Collection<ChessMove> piecesMove(ChessBoard board, ChessPosition position) {
        //create new ArrayList for valid moves
        Collection<ChessMove> moves = new ArrayList<>();

        /*bishops can move diagonal ONLY*/
        int[] x = {-1, -1, 1, 1};
        int[] y = {-1, 1, -1, 1};

        /*loop through 4 diagonal directions  */
        for (int i = 0; i < 4; i++) {
            int curr_x = position.getRow();
            int curr_y = position.getColumn();
            /*move diagonal until it hits the end of the board or hits another piece*/
            while (true) {
                curr_x += x[i];
                curr_y += y[i];
                /*end of board is anything less than 0 and greater than 7*/
                if (curr_x < 0 || curr_x > 7 || curr_y < 0 || curr_y > 7) {
                    break;
                }

                ChessPosition curr_position = new ChessPosition(curr_x, curr_y);
                ChessPiece piece_curr_pos = board.getPiece(curr_position);
                if (piece_curr_pos == null) {
                    moves.add(new ChessMove(position, curr_position, null));
                } else {
                    if (piece_curr_pos.getTeamColor() != board.getPiece(position).getTeamColor()) {
                        moves.add(new ChessMove(position, curr_position, null));
                    }
                    break;
                }
            }
        }
        return moves;
    }
}
