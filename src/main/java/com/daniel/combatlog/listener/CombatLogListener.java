package com.daniel.combatlog.listener;

import com.comphenix.protocol.PacketType;
import com.daniel.combatlog.CombatLog;
import com.daniel.combatlog.combat.CombatLogManager;
import com.daniel.combatlog.enums.CommandType;
import com.daniel.combatlog.handler.RegionHandler;
import com.daniel.combatlog.model.CombatInfo;
import com.daniel.combatlog.utils.ClanUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.*;

public class CombatLogListener implements Listener {

    private final CombatLogManager manager;

    public CombatLogListener(CombatLogManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        if (CombatLog.config().getStringList("worlds").contains(e.getEntity().getWorld().getName())) {
            if (e.getDamager() instanceof Player && e.getEntity() instanceof Player) {
                Player damager = (Player) e.getDamager();
                Player damaged = (Player) e.getEntity();

                if (RegionHandler.PvpIsAllowed(damaged.getLocation()) && RegionHandler.PvpIsAllowed(damager.getLocation())) {
                    if (CombatLog.getClanAPI() != null) {
                        if (!ClanUtils.isFriend(damaged, damager)) {
                            manager.add(damaged.getUniqueId(), damager.getUniqueId());
                        }
                    } else {
                        manager.add(damaged.getUniqueId(), damager.getUniqueId());
                    }
                }

            } else if (e.getCause() == EntityDamageEvent.DamageCause.PROJECTILE) {

                if (e.getDamager() instanceof Arrow ||
                        e.getDamager() instanceof Snowball ||
                        e.getDamager() instanceof Fireball ||
                        e.getDamager() instanceof Egg ||
                        (e.getDamager() instanceof ThrownPotion && hasDamageEffects((ThrownPotion) e.getDamager()))) {

                    Projectile projectile = (Projectile) e.getDamager();

                    if (!(projectile.getShooter() instanceof Player)) return;

                    Player damager = (Player) projectile.getShooter();

                    if (!(e.getEntity() instanceof Player)) return;

                    Player damaged = (Player) e.getEntity();

                    if (RegionHandler.PvpIsAllowed(damaged.getLocation()) && RegionHandler.PvpIsAllowed(damager.getLocation())) {


                        if (CombatLog.getClanAPI() != null) {
                            if (!ClanUtils.isFriend(damaged, damager)) {
                                manager.add(damaged.getUniqueId(), damager.getUniqueId());
                            }
                        } else {
                            manager.add(damaged.getUniqueId(), damager.getUniqueId());
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {

        CombatInfo combat = manager.findCombat(e.getPlayer().getUniqueId());
        if (combat == null) return;

        Location from = e.getFrom();
        Location to = e.getTo();

        if (to.distanceSquared(from) < 0.01) return;

        if (!RegionHandler.PvpIsAllowed(to)) {
            e.setTo(from);
        }

    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        if (e.getEntity().getKiller() == null) return;

        Player killed = e.getEntity();
        Player killer = e.getEntity().getKiller();

        CombatInfo combat = manager.findCombat(killed.getUniqueId());
        if (combat == null) return;

        manager.remove(killed.getUniqueId());

        CombatInfo killerCombat = manager.findCombat(killer.getUniqueId());
        if (killerCombat != null) killerCombat.setDamager(null);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (!CombatLog.config().getBoolean("config.enable-enderpearl")) {
            if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {

                Player player = e.getPlayer();
                ItemStack item = e.getItem();

                if (item != null && item.getType() == Material.ENDER_PEARL && manager.findCombat(player.getUniqueId()) != null) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', CombatLog.config().getString("message.use-enderpearl")));
                    e.setCancelled(true);
                    player.updateInventory();
                }
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {

        if (CombatLog.config().getBoolean("config.punish")) {

            CombatInfo combat = manager.findCombat(e.getPlayer().getUniqueId());
            if (combat == null) return;

            Player player = e.getPlayer();

            player.setHealth(0.0);

            if (CombatLog.config().getBoolean("config.message-punish")) {

                Bukkit.getOnlinePlayers().forEach(target -> CombatLog.config().getStringList("message.message-punish")
                        .forEach(msg -> target.sendMessage(msg.replace('&', '§')
                                .replaceAll("%player%", player.getName()))));

            }
        }

    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent e) {
        if (CombatLog.config().getBoolean("config.command.enable")) {

            CombatInfo combat = manager.findCombat(e.getPlayer().getUniqueId());
            if (combat == null) return;

            CommandType type;

            try {
                type = CommandType.valueOf(CombatLog.config().get("config.command.type").toString().toUpperCase());
            } catch (IllegalArgumentException exception) {
                System.out.println("Erro na configuração: 'config.command.type' não é 'WHITELIST' ou 'BLACKLIST'");
                return;
            }

            Player player = e.getPlayer();
            String command = e.getMessage().toLowerCase().split(" ")[0];

            FileConfiguration config = CombatLog.config();

            boolean commandAllowed = type == CommandType.WHITELIST ?
                    config.getStringList("config.command.commands").contains(command) :
                    !config.getStringList("config.command.commands").contains(command);

            if (!commandAllowed) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("message.unavaliable-command")));
                e.setCancelled(true);
            }

        }
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent e) {
        if (CombatLog.config().getBoolean("config.teleport.enable")) {

            Player player = e.getPlayer();

            CombatInfo combat = manager.findCombat(player.getUniqueId());
            if (combat == null) return;

            PlayerTeleportEvent.TeleportCause cause = e.getCause();
            FileConfiguration config = CombatLog.config();

            boolean allowed = config.getStringList("config.blocked-teleport-cause-list").contains(cause.toString());

            if (allowed) {

                player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("message.teleport-canceled")));
                e.setCancelled(true);

            }
        }
    }

    private boolean hasDamageEffects(ThrownPotion potion) {
        Collection<PotionEffect> effects = potion.getEffects();
        for (PotionEffect effect : effects) {
            PotionEffectType type = effect.getType();
            if (type.equals(PotionEffectType.HARM) || type.equals(PotionEffectType.POISON)) {
                return true;
            }
        }
        return false;
    }
}
