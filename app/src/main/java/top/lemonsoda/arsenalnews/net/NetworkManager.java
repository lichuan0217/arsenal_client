package top.lemonsoda.arsenalnews.net;

import java.util.List;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import rx.Observable;
import top.lemonsoda.arsenalnews.bean.NewDetail;
import top.lemonsoda.arsenalnews.bean.NewItem;
import top.lemonsoda.arsenalnews.bean.RequestFavorite;
import top.lemonsoda.arsenalnews.bean.RequestUser;
import top.lemonsoda.arsenalnews.bean.ResponseFavorite;

/**
 * Created by chuanl on 4/7/16.
 */
public class NetworkManager {

    private final static String BASE_URL = "http://101.200.141.146:9000/arsenal/";

    public interface ApiManagerService {
        @GET("item/{page}")
        Observable<List<NewItem>> getNewsItem(@Path("page") int page);

        @GET("artical/{id}")
        Observable<NewDetail> getArtical(@Path("id") String id);

        @Headers( "Content-Type: application/json" )
        @POST("artical/{id}/")
        Observable<NewDetail> getArticleWithUserId(@Path("id") String id, @Body RequestUser user);

        @Headers( "Content-Type: application/json" )
        @POST("favorites/")
        Observable<ResponseFavorite> postFavorite(@Body RequestFavorite favorite);
    }

    private static final Retrofit mRetrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
            .build();

    public static final ApiManagerService newsItemService = mRetrofit.create(ApiManagerService.class);
}
