package io.face.tracking.sample.arc_face;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import io.face.tracking.sample.R;
import io.face.tracking.sample.utils.ImageUtils;


/**
 * @author Ztiany
 *         Email: 1169654504@qq.com
 *         Date : 2017-06-01 16:55
 */
public class ShowImageFragment extends Fragment {

    private static final String SOURCE_PATH_KEY = "source_path_key";

    private String mSourcePath;
    private Stickers mStickers;
    private ShowImageFragmentCallback mShowImageFragmentCallback;

    public interface ShowImageFragmentCallback {
        void showTakePhotoPage();

        FacePosition getFacePosition();
    }

    public static Fragment newInstance(String source) {
        ShowImageFragment showImageFragment = new ShowImageFragment();
        Bundle args = new Bundle();
        args.putString(SOURCE_PATH_KEY, source);
        showImageFragment.setArguments(args);
        return showImageFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mShowImageFragmentCallback = (ShowImageFragmentCallback) getContext();
        mSourcePath = getArguments().getString(SOURCE_PATH_KEY);
        mStickers = new NoEditStickers();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return mStickers.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mStickers.setup();
    }


    interface Stickers {
        View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState);

        void setup();
    }

    private class NoEditStickers implements Stickers {

        private ImageView mImageView;
        private View mLayout;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            mLayout = inflater.inflate(R.layout.fragment_stickers_show, container, false);
            return mLayout;
        }

        @Override
        public void setup() {
            mImageView = (ImageView) mLayout.findViewById(R.id.iv_editor_picture);
            mLayout.findViewById(R.id.tv_return).setOnClickListener(v -> mShowImageFragmentCallback.showTakePhotoPage());

            FacePosition savedFacePosition = mShowImageFragmentCallback.getFacePosition();

            if (savedFacePosition == null) {
                return;
            }

            Bitmap bitmap = BitmapFactory.decodeFile(mSourcePath);
            int rotateDegree = ImageUtils.getRotateDegree(mSourcePath);
            Bitmap result = ImageUtils.rotate(bitmap, rotateDegree, 0, 0, true);
            Bitmap faceMaskBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.img_avatar_portrait);
            PictureEditorUtils.addFaceMask(savedFacePosition, result, faceMaskBitmap,
                    compositeBitmap -> mImageView.setImageBitmap(compositeBitmap)
            );
        }
    }

}
