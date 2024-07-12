package com.daniel.combatlog.combat.impl;

import com.daniel.combatlog.CombatLog;
import com.daniel.combatlog.combat.CombatLogManager;
import com.daniel.combatlog.commands.CombatLogCommand;
import com.daniel.combatlog.model.CombatInfo;
import com.daniel.combatlog.task.CombatTask;
import com.google.common.collect.Maps;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class CombatLogImpl implements CombatLogManager {

    private final Map<UUID, CombatInfo> handler = Maps.newConcurrentMap();

    private CombatTask combatTask;

    @Override
    public void startTask() {
        if (combatTask != null) return;
        combatTask = new CombatTask(this);
        combatTask.runTaskTimerAsynchronously(CombatLog.getInstance(), 0, 20L);
    }

    @Override
    public void stopTask() {
        if (!isEmpty()) return;
        if (combatTask == null) return;
        combatTask.cancel();
        combatTask = null;
    }

    @Override
    public void add(UUID damaged, UUID damager) {
        boolean shouldStartTask = false;

        if (!CombatLogCommand.getSet().contains(damaged)) {
            handler.computeIfAbsent(damaged, k -> new CombatInfo(damager, damaged)).updateDamager(damager);
            shouldStartTask = true;
        }

        if (!CombatLogCommand.getSet().contains(damager)) {
            handler.computeIfAbsent(damager, k -> new CombatInfo(damaged, damager)).updateDamager(damaged);
            shouldStartTask = true;
        }

        if (shouldStartTask) {
            startTask();
        }
    }

    @Override
    public void remove(UUID uuid) {
        handler.remove(uuid);
    }

    @Override
    public boolean isEmpty() {
        return handler.isEmpty();
    }

    @Override
    public CombatInfo findCombat(UUID uuid) {
        return Optional.ofNullable(handler.get(uuid)).orElse(null);
    }

    @Override
    public Map<UUID, CombatInfo> getAll() {
        return handler;
    }
}
