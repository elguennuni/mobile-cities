package app.thecity.model;

import java.io.Serializable;

public class NewsInfo implements Serializable{

    public int id;
    public String title;
    public String brief_content;
    public String full_content;
    public String image;
    public long last_update;

    public String title_ar;
    public String brief_content_ar;
    public String full_content_ar;

    public String title_fr;
    public String brief_content_fr;
    public String full_content_fr;

}
