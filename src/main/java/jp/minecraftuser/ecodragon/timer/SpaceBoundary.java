package jp.minecraftuser.ecodragon.timer;

import jp.minecraftuser.ecoframework.PluginFrame;
import jp.minecraftuser.ecoframework.TimerFrame;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class SpaceBoundary extends TimerFrame {
    public SpaceBoundary(PluginFrame plg) {
        super(plg, "space");
    }
    @Override
    public void run()
    {
        // 定期動作
        for (Player p : plg.getServer().getOnlinePlayers()) {
            World w = p.getWorld();
            // 指定ワールドがエンドラランキング対象かチェック
            if (w.getName().toLowerCase().startsWith(conf.getString("worldprefix").toLowerCase())) {
                Location loc = p.getLocation();
                int x = Math.abs(loc.getBlockX());
                int z = Math.abs(loc.getBlockZ());
                if (p.isGliding()) {
                    if ((x >= 600) || (z >= 600)) {
                        p.setGliding(false);
                        p.sendMessage("暫定でエンドシティ内でのエリトラでの飛行は抑止されています");
                    }
                    if ((x < 600) && (z < 600) && (!((x < 200) && (z < 200)))) {
                        p.sendMessage("当該空間のエリトラでの飛行は禁止されています");
                    }
                    if ((x < 500) && (z < 500) && (!((x < 300) && (z < 300)))) {
                        p.setGliding(false);
                        p.teleport(p.getWorld().getSpawnLocation());
                        w.strikeLightningEffect(p.getLocation());
                        p.damage(p.getHealth() - 0.5);
                    }
                } else {
                    if ((x < 450) && (z < 450) && (!((x < 350) && (z < 350)))) {
                        p.setGliding(false);
                        p.teleport(p.getWorld().getSpawnLocation());
                        w.strikeLightningEffect(p.getLocation());
                        p.damage(p.getHealth() - 0.5);
                    }
                }
            }
        }
    }
}
