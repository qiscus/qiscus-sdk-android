package com.qiscus.sdk.ui.adapter.viewholder;

import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.github.lzyzsd.circleprogress.CircleProgress;
import com.qiscus.sdk.Qiscus;
import com.qiscus.library.chat.R;
import com.qiscus.library.chat.R2;
import com.qiscus.sdk.data.local.DataBaseHelper;
import com.qiscus.sdk.data.model.QiscusComment;
import com.qiscus.sdk.ui.adapter.BaseRecyclerAdapter.OnItemClickListener;
import com.qiscus.sdk.ui.adapter.BaseRecyclerAdapter.OnLongItemClickListener;
import com.qiscus.sdk.util.DateUtil;

import java.io.File;

import butterknife.BindView;
import butterknife.OnLongClick;

/**
 * Created on : May 30, 2016
 * Author     : zetbaitsu
 * Name       : Zetra
 * Email      : zetra@mail.ugm.ac.id
 * GitHub     : https://github.com/zetbaitsu
 * LinkedIn   : https://id.linkedin.com/in/zetbaitsu
 */
public class MessageViewHolder extends BaseItemViewHolder<QiscusComment> implements
        QiscusComment.ProgressListener, QiscusComment.DownloadingListener {

    @Nullable @BindView(R2.id.date) TextView date;
    @Nullable @BindView(R2.id.time) TextView time;
    @Nullable @BindView(R2.id.icon_read) ImageView iconRead;
    @Nullable @BindView(R2.id.contents) TextView message;
    @Nullable @BindView(R2.id.thumbnail) ImageView thumbnail;
    @Nullable @BindView(R2.id.file_name) TextView fileName;
    @Nullable @BindView(R2.id.file_type) TextView fileType;
    @Nullable @BindView(R2.id.holder) RelativeLayout holder;
    @Nullable @BindView(R2.id.progress) CircleProgress progress;
    @Nullable @BindView(R2.id.iv_download) ImageView downloadIcon;
    @BindView(R2.id.bubble) ImageView bubble;
    @BindView(R2.id.message) View messageBubble;

    private boolean showDate;
    private boolean fromMe;
    private boolean showBubble;

    public MessageViewHolder(View itemView, OnItemClickListener itemClickListener, OnLongItemClickListener longItemClickListener) {
        super(itemView, itemClickListener, longItemClickListener);
        messageBubble.setOnClickListener(this);
    }

    public void setShowDate(boolean showDate) {
        this.showDate = showDate;
    }

    public boolean isShowDate() {
        return showDate;
    }

    public void setFromMe(boolean fromMe) {
        this.fromMe = fromMe;
    }

    public void setShowBubble(boolean showBubble) {
        this.showBubble = showBubble;
    }

    @Override
    public void bind(QiscusComment qiscusComment) {
        bubble.setVisibility(showBubble ? View.VISIBLE : View.GONE);
        qiscusComment.setProgressListener(this);
        qiscusComment.setDownloadingListener(this);
        showProgressOrNot(qiscusComment);
        showDateOrNot(qiscusComment);
        showTime(qiscusComment);
        showIconReadOrNot(qiscusComment);
        setUpDownloadIcon(qiscusComment);

        switch (qiscusComment.getType()) {
            case TEXT:
                showTextMessage(qiscusComment);
                break;
            case IMAGE:
                showThumbnail(qiscusComment);
                break;
            case FILE:
                showExtension(qiscusComment);
                showFileMessage(qiscusComment);
                break;
        }
    }

    private void setUpDownloadIcon(QiscusComment qiscusComment) {
        if (downloadIcon != null) {
            if (qiscusComment.isImage()) {
                if (qiscusComment.getState() == QiscusComment.STATE_FAILED || qiscusComment.getState() == QiscusComment.STATE_SENDING) {
                    downloadIcon.setImageResource(R.drawable.ic_upload_big);
                } else {
                    downloadIcon.setImageResource(R.drawable.ic_download_big);
                }
            } else {
                if (qiscusComment.getState() == QiscusComment.STATE_FAILED || qiscusComment.getState() == QiscusComment.STATE_SENDING) {
                    downloadIcon.setImageResource(R.drawable.ic_upload);
                } else {
                    downloadIcon.setImageResource(R.drawable.ic_download);
                }
            }
        }
    }

    private void showProgressOrNot(QiscusComment qiscusComment) {
        if (progress != null) {
            progress.setProgress(qiscusComment.getProgress());
            progress.setVisibility(qiscusComment.isDownloading() ? View.VISIBLE : View.GONE);
        }
    }

    private void showTime(QiscusComment qiscusComment) {
        if (time != null) {
            if (qiscusComment.getState() == QiscusComment.STATE_FAILED) {
                time.setText(R.string.sending_failed);
                time.setTextColor(ContextCompat.getColor(Qiscus.getApps(), R.color.red));
            } else {
                time.setText(DateUtil.toHour(qiscusComment.getTime()));
                time.setTextColor(ContextCompat.getColor(Qiscus.getApps(),
                                                         fromMe ? R.color.secondary_text : R.color.primary_light));
            }
        }
    }

    private void showFileMessage(QiscusComment qiscusComment) {
        File localPath = DataBaseHelper.getInstance().getLocalPath(qiscusComment.getId());
        if (downloadIcon != null) {
            downloadIcon.setVisibility(localPath == null ? View.VISIBLE : View.GONE);
        }
        if (fileName != null) {
            fileName.setText(qiscusComment.getAttachmentName());
        }
    }

    private void showExtension(final QiscusComment qiscusComment) {
        if (fileType != null) {
            if (qiscusComment.getExtension().isEmpty()) {
                fileType.setText(R.string.unkown_type);
            } else {
                fileType.setText(String.format("%s File", qiscusComment.getExtension().toUpperCase()));
            }
        }
    }

    private void showThumbnail(final QiscusComment qiscusComment) {
        if (thumbnail != null) {
            if (fromMe) {
                showMyImage(qiscusComment);
            } else {
                showOthersImage(qiscusComment);
            }
        }
        if (fileName != null) {
            fileName.setText(qiscusComment.getAttachmentName());
        }
    }

    private void showOthersImage(final QiscusComment qiscusComment) {
        File localPath = DataBaseHelper.getInstance().getLocalPath(qiscusComment.getId());
        if (localPath == null) {
            if (holder != null) {
                holder.setVisibility(View.VISIBLE);
            }
            if (thumbnail != null) {
                thumbnail.setVisibility(View.GONE);
            }
        } else {
            if (holder != null) {
                holder.setVisibility(View.INVISIBLE);
            }
            if (thumbnail != null) {
                thumbnail.setVisibility(View.VISIBLE);
            }
            showImage(localPath);
        }
    }

    private void showMyImage(final QiscusComment qiscusComment) {
        if (qiscusComment.getState() == QiscusComment.STATE_SENDING) {
            if (holder != null) {
                holder.setVisibility(View.INVISIBLE);
            }
            if (thumbnail != null) {
                thumbnail.setVisibility(View.VISIBLE);
                Glide.with(thumbnail.getContext())
                        .load(new File(qiscusComment.getAttachmentUri().toString()))
                        .error(R.drawable.ic_img)
                        .into(thumbnail);
            }
        } else {
            File localPath = DataBaseHelper.getInstance().getLocalPath(qiscusComment.getId());
            if (localPath == null) {
                if (holder != null) {
                    holder.setVisibility(View.VISIBLE);
                }
                if (thumbnail != null) {
                    thumbnail.setVisibility(View.GONE);
                }
            } else {
                if (holder != null) {
                    holder.setVisibility(View.INVISIBLE);
                }
                if (thumbnail != null) {
                    thumbnail.setVisibility(View.VISIBLE);
                }
                showImage(localPath);
            }
        }
    }

    private void showImage(File file) {
        if (thumbnail != null) {
            Glide.with(thumbnail.getContext())
                    .load(file)
                    .error(R.drawable.ic_img)
                    .listener(new RequestListener<File, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, File model, Target<GlideDrawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, File model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            if (holder != null) {
                                holder.setVisibility(View.INVISIBLE);
                            }
                            thumbnail.setVisibility(View.VISIBLE);
                            return false;
                        }
                    })
                    .into(thumbnail);
        }
    }

    private void showTextMessage(QiscusComment qiscusComment) {
        if (message != null) {
            message.setText(qiscusComment.getMessage());
        }
    }

    private void showIconReadOrNot(QiscusComment qiscusComment) {
        if (iconRead != null) {
            switch (qiscusComment.getState()) {
                case QiscusComment.STATE_SENDING:
                    iconRead.setImageResource(R.drawable.ic_info_time);
                    break;
                case QiscusComment.STATE_ON_QISCUS:
                    iconRead.setImageResource(R.drawable.ic_sending);
                    break;
                case QiscusComment.STATE_ON_PUSHER:
                    iconRead.setImageResource(R.drawable.ic_read);
                    break;
                case QiscusComment.STATE_FAILED:
                    iconRead.setImageResource(R.drawable.ic_sending_failed);
                    break;
            }
        }
    }

    private void showDateOrNot(QiscusComment qiscusComment) {
        if (date != null) {
            if (showDate) {
                date.setText(DateUtil.toTodayOrDate(qiscusComment.getTime()));
                date.setVisibility(View.VISIBLE);
            } else {
                date.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v.equals(messageBubble)) {
            super.onClick(v);
        }
    }

    @Override
    public boolean onLongClick(View v) {
        return false;
    }

    @OnLongClick(R2.id.message)
    public boolean onMessageLongClick() {
        return super.onLongClick(messageBubble);
    }

    @Override
    public void onProgress(QiscusComment qiscusComment, int percentage) {
        if (progress != null) {
            progress.setProgress(percentage);
        }
    }

    @Override
    public void onDownloading(QiscusComment qiscusComment, boolean downloading) {
        if (progress != null) {
            progress.setVisibility(downloading ? View.VISIBLE : View.GONE);
        }
    }
}
