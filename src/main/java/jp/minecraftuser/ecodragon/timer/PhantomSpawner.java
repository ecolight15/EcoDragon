package jp.minecraftuser.ecodragon.timer;

import jp.minecraftuser.ecoframework.PluginFrame;
import jp.minecraftuser.ecodragon.listener.PowerDragonListener;
import jp.minecraftuser.ecodragon.listener.RankingListener;
import jp.minecraftuser.ecoframework.TimerFrame;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.Player;
import org.bukkit.entity.Stray;


public class PhantomSpawner extends TimerFrame {
    public PhantomSpawner(PluginFrame plg) {
        super(plg, "phantom");
    }
    @Override
    public void run()
    {
        // 定期動作
        PowerDragonListener power = (PowerDragonListener) plg.getPluginListener("dragon");
        for (Player p : plg.getServer().getOnlinePlayers()) {
            // 指定ワールドがエンドラランキング対象かチェック
            World w = p.getWorld();
            if (w.getName().toLowerCase().startsWith(conf.getString("worldprefix").toLowerCase())) {
                // グライド中のプレイヤーの近距離にファントムを召喚する
                RankingListener lisn = (RankingListener) plg.getPluginListener("ranking");
                if ((!lisn.isRanking()) || (power.hp < 60)) {
                    if (p.isGliding()) {
                        Location loc = p.getLocation();
                        loc.setY(loc.getY() - 4);
                        if (loc.getBlock().getType() != Material.AIR) {
                            loc.setY(loc.getY() + 8);
                            if (loc.getBlock().getType() != Material.AIR) {
                                loc.setY(loc.getY() - 4);
                            }
                        }
                        Phantom phantom = power.spawnPhantom(p, loc, 600);
                        new PhantomBooster(plg, phantom, p).runTaskTimer(plg, 40, 5);
                        for (Entity ent : phantom.getPassengers()) {
                            if (ent instanceof Stray) {
                                new StrayBooster(plg, (Stray) ent, p).runTaskTimer(plg, 40, 10);
                            }
                        }
                    }
                }
            }
        }
    }
}
