package com.andrewlevada.carephone.logic.network;

import com.andrewlevada.carephone.logic.PhoneNumber;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;

public interface RetrofitRequests {
    @GET("whitelist")
    Call<List<PhoneNumber>> getWhitelist(@Query("userToken") String userToken);

    @PUT("/whitelist")
    Call<Void> putWhitelist(@Query("userToken") String userToken, @Query("phone") String phoneNumber, @Query("label") String label);

    @POST("/whitelist")
    Call<Void> postWhitelist(@Query("userToken") String userToken, @Query("prevPhone") String prevPhone, @Query("phone") String phoneNumber, @Query("label") String label);

    @DELETE("/whitelist")
    Call<Void> deleteWhitelist(@Query("userToken") String userToken, @Query("phone") String phoneNumber);
}
