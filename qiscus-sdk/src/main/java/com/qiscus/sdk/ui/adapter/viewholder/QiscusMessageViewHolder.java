package com.qiscus.sdk.ui.adapter.viewholder;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
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
import com.qiscus.sdk.R;
import com.qiscus.sdk.R2;
import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.data.local.QiscusDataBaseHelper;
import com.qiscus.sdk.data.model.QiscusComment;
import com.qiscus.sdk.ui.adapter.OnItemClickListener;
import com.qiscus.sdk.ui.adapter.OnLongItemClickListener;

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
public class QiscusMessageViewHolder extends QiscusItemViewHolder<QiscusComment> implements
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
    @Nullable @BindView(R2.id.frame) ImageView frame;
    @BindView(R2.id.bubble) ImageView bubble;
    @BindView(R2.id.message) View messageBubble;

    private boolean showDate;
    private boolean fromMe;
    private boolean showBubble;

    private Drawable rightBubbleDrawable;
    private Drawable leftBubbleDrawable;
    private int rightBubbleColor;
    private int leftBubbleColor;
    private int rightBubbleTextColor;
    private int leftBubbleTextColor;
    private int rightBubbleTimeColor;
    private int leftBubbleTimeColor;
    private int failedToSendMessageColor;
    private int dateColor;

    public QiscusMessageViewHolder(View itemView, OnItemClickListener itemClickListener, OnLongItemClickListener longItemClickListener) {
        super(itemView, itemClickListener, longItemClickListener);
        messageBubble.setOnClickListener(this);

        rightBubbleColor = ContextCompat.getColor(Qiscus.getApps(), Qiscus.getChatConfig().getRightBubbleColor());
        rightBubbleTextColor = ContextCompat.getColor(Qiscus.getApps(), Qiscus.getChatConfig().getRightBubbleTextColor());
        rightBubbleTimeColor = ContextCompat.getColor(Qiscus.getApps(), Qiscus.getChatConfig().getRightBubbleTimeColor());
        rightBubbleDrawable = ContextCompat.getDrawable(Qiscus.getApps(), R.drawable.qiscus_rounded_primary_light_chat_bg);
        rightBubbleDrawable.setColorFilter(rightBubbleColor, PorterDuff.Mode.SRC_ATOP);

        leftBubbleColor = ContextCompat.getColor(Qiscus.getApps(), Qiscus.getChatConfig().getLeftBubbleColor());
        leftBubbleTextColor = ContextCompat.getColor(Qiscus.getApps(), Qiscus.getChatConfig().getLeftBubbleTextColor());
        leftBubbleTimeColor = ContextCompat.getColor(Qiscus.getApps(), Qiscus.getChatConfig().getLeftBubbleTimeColor());
        leftBubbleDrawable = ContextCompat.getDrawable(Qiscus.getApps(), R.drawable.qiscus_rounded_primary_chat_bg);
        leftBubbleDrawable.setColorFilter(leftBubbleColor, PorterDuff.Mode.SRC_ATOP);

        failedToSendMessageColor = ContextCompat.getColor(Qiscus.getApps(), Qiscus.getChatConfig().getFailedToSendMessageColor());
        dateColor = ContextCompat.getColor(Qiscus.getApps(), Qiscus.getChatConfig().getDateColor());
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

        setUpColor();

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

    private void setUpColor() {
        if (fromMe) {
            messageBubble.setBackground(rightBubbleDrawable);
            bubble.setColorFilter(rightBubbleColor);

            if (message != null) {
                message.setTextColor(rightBubbleTextColor);
            }

            if (fileType != null) {
                fileType.setTextColor(rightBubbleTimeColor);
            }

            if (frame != null) {
                frame.setColorFilter(rightBubbleColor);
            }

            if (progress != null) {
                progress.setFinishedColor(rightBubbleColor);
            }
        } else {
            messageBubble.setBackground(leftBubbleDrawable);
            bubble.setColorFilter(leftBubbleColor);

            if (message != null) {
                message.setTextColor(leftBubbleTextColor);
            }

            if (fileType != null) {
                fileType.setTextColor(leftBubbleTimeColor);
            }

            if (frame != null) {
                frame.setColorFilter(leftBubbleColor);
            }

            if (progress != null) {
                progress.setFinishedColor(leftBubbleColor);
            }
        }

        if (date != null) {
            date.setTextColor(dateColor);
        }
    }

    private void setUpDownloadIcon(QiscusComment qiscusComment) {
        if (downloadIcon != null) {
            if (qiscusComment.isImage()) {
                if (qiscusComment.getState() == QiscusComment.STATE_FAILED || qiscusComment.getState() == QiscusComment.STATE_SENDING) {
                    downloadIcon.setImageResource(R.drawable.ic_qiscus_upload_big);
                } else {
                    downloadIcon.setImageResource(R.drawable.ic_qiscus_download_big);
                }
            } else {
                if (qiscusComment.getState() == QiscusComment.STATE_FAILED || qiscusComment.getState() == QiscusComment.STATE_SENDING) {
                    downloadIcon.setImageResource(R.drawable.ic_qiscus_upload);
                } else {
                    downloadIcon.setImageResource(R.drawable.ic_qiscus_download);
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
                time.setTextColor(failedToSendMessageColor);
            } else {
                time.setText(Qiscus.getChatConfig().getTimeFormat().format(qiscusComment.getTime()));
                time.setTextColor(fromMe ? rightBubbleTimeColor : leftBubbleTimeColor);
            }
        }
    }

    private void showFileMessage(QiscusComment qiscusComment) {
        File localPath = QiscusDataBaseHelper.getInstance().getLocalPath(qiscusComment.getId());
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
        File localPath = QiscusDataBaseHelper.getInstance().getLocalPath(qiscusComment.getId());
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
                        .error(R.drawable.ic_qiscus_img)
                        .into(thumbnail);
            }
        } else {
            File localPath = QiscusDataBaseHelper.getInstance().getLocalPath(qiscusComment.getId());
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
                    .error(R.drawable.ic_qiscus_img)
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
            iconRead.setColorFilter(qiscusComment.getState() == QiscusComment.STATE_FAILED ?
                    failedToSendMessageColor : rightBubbleTimeColor);
            switch (qiscusComment.getState()) {
                case QiscusComment.STATE_SENDING:
                    iconRead.setImageResource(R.drawable.ic_qiscus_info_time);
                    break;
                case QiscusComment.STATE_ON_QISCUS:
                    iconRead.setImageResource(R.drawable.ic_qiscus_sending);
                    break;
                case QiscusComment.STATE_ON_PUSHER:
                    iconRead.setImageResource(R.drawable.ic_qiscus_read);
                    break;
                case QiscusComment.STATE_FAILED:
                    iconRead.setImageResource(R.drawable.ic_qiscus_sending_failed);
                    break;
            }
        }
    }

    private void showDateOrNot(QiscusComment qiscusComment) {
        if (date != null) {
            if (showDate) {
                date.setText(Qiscus.getChatConfig().getDateFormat().format(qiscusComment.getTime()));
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
