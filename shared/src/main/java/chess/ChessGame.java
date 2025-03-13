package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
//for convenience when calling PieceType.KING:
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
    private ChessMove prevMove;


    public ChessGame() {
        this.board = new ChessBoard();
        this.teamColor = TeamColor.WHITE;
        this.board.resetBoard();
        this.prevMove = null;
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
        if (piece == null) return null;
        List<ChessMove> moves = new ArrayList<>(piece.pieceMoves(board, startPosition));
        List<ChessMove> correctMoves = new ArrayList<>();
        for (ChessMove move : moves) {
            ChessBoard boardCopy = board.clone();
            boardCopy.addPiece(move.getEndPosition(), new ChessPiece(piece.getTeamColor(), piece.getPieceType()));
            boardCopy.addPiece(move.getStartPosition(), null);
            ChessGame gameCopy = new ChessGame();
            gameCopy.setBoard(boardCopy);
            gameCopy.setTeamTurn(this.teamColor);
            if (!gameCopy.isInCheck(piece.getTeamColor())) {
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
        //for convenience to use simpler names:
        ChessPosition startPosition = move.getStartPosition();
        ChessPosition endPosition = move.getEndPosition();

        ChessPiece piece = board.getPiece(startPosition);
        if (piece == null) {
            throw new InvalidMoveException("No piece at position!");
        }
        if (!(piece.getTeamColor() == getTeamTurn())) {
            throw new InvalidMoveException("Not your turn!");
        }
        Collection<ChessMove> validMoves = validMoves(startPosition);
        if (!validMoves.contains(move)) {
            throw new InvalidMoveException("Not a valid move!");
        }
        board.addPiece(endPosition, piece);
        board.addPiece(startPosition, null);//to remove, same as adding a remove func in ChessBoard
        //for promotion pieces
        if ((endPosition.getRow()==1 || endPosition.getRow()==8) && piece.getPieceType() == ChessPiece.PieceType.PAWN) {
            if (move.getPromotionPiece() != null ) {
                ChessPiece promotion = new ChessPiece(piece.getTeamColor(), move.getPromotionPiece());
                board.addPiece(endPosition, promotion);
            }
        }
        prevMove = move;
        nextTurn();
    }

    private void nextTurn() {
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
                    kingPosition = position;
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
                    Collection<ChessMove> moves = piece.pieceMoves(board, position);
                    for (ChessMove move : moves) {
                        if (move.getEndPosition().equals(kingPosition)) {
                            return true;
                        }
                    }
                }
            }
        }
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
        } else {
            return noLegalMoves(teamColor);
        }
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
        } else {
            return noLegalMoves(teamColor);
        }
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

    /**
     * Determines if there are validMoves
     *
     * @return whether
     */
    private boolean noLegalMoves(TeamColor teamColor) {
        ChessBoard board = getBoard();
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                ChessPosition position = new ChessPosition(x+1, y+1);
                ChessPiece piece = board.getPiece(position);
                if (piece != null && piece.getTeamColor() == teamColor) {
                    Collection<ChessMove> moves = piece.pieceMoves(board, position);
                    for (ChessMove move : moves) {
                        ChessBoard boardCopy = board.clone();
                        ChessPiece movePiece = new ChessPiece(piece.getTeamColor(), piece.getPieceType());
                        boardCopy.addPiece(move.getEndPosition(), movePiece);
                        boardCopy.addPiece(position, null);//remove piece
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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ChessGame that = (ChessGame) obj;
        if ((teamColor == that.teamColor) && (Objects.equals(board, that.board))) {
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    public String toString() {
        return "board = " + board + ", teamColor/teamTurn = " + teamColor + '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(board, teamColor);
    }
}