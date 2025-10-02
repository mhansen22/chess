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
    private final ChessPiece.PieceType type;

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
    public ChessGame.TeamColor getTeamColor() { return pieceColor; }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() { return type; }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        PieceType pieceType = getPieceType();
        return switch (pieceType) {
            case BISHOP -> {
                PieceMovesCalculator calculator = new BishopMovesCalculator();
                yield calculator.calcMoves(board, myPosition);
            }
            case KING -> {
                PieceMovesCalculator calculator2 = new KingMovesCalculator();
                yield calculator2.calcMoves(board, myPosition);
            }
            case KNIGHT -> {
                PieceMovesCalculator calculator3 = new KnightMovesCalculator();
                yield calculator3.calcMoves(board, myPosition);
            }
            case PAWN -> {
                PieceMovesCalculator calculator4 = new PawnMovesCalculator();
                yield calculator4.calcMoves(board, myPosition);
            }
            case QUEEN -> {
                PieceMovesCalculator calculator5 = new QueenMovesCalculator();
                yield calculator5.calcMoves(board, myPosition);
            }
            case ROOK -> {
                PieceMovesCalculator calculator6 = new RookMovesCalculator();
                yield calculator6.calcMoves(board, myPosition);
            }
        };

    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof ChessPiece comp)) {
            return false;
        }
        return type == comp.type && pieceColor == comp.pieceColor;
    }

    @Override
    public int hashCode() {
        return 31 * pieceColor.hashCode() + type.hashCode();
    }
}
