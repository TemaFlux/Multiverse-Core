package com.onarandombox.MultiverseCore.utils;

import com.dumptruckman.minecraft.util.Logging;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Helper class to get {@link Player} from name, UUID or Selectors.
 */
public class PlayerFinder {

    private static final Pattern UUID_REGEX = Pattern.compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");
    private static final Pattern COMMA_SPLIT = Pattern.compile(",");

    /**
     * Get a {@link Player} based on an identifier of name UUID or selector.
     *
     * @param playerIdentifier  An identifier of name UUID or selector.
     * @param sender            Target sender for selector.
     * @return The player if found, else null.
     */
    @Nullable
    public static Player get(@NotNull String playerIdentifier,
                             @NotNull CommandSender sender) {

        Player targetPlayer = getByName(playerIdentifier);
        if (targetPlayer != null) {
            return targetPlayer;
        }
        targetPlayer = getByUuid(playerIdentifier);
        if (targetPlayer != null) {
            return targetPlayer;
        }
        return getBySelector(playerIdentifier, sender);
    }

    /**
     * Get multiple {@link Player} based on many identifiers of name UUID or selector.
     *
     * @param playerIdentifiers An identifier of multiple names, UUIDs or selectors, separated by comma.
     * @param sender            Target sender for selector.
     * @return A list of all the {@link Player} found.
     */
    @Nullable
    public static List<Player> getMulti(@NotNull String playerIdentifiers,
                                        @NotNull CommandSender sender) {

        String[] playerIdentifierArray = COMMA_SPLIT.split(playerIdentifiers);
        if (playerIdentifierArray == null || playerIdentifierArray.length == 0) {
            return null;
        }

        List<Player> playerResults = new ArrayList<>();
        for (String playerIdentifier : playerIdentifierArray) {
            Player targetPlayer = getByName(playerIdentifier);
            if (targetPlayer != null) {
                playerResults.add(targetPlayer);
                continue;
            }
            targetPlayer = getByUuid(playerIdentifier);
            if (targetPlayer != null) {
                playerResults.add(targetPlayer);
                continue;
            }
            List<Player> targetPlayers = getMultiBySelector(playerIdentifier, sender);
            if (targetPlayers != null) {
                playerResults.addAll(targetPlayers);
            }
        }
        return playerResults;
    }

    /**
     * Get a {@link Player} based on player name.
     *
     * @param playerName    Name of a {@link Player}.
     * @return The player if found, else null.
     */
    @SuppressWarnings("deprecation")
	@Nullable
    public static Player getByName(@NotNull String playerName) {
        return Bukkit.getPlayerExact(playerName);
    }

    /**
     * Get a {@link Player} based on player UUID.
     *
     * @param playerUuid    UUID of a player.
     * @return The player if found, else null.
     */
    @Nullable
    public static Player getByUuid(@NotNull String playerUuid) {
        if (!UUID_REGEX.matcher(playerUuid).matches()) {
            return null;
        }
        UUID uuid;
        try {
            uuid = UUID.fromString(playerUuid);
        } catch (Exception e) {
            return null;
        }
        return getByUuid(uuid);
    }

    /**
     * Get a {@link Player} based on playerUUID.
     *
     * @param playerUuid    UUID of a player.
     * @return The player if found, else null.
     */
    @Nullable
    public static Player getByUuid(@NotNull UUID playerUuid) {
        return Bukkit.getPlayer(playerUuid);
    }

    /**
     * Get a {@link Player} based on vanilla selectors.
     * https://minecraft.gamepedia.com/Commands#Target_selectors
     *
     * @param playerSelector    A target selector, usually starts with an '@'.
     * @param sender            Target sender for selector.
     * @return The player if only one found, else null.
     */
    @Nullable
    public static Player getBySelector(@NotNull String playerSelector,
                                       @NotNull CommandSender sender) {

        List<Player> matchedPlayers = getMultiBySelector(playerSelector, sender);
        if (matchedPlayers == null || matchedPlayers.isEmpty()) {
            Logging.fine("No player found with selector '%s' for %s.", playerSelector, sender.getName());
            return null;
        }
        if (matchedPlayers.size() > 1) {
            Logging.warning("Ambiguous selector result '%s' for %s (more than one player matched) - %s",
                    playerSelector, sender.getName(), matchedPlayers.toString());
            return null;
        }
        return matchedPlayers.get(0);
    }

    /**
     * Get multiple {@link Player} based on selector.
     * https://minecraft.gamepedia.com/Commands#Target_selectors
     *
     * @param playerSelector    A target selector, usually starts with an '@'.
     * @param sender            Target sender for selector.
     * @return A list of all the {@link Player} found.
     */
    @Nullable
    public static List<Player> getMultiBySelector(@NotNull String playerSelector,
                                                  @NotNull CommandSender sender) {

        if (playerSelector.charAt(0) != '@') {
            return null;
        }
        
        try {
            return selectEntities(sender, playerSelector).stream()
                    .filter(e -> e instanceof Player)
                    .map(e -> ((Player) e))
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            Logging.warning("An error occurred while parsing selector '%s' for %s. Is it is the correct format?",
                    playerSelector, sender.getName());
            e.printStackTrace();
            return null;
        }
    }

	public static List<Entity> selectEntities(CommandSender sender, String playerSelector) {
		final Entity e = (Entity) sender;
		final String selector = playerSelector.split("[")[0];
		final String data = playerSelector.replace(selector, "");
		
		if (selector.equalsIgnoreCase("@p")) {
			return e.getNearbyEntities(30, 30, 30).stream().filter(entity -> entity instanceof Player).limit(getValue(data, "limit", 1)).collect(Collectors.toList());
		}
		else if (selector.equalsIgnoreCase("@r")) {
			return e.getWorld().getEntities().stream()
			.filter(entity -> entity instanceof Player)
			.sorted((o1, o2) -> ThreadLocalRandom.current().nextInt(-1, 2))
			.limit(getValue(data, "limit", 1)).collect(Collectors.toList());
		}
		else if (selector.equalsIgnoreCase("@a")) return e.getWorld().getEntities().stream().filter(entity -> entity instanceof Player).collect(Collectors.toList());
		else if (selector.equalsIgnoreCase("@e")) {
			final EntityType type;
			
			EntityType tmp = null;
			try {
				tmp = EntityType.valueOf(getValue(data, "type", ""));
			} catch (Exception | Error ex) {}
			type = tmp;
			
			return e.getWorld().getEntities().stream().filter(entity -> type == null || entity.getType() == type).collect(Collectors.toList());
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	private static <T> T getValue(String data, String name, T defValue) {
		String parsed = null;
		try {
			parsed = data.split(name + "=")[1].split(",")[0];
		} catch (Exception | Error ex) {}
		
		if (parsed == null) return defValue;
		
		T result = defValue;
		if (defValue instanceof Integer) result = (T) Integer.valueOf(parsed);
		return result;
	}
}
