package tech.zhiwei.tool.io;

import cn.hutool.core.io.file.FileNameUtil;

/**
 * 文件和目录工具类
 *
 * @author LIEN
 * @since 2024/9/1
 */
public class FileUtil extends cn.hutool.core.io.FileUtil {
    /**
     * 获取文件的扩展名（不带“.”)
     *
     * @param fileName 文件名
     * @return 扩展名
     * @see FileNameUtil#extName(String)
     */
    public static String getExt(String fileName) {
        return FileNameUtil.extName(fileName);
    }
}
