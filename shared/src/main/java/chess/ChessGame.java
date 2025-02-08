package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static chess.ChessPiece.PieceType.KING;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private ChessBoard board;
    private TeamColor teamColor;

    public ChessGame() {
        this.board = new ChessBoard();
        this.teamColor = TeamColor.WHITE;
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return this.teamColor;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        this.teamColor = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);
        if (piece == null) {
            return null;
        }
        List<ChessMove> moves = new ArrayList<>(piece.pieceMoves(board, startPosition));
        List<ChessMove> correctMoves = new ArrayList<>();
        for (ChessMove move : moves) {
            ChessBoard boardCopy = board.clone();
            boardCopy.addPiece(move.getEndPosition(), boardCopy.getPiece(move.getStartPosition()));
            boardCopy.addPiece(move.getStartPosition(), null);
            ChessGame gameCopy = new ChessGame();
            gameCopy.setBoard(boardCopy);
            if (!gameCopy.isInCheck(teamColor)) {
                correctMoves.add(move);
            }
        }
        return correctMoves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPosition startPosition = move.getStartPosition();
        ChessPosition endPosition = move.getEndPosition();
        ChessPiece piece = board.getPiece(startPosition);
        Collection<ChessMove> validMoves = validMoves(startPosition);
        if (piece == null) {
            throw new InvalidMoveException("There is no piece at that position!");
        }
        if (!validMoves.contains(move)) {
            throw new InvalidMoveException("Not a valid move!");
        }
        board.addPiece(endPosition, piece);
        board.addPiece(startPosition, null);//to remove

        if (piece.getPieceType() == ChessPiece.PieceType.PAWN && (endPosition.getRow() == 1 || endPosition.getRow()== 8)) {
            if (move.getPromotionPiece() != null ) {
                ChessPiece promotion = new ChessPiece(piece.getTeamColor(), move.getPromotionPiece());
                board.addPiece(endPosition, promotion);
            }
        }

        if (getTeamTurn() == TeamColor.WHITE) {
            setTeamTurn(TeamColor.BLACK);
        } else {
            setTeamTurn(TeamColor.WHITE);
        }
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        ChessBoard board = getBoard();
        //find teamColor king position
        ChessPosition kingPosition = null;
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                ChessPosition position = new ChessPosition(x+1, y+1);
                ChessPiece piece = board.getPiece(position);
                if (piece != null && piece.getTeamColor() == teamColor && piece.getPieceType() == KING) {
                    kingPosition = new ChessPosition(x+1, y+1);
                    break;
                }
            }
        }
        //if one of the other teamColor's validMoves lands on the king, return that it is TRUE inCheck
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                ChessPosition position = new ChessPosition(x+1, y+1);
                ChessPiece piece = board.getPiece(position);
                if (piece != null && piece.getTeamColor() != teamColor) {
                    Collection<ChessMove> validMoves = piece.pieceMoves(board, position);
                    for (ChessMove move : validMoves) {
                        if (move.getEndPosition().equals(kingPosition)) {
                            return true;
                        }
                    }
                }
            }
        }
        //otherwise return false
        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        if (!isInCheck(teamColor)) {
            return false;
        }
        ChessBoard board = getBoard();
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                ChessPosition position = new ChessPosition(x+1, y+1);
                ChessPiece piece = board.getPiece(position);
                if (piece != null && piece.getTeamColor() == teamColor) {
                    Collection<ChessMove> validMoves = piece.pieceMoves(board, position);
                    for (ChessMove move : validMoves) {
                        ChessBoard boardCopy = board.clone();
                        ChessPiece movePiece = new ChessPiece(piece.getTeamColor(), piece.getPieceType());
                        boardCopy.addPiece(move.getEndPosition(), movePiece);
                        boardCopy.addPiece(position, null); //remove piece
                        ChessGame gameCopy = new ChessGame();
                        gameCopy.setBoard(boardCopy);
                        if (!gameCopy.isInCheck(teamColor)) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if (isInCheck(teamColor)) {
            return false;
        }
        ChessBoard board = getBoard();
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                ChessPosition position = new ChessPosition(x+1, y+1);
                ChessPiece piece = board.getPiece(position);
                if (piece != null && piece.getTeamColor() == teamColor) {
                    Collection<ChessMove> validMoves = piece.pieceMoves(board, position);
                    for (ChessMove move : validMoves) {
                        ChessBoard boardCopy = board.clone();
                        ChessPiece movePiece = new ChessPiece(piece.getTeamColor(), piece.getPieceType());
                        boardCopy.addPiece(move.getEndPosition(), movePiece);
                        boardCopy.addPiece(position, null); //remove piece

                        ChessGame gameCopy = new ChessGame();
                        gameCopy.setBoard(boardCopy);
                        if (!gameCopy.isInCheck(teamColor)) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }

//    private boolean noValidMoves(TeamColor teamColor) {
//
//    }
}
