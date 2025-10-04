package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import static chess.ChessPiece.PieceType.KING;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private ChessBoard board;
    private TeamColor turn;

    public ChessGame() {
        this.board = new ChessBoard();
        this.board.resetBoard();
        this.turn = TeamColor.WHITE;
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() { return turn; }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) { this.turn = team; }

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
        ChessPiece p = board.getPiece(startPosition);
        if (p == null) {
            return null;
        }

        List<ChessMove> legalMoves = new ArrayList<>();
        for (ChessMove m : p.pieceMoves(board, startPosition)) {
            ChessBoard boardCopy = board.deepCopy();
            boardCopy.addPiece(m.getEndPosition(), new ChessPiece(p.getTeamColor(), p.getPieceType()));
            boardCopy.addPiece(m.getStartPosition(), null);
            ChessGame copiedGame = new ChessGame();
            copiedGame.setBoard(boardCopy);
            copiedGame.setTeamTurn(this.turn);

            if (!copiedGame.isInCheck(p.getTeamColor())) {
                legalMoves.add(m);
            }
        }
        return legalMoves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPosition startPos = move.getStartPosition();
        ChessPosition endPos = move.getEndPosition();
        ChessPiece p = board.getPiece(startPos);

        if (p == null) {
            throw new InvalidMoveException("No piece at this position");
        }
        if (!(p.getTeamColor() == getTeamTurn())) {
            throw new InvalidMoveException("Wrong teamTurn");
        }
        Collection<ChessMove> correctMoves = validMoves(startPos);
        if (!correctMoves.contains(move) || correctMoves == null) {
            throw new InvalidMoveException("not valid/illegal move");
        }
        board.addPiece(endPos, p);
        board.addPiece(startPos, null);

        //promotion piece logic
        if ((p.getPieceType() ==ChessPiece.PieceType.PAWN) && (endPos.getRow() == 8 || endPos.getRow() == 1) ) {
            if (move.getPromotionPiece() != null ) {
                ChessPiece promoted = new ChessPiece(p.getTeamColor(), move.getPromotionPiece() );
                board.addPiece(endPos, promoted);
            }
        }

        if (getTeamTurn() == TeamColor.BLACK) {//turnColor
            setTeamTurn(TeamColor.WHITE);
        } else {
            setTeamTurn(TeamColor.BLACK);
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
        ChessPosition kingPos = null;

        for (int row = 0; row <= 7; row++) {
            for (int col = 0; col <= 7; col++) {
                ChessPosition pos = new ChessPosition(row +1, col +1);
                ChessPiece p = board.getPiece(pos);
                if (p != null && (p.getPieceType() == KING && p.getTeamColor() == teamColor)) {
                    kingPos = pos;
                    break;
                }
            }
        }
        //opposite team, setting
        TeamColor otherTeam = (teamColor == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
        for (int row = 0; row <= 7; row++) {
            for (int col = 0; col <= 7; col++) {
                ChessPosition pos = new ChessPosition(row +1, col +1);
                ChessPiece p = board.getPiece(pos);
                if (p != null && p.getTeamColor() == otherTeam) {
                    for (ChessMove move : p.pieceMoves(board, pos)) {
                        if (move.getEndPosition().equals(kingPos)) {
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
            return noCorrectMoves(teamColor);
        }
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if (isInCheck(teamColor)) {
            return false;
        } else {
            return noCorrectMoves(teamColor);
        }
    }

    //helper func to reduce lines of code, reused logic
    private boolean noCorrectMoves(TeamColor teamColor) {
        ChessBoard board = getBoard();
        for (int row = 0; row <= 7; row++) {
            for (int col = 0; col <= 7; col++) {
                ChessPosition pos = new ChessPosition(row+1, col+1 );
                ChessPiece p = board.getPiece(pos);

                if (p != null && p.getTeamColor() ==teamColor) {
                    for (ChessMove m : p.pieceMoves(board, pos)) {
                        ChessBoard copyBoard = board.deepCopy();
                        ChessPiece movePiece = new ChessPiece(p.getTeamColor(), p.getPieceType());
                        copyBoard.addPiece(pos, null);//remove piece here
                        copyBoard.addPiece(m.getEndPosition(), movePiece);

                        ChessGame gameCopy = new ChessGame();
                        gameCopy.setBoard(copyBoard);
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
    public void setBoard(ChessBoard board) { this.board = board; }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() { return board; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof ChessGame comp)) {
            return false;
        }
        return (turn == comp.turn) && (Objects.equals(board, comp.board));
    }

    @Override
    public String toString() {
        return "board: " + board + " teamColor & turn: " + turn;
    }

    @Override
    public int hashCode() {
        return Objects.hash(board, turn);
    }
}