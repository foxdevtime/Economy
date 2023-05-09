package com.diseenterprise.diseeconomy;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class DiseEcoCommands implements CommandExecutor {
    private Economy economy;
    private DiseEconomy plugin;
    private BalanceManager manager;

    public DiseEcoCommands(DiseEconomy plugin) {
        this.plugin = plugin;
        this.economy = economy;
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            // Если не указаны аргументы, то выводим список всех подкоманд
            sender.sendMessage(ChatColor.GREEN + "=== Список подкоманд /eco ===");
            sender.sendMessage(ChatColor.YELLOW + "/eco balance | balance <игрок>" + ChatColor.WHITE + " - Показать баланс игрока");
            sender.sendMessage(ChatColor.YELLOW + "/eco pay <игрок> <валюта> <сумма>" + ChatColor.WHITE + " - Перевести деньги другому игроку");
            sender.sendMessage(ChatColor.YELLOW + "/eco add <игрок> <валюта> <сумма>" + ChatColor.WHITE + " - Установить баланс игрока");
            return true;
        }
        if (args[0].equalsIgnoreCase("balance")) {
            if (args.length == 2) {
                String playerName = args[1];
                Player player = Bukkit.getPlayer(playerName);
                if (player == null) {
                    String noUser = plugin.getMessage("noUser");
                    noUser = noUser.replace("%playerName%", playerName);
                    sender.sendMessage(noUser);
                    return true;
                }
                UUID uuid = player.getUniqueId();
                File file = new File(plugin.getDataFolder() + "/users/" + uuid + ".yml");
                if (!file.exists()) {
                    String noTargBank = plugin.getMessage("noTargBank");
                    sender.sendMessage(noTargBank);
                    return true;
                }
                YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                double rubs = config.getDouble("balances.rubles");
                double cons = config.getDouble("balances.coins");
                String rubles = String.valueOf(rubs);
                String coins = String.valueOf(cons);
                String sucOtherBal = plugin.getMessage("sucOtherBal");
                sucOtherBal = sucOtherBal.replace("%playerName%", playerName);
                sucOtherBal = sucOtherBal.replace("%rubs%", rubles);
                sucOtherBal = sucOtherBal.replace("%cons%", coins);
                sender.sendMessage(sucOtherBal);
            } else {
                Player player = (Player) sender;
                UUID uuid = player.getUniqueId();
                File file = new File(plugin.getDataFolder() + "/users/" + uuid + ".yml");
                if (!file.exists()) {
                    String noUserBank = plugin.getMessage("noUserBank");
                    sender.sendMessage(noUserBank);
                    return true;
                }
                YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                double rubs = config.getDouble("balances.rubles");
                double cons = config.getDouble("balances.coins");
                String rubles = String.valueOf(rubs);
                String coins = String.valueOf(cons);
                String sucOwnerBal = plugin.getMessage("sucOwnerBal");
                sucOwnerBal = sucOwnerBal.replace("%playerName%", player.getName());
                sucOwnerBal = sucOwnerBal.replace("%rubs%", rubles);
                sucOwnerBal = sucOwnerBal.replace("%cons%", coins);
                sender.sendMessage(sucOwnerBal);
                return true;
            }
        } else {
            String helpBal = plugin.getMessage("helpBal");
            sender.sendMessage(helpBal);
        }
        if (args[0].equalsIgnoreCase("pay")) {
            String targetName = args[1];
            Player target = Bukkit.getPlayer(targetName);
            if (target == null) {
                String noUser = plugin.getMessage("noUser");
                noUser = noUser.replace("%playerName%", targetName);
                sender.sendMessage(noUser);
                return true;
            }
            UUID targetUUID = target.getUniqueId();
            File fileTarget = new File(plugin.getDataFolder() + "/users/" + targetUUID + ".yml");
            if (!fileTarget.exists()) {
                String noUserBank = plugin.getMessage("noTargBank");
                sender.sendMessage(noUserBank);
                return true;
            }
            YamlConfiguration configTarget = YamlConfiguration.loadConfiguration(fileTarget);
            String currTarget = args[2];
            if (!currTarget.equals("rubles") && !currTarget.equals("coins")) {
                String errorTypes = plugin.getMessage("errorTypes");
                sender.sendMessage(errorTypes);
                return true;
            }
            double currencyTarget = configTarget.getDouble("balances." + currTarget);
            Player player = (Player) sender;
            UUID playerUUID = player.getUniqueId();
            File filePlayer = new File(plugin.getDataFolder() + "/users/" + playerUUID + ".yml");
            if (!filePlayer.exists()) {
                String noUserBank = plugin.getMessage("noUserBank");
                sender.sendMessage(noUserBank);
                return true;
            }
            YamlConfiguration configPlayer = YamlConfiguration.loadConfiguration(filePlayer);
            String currPlayer = args[2];
            if (!currPlayer.equals("rubles") && !currPlayer.equals("coins")) {
                String errorTypes = plugin.getMessage("errorTypes");
                sender.sendMessage(errorTypes);
                return true;
            }
            double currencyPlayer = configPlayer.getDouble("balances." + currPlayer);
            int sum = Integer.parseInt(args[3]);
            if (sum <= 0) {
                String errorCount = plugin.getMessage("errorCount");
                sender.sendMessage(errorCount);
                return true;
            }
            if (currencyPlayer >= sum) {
                // Увеличиваем баланс целевого игрока на указанную сумму
                currencyTarget += sum;
                // Уменьшаем баланс отправителя на указанную сумму
                currencyPlayer -= sum;
                // Сохраняем изменения в файлах
                configTarget.set("balances." + currTarget, currencyTarget);
                configPlayer.set("balances." + currPlayer, currencyPlayer);
                try {
                    configTarget.save(fileTarget);
                    configPlayer.save(filePlayer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // Отправляем сообщения об успешном изменении баланса
                String playerName = player.getName();
                String count = String.valueOf(sum);
                String sucOtherPay = plugin.getMessage("sucOtherPay");
                sucOtherPay = sucOtherPay.replace("%playerName%", playerName);
                sucOtherPay = sucOtherPay.replace("%count%", count);
                sucOtherPay = sucOtherPay.replace("%currPlayer%", currPlayer);
                target.sendMessage(sucOtherPay);
                String sucOwnerPay = plugin.getMessage("sucOwnerPay");
                sucOwnerPay = sucOwnerPay.replace("%count%", count);
                sucOwnerPay = sucOwnerPay.replace("%currPlayer%", currPlayer);
                sucOwnerPay = sucOwnerPay.replace("%targetName%", targetName);
                player.sendMessage(sucOwnerPay);
                return true;
            } else {
                String notEnougth = plugin.getMessage("notEnougth");
                player.sendMessage(notEnougth);
            }
            return true;
        } else {
            String helpPay = plugin.getMessage("helpPay");
            sender.sendMessage(helpPay);
        }
        if (args[0].equalsIgnoreCase("add")) {
            String targ = args[1];
            Player target = Bukkit.getPlayer(targ);
            if (target == null) {
                String noUser = plugin.getMessage("noUser");
                noUser = noUser.replace("%playerName%", target.getName());
                sender.sendMessage(noUser);
                return true;
            }
            UUID targetUUID = target.getUniqueId();
            File fileTarget = new File(plugin.getDataFolder() + "/users/" + targetUUID + ".yml");
            if (!fileTarget.exists()) {
                String noUserBank = plugin.getMessage("noTargBank");
                sender.sendMessage(noUserBank);
                return true;
            }
            YamlConfiguration configTarget = YamlConfiguration.loadConfiguration(fileTarget);
            String currTarget = args[2];
            if (!currTarget.equals("rubles") && !currTarget.equals("coins")) {
                String errorTypes = plugin.getMessage("errorTypes");
                sender.sendMessage(errorTypes);
                return true;
            }
            double currencyTarget = configTarget.getDouble("balances." + currTarget);
            int sum = Integer.parseInt(args[3]);
            if (sum <= 0) {
                String errorCount = plugin.getMessage("errorCount");
                sender.sendMessage(errorCount);
                return true;
            }
            currencyTarget += sum;
            configTarget.set("balances." + currTarget, currencyTarget);
            try {
                configTarget.save(fileTarget);
            } catch (IOException e) {
                e.printStackTrace();
            }
            String targetName = target.getName();
            String count = String.valueOf(sum);
            String sucOtherAdd = plugin.getMessage("sucOtherAdd");
            sucOtherAdd = sucOtherAdd.replace("%count%", count);
            sucOtherAdd = sucOtherAdd.replace("%currTarget%", currTarget);
            target.sendMessage(sucOtherAdd);
            String sucOwnerAdd = plugin.getMessage("sucOwnerAdd");
            sucOwnerAdd = sucOwnerAdd.replace("%targetName%", targetName);
            sucOwnerAdd = sucOwnerAdd.replace("%count%", count);
            sucOwnerAdd = sucOwnerAdd.replace("%currTarget%", currTarget);
            sender.sendMessage(sucOwnerAdd);
            return true;
        } else {
            String helpAdd = plugin.getMessage("helpAdd");
            sender.sendMessage(helpAdd);
        }
        return true;
    }
}

