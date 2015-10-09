//-------------------------------------------------------------------------------------------------------------//
// Code based on a tutorial by Shekhar Gulati of SparkJava at
// https://blog.openshift.com/developing-single-page-web-applications-using-java-8-spark-mongodb-and-angularjs/
//-------------------------------------------------------------------------------------------------------------//

package com.oose2015.abrahmb1.hareandhounds;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sql2o.Connection;
import org.sql2o.Sql2o;
import org.sql2o.Sql2oException;


import javax.sql.DataSource;
import java.util.*;

public class HareAndHoundsService {

    private Sql2o db;
    private String gameId;
    private Map<String,ArrayList<String>> map;
    private static final int INVALID_GAME_ID = 4041;
    private static final int INVALID_PLAYER_ID = 4042;
    private static final int INCORRECT_TURN = 4221;
    private static final int ILLEGAL_MOVE = 4222;

    private final Logger logger = LoggerFactory.getLogger(HareAndHoundsService.class);

    /**
     * Construct the model with a pre-defined datasource. The current implementation
     * also ensures that the DB schema is created if necessary.
     *
     * @param dataSource
     */
    public HareAndHoundsService(DataSource dataSource) throws HareAndHoundsServiceException {
        db = new Sql2o(dataSource);
        map = new HashMap<>();
        map.put("01",new ArrayList<String>(Arrays.asList("10","11","12")));
        map.put("10", new ArrayList<String>(Arrays.asList("01", "11", "20","21")));
        map.put("11",new ArrayList<String>(Arrays.asList("10","01","12","21")));
        map.put("12",new ArrayList<String>(Arrays.asList("01","11","21","22")));
        map.put("20",new ArrayList<String>(Arrays.asList("10","21","30")));
        map.put("21",new ArrayList<String>(Arrays.asList("10","11","12","20","22","30","31","32")));
        map.put("22",new ArrayList<String>(Arrays.asList("12","21","32")));
        map.put("30",new ArrayList<String>(Arrays.asList("20","21","31","41")));
        map.put("31",new ArrayList<String>(Arrays.asList("30","21","32","41")));
        map.put("32",new ArrayList<String>(Arrays.asList("21","22","31","41")));
        map.put("41",new ArrayList<String>(Arrays.asList("30","31","32")));
        //Create the schema for the database if necessary. This allows this
        //program to mostly self-contained. But this is not always what you want;
        //sometimes you want to create the schema externally via a script.
        try (Connection conn = db.open()) {
            String sql = "CREATE TABLE IF NOT EXISTS game (itemId INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "gameId TEXT, player1Id TEXT, pieceType1 TEXT, player2Id TEXT, " +
                    "pieceType2 TEXT, hareX INTEGER, hareY INTEGER, hound1X INTEGER, hound1Y INTEGER, " +
                    "hound2X INTEGER, hound2Y INTEGER, hound3X INTEGER, hound3Y INTEGER, state TEXT)" ;
            conn.createQuery(sql).executeUpdate();
        } catch(Sql2oException ex) {
            logger.error("Failed to create schema at startup", ex);
            throw new HareAndHoundsServiceException("Failed to create schema at startup", ex);
        }
   }

    /**
     * Check and return the gameID if it exists in the database
     * @param id sent as a request parameter
     * @return gameId if it exists in database
     * @throws HareAndHoundsServiceException
     */

    public String getGameId(String id) throws HareAndHoundsServiceException{
        String sql = "SELECT gameId FROM game WHERE gameId = :gameId ORDER BY itemId DESC LIMIT 1" ;
        //Check whether the id exists in the database and return the id if it does exist
        try (Connection conn = db.open()) {
            gameId = conn.createQuery(sql)
                    .addParameter("gameId",id)
                    .executeScalar(String.class);
            return gameId;
        }catch(Sql2oException ex) {
            logger.error("HareAndHoundsService.getGameId: Failed to query database", ex);
            throw new HareAndHoundsServiceException("HareAndHoundsService.getGameId: Failed to query database", ex);
        }
    }

    /**
     * Fetch the list of board pieces and their positions
     * @param gameId
     * @return List of BoardPiece objects
     * @throws HareAndHoundsServiceException
     */
    public List<BoardPiece> getBoard(String gameId) throws HareAndHoundsServiceException{
        List<BoardPiece> boardPieceList = new ArrayList<>();
            String sql = "SELECT * FROM game WHERE gameId = :gameId ORDER BY itemId DESC LIMIT 1" ;
        //Get the board piece details for the mentioned gameId
            try (Connection conn = db.open()) {
                NewBoard board =  conn.createQuery(sql)
                        .addParameter("gameId", gameId)
                        .executeAndFetchFirst(NewBoard.class);
                boardPieceList.add(new BoardPiece("HARE",board.getHareX(),board.getHareY()));
                boardPieceList.add(new BoardPiece("HOUND",board.getHound1X(),board.getHound1Y()));
                boardPieceList.add(new BoardPiece("HOUND",board.getHound2X(),board.getHound2Y()));
                boardPieceList.add(new BoardPiece("HOUND",board.getHound3X(),board.getHound3Y()));
                return boardPieceList;
            } catch(Sql2oException ex) {
                logger.error("HareAndHoundsService.getBoard: Failed to query database", ex);
                throw new HareAndHoundsServiceException("HareAndHoundsService.getBoard(): Failed to query database", ex);
            }

    }

    /**
     * Fetch the state of the board
     * @param gameId
     * @return HashMap consisting of "state" as key and board state as value
     * @throws HareAndHoundsServiceException
     */

    public Map<String,String> getBoardState(String gameId) throws HareAndHoundsServiceException {
        Map<String,String> result = new HashMap<>();
        String sql = "SELECT state FROM game WHERE gameId = :gameId ORDER BY itemId DESC LIMIT 1" ;
        //Get the board state for the mentioned gameId
        try (Connection conn = db.open()) {
            String state =  conn.createQuery(sql)
                    .addParameter("gameId", gameId)
                    .executeScalar(String.class);
            result.put("state", state);
            return result;
        } catch(Sql2oException ex) {
            logger.error("HareAndHoundsService.getState: Failed to query database", ex);
            throw new HareAndHoundsServiceException("HareAndHoundsService.getState: Failed to query database", ex);
        }
    }

    /**
     * Create a new Board.
     * @param body Contains pieceType of first player
     * @return Player object
     * @throws HareAndHoundsServiceException
     */
    public Player createNewBoard(String body) throws HareAndHoundsServiceException {
        gameId = UUID.randomUUID().toString();
        PieceType pType = new Gson().fromJson(body, PieceType.class);
        Player player1 = new Player(gameId,"1",pType.getPieceType());
        String state = "WAITING_FOR_SECOND_PLAYER";

        String sql = "INSERT INTO game (gameId, player1Id, pieceType1, hareX, hareY, hound1X," +
                " hound1Y, hound2X, hound2Y, hound3X, hound3Y, state) VALUES (:gameId, :playerId, :pieceType,:hareX," +
                ":hareY, :hound1X, :hound1Y, :hound2X, :hound2Y, :hound3X, :hound3Y, :state)" ;
        //Create a new board
        try (Connection conn = db.open()) {
            conn.createQuery(sql)
                .bind(player1)
                .addParameter("hareX",4)
                .addParameter("hareY",1)
                .addParameter("hound1X",1)
                .addParameter("hound1Y",0)
                .addParameter("hound2X",0)
                .addParameter("hound2Y",1)
                .addParameter("hound3X",1)
                .addParameter("hound3Y",2)
                .addParameter("state",state)
                .executeUpdate();
            return player1;
        } catch(Sql2oException ex) {
            logger.error("HareAndHoundsService.createNewBoard: Failed to create new entry", ex);
            throw new HareAndHoundsServiceException("HareAndHoundsService.createNewBoard: Failed to create new entry", ex);
        }
    }

    /**
     * Inserts the details of second player if no one has already joined
     * @param gameId
     * @return Player object
     * @throws HareAndHoundsServiceException
     */
    public Player joinGame(String gameId) throws HareAndHoundsServiceException{
        String sql = "SELECT state FROM game WHERE gameId = :gameId ORDER BY itemId DESC LIMIT 1" ;
        String sql1= "SELECT gameId, player1Id, pieceType1 FROM game WHERE gameId = :gameId ORDER BY itemId DESC LIMIT 1";
        String sql2 = "UPDATE game SET state = :newState, player2Id = :playerId, pieceType2 = :pieceType" +
                "      WHERE state = :state AND gameId = :gameId";
        Player player2;
        try (Connection conn = db.open()) {
            String state =  conn.createQuery(sql)
                    .addParameter("gameId",gameId)
                    .executeScalar(String.class);
            if(!state.equals("WAITING_FOR_SECOND_PLAYER"))
                return null;
            Player player1 = conn.createQuery(sql1)
                    .addColumnMapping("gameId", "gameId")
                    .addColumnMapping("player1Id","playerId")
                    .addColumnMapping("pieceType1","pieceType")
                    .addParameter("gameId", gameId)
                    .executeAndFetchFirst(Player.class);
            if(player1.getPieceType().equals("HARE"))
                player2= new Player(gameId,"2","HOUND");
            else
                player2= new Player(gameId,"2","HARE");
            state = "TURN_HOUND";
            conn.createQuery(sql2)
                    .addParameter("newState", state)
                    .addParameter("playerId", "2")
                    .addParameter("pieceType", player2.getPieceType())
                    .addParameter("state", "WAITING_FOR_SECOND_PLAYER")
                    .addParameter("gameId",gameId)
                    .executeUpdate();
            return player2;

        } catch(Sql2oException ex) {
            logger.error("HareAndHoundsService.joinGame: Failed to query database", ex);
            throw new HareAndHoundsServiceException("HareAndHoundsService.joinGame: Failed to query database", ex);
        }

    }

    /**
     * Checks for the validity of the turn, and updates the table if the move is valid
     * @param id GameId
     * @param body Contains piecetype and move info
     * @return int
     * @throws HareAndHoundsServiceException
     */

    public int playGame (String id,String body) throws HareAndHoundsServiceException{
        Player player1, player2, player;
        Turn turn = new Gson().fromJson(body, Turn.class);
        String sql = "SELECT * FROM game WHERE gameId = :gameId ORDER BY itemId DESC";
        String sql1 = "INSERT INTO game (itemId, gameId, player1Id, pieceType1, player2Id, pieceType2," +
                "hareX, hareY, hound1X, hound1Y,hound2X, hound2Y, hound3X, hound3Y, state) " +
                "VALUES (:itemId, :gameId, :player1Id, :pieceType1, :player2Id, :pieceType2, :hareX,:hareY," +
                ":hound1X, :hound1Y, :hound2X, :hound2Y, :hound3X, :hound3Y, :state)" ;
        String sql2 = "SELECT itemId FROM game ORDER BY itemId DESC LIMIT 1";
        try (Connection conn = db.open()) {
            gameId=getGameId(id);
            if (!(gameId.equals(id)))
                return INVALID_GAME_ID;
            //Get all the entries of the board for the particular gameId
            List<NewBoard> boardList = conn.createQuery(sql)
                    .addParameter("gameId",gameId)
                    .executeAndFetch(NewBoard.class);
            NewBoard board = new NewBoard(boardList.get(0));
            player1 = new Player(board.getGameId(), board.getPlayer1Id(), board.getPieceType1());
            player2 = new Player(board.getGameId(), board.getPlayer2Id(), board.getPieceType2());
            String state = board.getState();
            //Check the validity of the playerId
            if (player1.getPlayerId().equals(turn.getPlayerId()))
                player = player1;
            else if (player2.getPlayerId().equals(turn.getPlayerId()))
                player = player2;
            else
                return INVALID_PLAYER_ID;
            if (player.getPieceType().equals("HARE")) {
                if (state.equals("TURN_HARE")) {
                    //Check the validity of the move
                    int ret = checkTurn(board, turn.getFromX(), turn.getFromY(), turn.getToX(), turn.getToY(), "HARE");
                    if (ret == 0) {
                        return ILLEGAL_MOVE;
                    }
                    //Update the board if move is valid
                    else {
                        board.setHareX(turn.getToX());
                        board.setHareY(turn.getToY());
                        board.setState("TURN_HOUND");
                    }
                } else {
                    return INCORRECT_TURN;
                }
            } else {
                if (state.equals("TURN_HOUND")) {
                    //Check the validity of the move
                    int ret = checkTurn(board, turn.getFromX(), turn.getFromY(), turn.getToX(), turn.getToY(), "HOUND");
                    if (ret == 0)
                        return ILLEGAL_MOVE;
                    //Update the board if the move is valid
                    else {
                        if ((board.getHound1X() == turn.getFromX()) && (board.getHound1Y() == turn.getFromY())) {
                            board.setHound1X(turn.getToX());
                            board.setHound1Y(turn.getToY());
                        } else if ((board.getHound2X() == turn.getFromX()) && (board.getHound2Y() == turn.getFromY())) {
                            board.setHound2X(turn.getToX());
                            board.setHound2Y(turn.getToY());
                        } else {
                            board.setHound3X(turn.getToX());
                            board.setHound3Y(turn.getToY());
                        }
                        board.setState("TURN_HARE");
                    }
                } else
                    return INCORRECT_TURN;
            }
            int counter = 0;
            //Count the number of times the board has repeated in the past.
            //A check for stalling condition
            for (NewBoard newBoard : boardList) {
                if (newBoard.equals(board))
                    counter++;
            }
            if (counter == 2)
                board.setState("WIN_HARE_BY_STALLING");

            //Check if there are any hounds in the left of the hare
            if ((board.getHareX() <= board.getHound1X()) && (board.getHareX() <= board.getHound2X()) &&
                    (board.getHareX() <= board.getHound3X()))
                board.setState("WIN_HARE_BY_ESCAPE");
            //Check if there is any possible move for hare left
            String hareXY = Integer.toString(board.getHareX()) + Integer.toString(board.getHareY());
            List<String> possibleMoves = map.get(hareXY);
            int count = 0;
            BoardPiece hound[] = new BoardPiece[3];
            hound[0] = new BoardPiece("HOUND", board.getHound1X(), board.getHound1Y());
            hound[1] = new BoardPiece("HOUND", board.getHound2X(), board.getHound2Y());
            hound[2] = new BoardPiece("HOUND", board.getHound3X(), board.getHound3Y());
            for (int i = 0; i < 3; i++) {
                String houndXY = Integer.toString(hound[i].getX()) + Integer.toString(hound[i].getY());
                if (possibleMoves.contains(houndXY))
                    count++;
            }
            if (possibleMoves.size() == count)
                board.setState("WIN_HOUND");
            //Insert the modified board as a new database entry
            int itemId = conn.createQuery(sql2)
                    .executeScalar(Integer.class);
            board.setItemId(itemId+1);
            conn.createQuery(sql1)
                .bind(board)
                .executeUpdate();
            return Integer.parseInt(turn.getPlayerId());
        } catch (Sql2oException ex) {
            logger.error("HareAndHoundsService.playGame: Failed to query database", ex);
            throw new HareAndHoundsServiceException("HareAndHoundsService.playGame: Failed to query database", ex);
        }
    }

    /**
     * Helper method to check the validity of the move
     * @param board
     * @param fromX
     * @param fromY
     * @param toX
     * @param toY
     * @param pType
     * @return int
     */
    public int checkTurn(NewBoard board,int fromX,int fromY,int toX,int toY, String pType){
        int i;
        BoardPiece hound[] = new BoardPiece[3];
        BoardPiece hare = new BoardPiece("HARE",board.getHareX(),board.getHareY());
        hound[0] = new BoardPiece("HOUND",board.getHound1X(),board.getHound1Y());
        hound[1] = new BoardPiece("HOUND",board.getHound2X(),board.getHound2Y());
        hound[2] = new BoardPiece("HOUND",board.getHound3X(),board.getHound3Y());
        //Check if there is already any piece present in the destination location
        if((hare.getX()==toX)&&(hare.getY()==toY))
            return 0;
        for(i=0;i<3;i++){
            if((hound[i].getX()==toX)&&(hound[i].getY()==toY))
                return 0;
        }
        //Check if the selected box contains any piece and is not blank
        if((pType.equals("HARE"))&&((hare.getX()!=fromX)||(hare.getY()!=fromY)))
            return 0;
        int flag=0;
        if(pType.equals("HOUND")){
            for(i=0;i<3;i++){
                if((hound[i].getX()==fromX)&&(hound[i].getY()==fromY))
                    flag=-1;
            }
            if(flag==0)
                return 0;
        }
        //Check if the destination location parameters are valid
        if((toX<0)||(toX>4))
            return 0;
        if((toY<0)||(toY>2))
            return 0;
        //Check if the hound is being moved to its left
        if((pType.equals("HOUND"))&&(toX<fromX))
            return 0;
        //Check if the destination location is reachable from the current location
        String from = Integer.toString(fromX)+ Integer.toString(fromY);
        String to = Integer.toString(toX)+ Integer.toString(toY);
        List<String> temp = map.get(from);
        if(!temp.contains(to))
            return 0;
        else
            return 1;
    }

    //-----------------------------------------------------------------------------//
    // Helper Classes and Methods
    //-----------------------------------------------------------------------------//

    public static class HareAndHoundsServiceException extends Exception {
        public HareAndHoundsServiceException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
