package chess;

import java.util.Collection;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {
    private final ChessGame.TeamColor pieceColor;
    private final PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        PieceType pieceType = getPieceType();
        //check what each type is
        //probably a more efficient way than craeting new ones each time
        switch (pieceType) {
            case BISHOP:
                PieceMovesCalculator calculator = new BishopMovesCalculator();
                return calculator.piecesMove(board, myPosition);
            case KING:
                PieceMovesCalculator calculator2 = new KingMovesCalculator();
                return calculator2.piecesMove(board, myPosition);
            case KNIGHT:
                PieceMovesCalculator calculator3 = new KnightMovesCalculator();
                return calculator3.piecesMove(board, myPosition);
            case PAWN:
                PieceMovesCalculator calculator4 = new PawnMovesCalculator();
                return calculator4.piecesMove(board, myPosition);
            case QUEEN:
                PieceMovesCalculator calculator5 = new QueenMovesCalculator();
                return calculator5.piecesMove(board, myPosition);
            case ROOK:
                PieceMovesCalculator calculator6 = new RookMovesCalculator();
                return calculator6.piecesMove(board, myPosition);
        }
        return null;
    }

    @Override
    public int hashCode() {
        int result = 7;
        result = result * 13 + pieceColor.hashCode();
        result = result * 13 + type.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        //change here
        ChessPiece that = (ChessPiece) obj;
        return pieceColor == that.pieceColor && type == that.type;
    }
}