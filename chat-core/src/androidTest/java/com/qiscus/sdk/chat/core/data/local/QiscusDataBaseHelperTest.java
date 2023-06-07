package com.qiscus.sdk.chat.core.data.local;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.qiscus.sdk.chat.core.InstrumentationBaseTest;
import com.qiscus.sdk.chat.core.QiscusCore;
import com.qiscus.sdk.chat.core.data.model.QiscusChatRoom;
import com.qiscus.sdk.chat.core.data.model.QiscusComment;
import com.qiscus.sdk.chat.core.data.model.QiscusRoomMember;
import com.qiscus.sdk.chat.core.data.remote.QiscusApiParser;
import com.qiscus.sdk.chat.core.util.QiscusErrorLogger;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

@RunWith(AndroidJUnit4ClassRunner.class)
public class QiscusDataBaseHelperTest extends InstrumentationBaseTest {

    private final String jsonString = "{\"results\":{\"comments\":[{\"comment_before_id\":1167549498,\"comment_before_id_str\":\"1167549498\",\"disable_link_preview\":false,\"email\":\"testing34\",\"extras\":{},\"id\":1172292157,\"id_str\":\"1172292157\",\"is_deleted\":false,\"is_public_channel\":false,\"message\":\"Ok\",\"payload\":{},\"room_avatar\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"room_id\":96304367,\"room_id_str\":\"96304367\",\"room_name\":\"arief93 testing34\",\"room_type\":\"single\",\"status\":\"sent\",\"timestamp\":\"2022-12-13T07:11:59Z\",\"topic_id\":96304367,\"topic_id_str\":\"96304367\",\"type\":\"text\",\"unique_temp_id\":\"android_1670915519612kdccsvcv15c9d59244c2d094\",\"unix_nano_timestamp\":1670915519723558000,\"unix_timestamp\":1670915519,\"user_avatar\":{\"avatar\":{\"url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\"}},\"user_avatar_url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"user_extras\":{},\"user_id\":1449907638,\"user_id_str\":\"1449907638\",\"username\":\"testing34\"},{\"comment_before_id\":1166639891,\"comment_before_id_str\":\"1166639891\",\"disable_link_preview\":false,\"email\":\"testing34\",\"extras\":{},\"id\":1167549498,\"id_str\":\"1167549498\",\"is_deleted\":false,\"is_public_channel\":false,\"message\":\"[file] https://d1edrlpyc25xu0.cloudfront.net/sdksample/image/upload/2GIfEQjvAV/balita5b21fef3-aa03-46b8-8056-d4eb063e1725.jpg [/file]\",\"payload\":{\"url\":\"https://d1edrlpyc25xu0.cloudfront.net/sdksample/image/upload/2GIfEQjvAV/balita5b21fef3-aa03-46b8-8056-d4eb063e1725.jpg\",\"caption\":\"\",\"file_name\":\"balita5b21fef3-aa03-46b8-8056-d4eb063e1725.png\",\"size\":0,\"pages\":1,\"encryption_key\":\"\"},\"room_avatar\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"room_id\":96304367,\"room_id_str\":\"96304367\",\"room_name\":\"arief93 testing34\",\"room_type\":\"single\",\"status\":\"sent\",\"timestamp\":\"2022-12-08T05:45:54Z\",\"topic_id\":96304367,\"topic_id_str\":\"96304367\",\"type\":\"file_attachment\",\"unique_temp_id\":\"android_1670478090617159uwee63c06857074d69d51\",\"unix_nano_timestamp\":1670478354425846000,\"unix_timestamp\":1670478354,\"user_avatar\":{\"avatar\":{\"url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\"}},\"user_avatar_url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"user_extras\":{},\"user_id\":1449907638,\"user_id_str\":\"1449907638\",\"username\":\"testing34\"},{\"comment_before_id\":1166637910,\"comment_before_id_str\":\"1166637910\",\"disable_link_preview\":false,\"email\":\"testing34\",\"extras\":{},\"id\":1166639891,\"id_str\":\"1166639891\",\"is_deleted\":false,\"is_public_channel\":false,\"message\":\"Gg\",\"payload\":{},\"room_avatar\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"room_id\":96304367,\"room_id_str\":\"96304367\",\"room_name\":\"arief93 testing34\",\"room_type\":\"single\",\"status\":\"sent\",\"timestamp\":\"2022-12-07T06:48:26Z\",\"topic_id\":96304367,\"topic_id_str\":\"96304367\",\"type\":\"text\",\"unique_temp_id\":\"android_1670395706312v1r6xfvs3c06857074d69d51\",\"unix_nano_timestamp\":1670395706498316000,\"unix_timestamp\":1670395706,\"user_avatar\":{\"avatar\":{\"url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\"}},\"user_avatar_url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"user_extras\":{},\"user_id\":1449907638,\"user_id_str\":\"1449907638\",\"username\":\"testing34\"},{\"comment_before_id\":1166637880,\"comment_before_id_str\":\"1166637880\",\"disable_link_preview\":false,\"email\":\"testing34\",\"extras\":{},\"id\":1166637910,\"id_str\":\"1166637910\",\"is_deleted\":false,\"is_public_channel\":false,\"message\":\"Jhh\",\"payload\":{},\"room_avatar\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"room_id\":96304367,\"room_id_str\":\"96304367\",\"room_name\":\"arief93 testing34\",\"room_type\":\"single\",\"status\":\"sent\",\"timestamp\":\"2022-12-07T06:47:01Z\",\"topic_id\":96304367,\"topic_id_str\":\"96304367\",\"type\":\"text\",\"unique_temp_id\":\"android_1670395620974itxge5xt3c06857074d69d51\",\"unix_nano_timestamp\":1670395621130158000,\"unix_timestamp\":1670395621,\"user_avatar\":{\"avatar\":{\"url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\"}},\"user_avatar_url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"user_extras\":{},\"user_id\":1449907638,\"user_id_str\":\"1449907638\",\"username\":\"testing34\"},{\"comment_before_id\":1166637861,\"comment_before_id_str\":\"1166637861\",\"disable_link_preview\":false,\"email\":\"testing34\",\"extras\":{},\"id\":1166637880,\"id_str\":\"1166637880\",\"is_deleted\":false,\"is_public_channel\":false,\"message\":\"Jjj\",\"payload\":{},\"room_avatar\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"room_id\":96304367,\"room_id_str\":\"96304367\",\"room_name\":\"arief93 testing34\",\"room_type\":\"single\",\"status\":\"sent\",\"timestamp\":\"2022-12-07T06:47:00Z\",\"topic_id\":96304367,\"topic_id_str\":\"96304367\",\"type\":\"text\",\"unique_temp_id\":\"android_1670395619939vjwtabqc3c06857074d69d51\",\"unix_nano_timestamp\":1670395620120134000,\"unix_timestamp\":1670395620,\"user_avatar\":{\"avatar\":{\"url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\"}},\"user_avatar_url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"user_extras\":{},\"user_id\":1449907638,\"user_id_str\":\"1449907638\",\"username\":\"testing34\"},{\"comment_before_id\":1162327164,\"comment_before_id_str\":\"1162327164\",\"disable_link_preview\":false,\"email\":\"testing34\",\"extras\":{},\"id\":1166637861,\"id_str\":\"1166637861\",\"is_deleted\":false,\"is_public_channel\":false,\"message\":\"Hhha\",\"payload\":{},\"room_avatar\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"room_id\":96304367,\"room_id_str\":\"96304367\",\"room_name\":\"arief93 testing34\",\"room_type\":\"single\",\"status\":\"sent\",\"timestamp\":\"2022-12-07T06:46:59Z\",\"topic_id\":96304367,\"topic_id_str\":\"96304367\",\"type\":\"text\",\"unique_temp_id\":\"android_1670395618808fk84mxs53c06857074d69d51\",\"unix_nano_timestamp\":1670395619000515000,\"unix_timestamp\":1670395619,\"user_avatar\":{\"avatar\":{\"url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\"}},\"user_avatar_url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"user_extras\":{},\"user_id\":1449907638,\"user_id_str\":\"1449907638\",\"username\":\"testing34\"},{\"comment_before_id\":1162327109,\"comment_before_id_str\":\"1162327109\",\"disable_link_preview\":false,\"email\":\"testing34\",\"extras\":{},\"id\":1162327164,\"id_str\":\"1162327164\",\"is_deleted\":false,\"is_public_channel\":false,\"message\":\"d\",\"payload\":{},\"room_avatar\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"room_id\":96304367,\"room_id_str\":\"96304367\",\"room_name\":\"arief93 testing34\",\"room_type\":\"single\",\"status\":\"delivered\",\"timestamp\":\"2022-12-02T06:30:55Z\",\"topic_id\":96304367,\"topic_id_str\":\"96304367\",\"type\":\"text\",\"unique_temp_id\":\"javascript-1669962655850\",\"unix_nano_timestamp\":1669962655988072000,\"unix_timestamp\":1669962655,\"user_avatar\":{\"avatar\":{\"url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\"}},\"user_avatar_url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"user_extras\":{},\"user_id\":1449907638,\"user_id_str\":\"1449907638\",\"username\":\"testing34\"},{\"comment_before_id\":1162327028,\"comment_before_id_str\":\"1162327028\",\"disable_link_preview\":false,\"email\":\"testing34\",\"extras\":{},\"id\":1162327109,\"id_str\":\"1162327109\",\"is_deleted\":false,\"is_public_channel\":false,\"message\":\"a\",\"payload\":{},\"room_avatar\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"room_id\":96304367,\"room_id_str\":\"96304367\",\"room_name\":\"arief93 testing34\",\"room_type\":\"single\",\"status\":\"delivered\",\"timestamp\":\"2022-12-02T06:30:52Z\",\"topic_id\":96304367,\"topic_id_str\":\"96304367\",\"type\":\"text\",\"unique_temp_id\":\"javascript-1669962651953\",\"unix_nano_timestamp\":1669962652129883000,\"unix_timestamp\":1669962652,\"user_avatar\":{\"avatar\":{\"url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\"}},\"user_avatar_url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"user_extras\":{},\"user_id\":1449907638,\"user_id_str\":\"1449907638\",\"username\":\"testing34\"},{\"comment_before_id\":1162326986,\"comment_before_id_str\":\"1162326986\",\"disable_link_preview\":false,\"email\":\"testing34\",\"extras\":{},\"id\":1162327028,\"id_str\":\"1162327028\",\"is_deleted\":false,\"is_public_channel\":false,\"message\":\"F\",\"payload\":{},\"room_avatar\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"room_id\":96304367,\"room_id_str\":\"96304367\",\"room_name\":\"arief93 testing34\",\"room_type\":\"single\",\"status\":\"delivered\",\"timestamp\":\"2022-12-02T06:30:48Z\",\"topic_id\":96304367,\"topic_id_str\":\"96304367\",\"type\":\"text\",\"unique_temp_id\":\"android_1669962648409s0zlbrm13c06857074d69d51\",\"unix_nano_timestamp\":1669962648551284000,\"unix_timestamp\":1669962648,\"user_avatar\":{\"avatar\":{\"url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\"}},\"user_avatar_url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"user_extras\":{},\"user_id\":1449907638,\"user_id_str\":\"1449907638\",\"username\":\"testing34\"},{\"comment_before_id\":1162175835,\"comment_before_id_str\":\"1162175835\",\"disable_link_preview\":false,\"email\":\"testing34\",\"extras\":{},\"id\":1162326986,\"id_str\":\"1162326986\",\"is_deleted\":false,\"is_public_channel\":false,\"message\":\"Hh\",\"payload\":{},\"room_avatar\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"room_id\":96304367,\"room_id_str\":\"96304367\",\"room_name\":\"arief93 testing34\",\"room_type\":\"single\",\"status\":\"delivered\",\"timestamp\":\"2022-12-02T06:30:46Z\",\"topic_id\":96304367,\"topic_id_str\":\"96304367\",\"type\":\"text\",\"unique_temp_id\":\"android_1669962646359sguzthb43c06857074d69d51\",\"unix_nano_timestamp\":1669962646475867000,\"unix_timestamp\":1669962646,\"user_avatar\":{\"avatar\":{\"url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\"}},\"user_avatar_url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"user_extras\":{},\"user_id\":1449907638,\"user_id_str\":\"1449907638\",\"username\":\"testing34\"},{\"comment_before_id\":1156050638,\"comment_before_id_str\":\"1156050638\",\"disable_link_preview\":false,\"email\":\"testing34\",\"extras\":{},\"id\":1162175835,\"id_str\":\"1162175835\",\"is_deleted\":false,\"is_public_channel\":false,\"message\":\"[file] https://d1edrlpyc25xu0.cloudfront.net/sdksample/image/upload/0EyfYMiIJf/kisspng-video-royalty-free-social-media-youtube-clip-art-piktochart-visual-editor-5ba36298529527.2262.png [/file]\",\"payload\":{\"url\":\"https://d1edrlpyc25xu0.cloudfront.net/sdksample/image/upload/0EyfYMiIJf/kisspng-video-royalty-free-social-media-youtube-clip-art-piktochart-visual-editor-5ba36298529527.2262.png\",\"caption\":\"\",\"file_name\":\"kisspng-video-royalty-free-social-media-youtube-clip-art-piktochart-visual-editor-5ba36298529527.2262200415374342643383.png\",\"size\":990985,\"pages\":1,\"encryption_key\":\"\"},\"room_avatar\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"room_id\":96304367,\"room_id_str\":\"96304367\",\"room_name\":\"arief93 testing34\",\"room_type\":\"single\",\"status\":\"delivered\",\"timestamp\":\"2022-12-02T03:59:36Z\",\"topic_id\":96304367,\"topic_id_str\":\"96304367\",\"type\":\"file_attachment\",\"unique_temp_id\":\"1669953571836\",\"unix_nano_timestamp\":1669953576260008000,\"unix_timestamp\":1669953576,\"user_avatar\":{\"avatar\":{\"url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\"}},\"user_avatar_url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"user_extras\":{},\"user_id\":1449907638,\"user_id_str\":\"1449907638\",\"username\":\"testing34\"},{\"comment_before_id\":1156050632,\"comment_before_id_str\":\"1156050632\",\"disable_link_preview\":false,\"email\":\"testing34\",\"extras\":{},\"id\":1156050638,\"id_str\":\"1156050638\",\"is_deleted\":false,\"is_public_channel\":false,\"message\":\"[file] https://d1edrlpyc25xu0.cloudfront.net/sdksample/image/upload/wb7USsysPO/IMG_20221116_070617.jpg [/file]\",\"payload\":{\"url\":\"https://d1edrlpyc25xu0.cloudfront.net/sdksample/image/upload/wb7USsysPO/IMG_20221116_070617.jpg\",\"caption\":\"\",\"file_name\":\"IMG_20221116_070617.png\",\"size\":0,\"pages\":1,\"encryption_key\":\"\"},\"room_avatar\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"room_id\":96304367,\"room_id_str\":\"96304367\",\"room_name\":\"arief93 testing34\",\"room_type\":\"single\",\"status\":\"delivered\",\"timestamp\":\"2022-11-24T19:41:36Z\",\"topic_id\":96304367,\"topic_id_str\":\"96304367\",\"type\":\"file_attachment\",\"unique_temp_id\":\"android_1669318892708mvkn0fh23cc1ffd39af94204\",\"unix_nano_timestamp\":1669318896399483000,\"unix_timestamp\":1669318896,\"user_avatar\":{\"avatar\":{\"url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\"}},\"user_avatar_url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"user_extras\":{},\"user_id\":1449907638,\"user_id_str\":\"1449907638\",\"username\":\"testing34\"},{\"comment_before_id\":1155176386,\"comment_before_id_str\":\"1155176386\",\"disable_link_preview\":false,\"email\":\"testing34\",\"extras\":{},\"id\":1156050632,\"id_str\":\"1156050632\",\"is_deleted\":false,\"is_public_channel\":false,\"message\":\"Hi\",\"payload\":{},\"room_avatar\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"room_id\":96304367,\"room_id_str\":\"96304367\",\"room_name\":\"arief93 testing34\",\"room_type\":\"single\",\"status\":\"delivered\",\"timestamp\":\"2022-11-24T19:41:23Z\",\"topic_id\":96304367,\"topic_id_str\":\"96304367\",\"type\":\"text\",\"unique_temp_id\":\"android_1669318882522i964s7z53cc1ffd39af94204\",\"unix_nano_timestamp\":1669318883672814000,\"unix_timestamp\":1669318883,\"user_avatar\":{\"avatar\":{\"url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\"}},\"user_avatar_url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"user_extras\":{},\"user_id\":1449907638,\"user_id_str\":\"1449907638\",\"username\":\"testing34\"},{\"comment_before_id\":1148573198,\"comment_before_id_str\":\"1148573198\",\"disable_link_preview\":false,\"email\":\"testing34\",\"extras\":{},\"id\":1155176386,\"id_str\":\"1155176386\",\"is_deleted\":false,\"is_public_channel\":false,\"message\":\"Yy\",\"payload\":{},\"room_avatar\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"room_id\":96304367,\"room_id_str\":\"96304367\",\"room_name\":\"arief93 testing34\",\"room_type\":\"single\",\"status\":\"delivered\",\"timestamp\":\"2022-11-23T14:28:20Z\",\"topic_id\":96304367,\"topic_id_str\":\"96304367\",\"type\":\"text\",\"unique_temp_id\":\"android_1669134213965tnukhw8815c9d59244c2d094\",\"unix_nano_timestamp\":1669213700058275000,\"unix_timestamp\":1669213700,\"user_avatar\":{\"avatar\":{\"url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\"}},\"user_avatar_url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"user_extras\":{},\"user_id\":1449907638,\"user_id_str\":\"1449907638\",\"username\":\"testing34\"},{\"comment_before_id\":1148573190,\"comment_before_id_str\":\"1148573190\",\"disable_link_preview\":false,\"email\":\"arief93\",\"extras\":{},\"id\":1148573198,\"id_str\":\"1148573198\",\"is_deleted\":false,\"is_public_channel\":false,\"message\":\"s\",\"payload\":{},\"room_avatar\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"room_id\":96304367,\"room_id_str\":\"96304367\",\"room_name\":\"arief93 testing34\",\"room_type\":\"single\",\"status\":\"delivered\",\"timestamp\":\"2022-11-15T07:46:19Z\",\"topic_id\":96304367,\"topic_id_str\":\"96304367\",\"type\":\"text\",\"unique_temp_id\":\"javascript-1668498379171\",\"unix_nano_timestamp\":1668498379299815000,\"unix_timestamp\":1668498379,\"user_avatar\":{\"avatar\":{\"url\":\"https://robohash.org/arief93/bgset_bg2/3.14160?set=set4\"}},\"user_avatar_url\":\"https://robohash.org/arief93/bgset_bg2/3.14160?set=set4\",\"user_extras\":{},\"user_id\":133493390,\"user_id_str\":\"133493390\",\"username\":\"arief93\"},{\"comment_before_id\":1148573053,\"comment_before_id_str\":\"1148573053\",\"disable_link_preview\":false,\"email\":\"arief93\",\"extras\":{},\"id\":1148573190,\"id_str\":\"1148573190\",\"is_deleted\":false,\"is_public_channel\":false,\"message\":\"d\",\"payload\":{},\"room_avatar\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"room_id\":96304367,\"room_id_str\":\"96304367\",\"room_name\":\"arief93 testing34\",\"room_type\":\"single\",\"status\":\"read\",\"timestamp\":\"2022-11-15T07:46:18Z\",\"topic_id\":96304367,\"topic_id_str\":\"96304367\",\"type\":\"text\",\"unique_temp_id\":\"javascript-1668498378441\",\"unix_nano_timestamp\":1668498378591543000,\"unix_timestamp\":1668498378,\"user_avatar\":{\"avatar\":{\"url\":\"https://robohash.org/arief93/bgset_bg2/3.14160?set=set4\"}},\"user_avatar_url\":\"https://robohash.org/arief93/bgset_bg2/3.14160?set=set4\",\"user_extras\":{},\"user_id\":133493390,\"user_id_str\":\"133493390\",\"username\":\"arief93\"},{\"comment_before_id\":1148573049,\"comment_before_id_str\":\"1148573049\",\"disable_link_preview\":false,\"email\":\"arief93\",\"extras\":{},\"id\":1148573053,\"id_str\":\"1148573053\",\"is_deleted\":false,\"is_public_channel\":false,\"message\":\"r\",\"payload\":{},\"room_avatar\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"room_id\":96304367,\"room_id_str\":\"96304367\",\"room_name\":\"arief93 testing34\",\"room_type\":\"single\",\"status\":\"read\",\"timestamp\":\"2022-11-15T07:46:08Z\",\"topic_id\":96304367,\"topic_id_str\":\"96304367\",\"type\":\"text\",\"unique_temp_id\":\"javascript-1668498368745\",\"unix_nano_timestamp\":1668498368878055000,\"unix_timestamp\":1668498368,\"user_avatar\":{\"avatar\":{\"url\":\"https://robohash.org/arief93/bgset_bg2/3.14160?set=set4\"}},\"user_avatar_url\":\"https://robohash.org/arief93/bgset_bg2/3.14160?set=set4\",\"user_extras\":{},\"user_id\":133493390,\"user_id_str\":\"133493390\",\"username\":\"arief93\"},{\"comment_before_id\":1148573041,\"comment_before_id_str\":\"1148573041\",\"disable_link_preview\":false,\"email\":\"arief93\",\"extras\":{},\"id\":1148573049,\"id_str\":\"1148573049\",\"is_deleted\":false,\"is_public_channel\":false,\"message\":\"e\",\"payload\":{},\"room_avatar\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"room_id\":96304367,\"room_id_str\":\"96304367\",\"room_name\":\"arief93 testing34\",\"room_type\":\"single\",\"status\":\"read\",\"timestamp\":\"2022-11-15T07:46:08Z\",\"topic_id\":96304367,\"topic_id_str\":\"96304367\",\"type\":\"text\",\"unique_temp_id\":\"javascript-1668498368377\",\"unix_nano_timestamp\":1668498368522827000,\"unix_timestamp\":1668498368,\"user_avatar\":{\"avatar\":{\"url\":\"https://robohash.org/arief93/bgset_bg2/3.14160?set=set4\"}},\"user_avatar_url\":\"https://robohash.org/arief93/bgset_bg2/3.14160?set=set4\",\"user_extras\":{},\"user_id\":133493390,\"user_id_str\":\"133493390\",\"username\":\"arief93\"},{\"comment_before_id\":1148572897,\"comment_before_id_str\":\"1148572897\",\"disable_link_preview\":false,\"email\":\"arief93\",\"extras\":{},\"id\":1148573041,\"id_str\":\"1148573041\",\"is_deleted\":false,\"is_public_channel\":false,\"message\":\"d\",\"payload\":{},\"room_avatar\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"room_id\":96304367,\"room_id_str\":\"96304367\",\"room_name\":\"arief93 testing34\",\"room_type\":\"single\",\"status\":\"read\",\"timestamp\":\"2022-11-15T07:46:08Z\",\"topic_id\":96304367,\"topic_id_str\":\"96304367\",\"type\":\"text\",\"unique_temp_id\":\"javascript-1668498367983\",\"unix_nano_timestamp\":1668498368140856000,\"unix_timestamp\":1668498368,\"user_avatar\":{\"avatar\":{\"url\":\"https://robohash.org/arief93/bgset_bg2/3.14160?set=set4\"}},\"user_avatar_url\":\"https://robohash.org/arief93/bgset_bg2/3.14160?set=set4\",\"user_extras\":{},\"user_id\":133493390,\"user_id_str\":\"133493390\",\"username\":\"arief93\"},{\"comment_before_id\":1148569127,\"comment_before_id_str\":\"1148569127\",\"disable_link_preview\":false,\"email\":\"arief93\",\"extras\":{},\"id\":1148572897,\"id_str\":\"1148572897\",\"is_deleted\":false,\"is_public_channel\":false,\"message\":\"c\",\"payload\":{},\"room_avatar\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"room_id\":96304367,\"room_id_str\":\"96304367\",\"room_name\":\"arief93 testing34\",\"room_type\":\"single\",\"status\":\"read\",\"timestamp\":\"2022-11-15T07:46:00Z\",\"topic_id\":96304367,\"topic_id_str\":\"96304367\",\"type\":\"text\",\"unique_temp_id\":\"javascript-1668498360231\",\"unix_nano_timestamp\":1668498360360627000,\"unix_timestamp\":1668498360,\"user_avatar\":{\"avatar\":{\"url\":\"https://robohash.org/arief93/bgset_bg2/3.14160?set=set4\"}},\"user_avatar_url\":\"https://robohash.org/arief93/bgset_bg2/3.14160?set=set4\",\"user_extras\":{},\"user_id\":133493390,\"user_id_str\":\"133493390\",\"username\":\"arief93\"}],\"is_participant\":true,\"room\":{\"avatar_url\":\"https://robohash.org/arief93/bgset_bg2/3.14160?set=set4\",\"chat_type\":\"single\",\"id\":96304367,\"id_str\":\"96304367\",\"is_public_channel\":false,\"last_comment_id\":1172292157,\"last_comment_id_str\":\"1172292157\",\"last_comment_message\":\"Ok\",\"last_topic_id\":96304367,\"last_topic_id_str\":\"96304367\",\"options\":\"{}\",\"participants\":[{\"active\":true,\"avatar_url\":\"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\"email\":\"testing34\",\"extras\":{},\"id\":1449907638,\"id_str\":\"1449907638\",\"last_comment_read_id\":1172292157,\"last_comment_read_id_str\":\"1172292157\",\"last_comment_received_id\":1172292157,\"last_comment_received_id_str\":\"1172292157\",\"username\":\"testing34\"},{\"active\":true,\"avatar_url\":\"https://robohash.org/arief93/bgset_bg2/3.14160?set=set4\",\"email\":\"arief93\",\"extras\":{},\"id\":133493390,\"id_str\":\"133493390\",\"last_comment_read_id\":1148573190,\"last_comment_read_id_str\":\"1148573190\",\"last_comment_received_id\":1172292157,\"last_comment_received_id_str\":\"1172292157\",\"username\":\"arief93\"}],\"raw_room_name\":\"arief93 testing34\",\"room_name\":\"arief93\",\"room_total_participants\":2,\"unique_id\":\"0b1d6da69656afc49343a3e4658f338a\",\"unread_count\":0}},\"status\":200}";
    private final JsonObject jsonObject = new JsonParser().parse(jsonString).getAsJsonObject();
    private QiscusDataBaseHelper dataBaseHelper;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        super.setupEngine();
        MockitoAnnotations.openMocks(this);
//        dataBaseHelper = new QiscusDataBaseHelper();
    }

    @Test
    public void saveDBaddOrUpdateTest() {
        QiscusChatRoom qiscusChatRoom = QiscusApiParser.parseQiscusChatRoom(jsonObject);
        QiscusCore.getDataStore().add(qiscusChatRoom);
        QiscusCore.getDataStore().addOrUpdate(qiscusChatRoom);
    }

    @Test
    public void addOrUpdateTest() {
        QiscusChatRoom qiscusChatRoom = QiscusApiParser.parseQiscusChatRoom(jsonObject);
        qiscusChatRoom.setId(963043691010L);
        QiscusCore.getDataStore().addOrUpdate(qiscusChatRoom);
    }

    @Test
    public void getChatRoomTest() {
        QiscusChatRoom qiscusChatRoom = QiscusApiParser.parseQiscusChatRoom(jsonObject);
        QiscusCore.getDataStore().getChatRoom(qiscusChatRoom.getId());
    }

    @Test
    public void getChatRoomEmailTest() {
        QiscusChatRoom qiscusChatRoom = QiscusApiParser.parseQiscusChatRoom(jsonObject);
        QiscusCore.getDataStore().addOrUpdate(qiscusChatRoom);
        QiscusCore.getDataStore().getChatRoom(qiscusChatRoom.getLastComment().getSenderEmail());
    }

    @Test
    public void getChatRoomEmailDistinctIdTest() {
        QiscusChatRoom qiscusChatRoom = QiscusApiParser.parseQiscusChatRoom(jsonObject);
        QiscusCore.getDataStore().getChatRoom(
                qiscusChatRoom.getLastComment().getSenderEmail(), qiscusChatRoom.getDistinctId()
        );

        QiscusCore.getDataStore().getChatRoom(
                "email", "qiscusChatRoom.getDistinctId()"
        );
    }

    @Test
    public void getChatRoomEmailDistinctIdGroupTest() {
        QiscusChatRoom qiscusChatRoom = QiscusApiParser.parseQiscusChatRoom(jsonObject);
        qiscusChatRoom.setId(10234);
        qiscusChatRoom.setUniqueId("uniqueId_" + qiscusChatRoom.getId());
        qiscusChatRoom.setDistinctId("distinctId_" + qiscusChatRoom.getUniqueId());
        qiscusChatRoom.setGroup(true);

        QiscusCore.getDataStore().addOrUpdate(qiscusChatRoom);

        QiscusCore.getDataStore().getChatRoom(
                qiscusChatRoom.getLastComment().getSenderEmail(), qiscusChatRoom.getDistinctId()
        );
    }

    @Test
    public void getChatRoomEmailDistinctIdNotFoundTest() {
        QiscusCore.getDataStore().getChatRoom(
                "not_found", "not_found"
        );
    }

    @Test
    public void getChatRoomWithUniqueIdTest() {
        QiscusChatRoom qiscusChatRoom = QiscusApiParser.parseQiscusChatRoom(jsonObject);
        QiscusCore.getDataStore().getChatRoomWithUniqueId(
                qiscusChatRoom.getUniqueId()
        );

        QiscusCore.getDataStore().addOrUpdate(qiscusChatRoom);
        QiscusCore.getDataStore().getChatRoomWithUniqueId(
                qiscusChatRoom.getUniqueId()
        );
    }

    @Test
    public void getChatRoomWithUniqueIdTest2() {
        QiscusChatRoom qiscusChatRoom = QiscusApiParser.parseQiscusChatRoom(jsonObject);
        QiscusCore.getDataStore().addOrUpdate(qiscusChatRoom);

        QiscusCore.getDataStore().getChatRoomWithUniqueId(
                qiscusChatRoom.getUniqueId()
        );
    }

    @Test
    public void getChatRoomsTest() {
        QiscusCore.getDataStore().getChatRooms(10);
    }

    @Test
    public void getChatRoomsOffsetStrTest() {
        QiscusCore.getDataStore().getChatRooms(10, 1);
    }

    @Test
    public void getObservableChatRoomsOffsetTest() {
        QiscusCore.getDataStore().getObservableChatRooms(10, 1);
    }

    @Test
    public void getObservableChatRoomsTest() {
        QiscusCore.getDataStore().getObservableChatRooms(10);
    }

    @Test
    public void getChatRoomsListIdTest() {
        QiscusChatRoom qiscusChatRoom = QiscusApiParser.parseQiscusChatRoom(jsonObject);

        List<Long> roomIds = new ArrayList<>();
        roomIds.add(qiscusChatRoom.getId());
        List<String> uniqueIds = new ArrayList<>();
        uniqueIds.add(qiscusChatRoom.getUniqueId());

        QiscusCore.getDataStore().getChatRooms(roomIds, uniqueIds);
    }

    @Test
    public void addMemberFirstTest() {
        QiscusRoomMember roomMember = new QiscusRoomMember();
        roomMember.setLastReadCommentId(1000L);
        roomMember.setLastDeliveredCommentId(1000L);
        roomMember.setEmail("email@mail.com");
        roomMember.setAvatar("avatar");
        roomMember.setUsername("unameSample");
        roomMember.setExtras(new JSONObject());

        QiscusCore.getDataStore().add(roomMember);
        QiscusCore.getDataStore().isContains(roomMember);
        QiscusCore.getDataStore().update(roomMember);
    }

    @Test
    public void addMemberTest() {
        QiscusChatRoom qiscusChatRoom = QiscusApiParser.parseQiscusChatRoom(jsonObject);
        QiscusRoomMember roomMember = new QiscusRoomMember();
        roomMember.setLastReadCommentId(1000L);
        roomMember.setLastDeliveredCommentId(1000L);
        roomMember.setEmail("email@mail.com");
        roomMember.setAvatar("avatar");
        roomMember.setUsername("unameSample");
        roomMember.setExtras(new JSONObject());

        QiscusCore.getDataStore().addRoomMember(qiscusChatRoom.getId(), roomMember, qiscusChatRoom.getDistinctId());
    }

    @Test
    public void isContainsRoomMemberTest() {
        QiscusChatRoom qiscusChatRoom = QiscusApiParser.parseQiscusChatRoom(jsonObject);

        QiscusCore.getDataStore().isContainsRoomMember(qiscusChatRoom.getId(), qiscusChatRoom.getLastComment().getSenderEmail());
    }

    @Test
    public void updateRoomMemberTest() {
        QiscusChatRoom qiscusChatRoom = QiscusApiParser.parseQiscusChatRoom(jsonObject);

        QiscusCore.getDataStore().updateRoomMember(
                qiscusChatRoom.getId(), qiscusChatRoom.getMember().get(0), qiscusChatRoom.getLastComment().getSenderEmail()
        );
    }

    @Test
    public void deleteRomTest() {
        QiscusChatRoom qiscusChatRoom = QiscusApiParser.parseQiscusChatRoom(jsonObject);
        qiscusChatRoom.setId(1010101L);
        qiscusChatRoom.setUniqueId(qiscusChatRoom.getDistinctId() + 123445);

        QiscusCore.getDataStore().add(qiscusChatRoom);
        QiscusCore.getDataStore().deleteChatRoom(
                qiscusChatRoom.getId()
        );
    }

    @Test
    public void add() {
        QiscusComment qiscusComment = QiscusComment.generateMessage(12345, "test");
        qiscusComment.setTime(null);
        dataBaseHelper.add(qiscusComment);
    }

    @Test
    public void saveLocalPath() {
        dataBaseHelper
                .saveLocalPath(96304367,
                        1234474685, "/storage/emulated/0/Android/data/com.qiscus.dragonfly/files/Pictures/DragonFly/DragonFly Images/IMG-20230214-WA0000.jpg");

        dataBaseHelper.updateLocalPath(96304367, 1234474685, "/storage/emulated/0/Android/data/com.qiscus.dragonfly/files/Pictures/DragonFly/DragonFly Images/IMG-20230214-WA0000.jpg");

        dataBaseHelper.getLocalPath(1234474685);

        dataBaseHelper.addOrUpdateLocalPath(96304367, 1234474685, "/storage/emulated/0/Android/data/com.qiscus.dragonfly/files/Pictures/DragonFly/DragonFly Images/IMG-20230214-WA0000.jpg");
    }

    @Test
    public void checkIsContainsFileOfComment() {
        dataBaseHelper.isContainsFileOfComment(1234474685);
    }

    @Test
    public void update() {
        QiscusComment qiscusComment = QiscusComment.generateMessage(12345, "test");
        QiscusComment qiscusComment2 = QiscusComment.generateMessage(12345, "test2");
        qiscusComment.setTime(null);
        dataBaseHelper.update(qiscusComment);
        dataBaseHelper.update(qiscusComment2);
    }

    @Test
    public void updateRoomEmptyAndNullTest() {
        QiscusChatRoom qiscusChatRoom = QiscusApiParser.parseQiscusChatRoom(jsonObject);
        qiscusChatRoom.setMember(new ArrayList<>());
        qiscusChatRoom.getLastComment().setId(0);
        dataBaseHelper.update(qiscusChatRoom);

        qiscusChatRoom.setLastComment(null);
        qiscusChatRoom.setMember(null);
        dataBaseHelper.update(qiscusChatRoom);
    }

    @Test
    public void updateRoomErrorTest() {
        QiscusChatRoom qiscusChatRoom = mock(QiscusChatRoom.class);
        when(qiscusChatRoom.getId()).thenReturn(100L);
        when(qiscusChatRoom.getDistinctId()).thenThrow(new IllegalArgumentException("msg"));

        dataBaseHelper.update(qiscusChatRoom);
    }

    @Test
    public void delete() {
        QiscusComment qiscusComment = QiscusComment.generateMessage(12345, "test");
        dataBaseHelper.delete(qiscusComment);
    }

    @Test
    public void addOrUpdate() {
        QiscusComment qiscusComment = QiscusComment.generateMessage(123425, "test33");
        qiscusComment.setTime(null);
        dataBaseHelper.addOrUpdate(qiscusComment);
    }

    @Test
    public void deleteCommentsByRoomId() {
        QiscusComment qiscusComment = QiscusComment.generateMessage(12342245, "test33");
        qiscusComment.setTime(null);
        qiscusComment.setId(1234);

        QiscusComment qiscusComment2 = QiscusComment.generateMessage(12342245, "test333");
        qiscusComment2.setTime(null);
        qiscusComment2.setId(1235);

        dataBaseHelper.addOrUpdate(qiscusComment);
        dataBaseHelper.addOrUpdate(qiscusComment2);

        dataBaseHelper.deleteCommentsByRoomId(-1L);
        dataBaseHelper.deleteCommentsByRoomId(
                qiscusComment.getRoomId()
        );
    }

    @Test
    public void deleteCommentsByRoomId2() {
        QiscusComment qiscusComment = QiscusComment.generateMessage(123422225, "test33");
        qiscusComment.setTime(null);
        dataBaseHelper.addOrUpdate(qiscusComment);
        dataBaseHelper.deleteCommentsByRoomId(123422225, 0);
    }

    @Test
    public void getComment() {
        QiscusComment qiscusComment = QiscusComment.generateMessage(123422225, "test33");
        qiscusComment.setTime(null);
        qiscusComment.setId(12345555);
        dataBaseHelper.addOrUpdate(qiscusComment);

        dataBaseHelper.getComment(12345555);
    }

    @Test
    public void getComment2() {
        QiscusComment qiscusComment = QiscusComment.generateMessage(123422225, "test33");
        qiscusComment.setTime(null);
        qiscusComment.setId(12345555);
        qiscusComment.setSenderEmail("ariefxx");
        qiscusComment.setSender("ariefxx");

        QiscusRoomMember qiscusRoomMember = new QiscusRoomMember();
        qiscusRoomMember.setEmail("ariefxx");
        qiscusRoomMember.setUsername("ariefxx");
        qiscusRoomMember.setAvatar("https://");

        dataBaseHelper.add(qiscusRoomMember);
        dataBaseHelper.addOrUpdate(qiscusComment);

        dataBaseHelper.getComment(12345555);
    }

    @Test
    public void getCommentByBeforeId() {
        QiscusComment qiscusComment = QiscusComment.generateMessage(123422225, "test343");
        qiscusComment.setTime(null);
        qiscusComment.setId(12345555);

        QiscusComment qiscusComment2 = QiscusComment.generateMessage(123422225, "test3423");
        qiscusComment2.setTime(null);
        qiscusComment2.setId(12345556);
        qiscusComment2.setCommentBeforeId(qiscusComment.getId());

        QiscusComment qiscusComment3 = QiscusComment.generateMessage(123422225, "test3423");
        qiscusComment3.setTime(null);
        qiscusComment3.setId(12345557);
        qiscusComment3.setCommentBeforeId(qiscusComment2.getId());

        dataBaseHelper.add(qiscusComment);
        dataBaseHelper.add(qiscusComment2);
        dataBaseHelper.add(qiscusComment3);

        dataBaseHelper.getCommentByBeforeId(
                qiscusComment.getCommentBeforeId()
        );
        dataBaseHelper.getCommentByBeforeId(
                qiscusComment3.getCommentBeforeId()
        );
    }

    @Test
    public void getComments() {
        QiscusComment qiscusComment = QiscusComment.generateMessage(123422225, "test343");
        qiscusComment.setTime(null);
        qiscusComment.setId(12345555);
        qiscusComment.setSenderEmail("ariefxx");
        qiscusComment.setSender("ariefxx");
        qiscusComment.setTime(new Date());

        QiscusComment qiscusComment2 = QiscusComment.generateMessage(123422225, "test3423");
        qiscusComment2.setTime(null);
        qiscusComment2.setId(12345556);
        qiscusComment2.setSenderEmail("ariefxx");
        qiscusComment2.setSender("ariefxx");


        QiscusRoomMember qiscusRoomMember = new QiscusRoomMember();
        qiscusRoomMember.setEmail("ariefxx");
        qiscusRoomMember.setUsername("ariefxx");
        qiscusRoomMember.setAvatar("https://");

        dataBaseHelper.add(qiscusRoomMember);

        dataBaseHelper.addOrUpdate(qiscusComment);
        dataBaseHelper.addOrUpdate(qiscusComment2);

        dataBaseHelper.getComments(123422225);
        dataBaseHelper.getComments(123422225, 3);
        dataBaseHelper.getComments(qiscusComment.getRoomId(), qiscusComment.getTime().getTime());

    }

    @Test
    public void deleteCommentsByRoomId3() {
        QiscusComment qiscusComment = QiscusComment.generateMessage(123422225, "test343");
        qiscusComment.setTime(null);
        qiscusComment.setId(12345555);

        QiscusComment qiscusComment2 = QiscusComment.generateMessage(123422225, "test3423");
        qiscusComment2.setTime(null);
        qiscusComment2.setId(12345556);
        dataBaseHelper.addOrUpdate(qiscusComment);
        dataBaseHelper.addOrUpdate(qiscusComment2);

        dataBaseHelper.deleteCommentsByRoomId(123422225, 0);

    }

    @Test
    public void getObservableComments() {

        QiscusComment qiscusComment = QiscusComment.generateMessage(1234222252, "test343");
        qiscusComment.setTime(null);
        qiscusComment.setId(12345545);

        QiscusComment qiscusComment2 = QiscusComment.generateMessage(1234222252, "test3423");
        qiscusComment2.setTime(null);
        qiscusComment2.setId(12345546);

        QiscusComment qiscusComment3 = QiscusComment.generateMessage(1234222252, "test3423");
        qiscusComment3.setTime(null);
        qiscusComment3.setId(12345547);

        dataBaseHelper.add(qiscusComment);
        dataBaseHelper.add(qiscusComment2);
        dataBaseHelper.add(qiscusComment3);

        Func2<QiscusComment, QiscusComment, Integer> commentComparator = (lhs, rhs) -> rhs.getTime().compareTo(lhs.getTime());

        QiscusCore.getDataStore().getObservableComments(1234222252, 2)
                .flatMap(Observable::from)
                .toSortedList(commentComparator)
                .map(comments -> {
                    if (comments.size() > 2) {
                        return comments.subList(0, 2);
                    }
                    return comments;
                }).doOnNext(qiscusComments -> {

                })
                .subscribeOn(Schedulers.io());

        QiscusCore.getDataStore().getObservableComments(1234222252)
                .flatMap(Observable::from)
                .toSortedList(commentComparator)
                .map(comments -> {
                    if (comments.size() > 2) {
                        return comments.subList(0, 2);
                    }
                    return comments;
                })
                .doOnNext(qiscusComments -> {

                })
                .subscribeOn(Schedulers.io());

        List<QiscusComment> list = dataBaseHelper.getOlderCommentsThan(qiscusComment2, 1234222252, 2);

        List<QiscusComment> list2 = dataBaseHelper.getCommentsAfter(qiscusComment2, 1234222252);

        QiscusComment qiscusComment4 = QiscusComment.generateMessage(1234222252, "test3433");
        qiscusComment4.setTime(null);
        qiscusComment4.setId(12345548);


        QiscusCore.getDataStore().getObservableOlderCommentsThan(qiscusComment4, 1234222252, 2)
                .flatMap(Observable::from)
                .filter(qiscusComment1 -> qiscusComment.getId() == -1 || qiscusComment1.getId() < qiscusComment.getId())
                .toSortedList(commentComparator)
                .map(comments -> {
                    if (comments.size() >= 20) {
                        return comments.subList(0, 20);
                    }
                    return comments;
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(comments -> {

                }, throwable -> {
                    QiscusErrorLogger.print(throwable);

                });

        QiscusCore.getDataStore().getObservableOlderCommentsThan(qiscusComment2, 1234222252, 2)
                .flatMap(Observable::from)
                .filter(qiscusComment1 -> qiscusComment.getId() == -1 || qiscusComment1.getId() < qiscusComment.getId())
                .toSortedList(commentComparator)
                .map(comments -> {
                    if (comments.size() >= 20) {
                        return comments.subList(0, 20);
                    }
                    return comments;
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(comments -> {

                }, throwable -> {
                    QiscusErrorLogger.print(throwable);

                });


        dataBaseHelper.deleteCommentsByRoomId(1234222252, 0L);
    }

    @Test
    public void getLatestStateComment() {
        QiscusComment qiscusComment = QiscusComment.generateMessage(1234222253, "test343");
        qiscusComment.setTime(null);
        qiscusComment.setState(3);
        qiscusComment.setId(12345535);

        QiscusComment qiscusComment2 = QiscusComment.generateMessage(1234222253, "test3423");
        qiscusComment2.setTime(null);
        qiscusComment2.setId(12345536);

        QiscusComment qiscusComment3 = QiscusComment.generateMessage(1234222253, "test3423");
        qiscusComment3.setTime(null);
        qiscusComment3.setState(4);
        qiscusComment3.setId(12345537);

        dataBaseHelper.add(qiscusComment);
        dataBaseHelper.add(qiscusComment2);
        dataBaseHelper.add(qiscusComment3);


        dataBaseHelper.getLatestDeliveredComment(1234222253);
        dataBaseHelper.getLatestReadComment(1234222253);
    }


    @Test
    public void searchComments() {
        QiscusComment qiscusComment = QiscusComment.generateMessage(1234222253, "test343");
        qiscusComment.setTime(null);
        qiscusComment.setState(3);
        qiscusComment.setId(12345535);
        qiscusComment.setSender("ariefxx");
        qiscusComment.setSenderEmail("ariefxx");

        dataBaseHelper.add(qiscusComment);


        QiscusRoomMember qiscusRoomMember = new QiscusRoomMember();
        qiscusRoomMember.setEmail("ariefxx");
        qiscusRoomMember.setUsername("ariefxx");
        qiscusRoomMember.setAvatar("https://");

        dataBaseHelper.add(qiscusRoomMember);

        dataBaseHelper.searchComments("test343", 1234222253, 1, 0);
        dataBaseHelper.searchComments("test343", 1, 0);
    }

    @Test
    public void sortRoomsTest() {
        try {
            QiscusChatRoom qiscusChatRoom = QiscusApiParser.parseQiscusChatRoom(jsonObject);
            QiscusChatRoom qiscusChatRoom2 = QiscusApiParser.parseQiscusChatRoom(jsonObject);
            qiscusChatRoom2.setId(1000000078L);

            List<QiscusChatRoom> chatRooms = new ArrayList<>();
            chatRooms.add(qiscusChatRoom);
            chatRooms.add(qiscusChatRoom2);

            extractMethode(dataBaseHelper, "sortRooms", List.class)
                    .invoke(dataBaseHelper, chatRooms);
        } catch (IllegalAccessException | InvocationTargetException e) {
            // ignored
        }
    }

    @Test
    public void sortRoomsFirstNullTest() {
        try {
            QiscusChatRoom qiscusChatRoom = QiscusApiParser.parseQiscusChatRoom(jsonObject);
            qiscusChatRoom.setLastComment(null);
            QiscusChatRoom qiscusChatRoom2 = QiscusApiParser.parseQiscusChatRoom(jsonObject);
            qiscusChatRoom2.setId(1000000078L);

            List<QiscusChatRoom> chatRooms = new ArrayList<>();
            chatRooms.add(qiscusChatRoom);
            chatRooms.add(qiscusChatRoom2);

            extractMethode(dataBaseHelper, "sortRooms", List.class)
                    .invoke(dataBaseHelper, chatRooms);
        } catch (IllegalAccessException | InvocationTargetException e) {
            // ignored
        }
    }

    @Test
    public void sortRoomsSecondNullTest() {
        try {
            QiscusChatRoom qiscusChatRoom = QiscusApiParser.parseQiscusChatRoom(jsonObject);
            QiscusChatRoom qiscusChatRoom2 = QiscusApiParser.parseQiscusChatRoom(jsonObject);
            qiscusChatRoom2.setId(1000000078L);
            qiscusChatRoom2.setLastComment(null);

            List<QiscusChatRoom> chatRooms = new ArrayList<>();
            chatRooms.add(qiscusChatRoom);
            chatRooms.add(qiscusChatRoom2);

            extractMethode(dataBaseHelper, "sortRooms", List.class)
                    .invoke(dataBaseHelper, chatRooms);
        } catch (IllegalAccessException | InvocationTargetException e) {
            // ignored
        }
    }
    @Test
    public void sortRoomsAllNullTest() {
        try {
            QiscusChatRoom qiscusChatRoom = QiscusApiParser.parseQiscusChatRoom(jsonObject);
            qiscusChatRoom.setLastComment(null);
            QiscusChatRoom qiscusChatRoom2 = QiscusApiParser.parseQiscusChatRoom(jsonObject);
            qiscusChatRoom2.setId(1000000078L);
            qiscusChatRoom2.setLastComment(null);

            List<QiscusChatRoom> chatRooms = new ArrayList<>();
            chatRooms.add(qiscusChatRoom);
            chatRooms.add(qiscusChatRoom2);

            extractMethode(dataBaseHelper, "sortRooms", List.class)
                    .invoke(dataBaseHelper, chatRooms);
        } catch (IllegalAccessException | InvocationTargetException e) {
            // ignored
        }
    }

}