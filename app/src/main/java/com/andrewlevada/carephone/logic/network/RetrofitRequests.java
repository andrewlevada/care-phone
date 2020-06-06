package com.andrewlevada.carephone.logic.network;

import com.andrewlevada.carephone.logic.CaredUser;
import com.andrewlevada.carephone.logic.LogRecord;
import com.andrewlevada.carephone.logic.PhoneNumber;
import com.andrewlevada.carephone.logic.StatisticsPack;

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

    @GET("/whitelist/r")
    Call<List<PhoneNumber>> getWhitelistR(@Query("userToken") String userToken, @Query("rUid") String rUid);

    @PUT("/whitelist/r")
    Call<Void> putWhitelistR(@Query("userToken") String userToken, @Query("rUid") String rUid, @Query("phone") String phoneNumber, @Query("label") String label);

    @POST("/whitelist/r")
    Call<Void> postWhitelistR(@Query("userToken") String userToken, @Query("rUid") String rUid, @Query("prevPhone") String prevPhone, @Query("phone") String phoneNumber, @Query("label") String label);

    @DELETE("/whitelist/r")
    Call<Void> deleteWhitelistR(@Query("userToken") String userToken, @Query("rUid") String rUid, @Query("phone") String phoneNumber);

    // Whitelist State

    @GET("/whitelist/state")
    Call<Boolean> getWhitelistState(@Query("userToken") String userToken);

    @POST("/whitelist/state")
    Call<Void> postWhitelistState(@Query("userToken") String userToken, @Query("state") Boolean state);

    @GET("/whitelist/state/r")
    Call<Boolean> getWhitelistStateR(@Query("userToken") String userToken, @Query("rUid") String rUid);

    @POST("/whitelist/state/r")
    Call<Void> postWhitelistStateR(@Query("userToken") String userToken, @Query("rUid") String rUid, @Query("state") Boolean state);

    // Statistics

    @GET("/statistics")
    Call<StatisticsPack> getStatisticsPack(@Query("userToken") String userToken);

    @GET("/statistics/r")
    Call<StatisticsPack> getStatisticsPackR(@Query("userToken") String userToken, @Query("rUid") String rUid);

    // Log

    @GET("/log")
    Call<List<LogRecord>> getLog(@Query("userToken") String userToken, @Query("limit") int limit, @Query("offset") int offset);

    @PUT("/log")
    Call<Void> putLog(@Query("userToken") String userToken, @Query("phoneNumber") String phoneNumber, @Query("startTimestamp") long startTimestamp, @Query("secondsDuration") int secondsDuration, @Query("type") int type);

    @GET("/log/r")
    Call<List<LogRecord>> getLogR(@Query("userToken") String userToken, @Query("rUid") String rUid, @Query("limit") int limit, @Query("offset") int offset);

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
