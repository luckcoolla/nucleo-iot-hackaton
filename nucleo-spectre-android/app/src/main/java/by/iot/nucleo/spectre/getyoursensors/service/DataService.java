package by.iot.nucleo.spectre.getyoursensors.service;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;

import by.iot.nucleo.spectre.getyoursensors.App;
import by.iot.nucleo.spectre.getyoursensors.BuildConfig;
import by.iot.nucleo.spectre.getyoursensors.Settings;
import by.iot.nucleo.spectre.getyoursensors.data.Board;
import retrofit.RestAdapter;
import retrofit.http.GET;
import retrofit.http.Query;

public class DataService {
    private static final String TAG = DataService.class.getName();
    private static DataService instance;
    private BasApiData basApiData;
    private String basToken;
    private String userName;

    public static DataService getInstance() {
        if (instance == null) {
            instance = new DataService(App.getContext(), Settings.BACKEND_URL);
        }
        return instance;
    }

    private DataService(Context context, @Nullable String endPoint) {
        if (endPoint == null || endPoint.trim().length() == 0) {
            Log.e(TAG, "DataService() creation error: endPoint == null or endPoint is empty string");
            return;
        }
        final RestAdapter restAdapter = new RestAdapter.Builder()
                .setLogLevel((BuildConfig.DEBUG) ? RestAdapter.LogLevel.FULL : RestAdapter.LogLevel.NONE)
                .setEndpoint(endPoint)
                //.setErrorHandler(errorHandler)
                .build();

        basApiData = restAdapter.create(BasApiData.class);
    }

    public String login(String login, String password) {
        this.basToken = basApiData.login(login, password);
        this.userName = login;
        return this.basToken;
    }

    public ArrayList<Board> boards() {
        return basApiData.boards(this.basToken);
    }

    public String getUserName() {
        return userName;
    }

//    private GsonConverter initConverter() {
//        //channels
//        final Type answerBoards = new TypeToken<ArrayList<Board>>() {
//        }.getType();
//
//        final Gson gson = new GsonBuilder()
//                .registerTypeAdapter(answerChannels, new AnswerDeserializer<ArrayList<Channel>>(answerChannelsInnerType))
//                .registerTypeAdapter(answerSearch, new AnswerDeserializer<ArrayList<EventItem>>(answerSearchInnerType))
//                .registerTypeAdapter(answerDevices, new AnswerDeserializer<ArrayList<ServiceDevice>>(answerDevicesInnerType))
//                .create();
//
//        return new GsonConverter(gson);
//    }

    private interface BasApiData {
        //@FormUrlEncoded
        @GET("/auth")
        String login(@Query("login") String login, @Query("password") String password);

        @GET("/boards")
        ArrayList<Board> boards(@Query("token") String token);
    }
}

/*
* curl 'https://iot-hackathon.herokuapp.com/auth?login=Kir&password=123' -v
curl -X POST 'https://iot-hackathon.herokuapp.com/auth?login=Kir&password=123' -v (это создание, по идее тебе не нужно)
curl 'https://iot-hackathon.herokuapp.com/boards?token=580c164b1242690003f88759' -v
* */