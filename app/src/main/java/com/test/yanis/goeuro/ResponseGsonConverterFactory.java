package com.test.yanis.goeuro;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.io.Reader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

/**
 * Created by Yanis on 09.06.2016.
 */
public class ResponseGsonConverterFactory extends Converter.Factory {

    public static ResponseGsonConverterFactory create() {
        return create(new Gson());
    }

    public static ResponseGsonConverterFactory create(Gson gson) {
        return new ResponseGsonConverterFactory(gson);
    }

    private final Gson gson;

    private ResponseGsonConverterFactory(Gson gson) {
        if (gson == null) throw new NullPointerException("gson == null");
        this.gson = gson;
    }

    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
        final TypeAdapter<?> adapter = gson.getAdapter(TypeToken.get(type));
        return new Converter<ResponseBody, Object>() {
            @Override
            public Object convert(ResponseBody value) throws IOException {
                JsonReader jsonReader = gson.newJsonReader(value.charStream());
                jsonReader.setLenient(true);
                try {
                    return adapter.read(jsonReader);
                } finally {
                    value.close();
                }
            }
        };
    }

    @Override
    public Converter<?, RequestBody> requestBodyConverter(Type type, Annotation[] parameterAnnotations,
                                                          Annotation[] methodAnnotations, Retrofit retrofit) {
        return null;
    }
}
