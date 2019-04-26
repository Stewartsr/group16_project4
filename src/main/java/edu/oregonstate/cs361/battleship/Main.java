package edu.oregonstate.cs361.battleship;

import com.google.gson.Gson;
import spark.Request;

import java.io.UnsupportedEncodingException;

import static spark.Spark.get;
import static spark.Spark.post;
import static spark.Spark.staticFiles;

public class Main {

    public static void main(String[] args) {
        //This is for debugging purposes
//        if (true) {
//            String projectDir = System.getProperty("user.dir");
//            String staticDir = "/src/main/resources/public";
//            staticFiles.externalLocation(projectDir + staticDir);
//        } else {
//            staticFiles.location("/public");
//        }

        staticFiles.location("/public");

        //This will listen to GET requests to /model and return a clean new model
        get("/model", (req, res) -> newModel());
        //This will listen to POST requests and expects to receive a game model, as well as location to fire to
        post("/fire/:row/:col", (req, res) -> fireAt(req));
        //This will listen to POST requests and expects to receive a game model, as well as location to scan at
        post("/scan/:row/:col", (req, res) -> Scan(req));
        //This will listen to POST requests and expects to receive a game model, as well as location to place the ship
        post("/placeShip/:id/:row/:col/:orientation", (req, res) -> placeShip(req));
        //This will listen to POST requests and expects to receive a game model, as well as game difficulty
        post("/submit/:type", (req, res) -> setMode(req));
    }

    //This function returns a new model
    public static String newModel() {
        BattleshipModel bm = new BattleshipModel();
        Gson gson = new Gson();
        return gson.toJson(bm);
    }

    //This function accepts an HTTP request and deseralizes it into an actual Java object.
    private static BattleshipModel getModelFromReq(Request req){
        Gson gson = new Gson();
        String result = "";
        try {
            result = java.net.URLDecoder.decode(req.body(),"US-ASCII");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        BattleshipModel modelFromReq = gson.fromJson(result, BattleshipModel.class);
        return modelFromReq;
    }

    //This controller
    public static String placeShip(Request req) {
        BattleshipModel currModel = getModelFromReq(req);
        String id = req.params("id");
        String row = req.params("row");
        String col = req.params("col");
        String orientation = req.params("orientation");
        currModel = currModel.placeShip(id,row,col,orientation,currModel);
        Gson gson = new Gson();
        return gson.toJson(currModel);
    }

    private static String setMode(Request req){
        BattleshipModel currModel = getModelFromReq(req);
        String type = req.params("type");
        currModel = currModel.setEzmode(type, currModel);
        Gson gson = new Gson();
        return gson.toJson(currModel);
    }

    private static String fireAt(Request req) {
        BattleshipModel currModel = getModelFromReq(req);
        String row = req.params("row");
        String col = req.params("col");
        int rowInt = Integer.parseInt(row);
        int colInt = Integer.parseInt(col);
        currModel.error_message = null;

        if(rowInt > 10 || rowInt < 1 || colInt > 10 || colInt < 1){
            // return a string to make the request fail to show error message
            currModel.error_message = "Fire is out of bounds!\nRow and Column MUST be integers between 1 and 10.";
            Gson gson = new Gson();
            return gson.toJson(currModel);
        }else if(currModel.checkfirepoint(rowInt, colInt)){
            currModel.error_message = "You already fired in this location!\nAre you even looking at the board?";
            Gson gson = new Gson();
            return gson.toJson(currModel);
        }else{
            currModel.shootAtComputer(rowInt,colInt);
            if(currModel.ezmode){
            currModel.ezFire(); // this is easy mode pattern firing
            }else if (currModel.lasthit){
                currModel.hardfire(); // this is the hard mode fire following shot
            }else{
                currModel.randFire(); // this is the hard mode random shot
            }
            Gson gson = new Gson();
            return gson.toJson(currModel);
        }
    }

    private static String Scan(Request req) {
        BattleshipModel currModel = getModelFromReq(req);
     //   String results;
        String row = req.params("row");
        String col = req.params("col");
        int rowInt = Integer.parseInt(row);
        int colInt = Integer.parseInt(col);
        currModel.error_message = null;

        if(rowInt > 10 || rowInt < 1 || colInt > 10 || colInt < 1){
            // return a string to make the request fail to show error message
            currModel.error_message = "Scan is out of bounds!\nRow and Column MUST be integers between 1 and 10.";
            Gson gson = new Gson();
            return gson.toJson(currModel);
        }
        else if (currModel.scanPlayer(rowInt,colInt)){
            currModel.scan_result = true;
        }else if(colInt > 1 && currModel.scanPlayer(rowInt,colInt-1)){
            currModel.scan_result = true;
        }else if(rowInt > 1 && currModel.scanPlayer(rowInt-1,colInt)){
            currModel.scan_result = true;
        }else if(colInt < 10 && currModel.scanPlayer(rowInt,colInt+1)){
            currModel.scan_result = true;
        }else if(rowInt < 10 && currModel.scanPlayer(rowInt+1,colInt)){
            currModel.scan_result = true;
        }else{
            currModel.scan_result = false;
        }
        if(currModel.ezmode) {
            currModel.ezFire(); // this is easy mode pattern firing
        }else if (currModel.lasthit){
            currModel.hardfire(); // this is the hard mode fire following shot
        }else{
            currModel.randFire(); // this is the hard mode random shot
        }

    //    return currModel.results;
        Gson gson = new Gson();
        return gson.toJson(currModel);
    }
}