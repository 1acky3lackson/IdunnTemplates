package com.jackyblackson.idunntemplates.backend.service;

import org.springframework.stereotype.Service;
import pitheguy.schemconvert.converter.Converter;
import pitheguy.schemconvert.converter.ConversionException;
import pitheguy.schemconvert.converter.formats.SchematicFormat;
import pitheguy.schemconvert.converter.formats.SchematicFormats;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

@Service
public class SchematicFormatService {

    // 实例化转换器核心类
    private final Converter converter = new Converter();

    /**
     * 将源文件转换为目标格式
     *
     * @param sourceFile 源文件 (.schem)
     * @param formatStr  目标格式字符串 (e.g. "litematic", "nbt")
     * @return 转换后的临时文件
     */
    public File convert(File sourceFile, String formatStr) throws IOException, ConversionException {
        // 1. 解析目标格式
        SchematicFormat targetFormat = resolveFormat(formatStr);

        // 2. 创建临时文件
        // 使用 UUID 防止并发文件名冲突，后缀名为目标格式后缀
        String tempFileName = "conv_" + UUID.randomUUID();
        // createTempFile 会自动在操作系统的临时文件夹下创建
        File tempFile = Files.createTempFile(tempFileName, targetFormat.getExtension()).toFile();

        // 确保JVM退出时删除（作为最后的兜底，建议配合 Resource 清理机制）
        tempFile.deleteOnExit();

        // 3. 执行转换
        converter.convert(sourceFile, tempFile, targetFormat);

        return tempFile;
    }

    /**
     * 辅助方法：将字符串映射为 SchematicFormat 枚举
     * 注意：你需要根据 pitheguy 库中 SchematicFormat 的实际定义调整此处代码
     */
    public SchematicFormat resolveFormat(String formatStr) {
        if (formatStr == null) {
            throw new IllegalArgumentException("Format cannot be null");
        }

        // 移除可能存在的点号，例如 ".litematic" -> "litematic"
        String cleaned = formatStr.replace(".", "").toLowerCase();

        // 假设 SchematicFormat 是一个枚举或有静态常量
        // 这里需要根据子模块的具体代码进行适配
        return switch (cleaned) {
            case "litematica", "litematic" -> SchematicFormats.LITEMATIC;
            case "nbt", "becrock", "structure", "structure_block" -> SchematicFormats.NBT; // 或是 SchematicFormat.NBT
            case "schem", "sponge" -> SchematicFormats.SCHEM;
            case "bp", "axiom" -> SchematicFormats.AXIOM; // 假设支持基岩版
            default -> throw new IllegalArgumentException("不支持的格式: " + formatStr);
        };
    }
}
