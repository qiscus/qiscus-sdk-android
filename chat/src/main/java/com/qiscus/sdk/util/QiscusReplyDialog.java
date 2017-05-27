package com.qiscus.sdk.util;

import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.qiscus.sdk.R;
import com.qiscus.sdk.ui.view.QiscusCircularImageView;

/**
 * Created by rajapulau on 5/26/17.
 */

public class QiscusReplyDialog extends DialogFragment {

    protected TextView textViewMessage;
    protected TextView textViewTitle;
    protected TextView tvTitle;
    protected EditText etInput;
    protected ImageView buttonSend;
    protected QiscusCircularImageView image;


    public static QiscusReplyDialog newInstance(Builder builder) {
        QiscusReplyDialog customDialog = new QiscusReplyDialog();
        Bundle args = new Bundle();
        args.putParcelable("builder", builder);
        customDialog.setArguments(args);

        return customDialog;
    }

    public static class Builder implements Parcelable {
        private final String title;
        private int titleColor;
        private final String message;
        private String hint;
        private String posButtonText;
        private String negButtonText;
        private String resImage;
        private String inputType;
        private int posButtonTextSize;
        private int negButtonTextSize;
        private int posButtonColor;
        private int negButtonColor;
        private String buttonOrientation;

        public Builder(String title, String message, QiscusReplyDialog.ButtonOrientation buttonOrientation) {
            this.title = title;
            this.message = message;
            this.buttonOrientation = buttonOrientation.name();
        }

        public Builder setTitleColor(int color) {
            this.titleColor = color;
            return this;
        }

        public Builder setEditText(String hint) {
            this.hint = hint;
            return this;
        }

        public Builder setPosButton(String btnText) {
            this.posButtonText = btnText;
            return this;
        }

        public Builder setNegButton(String btnText) {
            this.negButtonText = btnText;
            return this;
        }

        public Builder setResImage(String imageId) {
            this.resImage = imageId;
            return this;
        }

        public Builder setInputType(String inputType) {
            this.inputType = inputType;
            return this;
        }

        public QiscusReplyDialog build() {
            return QiscusReplyDialog.newInstance(this);
        }


        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.title);
            dest.writeString(this.message);
            dest.writeString(this.hint);
            dest.writeString(this.posButtonText);
            dest.writeString(this.negButtonText);
            dest.writeString(this.resImage);
            dest.writeString(this.inputType);
            dest.writeInt(this.posButtonTextSize);
            dest.writeInt(this.negButtonTextSize);
            dest.writeString(this.buttonOrientation);
            dest.writeInt(this.posButtonColor);
            dest.writeInt(this.negButtonColor);
            dest.writeInt(this.titleColor);
        }

        protected Builder(Parcel in) {
            this.title = in.readString();
            this.message = in.readString();
            this.hint = in.readString();
            this.posButtonText = in.readString();
            this.negButtonText = in.readString();
            this.resImage = in.readString();
            this.inputType = in.readString();
            this.posButtonTextSize = in.readInt();
            this.negButtonTextSize = in.readInt();
            this.buttonOrientation = in.readString();
            this.negButtonColor = in.readInt();
            this.posButtonColor = in.readInt();
            this.titleColor = in.readInt();

        }

        public static final Creator<Builder> CREATOR = new Creator<Builder>() {
            public Builder createFromParcel(Parcel source) {
                return new Builder(source);
            }

            public Builder[] newArray(int size) {
                return new Builder[size];
            }
        };
    }

    public QiscusReplyDialog() {
        // Required empty public constructor
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        dismissListener.onDismissDialog(dialog);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_qiscus_reply_dialog, container, false);

        tvTitle = (TextView) view.findViewById(R.id.tv_title);
        textViewMessage = (TextView) view.findViewById(R.id.textView_message);
        textViewTitle = (TextView) view.findViewById(R.id.textView_title);
        etInput = (EditText) view.findViewById(R.id.et_input);
        buttonSend = (ImageView) view.findViewById(R.id.button_send);
        image = (QiscusCircularImageView) view.findViewById(R.id.image);

        etInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                buttonSend.setOnClickListener(v -> btnSend());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        getDialog().getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(0));
        getDialog().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        return view;
    }

    public void btnSend() {
        if (clickSubmitListener != null) {
            Bundle bundle = new Bundle();
            bundle.putString("value", etInput.getText().toString());
            clickSubmitListener.onSend(etInput.getText().toString());
        }
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Builder builder = getArguments().getParcelable("builder");
        String title = builder.title;
        int titleColor = builder.titleColor;
        String message = builder.message;
        String resImage = builder.resImage;

        if (!TextUtils.isEmpty(title)) {
            textViewTitle.setText(title);
        } else {
            textViewTitle.setVisibility(View.INVISIBLE);
        }

        if (titleColor != 0) {
            textViewTitle.setTextColor(titleColor);
        }


        if (!TextUtils.isEmpty(message)) {
            textViewMessage.setText(message);
        } else {
            textViewMessage.setVisibility(View.GONE);
        }

        if (resImage != null) {
            Glide.with(this).load(resImage)
                    .error(R.drawable.ic_qiscus_avatar)
                    .placeholder(R.drawable.ic_qiscus_avatar)
                    .dontAnimate()
                    .into(image);
        } else {
            image.setVisibility(View.GONE);
        }
    }


    private OnClickSubmitListener clickSubmitListener;
    private OnDismissListener dismissListener;

    public void setOnClickSubmitListener(OnClickSubmitListener listener) {
        this.clickSubmitListener = listener;
    }

    public void setDismissListener(OnDismissListener dismissListener) {
        this.dismissListener = dismissListener;
    }

    public interface OnClickSubmitListener {
        void onSend(String result);
    }

    public interface OnDismissListener {
        void onDismissDialog(DialogInterface dialog);
    }

    public enum ButtonOrientation {
        HORIZONTAL
    }
}
