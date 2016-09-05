package com.qiscus.sdk.data.remote.response;

/**
 * Created on : August 18, 2016
 * Author     : zetbaitsu
 * Name       : Zetra
 * Email      : zetra@mail.ugm.ac.id
 * GitHub     : https://github.com/zetbaitsu
 * LinkedIn   : https://id.linkedin.com/in/zetbaitsu
 */
public class MetaListResponse<M, T> extends ListResponse<T> {
    private M meta;

    public void setMeta(M meta) {
        this.meta = meta;
    }

    public M getMeta() {
        return meta;
    }

    @Override
    public String toString() {
        return "MetaListResponse{" +
                "meta=" + meta +
                '}';
    }
}
