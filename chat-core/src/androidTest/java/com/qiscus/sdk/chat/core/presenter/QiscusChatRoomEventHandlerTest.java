package com.qiscus.sdk.chat.core.presenter;

import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.qiscus.sdk.chat.core.InstrumentationBaseTest;
import com.qiscus.sdk.chat.core.QiscusCore;
import com.qiscus.sdk.chat.core.data.model.QiscusChatRoom;
import com.qiscus.sdk.chat.core.data.model.QiscusComment;
import com.qiscus.sdk.chat.core.data.model.QiscusRoomMember;
import com.qiscus.sdk.chat.core.data.remote.QiscusApiParser;
import com.qiscus.sdk.chat.core.event.QiscusChatRoomEvent;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;

@RunWith(AndroidJUnit4ClassRunner.class)
public class QiscusChatRoomEventHandlerTest extends InstrumentationBaseTest {

    private String jsonString = "{\"results\":{\"comments\":[{\"comment_before_id\":1167549498,\"comment_before_id_str\":\"1167549498\",\"disable_link_preview\":false,\"email\":\"testing34\",\"extras\":{},\"id\":1172292157,\"id_str\":\"1172292157\",\"is_deleted\":false,\"is_public_channel\":false,\"message\":\"Ok\",\"payload\":{},\"room_avatar\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"room_id\":96304367,\"room_id_str\":\"96304367\",\"room_name\":\"arief93 testing34\",\"room_type\":\"single\",\"status\":\"sent\",\"timestamp\":\"2022-12-13T07:11:59Z\",\"topic_id\":96304367,\"topic_id_str\":\"96304367\",\"type\":\"text\",\"unique_temp_id\":\"android_1670915519612kdccsvcv15c9d59244c2d094\",\"unix_nano_timestamp\":1670915519723558000,\"unix_timestamp\":1670915519,\"user_avatar\":{\"avatar\":{\"url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\"}},\"user_avatar_url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"user_extras\":{},\"user_id\":1449907638,\"user_id_str\":\"1449907638\",\"username\":\"testing34\"},{\"comment_before_id\":1166639891,\"comment_before_id_str\":\"1166639891\",\"disable_link_preview\":false,\"email\":\"testing34\",\"extras\":{},\"id\":1167549498,\"id_str\":\"1167549498\",\"is_deleted\":false,\"is_public_channel\":false,\"message\":\"[file] https://d1edrlpyc25xu0.cloudfront.net/sdksample/image/upload/2GIfEQjvAV/balita5b21fef3-aa03-46b8-8056-d4eb063e1725.jpg [/file]\",\"payload\":{\"url\":\"https://d1edrlpyc25xu0.cloudfront.net/sdksample/image/upload/2GIfEQjvAV/balita5b21fef3-aa03-46b8-8056-d4eb063e1725.jpg\",\"caption\":\"\",\"file_name\":\"balita5b21fef3-aa03-46b8-8056-d4eb063e1725.png\",\"size\":0,\"pages\":1,\"encryption_key\":\"\"},\"room_avatar\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"room_id\":96304367,\"room_id_str\":\"96304367\",\"room_name\":\"arief93 testing34\",\"room_type\":\"single\",\"status\":\"sent\",\"timestamp\":\"2022-12-08T05:45:54Z\",\"topic_id\":96304367,\"topic_id_str\":\"96304367\",\"type\":\"file_attachment\",\"unique_temp_id\":\"android_1670478090617159uwee63c06857074d69d51\",\"unix_nano_timestamp\":1670478354425846000,\"unix_timestamp\":1670478354,\"user_avatar\":{\"avatar\":{\"url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\"}},\"user_avatar_url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"user_extras\":{},\"user_id\":1449907638,\"user_id_str\":\"1449907638\",\"username\":\"testing34\"},{\"comment_before_id\":1166637910,\"comment_before_id_str\":\"1166637910\",\"disable_link_preview\":false,\"email\":\"testing34\",\"extras\":{},\"id\":1166639891,\"id_str\":\"1166639891\",\"is_deleted\":false,\"is_public_channel\":false,\"message\":\"Gg\",\"payload\":{},\"room_avatar\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"room_id\":96304367,\"room_id_str\":\"96304367\",\"room_name\":\"arief93 testing34\",\"room_type\":\"single\",\"status\":\"sent\",\"timestamp\":\"2022-12-07T06:48:26Z\",\"topic_id\":96304367,\"topic_id_str\":\"96304367\",\"type\":\"text\",\"unique_temp_id\":\"android_1670395706312v1r6xfvs3c06857074d69d51\",\"unix_nano_timestamp\":1670395706498316000,\"unix_timestamp\":1670395706,\"user_avatar\":{\"avatar\":{\"url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\"}},\"user_avatar_url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"user_extras\":{},\"user_id\":1449907638,\"user_id_str\":\"1449907638\",\"username\":\"testing34\"},{\"comment_before_id\":1166637880,\"comment_before_id_str\":\"1166637880\",\"disable_link_preview\":false,\"email\":\"testing34\",\"extras\":{},\"id\":1166637910,\"id_str\":\"1166637910\",\"is_deleted\":false,\"is_public_channel\":false,\"message\":\"Jhh\",\"payload\":{},\"room_avatar\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"room_id\":96304367,\"room_id_str\":\"96304367\",\"room_name\":\"arief93 testing34\",\"room_type\":\"single\",\"status\":\"sent\",\"timestamp\":\"2022-12-07T06:47:01Z\",\"topic_id\":96304367,\"topic_id_str\":\"96304367\",\"type\":\"text\",\"unique_temp_id\":\"android_1670395620974itxge5xt3c06857074d69d51\",\"unix_nano_timestamp\":1670395621130158000,\"unix_timestamp\":1670395621,\"user_avatar\":{\"avatar\":{\"url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\"}},\"user_avatar_url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"user_extras\":{},\"user_id\":1449907638,\"user_id_str\":\"1449907638\",\"username\":\"testing34\"},{\"comment_before_id\":1166637861,\"comment_before_id_str\":\"1166637861\",\"disable_link_preview\":false,\"email\":\"testing34\",\"extras\":{},\"id\":1166637880,\"id_str\":\"1166637880\",\"is_deleted\":false,\"is_public_channel\":false,\"message\":\"Jjj\",\"payload\":{},\"room_avatar\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"room_id\":96304367,\"room_id_str\":\"96304367\",\"room_name\":\"arief93 testing34\",\"room_type\":\"single\",\"status\":\"sent\",\"timestamp\":\"2022-12-07T06:47:00Z\",\"topic_id\":96304367,\"topic_id_str\":\"96304367\",\"type\":\"text\",\"unique_temp_id\":\"android_1670395619939vjwtabqc3c06857074d69d51\",\"unix_nano_timestamp\":1670395620120134000,\"unix_timestamp\":1670395620,\"user_avatar\":{\"avatar\":{\"url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\"}},\"user_avatar_url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"user_extras\":{},\"user_id\":1449907638,\"user_id_str\":\"1449907638\",\"username\":\"testing34\"},{\"comment_before_id\":1162327164,\"comment_before_id_str\":\"1162327164\",\"disable_link_preview\":false,\"email\":\"testing34\",\"extras\":{},\"id\":1166637861,\"id_str\":\"1166637861\",\"is_deleted\":false,\"is_public_channel\":false,\"message\":\"Hhha\",\"payload\":{},\"room_avatar\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"room_id\":96304367,\"room_id_str\":\"96304367\",\"room_name\":\"arief93 testing34\",\"room_type\":\"single\",\"status\":\"sent\",\"timestamp\":\"2022-12-07T06:46:59Z\",\"topic_id\":96304367,\"topic_id_str\":\"96304367\",\"type\":\"text\",\"unique_temp_id\":\"android_1670395618808fk84mxs53c06857074d69d51\",\"unix_nano_timestamp\":1670395619000515000,\"unix_timestamp\":1670395619,\"user_avatar\":{\"avatar\":{\"url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\"}},\"user_avatar_url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"user_extras\":{},\"user_id\":1449907638,\"user_id_str\":\"1449907638\",\"username\":\"testing34\"},{\"comment_before_id\":1162327109,\"comment_before_id_str\":\"1162327109\",\"disable_link_preview\":false,\"email\":\"testing34\",\"extras\":{},\"id\":1162327164,\"id_str\":\"1162327164\",\"is_deleted\":false,\"is_public_channel\":false,\"message\":\"d\",\"payload\":{},\"room_avatar\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"room_id\":96304367,\"room_id_str\":\"96304367\",\"room_name\":\"arief93 testing34\",\"room_type\":\"single\",\"status\":\"delivered\",\"timestamp\":\"2022-12-02T06:30:55Z\",\"topic_id\":96304367,\"topic_id_str\":\"96304367\",\"type\":\"text\",\"unique_temp_id\":\"javascript-1669962655850\",\"unix_nano_timestamp\":1669962655988072000,\"unix_timestamp\":1669962655,\"user_avatar\":{\"avatar\":{\"url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\"}},\"user_avatar_url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"user_extras\":{},\"user_id\":1449907638,\"user_id_str\":\"1449907638\",\"username\":\"testing34\"},{\"comment_before_id\":1162327028,\"comment_before_id_str\":\"1162327028\",\"disable_link_preview\":false,\"email\":\"testing34\",\"extras\":{},\"id\":1162327109,\"id_str\":\"1162327109\",\"is_deleted\":false,\"is_public_channel\":false,\"message\":\"a\",\"payload\":{},\"room_avatar\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"room_id\":96304367,\"room_id_str\":\"96304367\",\"room_name\":\"arief93 testing34\",\"room_type\":\"single\",\"status\":\"delivered\",\"timestamp\":\"2022-12-02T06:30:52Z\",\"topic_id\":96304367,\"topic_id_str\":\"96304367\",\"type\":\"text\",\"unique_temp_id\":\"javascript-1669962651953\",\"unix_nano_timestamp\":1669962652129883000,\"unix_timestamp\":1669962652,\"user_avatar\":{\"avatar\":{\"url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\"}},\"user_avatar_url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"user_extras\":{},\"user_id\":1449907638,\"user_id_str\":\"1449907638\",\"username\":\"testing34\"},{\"comment_before_id\":1162326986,\"comment_before_id_str\":\"1162326986\",\"disable_link_preview\":false,\"email\":\"testing34\",\"extras\":{},\"id\":1162327028,\"id_str\":\"1162327028\",\"is_deleted\":false,\"is_public_channel\":false,\"message\":\"F\",\"payload\":{},\"room_avatar\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"room_id\":96304367,\"room_id_str\":\"96304367\",\"room_name\":\"arief93 testing34\",\"room_type\":\"single\",\"status\":\"delivered\",\"timestamp\":\"2022-12-02T06:30:48Z\",\"topic_id\":96304367,\"topic_id_str\":\"96304367\",\"type\":\"text\",\"unique_temp_id\":\"android_1669962648409s0zlbrm13c06857074d69d51\",\"unix_nano_timestamp\":1669962648551284000,\"unix_timestamp\":1669962648,\"user_avatar\":{\"avatar\":{\"url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\"}},\"user_avatar_url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"user_extras\":{},\"user_id\":1449907638,\"user_id_str\":\"1449907638\",\"username\":\"testing34\"},{\"comment_before_id\":1162175835,\"comment_before_id_str\":\"1162175835\",\"disable_link_preview\":false,\"email\":\"testing34\",\"extras\":{},\"id\":1162326986,\"id_str\":\"1162326986\",\"is_deleted\":false,\"is_public_channel\":false,\"message\":\"Hh\",\"payload\":{},\"room_avatar\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"room_id\":96304367,\"room_id_str\":\"96304367\",\"room_name\":\"arief93 testing34\",\"room_type\":\"single\",\"status\":\"delivered\",\"timestamp\":\"2022-12-02T06:30:46Z\",\"topic_id\":96304367,\"topic_id_str\":\"96304367\",\"type\":\"text\",\"unique_temp_id\":\"android_1669962646359sguzthb43c06857074d69d51\",\"unix_nano_timestamp\":1669962646475867000,\"unix_timestamp\":1669962646,\"user_avatar\":{\"avatar\":{\"url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\"}},\"user_avatar_url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"user_extras\":{},\"user_id\":1449907638,\"user_id_str\":\"1449907638\",\"username\":\"testing34\"},{\"comment_before_id\":1156050638,\"comment_before_id_str\":\"1156050638\",\"disable_link_preview\":false,\"email\":\"testing34\",\"extras\":{},\"id\":1162175835,\"id_str\":\"1162175835\",\"is_deleted\":false,\"is_public_channel\":false,\"message\":\"[file] https://d1edrlpyc25xu0.cloudfront.net/sdksample/image/upload/0EyfYMiIJf/kisspng-video-royalty-free-social-media-youtube-clip-art-piktochart-visual-editor-5ba36298529527.2262.png [/file]\",\"payload\":{\"url\":\"https://d1edrlpyc25xu0.cloudfront.net/sdksample/image/upload/0EyfYMiIJf/kisspng-video-royalty-free-social-media-youtube-clip-art-piktochart-visual-editor-5ba36298529527.2262.png\",\"caption\":\"\",\"file_name\":\"kisspng-video-royalty-free-social-media-youtube-clip-art-piktochart-visual-editor-5ba36298529527.2262200415374342643383.png\",\"size\":990985,\"pages\":1,\"encryption_key\":\"\"},\"room_avatar\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"room_id\":96304367,\"room_id_str\":\"96304367\",\"room_name\":\"arief93 testing34\",\"room_type\":\"single\",\"status\":\"delivered\",\"timestamp\":\"2022-12-02T03:59:36Z\",\"topic_id\":96304367,\"topic_id_str\":\"96304367\",\"type\":\"file_attachment\",\"unique_temp_id\":\"1669953571836\",\"unix_nano_timestamp\":1669953576260008000,\"unix_timestamp\":1669953576,\"user_avatar\":{\"avatar\":{\"url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\"}},\"user_avatar_url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"user_extras\":{},\"user_id\":1449907638,\"user_id_str\":\"1449907638\",\"username\":\"testing34\"},{\"comment_before_id\":1156050632,\"comment_before_id_str\":\"1156050632\",\"disable_link_preview\":false,\"email\":\"testing34\",\"extras\":{},\"id\":1156050638,\"id_str\":\"1156050638\",\"is_deleted\":false,\"is_public_channel\":false,\"message\":\"[file] https://d1edrlpyc25xu0.cloudfront.net/sdksample/image/upload/wb7USsysPO/IMG_20221116_070617.jpg [/file]\",\"payload\":{\"url\":\"https://d1edrlpyc25xu0.cloudfront.net/sdksample/image/upload/wb7USsysPO/IMG_20221116_070617.jpg\",\"caption\":\"\",\"file_name\":\"IMG_20221116_070617.png\",\"size\":0,\"pages\":1,\"encryption_key\":\"\"},\"room_avatar\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"room_id\":96304367,\"room_id_str\":\"96304367\",\"room_name\":\"arief93 testing34\",\"room_type\":\"single\",\"status\":\"delivered\",\"timestamp\":\"2022-11-24T19:41:36Z\",\"topic_id\":96304367,\"topic_id_str\":\"96304367\",\"type\":\"file_attachment\",\"unique_temp_id\":\"android_1669318892708mvkn0fh23cc1ffd39af94204\",\"unix_nano_timestamp\":1669318896399483000,\"unix_timestamp\":1669318896,\"user_avatar\":{\"avatar\":{\"url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\"}},\"user_avatar_url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"user_extras\":{},\"user_id\":1449907638,\"user_id_str\":\"1449907638\",\"username\":\"testing34\"},{\"comment_before_id\":1155176386,\"comment_before_id_str\":\"1155176386\",\"disable_link_preview\":false,\"email\":\"testing34\",\"extras\":{},\"id\":1156050632,\"id_str\":\"1156050632\",\"is_deleted\":false,\"is_public_channel\":false,\"message\":\"Hi\",\"payload\":{},\"room_avatar\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"room_id\":96304367,\"room_id_str\":\"96304367\",\"room_name\":\"arief93 testing34\",\"room_type\":\"single\",\"status\":\"delivered\",\"timestamp\":\"2022-11-24T19:41:23Z\",\"topic_id\":96304367,\"topic_id_str\":\"96304367\",\"type\":\"text\",\"unique_temp_id\":\"android_1669318882522i964s7z53cc1ffd39af94204\",\"unix_nano_timestamp\":1669318883672814000,\"unix_timestamp\":1669318883,\"user_avatar\":{\"avatar\":{\"url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\"}},\"user_avatar_url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"user_extras\":{},\"user_id\":1449907638,\"user_id_str\":\"1449907638\",\"username\":\"testing34\"},{\"comment_before_id\":1148573198,\"comment_before_id_str\":\"1148573198\",\"disable_link_preview\":false,\"email\":\"testing34\",\"extras\":{},\"id\":1155176386,\"id_str\":\"1155176386\",\"is_deleted\":false,\"is_public_channel\":false,\"message\":\"Yy\",\"payload\":{},\"room_avatar\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"room_id\":96304367,\"room_id_str\":\"96304367\",\"room_name\":\"arief93 testing34\",\"room_type\":\"single\",\"status\":\"delivered\",\"timestamp\":\"2022-11-23T14:28:20Z\",\"topic_id\":96304367,\"topic_id_str\":\"96304367\",\"type\":\"text\",\"unique_temp_id\":\"android_1669134213965tnukhw8815c9d59244c2d094\",\"unix_nano_timestamp\":1669213700058275000,\"unix_timestamp\":1669213700,\"user_avatar\":{\"avatar\":{\"url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\"}},\"user_avatar_url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"user_extras\":{},\"user_id\":1449907638,\"user_id_str\":\"1449907638\",\"username\":\"testing34\"},{\"comment_before_id\":1148573190,\"comment_before_id_str\":\"1148573190\",\"disable_link_preview\":false,\"email\":\"arief93\",\"extras\":{},\"id\":1148573198,\"id_str\":\"1148573198\",\"is_deleted\":false,\"is_public_channel\":false,\"message\":\"s\",\"payload\":{},\"room_avatar\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"room_id\":96304367,\"room_id_str\":\"96304367\",\"room_name\":\"arief93 testing34\",\"room_type\":\"single\",\"status\":\"delivered\",\"timestamp\":\"2022-11-15T07:46:19Z\",\"topic_id\":96304367,\"topic_id_str\":\"96304367\",\"type\":\"text\",\"unique_temp_id\":\"javascript-1668498379171\",\"unix_nano_timestamp\":1668498379299815000,\"unix_timestamp\":1668498379,\"user_avatar\":{\"avatar\":{\"url\":\"https://robohash.org/arief93/bgset_bg2/3.14160?set=set4\"}},\"user_avatar_url\":\"https://robohash.org/arief93/bgset_bg2/3.14160?set=set4\",\"user_extras\":{},\"user_id\":133493390,\"user_id_str\":\"133493390\",\"username\":\"arief93\"},{\"comment_before_id\":1148573053,\"comment_before_id_str\":\"1148573053\",\"disable_link_preview\":false,\"email\":\"arief93\",\"extras\":{},\"id\":1148573190,\"id_str\":\"1148573190\",\"is_deleted\":false,\"is_public_channel\":false,\"message\":\"d\",\"payload\":{},\"room_avatar\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"room_id\":96304367,\"room_id_str\":\"96304367\",\"room_name\":\"arief93 testing34\",\"room_type\":\"single\",\"status\":\"read\",\"timestamp\":\"2022-11-15T07:46:18Z\",\"topic_id\":96304367,\"topic_id_str\":\"96304367\",\"type\":\"text\",\"unique_temp_id\":\"javascript-1668498378441\",\"unix_nano_timestamp\":1668498378591543000,\"unix_timestamp\":1668498378,\"user_avatar\":{\"avatar\":{\"url\":\"https://robohash.org/arief93/bgset_bg2/3.14160?set=set4\"}},\"user_avatar_url\":\"https://robohash.org/arief93/bgset_bg2/3.14160?set=set4\",\"user_extras\":{},\"user_id\":133493390,\"user_id_str\":\"133493390\",\"username\":\"arief93\"},{\"comment_before_id\":1148573049,\"comment_before_id_str\":\"1148573049\",\"disable_link_preview\":false,\"email\":\"arief93\",\"extras\":{},\"id\":1148573053,\"id_str\":\"1148573053\",\"is_deleted\":false,\"is_public_channel\":false,\"message\":\"r\",\"payload\":{},\"room_avatar\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"room_id\":96304367,\"room_id_str\":\"96304367\",\"room_name\":\"arief93 testing34\",\"room_type\":\"single\",\"status\":\"read\",\"timestamp\":\"2022-11-15T07:46:08Z\",\"topic_id\":96304367,\"topic_id_str\":\"96304367\",\"type\":\"text\",\"unique_temp_id\":\"javascript-1668498368745\",\"unix_nano_timestamp\":1668498368878055000,\"unix_timestamp\":1668498368,\"user_avatar\":{\"avatar\":{\"url\":\"https://robohash.org/arief93/bgset_bg2/3.14160?set=set4\"}},\"user_avatar_url\":\"https://robohash.org/arief93/bgset_bg2/3.14160?set=set4\",\"user_extras\":{},\"user_id\":133493390,\"user_id_str\":\"133493390\",\"username\":\"arief93\"},{\"comment_before_id\":1148573041,\"comment_before_id_str\":\"1148573041\",\"disable_link_preview\":false,\"email\":\"arief93\",\"extras\":{},\"id\":1148573049,\"id_str\":\"1148573049\",\"is_deleted\":false,\"is_public_channel\":false,\"message\":\"e\",\"payload\":{},\"room_avatar\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"room_id\":96304367,\"room_id_str\":\"96304367\",\"room_name\":\"arief93 testing34\",\"room_type\":\"single\",\"status\":\"read\",\"timestamp\":\"2022-11-15T07:46:08Z\",\"topic_id\":96304367,\"topic_id_str\":\"96304367\",\"type\":\"text\",\"unique_temp_id\":\"javascript-1668498368377\",\"unix_nano_timestamp\":1668498368522827000,\"unix_timestamp\":1668498368,\"user_avatar\":{\"avatar\":{\"url\":\"https://robohash.org/arief93/bgset_bg2/3.14160?set=set4\"}},\"user_avatar_url\":\"https://robohash.org/arief93/bgset_bg2/3.14160?set=set4\",\"user_extras\":{},\"user_id\":133493390,\"user_id_str\":\"133493390\",\"username\":\"arief93\"},{\"comment_before_id\":1148572897,\"comment_before_id_str\":\"1148572897\",\"disable_link_preview\":false,\"email\":\"arief93\",\"extras\":{},\"id\":1148573041,\"id_str\":\"1148573041\",\"is_deleted\":false,\"is_public_channel\":false,\"message\":\"d\",\"payload\":{},\"room_avatar\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"room_id\":96304367,\"room_id_str\":\"96304367\",\"room_name\":\"arief93 testing34\",\"room_type\":\"single\",\"status\":\"read\",\"timestamp\":\"2022-11-15T07:46:08Z\",\"topic_id\":96304367,\"topic_id_str\":\"96304367\",\"type\":\"text\",\"unique_temp_id\":\"javascript-1668498367983\",\"unix_nano_timestamp\":1668498368140856000,\"unix_timestamp\":1668498368,\"user_avatar\":{\"avatar\":{\"url\":\"https://robohash.org/arief93/bgset_bg2/3.14160?set=set4\"}},\"user_avatar_url\":\"https://robohash.org/arief93/bgset_bg2/3.14160?set=set4\",\"user_extras\":{},\"user_id\":133493390,\"user_id_str\":\"133493390\",\"username\":\"arief93\"},{\"comment_before_id\":1148569127,\"comment_before_id_str\":\"1148569127\",\"disable_link_preview\":false,\"email\":\"arief93\",\"extras\":{},\"id\":1148572897,\"id_str\":\"1148572897\",\"is_deleted\":false,\"is_public_channel\":false,\"message\":\"c\",\"payload\":{},\"room_avatar\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"room_id\":96304367,\"room_id_str\":\"96304367\",\"room_name\":\"arief93 testing34\",\"room_type\":\"single\",\"status\":\"read\",\"timestamp\":\"2022-11-15T07:46:00Z\",\"topic_id\":96304367,\"topic_id_str\":\"96304367\",\"type\":\"text\",\"unique_temp_id\":\"javascript-1668498360231\",\"unix_nano_timestamp\":1668498360360627000,\"unix_timestamp\":1668498360,\"user_avatar\":{\"avatar\":{\"url\":\"https://robohash.org/arief93/bgset_bg2/3.14160?set=set4\"}},\"user_avatar_url\":\"https://robohash.org/arief93/bgset_bg2/3.14160?set=set4\",\"user_extras\":{},\"user_id\":133493390,\"user_id_str\":\"133493390\",\"username\":\"arief93\"}],\"is_participant\":true,\"room\":{\"avatar_url\":\"https://robohash.org/arief93/bgset_bg2/3.14160?set=set4\",\"chat_type\":\"single\",\"id\":96304367,\"id_str\":\"96304367\",\"is_public_channel\":false,\"last_comment_id\":1172292157,\"last_comment_id_str\":\"1172292157\",\"last_comment_message\":\"Ok\",\"last_topic_id\":96304367,\"last_topic_id_str\":\"96304367\",\"options\":\"{}\",\"participants\":[{\"active\":true,\"avatar_url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"email\":\"testing34\",\"extras\":{},\"id\":1449907638,\"id_str\":\"1449907638\",\"last_comment_read_id\":1172292157,\"last_comment_read_id_str\":\"1172292157\",\"last_comment_received_id\":1172292157,\"last_comment_received_id_str\":\"1172292157\",\"username\":\"testing34\"},{\"active\":true,\"avatar_url\":\"https://robohash.org/arief93/bgset_bg2/3.14160?set=set4\",\"email\":\"arief93\",\"extras\":{},\"id\":133493390,\"id_str\":\"133493390\",\"last_comment_read_id\":1148573190,\"last_comment_read_id_str\":\"1148573190\",\"last_comment_received_id\":1172292157,\"last_comment_received_id_str\":\"1172292157\",\"username\":\"arief93\"}],\"raw_room_name\":\"arief93 testing34\",\"room_name\":\"arief93\",\"room_total_participants\":2,\"unique_id\":\"0b1d6da69656afc49343a3e4658f338a\",\"unread_count\":0}},\"status\":200}";
    private JsonObject jsonObject = new JsonParser().parse(jsonString).getAsJsonObject();
    QiscusChatRoom qiscusChatRoom = QiscusApiParser.parseQiscusChatRoom(jsonObject);
    private QiscusChatRoomEventHandler qiscusChatRoomEventHandler;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        setupEngine();
        qiscusChatRoomEventHandler = getEventHandler(qiscusChatRoom);
        QiscusCore.getDataStore().addOrUpdate(qiscusChatRoom);

        /*QiscusCore.setup(application, "sdksample");

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
                    }});*/

//        QiscusChatRoomEvent event = new QiscusChatRoomEvent();
    }

    @Test
    public void qiscusChatRoomEventHandler() {
        qiscusChatRoomEventHandler.detach();
        QiscusChatRoomEvent event = new QiscusChatRoomEvent()
                .setRoomId(96304367)
                .setUser("sender2")
                .setEvent(QiscusChatRoomEvent.Event.READ)
                .setEventData(new JSONObject());

        QiscusChatRoomEvent event2 = new QiscusChatRoomEvent()
                .setRoomId(96304367)
                .setUser("sender")
                .setEvent(QiscusChatRoomEvent.Event.DELIVERED)
                .setEventData(new JSONObject());

        QiscusChatRoomEvent event3 = new QiscusChatRoomEvent()
                .setRoomId(96304367)
                .setUser("sender3")
                .setEvent(QiscusChatRoomEvent.Event.TYPING)
                .setEventData(new JSONObject());

        QiscusChatRoomEvent event4 = new QiscusChatRoomEvent()
                .setRoomId(96304367)
                .setUser("sender3")
                .setEvent(QiscusChatRoomEvent.Event.CUSTOM)
                .setEventData(new JSONObject());


        qiscusChatRoomEventHandler.onChatRoomEvent(event);
        qiscusChatRoomEventHandler.onChatRoomEvent(event2);
        qiscusChatRoomEventHandler.onChatRoomEvent(event3);
        qiscusChatRoomEventHandler.onChatRoomEvent(event4);
    }

    @Test
    public void qiscusChatRoomEventHandler2() {
        QiscusChatRoomEvent event = new QiscusChatRoomEvent()
                .setRoomId(963024367)
                .setUser("sender2")
                .setEvent(QiscusChatRoomEvent.Event.CUSTOM)
                .setEventData(new JSONObject());

        qiscusChatRoomEventHandler.onChatRoomEvent(event);
        qiscusChatRoomEventHandler.listenChatRoomEvent();

        qiscusChatRoomEventHandler.setMemberState();


        QiscusChatRoom room = new QiscusChatRoom();
        room.setMember(new ArrayList<>());
        QiscusCore.getDataStore().addOrUpdate(room);

        qiscusChatRoomEventHandler.setMemberState();

    }

    @Test
    public void onGotoComment() {
        QiscusComment qiscusComment = new QiscusComment();
        qiscusComment.setId(1180829033);
        qiscusComment.setRoomId(96304367);
        qiscusComment.setUniqueId("javascript-1671681387726");
        qiscusComment.setCommentBeforeId(1180828960);
        qiscusComment.setMessage("aa");
        qiscusComment.setSender("arief93");
        qiscusComment.setSenderEmail("arief93");
        qiscusComment.setSenderAvatar("https://robohash.org/arief93/bgset_bg2/3.14160?set=set4");
        qiscusComment.setTime(new Date("Thu Dec 22 10:56:27 GMT+07:00 2022"));
        qiscusComment.setState(2);
        qiscusComment.setDeleted(false);
        qiscusComment.setHardDeleted(false);
        try {
            qiscusComment.setUserExtras(new JSONObject("{}"));
            qiscusComment.setExtras(new JSONObject("{}"));
            qiscusComment.setExtraPayload("{}");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        qiscusChatRoom.setLastComment(qiscusComment);
        QiscusCore.getDataStore().addOrUpdate(qiscusChatRoom);
        qiscusChatRoomEventHandler.onGotComment(qiscusComment);
    }

    @Test
    public void onGotoCommnet2() {
        QiscusComment qiscusComment2 = new QiscusComment();
        qiscusComment2.setId(1180829023);
        qiscusComment2.setRoomId(963043673);
        qiscusComment2.setUniqueId("javascript-16716813287726");
        qiscusComment2.setCommentBeforeId(1180828260);
        qiscusComment2.setMessage("aaa");
        qiscusComment2.setSender("arief92");
        qiscusComment2.setSenderEmail("arief92");

        qiscusChatRoom.setLastComment(qiscusComment2);
        QiscusCore.getDataStore().addOrUpdate(qiscusChatRoom);
        qiscusChatRoomEventHandler.onGotComment(qiscusComment2);
    }

    @Test
    public void onGotoCommnet3() {
        QiscusComment qiscusComment3 = new QiscusComment();
        qiscusComment3.setId(1180829033);
        qiscusComment3.setRoomId(96304367);
        qiscusComment3.setUniqueId("javascript-1671681387726");
        qiscusComment3.setCommentBeforeId(1180828960);
        qiscusComment3.setMessage("aa");
        qiscusComment3.setRawType("system_event");
        qiscusComment3.setSender("arief93");
        qiscusComment3.setSenderEmail("arief93");
        qiscusComment3.setSenderAvatar("https://robohash.org/arief93/bgset_bg2/3.14160?set=set4");
        qiscusComment3.setTime(new Date("Thu Dec 22 10:56:27 GMT+07:00 2022"));
        qiscusComment3.setState(3);
        qiscusComment3.setDeleted(false);
        qiscusComment3.setHardDeleted(false);

        try {
            qiscusComment3.setUserExtras(new JSONObject("{}"));
            qiscusComment3.setExtras(new JSONObject("{}"));
            qiscusComment3.setExtraPayload("{\n" +
                    "            \"type\": \"add_member\",\n" +
                    "                \"subject_username\": \"Ucup\",\n" +
                    "                \"subject_email\": \"ucup@qiscus.com\",\n" +
                    "                \"object_username\": \"Ani\",\n" +
                    "                \"object_email\": \"ani@qiscus.com\"\n" +
                    "        }");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        qiscusChatRoom.setLastComment(qiscusComment3);
        QiscusCore.getDataStore().addOrUpdate(qiscusChatRoom);
        qiscusChatRoomEventHandler.onGotComment(qiscusComment3);
    }

    @Test
    public void onGotoCommnet4() {
        QiscusComment qiscusComment4 = new QiscusComment();
        qiscusComment4.setId(1180829033);
        qiscusComment4.setRoomId(96304367);
        qiscusComment4.setUniqueId("javascript-1671681387726");
        qiscusComment4.setCommentBeforeId(1180828960);
        qiscusComment4.setMessage("aa");
        qiscusComment4.setRawType("system_event");
        qiscusComment4.setSender("arief93");
        qiscusComment4.setSenderEmail("arief93");
        qiscusComment4.setSenderAvatar("https://robohash.org/arief93/bgset_bg2/3.14160?set=set4");
        qiscusComment4.setTime(new Date("Thu Dec 22 10:56:27 GMT+07:00 2022"));
        qiscusComment4.setState(3);
        qiscusComment4.setDeleted(false);
        qiscusComment4.setHardDeleted(false);

        try {
            qiscusComment4.setUserExtras(new JSONObject("{}"));
            qiscusComment4.setExtras(new JSONObject("{}"));
            qiscusComment4.setExtraPayload("{\n" +
                    "    \"type\": \"join_room\",\n" +
                    "    \"subject_username\": \"Ucup\",\n" +
                    "    \"subject_email\": \"ucup@qiscus.com\",\n" +
                    "    \"room_name\": \"Qiscus\"\n" +
                    "}");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        qiscusChatRoom.setLastComment(qiscusComment4);
        QiscusCore.getDataStore().addOrUpdate(qiscusChatRoom);
        qiscusChatRoomEventHandler.onGotComment(qiscusComment4);
    }

    @Test
    public void onGotoCommnet5() {
        QiscusComment qiscusComment5 = new QiscusComment();
        qiscusComment5.setId(1180829033);
        qiscusComment5.setRoomId(96304367);
        qiscusComment5.setUniqueId("javascript-1671681387726");
        qiscusComment5.setCommentBeforeId(1180828960);
        qiscusComment5.setMessage("aa");
        qiscusComment5.setRawType("system_event");
        qiscusComment5.setSender("arief93");
        qiscusComment5.setSenderEmail("arief93");
        qiscusComment5.setSenderAvatar("https://robohash.org/arief93/bgset_bg2/3.14160?set=set4");
        qiscusComment5.setTime(new Date("Thu Dec 22 10:56:27 GMT+07:00 2022"));
        qiscusComment5.setState(3);
        qiscusComment5.setDeleted(false);
        qiscusComment5.setHardDeleted(false);

        try {
            qiscusComment5.setUserExtras(new JSONObject("{}"));
            qiscusComment5.setExtras(new JSONObject("{}"));
            qiscusComment5.setExtraPayload("{\n" +
                    "    \"type\": \"remove_member\",\n" +
                    "    \"subject_username\": \"Ucup\",\n" +
                    "    \"subject_email\": \"ucup@qiscus.com\",\n" +
                    "    \"object_username\": \"Ani\",\n" +
                    "    \"object_email\": \"ani@qiscus.com\"\n" +
                    "}");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        qiscusChatRoom.setLastComment(qiscusComment5);
        QiscusCore.getDataStore().addOrUpdate(qiscusChatRoom);
        qiscusChatRoomEventHandler.onGotComment(qiscusComment5);
    }

    @Test
    public void onGotoCommnet6() {
        QiscusComment qiscusComment6 = new QiscusComment();
        qiscusComment6.setId(1180829033);
        qiscusComment6.setRoomId(96304367);
        qiscusComment6.setUniqueId("javascript-1671681387726");
        qiscusComment6.setCommentBeforeId(1180828960);
        qiscusComment6.setMessage("aa");
        qiscusComment6.setRawType("system_event");
        qiscusComment6.setSender("arief93");
        qiscusComment6.setSenderEmail("arief93");
        qiscusComment6.setSenderAvatar("https://robohash.org/arief93/bgset_bg2/3.14160?set=set4");
        qiscusComment6.setTime(new Date("Thu Dec 22 10:56:27 GMT+07:00 2022"));
        qiscusComment6.setState(3);
        qiscusComment6.setDeleted(false);
        qiscusComment6.setHardDeleted(false);

        try {
            qiscusComment6.setUserExtras(new JSONObject("{}"));
            qiscusComment6.setExtras(new JSONObject("{}"));
            qiscusComment6.setExtraPayload("{\n" +
                    "    \"type\": \"left_room\",\n" +
                    "    \"subject_username\": \"Ucup\",\n" +
                    "    \"subject_email\": \"ucup@qiscus.com\",\n" +
                    "    \"object_username\": \"Ani\",\n" +
                    "    \"object_email\": \"ani@qiscus.com\"\n" +
                    "}");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        qiscusChatRoom.setLastComment(qiscusComment6);
        QiscusCore.getDataStore().addOrUpdate(qiscusChatRoom);
        qiscusChatRoomEventHandler.onGotComment(qiscusComment6);
    }

    @Test
    public void onGotoCommnet7() {
        QiscusComment qiscusComment7 = new QiscusComment();
        qiscusComment7.setId(1180829033);
        qiscusComment7.setRoomId(102220202);
        qiscusComment7.setUniqueId("javascript-1671681387726");
        qiscusComment7.setCommentBeforeId(1180828960);
        qiscusComment7.setMessage("aa");
        qiscusComment7.setRawType("system_event");
        qiscusComment7.setSender("arief93");
        qiscusComment7.setSenderEmail("arief93");
        qiscusComment7.setSenderAvatar("https://robohash.org/arief93/bgset_bg2/3.14160?set=set4");
        qiscusComment7.setTime(new Date("Thu Dec 22 10:56:27 GMT+07:00 2022"));
        qiscusComment7.setState(3);
        qiscusComment7.setDeleted(false);
        qiscusComment7.setHardDeleted(false);

        try {
            qiscusComment7.setUserExtras(new JSONObject("{}"));
            qiscusComment7.setExtras(new JSONObject("{}"));
            qiscusComment7.setExtraPayload("{\n" +
                    "            \"type\": \"change_room_name\",\n" +
                    "                \"subject_username\": \"Ucup\",\n" +
                    "                \"subject_email\": \"ucup@qiscus.com\",\n" +
                    "                \"room_name\": \"Qiscus Super Family\"\n" +
                    "        }");
        } catch (JSONException e) {
            e.printStackTrace();
        }

//        qiscusChatRoom.setLastComment(qiscusComment7);
//        QiscusCore.getDataStore().addOrUpdate(qiscusChatRoom);
        qiscusChatRoomEventHandler.onGotComment(qiscusComment7);
    }

    @Test
    public void handleMemberRemovedTest() {
        QiscusRoomMember member = new QiscusRoomMember();
        member.setEmail("testing34");

        try {
            extractMethode(qiscusChatRoomEventHandler, "handleMemberRemoved", QiscusRoomMember.class)
                    .invoke(qiscusChatRoomEventHandler, member);
        } catch (IllegalAccessException | InvocationTargetException e) {
            // ignored
        }
    }

    @Test
    public void handleMemberAddedTest() {
        QiscusRoomMember member = new QiscusRoomMember();
        member.setEmail("not_found");

        try {
            extractMethode(qiscusChatRoomEventHandler, "handleMemberAdded", QiscusRoomMember.class)
                    .invoke(qiscusChatRoomEventHandler, member);
        } catch (IllegalAccessException | InvocationTargetException e) {
            // ignored
        }
    }

    private QiscusChatRoomEventHandler getEventHandler(QiscusChatRoom qiscusChatRoom) {
        return new QiscusChatRoomEventHandler(qiscusChatRoom, new QiscusChatRoomEventHandler.StateListener() {
            @Override
            public void onChatRoomNameChanged(String name) {

            }

            @Override
            public void onChatRoomMemberAdded(QiscusRoomMember member) {

            }

            @Override
            public void onChatRoomMemberRemoved(QiscusRoomMember member) {

            }

            @Override
            public void onUserTypng(String email, boolean typing) {

            }

            @Override
            public void onChangeLastDelivered(long lastDeliveredCommentId) {

            }

            @Override
            public void onChangeLastRead(long lastReadCommentId) {

            }
        });
    }

}