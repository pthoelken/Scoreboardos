package rien.bijl.Scoreboard.r;

import org.apache.commons.lang3.StringEscapeUtils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import rien.bijl.Scoreboard.r.board.App;
import rien.bijl.Scoreboard.r.util.ConfigControl;

import java.util.HashMap;

/**
 * Created by Rien on 21-10-2018.
 */
public class Main extends JavaPlugin {

    public static Scoreboard empty;

    public static HashMap<String, App> apps = new HashMap<>();

    @Override
    public void onEnable() {
        init();
    }

    private void init()
    {
        Session.plugin = this;
        Session.isUpToDate("14754");
        ConfigControl.get().createDataFiles();
        empty = getServer().getScoreboardManager().getNewScoreboard();

        autoloadDependencies();
        setupCommands();
        loadBoards();

        new Metrics(this);

        finished();
    }

    private void autoloadDependencies()
    {
        for(String dependency : Session.dependencies)
            if(Bukkit.getPluginManager().isPluginEnabled(dependency))
                Session.enabled_dependencies.add(dependency);
    }

    private void setupCommands()
    {
        getCommand("sb").setExecutor(new CommandManager());
    }

    public static void loadBoards()
    {
        newApp("board", true); // Default board

        for(String s : ConfigControl.get().gc("settings").getStringList("enabled-boards"))
        {
            Session.plugin.getLogger().info("Attempting to start app-creator for: " + s);
            if(ConfigControl.get().gc("settings").isConfigurationSection(s)) newApp(s, false);
            else Session.plugin.getLogger().severe("Tried enabling board '" + s + "', but it does not exist!");
        }
    }

    public static void disolveBoards()
    {
        for(App app : apps.values())
            app.cancel();
        apps.clear();
    }

    public static void newApp(String board, boolean isdefault)
    {
        App app = new App(board);
        if(ConfigControl.get().gc("settings").getBoolean("settings.safe-mode"))
            app.runTaskTimer(Session.plugin, 1L, 1L);
        else app.runTaskTimerAsynchronously(Session.plugin, 1L, 1L);
        apps.put(board, app);
        Session.plugin.getLogger().info("Loaded app handler for board: " + board);
        app.isdefault = isdefault;
    }

    public static void finished()
    {
        System.out.println("Scoreboard is online!. Scoreboard version: " + Session.plugin.getDescription().getVersion() +
        " (" + (Session.isuptodate ? "UP TO DATE" : "OUTDATED") + ")");

        System.out.println();
    }

}
