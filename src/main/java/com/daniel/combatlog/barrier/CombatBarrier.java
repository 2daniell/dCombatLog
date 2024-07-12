package com.daniel.combatlog.barrier;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import com.daniel.combatlog.CombatLog;
import com.daniel.combatlog.combat.CombatLogManager;
import com.daniel.combatlog.event.impl.BarrierBreakEvent;
import com.daniel.combatlog.handler.RegionHandler;
import com.daniel.combatlog.model.CombatInfo;
import com.google.common.collect.Maps;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@SuppressWarnings("ALL")
public class CombatBarrier implements Listener {

    private static final CombatLogManager manager = CombatLog.getInstance().getCombatManager();
    private static final Map<UUID, CopyOnWriteArrayList<BlockPosition>> blocks = Maps.newConcurrentMap();

    public static void init(JavaPlugin plugin) {
        ProtocolManager protocolManager = CombatLog.getProtocol();
        protocolManager.addPacketListener(new PacketAdapter(plugin, PacketType.Play.Client.BLOCK_DIG) {
            @Override
            public void onPacketReceiving(PacketEvent event) {

                Player player = event.getPlayer();

                CombatInfo combat = manager.findCombat(player.getUniqueId());
                if (combat == null) return;

                BlockPosition blockPosition = event.getPacket().getBlockPositionModifier().read(0);

                CopyOnWriteArrayList copyOnWriteArrayList = blocks.getOrDefault(player.getUniqueId(), new CopyOnWriteArrayList<>());
                if (copyOnWriteArrayList.isEmpty()) return;

                if (!copyOnWriteArrayList.contains(blockPosition)) return;

                EnumWrappers.PlayerDigType playerDigType = event.getPacket().getPlayerDigTypes().read(0);

                if (playerDigType == EnumWrappers.PlayerDigType.STOP_DESTROY_BLOCK) {

                    Block block = blockPosition.toLocation(player.getLocation().getWorld()).getBlock();

                    BarrierBreakEvent barrierBreakEvent = new BarrierBreakEvent(player, blockPosition);
                    barrierBreakEvent.call();
                }
            }
        });
    }

    @EventHandler
    public void onBreak(BarrierBreakEvent e) {

        Player player = e.getPlayer();

        CombatInfo combat = manager.findCombat(player.getUniqueId());
        if (combat == null) return;

        BlockPosition blockPosition = e.getBlockPosition();
        Bukkit.getScheduler().runTask(CombatLog.getInstance(), () -> send(player, blockPosition.getX(), blockPosition.getY(), blockPosition.getZ()));

        player.sendMessage(CombatLog.config().getString("message.barrier-break").replace('&', '§'));

    }

    /*@EventHandler(priority = EventPriority.HIGHEST)
    public void onMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();

        CombatInfo combat = manager.findCombat(player.getUniqueId());
        if (combat == null) return;

        Location from = e.getFrom();
        Location to = e.getTo();

        if (to.distanceSquared(from) < 0.01) return;

        Vector direction = to.toVector().subtract(from.toVector()).normalize();
        Location frontLocation = to.clone().add(direction);

        List<Location> locationsToCheck = Arrays.asList(
                frontLocation,
                frontLocation.clone().add(0, 1, 0),
                frontLocation.clone().add(1, 0, 0),
                frontLocation.clone().add(-1, 0, 0),
                frontLocation.clone().add(0, 0, 1),
                frontLocation.clone().add(0, 0, -1),
                frontLocation.clone().add(1, 1, 0),
                frontLocation.clone().add(-1, 1, 0),
                frontLocation.clone().add(0, 1, 1),
                frontLocation.clone().add(0, 1, -1)
        );

        List<Location> barriersToPlace = new ArrayList<>();

        for (Location loc : locationsToCheck) {
            if (!RegionHandler.PvpIsAllowed(loc)) {
                barriersToPlace.add(loc);
            }
        }

        if (!barriersToPlace.isEmpty()) {
            for (Location barrierLoc : barriersToPlace) {
                Bukkit.getScheduler().runTask(CombatLog.getInstance(), () -> send(player, barrierLoc.getBlockX(), barrierLoc.getBlockY(), barrierLoc.getBlockZ()));
            }
        }
    }*/

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();

        CombatInfo combat = manager.findCombat(player.getUniqueId());
        if (combat == null) return;

        Location from = e.getFrom();
        Location to = e.getTo();

        if (to.distanceSquared(from) < 0.01) return;

        Vector direction = to.toVector().subtract(from.toVector()).normalize();
        Location frontLocation = to.clone().add(direction);

        List<Location> locationsToCheck = Arrays.asList(
                frontLocation,
                frontLocation.clone().add(0, 1, 0),
                frontLocation.clone().add(1, 0, 0),
                frontLocation.clone().add(-1, 0, 0),
                frontLocation.clone().add(0, 0, 1),
                frontLocation.clone().add(0, 0, -1),
                frontLocation.clone().add(1, 1, 0),
                frontLocation.clone().add(-1, 1, 0),
                frontLocation.clone().add(0, 1, 1),
                frontLocation.clone().add(0, 1, -1)
        );

        List<Location> barriersToPlace = new ArrayList<>();

        for (Location loc : locationsToCheck) {
            if (!RegionHandler.PvpIsAllowed(loc)) {
                if (RegionHandler.isAtRegionEdge(loc)) {
                    barriersToPlace.add(loc);
                }
            }
        }

        if (!barriersToPlace.isEmpty()) {
            for (Location barrierLoc : barriersToPlace) {
                Bukkit.getScheduler().runTask(CombatLog.getInstance(), () -> send(player, barrierLoc.getBlockX(), barrierLoc.getBlockY(), barrierLoc.getBlockZ()));
            }
        }
    }

    public static void send(Player player, int x, int y, int z) {
        final ProtocolManager protocolManager = CombatLog.getProtocol();

        Block block = player.getWorld().getBlockAt(x, y, z);
        if (block.getType() != Material.AIR) return;

        PacketContainer packet = new PacketContainer(PacketType.Play.Server.BLOCK_CHANGE);

        BlockPosition blockPosition = new BlockPosition(x, y, z);
        blocks.computeIfAbsent(player.getUniqueId(), (k) -> new CopyOnWriteArrayList<>()).addIfAbsent(blockPosition);

        packet.getBlockPositionModifier().write(0, blockPosition);
        packet.getBlockData().write(0, WrappedBlockData.createData(Material.STAINED_GLASS, 14));
        

        protocolManager.sendServerPacket(player, packet);

    }

    public static void restore(Player player, int x, int y, int z) {
        final ProtocolManager protocolManager = CombatLog.getProtocol();

        Block block = player.getWorld().getBlockAt(x, y, z);

        PacketContainer packet = new PacketContainer(PacketType.Play.Server.BLOCK_CHANGE);

        BlockPosition blockPosition = new BlockPosition(x, y, z);
        blocks.computeIfPresent(player.getUniqueId(), (k, v) -> {
            v.removeIf(e -> e.equals(blockPosition));

            if (v.isEmpty()) {
                return null;
            }

            return v;

        });

        packet.getBlockPositionModifier().write(0, blockPosition);

        if (block == null ||block.getType() == Material.AIR) {
            packet.getBlockData().write(0, WrappedBlockData.createData(Material.AIR));
        } else {
            packet.getBlockData().write(0, WrappedBlockData.createData(block.getType(), block.getData()));
        }

        protocolManager.sendServerPacket(player, packet);

    }

    public static Map<UUID, CopyOnWriteArrayList<BlockPosition>> getBlocks() {
        return blocks;
    }
}