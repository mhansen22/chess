import chess.*;
import spark.Spark;

public class Main {
    public static void main(String[] args) {
        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        System.out.println("♕ 240 Chess Server: " + piece);

        //static files are located in /web
        Spark.staticFiles.location("/web");
        Spark.port(8080);
        Spark.init();
        System.out.println("Server running on port: 8080");
    }
}