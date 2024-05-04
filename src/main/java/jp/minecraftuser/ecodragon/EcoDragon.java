
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

/**
 * エンダードラゴン改造プラグインメインクラス
 * @author ecolight
 */
public class EcoDragon extends PluginFrame {
    
    PhantomSpawner timer;
    SpaceBoundary space;
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
    public void onDisable()
    {
        timer.cancel();
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
}
