package top.lemonsoda.arsenalnews.net;

import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
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
    private final static int DEFAULT_TIMEOUT = 5;

    private Retrofit retrofit;
    private NewsService newsService;

    private NetworkManager() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        // add logging as last interceptor
        httpClient.addInterceptor(logging);

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        newsService = retrofit.create(NewsService.class);
    }

    private static class SingletonHolder {
        private static final NetworkManager INSTANCE = new NetworkManager();
    }

    public static NetworkManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void getNewsItem(Observer<List<NewItem>> observer, int page) {
        newsService.getNewsItem(page)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

    public void getArticle(Subscriber<NewDetail> subscriber, String id) {
        newsService.getArticle(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    public void getArticleWithUserID(Subscriber<NewDetail> subscriber, String id, RequestUser user) {
        newsService.getArticleWithUserId(id, user)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    public void getFavorites(Observer<List<NewItem>> observer, String id) {
        newsService.getFavorites(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

    public void postFavorite(Observer<ResponseFavorite> observer, RequestFavorite favorite) {
        newsService.postFavorite(favorite)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

    public void deleteFavorite(Observer<ResponseFavorite> observer, String user_id, String article_id) {
        newsService.deleteFavorite(user_id, article_id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }


    public interface NewsService {
        @GET("item/{page}")
        Observable<List<NewItem>> getNewsItem(@Path("page") int page);

        @GET("artical/{id}")
        Observable<NewDetail> getArticle(@Path("id") String id);

        @Headers("Content-Type: application/json")
        @POST("artical/{id}/")
        Observable<NewDetail> getArticleWithUserId(@Path("id") String id, @Body RequestUser user);

        @GET("favorites/{id}/")
        Observable<List<NewItem>> getFavorites(@Path("id") String id);

        @Headers("Content-Type: application/json")
        @POST("favorites/")
        Observable<ResponseFavorite> postFavorite(@Body RequestFavorite favorite);

        @DELETE("favorites/{user_id}/{article_id}/")
        Observable<ResponseFavorite> deleteFavorite(
                @Path("user_id") String user_id, @Path("article_id") String article_id);
    }

}
