package client;
import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;
import model.AuthData;
import model.Game;
import java.util.*;
import static ui.EscapeSequences.*;

public class ChessClient {
    private String currentUser = null;
    private final Map<Integer, Game> prevGameList = new HashMap<>();
    private final ServerFacade server;
    private State state = State.SIGNEDOUT;

    public ChessClient(String serverUrl) {
        server = new ServerFacade(serverUrl);
    }

    public void run() {
        System.out.print(ERASE_SCREEN);//clear screen
        System.out.println("Welcome to 240 chess. Type help to get started.");

        Scanner scanner = new Scanner(System.in);
        var result = "";
        while (!"quit".equals(result)) {
            printPrompt();
            String line = scanner.nextLine();
            try {
                result = eval(line);
                if ((result ==null) ||("quit".equals(result))) {
                    continue;
                }
                System.out.println(result);
            } catch (ClientException e) {
                System.out.println("Error: " +e.getMessage());
            } catch (Throwable e) {
                System.out.println("oops something went wrong...try again");
            }
        }
        System.out.println();
    }

    private void printPrompt() {
        if (state == State.SIGNEDOUT) {
            System.out.print("\n" + RESET_TEXT_COLOR + RESET_BG_COLOR + "[LOGGED-OUT] >> ");
        } else {
            System.out.print("\n" + RESET_TEXT_COLOR + RESET_BG_COLOR + "[LOGGED-IN] >> ");
        }
    }

    private String eval(String input) throws ClientException {
        if (input.isBlank()) {
            return "";
        }
        String[] tokens = input.toLowerCase().trim().split("\\s+");
        String cmd = (tokens.length > 0) ? tokens[0] : "help";
        String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);
        if (state == State.SIGNEDOUT) {
            return switch (cmd) {
                case "help" -> signedOutHelp();
                case "register" -> register(params);
                case "login" -> login(params);
                case "quit" -> "quit";
                default -> signedOutHelp();
            };
        } else {
            return switch (cmd) {
                case "help" -> signedInHelp();
                case "logout" -> logout();
                case "create" -> create(params);
                case "list" -> list();
                case "join" -> join(params);
                case "observe" -> observe(params);
                case "quit" -> "quit";
                default -> signedInHelp();
            };
        }
    }

    private String register(String... params) throws ClientException {
        if (params.length>=3) {
            AuthData authData = server.register(params[0], params[1],params[2]);
            this.currentUser = authData.username();
            this.state = State.SIGNEDIN;
            return "successfully registered & logged in as "+ currentUser;
        }
        throw new ClientException("expected: register <USERNAME> <PASSWORD> <EMAIL>");
    }

    private String login(String... params) throws ClientException {
        if (params.length >= 2){
            AuthData authData = server.login(params[0],params[1]);
            this.currentUser = authData.username();
            this.state = State.SIGNEDIN;
            return "successfully logged in as " +currentUser;
        }
        throw new ClientException("expected: login <USERNAME> <PASSWORD>");
    }

    private String logout() throws ClientException {
        assertSignedIn();
        server.logout();
        this.state = State.SIGNEDOUT;
        this.currentUser = null;
        this.prevGameList.clear();
        return "successfully logged out!";
    }
    private String create(String... params) throws ClientException {
        assertSignedIn();
        if (params.length>=1) {
            String gameName = String.join(" ",params);
            server.createGame(gameName);
            return "successfully created game "+ gameName;
        }
        throw new ClientException("expected: create <GAMENAME>");
    }

    private String list() throws ClientException {
        assertSignedIn();
        var gameList =server.listGames();
        prevGameList.clear();
        if (gameList.isEmpty()) {
            return "no games found!";
        }
        StringBuilder gameText = new StringBuilder("games -->\n");
        int gameNumber = 1;
        for (Game game : gameList) {
            prevGameList.put(gameNumber, game);
            String white;
            String black;
            if (game.whiteUser()==null) {
                white = "(empty)";
            } else {
                white = game.whiteUser();
            }
            if (game.blackUser()==null) {
                black = "(empty)";
            } else {
                black = game.blackUser();
            }
            gameText.append(gameNumber).append(". ").append(game.gameName()).append("  WHITE: ").append(white).append("  BLACK: ").append(black).append("\n");
            gameNumber++;
        }
        return gameText.toString();
    }

    //Postlogin UI
    private String signedInHelp() {
        return """
                  create <GAMENAME> - a game
                  list - games
                  join <GAMEID> [WHITE|BLACK] - a game
                  observe <GAMEID> - a game
                  logout - when you're done
                  quit - to stop playing chess
                  help - for possible commands
                """;
    }
    //Prelogin UI
    private String signedOutHelp() {
        return """
              register <USERNAME> <PASSWORD> <EMAIL> - to create an account
              login <USERNAME> <PASSWORD> - to play
              quit - to stop playing chess
              help - for possible commands
            """;
    }

    private String getIcon(ChessPiece piece) {
        if (piece==null) {
            return EMPTY;
        }
        boolean is_white = (piece.getTeamColor() == ChessGame.TeamColor.WHITE);
        return switch (piece.getPieceType()) {
            case KING -> is_white ? WHITE_KING : BLACK_KING;
            case QUEEN -> is_white ? WHITE_QUEEN : BLACK_QUEEN;
            case BISHOP -> is_white ? WHITE_BISHOP : BLACK_BISHOP;
            case KNIGHT -> is_white ? WHITE_KNIGHT : BLACK_KNIGHT;
            case ROOK -> is_white ? WHITE_ROOK : BLACK_ROOK;
            case PAWN -> is_white ? WHITE_PAWN : BLACK_PAWN;
            default -> EMPTY;
        };
    }
}