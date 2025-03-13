package chess;

import java.util.Collection;
import java.util.ArrayList;

public abstract class PieceMovesCalculator {

    public abstract Collection<ChessMove> piecesMove(ChessBoard board, ChessPosition position);
    //edit, later, make this have code which the funcs inherit from, re-use more code
    protected Collection<ChessMove> calcMoves(ChessBoard board, ChessPosition position, int[] x, int[] y) {
        Collection<ChessMove> moves = new ArrayList<>();
        for (int i = 0; i < x.length; i++) {
            int currX = position.getRow();
            int currY = position.getColumn();
            while (true) {
                currX += x[i];
                currY += y[i];
                if (currX < 1 || currX > 8 || currY < 1 || currY > 8) {
                    break;
                }
                ChessPosition currPosition = new ChessPosition(currX, currY);
                ChessPiece pieceCurrPos = board.getPiece(currPosition);
                if (pieceCurrPos == null) {
                    moves.add(new ChessMove(position, currPosition, null));
                } else {
                    if (pieceCurrPos.getTeamColor() != board.getPiece(position).getTeamColor()) {
                        moves.add(new ChessMove(position, currPosition, null));
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
            int currX = position.getRow() + x[i];
            int currY = position.getColumn() + y[i];
            while (true) {

                if (currX < 1 || currX > 8 || currY < 1 || currY > 8) {
                    break;
                }
                ChessPosition currPosition = new ChessPosition(currX, currY);
                ChessPiece pieceCurrPos = board.getPiece(currPosition);
                if (pieceCurrPos == null) {
                    moves.add(new ChessMove(position, currPosition, null));
                } else {
                    if (pieceCurrPos.getTeamColor() != board.getPiece(position).getTeamColor()) {
                        moves.add(new ChessMove(position, currPosition, null));
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
        ChessPiece pieceCurrPos = board.getPiece(position);
        ChessGame.TeamColor teamColor = pieceCurrPos.getTeamColor();
        //list for PieceType, this is for promotion pieces where the pawn flips at the end of the board!!
        //make list accessable in whole func
        ArrayList<ChessPiece.PieceType> promotionPieceTypes = new ArrayList<>();
        promotionPieceTypes.add(ChessPiece.PieceType.ROOK);
        promotionPieceTypes.add(ChessPiece.PieceType.KNIGHT);
        promotionPieceTypes.add(ChessPiece.PieceType.BISHOP);
        promotionPieceTypes.add(ChessPiece.PieceType.QUEEN);

        //direction based on color
        int direction = 1;
        boolean white = (teamColor == ChessGame.TeamColor.WHITE);
        if (white == true) { direction = 1; }
        else { direction = -1; }

        //normal pawn one step move
        int nextX = position.getRow() + direction;

        //check in bounds
        if (nextX >=1 && nextX <= 8) {
            ChessPosition nextPosition = new ChessPosition(nextX, position.getColumn());
            ChessPiece pieceNextPos = board.getPiece(nextPosition);
            if (pieceNextPos == null) {
                // promotion piece --> if the next move is the top/bottom of board, then add promotion Type
                if (nextX == 8 || nextX == 1) {
                    for (ChessPiece.PieceType promotionType : promotionPieceTypes) {
                        moves.add(new ChessMove(position, nextPosition, promotionType));
                    }
                } else {
                    moves.add(new ChessMove(position, nextPosition, null));
                }
            }
        }

        //ugh why do pawns have to be sooo confusing :/
        int[] diagonalY = {-1, 1};
        int[] diagonalX = {1, -1};
        for (int i = 0; i < diagonalX.length; i++) {
            int newY = position.getColumn() + diagonalY[i];
            int newX = position.getRow() + direction;

            if (!inBounds(newX, newY)) { continue; }
            ChessPosition diagPos = new ChessPosition(newX, newY);
            ChessPiece pieceDiagPos = board.getPiece(diagPos);
            if (pieceDiagPos == null || pieceDiagPos.getTeamColor() == teamColor) { continue; }
            pawnCapture(
                    moves,
                    position,
                    diagPos,
                    newX,
                    promotionPieceTypes
            );
            }


        //first pawn move!! (why is chess this wayy...)
        //in order to move two spots
        if ((white && position.getRow() == 2) || (!white && position.getRow() == 7)) {
            //still has to move in the correct direction
            int nextNextX = position.getRow() + (2 * direction);
            if (nextNextX >= 1 && nextNextX <= 8) {
                ChessPosition nextPosition = new ChessPosition(nextX, position.getColumn());
                ChessPiece pieceNextPos = board.getPiece(nextPosition);
                ChessPosition nextNextPos = new ChessPosition(nextNextX, position.getColumn());
                ChessPiece pieceNextNextPos = board.getPiece(nextNextPos);

                //both places in front MUST be empty
                if (pieceNextPos == null && pieceNextNextPos == null) {
                    moves.add(new ChessMove(position, nextNextPos, null));
                }
            }
        }
        return moves;
    }
    //helperfunc to reduce code lines for quality code check!
    private boolean inBounds(int x, int y) {
        return x >= 1 && x <= 8 && y >= 1 && y <= 8;
    }
    //helperfunc to reduce nesting for quality code check...
    private void pawnCapture(Collection<ChessMove> moves, ChessPosition start, ChessPosition end, int newX, ArrayList<ChessPiece.PieceType> promotionPieceTypes) {
        if (newX == 8 || newX == 1) {
            for (ChessPiece.PieceType promotionType : promotionPieceTypes) {
                moves.add(new ChessMove(start, end, promotionType));
            }
        } else {
            moves.add(new ChessMove(start, end, null));
        }
    }
}