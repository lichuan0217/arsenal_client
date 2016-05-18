package top.lemonsoda.arsenalnews.net;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;
import top.lemonsoda.arsenalnews.bean.User;

/**
 * Created by Chuan on 5/16/16.
 */
public class WeiboNetworkManager {

    private final static String BASE_URL = "https://api.weibo.com/2/";

    public interface ApiManagerService {
        @GET("users/show.json")
        Observable<User> getUser(@Query("uid") long uid, @Query("access_token") String token);
    }

    private static final Retrofit mRetrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
            .build();

    public static final ApiManagerService userService = mRetrofit.create(ApiManagerService.class);
}
