package com.test.yanis.goeuro;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by Yanis on 09.06.2016.
 */
public interface GoEuroApi {
    String SUGGEST = "api/v2/position/suggest/{locale}/{term}";

    @GET(SUGGEST)
    Call<ArrayList<SuggestData>> suggest(
            @Path("locale") String locale,
            @Path("term") String term
    );
}
