package com.jackyblackson.idunntemplates.backend.util;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CollectionUtils {

    /**
     * 通用批处理保序方法
     * @param inputs 原始输入列表（可能包含 null）
     * @param keyExtractor 从输入对象中提取 Key 的函数
     * @param batchProcessor 批量处理非空 Key 并返回结果 Map 的逻辑
     * @param defaultValue 当输入为 null 或处理无结果时的默认值
     */
    public static <T, K, V> Map<K, V> resolveBatch(
            List<T> inputs,
            Function<T, K> keyExtractor,
            Function<List<K>, Map<K, V>> batchProcessor,
            V defaultValue) {

        if (inputs == null || inputs.isEmpty()) {
            return Collections.emptyMap();
        }

        // 1. 过滤掉 null 的输入，并提取非空的 Key
        List<K> validKeys = inputs.stream()
                .filter(Objects::nonNull)
                .map(keyExtractor)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // 2. 执行批量处理逻辑
        Map<K, V> processedResults = validKeys.isEmpty()
                ? Collections.emptyMap()
                : batchProcessor.apply(validKeys);

        // 3. 重新遍历原始列表，确保每个输入（包括 null）在结果中都有占位
        Map<K, V> finalMap = new LinkedHashMap<>(); // 使用 LinkedHashMap 保持输入顺序感
        for (T input : inputs) {
            if (input == null) {
                // 如果输入本身是 null，这里取决于你是否需要一个 null key，
                // 通常 UUID 场景下我们跳过或设默认值。
                continue;
            }
            K key = keyExtractor.apply(input);
            finalMap.put(key, processedResults.getOrDefault(key, defaultValue));
        }

        return finalMap;
    }

    /**
     * 将原始列表与关联数据 Map 合并转换为 DTO 列表
     * @param source 原始对象列表
     * @param keyExtractor 提取关联键的函数
     * @param dataMap 包含关联数据的 Map
     * @param mapper 转换函数 (原始对象, 关联数据) -> DTO
     * @param defaultValue 当 Map 中找不到数据时的默认值
     */
    public static <T, K, V, R> List<R> mapToList(
            List<T> source,
            Function<T, K> keyExtractor,
            Map<K, V> dataMap,
            BiFunction<T, V, R> mapper,
            V defaultValue) {

        if (source == null) return Collections.emptyList();

        return source.stream()
                .map(item -> {
                    if (item == null) return (R) null; // 保持原始列表中的 null 占位
                    K key = keyExtractor.apply(item);
                    V value = dataMap != null ? dataMap.getOrDefault(key, defaultValue) : defaultValue;
                    return mapper.apply(item, value);
                })
                .collect(Collectors.toList());
    }
}