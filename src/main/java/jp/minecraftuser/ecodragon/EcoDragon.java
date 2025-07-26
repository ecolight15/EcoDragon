package jp.minecraftuser.ecodragon;

import jp.minecraftuser.ecodragon.command.EcdBookCommand;
import jp.minecraftuser.ecodragon.command.EcdClearCommand;
import jp.minecraftuser.ecodragon.command.EcdCommand;
import jp.minecraftuser.ecodragon.command.EcdEndCommand;
import jp.minecraftuser.ecodragon.command.EcdPvPCommand;
import jp.minecraftuser.ecodragon.command.EcdRankCommand;
import jp.minecraftuser.ecodragon.command.EcdReloadCommand;
import jp.minecraftuser.ecodragon.command.EcdStartCommand;
import jp.minecraftuser.ecodragon.config.CertificateConfig;
import jp.minecraftuser.ecodragon.config.EcoDragonConfig;
import jp.minecraftuser.ecodragon.listener.DragonEggListener;
import jp.minecraftuser.ecodragon.listener.LightWeightListener;
import jp.minecraftuser.ecodragon.listener.PowerDragonListener;
import jp.minecraftuser.ecodragon.listener.RankingListener;
import jp.minecraftuser.ecodragon.timer.PhantomSpawner;
import jp.minecraftuser.ecodragon.timer.SpaceBoundary;
import jp.minecraftuser.ecoframework.PluginFrame;
import jp.minecraftuser.ecoframework.CommandFrame;
import jp.minecraftuser.ecoframework.ConfigFrame;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;

/**
 * エンダードラゴン改造プラグインメインクラス
 * @author ecolight
 */
public class EcoDragon extends PluginFrame {
    
    PhantomSpawner timer;
    SpaceBoundary space;
    private static final String TEMP_STOPWORLD_KEY = "tempStopworld";

    /**
     * プラグイン起動
     */
    @Override
    public void onEnable() {
        initialize();
        timer = new PhantomSpawner(this);
        timer.runTaskTimer(this, 200, 200);
        space = new SpaceBoundary(this);
        space.runTaskTimer(this, 0, 5);
    }

    /**
     * プラグイン停止
     */
    @Override
    public void onDisable() {
        if (timer != null) timer.cancel();
        moveTempStopWorldsToStopWorld();
        saveTempStopWorlds();
        disable();
    }

    /**
     * プラグインコンフィグ初期化
     */
    @Override
    public void initializeConfig() {
        // デフォルトコンフィグ
        ConfigFrame conf = new EcoDragonConfig(this);
        conf.registerString("worldprefix");
        conf.registerInt("roundmax");
        conf.registerInt("mob-exp-multiplier");
        conf.registerInt("crystal-break-penalty");
        conf.registerInt("crystal-break-bonus");
        conf.registerInt("crystal-place-bonus");
        conf.registerInt("fishing-trash");
        conf.registerInt("fishing-bonus");
        conf.registerInt("fishing-salmon-bonus");
        conf.registerInt("fishing-clownfish-bonus");
        conf.registerInt("fishing-pufferfish-bonus");
        conf.registerInt("fishing-enchantbook-bonus");
        conf.registerInt("fishing-nametag-bonus");
        conf.registerInt("fishing-shell-bonus");
        conf.registerInt("fishing-saddle-bonus");
        conf.registerInt("before-world-border");
        conf.registerArrayString("stopworld");
        registerPluginConfig(conf);
        // 賞状
        conf = new CertificateConfig(this, "certificate.yml", "certificate");
        conf.registerString("title");
        conf.registerString("name");
        conf.registerString("author");
        conf.registerArrayString("pages");
        registerPluginConfig(conf);
    }

    /**
     * プラグインコマンド初期化
     */
    @Override
    public void initializeCommand() {
        CommandFrame cmd = new EcdCommand(this, "ecd");
        cmd.addCommand(new EcdReloadCommand(this, "reload"));
        cmd.addCommand(new EcdRankCommand(this, "rank"));
        cmd.addCommand(new EcdStartCommand(this, "start"));
        cmd.addCommand(new EcdEndCommand(this, "end"));
        cmd.addCommand(new EcdClearCommand(this, "clear"));
        cmd.addCommand(new EcdPvPCommand(this, "pvp"));
        cmd.addCommand(new EcdBookCommand(this, "book"));
        registerPluginCommand(cmd);
    }

    /**
     * プラグインリスナ初期化
     */
    @Override
    public void initializeListener() {
        registerPluginListener(new RankingListener(this, "ranking"));
        registerPluginListener(new PowerDragonListener(this, "dragon"));
        registerPluginListener(new LightWeightListener(this, "light"));
        registerPluginListener(new DragonEggListener(this, "egg"));
    }

    private void saveTempStopWorlds() {
        ConfigFrame config = getDefaultConfig();
        String worldPrefix = config.getString("worldprefix");
        List<String> tempStopWorlds = config.getArrayList(TEMP_STOPWORLD_KEY);

        for (World world : Bukkit.getWorlds()) {
            if (world.getName().startsWith(worldPrefix) && !tempStopWorlds.contains(world.getName())) {
                tempStopWorlds.add(world.getName());
            }
        }

        config.getConf().set(TEMP_STOPWORLD_KEY, tempStopWorlds);
        config.saveConfig();
        config.reload();
    }

    private void moveTempStopWorldsToStopWorld() {
        ConfigFrame config = getDefaultConfig();
        List<String> tempStopWorlds = config.getArrayList(TEMP_STOPWORLD_KEY);
        List<String> stopWorlds = config.getArrayList("stopworld");

        for (String world : tempStopWorlds) {
            if (!stopWorlds.contains(world)) {
                stopWorlds.add(world);
            }
        }

        config.getConf().set("stopworld", stopWorlds);
        config.getConf().set(TEMP_STOPWORLD_KEY, new ArrayList<>()); // Clear tempStopworld
        config.saveConfig();
        config.reload();
    }
}
