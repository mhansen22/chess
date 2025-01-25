package chess;

import java.util.Collection;
import java.util.ArrayList;

public abstract class PieceMovesCalculator {

    public abstract Collection<ChessMove> piecesMove(ChessBoard board, ChessPosition position);
    //edit, later, make this have code which the funcs inherit from, re-use more code
}

class BishopMovesCalculator extends PieceMovesCalculator {
    //so this inherets from piecemovescalcultor!!, although not relevent right now

    //use override for overriding parent/super class
    //eventually should move logic into parent class, thus no redundant code
    @Override
    public Collection<ChessMove> piecesMove(ChessBoard board, ChessPosition position) {
        //create new ArrayList for valid moves, to be returned
        Collection<ChessMove> moves = new ArrayList<>();

        //bishops can move diagonal ONLY
        int[] x = {-1, -1, 1, 1};
        int[] y = {1, -1, 1, -1};

        for (int i = 0; i < x.length; i++) {
            int curr_x = position.getRow();
            int curr_y = position.getColumn();
            //move diagonal until it hits the end of the board or hits another piece
            while (true) {
                curr_x += x[i];
                curr_y += y[i];
                System.out.println("check curr_pos: (" + curr_x + ", " + curr_y + ")");

                if (curr_x < 1 || curr_x > 8 || curr_y < 1 || curr_y > 8) {
                    System.out.println("out of bounds!!! eeek: (" + curr_x + ", " + curr_y + ")");
                    break;
                }

                ChessPosition curr_position = new ChessPosition(curr_x, curr_y);
                ChessPiece piece_curr_pos = board.getPiece(curr_position);
                if (piece_curr_pos == null) {
                    System.out.println("empty yay!! valid: (" + curr_x + "," + curr_y + ")");
                    moves.add(new ChessMove(position, curr_position, null));
                } else {
                    if (piece_curr_pos.getTeamColor() != board.getPiece(position).getTeamColor()) {
                        moves.add(new ChessMove(position, curr_position, null));
                        System.out.println("opponent there!!captured: (" + curr_x + "," + curr_y + ")");
                    }
                    break;
                }
            }
        }
        return moves;
    }
}

class KingMovesCalculator extends PieceMovesCalculator {

    @Override
    public Collection<ChessMove> piecesMove(ChessBoard board, ChessPosition position) {
        Collection<ChessMove> moves = new ArrayList<>();

        //kings can move any direction, i think...im learning chess rules rn :)
        int[] x = {-1, -1, -1, 0, 1, 1, 1, 0};
        int[] y = {-1, 0, 1, 1, 1, 0, -1, -1};

        //but can only more one space at a time, so add move FIRST!!!
        for (int i = 0; i < x.length; i++) {
            int curr_x = position.getRow() + x[i];
            int curr_y = position.getColumn() + y[i];

            while (true) {
                System.out.println("check curr_pos: (" + curr_x + ", " + curr_y + ")");

                if (curr_x < 1 || curr_x > 8 || curr_y < 1 || curr_y > 8) {
                    System.out.println("out of bounds!!! eeek: (" + curr_x + ", " + curr_y + ")");
                    break;
                }

                ChessPosition curr_position = new ChessPosition(curr_x, curr_y);
                ChessPiece piece_curr_pos = board.getPiece(curr_position);
                if (piece_curr_pos == null) {
                    System.out.println("empty yay!! valid: (" + curr_x + "," + curr_y + ")");
                    moves.add(new ChessMove(position, curr_position, null));
                } else {
                    if (piece_curr_pos.getTeamColor() != board.getPiece(position).getTeamColor()) {
                        moves.add(new ChessMove(position, curr_position, null));
                        System.out.println("opponent there!!captured: (" + curr_x + "," + curr_y + ")");
                    }
                }
                break;
            }
        }
        return moves;
    }
}

class KnightMovesCalculator extends PieceMovesCalculator {

    @Override
    public Collection<ChessMove> piecesMove(ChessBoard board, ChessPosition position) {
        Collection<ChessMove> moves = new ArrayList<>();

        //moves 2 one way 1 the other
        int[] x = {2, 2, 1, 1, -2, -2, -1, -1};
        int[] y = {1, -1, 2, -2, -1, 1, 2, -2};

        //same as rest
        for (int i = 0; i < x.length; i++) {
            int curr_x = position.getRow() + x[i];
            int curr_y = position.getColumn() + y[i];

            while (true) {
                System.out.println("check curr_pos: (" + curr_x + ", " + curr_y + ")");

                if (curr_x < 1 || curr_x > 8 || curr_y < 1 || curr_y > 8) {
                    System.out.println("out of bounds!!! eeek: (" + curr_x + ", " + curr_y + ")");
                    break;
                }

                ChessPosition curr_position = new ChessPosition(curr_x, curr_y);
                ChessPiece piece_curr_pos = board.getPiece(curr_position);
                if (piece_curr_pos == null) {
                    System.out.println("empty yay!! valid: (" + curr_x + "," + curr_y + ")");
                    moves.add(new ChessMove(position, curr_position, null));
                } else {
                    if (piece_curr_pos.getTeamColor() != board.getPiece(position).getTeamColor()) {
                        moves.add(new ChessMove(position, curr_position, null));
                        System.out.println("opponent there!!captured: (" + curr_x + "," + curr_y + ")");
                    }
                }
                break;
            }
        }
        return moves;
    }
}

class QueenMovesCalculator extends PieceMovesCalculator {

    @Override
    public Collection<ChessMove> piecesMove(ChessBoard board, ChessPosition position) {
        Collection<ChessMove> moves = new ArrayList<>();

        //queens can move diagonal + vertical/horizontal
        //combine rook and bishop LOGIC yay
        int[] x = {-1, 1, 0, 0, 1, -1, 1, -1};
        int[] y = {0, 0, -1, 1, 1, 1, -1, -1};

        for (int i = 0; i < x.length; i++) {
            int curr_x = position.getRow();
            int curr_y = position.getColumn();

            while (true) {
                curr_x += x[i];
                curr_y += y[i];
                System.out.println("check curr_pos: (" + curr_x + ", " + curr_y + ")");

                if (curr_x < 1 || curr_x > 8 || curr_y < 1 || curr_y > 8) {
                    System.out.println("out of bounds!!! eeek: (" + curr_x + ", " + curr_y + ")");
                    break;
                }

                ChessPosition curr_position = new ChessPosition(curr_x, curr_y);
                ChessPiece piece_curr_pos = board.getPiece(curr_position);
                if (piece_curr_pos == null) {
                    System.out.println("empty yay!! valid: (" + curr_x + "," + curr_y + ")");
                    moves.add(new ChessMove(position, curr_position, null));
                } else {
                    if (piece_curr_pos.getTeamColor() != board.getPiece(position).getTeamColor()) {
                        moves.add(new ChessMove(position, curr_position, null));
                        System.out.println("opponent there!!captured: (" + curr_x + "," + curr_y + ")");
                    }
                    break;
                }
            }
        }
        return moves;
    }
}

class RookMovesCalculator extends PieceMovesCalculator {

    @Override
    public Collection<ChessMove> piecesMove(ChessBoard board, ChessPosition position) {
        Collection<ChessMove> moves = new ArrayList<>();

        //rooks move vertical and horizontal ONLY
        int[] x = {-1, 1, 0, 0};
        int[] y = {0, 0, -1, 1};

        for (int i = 0; i < x.length; i++) {
            int curr_x = position.getRow();
            int curr_y = position.getColumn();

            while (true) {
                curr_x += x[i];
                curr_y += y[i];
                System.out.println("check curr_pos: (" + curr_x + ", " + curr_y + ")");

                if (curr_x < 1 || curr_x > 8 || curr_y < 1 || curr_y > 8) {
                    System.out.println("out of bounds!!! eeek: (" + curr_x + ", " + curr_y + ")");
                    break;
                }

                ChessPosition curr_position = new ChessPosition(curr_x, curr_y);
                ChessPiece piece_curr_pos = board.getPiece(curr_position);
                if (piece_curr_pos == null) {
                    System.out.println("empty yay!! valid: (" + curr_x + "," + curr_y + ")");
                    moves.add(new ChessMove(position, curr_position, null));
                } else {
                    if (piece_curr_pos.getTeamColor() != board.getPiece(position).getTeamColor()) {
                        moves.add(new ChessMove(position, curr_position, null));
                        System.out.println("opponent there!!captured: (" + curr_x + "," + curr_y + ")");
                    }
                    break;
                }
            }
        }
        return moves;
    }
}

