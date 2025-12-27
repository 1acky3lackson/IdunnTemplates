package com.jackyblackson.idunntemplates.command.sub;

import com.jackyblackson.idunntemplates.command.IdunnSubCommand;
import java.util.List;
import java.util.stream.Collectors;

public abstract class BaseSubCommand implements IdunnSubCommand {

    protected List<String> filter(List<String> options, String current) {
        return options.stream()
                .filter(s -> s.toLowerCase().startsWith(current.toLowerCase()))
                .sorted()
                .collect(Collectors.toList());
    }

    protected int parseInt(String val, int def) {
        try { return Integer.parseInt(val); } catch (NumberFormatException e) { return def; }
    }
}
