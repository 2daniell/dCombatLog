package com.daniel.combatlog.event.impl;

import com.comphenix.protocol.wrappers.BlockPosition;
import com.daniel.combatlog.event.CombatLogEventWrapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Player;

@Getter
@AllArgsConstructor
public class BarrierBreakEvent extends CombatLogEventWrapper {

    private final Player player;
    private BlockPosition blockPosition;

}
