package com.daniel.combatlog.utils;

import com.daniel.combatlog.CombatLog;
import com.daniel.combatlog.api.ReflectionAPI;
import com.daniel.combatlog.api.Version;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class ActionBar {

    private static Method a;
    private static Object typeMessage;
    private static Constructor<?> chatConstructor;

    static {
        load();
    }

    public static void sendActionBar(Player player, String message) {
        try {
            Object chatMessage = a.invoke(null, "{\"text\":\"" + message + "\"}");
            Object packet = chatConstructor.newInstance(chatMessage, typeMessage);
            ReflectionAPI.sendPacket(player, packet);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void broadcastActionBar(String message) {
        try
        {
            Object chatMessage = a.invoke(null, "{\"text\":\"" + message + "\"}");
            Object packet = chatConstructor.newInstance(chatMessage, typeMessage);
            for (Player player : Bukkit.getOnlinePlayers()) {
                ReflectionAPI.sendPacket(player, packet);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    static void load() {
        try
        {
            Class<?> typeMessageClass;
            Class<?> icbc = ReflectionAPI.getNMSClass("IChatBaseComponent");
            Class<?> ppoc = ReflectionAPI.getNMSClass("PacketPlayOutChat");

            if (icbc.getDeclaredClasses().length > 0) {
                a = icbc.getDeclaredClasses()[0].getMethod("a", String.class);
            } else {
                a = ReflectionAPI.getNMSClass("ChatSerializer").getMethod("a", String.class);
            }

            if (CombatLog.getVersion() == Version.v1_12) {
                typeMessageClass = ReflectionAPI.getNMSClass("ChatMessageType");
                typeMessage = typeMessageClass.getEnumConstants()[2];
            } else {
                typeMessageClass = byte.class;
                typeMessage = (byte) 2;
            }

            chatConstructor = ppoc.getConstructor(icbc,  typeMessageClass);
        }
        catch (Throwable e) {}
    }
}
