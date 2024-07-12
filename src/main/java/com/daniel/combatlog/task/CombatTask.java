package com.daniel.combatlog.task;

import com.comphenix.protocol.wrappers.BlockPosition;
import com.daniel.combatlog.CombatLog;
import com.daniel.combatlog.barrier.CombatBarrier;
import com.daniel.combatlog.combat.CombatLogManager;
import com.daniel.combatlog.model.CombatInfo;
import com.daniel.combatlog.utils.ActionBar;
import org.apache.logging.log4j.spi.CopyOnWrite;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class CombatTask extends BukkitRunnable {

    private final CombatLogManager manager;

    public CombatTask(CombatLogManager manager) {
        this.manager = manager;
    }

    @Override
    public void run() {

        Map<UUID, CombatInfo> combats = manager.getAll();
        if (combats.isEmpty()) {
            manager.stopTask();
            return;
        }

        combats.forEach((k, v) -> {

            Player player = Bukkit.getPlayer(k);
            if (player == null || !player.isOnline()) {
                manager.remove(k); return;
            }

            CombatInfo combat = v;

            if (!(combat.isExpired())) {

                long timeLeft = combat.getTimeLeft();

                if (combat.getDamager() != null && !combat.getDamager().equals(combat.getDamaged())) {

                    OfflinePlayer target = Bukkit.getOfflinePlayer(combat.getDamager());

                    String message = CombatLog.config().getString("message.combat.in-combat").replace('&', '§')
                            .replaceAll("%time%", String.valueOf(timeLeft)).replaceAll("%target%", target.getName());

                    if (CombatLog.config().getBoolean("message.combat.use-effect")) {

                        String colorCode;
                        if (timeLeft % 2 == 0) {
                            colorCode = "§c";
                        } else {
                            colorCode = "§4";
                        }

                        message = colorCode + ChatColor.stripColor(message);
                        ActionBar.sendActionBar(player, message);

                    } else {
                        ActionBar.sendActionBar(player, message);
                    }

                } else {

                    String message = CombatLog.config().getString("message.combat.no-enemy").replace('&', '§')
                            .replaceAll("%time%", String.valueOf(timeLeft));

                    ActionBar.sendActionBar(player, message);

                }

                return;
            }

            String message = CombatLog.config().getString("message.leave-combat").replace('&', '§');

            ActionBar.sendActionBar(player, message);

            manager.remove(k);

            Bukkit.getScheduler().runTask(CombatLog.getInstance(), () -> {
                CopyOnWriteArrayList<BlockPosition> blocks = CombatBarrier.getBlocks().get(player.getUniqueId());
                if (blocks != null) {
                    if (!blocks.isEmpty()) {

                        for (BlockPosition blockPosition : blocks) {

                            CombatBarrier.restore(player, blockPosition.getX(), blockPosition.getY(), blockPosition.getZ());

                        }

                    }
                }
            });

            if (manager.isEmpty()) {
                manager.stopTask();
            }
        });
    }
}
