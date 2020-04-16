package me.saiintbrisson.tempmute.commands;

import lombok.AllArgsConstructor;
import me.saiintbrisson.commands.Execution;
import me.saiintbrisson.commands.annotations.Command;
import me.saiintbrisson.tempmute.MutePlugin;
import me.saiintbrisson.tempmute.mute.MuteModel;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@AllArgsConstructor
public class UnmuteCommand {

    private MutePlugin plugin;

    @Command(
        name = "unmute",
        permission = "mute.command",

        usage = "unmute <player>",
        description = "Unmutes a muted player",
        async = true
    )
    public void unmute(Execution execution, String target) {
        final MuteModel muteModel = plugin.getController().select(target);
        if(muteModel == null) {
            execution.sendMessage(plugin.getMessage("playerNotMuted"));
            return;
        }

        plugin.getController().remove(muteModel.getId());
        plugin.getRepository().delete(muteModel.getId());
        execution.sendMessage(muteModel.parseMessage(plugin.getMessage("playerUnmuted")));

        Player player = Bukkit.getPlayerExact(target);
        if(player != null) {
            player.sendMessage(plugin.getMessage("unmuted"));
        }
    }

}
