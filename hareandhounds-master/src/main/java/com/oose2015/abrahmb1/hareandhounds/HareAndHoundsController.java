//-------------------------------------------------------------------------------------------------------------//
// Code based on a tutorial by Shekhar Gulati of SparkJava at
// https://blog.openshift.com/developing-single-page-web-applications-using-java-8-spark-mongodb-and-angularjs/
//-------------------------------------------------------------------------------------------------------------//

package com.oose2015.abrahmb1.hareandhounds;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static spark.Spark.*;

public class HareAndHoundsController {

    private static final String API_CONTEXT = "/hareandhounds/api/games";
    private static final int INVALID_GAME_ID = 4041;
    private static final int INVALID_PLAYER_ID = 4042;
    private static final int INCORRECT_TURN = 4221;
    private static final int ILLEGAL_MOVE = 4222;

    private final HareAndHoundsService hareAndHoundsService;

    private final Logger logger = LoggerFactory.getLogger(HareAndHoundsController.class);

    public HareAndHoundsController(HareAndHoundsService hareAndHoundsService) {
        this.hareAndHoundsService = hareAndHoundsService;
        setupEndpoints();
    }

    private void setupEndpoints() {
        post(API_CONTEXT, "application/json", (request, response) -> {
            try {
                response.status(201);
                return hareAndHoundsService.createNewBoard(request.body());
            }catch (HareAndHoundsService.HareAndHoundsServiceException ex){
                logger.error("Failed to start game");
                response.status(500);
                return Collections.EMPTY_MAP;
            }
        }, new JsonTransformer());

        post(API_CONTEXT + "/*/turns", "application/json", (request, response) -> {
            try {
                Map<String,String> result = new HashMap<>();
                int ret = hareAndHoundsService.playGame(request.splat()[0], request.body());
                if (ret == ILLEGAL_MOVE) {
                    response.status(422);
                    result.put("reason", "ILLEGAL_MOVE");
                } else if (ret == INCORRECT_TURN) {
                    response.status(422);
                    result.put("reason", "INCORRECT_TURN");
                } else if (ret == INVALID_GAME_ID) {
                    response.status(404);
                    result.put("reason", "INVALID_GAME_ID");
                } else if (ret == INVALID_PLAYER_ID) {
                    response.status(404);
                    result.put("reason", "INVALID_PLAYER_ID");
                } else {
                    response.status(200);
                    result.put("playerId",Integer.toString(ret));
                }
                return result;
            }catch (HareAndHoundsService.HareAndHoundsServiceException ex){
                logger.error("Failed to play turn");
                response.status(500);
                return Collections.EMPTY_MAP;
            }
        }, new JsonTransformer());

        get(API_CONTEXT + "/:id/board", "application/json", (request, response) -> {
            try {
                if (!hareAndHoundsService.getGameId(request.params(":id")).equals(request.params(":id"))) {
                    response.status(404);
                    return Collections.EMPTY_MAP;
                }
                response.status(200);
                return hareAndHoundsService.getBoard(request.params(":id"));
            } catch (HareAndHoundsService.HareAndHoundsServiceException ex) {
                logger.error("Failed to get board");
                response.status(500);
                return Collections.EMPTY_MAP;
            }
        }, new JsonTransformer());

        get(API_CONTEXT + "/*/state", "application/json", (request, response) -> {
            try {
                if (!hareAndHoundsService.getGameId(request.splat()[0]).equals(request.splat()[0])) {
                    response.status(404);
                    return Collections.EMPTY_MAP;
                }
                response.status(200);
                return hareAndHoundsService.getBoardState(request.splat()[0]);
            }catch (HareAndHoundsService.HareAndHoundsServiceException ex) {
                logger.error("Failed to get board state");
                response.status(500);
                return Collections.EMPTY_MAP;
            }
        }, new JsonTransformer());

        put(API_CONTEXT + "/:id", "application/json", (request, response) -> {
            try{
                if (hareAndHoundsService.getGameId(request.params(":id")).equals(request.params(":id"))) {
                    Player player = hareAndHoundsService.joinGame(request.params(":id"));
                    if (player == null) {
                        response.status(410);
                        return Collections.EMPTY_MAP;
                    } else {
                        response.status(200);
                        return player;
                    }
                } else {
                    response.status(404);
                    return Collections.EMPTY_MAP;
                }
            }catch (HareAndHoundsService.HareAndHoundsServiceException ex) {
                logger.error("Failed to join game");
                response.status(500);
                return Collections.EMPTY_MAP;
            }
        }, new JsonTransformer());
    }
}
