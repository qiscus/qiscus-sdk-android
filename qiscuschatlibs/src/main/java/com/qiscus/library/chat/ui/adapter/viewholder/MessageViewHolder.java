package com.qiscus.library.chat.ui.adapter.viewholder;

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
import com.qiscus.library.chat.Qiscus;
import com.qiscus.library.chat.R;
import com.qiscus.library.chat.R2;
import com.qiscus.library.chat.data.local.DataBaseHelper;
import com.qiscus.library.chat.data.model.Comment;
import com.qiscus.library.chat.ui.adapter.BaseRecyclerAdapter.OnItemClickListener;
import com.qiscus.library.chat.ui.adapter.BaseRecyclerAdapter.OnLongItemClickListener;
import com.qiscus.library.chat.util.DateUtil;

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
public class MessageViewHolder extends BaseItemViewHolder<Comment> implements
        Comment.ProgressListener, Comment.DownloadingListener {

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
    public void bind(Comment comment) {
        bubble.setVisibility(showBubble ? View.VISIBLE : View.GONE);
        comment.setProgressListener(this);
        comment.setDownloadingListener(this);
        showProgressOrNot(comment);
        showDateOrNot(comment);
        showTime(comment);
        showIconReadOrNot(comment);
        setUpDownloadIcon(comment);

        switch (comment.getType()) {
            case TEXT:
                showTextMessage(comment);
                break;
            case IMAGE:
                showThumbnail(comment);
                break;
            case FILE:
                showExtension(comment);
                showFileMessage(comment);
                break;
        }
    }

    private void setUpDownloadIcon(Comment comment) {
        if (downloadIcon != null) {
            if (comment.isImage()) {
                if (comment.getState() == Comment.STATE_FAILED || comment.getState() == Comment.STATE_SENDING) {
                    downloadIcon.setImageResource(R.drawable.ic_upload_big);
                } else {
                    downloadIcon.setImageResource(R.drawable.ic_download_big);
                }
            } else {
                if (comment.getState() == Comment.STATE_FAILED || comment.getState() == Comment.STATE_SENDING) {
                    downloadIcon.setImageResource(R.drawable.ic_upload);
                } else {
                    downloadIcon.setImageResource(R.drawable.ic_download);
                }
            }
        }
    }

    private void showProgressOrNot(Comment comment) {
        if (progress != null) {
            progress.setProgress(comment.getProgress());
            progress.setVisibility(comment.isDownloading() ? View.VISIBLE : View.GONE);
        }
    }

    private void showTime(Comment comment) {
        if (time != null) {
            if (comment.getState() == Comment.STATE_FAILED) {
                time.setText(R.string.sending_failed);
                time.setTextColor(ContextCompat.getColor(Qiscus.getApps(), R.color.red));
            } else {
                time.setText(DateUtil.toHour(comment.getTime()));
                time.setTextColor(ContextCompat.getColor(Qiscus.getApps(),
                                                         fromMe ? R.color.secondary_text : R.color.primary_light));
            }
        }
    }

    private void showFileMessage(Comment comment) {
        File localPath = DataBaseHelper.getInstance().getLocalPath(comment.getId());
        if (downloadIcon != null) {
            downloadIcon.setVisibility(localPath == null ? View.VISIBLE : View.GONE);
        }
        if (fileName != null) {
            fileName.setText(comment.getAttachmentName());
        }
    }

    private void showExtension(final Comment comment) {
        if (fileType != null) {
            if (comment.getExtension().isEmpty()) {
                fileType.setText(R.string.unkown_type);
            } else {
                fileType.setText(String.format("%s File", comment.getExtension().toUpperCase()));
            }
        }
    }

    private void showThumbnail(final Comment comment) {
        if (thumbnail != null) {
            if (fromMe) {
                showMyImage(comment);
            } else {
                showOthersImage(comment);
            }
        }
        if (fileName != null) {
            fileName.setText(comment.getAttachmentName());
        }
    }

    private void showOthersImage(final Comment comment) {
        File localPath = DataBaseHelper.getInstance().getLocalPath(comment.getId());
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

    private void showMyImage(final Comment comment) {
        if (comment.getState() == Comment.STATE_SENDING) {
            if (holder != null) {
                holder.setVisibility(View.INVISIBLE);
            }
            if (thumbnail != null) {
                thumbnail.setVisibility(View.VISIBLE);
                Glide.with(thumbnail.getContext())
                        .load(new File(comment.getAttachmentUri().toString()))
                        .error(R.drawable.ic_img)
                        .into(thumbnail);
            }
        } else {
            File localPath = DataBaseHelper.getInstance().getLocalPath(comment.getId());
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

    private void showTextMessage(Comment comment) {
        if (message != null) {
            message.setText(comment.getMessage());
        }
    }

    private void showIconReadOrNot(Comment comment) {
        if (iconRead != null) {
            switch (comment.getState()) {
                case Comment.STATE_SENDING:
                    iconRead.setImageResource(R.drawable.ic_info_time);
                    break;
                case Comment.STATE_ON_QISCUS:
                    iconRead.setImageResource(R.drawable.ic_sending);
                    break;
                case Comment.STATE_ON_PUSHER:
                    iconRead.setImageResource(R.drawable.ic_read);
                    break;
                case Comment.STATE_FAILED:
                    iconRead.setImageResource(R.drawable.ic_sending_failed);
                    break;
            }
        }
    }

    private void showDateOrNot(Comment comment) {
        if (date != null) {
            if (showDate) {
                date.setText(DateUtil.toTodayOrDate(comment.getTime()));
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
    public void onProgress(Comment comment, int percentage) {
        if (progress != null) {
            progress.setProgress(percentage);
        }
    }

    @Override
    public void onDownloading(Comment comment, boolean downloading) {
        if (progress != null) {
            progress.setVisibility(downloading ? View.VISIBLE : View.GONE);
        }
    }
}
