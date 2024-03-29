package jp.minecraftuser.ecodragon.timer;

import jp.minecraftuser.ecoframework.PluginFrame;
import jp.minecraftuser.ecoframework.TimerFrame;

import org.bukkit.entity.LivingEntity;


public class MobKillTimer extends TimerFrame {
    private LivingEntity ent;
    public MobKillTimer(PluginFrame plg) {
        super(plg, "mob");
    }
    public MobKillTimer(PluginFrame plg, LivingEntity ent_) {
        super(plg, "mob");
        ent = ent_;
    }
    @Override
    public void run()
    {
        // 定期動作
        ent.remove();
    }
}
