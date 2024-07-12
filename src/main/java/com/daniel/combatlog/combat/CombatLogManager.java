package com.daniel.combatlog.combat;

import com.daniel.combatlog.CombatLog;
import com.daniel.combatlog.barrier.CombatBarrier;
import com.daniel.combatlog.model.CombatInfo;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public interface CombatLogManager {

    void startTask();
    void stopTask();
    void add(UUID damaged, UUID damager);
    void remove(UUID uuid);
    boolean isEmpty();
    CombatInfo findCombat(UUID uuid);
    Map<UUID, CombatInfo> getAll();

    static void removeAll() {
        CombatLog.getInstance().getCombatManager().getAll().clear();
    }
}
