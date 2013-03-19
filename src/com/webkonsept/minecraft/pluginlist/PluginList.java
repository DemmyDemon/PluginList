package com.webkonsept.minecraft.pluginlist;

import com.webkonsept.minecraft.boilerplate.JoinableArrayList;
import com.webkonsept.minecraft.boilerplate.KonseptPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;

public class PluginList extends KonseptPlugin {

    private FileConfiguration cfg;
    private HashSet<String> hidden = new HashSet<String>();

    @Override
    public void onEnable(){
        cfg = refreshConfig(); //NOTE: Has version check side-effect
        hidden.clear();
        hidden.addAll(cfg.getStringList("hide"));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args){
        if (command.getName().equals("pluginlist")){  // because more commands might be needed later?
            // Command mode defaults
            boolean reveal      = true;
            boolean showDetail  = false;
            boolean doReload    = false;


            // Requested detail?  reload?
            if (args.length > 0){
                if (args[0].equalsIgnoreCase("detail")){
                    showDetail = true;
                }
                else if (args[0].equalsIgnoreCase("reload")){
                    doReload = true;
                }
            }

            // If the sender is a player it'll need permission to do this stuff.
            if (sender instanceof Player){
                Player player = (Player) sender;
                verbose("Plugin list requested by "+player.getName());
                doReload = doReload ? allow(player,"pluginslist.reload") : false;
                showDetail = showDetail ? allow(player,"pluginlist.detail") : false;
                reveal = allow(player, "pluginlist.reveal");
                verboseYes("Reveal hidden",reveal);
            }

            if (doReload){
                sender.sendMessage(ChatColor.GREEN+"PluginList reloading!");
                if (sender instanceof Player){
                    getServer().getConsoleSender().sendMessage(ChatColor.RED+((Player)sender).getName()+" reloaded PluginList");
                }
                cfg = refreshConfig();
                hidden.clear();
                hidden.addAll(cfg.getStringList("hide"));
            }
            else {
                JoinableArrayList list = new JoinableArrayList();
                for (Plugin candidate : getServer().getPluginManager().getPlugins()){
                    String pluginName = candidate.getName();
                    String detail = showDetail ? " ["+candidate.getDescription().getVersion()+"]" : "";

                    if (!hidden.contains(pluginName)){
                        verbose(pluginName+" is not hidden");
                        ChatColor enabled = candidate.isEnabled() ? ChatColor.GREEN : ChatColor.RED;
                        list.add(enabled+pluginName+detail+ChatColor.RESET);
                    }
                    else if (reveal){
                        verbose(pluginName+" is hidden, but revealed");
                        ChatColor enabled = candidate.isEnabled() ? ChatColor.AQUA : ChatColor.BLUE;
                        list.add(enabled+pluginName+detail+ChatColor.RESET);
                    }
                }

                /*

                    I'll need a randomizer to insert the fake plugins in a random spot.
                    The spots can't change between command executions, however, so it has to be seeded in a static way.

                */
                Random rand = new Random((long) getServer().getPluginManager().getPlugins().length);

                for (String pluginName : cfg.getStringList("fake")){
                    ChatColor color = reveal ? ChatColor.YELLOW : ChatColor.GREEN;
                    String version = showDetail ? " [FAKE]" : "";
                    list.add(rand.nextInt(list.size()),color+pluginName+version+ChatColor.RESET);
                }
                if (showDetail){
                    sender.sendMessage(list.size()+" plugins:");
                    Object[] pluginList = list.toArray();
                    Arrays.sort(pluginList);
                    for (Object pluginName : pluginList){
                        sender.sendMessage((String)pluginName);
                    }
                }
                else {
                    sender.sendMessage("Plugins ("+list.size()+"): "+list.join(", "));
                }
            }
        }
        return true;
    }
}
