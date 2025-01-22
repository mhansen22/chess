package chess;

import java.util.Collection;
import java.util.ArrayList;

public abstract class PieceMovesCalculator {

    public abstract Collection<ChessMove> piecesMove(ChessBoard board, ChessPosition position);

}

class BishopMovesCalculator extends PieceMovesCalculator {

    @Override
    public Collection<ChessMove> piecesMove(ChessBoard board, ChessPosition position) {
        Collection<ChessMove> moves = new ArrayList<>();
        return moves;
    }
}
