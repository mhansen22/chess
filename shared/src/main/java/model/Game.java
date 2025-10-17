package model;
import chess.ChessGame;
public record Game (int gameId, String blackUser, String whiteUser, String gameName, ChessGame game) {
}
