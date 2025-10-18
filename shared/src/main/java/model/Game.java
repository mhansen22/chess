package model;
import chess.ChessGame;
public record Game (int gameId, String whiteUser, String blackUser, String gameName, ChessGame game) {
}
