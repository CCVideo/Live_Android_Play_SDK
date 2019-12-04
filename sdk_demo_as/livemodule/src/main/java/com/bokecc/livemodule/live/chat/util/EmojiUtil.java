package com.bokecc.livemodule.live.chat.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.widget.EditText;

import com.bokecc.livemodule.R;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmojiUtil {

    public static final Integer[] imgs = new Integer[] {
            R.drawable.em2_01, R.drawable.em2_02, R.drawable.em2_03, R.drawable.em2_04, R.drawable.em2_05, R.drawable.em2_06,
            R.drawable.em2_07, R.drawable.em2_08, R.drawable.em2_09, R.drawable.em2_10, R.drawable.em2_11, R.drawable.em2_12,
            R.drawable.em2_13, R.drawable.em2_14, R.drawable.em2_15, R.drawable.em2_16, R.drawable.em2_17, R.drawable.em2_18,
            R.drawable.em2_19, R.drawable.em2_20, R.drawable.em2_del
    };

    public static String[] imgNames = new String[] {
            "[em2_01]", "[em2_02]", "[em2_03]", "[em2_04]", "[em2_05]", "[em2_06]", "[em2_07]", "[em2_08]", "[em2_09]", "[em2_10]",
            "[em2_11]", "[em2_12]", "[em2_13]", "[em2_14]", "[em2_15]", "[em2_16]", "[em2_17]", "[em2_18]", "[em2_19]", "[em2_20]",
    };

    private static List<String> imgNamesList;

    static {
        imgNamesList = Arrays.asList(imgNames);
    }

    public static Pattern pattern = Pattern.compile("\\[em2_[0-2][0-9]\\]");

    public static SpannableString parseFaceMsg(Context context, SpannableString faceMsg) {
        Matcher m = pattern.matcher(faceMsg.toString());
        while (m.find()) {
            String imgStr = m.group();
            int imgStrIndex = imgNamesList.indexOf(imgStr);
            if (imgStrIndex != -1) {
                Bitmap imgBitmap = BitmapFactory.decodeResource(context.getResources(), imgs[imgStrIndex]);
                imgBitmap = ThumbnailUtils.extractThumbnail(imgBitmap, imgBitmap.getWidth(), imgBitmap.getHeight());
                ImageSpan imgSpan = new ImageSpan(context, imgBitmap);
                faceMsg.setSpan(imgSpan, m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        return faceMsg;
    }

    public static void deleteInputOne(EditText mInput) {
        Editable editable = mInput.getText();
        int length = editable.length();
        if (length <= 0) {
            return;
        }
        int arrowPosition = mInput.getSelectionStart();
        if (arrowPosition == 0) {
            return;
        }
        String subString = editable.toString().substring(0, arrowPosition);
        if (subString.length() >= 8) {
            int imgIndex = subString.lastIndexOf("[em2_");

            if ((imgIndex + 8) == arrowPosition) {
                if (EmojiUtil.pattern.matcher(editable.toString().substring(imgIndex, imgIndex + 8)).find()) {
                    editable.delete(arrowPosition - 8, arrowPosition);
                } else {
                    editable.delete(arrowPosition - 1, arrowPosition);
                }
            } else {
                editable.delete(arrowPosition - 1, arrowPosition);
            }
        } else {
            editable.delete(arrowPosition - 1, arrowPosition);
        }
    }

    public static void addEmoji(Context context, EditText mInput, int position) {
        String emojiStr = EmojiUtil.imgNames[position];
        int index = mInput.getSelectionStart();
        String content = mInput.getText().toString();
        String pre = content.substring(0, index);
        String next = content.substring(index, content.length());
        SpannableString ss = new SpannableString(pre + emojiStr + next);
        mInput.setText(parseFaceMsg(context, ss));
        mInput.setSelection(index + emojiStr.length());
    }
}
