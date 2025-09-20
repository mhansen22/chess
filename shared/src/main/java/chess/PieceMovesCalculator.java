package chess;

import java.util.Collection;
import java.util.ArrayList;

public abstract class PieceMovesCalculator {

    public abstract Collection<ChessMove> piecesMove(ChessBoard board, ChessPosition position);

    protected Collection<ChessMove> slidingMoves(ChessBoard board, ChessPosition position, int[] row, int[] col) {
        Collection<ChessMove> moves = new ArrayList<>();
        ChessPiece startPiece = board.getPiece(position);
        for (int i = 0; i < row.length; i++) {
            int currRow = position.getRow();
            int currCol = position.getColumn();
            for (int j = 1; j <= 7; j++) {
                currRow += row[i];
                currCol += col[i];
                if (currCol < 1 || currCol > 8 || currRow < 1 || currRow > 8) {
                    break;
                }
                ChessPosition currPosition = new ChessPosition(currRow, currCol);
                ChessPiece pieceCurrPos = board.getPiece(currPosition);
                if (pieceCurrPos != null) {
                    if (pieceCurrPos.getTeamColor() != startPiece.getTeamColor()) {
                        moves.add(new ChessMove(position, currPosition, null));
                    }
                    break;
                }
                moves.add(new ChessMove(position, currPosition, null));
            }
        }
        return moves;
    }

    protected Collection<ChessMove> stepMoves(ChessBoard board, ChessPosition position, int[] row, int[] col) {
        Collection<ChessMove> moves = new ArrayList<>();
        for (int i = 0; i < row.length; i++) {
            int currRow = position.getRow() + row[i];
            int currCol = position.getColumn() + col[i];

            if (currRow < 1 || currRow > 8 || currCol < 1 || currCol > 8) continue;
            ChessPosition currPosition = new ChessPosition(currRow, currCol);
            ChessPiece pieceCurrPos = board.getPiece(currPosition);
            if (pieceCurrPos == null || pieceCurrPos.getTeamColor() != board.getPiece(position).getTeamColor()) {
                moves.add(new ChessMove(position, currPosition, null));
            }
        }
        return moves;
    }

}

class KingMovesCalculator extends PieceMovesCalculator {
    @Override
    public Collection<ChessMove> piecesMove(ChessBoard board, ChessPosition position) {
        int[] row = { 1,0,-1, -1, 0, 1, -1, 1 };
        int[] col = {0, 1, 1, 0, -1, -1, -1, 1};
        return stepMoves(board, position, row, col);
    }
}

class KnightMovesCalculator extends PieceMovesCalculator {
    @Override
    public Collection<ChessMove> piecesMove(ChessBoard board, ChessPosition position) {
        int[] row = {-1,2,-2, 1,-1, 2, 1, -2};
        int[] col = { 2,-1,-1, 2,-2, 1, -2, 1};
        return stepMoves(board, position, row, col);
    }
}

class BishopMovesCalculator extends PieceMovesCalculator {
    @Override
    public Collection<ChessMove> piecesMove(ChessBoard board, ChessPosition position) {
        int[] row = {1, 1, -1,-1};
        int[] col = {1, -1, -1, 1};
        return slidingMoves(board, position, row, col);
    }
}

class RookMovesCalculator extends PieceMovesCalculator {
    @Override
    public Collection<ChessMove> piecesMove(ChessBoard board, ChessPosition position) {
        int[] row = {0, 0, 1, -1};
        int[] col = {1, -1, 0, 0};
        return slidingMoves(board, position, row, col);
    }
}

class QueenMovesCalculator extends PieceMovesCalculator {
    @Override
    public Collection<ChessMove> piecesMove(ChessBoard board, ChessPosition position) {
        int[] row = {1, -1, 1, -1, 0, 0, 1, -1};
        int[] col = {-1, 1, 1, -1, 1, -1, 0, 0};
        return slidingMoves(board, position, row, col);
    }
}

class PawnMovesCalculator extends PieceMovesCalculator {
    @Override
    public Collection<ChessMove> piecesMove(ChessBoard board, ChessPosition position) {
        Collection<ChessMove> moves = new ArrayList<>();
        ChessPiece pieceCurrPos = board.getPiece(position);
        ChessGame.TeamColor teamColor = pieceCurrPos.getTeamColor();
        ArrayList<ChessPiece.PieceType> promotionPieceTypes = new ArrayList<>();
        promotionPieceTypes.add(ChessPiece.PieceType.KNIGHT);
        promotionPieceTypes.add(ChessPiece.PieceType.BISHOP);
        promotionPieceTypes.add(ChessPiece.PieceType.ROOK);
        promotionPieceTypes.add(ChessPiece.PieceType.QUEEN);

        boolean white = (teamColor ==ChessGame.TeamColor.WHITE);
        int direction = 1;
        if (!white) {
            direction = -1;
        }
        int nextRow = position.getRow()+ direction;

        if (nextRow >= 1 && nextRow <= 8) {
            ChessPosition nextPosition = new ChessPosition(nextRow, position.getColumn());
            ChessPiece pieceNextPos = board.getPiece(nextPosition);
            if (pieceNextPos == null) {
                boolean promote = (nextRow == 8 ||nextRow == 1);
                if (promote) {
                    for (ChessPiece.PieceType promotionType : promotionPieceTypes){
                        moves.add(new ChessMove(position, nextPosition, promotionType));
                    }
                } else {
                    moves.add(new ChessMove(position, nextPosition, null));
                }
            }
        }
        int[] diagonal = {-1,1};
        for (int j : diagonal) {
            int newCol = position.getColumn()+ j;
            int newRow = position.getRow() + direction;

            if (newCol >= 1 && newCol <= 8 && newRow >= 1 && newRow <= 8) {
                ChessPosition diagPos = new ChessPosition(newRow, newCol);
                ChessPiece pieceDiagPos = board.getPiece(diagPos);
                if (pieceDiagPos != null && pieceDiagPos.getTeamColor() !=teamColor) {
                    boolean promote = (newRow == 8 || newRow == 1);
                    if (promote) {
                        for (ChessPiece.PieceType promotionType : promotionPieceTypes) {
                            moves.add(new ChessMove(position, diagPos, promotionType));
                        }
                    } else {
                        moves.add(new ChessMove(position, diagPos, null));
                    }
                }
            }
        }
        if ((!white && position.getRow() == 7) || (white && position.getRow() == 2)) {
            int nextNextRow = position.getRow()+ (2 *direction);
            if (nextNextRow <= 8 && nextNextRow >= 1) {
                ChessPosition nextNextPos = new ChessPosition(nextNextRow, position.getColumn());
                ChessPosition nextPosition = new ChessPosition(nextRow, position.getColumn());

                ChessPiece pieceNextPos = board.getPiece(nextPosition);
                ChessPiece pieceNextNextPos = board.getPiece(nextNextPos);
                if (pieceNextPos == null && pieceNextNextPos == null) {
                    moves.add(new ChessMove(position, nextNextPos, null));
                }
            }
        }
        return moves;
    }
}
