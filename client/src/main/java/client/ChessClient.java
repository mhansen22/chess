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
            gameText.append(gameNumber).append(". ").append(game.gameName()).append("  WHITE: ")
                    .append(white).append("  BLACK: ").append(black).append("\n");
            gameNumber++;
        }
        return gameText.toString();
    }
    private String join(String... params) throws ClientException {
        assertSignedIn();
        int i;
        if (prevGameList.isEmpty()) {
            throw new ClientException("enter: list first to see the gameID");
        }
        if (params.length<2){
            throw new ClientException("correct format: join <GAMEID> [WHITE|BLACK]");
        }
        try {
            i = Integer.parseInt(params[0]);
        } catch (NumberFormatException e) {
            throw new ClientException("gameID must be a real number!");
        }
        Game game = prevGameList.get(i);
        if (game==null) {
            throw new ClientException("not a real gameID...p.s. use command list to see available games ;)");
        }
        String colorText = params[1].toLowerCase();
        ChessGame.TeamColor color;
        if (colorText.equals("white")) {
            color =ChessGame.TeamColor.WHITE;
        } else if (colorText.equals("black")) {
            color = ChessGame.TeamColor.BLACK;
        } else {
            throw new ClientException("color can only be white or black!");
        }
        server.joinGame(game.gameId(), color);
        createBoard(color);
        return "successfully joined " + game.gameName() + " as " + color;
    }

    private String observe(String... params) throws ClientException {
        assertSignedIn();
        int i;
        if (prevGameList.isEmpty()) {
            throw new ClientException("enter: list first to see the gameID");
        }
        if (params.length <1) {
            throw new ClientException("correct format: observe <GAMEID>");
        }
        try {
            i = Integer.parseInt(params[0]);
        } catch (NumberFormatException e) {
            throw new ClientException("gameID needs to be a real number!");
        }
        Game game = prevGameList.get(i);
        if (game==null) {
            throw new ClientException("not a real gameID...p.s. use command list to see available games ;)");
        }
        createBoard(ChessGame.TeamColor.WHITE);
        return "successfully observing "+ game.gameName();
    }

    private void assertSignedIn() throws ClientException {
        if (state == State.SIGNEDOUT){
            throw new ClientException("you need to sign in first!!");
        }
    }

    private void createBoard(ChessGame.TeamColor color) {
        ChessBoard board = new ChessBoard();
        board.resetBoard();
        System.out.print(ERASE_SCREEN);
        if (color==ChessGame.TeamColor.WHITE) {
            createWhiteBoard(board);
        } else {
            createBlackBoard(board);
        }
        System.out.print(RESET_BG_COLOR+RESET_TEXT_COLOR +"\n");
    }

    private void createWhiteBoard(ChessBoard board) {
        System.out.println("    a  b  c  d  e  f  g  h");//top, letters
        for (int row = 8; row >= 1; row--) {
            System.out.print(" " + row + " ");//nums, left
            for (int col = 1; col <= 8; col++) {
                String squareColor;
                if (((row+col) % 2) == 0){
                    squareColor = SET_BG_COLOR_LIGHT_GREY;
                } else {
                    squareColor = SET_BG_COLOR_DARK_GREY;
                }
                ChessPiece piece = board.getPiece(new ChessPosition(row,col));
                System.out.print(squareColor +getIcon(piece));
            }
            System.out.print(RESET_BG_COLOR + " " + row +"\n");
        }
        System.out.println("    a  b  c  d  e  f  g  h");//bottom
    }
    private void createBlackBoard(ChessBoard board) {
        System.out.println("    h  g  f  e  d  c  b  a");
        for (int row = 1; row <= 8; row++) {
            System.out.print(" " +row+ " ");
            for (int col = 1;col <= 8; col++) {
                int col2 = (9-col);
                String squareColor;
                if ((row+col2) % 2 == 0){
                    squareColor = SET_BG_COLOR_LIGHT_GREY;
                } else {
                    squareColor = SET_BG_COLOR_DARK_GREY;
                }
                ChessPiece piece= board.getPiece(new ChessPosition(row,col2));
                System.out.print(squareColor +getIcon(piece));
            }
            System.out.print(RESET_BG_COLOR + " " + row+"\n");
        }
        System.out.println("    h  g  f  e  d  c  b  a");
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
        boolean whiteBool = (piece.getTeamColor() == ChessGame.TeamColor.WHITE);
        return switch (piece.getPieceType()) {
            case KING -> whiteBool ? WHITE_KING : BLACK_KING;
            case QUEEN -> whiteBool ? WHITE_QUEEN : BLACK_QUEEN;
            case BISHOP -> whiteBool ? WHITE_BISHOP : BLACK_BISHOP;
            case KNIGHT -> whiteBool ? WHITE_KNIGHT : BLACK_KNIGHT;
            case ROOK -> whiteBool ? WHITE_ROOK : BLACK_ROOK;
            case PAWN -> whiteBool ? WHITE_PAWN : BLACK_PAWN;
            default -> EMPTY;
        };
    }
}