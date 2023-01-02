package com.qiscus.sdk.chat.core.data.remote;

import static org.junit.Assert.*;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.qiscus.sdk.chat.core.InstrumentationBaseTest;
import com.qiscus.sdk.chat.core.QiscusCore;
import com.qiscus.sdk.chat.core.data.model.QiscusAccount;

import junit.framework.TestCase;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

public class QiscusApiParserTest extends InstrumentationBaseTest {

    @Before
    public void setUp() throws Exception {
        super.setUp();
        QiscusCore.setup(application, "sdksample");
        QiscusCore.setUser("arief92", "arief92")
                .withUsername("arief92")
                .withAvatarUrl("https://")
                .withExtras(null)
                .save(new QiscusCore.SetUserListener() {
                    @Override
                    public void onSuccess(QiscusAccount qiscusAccount) {
                        //on success


                    }
                    @Override
                    public void onError(Throwable throwable) {
                        //on error
                    }});

        QiscusApiParser parser = new QiscusApiParser();
    }

    @Test
    public void parseRefreshToken(){
        String jsonString = "{\n" +
                "            \"results\": {\n" +
                "            \"refresh_token\": \"9gM9ZQHxzAcjS2ti74Z31662003877\",\n" +
                "            \"token_expires_at\": \"9gM9ZQHxzAcjS2ti74Z31662003877\",\n" +
                "                    \"token\": \"K96nBULPNh3QGA3NrTnh1662003877\"\n" +
                "        },\n" +
                "            \"status\": 200\n" +
                "        }";

        String jsonString2 = "{\n" +
                "            \"resultss\": {\n" +
                "            \"refresh_token\": \"9gM9ZQHxzAcjS2ti74Z31662003877\",\n" +
                "            \"token_expires_at\": \"9gM9ZQHxzAcjS2ti74Z31662003877\",\n" +
                "                    \"token\": \"K96nBULPNh3QGA3NrTnh1662003877\"\n" +
                "        },\n" +
                "            \"status\": 200\n" +
                "        }";


        String jsonString3 = "{\n" +
                "            \"results\": {\n" +
                "            \"refresh_tokenn\": \"9gM9ZQHxzAcjS2ti74Z31662003877\",\n" +
                "            \"token_expires_att\": \"9gM9ZQHxzAcjS2ti74Z31662003877\",\n" +
                "                    \"tokenn\": \"K96nBULPNh3QGA3NrTnh1662003877\"\n" +
                "        },\n" +
                "            \"status\": 200\n" +
                "        }";

        JsonObject jsonObject = new JsonParser().parse(jsonString).getAsJsonObject();
        JsonObject jsonObject2 = new JsonParser().parse(jsonString2).getAsJsonObject();
        JsonObject jsonObject3 = new JsonParser().parse(jsonString3).getAsJsonObject();



        QiscusApiParser.parseRefreshToken(jsonObject);
        QiscusApiParser.parseRefreshToken(jsonObject2);
        QiscusApiParser.parseRefreshToken(jsonObject3);
    }

    @Test
    public void parseQUserPresence(){
        String jsonString = "{\n" +
                "            \"results\": {\n" +
                "            \"user_status\": [\n" +
                "            {\n" +
                "                \"email\": \"marco@mail.com\",\n" +
                "                    \"status\": 0,\n" +
                "                    \"timestamp\": \"1587723201\"\n" +
                "            }\n" +
                "        ]\n" +
                "        },\n" +
                "            \"status\": 200\n" +
                "        }";

        String jsonString2 = "{\n" +
                "            \"results\": {\n" +
                "            \"user_status\": [\n" +
                "            {\n" +
                "                \"emaill\": \"marco@mail.com\",\n" +
                "                    \"status\": 1,\n" +
                "                    \"timestampp\": \"1587723201\"\n" +
                "            }\n" +
                "        ]\n" +
                "        },\n" +
                "            \"status\": 200\n" +
                "        }";

        String jsonString3 = "{\n" +
                "            \"results\": {\n" +
                "            \"user_status\": [\n" +
                "            {\n" +
                "                \"email\": \"marco@mail.com\",\n" +
                "                    \"status\": 1,\n" +
                "                    \"timestamp\": \"1587723201\"\n" +
                "            }\n" +
                "        ]\n" +
                "        },\n" +
                "            \"status\": 200\n" +
                "        }";


        JsonObject jsonObject = new JsonParser().parse(jsonString).getAsJsonObject();
        JsonObject jsonObject2 = new JsonParser().parse(jsonString2).getAsJsonObject();
        JsonObject jsonObject3 = new JsonParser().parse(jsonString3).getAsJsonObject();

        QiscusApiParser.parseQiscusUserPresence(jsonObject);
        QiscusApiParser.parseQiscusUserPresence(jsonObject2);
        QiscusApiParser.parseQiscusUserPresence(jsonObject3);
        QiscusApiParser.parseQiscusUserPresence(null);


    }

    @Test
    public void parseQiscusAccount(){

        QiscusApiParser.parseQiscusAccount(null, false);

    }


    @Test
    public void parseQiscusChatRoom(){
        String jsonString = "{\"results\":{\"comments\":[{\"comment_before_id\":1167549498,\"comment_before_id_str\":\"1167549498\",\"disable_link_preview\":false,\"email\":\"testing34\",\"extras\":{},\"id\":1172292157,\"id_str\":\"1172292157\",\"is_deleted\":false,\"is_public_channel\":false,\"message\":\"Ok\",\"payload\":{},\"room_avatar\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"room_id\":96304367,\"room_id_str\":\"96304367\",\"room_name\":\"arief93 testing34\",\"room_type\":\"single\",\"status\":\"sent\",\"timestamp\":\"2022-12-13T07:11:59Z\",\"topic_id\":96304367,\"topic_id_str\":\"96304367\",\"type\":\"text\",\"unique_temp_id\":\"android_1670915519612kdccsvcv15c9d59244c2d094\",\"unix_nano_timestamp\":1670915519723558000,\"unix_timestamp\":1670915519,\"user_avatar\":{\"avatar\":{\"url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\"}},\"user_avatar_url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"user_extras\":{},\"user_id\":1449907638,\"user_id_str\":\"1449907638\",\"username\":\"testing34\"},{\"comment_before_id\":1166639891,\"comment_before_id_str\":\"1166639891\",\"disable_link_preview\":false,\"email\":\"testing34\",\"extras\":{},\"id\":1167549498,\"id_str\":\"1167549498\",\"is_deleted\":false,\"is_public_channel\":false,\"message\":\"[file] https://d1edrlpyc25xu0.cloudfront.net/sdksample/image/upload/2GIfEQjvAV/balita5b21fef3-aa03-46b8-8056-d4eb063e1725.jpg [/file]\",\"payload\":{\"url\":\"https://d1edrlpyc25xu0.cloudfront.net/sdksample/image/upload/2GIfEQjvAV/balita5b21fef3-aa03-46b8-8056-d4eb063e1725.jpg\",\"caption\":\"\",\"file_name\":\"balita5b21fef3-aa03-46b8-8056-d4eb063e1725.png\",\"size\":0,\"pages\":1,\"encryption_key\":\"\"},\"room_avatar\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"room_id\":96304367,\"room_id_str\":\"96304367\",\"room_name\":\"arief93 testing34\",\"room_type\":\"single\",\"status\":\"sent\",\"timestamp\":\"2022-12-08T05:45:54Z\",\"topic_id\":96304367,\"topic_id_str\":\"96304367\",\"type\":\"file_attachment\",\"unique_temp_id\":\"android_1670478090617159uwee63c06857074d69d51\",\"unix_nano_timestamp\":1670478354425846000,\"unix_timestamp\":1670478354,\"user_avatar\":{\"avatar\":{\"url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\"}},\"user_avatar_url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"user_extras\":{},\"user_id\":1449907638,\"user_id_str\":\"1449907638\",\"username\":\"testing34\"},{\"comment_before_id\":1166637910,\"comment_before_id_str\":\"1166637910\",\"disable_link_preview\":false,\"email\":\"testing34\",\"extras\":{},\"id\":1166639891,\"id_str\":\"1166639891\",\"is_deleted\":false,\"is_public_channel\":false,\"message\":\"Gg\",\"payload\":{},\"room_avatar\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"room_id\":96304367,\"room_id_str\":\"96304367\",\"room_name\":\"arief93 testing34\",\"room_type\":\"single\",\"status\":\"sent\",\"timestamp\":\"2022-12-07T06:48:26Z\",\"topic_id\":96304367,\"topic_id_str\":\"96304367\",\"type\":\"text\",\"unique_temp_id\":\"android_1670395706312v1r6xfvs3c06857074d69d51\",\"unix_nano_timestamp\":1670395706498316000,\"unix_timestamp\":1670395706,\"user_avatar\":{\"avatar\":{\"url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\"}},\"user_avatar_url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"user_extras\":{},\"user_id\":1449907638,\"user_id_str\":\"1449907638\",\"username\":\"testing34\"},{\"comment_before_id\":1166637880,\"comment_before_id_str\":\"1166637880\",\"disable_link_preview\":false,\"email\":\"testing34\",\"extras\":{},\"id\":1166637910,\"id_str\":\"1166637910\",\"is_deleted\":false,\"is_public_channel\":false,\"message\":\"Jhh\",\"payload\":{},\"room_avatar\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"room_id\":96304367,\"room_id_str\":\"96304367\",\"room_name\":\"arief93 testing34\",\"room_type\":\"single\",\"status\":\"sent\",\"timestamp\":\"2022-12-07T06:47:01Z\",\"topic_id\":96304367,\"topic_id_str\":\"96304367\",\"type\":\"text\",\"unique_temp_id\":\"android_1670395620974itxge5xt3c06857074d69d51\",\"unix_nano_timestamp\":1670395621130158000,\"unix_timestamp\":1670395621,\"user_avatar\":{\"avatar\":{\"url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\"}},\"user_avatar_url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"user_extras\":{},\"user_id\":1449907638,\"user_id_str\":\"1449907638\",\"username\":\"testing34\"},{\"comment_before_id\":1166637861,\"comment_before_id_str\":\"1166637861\",\"disable_link_preview\":false,\"email\":\"testing34\",\"extras\":{},\"id\":1166637880,\"id_str\":\"1166637880\",\"is_deleted\":false,\"is_public_channel\":false,\"message\":\"Jjj\",\"payload\":{},\"room_avatar\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"room_id\":96304367,\"room_id_str\":\"96304367\",\"room_name\":\"arief93 testing34\",\"room_type\":\"single\",\"status\":\"sent\",\"timestamp\":\"2022-12-07T06:47:00Z\",\"topic_id\":96304367,\"topic_id_str\":\"96304367\",\"type\":\"text\",\"unique_temp_id\":\"android_1670395619939vjwtabqc3c06857074d69d51\",\"unix_nano_timestamp\":1670395620120134000,\"unix_timestamp\":1670395620,\"user_avatar\":{\"avatar\":{\"url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\"}},\"user_avatar_url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"user_extras\":{},\"user_id\":1449907638,\"user_id_str\":\"1449907638\",\"username\":\"testing34\"},{\"comment_before_id\":1162327164,\"comment_before_id_str\":\"1162327164\",\"disable_link_preview\":false,\"email\":\"testing34\",\"extras\":{},\"id\":1166637861,\"id_str\":\"1166637861\",\"is_deleted\":false,\"is_public_channel\":false,\"message\":\"Hhha\",\"payload\":{},\"room_avatar\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"room_id\":96304367,\"room_id_str\":\"96304367\",\"room_name\":\"arief93 testing34\",\"room_type\":\"single\",\"status\":\"sent\",\"timestamp\":\"2022-12-07T06:46:59Z\",\"topic_id\":96304367,\"topic_id_str\":\"96304367\",\"type\":\"text\",\"unique_temp_id\":\"android_1670395618808fk84mxs53c06857074d69d51\",\"unix_nano_timestamp\":1670395619000515000,\"unix_timestamp\":1670395619,\"user_avatar\":{\"avatar\":{\"url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\"}},\"user_avatar_url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"user_extras\":{},\"user_id\":1449907638,\"user_id_str\":\"1449907638\",\"username\":\"testing34\"},{\"comment_before_id\":1162327109,\"comment_before_id_str\":\"1162327109\",\"disable_link_preview\":false,\"email\":\"testing34\",\"extras\":{},\"id\":1162327164,\"id_str\":\"1162327164\",\"is_deleted\":false,\"is_public_channel\":false,\"message\":\"d\",\"payload\":{},\"room_avatar\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"room_id\":96304367,\"room_id_str\":\"96304367\",\"room_name\":\"arief93 testing34\",\"room_type\":\"single\",\"status\":\"delivered\",\"timestamp\":\"2022-12-02T06:30:55Z\",\"topic_id\":96304367,\"topic_id_str\":\"96304367\",\"type\":\"text\",\"unique_temp_id\":\"javascript-1669962655850\",\"unix_nano_timestamp\":1669962655988072000,\"unix_timestamp\":1669962655,\"user_avatar\":{\"avatar\":{\"url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\"}},\"user_avatar_url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"user_extras\":{},\"user_id\":1449907638,\"user_id_str\":\"1449907638\",\"username\":\"testing34\"},{\"comment_before_id\":1162327028,\"comment_before_id_str\":\"1162327028\",\"disable_link_preview\":false,\"email\":\"testing34\",\"extras\":{},\"id\":1162327109,\"id_str\":\"1162327109\",\"is_deleted\":false,\"is_public_channel\":false,\"message\":\"a\",\"payload\":{},\"room_avatar\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"room_id\":96304367,\"room_id_str\":\"96304367\",\"room_name\":\"arief93 testing34\",\"room_type\":\"single\",\"status\":\"delivered\",\"timestamp\":\"2022-12-02T06:30:52Z\",\"topic_id\":96304367,\"topic_id_str\":\"96304367\",\"type\":\"text\",\"unique_temp_id\":\"javascript-1669962651953\",\"unix_nano_timestamp\":1669962652129883000,\"unix_timestamp\":1669962652,\"user_avatar\":{\"avatar\":{\"url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\"}},\"user_avatar_url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"user_extras\":{},\"user_id\":1449907638,\"user_id_str\":\"1449907638\",\"username\":\"testing34\"},{\"comment_before_id\":1162326986,\"comment_before_id_str\":\"1162326986\",\"disable_link_preview\":false,\"email\":\"testing34\",\"extras\":{},\"id\":1162327028,\"id_str\":\"1162327028\",\"is_deleted\":false,\"is_public_channel\":false,\"message\":\"F\",\"payload\":{},\"room_avatar\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"room_id\":96304367,\"room_id_str\":\"96304367\",\"room_name\":\"arief93 testing34\",\"room_type\":\"single\",\"status\":\"delivered\",\"timestamp\":\"2022-12-02T06:30:48Z\",\"topic_id\":96304367,\"topic_id_str\":\"96304367\",\"type\":\"text\",\"unique_temp_id\":\"android_1669962648409s0zlbrm13c06857074d69d51\",\"unix_nano_timestamp\":1669962648551284000,\"unix_timestamp\":1669962648,\"user_avatar\":{\"avatar\":{\"url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\"}},\"user_avatar_url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"user_extras\":{},\"user_id\":1449907638,\"user_id_str\":\"1449907638\",\"username\":\"testing34\"},{\"comment_before_id\":1162175835,\"comment_before_id_str\":\"1162175835\",\"disable_link_preview\":false,\"email\":\"testing34\",\"extras\":{},\"id\":1162326986,\"id_str\":\"1162326986\",\"is_deleted\":false,\"is_public_channel\":false,\"message\":\"Hh\",\"payload\":{},\"room_avatar\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"room_id\":96304367,\"room_id_str\":\"96304367\",\"room_name\":\"arief93 testing34\",\"room_type\":\"single\",\"status\":\"delivered\",\"timestamp\":\"2022-12-02T06:30:46Z\",\"topic_id\":96304367,\"topic_id_str\":\"96304367\",\"type\":\"text\",\"unique_temp_id\":\"android_1669962646359sguzthb43c06857074d69d51\",\"unix_nano_timestamp\":1669962646475867000,\"unix_timestamp\":1669962646,\"user_avatar\":{\"avatar\":{\"url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\"}},\"user_avatar_url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"user_extras\":{},\"user_id\":1449907638,\"user_id_str\":\"1449907638\",\"username\":\"testing34\"},{\"comment_before_id\":1156050638,\"comment_before_id_str\":\"1156050638\",\"disable_link_preview\":false,\"email\":\"testing34\",\"extras\":{},\"id\":1162175835,\"id_str\":\"1162175835\",\"is_deleted\":false,\"is_public_channel\":false,\"message\":\"[file] https://d1edrlpyc25xu0.cloudfront.net/sdksample/image/upload/0EyfYMiIJf/kisspng-video-royalty-free-social-media-youtube-clip-art-piktochart-visual-editor-5ba36298529527.2262.png [/file]\",\"payload\":{\"url\":\"https://d1edrlpyc25xu0.cloudfront.net/sdksample/image/upload/0EyfYMiIJf/kisspng-video-royalty-free-social-media-youtube-clip-art-piktochart-visual-editor-5ba36298529527.2262.png\",\"caption\":\"\",\"file_name\":\"kisspng-video-royalty-free-social-media-youtube-clip-art-piktochart-visual-editor-5ba36298529527.2262200415374342643383.png\",\"size\":990985,\"pages\":1,\"encryption_key\":\"\"},\"room_avatar\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"room_id\":96304367,\"room_id_str\":\"96304367\",\"room_name\":\"arief93 testing34\",\"room_type\":\"single\",\"status\":\"delivered\",\"timestamp\":\"2022-12-02T03:59:36Z\",\"topic_id\":96304367,\"topic_id_str\":\"96304367\",\"type\":\"file_attachment\",\"unique_temp_id\":\"1669953571836\",\"unix_nano_timestamp\":1669953576260008000,\"unix_timestamp\":1669953576,\"user_avatar\":{\"avatar\":{\"url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\"}},\"user_avatar_url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"user_extras\":{},\"user_id\":1449907638,\"user_id_str\":\"1449907638\",\"username\":\"testing34\"},{\"comment_before_id\":1156050632,\"comment_before_id_str\":\"1156050632\",\"disable_link_preview\":false,\"email\":\"testing34\",\"extras\":{},\"id\":1156050638,\"id_str\":\"1156050638\",\"is_deleted\":false,\"is_public_channel\":false,\"message\":\"[file] https://d1edrlpyc25xu0.cloudfront.net/sdksample/image/upload/wb7USsysPO/IMG_20221116_070617.jpg [/file]\",\"payload\":{\"url\":\"https://d1edrlpyc25xu0.cloudfront.net/sdksample/image/upload/wb7USsysPO/IMG_20221116_070617.jpg\",\"caption\":\"\",\"file_name\":\"IMG_20221116_070617.png\",\"size\":0,\"pages\":1,\"encryption_key\":\"\"},\"room_avatar\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"room_id\":96304367,\"room_id_str\":\"96304367\",\"room_name\":\"arief93 testing34\",\"room_type\":\"single\",\"status\":\"delivered\",\"timestamp\":\"2022-11-24T19:41:36Z\",\"topic_id\":96304367,\"topic_id_str\":\"96304367\",\"type\":\"file_attachment\",\"unique_temp_id\":\"android_1669318892708mvkn0fh23cc1ffd39af94204\",\"unix_nano_timestamp\":1669318896399483000,\"unix_timestamp\":1669318896,\"user_avatar\":{\"avatar\":{\"url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\"}},\"user_avatar_url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"user_extras\":{},\"user_id\":1449907638,\"user_id_str\":\"1449907638\",\"username\":\"testing34\"},{\"comment_before_id\":1155176386,\"comment_before_id_str\":\"1155176386\",\"disable_link_preview\":false,\"email\":\"testing34\",\"extras\":{},\"id\":1156050632,\"id_str\":\"1156050632\",\"is_deleted\":false,\"is_public_channel\":false,\"message\":\"Hi\",\"payload\":{},\"room_avatar\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"room_id\":96304367,\"room_id_str\":\"96304367\",\"room_name\":\"arief93 testing34\",\"room_type\":\"single\",\"status\":\"delivered\",\"timestamp\":\"2022-11-24T19:41:23Z\",\"topic_id\":96304367,\"topic_id_str\":\"96304367\",\"type\":\"text\",\"unique_temp_id\":\"android_1669318882522i964s7z53cc1ffd39af94204\",\"unix_nano_timestamp\":1669318883672814000,\"unix_timestamp\":1669318883,\"user_avatar\":{\"avatar\":{\"url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\"}},\"user_avatar_url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"user_extras\":{},\"user_id\":1449907638,\"user_id_str\":\"1449907638\",\"username\":\"testing34\"},{\"comment_before_id\":1148573198,\"comment_before_id_str\":\"1148573198\",\"disable_link_preview\":false,\"email\":\"testing34\",\"extras\":{},\"id\":1155176386,\"id_str\":\"1155176386\",\"is_deleted\":false,\"is_public_channel\":false,\"message\":\"Yy\",\"payload\":{},\"room_avatar\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"room_id\":96304367,\"room_id_str\":\"96304367\",\"room_name\":\"arief93 testing34\",\"room_type\":\"single\",\"status\":\"delivered\",\"timestamp\":\"2022-11-23T14:28:20Z\",\"topic_id\":96304367,\"topic_id_str\":\"96304367\",\"type\":\"text\",\"unique_temp_id\":\"android_1669134213965tnukhw8815c9d59244c2d094\",\"unix_nano_timestamp\":1669213700058275000,\"unix_timestamp\":1669213700,\"user_avatar\":{\"avatar\":{\"url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\"}},\"user_avatar_url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"user_extras\":{},\"user_id\":1449907638,\"user_id_str\":\"1449907638\",\"username\":\"testing34\"},{\"comment_before_id\":1148573190,\"comment_before_id_str\":\"1148573190\",\"disable_link_preview\":false,\"email\":\"arief93\",\"extras\":{},\"id\":1148573198,\"id_str\":\"1148573198\",\"is_deleted\":false,\"is_public_channel\":false,\"message\":\"s\",\"payload\":{},\"room_avatar\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"room_id\":96304367,\"room_id_str\":\"96304367\",\"room_name\":\"arief93 testing34\",\"room_type\":\"single\",\"status\":\"delivered\",\"timestamp\":\"2022-11-15T07:46:19Z\",\"topic_id\":96304367,\"topic_id_str\":\"96304367\",\"type\":\"text\",\"unique_temp_id\":\"javascript-1668498379171\",\"unix_nano_timestamp\":1668498379299815000,\"unix_timestamp\":1668498379,\"user_avatar\":{\"avatar\":{\"url\":\"https://robohash.org/arief93/bgset_bg2/3.14160?set=set4\"}},\"user_avatar_url\":\"https://robohash.org/arief93/bgset_bg2/3.14160?set=set4\",\"user_extras\":{},\"user_id\":133493390,\"user_id_str\":\"133493390\",\"username\":\"arief93\"},{\"comment_before_id\":1148573053,\"comment_before_id_str\":\"1148573053\",\"disable_link_preview\":false,\"email\":\"arief93\",\"extras\":{},\"id\":1148573190,\"id_str\":\"1148573190\",\"is_deleted\":false,\"is_public_channel\":false,\"message\":\"d\",\"payload\":{},\"room_avatar\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"room_id\":96304367,\"room_id_str\":\"96304367\",\"room_name\":\"arief93 testing34\",\"room_type\":\"single\",\"status\":\"read\",\"timestamp\":\"2022-11-15T07:46:18Z\",\"topic_id\":96304367,\"topic_id_str\":\"96304367\",\"type\":\"text\",\"unique_temp_id\":\"javascript-1668498378441\",\"unix_nano_timestamp\":1668498378591543000,\"unix_timestamp\":1668498378,\"user_avatar\":{\"avatar\":{\"url\":\"https://robohash.org/arief93/bgset_bg2/3.14160?set=set4\"}},\"user_avatar_url\":\"https://robohash.org/arief93/bgset_bg2/3.14160?set=set4\",\"user_extras\":{},\"user_id\":133493390,\"user_id_str\":\"133493390\",\"username\":\"arief93\"},{\"comment_before_id\":1148573049,\"comment_before_id_str\":\"1148573049\",\"disable_link_preview\":false,\"email\":\"arief93\",\"extras\":{},\"id\":1148573053,\"id_str\":\"1148573053\",\"is_deleted\":false,\"is_public_channel\":false,\"message\":\"r\",\"payload\":{},\"room_avatar\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"room_id\":96304367,\"room_id_str\":\"96304367\",\"room_name\":\"arief93 testing34\",\"room_type\":\"single\",\"status\":\"read\",\"timestamp\":\"2022-11-15T07:46:08Z\",\"topic_id\":96304367,\"topic_id_str\":\"96304367\",\"type\":\"text\",\"unique_temp_id\":\"javascript-1668498368745\",\"unix_nano_timestamp\":1668498368878055000,\"unix_timestamp\":1668498368,\"user_avatar\":{\"avatar\":{\"url\":\"https://robohash.org/arief93/bgset_bg2/3.14160?set=set4\"}},\"user_avatar_url\":\"https://robohash.org/arief93/bgset_bg2/3.14160?set=set4\",\"user_extras\":{},\"user_id\":133493390,\"user_id_str\":\"133493390\",\"username\":\"arief93\"},{\"comment_before_id\":1148573041,\"comment_before_id_str\":\"1148573041\",\"disable_link_preview\":false,\"email\":\"arief93\",\"extras\":{},\"id\":1148573049,\"id_str\":\"1148573049\",\"is_deleted\":false,\"is_public_channel\":false,\"message\":\"e\",\"payload\":{},\"room_avatar\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"room_id\":96304367,\"room_id_str\":\"96304367\",\"room_name\":\"arief93 testing34\",\"room_type\":\"single\",\"status\":\"read\",\"timestamp\":\"2022-11-15T07:46:08Z\",\"topic_id\":96304367,\"topic_id_str\":\"96304367\",\"type\":\"text\",\"unique_temp_id\":\"javascript-1668498368377\",\"unix_nano_timestamp\":1668498368522827000,\"unix_timestamp\":1668498368,\"user_avatar\":{\"avatar\":{\"url\":\"https://robohash.org/arief93/bgset_bg2/3.14160?set=set4\"}},\"user_avatar_url\":\"https://robohash.org/arief93/bgset_bg2/3.14160?set=set4\",\"user_extras\":{},\"user_id\":133493390,\"user_id_str\":\"133493390\",\"username\":\"arief93\"},{\"comment_before_id\":1148572897,\"comment_before_id_str\":\"1148572897\",\"disable_link_preview\":false,\"email\":\"arief93\",\"extras\":{},\"id\":1148573041,\"id_str\":\"1148573041\",\"is_deleted\":false,\"is_public_channel\":false,\"message\":\"d\",\"payload\":{},\"room_avatar\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"room_id\":96304367,\"room_id_str\":\"96304367\",\"room_name\":\"arief93 testing34\",\"room_type\":\"single\",\"status\":\"read\",\"timestamp\":\"2022-11-15T07:46:08Z\",\"topic_id\":96304367,\"topic_id_str\":\"96304367\",\"type\":\"text\",\"unique_temp_id\":\"javascript-1668498367983\",\"unix_nano_timestamp\":1668498368140856000,\"unix_timestamp\":1668498368,\"user_avatar\":{\"avatar\":{\"url\":\"https://robohash.org/arief93/bgset_bg2/3.14160?set=set4\"}},\"user_avatar_url\":\"https://robohash.org/arief93/bgset_bg2/3.14160?set=set4\",\"user_extras\":{},\"user_id\":133493390,\"user_id_str\":\"133493390\",\"username\":\"arief93\"},{\"comment_before_id\":1148569127,\"comment_before_id_str\":\"1148569127\",\"disable_link_preview\":false,\"email\":\"arief93\",\"extras\":{},\"id\":1148572897,\"id_str\":\"1148572897\",\"is_deleted\":false,\"is_public_channel\":false,\"message\":\"c\",\"payload\":{},\"room_avatar\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"room_id\":96304367,\"room_id_str\":\"96304367\",\"room_name\":\"arief93 testing34\",\"room_type\":\"single\",\"status\":\"read\",\"timestamp\":\"2022-11-15T07:46:00Z\",\"topic_id\":96304367,\"topic_id_str\":\"96304367\",\"type\":\"text\",\"unique_temp_id\":\"javascript-1668498360231\",\"unix_nano_timestamp\":1668498360360627000,\"unix_timestamp\":1668498360,\"user_avatar\":{\"avatar\":{\"url\":\"https://robohash.org/arief93/bgset_bg2/3.14160?set=set4\"}},\"user_avatar_url\":\"https://robohash.org/arief93/bgset_bg2/3.14160?set=set4\",\"user_extras\":{},\"user_id\":133493390,\"user_id_str\":\"133493390\",\"username\":\"arief93\"}],\"room\":{\"avatar_url\":\"https://robohash.org/arief93/bgset_bg2/3.14160?set=set4\",\"chat_type\":\"single\",\"id\":96304367,\"id_str\":\"96304367\",\"is_public_channel\":false,\"last_comment_id\":1172292157,\"last_comment_id_str\":\"1172292157\",\"last_comment_message\":\"Ok\",\"last_topic_id\":96304367,\"last_topic_id_str\":\"96304367\",\"options\":\"{}\",\"participants\":[{\"active\":true,\"avatar_url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"email\":\"testing34\",\"extras\":{},\"id\":1449907638,\"id_str\":\"1449907638\",\"last_comment_read_id\":1172292157,\"last_comment_read_id_str\":\"1172292157\",\"last_comment_received_id\":1172292157,\"last_comment_received_id_str\":\"1172292157\",\"username\":\"testing34\"},{\"active\":true,\"avatar_url\":\"https://robohash.org/arief93/bgset_bg2/3.14160?set=set4\",\"email\":\"arief93\",\"extras\":{},\"id\":133493390,\"id_str\":\"133493390\",\"last_comment_read_id\":1148573190,\"last_comment_read_id_str\":\"1148573190\",\"last_comment_received_id\":1172292157,\"last_comment_received_id_str\":\"1172292157\",\"username\":\"arief93\"}],\"raw_room_name\":\"arief93 testing34\",\"room_name\":\"arief93\",\"room_total_participants\":2,\"unique_id\":\"0b1d6da69656afc49343a3e4658f338a\",\"unread_count\":0}},\"status\":200}";
        String jsonString2 = "{\"results\":{\"comments\":[{\"comment_before_id\":1167549498,\"comment_before_id_str\":\"1167549498\",\"disable_link_preview\":false,\"email\":\"testing34\",\"extras\":{},\"id\":1172292157,\"id_str\":\"1172292157\",\"is_deleted\":false,\"message\":\"Ok\",\"payload\":{},\"room_avatar\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"room_id\":96304367,\"room_id_str\":\"96304367\",\"room_name\":\"arief93 testing34\",\"room_type\":\"group\",\"status\":\"sent\",\"timestamp\":\"2022-12-13T07:11:59Z\",\"topic_id\":96304367,\"topic_id_str\":\"96304367\",\"type\":\"text\",\"unique_temp_id\":\"android_1670915519612kdccsvcv15c9d59244c2d094\",\"unix_nano_timestamp\":1670915519723558000,\"unix_timestamp\":1670915519,\"user_avatar\":{\"avatar\":{\"url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\"}},\"user_avatar_url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"user_extras\":{},\"user_id\":1449907638,\"user_id_str\":\"1449907638\",\"username\":\"testing34\"},{\"comment_before_id\":1166639891,\"comment_before_id_str\":\"1166639891\",\"disable_link_preview\":false,\"email\":\"testing34\",\"extras\":{},\"id\":1167549498,\"id_str\":\"1167549498\",\"is_deleted\":false,\"message\":\"[file] https://d1edrlpyc25xu0.cloudfront.net/sdksample/image/upload/2GIfEQjvAV/balita5b21fef3-aa03-46b8-8056-d4eb063e1725.jpg [/file]\",\"payload\":{\"url\":\"https://d1edrlpyc25xu0.cloudfront.net/sdksample/image/upload/2GIfEQjvAV/balita5b21fef3-aa03-46b8-8056-d4eb063e1725.jpg\",\"caption\":\"\",\"file_name\":\"balita5b21fef3-aa03-46b8-8056-d4eb063e1725.png\",\"size\":0,\"pages\":1,\"encryption_key\":\"\"},\"room_avatar\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"room_id\":96304367,\"room_id_str\":\"96304367\",\"room_name\":\"arief93 testing34\",\"room_type\":\"group\",\"status\":\"sent\",\"timestamp\":\"2022-12-08T05:45:54Z\",\"topic_id\":96304367,\"topic_id_str\":\"96304367\",\"type\":\"file_attachment\",\"unique_temp_id\":\"android_1670478090617159uwee63c06857074d69d51\",\"unix_nano_timestamp\":1670478354425846000,\"unix_timestamp\":1670478354,\"user_avatar\":{\"avatar\":{\"url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\"}},\"user_avatar_url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"user_extras\":{},\"user_id\":1449907638,\"user_id_str\":\"1449907638\",\"username\":\"testing34\"},{\"comment_before_id\":1166637910,\"comment_before_id_str\":\"1166637910\",\"disable_link_preview\":false,\"email\":\"testing34\",\"extras\":{},\"id\":1166639891,\"id_str\":\"1166639891\",\"is_deleted\":false,\"is_public_channel\":false,\"message\":\"Gg\",\"payload\":{},\"room_avatar\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"room_id\":96304367,\"room_id_str\":\"96304367\",\"room_name\":\"arief93 testing34\",\"room_type\":\"single\",\"status\":\"sent\",\"timestamp\":\"2022-12-07T06:48:26Z\",\"topic_id\":96304367,\"topic_id_str\":\"96304367\",\"type\":\"text\",\"unique_temp_id\":\"android_1670395706312v1r6xfvs3c06857074d69d51\",\"unix_nano_timestamp\":1670395706498316000,\"unix_timestamp\":1670395706,\"user_avatar\":{\"avatar\":{\"url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\"}},\"user_avatar_url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"user_extras\":{},\"user_id\":1449907638,\"user_id_str\":\"1449907638\",\"username\":\"testing34\"},{\"comment_before_id\":1166637880,\"comment_before_id_str\":\"1166637880\",\"disable_link_preview\":false,\"email\":\"testing34\",\"extras\":{},\"id\":1166637910,\"id_str\":\"1166637910\",\"is_deleted\":false,\"is_public_channel\":false,\"message\":\"Jhh\",\"payload\":{},\"room_avatar\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"room_id\":96304367,\"room_id_str\":\"96304367\",\"room_name\":\"arief93 testing34\",\"room_type\":\"single\",\"status\":\"sent\",\"timestamp\":\"2022-12-07T06:47:01Z\",\"topic_id\":96304367,\"topic_id_str\":\"96304367\",\"type\":\"text\",\"unique_temp_id\":\"android_1670395620974itxge5xt3c06857074d69d51\",\"unix_nano_timestamp\":1670395621130158000,\"unix_timestamp\":1670395621,\"user_avatar\":{\"avatar\":{\"url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\"}},\"user_avatar_url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"user_extras\":{},\"user_id\":1449907638,\"user_id_str\":\"1449907638\",\"username\":\"testing34\"},{\"comment_before_id\":1166637861,\"comment_before_id_str\":\"1166637861\",\"disable_link_preview\":false,\"email\":\"testing34\",\"extras\":{},\"id\":1166637880,\"id_str\":\"1166637880\",\"is_deleted\":false,\"is_public_channel\":false,\"message\":\"Jjj\",\"payload\":{},\"room_avatar\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"room_id\":96304367,\"room_id_str\":\"96304367\",\"room_name\":\"arief93 testing34\",\"room_type\":\"single\",\"status\":\"sent\",\"timestamp\":\"2022-12-07T06:47:00Z\",\"topic_id\":96304367,\"topic_id_str\":\"96304367\",\"type\":\"text\",\"unique_temp_id\":\"android_1670395619939vjwtabqc3c06857074d69d51\",\"unix_nano_timestamp\":1670395620120134000,\"unix_timestamp\":1670395620,\"user_avatar\":{\"avatar\":{\"url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\"}},\"user_avatar_url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"user_extras\":{},\"user_id\":1449907638,\"user_id_str\":\"1449907638\",\"username\":\"testing34\"},{\"comment_before_id\":1162327164,\"comment_before_id_str\":\"1162327164\",\"disable_link_preview\":false,\"email\":\"testing34\",\"extras\":{},\"id\":1166637861,\"id_str\":\"1166637861\",\"is_deleted\":false,\"is_public_channel\":false,\"message\":\"Hhha\",\"payload\":{},\"room_avatar\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"room_id\":96304367,\"room_id_str\":\"96304367\",\"room_name\":\"arief93 testing34\",\"room_type\":\"single\",\"status\":\"sent\",\"timestamp\":\"2022-12-07T06:46:59Z\",\"topic_id\":96304367,\"topic_id_str\":\"96304367\",\"type\":\"text\",\"unique_temp_id\":\"android_1670395618808fk84mxs53c06857074d69d51\",\"unix_nano_timestamp\":1670395619000515000,\"unix_timestamp\":1670395619,\"user_avatar\":{\"avatar\":{\"url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\"}},\"user_avatar_url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"user_extras\":{},\"user_id\":1449907638,\"user_id_str\":\"1449907638\",\"username\":\"testing34\"},{\"comment_before_id\":1162327109,\"comment_before_id_str\":\"1162327109\",\"disable_link_preview\":false,\"email\":\"testing34\",\"extras\":{},\"id\":1162327164,\"id_str\":\"1162327164\",\"is_deleted\":false,\"is_public_channel\":false,\"message\":\"d\",\"payload\":{},\"room_avatar\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"room_id\":96304367,\"room_id_str\":\"96304367\",\"room_name\":\"arief93 testing34\",\"room_type\":\"group\",\"status\":\"delivered\",\"timestamp\":\"2022-12-02T06:30:55Z\",\"topic_id\":96304367,\"topic_id_str\":\"96304367\",\"type\":\"text\",\"unique_temp_id\":\"javascript-1669962655850\",\"unix_nano_timestamp\":1669962655988072000,\"unix_timestamp\":1669962655,\"user_avatar\":{\"avatar\":{\"url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\"}},\"user_avatar_url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"user_extras\":{},\"user_id\":1449907638,\"user_id_str\":\"1449907638\",\"username\":\"testing34\"},{\"comment_before_id\":1162327028,\"comment_before_id_str\":\"1162327028\",\"disable_link_preview\":false,\"email\":\"testing34\",\"extras\":{},\"id\":1162327109,\"id_str\":\"1162327109\",\"is_deleted\":false,\"is_public_channel\":false,\"message\":\"a\",\"payload\":{},\"room_avatar\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"room_id\":96304367,\"room_id_str\":\"96304367\",\"room_name\":\"arief93 testing34\",\"room_type\":\"single\",\"status\":\"delivered\",\"timestamp\":\"2022-12-02T06:30:52Z\",\"topic_id\":96304367,\"topic_id_str\":\"96304367\",\"type\":\"text\",\"unique_temp_id\":\"javascript-1669962651953\",\"unix_nano_timestamp\":1669962652129883000,\"unix_timestamp\":1669962652,\"user_avatar\":{\"avatar\":{\"url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\"}},\"user_avatar_url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"user_extras\":{},\"user_id\":1449907638,\"user_id_str\":\"1449907638\",\"username\":\"testing34\"},{\"comment_before_id\":1162326986,\"comment_before_id_str\":\"1162326986\",\"disable_link_preview\":false,\"email\":\"testing34\",\"extras\":{},\"id\":1162327028,\"id_str\":\"1162327028\",\"is_deleted\":false,\"is_public_channel\":false,\"message\":\"F\",\"payload\":{},\"room_avatar\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"room_id\":96304367,\"room_id_str\":\"96304367\",\"room_name\":\"arief93 testing34\",\"room_type\":\"single\",\"status\":\"delivered\",\"timestamp\":\"2022-12-02T06:30:48Z\",\"topic_id\":96304367,\"topic_id_str\":\"96304367\",\"type\":\"text\",\"unique_temp_id\":\"android_1669962648409s0zlbrm13c06857074d69d51\",\"unix_nano_timestamp\":1669962648551284000,\"unix_timestamp\":1669962648,\"user_avatar\":{\"avatar\":{\"url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\"}},\"user_avatar_url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"user_extras\":{},\"user_id\":1449907638,\"user_id_str\":\"1449907638\",\"username\":\"testing34\"},{\"comment_before_id\":1162175835,\"comment_before_id_str\":\"1162175835\",\"disable_link_preview\":false,\"email\":\"testing34\",\"extras\":{},\"id\":1162326986,\"id_str\":\"1162326986\",\"is_deleted\":false,\"is_public_channel\":false,\"message\":\"Hh\",\"payload\":{},\"room_avatar\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"room_id\":96304367,\"room_id_str\":\"96304367\",\"room_name\":\"arief93 testing34\",\"room_type\":\"single\",\"status\":\"delivered\",\"timestamp\":\"2022-12-02T06:30:46Z\",\"topic_id\":96304367,\"topic_id_str\":\"96304367\",\"type\":\"text\",\"unique_temp_id\":\"android_1669962646359sguzthb43c06857074d69d51\",\"unix_nano_timestamp\":1669962646475867000,\"unix_timestamp\":1669962646,\"user_avatar\":{\"avatar\":{\"url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\"}},\"user_avatar_url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"user_extras\":{},\"user_id\":1449907638,\"user_id_str\":\"1449907638\",\"username\":\"testing34\"},{\"comment_before_id\":1156050638,\"comment_before_id_str\":\"1156050638\",\"disable_link_preview\":false,\"email\":\"testing34\",\"extras\":{},\"id\":1162175835,\"id_str\":\"1162175835\",\"is_deleted\":false,\"is_public_channel\":false,\"message\":\"[file] https://d1edrlpyc25xu0.cloudfront.net/sdksample/image/upload/0EyfYMiIJf/kisspng-video-royalty-free-social-media-youtube-clip-art-piktochart-visual-editor-5ba36298529527.2262.png [/file]\",\"payload\":{\"url\":\"https://d1edrlpyc25xu0.cloudfront.net/sdksample/image/upload/0EyfYMiIJf/kisspng-video-royalty-free-social-media-youtube-clip-art-piktochart-visual-editor-5ba36298529527.2262.png\",\"caption\":\"\",\"file_name\":\"kisspng-video-royalty-free-social-media-youtube-clip-art-piktochart-visual-editor-5ba36298529527.2262200415374342643383.png\",\"size\":990985,\"pages\":1,\"encryption_key\":\"\"},\"room_avatar\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"room_id\":96304367,\"room_id_str\":\"96304367\",\"room_name\":\"arief93 testing34\",\"room_type\":\"single\",\"status\":\"delivered\",\"timestamp\":\"2022-12-02T03:59:36Z\",\"topic_id\":96304367,\"topic_id_str\":\"96304367\",\"type\":\"file_attachment\",\"unique_temp_id\":\"1669953571836\",\"unix_nano_timestamp\":1669953576260008000,\"unix_timestamp\":1669953576,\"user_avatar\":{\"avatar\":{\"url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\"}},\"user_avatar_url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"user_extras\":{},\"user_id\":1449907638,\"user_id_str\":\"1449907638\",\"username\":\"testing34\"},{\"comment_before_id\":1156050632,\"comment_before_id_str\":\"1156050632\",\"disable_link_preview\":false,\"email\":\"testing34\",\"extras\":{},\"id\":1156050638,\"id_str\":\"1156050638\",\"is_deleted\":false,\"is_public_channel\":false,\"message\":\"[file] https://d1edrlpyc25xu0.cloudfront.net/sdksample/image/upload/wb7USsysPO/IMG_20221116_070617.jpg [/file]\",\"payload\":{\"url\":\"https://d1edrlpyc25xu0.cloudfront.net/sdksample/image/upload/wb7USsysPO/IMG_20221116_070617.jpg\",\"caption\":\"\",\"file_name\":\"IMG_20221116_070617.png\",\"size\":0,\"pages\":1,\"encryption_key\":\"\"},\"room_avatar\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"room_id\":96304367,\"room_id_str\":\"96304367\",\"room_name\":\"arief93 testing34\",\"room_type\":\"single\",\"status\":\"delivered\",\"timestamp\":\"2022-11-24T19:41:36Z\",\"topic_id\":96304367,\"topic_id_str\":\"96304367\",\"type\":\"file_attachment\",\"unique_temp_id\":\"android_1669318892708mvkn0fh23cc1ffd39af94204\",\"unix_nano_timestamp\":1669318896399483000,\"unix_timestamp\":1669318896,\"user_avatar\":{\"avatar\":{\"url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\"}},\"user_avatar_url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"user_extras\":{},\"user_id\":1449907638,\"user_id_str\":\"1449907638\",\"username\":\"testing34\"},{\"comment_before_id\":1155176386,\"comment_before_id_str\":\"1155176386\",\"disable_link_preview\":false,\"email\":\"testing34\",\"extras\":{},\"id\":1156050632,\"id_str\":\"1156050632\",\"is_deleted\":false,\"is_public_channel\":false,\"message\":\"Hi\",\"payload\":{},\"room_avatar\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"room_id\":96304367,\"room_id_str\":\"96304367\",\"room_name\":\"arief93 testing34\",\"room_type\":\"single\",\"status\":\"delivered\",\"timestamp\":\"2022-11-24T19:41:23Z\",\"topic_id\":96304367,\"topic_id_str\":\"96304367\",\"type\":\"text\",\"unique_temp_id\":\"android_1669318882522i964s7z53cc1ffd39af94204\",\"unix_nano_timestamp\":1669318883672814000,\"unix_timestamp\":1669318883,\"user_avatar\":{\"avatar\":{\"url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\"}},\"user_avatar_url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"user_extras\":{},\"user_id\":1449907638,\"user_id_str\":\"1449907638\",\"username\":\"testing34\"},{\"comment_before_id\":1148573198,\"comment_before_id_str\":\"1148573198\",\"disable_link_preview\":false,\"email\":\"testing34\",\"extras\":{},\"id\":1155176386,\"id_str\":\"1155176386\",\"is_deleted\":false,\"is_public_channel\":false,\"message\":\"Yy\",\"payload\":{},\"room_avatar\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"room_id\":96304367,\"room_id_str\":\"96304367\",\"room_name\":\"arief93 testing34\",\"room_type\":\"single\",\"status\":\"delivered\",\"timestamp\":\"2022-11-23T14:28:20Z\",\"topic_id\":96304367,\"topic_id_str\":\"96304367\",\"type\":\"text\",\"unique_temp_id\":\"android_1669134213965tnukhw8815c9d59244c2d094\",\"unix_nano_timestamp\":1669213700058275000,\"unix_timestamp\":1669213700,\"user_avatar\":{\"avatar\":{\"url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\"}},\"user_avatar_url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"user_extras\":{},\"user_id\":1449907638,\"user_id_str\":\"1449907638\",\"username\":\"testing34\"},{\"comment_before_id\":1148573190,\"comment_before_id_str\":\"1148573190\",\"disable_link_preview\":false,\"email\":\"arief93\",\"extras\":{},\"id\":1148573198,\"id_str\":\"1148573198\",\"is_deleted\":false,\"is_public_channel\":false,\"message\":\"s\",\"payload\":{},\"room_avatar\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"room_id\":96304367,\"room_id_str\":\"96304367\",\"room_name\":\"arief93 testing34\",\"room_type\":\"single\",\"status\":\"delivered\",\"timestamp\":\"2022-11-15T07:46:19Z\",\"topic_id\":96304367,\"topic_id_str\":\"96304367\",\"type\":\"text\",\"unique_temp_id\":\"javascript-1668498379171\",\"unix_nano_timestamp\":1668498379299815000,\"unix_timestamp\":1668498379,\"user_avatar\":{\"avatar\":{\"url\":\"https://robohash.org/arief93/bgset_bg2/3.14160?set=set4\"}},\"user_avatar_url\":\"https://robohash.org/arief93/bgset_bg2/3.14160?set=set4\",\"user_extras\":{},\"user_id\":133493390,\"user_id_str\":\"133493390\",\"username\":\"arief93\"},{\"comment_before_id\":1148573053,\"comment_before_id_str\":\"1148573053\",\"disable_link_preview\":false,\"email\":\"arief93\",\"extras\":{},\"id\":1148573190,\"id_str\":\"1148573190\",\"is_deleted\":false,\"is_public_channel\":false,\"message\":\"d\",\"payload\":{},\"room_avatar\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"room_id\":96304367,\"room_id_str\":\"96304367\",\"room_name\":\"arief93 testing34\",\"room_type\":\"single\",\"status\":\"read\",\"timestamp\":\"2022-11-15T07:46:18Z\",\"topic_id\":96304367,\"topic_id_str\":\"96304367\",\"type\":\"text\",\"unique_temp_id\":\"javascript-1668498378441\",\"unix_nano_timestamp\":1668498378591543000,\"unix_timestamp\":1668498378,\"user_avatar\":{\"avatar\":{\"url\":\"https://robohash.org/arief93/bgset_bg2/3.14160?set=set4\"}},\"user_avatar_url\":\"https://robohash.org/arief93/bgset_bg2/3.14160?set=set4\",\"user_extras\":{},\"user_id\":133493390,\"user_id_str\":\"133493390\",\"username\":\"arief93\"},{\"comment_before_id\":1148573049,\"comment_before_id_str\":\"1148573049\",\"disable_link_preview\":false,\"email\":\"arief93\",\"extras\":{},\"id\":1148573053,\"id_str\":\"1148573053\",\"is_deleted\":false,\"is_public_channel\":false,\"message\":\"r\",\"payload\":{},\"room_avatar\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"room_id\":96304367,\"room_id_str\":\"96304367\",\"room_name\":\"arief93 testing34\",\"room_type\":\"single\",\"status\":\"read\",\"timestamp\":\"2022-11-15T07:46:08Z\",\"topic_id\":96304367,\"topic_id_str\":\"96304367\",\"type\":\"text\",\"unique_temp_id\":\"javascript-1668498368745\",\"unix_nano_timestamp\":1668498368878055000,\"unix_timestamp\":1668498368,\"user_avatar\":{\"avatar\":{\"url\":\"https://robohash.org/arief93/bgset_bg2/3.14160?set=set4\"}},\"user_avatar_url\":\"https://robohash.org/arief93/bgset_bg2/3.14160?set=set4\",\"user_extras\":{},\"user_id\":133493390,\"user_id_str\":\"133493390\",\"username\":\"arief93\"},{\"comment_before_id\":1148573041,\"comment_before_id_str\":\"1148573041\",\"disable_link_preview\":false,\"email\":\"arief93\",\"extras\":{},\"id\":1148573049,\"id_str\":\"1148573049\",\"is_deleted\":false,\"is_public_channel\":false,\"message\":\"e\",\"payload\":{},\"room_avatar\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"room_id\":96304367,\"room_id_str\":\"96304367\",\"room_name\":\"arief93 testing34\",\"room_type\":\"single\",\"status\":\"read\",\"timestamp\":\"2022-11-15T07:46:08Z\",\"topic_id\":96304367,\"topic_id_str\":\"96304367\",\"type\":\"text\",\"unique_temp_id\":\"javascript-1668498368377\",\"unix_nano_timestamp\":1668498368522827000,\"unix_timestamp\":1668498368,\"user_avatar\":{\"avatar\":{\"url\":\"https://robohash.org/arief93/bgset_bg2/3.14160?set=set4\"}},\"user_avatar_url\":\"https://robohash.org/arief93/bgset_bg2/3.14160?set=set4\",\"user_extras\":{},\"user_id\":133493390,\"user_id_str\":\"133493390\",\"username\":\"arief93\"},{\"comment_before_id\":1148572897,\"comment_before_id_str\":\"1148572897\",\"disable_link_preview\":false,\"email\":\"arief93\",\"extras\":{},\"id\":1148573041,\"id_str\":\"1148573041\",\"is_deleted\":false,\"is_public_channel\":false,\"message\":\"d\",\"payload\":{},\"room_avatar\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"room_id\":96304367,\"room_id_str\":\"96304367\",\"room_name\":\"arief93 testing34\",\"room_type\":\"single\",\"status\":\"read\",\"timestamp\":\"2022-11-15T07:46:08Z\",\"topic_id\":96304367,\"topic_id_str\":\"96304367\",\"type\":\"text\",\"unique_temp_id\":\"javascript-1668498367983\",\"unix_nano_timestamp\":1668498368140856000,\"unix_timestamp\":1668498368,\"user_avatar\":{\"avatar\":{\"url\":\"https://robohash.org/arief93/bgset_bg2/3.14160?set=set4\"}},\"user_avatar_url\":\"https://robohash.org/arief93/bgset_bg2/3.14160?set=set4\",\"user_extras\":{},\"user_id\":133493390,\"user_id_str\":\"133493390\",\"username\":\"arief93\"},{\"comment_before_id\":1148569127,\"comment_before_id_str\":\"1148569127\",\"disable_link_preview\":false,\"email\":\"arief93\",\"extras\":{},\"id\":1148572897,\"id_str\":\"1148572897\",\"is_deleted\":false,\"is_public_channel\":false,\"message\":\"c\",\"payload\":{},\"room_avatar\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"room_id\":96304367,\"room_id_str\":\"96304367\",\"room_name\":\"arief93 testing34\",\"room_type\":\"single\",\"status\":\"read\",\"timestamp\":\"2022-11-15T07:46:00Z\",\"topic_id\":96304367,\"topic_id_str\":\"96304367\",\"type\":\"text\",\"unique_temp_id\":\"javascript-1668498360231\",\"unix_nano_timestamp\":1668498360360627000,\"unix_timestamp\":1668498360,\"user_avatar\":{\"avatar\":{\"url\":\"https://robohash.org/arief93/bgset_bg2/3.14160?set=set4\"}},\"user_avatar_url\":\"https://robohash.org/arief93/bgset_bg2/3.14160?set=set4\",\"user_extras\":{},\"user_id\":133493390,\"user_id_str\":\"133493390\",\"username\":\"arief93\"}],\"room\":{\"avatar_url\":\"https://robohash.org/arief93/bgset_bg2/3.14160?set=set4\",\"chat_type\":\"single\",\"id\":96304367,\"id_str\":\"96304367\",\"is_public_channel\":false,\"last_comment_id\":1172292157,\"last_comment_id_str\":\"1172292157\",\"last_comment_message\":\"Ok\",\"last_topic_id\":96304367,\"last_topic_id_str\":\"96304367\",\"options\":\"{}\",\"participants\":[{\"active\":true,\"avatar_url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"email\":\"testing34\",\"extras\":{},\"id\":1449907638,\"id_str\":\"1449907638\",\"last_comment_read_id\":1172292157,\"last_comment_read_id_str\":\"1172292157\",\"last_comment_received_id\":1172292157,\"last_comment_received_id_str\":\"1172292157\",\"username\":\"testing34\"},{\"active\":true,\"avatar_url\":\"https://robohash.org/arief93/bgset_bg2/3.14160?set=set4\",\"email\":\"arief93\",\"extras\":{},\"id\":133493390,\"id_str\":\"133493390\",\"last_comment_read_id\":1148573190,\"last_comment_read_id_str\":\"1148573190\",\"last_comment_received_id\":1172292157,\"last_comment_received_id_str\":\"1172292157\",\"username\":\"arief93\"}],\"raw_room_name\":\"arief93 testing34\",\"room_name\":\"arief93\",\"unique_id\":\"0b1d6da69656afc49343a3e4658f338a\",\"unread_count\":0}},\"status\":200}";
        JsonObject jsonObject = new JsonParser().parse(jsonString).getAsJsonObject();
        JsonObject jsonObject2 = new JsonParser().parse(jsonString2).getAsJsonObject();

        QiscusApiParser.parseQiscusChatRoom(jsonObject);
        QiscusApiParser.parseQiscusChatRoom(jsonObject2);
        QiscusApiParser.parseQiscusChatRoom(null);

    }

    @Test
    public void parseQiscusChatRoomWithComments(){
        String jsonString = "{\n" +
                "    \"results\": {\n" +
                "        \"comments\": [\n" +
                "            {\n" +
                "                \"comment_before_id\": 6271,\n" +
                "                \"comment_before_id_str\": \"6271\",\n" +
                "                \"disable_link_preview\": false,\n" +
                "                \"email\": \"jarjit@mail.com\",\n" +
                "                \"extras\": {},\n" +
                "                \"id\": 6272,\n" +
                "                \"id_str\": \"6272\",\n" +
                "                \"is_deleted\": false,\n" +
                "                \"is_public_channel\": false,\n" +
                "                \"message\": \"oke siap\",\n" +
                "                \"payload\": {},\n" +
                "                \"room_avatar\": \"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\n" +
                "                \"room_id\": 2,\n" +
                "                \"room_id_str\": \"2\",\n" +
                "                \"room_name\": \"30134848@singh.com jarjit@mail.com\",\n" +
                "                \"room_type\": \"single\",\n" +
                "                \"status\": \"delivered\",\n" +
                "                \"timestamp\": \"2019-03-13T13:00:42Z\",\n" +
                "                \"topic_id\": 2,\n" +
                "                \"topic_id_str\": \"2\",\n" +
                "                \"type\": \"text\",\n" +
                "                \"unique_temp_id\": \"ETb0IIdgIKtfel3OSXry\",\n" +
                "                \"unix_nano_timestamp\": 1552482042849135000,\n" +
                "                \"unix_timestamp\": 1552482042,\n" +
                "                \"user_avatar\": {\n" +
                "                    \"avatar\": {\n" +
                "                        \"url\": \"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\"\n" +
                "                    }\n" +
                "                },\n" +
                "                \"user_avatar_url\": \"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\n" +
                "                \"user_id\": 13,\n" +
                "                \"user_id_str\": \"13\",\n" +
                "                \"username\": \"Jarjit singh\"\n" +
                "            }\n" +
                "        ],\n" +
                "        \"room\": {\n" +
                "            \"avatar_url\": \"https://api.adorable.io/avatars/120/Jarjit30134848.png\",\n" +
                "            \"chat_type\": \"single\",\n" +
                "            \"id\": 2,\n" +
                "            \"id_str\": \"2\",\n" +
                "            \"is_public_channel\": false,\n" +
                "            \"last_comment_id\": 6272,\n" +
                "            \"last_comment_id_str\": \"6272\",\n" +
                "            \"last_comment_message\": \"oke siap\",\n" +
                "            \"last_topic_id\": 2,\n" +
                "            \"last_topic_id_str\": \"2\",\n" +
                "            \"options\": \"{}\",\n" +
                "            \"participants\": [\n" +
                "                {\n" +
                "                    \"avatar_url\": \"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\n" +
                "                    \"email\": \"jarjit@mail.com\",\n" +
                "                    \"extras\": {\n" +
                "                        \"role\": \"admin\"\n" +
                "                    },\n" +
                "                    \"id\": 13,\n" +
                "                    \"id_str\": \"13\",\n" +
                "                    \"last_comment_read_id\": 6272,\n" +
                "                    \"last_comment_read_id_str\": \"6272\",\n" +
                "                    \"last_comment_received_id\": 6272,\n" +
                "                    \"last_comment_received_id_str\": \"6272\",\n" +
                "                    \"username\": \"Jarjit singh\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"avatar_url\": \"https://api.adorable.io/avatars/120/Jarjit30134848.png\",\n" +
                "                    \"email\": \"30134848@singh.com\",\n" +
                "                    \"extras\": {},\n" +
                "                    \"id\": 2,\n" +
                "                    \"id_str\": \"2\",\n" +
                "                    \"last_comment_read_id\": 0,\n" +
                "                    \"last_comment_read_id_str\": \"0\",\n" +
                "                    \"last_comment_received_id\": 6272,\n" +
                "                    \"last_comment_received_id_str\": \"6272\",\n" +
                "                    \"username\": \"partisipan\"\n" +
                "                }\n" +
                "            ],\n" +
                "            \"raw_room_name\": \"30134848@singh.com jarjit@mail.com\",\n" +
                "            \"room_name\": \"partisipan\",\n" +
                "            \"room_total_participants\": 2,\n" +
                "            \"unique_id\": \"0c1a4d31b45297ba7c8999b47791a79d\",\n" +
                "            \"unread_count\": 0\n" +
                "        }\n" +
                "    },\n" +
                "    \"status\": 200\n" +
                "}";

        JsonObject jsonObject = new JsonParser().parse(jsonString).getAsJsonObject();
        QiscusApiParser.parseQiscusChatRoomWithComments(jsonObject);
    }

    @Test
    public void parseQiscusChannels(){
        String jsonString = "{\"results\": {\"channels\": [{\"avatar_url\": \"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/E2nVru1t25/1507541900-avatar.png\",\"created_at\": \"2019-02-13T16:15:35.554594Z\",\"extras\": {},\"is_joined\": *false*,\"name\": \"channelidrandomstring\",\"unique_id\": \"channelidrandomstring\"},{\"avatar_url\": \"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/E2nVru1t25/1507541900-avatar.png\",\"created_at\": \"2019-03-25T14:08:38.731967Z\",\"extras\": {\"muted\": *true*},\"is_joined\": *false*,\"name\": \"channel abal abal\",\"unique_id\": \"inichannelidnya\"}]},\"status\": 200}";
        JsonObject jsonObject = new JsonParser().parse(jsonString).getAsJsonObject();

        QiscusApiParser.parseQiscusChannel(jsonObject);
    }

    @Test
    public void parseQiscusChannels2(){
        String jsonString = "{\"results\": {\"channels\": [{},{\"avatar_url\": \"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/E2nVru1t25/1507541900-avatar.png\",\"created_at\": \"2019-03-25T14:08:38.731967Z\",\"extras\": {\"muted\": *true*},\"is_joined\": *false*,\"name\": \"channel abal abal\",\"unique_id\": \"inichannelidnya\",\"id\": 123}]},\"status\": 200}";
        JsonObject jsonObject = new JsonParser().parse(jsonString).getAsJsonObject();

        QiscusApiParser.parseQiscusChannels(jsonObject);
    }

    @Test
    public void parseFileListAndSearchMessage(){
        String jsonString = "\n" +
                "{\n" +
                "    \"results\": {\n" +
                "        \"comments\": [\n" +
                "            {\n" +
                "                \"comment_before_id\": 0,\n" +
                "                \"comment_before_id_str\": \"0\",\n" +
                "                \"disable_link_preview\": false,\n" +
                "                \"email\": \"jarjit@mail.com\",\n" +
                "                \"extras\": {},\n" +
                "                \"id\": 6276,\n" +
                "                \"id_str\": \"6276\",\n" +
                "                \"is_deleted\": true,\n" +
                "                \"is_public_channel\": false,\n" +
                "                \"message\": \"This message has been deleted.\",\n" +
                "                \"payload\": {},\n" +
                "                \"room_avatar\": \"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\n" +
                "                \"room_id\": 17,\n" +
                "                \"room_id_str\": \"17\",\n" +
                "                \"room_name\": \"17856317@singh.com jarjit@mail.com\",\n" +
                "                \"room_type\": \"single\",\n" +
                "                \"status\": \"sent\",\n" +
                "                \"timestamp\": \"2019-03-25T17:06:29Z\",\n" +
                "                \"topic_id\": 17,\n" +
                "                \"topic_id_str\": \"17\",\n" +
                "                \"type\": \"text\",\n" +
                "                \"unique_temp_id\": \"EINMre4mxW0H3g06CfWy\",\n" +
                "                \"unix_nano_timestamp\": 1553533589415023000,\n" +
                "                \"unix_timestamp\": 1553533589,\n" +
                "                \"user_avatar\": {\n" +
                "                    \"avatar\": {\n" +
                "                        \"url\": \"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\"\n" +
                "                    }\n" +
                "                },\n" +
                "                \"user_avatar_url\": \"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\n" +
                "                \"user_id\": 13,\n" +
                "                \"user_id_str\": \"13\",\n" +
                "                \"username\": \"Jarjit singh\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"comment_before_id\": 0,\n" +
                "                \"comment_before_id_str\": \"0\",\n" +
                "                \"disable_link_preview\": false,\n" +
                "                \"email\": \"jarjit@mail.com\",\n" +
                "                \"extras\": {},\n" +
                "                \"id\": 6275,\n" +
                "                \"id_str\": \"6275\",\n" +
                "                \"is_deleted\": true,\n" +
                "                \"is_public_channel\": false,\n" +
                "                \"message\": \"This message has been deleted.\",\n" +
                "                \"payload\": {},\n" +
                "                \"room_avatar\": \"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\n" +
                "                \"room_id\": 16,\n" +
                "                \"room_id_str\": \"16\",\n" +
                "                \"room_name\": \"59909426@singh.com jarjit@mail.com\",\n" +
                "                \"room_type\": \"single\",\n" +
                "                \"status\": \"delivered\",\n" +
                "                \"timestamp\": \"2019-03-25T17:06:29Z\",\n" +
                "                \"topic_id\": 16,\n" +
                "                \"topic_id_str\": \"16\",\n" +
                "                \"type\": \"text\",\n" +
                "                \"unique_temp_id\": \"zObpmz4Xdyha5dN7NGh5\",\n" +
                "                \"unix_nano_timestamp\": 1553533589243730000,\n" +
                "                \"unix_timestamp\": 1553533589,\n" +
                "                \"user_avatar\": {\n" +
                "                    \"avatar\": {\n" +
                "                        \"url\": \"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\"\n" +
                "                    }\n" +
                "                },\n" +
                "                \"user_avatar_url\": \"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\n" +
                "                \"user_id\": 13,\n" +
                "                \"user_id_str\": \"13\",\n" +
                "                \"username\": \"Jarjit singh\"\n" +
                "            }\n" +
                "        ],\n" +
                "        \"limit\": 10, \n" +
                "        \"page\": 0, \n" +
                "        \"total\": \"7\"\n" +
                "    },\n" +
                "    \"status\": 200\n" +
                "}";

        JsonObject jsonObject = new JsonParser().parse(jsonString).getAsJsonObject();

        QiscusApiParser.parseFileListAndSearchMessage(jsonObject);

    }

    @Test
    public void parseFileListAndSearchMessage2(){
        String jsonString = "\n" +
                "{\n" +
                "    \"results\": {\n" +
                "        \"comments\": [\n" +
                "            {\n" +
                "                \"comment_before_id\": 0,\n" +
                "                \"comment_before_id_str\": \"0\",\n" +
                "                \"disable_link_preview\": false,\n" +
                "                \"email\": \"jarjit@mail.com\",\n" +
                "                \"extras\": {},\n" +
                "                \"id\": 6276,\n" +
                "                \"id_str\": \"6276\",\n" +
                "                \"is_public_channel\": false,\n" +
                "                \"message\": \"This message has been deleted.\",\n" +
                "                \"payload\": {},\n" +
                "                \"room_id_str\": \"17\",\n" +
                "                \"status\": \"sent\",\n" +
                "                \"timestamp\": \"2019-03-25T17:06:29Z\",\n" +
                "                \"topic_id\": 17,\n" +
                "                \"topic_id_str\": \"17\",\n" +
                "                \"unix_nano_timestamp\": 1553533589415023000,\n" +
                "                \"unix_timestamp\": 1553533589,\n" +
                "                \"user_avatar\": {\n" +
                "                    \"avatar\": {\n" +
                "                        \"url\": \"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\"\n" +
                "                    }\n" +
                "                },\n" +
                "                \"user_avatar_url\": \"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\n" +
                "                \"user_id\": 13,\n" +
                "                \"user_id_str\": \"13\",\n" +
                "                \"username\": \"Jarjit singh\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"comment_before_id\": 0,\n" +
                "                \"comment_before_id_str\": \"0\",\n" +
                "                \"disable_link_preview\": false,\n" +
                "                \"email\": \"jarjit@mail.com\",\n" +
                "                \"extras\": {},\n" +
                "                \"id\": 6275,\n" +
                "                \"id_str\": \"6275\",\n" +
                "                \"is_deleted\": true,\n" +
                "                \"is_public_channel\": false,\n" +
                "                \"message\": \"This message has been deleted.\",\n" +
                "                \"payload\": {},\n" +
                "                \"room_avatar\": \"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\n" +
                "                \"room_id\": 16,\n" +
                "                \"room_id_str\": \"16\",\n" +
                "                \"room_name\": \"59909426@singh.com jarjit@mail.com\",\n" +
                "                \"room_type\": \"single\",\n" +
                "                \"status\": \"delivered\",\n" +
                "                \"timestamp\": \"2019-03-25T17:06:29Z\",\n" +
                "                \"topic_id\": 16,\n" +
                "                \"topic_id_str\": \"16\",\n" +
                "                \"type\": \"text\",\n" +
                "                \"unique_temp_id\": \"zObpmz4Xdyha5dN7NGh5\",\n" +
                "                \"unix_nano_timestamp\": 1553533589243730000,\n" +
                "                \"unix_timestamp\": 1553533589,\n" +
                "                \"user_avatar\": {\n" +
                "                    \"avatar\": {\n" +
                "                        \"url\": \"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\"\n" +
                "                    }\n" +
                "                },\n" +
                "                \"user_avatar_url\": \"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\n" +
                "                \"user_id\": 13,\n" +
                "                \"user_id_str\": \"13\",\n" +
                "                \"username\": \"Jarjit singh\"\n" +
                "            }\n" +
                "        ],\n" +
                "        \"limit\": 10, \n" +
                "        \"page\": 0, \n" +
                "        \"total\": \"7\"\n" +
                "    },\n" +
                "    \"status\": 200\n" +
                "}";

        JsonObject jsonObject = new JsonParser().parse(jsonString).getAsJsonObject();

        QiscusApiParser.parseFileListAndSearchMessage(jsonObject);

    }


}