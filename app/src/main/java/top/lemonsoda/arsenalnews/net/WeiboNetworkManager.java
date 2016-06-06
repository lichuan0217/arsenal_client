package top.lemonsoda.arsenalnews.net;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import top.lemonsoda.arsenalnews.bean.User;

/**
 * Created by Chuan on 5/16/16.
 */
public class WeiboNetworkManager {

    private final static String BASE_URL = "https://api.weibo.com/2/";

    private Retrofit retrofit;
    private WeiboUserService userService;

    private WeiboNetworkManager(){
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        userService = retrofit.create(WeiboUserService.class);
    }

    private static class SingletonHolder{
        private static final WeiboNetworkManager INSTANCE = new WeiboNetworkManager();
    }

    public static WeiboNetworkManager getInstance(){
        return SingletonHolder.INSTANCE;
    }

    public void getUser(Subscriber<User> subscriber, long uid, String token){
        userService.getUser(uid, token)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    public interface WeiboUserService {
        @GET("users/show.json")
        Observable<User> getUser(@Query("uid") long uid, @Query("access_token") String token);
    }

}
