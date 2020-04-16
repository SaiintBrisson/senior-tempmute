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
            singleArgument(execution, builder);
        } else if(execution.argsCount() > 2) {
            multipleArguments(execution, builder);
        }

        processBuilder(execution, (Player) target, muteModel, builder);
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

    private void singleArgument(Execution execution, MuteModel.MuteModelBuilder builder) {
        final String arg = execution.getArg(1);
        final Long timestamp = DateUtil.convertToTimestamp(arg);

        if(timestamp == null) {
            builder.reason(arg);
        } else {
            builder.expirationDate(
                Timestamp.from(
                    Instant.ofEpochMilli(System.currentTimeMillis() + timestamp)
                )
            );
        }
    }

    private void multipleArguments(Execution execution, MuteModel.MuteModelBuilder builder) {
        long time = 0;

        int s = 0;
        for(int i = execution.argsCount() - 1; i > 0; i--) {
            final String arg = execution.getArg(i);
            if(arg == null) break;

            final Long aLong = DateUtil.convertToTimestamp(arg);
            if(aLong == null) break;

            time += aLong;
            s++;
        }

        String[] reason;
        if(time == 0) {
            reason = execution.getArgs(1, execution.argsCount());
        } else {
            reason = execution.getArgs(1, execution.argsCount() - s);
            builder.expirationDate(
                Timestamp.from(
                    Instant.ofEpochMilli(System.currentTimeMillis() + time)
                )
            );
        }

        builder.reason(reason == null || reason.length == 0 ? null : String.join(" ", reason));
    }

    private void processBuilder(Execution execution, Player target,
                                MuteModel model, MuteModel.MuteModelBuilder builder) {
        final Timestamp expirationDate = model == null ? null : model.getExpirationDate();
        model = builder.build();

        if((expirationDate != null && model.getExpirationDate() != null)
            && expirationDate.after(model.getExpirationDate())) {
            execution.sendMessage(plugin.getMessage("alreadyTemporarilyMuted"));
            return;
        }

        plugin.getController().insertAndSave(model);
        execution.sendMessage(model.parseMessage(plugin.getMessage("successfullyMuted")));

        target.sendMessage(
            model.parseMessage(plugin.getMessage(
                model.isTemporary()
                    ? "temporaryMute"
                    : "permanentMute"
            ))
        );
    }

}
