package jp.minecraftuser.ecodragon.timer;

import jp.minecraftuser.ecodragon.listener.RankingListener;
import jp.minecraftuser.ecoframework.PluginFrame;
import jp.minecraftuser.ecoframework.TimerFrame;

import org.bukkit.util.Vector;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Stray;
import org.bukkit.entity.AbstractArrow.PickupStatus;


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
        RankingListener lisn = (RankingListener) plg.getPluginListener("ranking");
        Vector v = stray.getVelocity();
        for (Player p : plg.getServer().getOnlinePlayers()) {
            if (p.getUniqueId().equals(uuid)) {
                Location loc = p.getLocation();

                //プレイヤーのファントムの位置を取得して、ファントムがプレイヤーの方向に進むようにベクトルを与える
                Vector v2 = loc.toVector().subtract(stray.getLocation().toVector()).normalize();
                if (lisn.isRanking()) {
                    v2 = v2.multiply(5);
                } else {
                    v2 = v2.multiply(3);
                }

                Arrow arrow = stray.launchProjectile(Arrow.class, v2);
                if (lisn.isRanking()) {
                    arrow.setDamage(5);
                    arrow.setKnockbackStrength(5);
                } else {
                    arrow.setDamage(1);
                    arrow.setKnockbackStrength(1);
                }
                arrow.setGlowing(true);
                arrow.setCritical(true);
                arrow.setShooter(stray);
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
