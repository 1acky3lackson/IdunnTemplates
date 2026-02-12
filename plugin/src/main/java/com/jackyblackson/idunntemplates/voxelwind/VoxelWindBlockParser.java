package com.jackyblackson.idunntemplates.voxelwind;

import com.fastasyncworldedit.core.extension.factory.parser.AliasedParser;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.factory.parser.DefaultBlockParser;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.internal.registry.InputParser;
import com.sk89q.worldedit.world.block.BaseBlock;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class VoxelWindBlockParser extends InputParser<BaseBlock> implements AliasedParser {

    private final DefaultBlockParser defaultParser;

    public VoxelWindBlockParser(WorldEdit worldEdit) {
        super(worldEdit);
        this.defaultParser = new DefaultBlockParser(worldEdit);
    }

    @Override
    public Stream<String> getSuggestions(String input, ParserContext context) {
        VoxelWindConfig config = VoxelWindConfig.get();
        String sep = config.getSeparator();
        String lowerInput = input.toLowerCase();

        // 1. 获取组合规则的补全
        Stream<String> combinationSuggestions = Stream.empty();
        if (input.contains(sep)) {
            String[] parts = input.split(sep, 2);
            String prefix = parts[0];
            String suffixPart = parts.length > 1 ? parts[1] : "";

            if (config.getValidPrefixes().contains(prefix.toLowerCase())) {
                combinationSuggestions = config.getAllowedSuffixes(prefix).stream()
                        .filter(s -> s.startsWith(suffixPart.toLowerCase()))
                        .map(s -> prefix + sep + s);
            }
        }

        // 2. 获取直白映射的补全
        Stream<String> directSuggestions = config.getDirectMappingKeys().stream()
                .filter(key -> key.startsWith(lowerInput))
                .map(key -> key); // 直接返回映射 key

        // 3. 获取前缀建议 (组合规则的起始点)
        Stream<String> vwPrefixes = config.getValidPrefixes().stream()
                .filter(p -> p.startsWith(lowerInput))
                .map(p -> p + sep);

        // 4. 原生建议
        Stream<String> nativeSuggestions = defaultParser.getSuggestions(input, context);

        return Stream.concat(
                Stream.concat(combinationSuggestions, directSuggestions),
                Stream.concat(vwPrefixes, nativeSuggestions)
        ).distinct();
    }

    @Override
    public BaseBlock parseFromInput(String input, ParserContext context) throws InputParseException {
        VoxelWindConfig config = VoxelWindConfig.get();
        String sep = config.getSeparator();

        if (input.contains(sep)) {
            String shorthand = input;
            String properties = "";

            if (input.contains("[")) {
                int bracketIdx = input.indexOf("[");
                shorthand = input.substring(0, bracketIdx);
                properties = input.substring(bracketIdx);
            }

            // A. 优先尝试直白映射 (Direct Mapping)
            String directId = config.getDirectMapping(shorthand);
            if (directId != null) {
                return worldEdit.getBlockFactory().parseFromInput(directId + properties, context);
            }

            // B. 尝试组合逻辑 (Combination Logic)
            String[] parts = shorthand.split(sep, 2);
            if (parts.length == 2) {
                String prefix = parts[0].toLowerCase();
                String suffixKey = parts[1].toLowerCase();

                if (config.isValidCombination(prefix, suffixKey)) {
                    String material = config.getMaterial(prefix);
                    String shape = config.getShape(suffixKey);

                    if (material != null && shape != null) {
                        String standardId = material + "_" + shape;
                        return worldEdit.getBlockFactory().parseFromInput(standardId + properties, context);
                    }
                }
            }
        }

        // C. Fallback
        return defaultParser.parseFromInput(input, context);
    }

    @Override
    public List<String> getMatchedAliases() {
        return Collections.emptyList();
    }
}