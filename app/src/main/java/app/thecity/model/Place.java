package app.thecity.model;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Place implements Serializable, ClusterItem {
    public int place_id;
    public String name;
    public String image;
    public String address;
    public String phone;
    public String website;
    public String description;
    public double lng;
    public double lat;
    public long last_update;
    public float distance = -1;

    public String name_fr;
    public String address_fr;
    public String description_fr;

    public String name_ar;
    public String address_ar;
    public String description_ar;

    public List<Category> categories = new ArrayList<>();
    public List<Images> images = new ArrayList<>();

    @Override
    public LatLng getPosition() {
        return new LatLng(lat, lng);
    }

    public boolean isDraft(){
        return (address == null && phone == null && website == null && description == null);
    }


    public String getName() {

        if("fr".equals(Locale.getDefault().getLanguage()))
            return name_fr;
        else if("ar".equals(Locale.getDefault().getLanguage()))
                return name_ar;
        else return name;

    }

    public String getAddress() {

        if("fr".equals(Locale.getDefault().getLanguage()))
            return address_fr;
        else if("ar".equals(Locale.getDefault().getLanguage()))
            return address_ar;
        else return address;
    }

    public String getDescription() {

        if("fr".equals(Locale.getDefault().getLanguage()))
            return description_fr;
        else if("ar".equals(Locale.getDefault().getLanguage()))
            return description_ar;
        else return description;

    }
}
