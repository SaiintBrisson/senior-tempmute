package me.saiintbrisson.tempmute.mute;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.RequiredArgsConstructor;
import me.saiintbrisson.tempmute.MutePlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

@RequiredArgsConstructor
public class MuteController {

    private final MutePlugin plugin;
    private Cache<UUID, MuteModel> occurrenceCache;

    {
        occurrenceCache = CacheBuilder.newBuilder().concurrencyLevel(2).build();
    }

    public void reloadAll() {
        occurrenceCache.cleanUp();

        for(Player player : Bukkit.getOnlinePlayers()) {
            load(player.getUniqueId());
        }
    }

    public MuteModel select(String name) {
        final Player player = Bukkit.getPlayerExact(name);
        final MuteModel select = player == null ? null : select(player.getUniqueId());

        return select == null
            ? plugin.getRepository().fetch(name)
            : select;
    }

    public MuteModel select(UUID id) {
        return occurrenceCache.getIfPresent(id);
    }

    public MuteModel selectOrSearch(UUID id) {
        final MuteModel select = select(id);
        if(select != null) return select;

        return plugin.getRepository().fetch(id);
    }

    public boolean insertAndSave(MuteModel occurrence) {
        occurrenceCache.put(occurrence.getId(), occurrence);
        return plugin.getRepository().insert(occurrence);
    }

    public void remove(UUID id) {
        occurrenceCache.invalidate(id);
    }

    public void load(UUID id) {
        if(select(id) != null) return;

        final MuteModel occurrence = plugin.getRepository().fetch(id);
        if(occurrence == null) return;

        occurrenceCache.put(id, occurrence);
    }

    public MuteModel verifyMute(UUID id) {
        final MuteModel muteModel = selectOrSearch(id);
        if(muteModel == null) {
            return null;
        }

        if(!muteModel.isTemporary()) return muteModel;

        if(!muteModel.isActive()) {
            plugin.getRepository().delete(id);
            return null;
        }

        return muteModel;
    }

    public boolean checkPlayer(Player player) {
        final MuteModel muteModel = plugin.getController().verifyMute(player.getUniqueId());
        if(muteModel == null) return false;

        final String message = plugin.getMessage(
            muteModel.isTemporary()
                ? "temporaryMute"
                : "permanentMute"
        );
        player.sendMessage(muteModel.parseMessage(message));

        return true;
    }

}
