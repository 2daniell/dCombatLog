package com.daniel.combatlog.handler;

import com.daniel.combatlog.CombatLog;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public final class RegionHandler {

    public static boolean PvpIsAllowed(Location location) {

        WorldGuardPlugin worldGuard = CombatLog.getWorldGuard();
        
        if (worldGuard == null) {
            System.out.println("WorldGuard não está sendo encontrado corretamente");
            return false;
        }

        RegionManager regionManager = worldGuard.getRegionManager(location.getWorld());
        if (regionManager == null) {
            return false;
        }

        ApplicableRegionSet applicableRegionSet = regionManager.getApplicableRegions(location);
        return applicableRegionSet.queryState(null, DefaultFlag.PVP) != StateFlag.State.DENY;
    }

    public static boolean isAtRegionEdge(Location location) {
        WorldGuardPlugin worldGuardPlugin = CombatLog.getWorldGuard();
        RegionManager regionManager = worldGuardPlugin.getRegionManager(location.getWorld());
        ApplicableRegionSet regions = regionManager.getApplicableRegions(location);

        for (ProtectedRegion region : regions) {
            if (isAtRegionEdge(location, region)) {
                return true;
            }
        }

        return false;
    }

    private static boolean isAtRegionEdge(Location location, ProtectedRegion region) {
        BlockVector min = region.getMinimumPoint();
        BlockVector max = region.getMaximumPoint();

        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();

        if ((x == min.getBlockX() || x == max.getBlockX()) &&
                (y >= min.getBlockY() && y <= max.getBlockY()) &&
                (z >= min.getBlockZ() && z <= max.getBlockZ())) {
            return true;
        }
        if ((y == min.getBlockY() || y == max.getBlockY()) &&
                (x >= min.getBlockX() && x <= max.getBlockX()) &&
                (z >= min.getBlockZ() && z <= max.getBlockZ())) {
            return true;
        }
        if ((z == min.getBlockZ() || z == max.getBlockZ()) &&
                (x >= min.getBlockX() && x <= max.getBlockX()) &&
                (y >= min.getBlockY() && y <= max.getBlockY())) {
            return true;
        }

        return false;
    }

    /*public static boolean hasCrossedRegionBorder(Location from, Location to) {
        WorldGuardPlugin worldGuardPlugin = CombatLog.getWorldGuard();
        RegionManager regionManager = worldGuardPlugin.getRegionManager(from.getWorld());

        ApplicableRegionSet fromRegions = regionManager.getApplicableRegions(from);
        boolean fromInPvpRegion = !fromRegions.getRegions().isEmpty();

        ApplicableRegionSet toRegions = regionManager.getApplicableRegions(to);
        boolean toInPvpRegion = !toRegions.getRegions().isEmpty();

        return fromInPvpRegion && !toInPvpRegion;
    }*/
}
