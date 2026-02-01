package com.jackyblackson.idunntemplates.core.util;

import java.io.File;
import java.nio.file.Path;

public class PathUtil {
    public static boolean isChild(Path child, Path parent) {
        try {
            // 1. 转换为绝对路径并规范化（处理 ../ 和 ./）
            Path absChild = child.toAbsolutePath().normalize();
            Path absParent = parent.toAbsolutePath().normalize();

            // 2. 检查 child 是否以 parent 开头，且两者不能相同
            return absChild.startsWith(absParent) && !absChild.equals(absParent);
        } catch (SecurityException e) {
            return false;
        }
    }
}
