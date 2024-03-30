package jp.minecraftuser.ecodragon.timer;

import jp.minecraftuser.ecodragon.listener.PowerDragonListener;
import jp.minecraftuser.ecodragon.listener.RankingListener;
import jp.minecraftuser.ecoframework.PluginFrame;
import jp.minecraftuser.ecoframework.TimerFrame;

import org.bukkit.util.Vector;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.Player;


public class PhantomBooster extends TimerFrame {
    private Phantom phantom;
    private UUID uuid;
    private int count = 0;
    public PhantomBooster(PluginFrame plg) {
        super(plg, "phantom");
    }
    public PhantomBooster(PluginFrame plg, Phantom phantom_, Player player) {
        super(plg, "phantom");
        phantom = phantom_;
        uuid = player.getUniqueId();
    }
    @Override
    public void run()
    {
        RankingListener listener = (RankingListener) plg.getPluginListener("ranking");
        Vector v = phantom.getVelocity();
        if (listener.isRanking()) {
            for (Player p : plg.getServer().getOnlinePlayers()) {
                if (p.getUniqueId().equals(uuid)) {
                    Location loc = p.getLocation();
                    if (loc.getY() > 70) {
                        //プレイヤーのファントムの位置を取得して、ファントムがプレイヤーの方向に進むようにベクトルを与える
                        Vector v2 = p.getLocation().toVector().subtract(phantom.getLocation().toVector()).normalize();
                        v2 = v2.multiply(3);
                        phantom.setVelocity(v2);
                        phantom.setTarget(p);
                        count++;
                        if (count % 30 == 0) {
                            Location phantomLoc = phantom.getLocation();
                            phantomLoc.setY(phantomLoc.getY() + 4);
                            plg.getServer().getWorld("").spawnParticle(Particle.VILLAGER_ANGRY, phantomLoc, 0);
                        }
                    }
                    break;
                }
            }
        }
    }
}
