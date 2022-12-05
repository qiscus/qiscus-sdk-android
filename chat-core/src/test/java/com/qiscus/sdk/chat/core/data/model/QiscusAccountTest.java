package com.qiscus.sdk.chat.core.data.model;

import android.os.Parcel;

import org.junit.Test;
public class QiscusAccountTest {
    QiscusAccount account;
    @Test
    public void setQiscusAccount() {
        //"{\"active\":true,\"app\":{\"code\":\"sdksample\",\"id\":947,\"id_str\":\"947\",\"name\":\"sdksample\"},\"avatar\":{\"avatar\":{\"url\":\"https:\\/\\/d1edrlpyc25xu0.cloudfront.net\\/kiwari-prod\\/image\\/upload\\/75r6s_jOHa\\/1507541871-avatar-mine.png\"}},\"avatar_url\":\"https:\\/\\/d1edrlpyc25xu0.cloudfront.net\\/kiwari-prod\\/image\\/upload\\/75r6s_jOHa\\/1507541871-avatar-mine.png\",\"email\":\"testing34\",\"extras\":{},\"id\":1449907638,\"id_str\":\"1449907638\",\"last_comment_id\":0,\"last_comment_id_str\":\"0\",\"last_sync_event_id\":0,\"pn_android_configured\":true,\"pn_ios_configured\":false,\"rtKey\":\"somestring\",\"token\":\"X0y1Nsd8u125k8gB6wiz1666680616\",\"username\":\"testing34\"}"
        account = new QiscusAccount();
        account.setToken("abvcs");
        account.getToken();

        account.setToken(null);
        account.getToken();

        account.setId(1);

        account.setEmail("okokk@mail.com");
        account.getEmail();

        account.setEmail(null);
        account.getEmail();

        account.setAvatar("");
        account.setExtras(null);

        account.setRefreshToken("23asfdasf");
        account.getRefreshToken();
        account.setRefreshToken(null);
        account.getRefreshToken();

        account.setTokenExpiresAt("2423432423");
        account.getTokenExpiresAt();

        account.setTokenExpiresAt(null);
        account.getTokenExpiresAt();

        account.setUsername("arief");
        account.getAvatar();
        account.getId();
        account.getExtras();
        account.getUsername();

    }


    @Test
    public void describeContents() {
        setQiscusAccount();
        account.describeContents();
    }

    @Test
    public void writeToParcel() {

    }

    @Test
    public void testHashCode() {
        account = new QiscusAccount();
        account.setToken("abvcs");
        account.setId(1);
        account.setEmail("okokk@mail.com");
        account.setAvatar("https://");
        account.setRefreshToken("23asfdasf");
        account.setTokenExpiresAt("2423432423");
        account.setUsername("arief");
        account.hashCode();

        QiscusAccount account2 = new QiscusAccount();
        account2.setToken(null);
        account2.setId(1);
        account2.setEmail(null);
        account2.setAvatar(null);
        account2.setRefreshToken(null);
        account2.setTokenExpiresAt(null);
        account2.setUsername(null);
        account2.hashCode();

    }

    @Test
    public void testEquals() {
    }

    @Test
    public void testToString() {
        setQiscusAccount();
        account.toString();
    }
}