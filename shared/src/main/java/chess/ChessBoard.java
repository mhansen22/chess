package chess;

import java.util.Objects;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {
    private ChessPiece[][] squares = new ChessPiece[8][8];
    public ChessBoard() {
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        //array is 0-7 index, chess board is 1-8
        //use this to transfer
        squares[position.getRow() - 1][position.getColumn() - 1] = piece;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        //array is 0-7 index, chess board is 1-8
        //use this to transfer
        return squares[position.getRow() - 1][position.getColumn() - 1];
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        //board created at top of class^^^

        //then add pieces in correct spot
        //white pawns
        for (int y = 0; y < 8; y++) {
            squares[1][y] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        }
        //black pawns
        for (int y = 0; y < 8; y++) {
            squares[6][y] = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.PAWN);
        }
        //white pieces specialty:
        squares[0][0] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.ROOK);
        squares[0][1] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KNIGHT);
        squares[0][2] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.BISHOP);
        squares[0][3] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.QUEEN);
        squares[0][4] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KING);
        squares[0][5] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.BISHOP);
        squares[0][6] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KNIGHT);
        squares[0][7] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.ROOK);
        //black pieces specialty:
        squares[7][0] = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.ROOK);
        squares[7][1] = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KNIGHT);
        squares[7][2] = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.BISHOP);
        squares[7][3] = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.QUEEN);
        squares[7][4] = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KING);
        squares[7][5] = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.BISHOP);
        squares[7][6] = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KNIGHT);
        squares[7][7] = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.ROOK);
    }

    //override hashcode and equals
    @Override
    public int hashCode() {
        int result = 5;
        //set for each piece on the baord
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                ChessPiece piece = squares[x][y];
                result = 13 * 5 + piece.hashCode();
            }
        }
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
        ChessBoard that = (ChessBoard) obj;

        //same for override, change starts here
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                ChessPiece piece = squares[x][y];
                ChessPiece thatPiece = that.squares[x][y];

                if ((piece != null && !piece.equals(thatPiece)) || (piece == null && thatPiece != null)) {
                    return false;
                }
            }
        }
        return true;
    }
}
