package top.lemonsoda.arsenalnews.bean;

/**
 * Created by chuanl on 4/5/16.
 */
public class NewItem {

    private String fullTextUrl;
    private String header;
    private String content;
    private String thumbnail;
    private String source;
    private String articalId;


    public NewItem(){
        header = "曝伊沃比同意3万周薪续约枪手";
        content = "英媒称，阿森纳小将伊沃比已同意签下一份周薪3万英镑的新合同。";
        fullTextUrl = "http://voice.hupu.com/soccer/2018601.html";
        thumbnail = "http://c1.hoopchina.com.cn/uploads/star/event/images/160405/thumbnail-7cfba15f35cc816e2d0bb003523c2d508d8736cb.jpg";
        source = "每日镜报";
    }

    public String getFullTextUrl() {
        return fullTextUrl;
    }

    public void setFullTextUrl(String fullTextUrl) {
        this.fullTextUrl = fullTextUrl;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getArticalId() {
        return articalId;
    }

    public void setArticalId(String articalId) {
        this.articalId = articalId;
    }

}
