package com.qiscus.sdk.data.remote.response;

import com.qiscus.sdk.util.Qson;

/**
 * Created on : August 18, 2016
 * Author     : zetbaitsu
 * Name       : Zetra
 * Email      : zetra@mail.ugm.ac.id
 * GitHub     : https://github.com/zetbaitsu
 * LinkedIn   : https://id.linkedin.com/in/zetbaitsu
 */
public class Response extends GenericResponse {
    public <T> T getResult(String key, Class<T> tClass) {
        if (!"success".equals(status)) {
            throw new RuntimeException(data.get("message").getAsString());
        }

        T result = Qson.pluck().getParser().fromJson(data.get(key), tClass);
        if (result == null) {
            throw new RuntimeException("Server error: unexpected response!");
        }
        return result;
    }
}
