package me.saiintbrisson.tempmute.commands;

import lombok.AllArgsConstructor;
import me.saiintbrisson.commands.Execution;
import me.saiintbrisson.commands.annotations.Command;
import me.saiintbrisson.tempmute.MutePlugin;
import me.saiintbrisson.tempmute.mute.MuteModel;
import me.saiintbrisson.tempmute.utils.DateUtil;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.sql.Timestamp;
import java.time.Instant;

@AllArgsConstructor
public class MuteCommand {

    private MutePlugin plugin;

    @Command(
        name = "mute",
        aliases = {"silence", "tempmute"},
        permission = "mute.command",

        usage = "mute <player> [reason] [time]",
        description = "Mutes a player for a time period or permanently",
        async = true
    )
    public void mute(Execution execution, OfflinePlayer target) {
        if(!target.isOnline()) {
            execution.sendMessage(plugin.getMessage("offlinePlayer"));
            return;
        }

        if(execution.isPlayer() && target == execution.getPlayer()) {
            execution.sendMessage(plugin.getMessage("cannotMuteSelf"));
            return;
        }

        MuteModel muteModel = plugin.getController().selectOrSearch(target.getUniqueId());
        if(muteModel != null && !muteModel.isTemporary()) {
            execution.sendMessage(plugin.getMessage("alreadyPermanentlyMuted"));
            return;
        }

        final MuteModel.MuteModelBuilder builder = MuteModel.builder()
            .id(target.getUniqueId())
            .name(target.getName())
            .author(execution.getSender().getName())
            .muteDate(Timestamp.from(Instant.now()));

        if(execution.argsCount() == 2) {
            final String arg = execution.getArg(1);
            final Timestamp timestamp = DateUtil.convertToTimestamp(
                arg
            );

            if(timestamp == null) {
                builder.reason(arg);
            } else {
                builder.expirationDate(timestamp);
            }
        } else if(execution.argsCount() > 2) {
            final Timestamp timestamp = DateUtil.convertToTimestamp(
                execution.getArg(execution.argsCount() - 1)
            );

            String[] reason;
            if(timestamp == null) {
                reason = execution.getArgs(1, execution.argsCount());
            } else {
                reason = execution.getArgs(1, execution.argsCount() - 1);
                builder.expirationDate(timestamp);
            }

            builder.reason(String.join(" ", reason));
        }

        final Timestamp expirationDate = muteModel == null ? null : muteModel.getExpirationDate();
        muteModel = builder.build();

        if(expirationDate != null && expirationDate.after(muteModel.getExpirationDate())) {
            execution.sendMessage(plugin.getMessage("alreadyTemporarilyMuted"));
            return;
        }

        plugin.getController().insertAndSave(muteModel);
        execution.sendMessage(muteModel.parseMessage(plugin.getMessage("successfullyMuted")));

        Player player = ((Player) target);
        if(muteModel.isTemporary()) {
            player.sendMessage(
                muteModel.parseMessage(plugin.getMessage("temporaryMute"))
            );
        } else {
            player.sendMessage(
                muteModel.parseMessage(plugin.getMessage("permanentMute"))
            );
        }
    }

    @Command(name = "mute.?", aliases = "help")
    public void muteHelp(Execution execution) {
        execution.sendMessage(new String[]{
            " ",
            " §a§lMUTE - HELP",
            " ",
            " §f/mute help §8- §7Shows this message;",
            " §f/mute rl §8- §7Reloads the config;",
            " §f/mute <player> [reason] [time] §8- §7Mutes a player permanently or temporary;",
            " ",
            " §7Available timestamps: s (seconds), m (minutes), h (hours), d (days), w (weeks) and y (years).",
            " "
        });
    }

    @Command(
        name = "mute.rl",
        description = "Reloads the config file"
    )
    public void muteReload(Execution execution) {
        plugin.reload();
        execution.sendMessage(plugin.getMessage("reloaded"));
    }

}
