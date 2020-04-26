package com.example.airsunny.blue.network;

import java.util.Map;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * @author lzq
 * @date 2020-04-02
 */
interface ApiService {
    @POST(Url.GET_FRIEND_POSITION)
    Observable<String> getFrindPosition(@Body Map map);

    @POST(Url.UPLOAD_POSITION)
    Observable<String> uploadPosition(@Body Map map);
}
