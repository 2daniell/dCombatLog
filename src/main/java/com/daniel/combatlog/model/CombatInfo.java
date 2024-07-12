package com.daniel.combatlog.model;

import com.daniel.combatlog.CombatLog;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Getter
@Setter
public class CombatInfo {

    private static final int EXPIRE_TIME = CombatLog.config().getInt("expire-time");

    private final UUID damaged;
    private UUID damager;
    private Instant instant;

    public CombatInfo(UUID damager, UUID damaged) {
        this.damager = damager;
        this.damaged = damaged;
        updateTime();
    }

    private void updateTime() {
        instant = Instant.now().plusMillis(TimeUnit.SECONDS.toMillis(EXPIRE_TIME + 1));
    }

    public void updateDamager(UUID damager) {
        if (!(damager.equals(this.damager))) this.damager = damager;
        updateTime();
    }

    public boolean isExpired() {
        return instant.isBefore(Instant.now());
    }

    public long getTimeLeft() {
        return TimeUnit.MILLISECONDS.toSeconds(instant.toEpochMilli() - System.currentTimeMillis());
    }
}
