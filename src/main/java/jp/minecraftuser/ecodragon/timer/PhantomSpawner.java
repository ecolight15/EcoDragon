package jp.minecraftuser.ecodragon.timer;

import jp.minecraftuser.ecoframework.PluginFrame;
import jp.minecraftuser.ecodragon.listener.PowerDragonListener;
import jp.minecraftuser.ecodragon.listener.RankingListener;
import jp.minecraftuser.ecoframework.TimerFrame;

import static jp.minecraftuser.ecoframework.Utl.sendPluginMessage;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
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
                        int x = Math.abs(loc.getBlockX());
                        int z = Math.abs(loc.getBlockZ());
                        // エンドラ戦以外の場合はENDの空白地帯だけ出現させる
                        // if ((!lisn.isRanking()) && ((x > 550) || (z > 550) || ((x < 250) && (z < 250)))) {
                        //     continue;
                        // }
                        // エンドシティの飛行禁止に伴い下手に眷属を出さないようエンドラ戦外は完全に対象外にする
                        if (!lisn.isRanking()) {
                            continue;
                        }

                        loc.setY(loc.getY() - 4);
                        if (loc.getBlock().getType() != Material.AIR) {
                            loc.setY(loc.getY() + 8);
                            if (loc.getBlock().getType() != Material.AIR) {
                                loc.setY(loc.getY() - 4);
                            }
                        }
                        Phantom phantom;
                        if (lisn.isRanking()) {
                            phantom = power.spawnPhantom(p, loc, 600, 5);
                        } else {
                            phantom = power.spawnPhantom(p, loc, 600, 0);
                        }
                        new PhantomBooster(plg, phantom, p).runTaskTimer(plg, 40, 5);
                        for (Entity ent : phantom.getPassengers()) {
                            if (ent instanceof Stray) {
                                if (lisn.isRanking()) {
                                    new StrayBooster(plg, (Stray) ent, p).runTaskTimer(plg, 40, 10);
                                } else {
                                    new StrayBooster(plg, (Stray) ent, p).runTaskTimer(plg, 40, 30);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
