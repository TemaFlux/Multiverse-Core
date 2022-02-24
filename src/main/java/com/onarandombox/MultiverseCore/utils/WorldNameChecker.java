package com.onarandombox.MultiverseCore.utils;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * <p>Utility class in helping to check the status of a world name and it's associated world folder.</p>
 *
 * <p>Note this is for preliminary checks and better command output. A valid result will suggest but not
 * 100% determine that a world name can be created, loaded or imported.</p>
 */
public class WorldNameChecker {

    private static final Pattern WORLD_NAME_PATTERN = Pattern.compile("[a-zA-Z0-9/._-]+");
    private static final Set<String> BLACKLIST_NAMES = Collections.unmodifiableSet(new HashSet<String>() {
    private static final long serialVersionUID = 1L;
	{
        add("plugins");
        add("logs");
        add("cache");
        add("crash-reports");
    }});

    /**
     * Checks if a world name is valid.
     *
     * @param worldName The world name to check on.
     * @return True if check result is valid, else false.
     */
    public static boolean isValidWorldName(@Nullable String worldName) {
        return checkName(worldName) == NameStatus.VALID;
    }

    /**
     * Checks the current validity status of a world name.
     *
     * @param worldName The world name to check on.
     * @return The resulting name status.
     */
    @NotNull
    public static NameStatus checkName(@Nullable String worldName) {
        if (BLACKLIST_NAMES.contains(worldName)) {
            return NameStatus.BLACKLISTED;
        }
        if (worldName == null || !WORLD_NAME_PATTERN.matcher(worldName).matches()) {
            return NameStatus.INVALID_CHARS;
        }
        return NameStatus.VALID;
    }

    /**
     * Checks if a world name has a valid world folder.
     *
     * @param worldName The world name to check on.
     * @return True if check result is valid, else false.
     */
    public static boolean isValidWorldFolder(@Nullable String worldName) {
        return checkFolder(worldName) == FolderStatus.VALID;
    }

    /**
     * Checks if a world folder is valid.
     *
     * @param worldFolder   The world folder to check on.
     * @return True if check result is valid, else false.
     */
    public static boolean isValidWorldFolder(@Nullable File worldFolder) {
        return checkFolder(worldFolder) == FolderStatus.VALID;
    }

    /**
     * Checks the current folder status for a world name.
     *
     * @param worldName The world name to check on.
     * @return The resulting folder status.
     */
    @NotNull
    public static FolderStatus checkFolder(@Nullable String worldName) {
        if (worldName == null) {
            return FolderStatus.DOES_NOT_EXIST;
        }
        File worldFolder = new File(Bukkit.getWorldContainer(), worldName);
        return checkFolder(worldFolder);
    }

    /**
     * Checks the current folder status.
     *
     * @param worldFolder   The world folder to check on.
     * @return The resulting folder status.
     */
    @NotNull
    public static FolderStatus checkFolder(@Nullable File worldFolder) {
        if (worldFolder == null || !worldFolder.exists() || !worldFolder.isDirectory()) {
            return FolderStatus.DOES_NOT_EXIST;
        }
        if (!folderHasDat(worldFolder)) {
            return FolderStatus.NOT_A_WORLD;
        }
        return FolderStatus.VALID;
    }

    /**
     * A very basic check to see if a folder has a level.dat file. If it does, we can safely assume
     * it's a world folder.
     *
     * @param worldFolder   The File that may be a world.
     * @return True if it looks like a world, else false.
     */
    private static boolean folderHasDat(@NotNull File worldFolder) {
        File[] files = worldFolder.listFiles((file, name) -> name.toLowerCase().endsWith(".dat"));
        return files != null && files.length > 0;
    }

    /**
     * Result after checking validity of world name.
     */
    public enum NameStatus {
        /**
         * Name is valid.
         */
        VALID,

        /**
         * Name not valid as it contains invalid characters.
         */
        INVALID_CHARS,

        /**
         * Name not valid as it is deemed blacklisted.
         */
        BLACKLISTED
    }

    /**
     * Result after checking validity of world folder.
     */
    public enum FolderStatus {
        /**
         * Folder is valid.
         */
        VALID,

        /**
         * Folder exist, but contents in it doesnt look like a world.
         */
        NOT_A_WORLD,

        /**
         * Folder does not exist.
         */
        DOES_NOT_EXIST
    }
}
