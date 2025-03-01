package chess;

import java.util.Collection;
import java.util.ArrayList;

public abstract class PieceMovesCalculator {

    public abstract Collection<ChessMove> piecesMove(ChessBoard board, ChessPosition position);
    //edit, later, make this have code which the funcs inherit from, re-use more code
    protected Collection<ChessMove> calcMoves(ChessBoard board, ChessPosition position, int[] x, int[] y) {
        Collection<ChessMove> moves = new ArrayList<>();
        for (int i = 0; i < x.length; i++) {
            int curr_x = position.getRow();
            int curr_y = position.getColumn();
            while (true) {
                curr_x += x[i];
                curr_y += y[i];
                if (curr_x < 1 || curr_x > 8 || curr_y < 1 || curr_y > 8) {
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
    protected Collection<ChessMove> calcMoves2(ChessBoard board, ChessPosition position, int[] x, int[] y) {
        Collection<ChessMove> moves = new ArrayList<>();
        for (int i = 0; i < x.length; i++) {
            int curr_x = position.getRow() + x[i];
            int curr_y = position.getColumn() + y[i];
            while (true) {

                if (curr_x < 1 || curr_x > 8 || curr_y < 1 || curr_y > 8) {
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
                }
                break;
            }
        }
        return moves;
    }
}
class BishopMovesCalculator extends PieceMovesCalculator {
    @Override
    public Collection<ChessMove> piecesMove(ChessBoard board, ChessPosition position) {
        //bishops can move diagonal ONLY
        int[] x = {-1, -1, 1, 1};
        int[] y = {1, -1, 1, -1};
        return calcMoves(board, position, x, y);
    }
}
class KingMovesCalculator extends PieceMovesCalculator {
    @Override
    public Collection<ChessMove> piecesMove(ChessBoard board, ChessPosition position) {
        int[] x = {-1, -1, -1, 0, 1, 1, 1, 0};
        int[] y = {-1, 0, 1, 1, 1, 0, -1, -1};
        return calcMoves2(board, position, x, y);
    }
}
class KnightMovesCalculator extends PieceMovesCalculator {

    @Override
    public Collection<ChessMove> piecesMove(ChessBoard board, ChessPosition position) {
        //moves 2 one way 1 the other
        int[] x = {2, 2, 1, 1, -2, -2, -1, -1};
        int[] y = {1, -1, 2, -2, -1, 1, 2, -2};
        return calcMoves2(board, position, x, y);
    }
}
class QueenMovesCalculator extends PieceMovesCalculator {
    @Override
    public Collection<ChessMove> piecesMove(ChessBoard board, ChessPosition position) {
        //queens can move diagonal + vertical/horizontal
        //combine rook and bishop LOGIC yay
        int[] x = {-1, 1, 0, 0, 1, -1, 1, -1};
        int[] y = {0, 0, -1, 1, 1, 1, -1, -1};
        return calcMoves(board, position, x, y);
    }
}

class RookMovesCalculator extends PieceMovesCalculator {
    @Override
    public Collection<ChessMove> piecesMove(ChessBoard board, ChessPosition position) {
        //rooks move vertical and horizontal ONLY
        int[] x = {-1, 1, 0, 0};
        int[] y = {0, 0, -1, 1};
        return calcMoves(board, position, x, y);
    }
}
class PawnMovesCalculator extends PieceMovesCalculator {

    @Override
    public Collection<ChessMove> piecesMove(ChessBoard board, ChessPosition position) {
        Collection<ChessMove> moves = new ArrayList<>();

        //for color and position
        ChessPiece piece_curr_pos = board.getPiece(position);
        ChessGame.TeamColor teamColor = piece_curr_pos.getTeamColor();

        //list for PieceType, this is for promotion pieces where the pawn flips at the end of the board!!
        //make list accessable in whole func
        ArrayList<ChessPiece.PieceType> promotion_piece_types = new ArrayList<>();
        promotion_piece_types.add(ChessPiece.PieceType.ROOK);
        promotion_piece_types.add(ChessPiece.PieceType.KNIGHT);
        promotion_piece_types.add(ChessPiece.PieceType.BISHOP);
        promotion_piece_types.add(ChessPiece.PieceType.QUEEN);

        //direction based on color
        int direction = 1;
        boolean white = (teamColor == ChessGame.TeamColor.WHITE);
        if (white == true) {
            direction = 1;
        } else {
            direction = -1;
        }

        //normal pawn one step move
        int next_x = position.getRow() + direction;

        //check in bounds
        if (next_x >=1 && next_x <= 8) {
            ChessPosition next_position = new ChessPosition(next_x, position.getColumn());
            ChessPiece piece_next_pos = board.getPiece(next_position);
            if (piece_next_pos == null) {
                // promotion piece --> if the next move is the top/bottom of board, then add promotion Type
                if (next_x == 8 || next_x == 1) {
                    for (ChessPiece.PieceType promotionType : promotion_piece_types) {
                        moves.add(new ChessMove(position, next_position, promotionType));
                    }
                } else {
                    moves.add(new ChessMove(position, next_position, null));
                }
            }
        }

        //ugh why do pawns have to be sooo confusing :/
        int[] diagonal_y = {-1, 1};
        int[] diagonal_x = {1, -1};

        for (int i = 0; i < diagonal_x.length; i++) {
            int new_y = position.getColumn() + diagonal_y[i];
            if (new_y >= 1 && new_y <= 8) {
                int new_x = position.getRow() + direction;
                if (new_x >= 1 && new_x <= 8) {
                    ChessPosition diag_pos = new ChessPosition(new_x, new_y);
                    ChessPiece piece_diag_pos = board.getPiece(diag_pos);
                    if (piece_diag_pos != null && piece_diag_pos.getTeamColor() != teamColor) {
                        //can also capture enemy and have promotion piece
                        if (new_x == 8 || new_x == 1) {
                            for (ChessPiece.PieceType promotionType : promotion_piece_types) {
                                moves.add(new ChessMove(position, diag_pos, promotionType));
                            }
                        } else {
                            moves.add(new ChessMove(position, diag_pos, null));
                        }
                    }
                }
            }
        }

        //first pawn move!! (why is chess this wayy...)
        //in order to move two spots
        if ((white && position.getRow() == 2) || (!white && position.getRow() == 7)) {

            //still has to move in the correct direction
            int next_next_x = position.getRow() + (2 * direction);
            if (next_next_x >= 1 && next_next_x <= 8) {
                ChessPosition next_position = new ChessPosition(next_x, position.getColumn());
                ChessPiece piece_next_pos = board.getPiece(next_position);
                ChessPosition next_next_pos = new ChessPosition(next_next_x, position.getColumn());
                ChessPiece piece_next_next_pos = board.getPiece(next_next_pos);

                //both places in front MUST be empty
                if (piece_next_pos == null && piece_next_next_pos == null) {
                    moves.add(new ChessMove(position, next_next_pos, null));
                }
            }
        }
        ChessMove prevMove = board.getPrevMove();  // Get the previous move from the board

        if (prevMove != null) {
            ChessPosition prevStart = prevMove.getStartPosition();
            ChessPosition prevEnd = prevMove.getEndPosition();
            ChessPiece prevPiece = board.getPiece(prevEnd);

            int rowDiff = Math.abs(prevEnd.getRow() - prevStart.getRow());
            if (rowDiff == 2 && prevPiece.getPieceType() == ChessPiece.PieceType.PAWN) {
                // Check if the previous move was a two-square pawn move
                int colDiff = Math.abs(prevEnd.getColumn() - position.getColumn());
                if (colDiff == 1 && prevEnd.getRow() == position.getRow()) {
                    ChessPosition capturePosition = new ChessPosition(position.getRow() + direction, position.getColumn());
                    ChessPiece capturedPiece = board.getPiece(capturePosition);

                    if (capturedPiece != null && capturedPiece.getPieceType() == ChessPiece.PieceType.PAWN) {
                        // Check if the opponent's pawn is next to the current pawn (en passant condition)
                        if (capturedPiece.getTeamColor() != piece_curr_pos.getTeamColor()) {
                            moves.add(new ChessMove(position, capturePosition, null));  // En Passant move
                        }
                    }
                }
            }
        }
        return moves;
    }
}