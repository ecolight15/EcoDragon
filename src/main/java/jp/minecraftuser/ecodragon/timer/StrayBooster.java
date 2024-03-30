package jp.minecraftuser.ecodragon.timer;

import jp.minecraftuser.ecodragon.listener.RankingListener;
import jp.minecraftuser.ecoframework.PluginFrame;
import jp.minecraftuser.ecoframework.TimerFrame;

import org.bukkit.util.Vector;

import java.util.UUID;

import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.Player;
import org.bukkit.entity.Stray;
import org.bukkit.entity.AbstractArrow.PickupStatus;
import org.bukkit.scheduler.BukkitTask;


public class StrayBooster extends TimerFrame {
    private Stray stray;
    private UUID uuid;
    private int count = 0;
    public StrayBooster(PluginFrame plg) {
        super(plg, "booster");
    }
    public StrayBooster(PluginFrame plg, Stray stray_, Player player) {
        super(plg, "booster");
        stray = stray_;
        uuid = player.getUniqueId();
    }
    @Override
    public void run()
    {
        if (stray.getHealth() <= 0) {
            this.cancel();
            return;
        }
        Vector v = stray.getVelocity();
        for (Player p : plg.getServer().getOnlinePlayers()) {
            if (p.getUniqueId().equals(uuid)) {
                Location loc = p.getLocation();

                //プレイヤーのファントムの位置を取得して、ファントムがプレイヤーの方向に進むようにベクトルを与える
                Vector v2 = p.getLocation().toVector().subtract(stray.getLocation().toVector()).normalize();
                v2 = v2.multiply(5);

                Arrow arrow = stray.launchProjectile(Arrow.class, v2);
                arrow.setDamage(10);
                arrow.setGlowing(true);
                arrow.setCritical(true);
                arrow.setShooter(stray);
                arrow.setKnockbackStrength(10);
                arrow.setCustomName("眷属の矢");
                arrow.setPickupStatus(PickupStatus.CREATIVE_ONLY);
                new TimerFrame(plg, "allow"){
                    @Override
                    public void run() {
                        arrow.remove();
                    }
                }.runTaskLater(plg, 200);
                break;
            }
        }
    }
}
