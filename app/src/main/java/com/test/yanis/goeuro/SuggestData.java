package com.test.yanis.goeuro;

import java.util.ArrayList;

/**
 * Created by Yanis on 09.06.2016.
 */
public class SuggestData {

    String name;
    String fullName;
    Geo geo_position;

    class Geo {
        float latitude;
        float longitude;
    }

    public String toString(){
        return fullName;
    }
}
