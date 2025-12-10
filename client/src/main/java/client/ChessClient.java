package client;
import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;
import client.websocket.ServerMessageHandler;
import client.websocket.WebSocketFacade;
import model.AuthData;
import model.Game;
import java.util.*;
import static ui.EscapeSequences.*;
import websocket.commands.UserGameCommand;
import websocket.messages.*;
import websocket.messages.ServerMessage;
import chess.ChessMove;

public class ChessClient implements ServerMessageHandler {
    private String currentUser = null;
    private final Map<Integer, Game> prevGameList = new HashMap<>();
    private final ServerFacade server;
    private State state = State.SIGNEDOUT;
    private ChessPosition posToHighlight;
    private Set<ChessPosition> highlightOptions =new HashSet<>();
    private String authToken = null;
    private WebSocketFacade wsF;
    private final String serverUrl;
    private Integer currGameId;
    private ChessGame currGame;
    private ChessGame.TeamColor color;

    public ChessClient(String serverUrl) {
        server = new ServerFacade(serverUrl);
        this.serverUrl = serverUrl;
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
                System.out.println(e.getMessage());
            } catch (Throwable e) {
                System.out.println("oops something went wrong...try again");
            }
        }
        System.out.println();
    }

    private void printPrompt() {
        if (state == State.SIGNEDOUT) {
            System.out.print("\n" + RESET_TEXT_COLOR + RESET_BG_COLOR + "[LOGGED-OUT] >> ");
        } else if (state == State.SIGNEDIN){
            System.out.print("\n" + RESET_TEXT_COLOR + RESET_BG_COLOR + "[LOGGED-IN] >> ");
        } else {
            System.out.print("\n" + RESET_TEXT_COLOR + RESET_BG_COLOR + "[CHESS GAME] >> ");
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
        } else if (state == State.SIGNEDIN) {
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
        } else {
                return switch (cmd) {
                    case "help" -> gameHelp();
                    case "redraw" -> {
                        posToHighlight = null;
                        highlightOptions.clear();
                        redrawBoard();
                        yield "";
                    }
                    case "leave" -> leaveGame();
                    case "move" -> makeMove(params);
                    case "resign" -> resign();
                    case "highlight" -> highlight(params);
                    case "quit" -> "quit";
                    default -> gameHelp();
                };
        }
    }

    private String register(String... params) throws ClientException {
        if (params.length>=3) {
            AuthData authData = server.register(params[0], params[1],params[2]);
            this.currentUser = authData.username();
            this.authToken = authData.authToken();
            this.state = State.SIGNEDIN;
            return "successfully registered & logged in as "+ currentUser;
        }
        throw new ClientException("expected: register <USERNAME> <PASSWORD> <EMAIL>");
    }

    private String login(String... params) throws ClientException {
        if (params.length >= 2){
            AuthData authData = server.login(params[0],params[1]);
            this.currentUser = authData.username();
            this.authToken= authData.authToken();
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
        if (wsF != null) {
            wsF.close();
        }
        wsF = null;
        return "successfully logged out!";
    }
    private String create(String... params) throws ClientException {
        assertSignedIn();
        if (params.length>=1) {
            String gameName = String.join(" ",params);
            server.createGame(gameName);
            prevGameList.clear();
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
//            throw new ClientException("enter: list first to see the gameID");
            list();
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
        connectGame(game.gameId(),color);
//        createBoard(color);
        return "successfully joined " + game.gameName() + " as " + color;
    }

    private String observe(String... params) throws ClientException {
        assertSignedIn();
        int i;
        if (prevGameList.isEmpty()) {
//            throw new ClientException("enter: list first to see the gameID");
            list();
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
//        createBoard(ChessGame.TeamColor.WHITE);
        connectGame(game.gameId(), null);
        return "successfully observing "+ game.gameName();
    }

    private void assertSignedIn() throws ClientException {
        if (state == State.SIGNEDOUT){
            throw new ClientException("you need to sign in first!!");
        }
    }

    private void redrawBoard() {
        if (currGame==null) {
            System.out.println("no game loaded right now");
            return;
        }
        System.out.print(ERASE_SCREEN);
        var board = currGame.getBoard();

        ChessGame.TeamColor playerSide;//just for observer to set them as white
        if (color!= null) {
            playerSide = color;
        } else{
            playerSide = ChessGame.TeamColor.WHITE;
        }
        if (playerSide ==ChessGame.TeamColor.WHITE) {
            createWhiteBoard(board, posToHighlight, highlightOptions);
        } else {
            createBlackBoard(board, posToHighlight, highlightOptions);
        }

        System.out.print(RESET_BG_COLOR + RESET_TEXT_COLOR +"\n");
    }

    private void createWhiteBoard(ChessBoard board, ChessPosition spot, Set<ChessPosition> possibleMoves) {
        System.out.println("    a  b  c  d  e  f  g  h");//top, letters
        for (int row = 8; row >= 1; row--) {
            System.out.print(" " + row + " ");//nums, left
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                String squareColor;
                if ((spot != null) &&(spot.equals(position))) {
                    squareColor= SET_BG_COLOR_GREEN;
                } else if ((possibleMoves != null) && (possibleMoves.contains(position))) {
                    squareColor= SET_BG_COLOR_YELLOW;
                } else {
                    if (((row+col) % 2) == 0){
                        squareColor = SET_BG_COLOR_DARK_GREY;
                    } else {
                        squareColor = SET_BG_COLOR_LIGHT_GREY;
                    }
                }
                ChessPiece piece = board.getPiece(new ChessPosition(row,col));
                System.out.print(squareColor +getIcon(piece));
            }
            System.out.print(RESET_BG_COLOR + " " + row +"\n");
        }
        System.out.println("    a  b  c  d  e  f  g  h");//bottom
    }

    private void createBlackBoard(ChessBoard board, ChessPosition spot, Set<ChessPosition> possibleMoves) {
        System.out.println("    h  g  f  e  d  c  b  a");
        for (int row = 1; row <= 8; row++) {
            System.out.print(" " +row+ " ");
            for (int col = 1;col <= 8; col++) {
                int col2 = (9-col);
                ChessPosition position = new ChessPosition(row, col2);
                String squareColor;
                if ((spot != null) &&(spot.equals(position))) {
                    squareColor= SET_BG_COLOR_GREEN;
                } else if ((possibleMoves != null) && (possibleMoves.contains(position))) {
                    squareColor= SET_BG_COLOR_YELLOW;
                } else {
                    if (((row+col2) % 2) == 0){
                        squareColor = SET_BG_COLOR_DARK_GREY;
                    } else {
                        squareColor = SET_BG_COLOR_LIGHT_GREY;
                    }
                }
                ChessPiece piece= board.getPiece(position);
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
    //during game UI
    private String gameHelp() {
        return """
             redraw - redraw the board
             move <source> <destination> - make a move
             highlight <position>  - show legal moves for a piece
             leave - leave game and go back to lobby
             resign - resign the game
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

    private void connectGame(int gameID, ChessGame.TeamColor color) throws ClientException {
        if (authToken==null) {
            throw new ClientException("you need to be logged in to join a game");
        }
        this.currGameId = gameID;
        this.color = color;
        //WS url building-->
        String wsUrl = serverUrl.replaceFirst("^http","ws")+ "/ws";
        this.wsF= new WebSocketFacade(wsUrl, this);
        UserGameCommand connectCmd = new UserGameCommand(UserGameCommand.CommandType.CONNECT,authToken,gameID);
        wsF.send(connectCmd) ;
        this.state = State.GAMEPLAY;
    }

    private String leaveGame() throws ClientException {
        if (wsF != null) {
            UserGameCommand leaveC = new UserGameCommand(UserGameCommand.CommandType.LEAVE,authToken,currGameId);
            wsF.send(leaveC);
            wsF.close();
        }
        wsF = null;
        currGame= null;
        currGameId = null;
        color= null;
        state= State.SIGNEDIN;
        return "you left the game";
    }

    private String resign() throws ClientException {
        if (wsF==null) {
            return "no active game";
        }
        System.out.print("are you sure you want to resign?(type: y or n) ");
        Scanner scanner = new Scanner(System.in);
        String answer = scanner.nextLine().trim().toLowerCase();
        if ((!answer.equals("y"))&&(!answer.equals("yes"))){
            return "you did not resign";
        }
        UserGameCommand resignC = new UserGameCommand(UserGameCommand.CommandType.RESIGN,authToken, currGameId);
        wsF.send(resignC);
        return "you sent a resign request";
    }

    private String makeMove(String... params) throws ClientException {
        if (params.length<2) {
            throw new ClientException("correct format: move <source> <destination>");
        }
        if (currGame==null) {
            throw new ClientException("no active game");
        }
        String fromSquare = params[0];
        String toSquare = params[1];
        ChessPosition startPos = convertSquare(fromSquare);
        ChessPosition endPos = convertSquare(toSquare);

        ChessPiece piece = currGame.getBoard().getPiece(startPos);
        ChessPiece.PieceType promotion = null;
        if ((piece!=null) && (piece.getPieceType()==ChessPiece.PieceType.PAWN)) {
           if (((piece.getTeamColor() == ChessGame.TeamColor.WHITE) && (endPos.getRow()==8))||
                   ((piece.getTeamColor()==ChessGame.TeamColor.BLACK)&&(endPos.getRow()==1))) {
                if (params.length>=3) {//move a7 a8 r
                    String text = params[2].toLowerCase();
                    switch (text){
                        case "q" -> promotion= ChessPiece.PieceType.QUEEN;
                        case "r" -> promotion= ChessPiece.PieceType.ROOK;
                        case "b" -> promotion= ChessPiece.PieceType.BISHOP;
                        case "n" -> promotion= ChessPiece.PieceType.KNIGHT;
                        default -> throw new ClientException("promotion must be q, r, b, or n");
                    }
                } else {
                    promotion= ChessPiece.PieceType.QUEEN;
                }
            }
        }
        ChessMove move = new ChessMove(startPos, endPos,promotion);

        UserGameCommand moveC = new UserGameCommand(UserGameCommand.CommandType.MAKE_MOVE,authToken,currGameId,move);
        wsF.send(moveC);
        posToHighlight = null;
        highlightOptions.clear();
        return "move requested: " +fromSquare+ " to " +toSquare;
    }

    private String highlight(String... params) throws ClientException {
        if (params.length<1) {
            throw new ClientException("correct format: highlight <position> ");
        }
        if (currGame==null) {
            throw new ClientException("no active game");
        }
        String spot = params[0].toLowerCase().trim();
        ChessPosition fromSquare = convertSquare(spot);
        Collection<ChessMove> moves = currGame.validMoves(fromSquare);
        if ((moves==null) || (moves.isEmpty())){
            posToHighlight = null;
            highlightOptions.clear();
            redrawBoard();
            return "no legal moves from " +spot;
        }
        posToHighlight = fromSquare;//to highlight
        highlightOptions.clear();
        for (ChessMove move : moves) {
            highlightOptions.add(move.getEndPosition());
        }
        redrawBoard();
        return "highlighting legal moves from " + spot;
    }
    //helper func::
    private ChessPosition convertSquare(String square) throws ClientException {
        //needs to be 2!!!!
        if (square.length() != 2) {
            throw new ClientException("position needs to look like e2");
        }
        char colLetter = square.charAt(0);
        char rowNumber = square.charAt(1);
        int col = ((colLetter -'a')+1);//letter to col#
        int row = (rowNumber -'0');//number char to num
        if ((col < 1) ||(col > 8)|| (row > 8)||(row < 1 )) {
            throw new ClientException("position is out of range " + square);
        }
        return new ChessPosition(row, col);
    }
    @Override
    public void handle(ServerMessage message) {
        ServerMessage.ServerMessageType messageType= message.getServerMessageType();
        switch (messageType) {
            case LOAD_GAME:
                System.out.println();
                LoadGameMessage gameMessage = (LoadGameMessage)message;
                currGame = gameMessage.getGame();
                redrawBoard();
                break;
            case NOTIFICATION:
                NotificationMessage notification = (NotificationMessage)message;
                System.out.println("\n"+notification.getMessage());
                printPrompt();
                break;
            case ERROR:
                ErrorMessage error = (ErrorMessage) message;
                System.out.println("\nerror: "+error.getErrorMessage());
                printPrompt();
                break;
        }
    }
}