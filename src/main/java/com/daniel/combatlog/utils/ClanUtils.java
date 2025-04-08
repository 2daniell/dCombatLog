package com.daniel.combatlog.utils;

import br.com.ystoreplugins.product.yclans.ClanAPIHolder;
import br.com.ystoreplugins.product.yclans.internal.ClanPlayerHolder;
import com.daniel.combatlog.CombatLog;
import org.bukkit.entity.Player;

public class ClanUtils {

    public static boolean isFriend(Player player, Player target) {
        ClanAPIHolder api = CombatLog.getClanAPI();

        ClanPlayerHolder clanPlayer = api.getPlayer(player);
        ClanPlayerHolder targetClanPlayer = api.getPlayer(target);

        if (clanPlayer == null || targetClanPlayer == null) return false;

        String playerClanName = clanPlayer.getClan().getName();
        String targetClanName = targetClanPlayer.getClan().getName();

        return playerClanName.equals(targetClanName);
    }
}
