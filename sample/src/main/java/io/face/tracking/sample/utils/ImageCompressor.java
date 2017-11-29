package io.face.tracking.sample.utils;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import com.zxy.tiny.Tiny;
import com.zxy.tiny.core.CompressEngine;

import java.io.File;

import io.face.tracking.sample.AppContext;

/**
 * 图片压缩工具
 *
 * @author Ztiany
 *         Email: 1169654504@qq.com
 *         Date : 2017-05-15 11:42
 */
public class ImageCompressor {

    static {
        Tiny.getInstance().init(AppContext.getAppContext());
    }

    private ImageCompressor() {
        throw new UnsupportedOperationException();
    }

    ///////////////////////////////////////////////////////////////////////////
    // To bitmap
    ///////////////////////////////////////////////////////////////////////////
    @SuppressWarnings("unused")
    public static void compressToBitmap(Source source, @NonNull final Options options, final BitmapCallback callback) {
        Tiny.BitmapCompressOptions bitmapCompressOptions = setupBitmapCompressOptions(options);
        source.mCompressEngine
                .asBitmap()
                .withOptions(bitmapCompressOptions)
                .compress(callback::onCompleted);
    }

    @NonNull
    private static Tiny.BitmapCompressOptions setupBitmapCompressOptions(@NonNull Options options) {
        Tiny.BitmapCompressOptions bitmapCompressOptions = new Tiny.BitmapCompressOptions();
        bitmapCompressOptions.config = options.config;
        bitmapCompressOptions.width = options.width;
        bitmapCompressOptions.height = options.height;
        return bitmapCompressOptions;
    }

    ///////////////////////////////////////////////////////////////////////////
    // To file
    ///////////////////////////////////////////////////////////////////////////

    @SuppressWarnings("unused")
    public static void compressToFile(Source source, @NonNull final FileOptions options, final FileCallback callback) {
        Tiny.FileCompressOptions fileCompressOptions = setupFileCompressOptions(options);
        source.mCompressEngine
                .asFile()
                .withOptions(fileCompressOptions)
                .compress((success, result, path) -> callback.onCompleted(success, path));
    }

    @NonNull
    private static Tiny.FileCompressOptions setupFileCompressOptions(@NonNull FileOptions options) {
        Tiny.FileCompressOptions fileCompressOptions = new Tiny.FileCompressOptions();
        fileCompressOptions.config = options.config;
        fileCompressOptions.width = options.width;
        fileCompressOptions.height = options.height;
        fileCompressOptions.outfile = options.outFile;
        return fileCompressOptions;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Options & CallBack
    ///////////////////////////////////////////////////////////////////////////

    @SuppressWarnings("all")
    public interface BitmapCallback {
        void onCompleted(boolean success, Bitmap bitmap);
    }

    @SuppressWarnings("all")
    public static class Options {
        public int height;
        public int width;
        public Bitmap.Config config = Bitmap.Config.RGB_565;
    }

    public interface FileCallback {
        void onCompleted(boolean success, String path);
    }

    public static class FileOptions extends Options {
        public String outFile;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Source
    ///////////////////////////////////////////////////////////////////////////
    @SuppressWarnings("unused")
    public static class Source {

        private Source() {
        }

        private CompressEngine mCompressEngine;

        public static Source ofBitmap(Bitmap bitmap) {
            Source source = new Source();
            source.mCompressEngine = Tiny.getInstance().source(bitmap);
            return source;
        }

        public static Source ofFile(File file) {
            Source source = new Source();
            source.mCompressEngine = Tiny.getInstance().source(file);
            return source;
        }

        public static Source ofBytes(byte[] bytes) {
            Source source = new Source();
            source.mCompressEngine = Tiny.getInstance().source(bytes);
            return source;
        }

        public static Source ofPath(String path) {
            Source source = new Source();
            source.mCompressEngine = Tiny.getInstance().source(path);
            return source;
        }
    }


}
