package jp.minecraftuser.ecodragon.timer;

import jp.minecraftuser.ecoframework.PluginFrame;
import jp.minecraftuser.ecodragon.EcoDragonPlayerList;
import jp.minecraftuser.ecodragon.listener.PowerDragonListener;
import jp.minecraftuser.ecodragon.listener.RankingListener;
import jp.minecraftuser.ecoframework.TimerFrame;

import java.time.LocalDate;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;


public class WorldTimer extends TimerFrame {
    private static RankingListener lisn = null;
    public WorldTimer(PluginFrame plg) {
        super(plg, "world");
    }
    public WorldTimer(PluginFrame plg, RankingListener lisn_) {
        super(plg, "world");
        lisn = lisn_;
    }
    @Override
    public void run()
    {
        // 定期動作
        // エンドラ戦中のワールドにいたらちょっとポイントを加算する
        if (lisn.isRanking()) {
            PowerDragonListener listener = (PowerDragonListener) plg.getPluginListener("dragon");
            for (Player p : plg.getServer().getOnlinePlayers()) {
                if (p.getWorld().equals(lisn.getWorld())) {
                    lisn.addPoint(p, 10);

                    // グライド中のプレイヤーの近距離にファントムを召喚する
                    if (listener.hp < 60) {
                        if (p.isGliding()) {
                            Location loc = p.getLocation();
                            loc.setY(loc.getY() - 4);
                            if (loc.getBlock().getType() != Material.AIR) {
                                loc.setY(loc.getY() + 8);
                                if (loc.getBlock().getType() != Material.AIR) {
                                    loc.setY(loc.getY() - 4);
                                }
                            }
                            listener.spawnPhantom(p, loc, 1200);
                        }
                    }
                }
            }
            lisn.refreshScoreBoard();
        }
    }
}
