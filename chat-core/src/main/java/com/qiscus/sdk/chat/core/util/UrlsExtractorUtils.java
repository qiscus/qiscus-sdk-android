package com.qiscus.sdk.chat.core.util;

import android.util.Log;

import com.qiscus.sdk.chat.core.data.model.urlsextractor.ImageInfo;
import com.qiscus.sdk.chat.core.data.model.urlsextractor.PreviewData;

import org.jsoup.Jsoup;
import org.jsoup.internal.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import rx.Observable;
import rx.Scheduler;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class UrlsExtractorUtils {
    private final OkHttpClient client;
    private final Scheduler scheduler;
    private final ImageDecoder decoder;

    private UrlsExtractorUtils(OkHttpClient client, Scheduler scheduler)
    {
        this.client = client;
        this.scheduler = scheduler;
        this.decoder = new ImageDecoder();
    }

    public Observable<PreviewData> generatePreview(String url) {
        return Observable.just(url)
                .flatMap((Func1<String, Observable<AbstractMap.SimpleEntry<PreviewData, List<String>>>>) this::extractData)
                .flatMap((Func1<AbstractMap.SimpleEntry<PreviewData, List<String>>, Observable<PreviewData>>) pair -> {
                    print("generatePreview Run");

                    Observable<PreviewData> meta = Observable.just(pair.getKey());
                    Observable<List<ImageInfo>> imgInfo = processImageDimension(pair.getValue());

                    return Observable.zip(meta, imgInfo, (previewData, images) -> {
                        print("onZip");
                        previewData.setImages(images);
                        return previewData;
                    });
                })
                .observeOn(scheduler);
    }

    private Observable<AbstractMap.SimpleEntry<PreviewData, List<String>>> extractData(String url) {
        print("extractData Urls :" + url);
        Request request = new Request.Builder().url(url).build();
        PreviewData previewData = new PreviewData();
        List<String> extractedURLs = new ArrayList<>();

        try {
            Response response = client.newCall(request).execute();
            print("extractData execute");

            // Check content type
            switch (response.body().contentType().type()) {
                // If its an image, just extract it
                case "image":
                    print("extractData onImage");
                    extractedURLs.add(url);
                    previewData.setUrl(url);
                    break;
                case "text":
                    switch (response.body().contentType().subtype()) {
                        // If its html, extract the meta tags
                        case "html":
                            print("extractData html");
                            Document document = Jsoup.parse(response.body().string(), url);

                            // Extract Open Graph data
                            // If no properties found, infer from existing information
                            for (Element property : document.select("meta[property^=og:]")) {
                                switch (property.attr("property")) {
                                    case "og:title":
                                        previewData.setTitle(property.attr("content"));
                                        break;
                                    case "og:url":
                                        previewData.setUrl(property.attr("abs:content"));
                                        break;
                                    case "og:description":
                                        previewData.setDescription(property.attr("content"));
                                        break;
                                    case "og:image":
                                        extractedURLs.add(property.attr("abs:content"));
                                        break;
                                }
                            }

                            // Fallback to <title>
                            if (StringUtil.isBlank(previewData.getTitle())) {
                                previewData.setTitle(document.title());
                            }
                            // Fallback to param listed url
                            if (StringUtil.isBlank(previewData.getUrl())) {
                                previewData.setUrl(url);
                            }
                            // Fallback to meta description
                            if (StringUtil.isBlank(previewData.getDescription())) {
                                for (Element property : document.select("meta[name=description]")) {
                                    String content = property.attr("content");

                                    if(!StringUtil.isBlank(content)) {
                                        previewData.setDescription(content);
                                        break;
                                    }
                                }
                            }
                            // Fallback to first <p> with text
                            if (StringUtil.isBlank(previewData.getDescription())) {
                                for (Element p : document.select("p")) {
                                    if (!p.text().equals("")) {
                                        previewData.setDescription(p.text());
                                        break;
                                    }
                                }
                            }

                            // Fallback to other media
                            if (extractedURLs.size() == 0) {
                                Elements media = document.select("[src]");
                                for (Element src : media) {
                                    if (src.tagName().equals("img")) {
                                        extractedURLs.add(src.attr("abs:src"));
                                    }
                                }
                            }

                            break;

                        default:
                            print("extractData text empty");
                            return Observable.empty();
                    }

                    break;

                default:
                    print("extractData empty");
                    return Observable.empty();
            }

            return Observable.just(new AbstractMap.SimpleEntry<>(previewData, extractedURLs));
        } catch (IOException e) {
            print("extractData error: " + e.getMessage());
            return Observable.error(e);
        }
    }

    private Observable<List<ImageInfo>> processImageDimension(List<String> urls) {
        print("processImageDimension");
        return Observable.from(urls)
                // Only query distinct urls
                .distinct()
                // Parse image only for their size
                .concatMapEager((Func1<String, Observable<ImageInfo>>) this::extractImageDimension)
                // Sort the results according to resolution
                .toSortedList((lhs, rhs) -> {
                    print("processImageDimension onSorted");
                    Integer lhsRes = lhs.getDimension().getWidth() * lhs.getDimension().getHeight();
                    Integer rhsRes = rhs.getDimension().getWidth() * rhs.getDimension().getHeight();

                    return rhsRes.compareTo(lhsRes);
                });
    }

    private Observable<ImageInfo> extractImageDimension(String url) {
        print("extractImageDimension");
        return Observable.just(url)
                .flatMap((Func1<String, Observable<ImageInfo>>) url1 -> {
                    Response response = null;
                    Request request = new Request.Builder()
                            .url(url1)
                            .build();
                    try {
                        response = client.newCall(request).execute();
                        switch (response.body().contentType().toString()) {
                            case "image/jpeg":
                                return Observable.just(new ImageInfo(url1, decoder.decodeJpegDimension(response.body().byteStream())));
                            case "image/png":
                                return Observable.just(new ImageInfo(url1, decoder.decodePngDimension(response.body().byteStream())));
                            case "image/bmp":
                                return Observable.just(new ImageInfo(url1, decoder.decodeBmpDimension(response.body().byteStream())));
                            case "image/gif":
                                return Observable.just(new ImageInfo(url1, decoder.decodeGifDimension(response.body().byteStream())));
                            default:
                                return Observable.empty();
                        }
                    }
                    catch (IOException e) {
                        return Observable.empty();
                    }
                    finally {
                        if (response != null) response.body().close();
                    }
                });
    }

    private void print(String message) {
        Log.d("ExtractorTest", message);
    }

    public static final class Builder {
        private OkHttpClient client;
        private Scheduler scheduler;

        public UrlsExtractorUtils.Builder client(OkHttpClient client)
        {
            this.client = client;
            return this;
        }

        public UrlsExtractorUtils.Builder scheduler(Scheduler scheduler)
        {
            this.scheduler = scheduler;
            return this;
        }

        public UrlsExtractorUtils build()
        {
            if(client == null)
            {
                client = new OkHttpClient();
            }

            if(scheduler == null)
            {
                scheduler = Schedulers.immediate();
            }

            return new UrlsExtractorUtils(client, scheduler);
        }
    }
}