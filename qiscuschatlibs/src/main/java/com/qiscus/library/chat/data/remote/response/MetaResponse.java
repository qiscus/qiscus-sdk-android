package com.qiscus.library.chat.data.remote.response;

/**
 * Created on : August 18, 2016
 * Author     : zetbaitsu
 * Name       : Zetra
 * Email      : zetra@mail.ugm.ac.id
 * GitHub     : https://github.com/zetbaitsu
 * LinkedIn   : https://id.linkedin.com/in/zetbaitsu
 */
public class MetaResponse<M> extends Response {
    private M meta;

    public M getMeta() {
        return meta;
    }

    public void setMeta(M meta) {
        this.meta = meta;
    }

    @Override
    public String toString() {
        return "MetaResponse{" +
                "meta=" + meta +
                '}';
    }
}
