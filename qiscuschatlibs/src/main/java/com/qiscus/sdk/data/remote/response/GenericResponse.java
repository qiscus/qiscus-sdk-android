package com.qiscus.sdk.data.remote.response;

import com.google.gson.JsonObject;

/**
 * Created on : August 18, 2016
 * Author     : zetbaitsu
 * Name       : Zetra
 * Email      : zetra@mail.ugm.ac.id
 * GitHub     : https://github.com/zetbaitsu
 * LinkedIn   : https://id.linkedin.com/in/zetbaitsu
 */
public class GenericResponse {
    protected String status;
    protected JsonObject data;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public JsonObject getData() {
        return data;
    }

    public void setData(JsonObject data) {
        this.data = data;
    }

    public boolean isSuccess() {
        return status != null && status.equals("success");
    }

    @Override
    public String toString() {
        return "GenericResponse{" +
                "status='" + status + '\'' +
                ", data=" + data +
                '}';
    }
}
