package com.jackyblackson.idunntemplates.core.util.brush;

import com.jackyblackson.idunntemplates.core.domain.brush.BrushSettings;

public class BrushFlagParser {

    public static void applyFlags(BrushSettings settings, String[] args, int startIndex) {
        for (int i = startIndex; i < args.length; i++) {
            String arg = args[i];
            if (arg.startsWith("-")) {
                // Parse flags
                // -r, -x, -z, -rxz
                String flags = arg.substring(1);
                for (char c : flags.toCharArray()) {
                    switch (c) {
                        case 'r':
                            settings.setRotation(BrushSettings.RotationMode.RANDOM);
                            break;
                        case 'x':
                            settings.setFlipX(BrushSettings.FlipMode.RANDOM);
                            break;
                        case 'z':
                            settings.setFlipZ(BrushSettings.FlipMode.RANDOM);
                            break;
                        // Add more flags if needed from spec?
                        // Spec mentions noair, emptyOnly default true.
                        // Flags usually toggle or set specific values.
                        // Spec says: "-r (rotate=random), -x (flipx=random), -z (flipz=random)"
                        // It doesn't mention flags for noair/emptyonly in the BIND command explicitly as simple flags in the summary list
                        // But "Brush Properties" section lists them.
                        // "If not provided... use player preference".
                        // Let's stick to r, x, z for now as explicit randomness.
                    }
                }
            }
        }
    }
}
