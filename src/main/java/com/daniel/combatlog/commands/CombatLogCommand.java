package com.daniel.combatlog.commands;

import com.daniel.combatlog.CombatLog;
import com.daniel.combatlog.combat.CombatLogManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class CombatLogCommand implements CommandExecutor {

    private static final Set<UUID> set = new HashSet<>();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {

        if (args.length == 0) {
            if (sender.hasPermission("combatlog.admin")) {
                sender.sendMessage("§cUse, /combatlog comandos | Para ver os comandos disponiveis.");
            } else {
                sender.sendMessage("§cSem permissão");
            }
            return true;
        }

        if(args[0].equalsIgnoreCase("reload")) {

            if (sender.hasPermission("combatlog.reload")) {

                CombatLog.getInstance().reloadConfig();
                CombatLogManager.removeAll();

                sender.sendMessage("§eConfigurações do plugin foram redefinidas");
            } else {
                sender.sendMessage("§cSem permissão.");
            }
            return true;

        }

        if (args[0].equalsIgnoreCase("comandos")) {

            if (sender.hasPermission("combatlog.admin")) {
                String[] message = {
                        "§e/combatlog reload §f- §7Recarregar configurações",
                        "§e/combatlog staff §f- §7Desativar/Ativar combate"
                };

                sender.sendMessage(message);
            } else {
                sender.sendMessage("§cSem permissão");
            }

            return true;
        }

        if (args[0].equalsIgnoreCase("staff")) {

            if (!(sender instanceof Player)) {
                sender.sendMessage("§cComando apenas para jogadores in-game");
                return true;
            }

            Player player = (Player) sender;

            if (player.hasPermission("combatlog.admin")) {

                if (set.contains(player.getUniqueId())) {

                    String[] message = {
                            "",
                            "§eVocê desativou o modo staff com sucesso.",
                            "§7Agora você pode entrar em combate."
                    };

                    player.sendMessage(message);
                    set.remove(player.getUniqueId());
                } else {

                    String[] message = {
                            "",
                            "§eVocê ativou o modo staff com sucesso.",
                            "§7Agora você não entrará em combate."
                    };

                    player.sendMessage(message);
                    set.add(player.getUniqueId());
                }
            } else {
                player.sendMessage("§cSem permissão");
            }

            return true;
        }

        return false;
    }

    public static Set<UUID> getSet() {
        return set;
    }
}
