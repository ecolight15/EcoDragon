package jp.minecraftuser.ecodragon.timer;

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
        super(plg, "booster");
    }
    public PhantomBooster(PluginFrame plg, Phantom phantom_, Player player) {
        super(plg, "booster");
        phantom = phantom_;
        uuid = player.getUniqueId();
    }
    @Override
    public void run()
    {
        if (phantom.getHealth() <= 0) {
            this.cancel();
            return;
        }
        Vector v = phantom.getVelocity();
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
                        phantom.getLocation().getWorld().spawnParticle(Particle.VILLAGER_ANGRY, phantomLoc, 0);
                    }
                }
                break;
            }
        }
    }
}
