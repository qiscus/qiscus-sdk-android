package com.qiscus.library.chat.sample.data.remote.response;

import com.google.gson.JsonArray;
import com.qiscus.library.chat.util.Qson;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on : August 18, 2016
 * Author     : zetbaitsu
 * Name       : Zetra
 * Email      : zetra@mail.ugm.ac.id
 * GitHub     : https://github.com/zetbaitsu
 * LinkedIn   : https://id.linkedin.com/in/zetbaitsu
 */
public class ListResponse<T> extends GenericResponse {
    public List<T> getResults(String key, Class<T> tClass) {
        if (!"success".equals(status)) {
            throw new RuntimeException(data.get("message").getAsString());
        }

        List<T> results = new ArrayList<>();

        JsonArray data = getData().get(key).getAsJsonArray();
        if (data == null) {
            throw new RuntimeException("Server error: unexpected response!");
        }

        for (int i = 0; i < data.size(); i++) {
            results.add(Qson.pluck().getParser().fromJson(data.get(i), tClass));
        }

        return results;
    }
}
