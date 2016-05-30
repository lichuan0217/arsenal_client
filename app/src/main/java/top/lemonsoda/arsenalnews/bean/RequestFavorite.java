package top.lemonsoda.arsenalnews.bean;

/**
 * Created by Chuan on 5/26/16.
 */
public class RequestFavorite {

    private String user_id;
    private String article_id;

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getArticle_id() {
        return article_id;
    }

    public void setArticle_id(String article_id) {
        this.article_id = article_id;
    }
}
