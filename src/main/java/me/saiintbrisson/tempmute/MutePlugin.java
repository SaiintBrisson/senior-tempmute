package me.saiintbrisson.tempmute;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import me.saiintbrisson.commands.CommandFrame;
import me.saiintbrisson.tempmute.commands.MuteCommand;
import me.saiintbrisson.tempmute.commands.UnmuteCommand;
import me.saiintbrisson.tempmute.listeners.PlayerListener;
import me.saiintbrisson.tempmute.mute.MuteController;
import me.saiintbrisson.tempmute.mute.Repository;
import me.saiintbrisson.tempmute.utils.SQLReader;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.List;

@Getter
public class MutePlugin extends JavaPlugin {

    private SQLReader sqlReader;
    private CommandFrame commandFrame;

    private HikariDataSource dataSource;
    private Repository repository;

    private MuteController controller;

    private List<String> blockedCommands;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        try {
            sqlReader = new SQLReader();
            sqlReader.loadFromResources();
        } catch (IOException e) {
            System.err.println("Could not load sql files from resources.");
            System.err.println("Disabling plugin...");
            e.printStackTrace();
            getPluginLoader().disablePlugin(this);
            return;
        }

        initDatabase();
        controller = new MuteController(this);

        blockedCommands = getConfig().getStringList("blockedCommands");

        registerCommands();

        registerListeners(new PlayerListener(this));
    }

    private void registerCommands() {
        commandFrame = new CommandFrame(this);
        commandFrame.registerType(OfflinePlayer.class, Bukkit::getOfflinePlayer);

        commandFrame.setLackPermMessage(getMessage("lackPermissions"));
        commandFrame.setUsageMessage(getMessage("usage"));

        commandFrame.register(
            new MuteCommand(this),
            new UnmuteCommand(this)
        );
    }

    private boolean initDatabase() {
        HikariConfig config = new HikariConfig();

        config.setJdbcUrl(getConfig().getString("database.url"));

        config.setUsername(getConfig().getString("database.username"));
        config.setPassword(getConfig().getString("database.password"));

        config.setDriverClassName(getConfig().getString("database.driver"));

        config.setMaximumPoolSize(3);

        dataSource = new HikariDataSource(config);
        repository = new Repository(this, dataSource);

        return repository.createTable();
    }

    private void registerListeners(Listener... listeners) {
        for(Listener listener : listeners) {
            getServer().getPluginManager().registerEvents(listener, this);
        }
    }

    public String getMessage(String message, String def) {
        String string = getConfig().getString(message);

        return string == null
            ? def
            : string.replace("&", "§");
    }

    public String getMessage(String message) {
        return getMessage("message." + message, null);
    }

    public void reload() {
        reloadConfig();
        dataSource.close();

        initDatabase();

        blockedCommands = getConfig().getStringList("blockedCommands");

        commandFrame.setLackPermMessage(getMessage("lackPermissions"));
        commandFrame.setUsageMessage(getMessage("usage"));

        controller.reloadAll();
    }

}
