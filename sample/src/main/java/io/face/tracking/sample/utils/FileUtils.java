package io.face.tracking.sample.utils;

import java.io.File;

/**
 * <br/>   Description：
 *
 * @author Ztiany
 *         Email: 1169654504@qq.com
 *         Date : 2016-11-30 16:29
 */

public class FileUtils {

    private FileUtils() {
        throw new UnsupportedOperationException("no need instantiation");
    }

    public static boolean makeFilePath(File file) {
        return file != null && makeDir(file.getParentFile());
    }

    private static boolean makeDir(File file) {
        return file != null && file.mkdirs();
    }

}
