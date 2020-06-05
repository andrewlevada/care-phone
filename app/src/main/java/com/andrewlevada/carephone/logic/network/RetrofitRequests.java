package com.andrewlevada.carephone.logic.network;

import com.andrewlevada.carephone.logic.CaredUser;
import com.andrewlevada.carephone.logic.LogRecord;
import com.andrewlevada.carephone.logic.PhoneNumber;
import com.andrewlevada.carephone.logic.StatisticsPack;

import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;

public interface RetrofitRequests {

    // Users

    @PUT("/users")
    Call<Void> tryToPutUser(@Query("userToken") String userToken);

    // Whitelist

    @GET("/whitelist")
    Call<List<PhoneNumber>> getWhitelist(@Query("userToken") String userToken);

    @PUT("/whitelist")
    Call<Void> putWhitelist(@Query("userToken") String userToken, @Query("phone") String phoneNumber, @Query("label") String label);

    @POST("/whitelist")
    Call<Void> postWhitelist(@Query("userToken") String userToken, @Query("prevPhone") String prevPhone, @Query("phone") String phoneNumber, @Query("label") String label);

    @DELETE("/whitelist")
    Call<Void> deleteWhitelist(@Query("userToken") String userToken, @Query("phone") String phoneNumber);

    // Whitelist State

    @GET("/whitelist/state")
    Call<Boolean> getWhitelistState(@Query("userToken") String userToken);

    @POST("/whitelist/state")
    Call<Void> postWhitelistState(@Query("userToken") String userToken, @Query("state") Boolean state);

    // Statistics

    @GET("/statistics")
    Call<StatisticsPack> getStatisticsPack(@Query("userToken") String userToken);

    // Log

    @GET("/log")
    Call<List<LogRecord>> getLog(@Query("userToken") String userToken, @Query("limit") int limit, @Query("offset") int offset);

    @PUT("/log")
    Call<Void> putLog(@Query("userToken") String userToken, @Query("phoneNumber") String phoneNumber, @Query("startTimestamp") Date startTimestamp, @Query("secondsDuration") int secondsDuration, @Query("type") int type);

    // Cared List

    @GET("/caredList")
    Call<List<CaredUser>> getCaredList(@Query("userToken") String userToken);

    // Link

    @PUT("/link")
    Call<String> putLink(@Query("userToken") String userToken);

    @DELETE("/link")
    Call<Void> deleteLink(@Query("userToken") String userToken);

    @POST("/link")
    Call<Integer> postLink(@Query("userToken") String userToken, @Query("code") String code);
}
