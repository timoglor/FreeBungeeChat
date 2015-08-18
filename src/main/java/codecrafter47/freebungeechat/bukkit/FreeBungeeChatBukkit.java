/*
 * Copyright (C) 2014 Florian Stober
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package codecrafter47.freebungeechat.bukkit;

import lombok.SneakyThrows;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Level;

import me.timoglor.killScore.KillScore;

/**
 * @author Florian Stober
 */
public class FreeBungeeChatBukkit extends JavaPlugin implements Listener {

    VaultHook vaultHook = null;
    Plugin killScore = null;

    @Override
    public void onEnable() {

        getServer().getMessenger().registerOutgoingPluginChannel(this,
                Constants.channel);
        getServer().getMessenger().registerIncomingPluginChannel(this,
                Constants.channel, new PluginMessageListener() {

                    @Override
                    @SneakyThrows
                    public void onPluginMessageReceived(String string,
                                                        Player player, byte[] bytes) {
                        DataInputStream in = new DataInputStream(
                                new ByteArrayInputStream(bytes));

                        String subchannel = null;
						try {
							subchannel = in.readUTF();
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
                        if (subchannel.equalsIgnoreCase(Constants.subchannel_chatMsg)) {
                            String text = null;
							try {
								text = in.readUTF();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
                            String prefix = null;
							try {
								prefix = in.readUTF();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
                            int id = 0;
							try {
								id = in.readInt();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
                            boolean allowBBCode = false;
							try {
								allowBBCode = in.readBoolean();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
                            try {
								processChatMessage(player, text, prefix, id, allowBBCode);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
                        }
                        if (subchannel.equalsIgnoreCase(Constants.subchannel_playSound)) {
                            try {
								player.playSound(player.getLocation(), Sound.valueOf(in.readUTF()), 5, 1);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
                        }

                    }
                });
        getServer().getPluginManager().registerEvents(this, this);

        // check for vault hook
        Plugin vault = getServer().getPluginManager().getPlugin("Vault");
        if (vault != null) {
            getLogger().info("hooked Vault");
            vaultHook = new VaultHook(this);
        }
        killScore = getServer().getPluginManager().getPlugin("KillScore");
        if (killScore != null) {
            getLogger().info("Found KillScore.");
        }
    }

    @SneakyThrows
    private void processChatMessage(Player player, String text, String prefix, int id, boolean allowBBCode) throws IOException {
        if (vaultHook != null) {
            vaultHook.refresh();
        }
        if (vaultHook != null && text.contains("%" + prefix + "group%")) {
            text = text.replace("%" + prefix + "group%", wrapVariable(vaultHook.getGroup(player), allowBBCode));
        }
        if (vaultHook != null && text.contains("%" + prefix + "prefix%")) {
            text = text.replace("%" + prefix + "prefix%", wrapVariable(vaultHook.getPrefix(player), allowBBCode));
        }
        if (vaultHook != null && text.contains("%" + prefix + "prefixcolor%")) {
            String prefix1 = vaultHook.getPrefix(player);
            text = text.replace("%" + prefix + "prefixcolor%", wrapVariable(prefix1.substring(0, Math.min(2, prefix1.length())), allowBBCode));
        }
        if (vaultHook != null && text.contains("%" + prefix + "suffix%")) {
            text = text.replace("%" + prefix + "suffix%", wrapVariable(vaultHook.getSuffix(player), allowBBCode));
        }
        if (vaultHook != null && text.contains("%" + prefix + "balance%")) {
            text = text.replace("%" + prefix + "balance%", wrapVariable(vaultHook.getBalance(player), allowBBCode));
        }
        if (vaultHook != null && text.contains("%" + prefix + "currency%")) {
            text = text.replace("%" + prefix + "currency%", wrapVariable(vaultHook.getCurrencyName(), allowBBCode));
        }
        if (vaultHook != null && text.contains("%" + prefix + "currencyPl%")) {
            text = text.replace("%" + prefix + "currencyPl%", wrapVariable(vaultHook.getCurrencyNamePl(), allowBBCode));
        }
        if (text.contains("%" + prefix + "tabName%")) {
            text = text.replace("%" + prefix + "tabName%", wrapVariable(player.getPlayerListName(), allowBBCode));
        }
        if (text.contains("%" + prefix + "displayName%")) {
            text = text.replace("%" + prefix + "displayName%", wrapVariable(player.getDisplayName(), allowBBCode));
        }
        if (text.contains("%" + prefix + "world%")) {
            text = text.replace("%" + prefix + "world%", wrapVariable(player.getWorld().getName(), allowBBCode));
        }
        if (text.contains("%" + prefix + "health%")) {
            text = text.replace("%" + prefix + "health%", wrapVariable(Double.toString(player.getHealth()), allowBBCode));
        }
        if (text.contains("%" + prefix + "level%")) {
            text = text.replace("%" + prefix + "level%", wrapVariable(Integer.toString(player.getLevel()), allowBBCode));
        }
        if (killScore != null && text.contains("%" + prefix + "killScorePrefix%")) {
        	String uuid = player.getUniqueId().toString();
            text = text.replace("%" + prefix + "killScorePrefix%", wrapVariable(KillScore.getPrefix(uuid) , allowBBCode));
        }
        if (killScore == null && text.contains("%" + prefix + "killScorePrefix%")) {
            text = text.replace("%" + prefix + "killScorePrefix%", wrapVariable("" , allowBBCode));
        }
        	
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        DataOutputStream outputStream1 = new DataOutputStream(outputStream);
        outputStream1.writeUTF(Constants.subchannel_chatMsg);
        outputStream1.writeInt(id);
        outputStream1.writeUTF(text);
        outputStream1.flush();
        outputStream1.close();
        player.sendPluginMessage(this, Constants.channel, outputStream.toByteArray());
    }

    public String wrapVariable(String variable, boolean allowBBCode) {
        if (allowBBCode) {
            return variable;
        } else {
            return "[nobbcode]" + variable + "[/nobbcode]";
        }
    }
}
