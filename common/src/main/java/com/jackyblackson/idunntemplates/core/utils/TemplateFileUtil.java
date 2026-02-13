package com.jackyblackson.idunntemplates.core.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class TemplateFileUtil {

    /**
     * Moves a template directory from oldPath to newPath relative to rootDir.
     * @param rootDir The root directory for templates/schematics.
     * @param oldPath The relative path of the existing template directory.
     * @param newPath The relative path for the new location.
     * @throws IOException If source doesn't exist, destination exists, or move fails.
     */
    public static void moveTemplateDirectory(File rootDir, String oldPath, String newPath) throws IOException {
        File oldDir = new File(rootDir, oldPath);
        File newDir = new File(rootDir, newPath);

        if (oldDir.getCanonicalPath().equals(newDir.getCanonicalPath())) {
            return;
        }

        // Security check: ensure new path is within root directory
        String rootCanonical = rootDir.getCanonicalPath();
        String newCanonical = newDir.getCanonicalPath();
        if (!newCanonical.startsWith(rootCanonical)) {
            throw new SecurityException("Access Denied: Path traversal attempt detected.");
        }

        if (!oldDir.exists()) {
            throw new IOException("Source directory does not exist: " + oldDir.getAbsolutePath());
        }
        if (newDir.exists()) {
            throw new IOException("Destination directory already exists: " + newDir.getAbsolutePath());
        }

        // Ensure parent directory exists
        File parentDir = newDir.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                throw new IOException("Failed to create parent directory: " + parentDir.getAbsolutePath());
            }
        }

        try {
            Files.move(oldDir.toPath(), newDir.toPath(), StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            // Fallback if atomic move fails (e.g. cross-filesystem or not supported)
            Files.move(oldDir.toPath(), newDir.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
