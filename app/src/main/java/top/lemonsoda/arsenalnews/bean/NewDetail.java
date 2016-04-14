package top.lemonsoda.arsenalnews.bean;

/**
 * Created by chuanl on 4/5/16.
 */
public class NewDetail {

    private String picture_src;
    private String source;
    private String content;
    private String header;
    private String editor;
    private String date;
    private String type;
    private String video;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getVideo() {
        return video;
    }

    public void setVideo(String video) {
        this.video = video;
    }


    public String getPicture_src() {
        return picture_src;
    }

    public void setPicture_src(String picture_src) {
        this.picture_src = picture_src;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getEditor() {
        return editor;
    }

    public void setEditor(String editor) {
        this.editor = editor;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
