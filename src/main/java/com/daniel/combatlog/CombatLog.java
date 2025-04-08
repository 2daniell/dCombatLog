package com.daniel.combatlog;

import br.com.ystoreplugins.product.yclans.ClanAPIHolder;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.daniel.combatlog.api.Version;
import com.daniel.combatlog.barrier.CombatBarrier;
import com.daniel.combatlog.combat.CombatLogManager;
import com.daniel.combatlog.combat.impl.CombatLogImpl;
import com.daniel.combatlog.commands.CombatLogCommand;
import com.daniel.combatlog.listener.CombatLogListener;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class CombatLog extends JavaPlugin {

    private static ProtocolManager protocol;
    private static WorldGuardPlugin worldGuard;
    private static CombatLog instance;

    private final CombatLogManager combatManager = new CombatLogImpl();

    private static Version version;

    @Override
    public void onEnable() {

        if (Bukkit.getPluginManager().getPlugin("ProtocolLib") == null) {
            System.out.println(" ");
            System.out.println("ProtocolLib não encontrado. Desativando...");
            System.out.println(" ");

            Bukkit.getPluginManager().disablePlugin(this); return;
        }

        if (Bukkit.getPluginManager().getPlugin("WorldGuard") == null) {
            System.out.println(" ");
            System.out.println("WorldGuard não encontrado. Desativando...");
            System.out.println(" ");

            Bukkit.getPluginManager().disablePlugin(this); return;
        }

        instance = this;
        version = Version.getServerVersion();

        worldGuard = (WorldGuardPlugin) Bukkit.getPluginManager().getPlugin("WorldGuard");
        protocol = ProtocolLibrary.getProtocolManager();

        saveDefaultConfig();

        register();
    }

    @Override
    public void onDisable() {
        CombatLogManager.removeAll();
    }

    private void register() {
        PluginManager pm = Bukkit.getPluginManager();
        CombatBarrier.init(this);
        pm.registerEvents(new CombatLogListener(combatManager), this);
        pm.registerEvents(new CombatBarrier(), this);

        getCommand("combatlog").setExecutor(new CombatLogCommand());
    }

    public CombatLogManager getCombatManager() {
        return combatManager;
    }

    public static FileConfiguration config() {
        return CombatLog.getInstance().getConfig();
    }

    public static Version getVersion() {
        return version;
    }

    public static ProtocolManager getProtocol() {
        return protocol;
    }

    public static WorldGuardPlugin getWorldGuard() {
        return worldGuard;
    }

    public static CombatLog getInstance() {
        return instance;
    }

    public static ClanAPIHolder getClanAPI() {
        try {
            RegisteredServiceProvider<ClanAPIHolder> provider = Bukkit.getServer().getServicesManager().getRegistration(ClanAPIHolder.class);
            return (provider == null) ? null : (ClanAPIHolder) provider.getProvider();
        } catch (Exception e) {
            return null;
        }
    }
}