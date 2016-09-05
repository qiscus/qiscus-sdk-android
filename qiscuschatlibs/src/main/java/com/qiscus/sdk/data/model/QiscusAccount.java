package com.qiscus.sdk.data.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created on : August 18, 2016
 * Author     : zetbaitsu
 * Name       : Zetra
 * Email      : zetra@mail.ugm.ac.id
 * GitHub     : https://github.com/zetbaitsu
 * LinkedIn   : https://id.linkedin.com/in/zetbaitsu
 */
public class QiscusAccount implements Parcelable {
    protected String email;
    protected String authenticationToken;
    protected String fullname;

    public QiscusAccount(String email, String authenticationToken, String fullname) {
        this.email = email;
        this.authenticationToken = authenticationToken;
        this.fullname = fullname;
    }

    protected QiscusAccount(Parcel in) {
        email = in.readString();
        authenticationToken = in.readString();
        fullname = in.readString();
    }

    public static final Creator<QiscusAccount> CREATOR = new Creator<QiscusAccount>() {
        @Override
        public QiscusAccount createFromParcel(Parcel in) {
            return new QiscusAccount(in);
        }

        @Override
        public QiscusAccount[] newArray(int size) {
            return new QiscusAccount[size];
        }
    };

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAuthenticationToken() {
        return authenticationToken;
    }

    public void setAuthenticationToken(String authenticationToken) {
        this.authenticationToken = authenticationToken;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    @Override
    public int describeContents() {
        return hashCode();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(email);
        dest.writeString(authenticationToken);
        dest.writeString(fullname);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof QiscusAccount && email.equalsIgnoreCase(((QiscusAccount) o).email);
    }

    @Override
    public String toString() {
        return "QiscusAccount{" +
                ", email='" + email + '\'' +
                ", authenticationToken='" + authenticationToken + '\'' +
                ", fullname='" + fullname + '\'' +
                '}';
    }
}
