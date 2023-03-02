package com.qiscus.sdk.chat.core.data.model;

import static org.junit.Assert.*;

import android.webkit.MimeTypeMap;

import com.qiscus.sdk.chat.core.InstrumentationBaseTest;
import com.qiscus.sdk.chat.core.QiscusCore;
import com.qiscus.sdk.chat.core.data.remote.QiscusApi;
import com.qiscus.sdk.chat.core.service.QiscusSyncService;
import com.qiscus.sdk.chat.core.util.QiscusAndroidUtil;
import com.qiscus.sdk.chat.core.util.QiscusErrorLogger;
import com.qiscus.sdk.chat.core.util.QiscusFileUtil;
import com.qiscus.sdk.chat.core.util.QiscusTextUtil;
import com.schinizer.rxunfurl.model.PreviewData;

import junit.framework.TestCase;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class QiscusCommentTest extends InstrumentationBaseTest {
    QiscusComment qiscusComment = null;
    Integer roomId = 10185397;
    String roomUniqId = "8d412fdd3411f5f261f8f30e0f90ff60";
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

        qiscusComment = new QiscusComment();

        qiscusComment.getRoomName();
        qiscusComment.getRoomAvatar();
        qiscusComment.isGroupMessage();
        qiscusComment.isSelected();
        qiscusComment.isHighlighted();
        qiscusComment.isDownloading();
        qiscusComment.getProgress();

        qiscusComment.setHighlighted(true);
        qiscusComment.setSelected(true);
    }

    @Test
    public void generateReplyMessage() {
        QiscusComment qiscusCommentMessage = QiscusComment.generateMessage(roomId,"oke");
        qiscusComment.generateReplyMessage(roomId,"test",qiscusCommentMessage);
    }

    @Test
    public void generateContactMessage() {
        qiscusComment.generateContactMessage(roomId, new QiscusContact("arief", "0938732423", "phone"));
    }

    @Test
    public void generateContactMessage2() {
        QiscusComment qiscusComment = new QiscusComment();
        qiscusComment.generateContactMessage(roomId, new QiscusContact("arief", "0938732423", "phone"));
        qiscusComment.setRawType("contact_person");
        qiscusComment.setContact( new QiscusContact("arief", "0938732423", "phone"));
        qiscusComment.getContact();
    }

    @Test
    public void generateLocationMessage() {
        QiscusLocation location = new QiscusLocation();
        location.setName("name");
        location.setAddress("address");
        location.setLatitude(12345);
        location.setLongitude(67890);

        qiscusComment.generateLocationMessage(roomId,location);

        location.describeContents();
        location.getThumbnailUrl();
        location.generateThumbnail();
        location.setThumbnailUrl("https://");

        location.toString();
    }

    @Test
    public void generateLocationMessage2() {
        QiscusLocation location = new QiscusLocation();
        location.setName("name");
        location.setAddress("address");
        location.setLatitude(12345);
        location.setLongitude(67890);

        QiscusComment qiscusComment2 = new QiscusComment();
        qiscusComment2.generateLocationMessage(roomId,location).getLocation();
        qiscusComment2.setLocation(location);
        qiscusComment2.getLocation();
    }

    @Test
    public void generatePostBackMessage() {
        qiscusComment.generatePostBackMessage(roomId, "content", "{}");
    }

    @Test
    public void generateCustomMessage() {
        qiscusComment.generateCustomMessage(roomId, "text", "typeNew", new JSONObject());
    }

    @Test
    public void getReplyTo() {
       // qiscusComment.getReplyTo();
    }

    @Test
    public void updateAttachmentUrl() {
        qiscusComment.updateAttachmentUrl("https://www.simplilearn.com/ice9/free_resources_article_thumb/what_is_image_Processing.jpg");
    }

    @Test
    public void setProgressListener() {
        qiscusComment.setProgressListener(new QiscusComment.ProgressListener() {
            @Override
            public void onProgress(QiscusComment qiscusComment, int percentage) {

            }
        });
    }

    @Test
    public void setDownloadingListener() {
        qiscusComment.setDownloadingListener(new QiscusComment.DownloadingListener() {
            @Override
            public void onDownloading(QiscusComment qiscusComment, boolean downloading) {

            }
        });
    }

    @Test
    public void setPlayingAudioListener() {
        qiscusComment.setPlayingAudioListener(new QiscusComment.PlayingAudioListener() {
            @Override
            public void onPlayingAudio(QiscusComment qiscusComment, int currentPosition) {

            }

            @Override
            public void onPauseAudio(QiscusComment qiscusComment) {

            }

            @Override
            public void onStopAudio(QiscusComment qiscusComment) {

            }
        });
    }

    @Test
    public void setLinkPreviewListener() {
        qiscusComment.setLinkPreviewListener(new QiscusComment.LinkPreviewListener() {
            @Override
            public void onLinkPreviewReady(QiscusComment qiscusComment, PreviewData previewData) {

            }
        });
    }

    @Test
    public void destroy() {

    }

    @Test
    public void getRoomAvatar(){
        qiscusComment = new QiscusComment();

        qiscusComment.setGroupMessage(true);
        qiscusComment.setRoomAvatar("https://");

        QiscusComment qiscusComment2 = new QiscusComment();

        qiscusComment2.setGroupMessage(false);
        qiscusComment2.setSenderAvatar("https://");
        qiscusComment2.setRoomAvatar("https://");

        qiscusComment.getRoomAvatar();
    }

    @Test
    public void isAttachment(){

        QiscusComment qiscusComment2 = new QiscusComment();
        qiscusComment2.generateFileAttachmentMessage(roomId, "https://fujifilm-x.com/wp-content/uploads/2021/01/gfx100s_sample_01_thum.jpg","caption", "name");
        qiscusComment2.setMessage(" [file] https://fujifilm-x.com/wp-content/uploads/2021/01/gfx100s_sample_01_thum.jpg [/file]");


        qiscusComment2.getAttachmentUri();
        qiscusComment2.getCaption();
        qiscusComment2.getAttachmentName();

        qiscusComment2.getUrls();
        qiscusComment2.isAttachment();

    }

    @Test
    public void loadLinkPreviewData(){
        QiscusComment qiscusComment = new QiscusComment();
        qiscusComment.setMessage("https://fujifilm-x.com/wp-content/uploads/2021/01/gfx100s_sample_01_thum.jpg");
        qiscusComment.setRawType("link");
        qiscusComment.loadLinkPreviewData();
    }

    @Test
    public void hashCodee(){

        QiscusComment qiscusComment = new QiscusComment();
        qiscusComment.generateFileAttachmentMessage(roomId,"https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3","","audio.mp3");
        qiscusComment.setMessage("[file] https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3 [/file]");
        qiscusComment.setUniqueId("123453212123123213");

        qiscusComment.hashCode();
        qiscusComment.describeContents();

    }

}