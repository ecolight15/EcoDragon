package jp.minecraftuser.ecodragon.timer;

import jp.minecraftuser.ecoframework.PluginFrame;
import jp.minecraftuser.ecoframework.TimerFrame;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;


public class EndPvPTimer extends TimerFrame {
    private Player p = null;
    public EndPvPTimer(PluginFrame plg) {
        super(plg, "pvp");
    }
    public EndPvPTimer(PluginFrame plg, Player p_) {
        super(plg, "pvp");
        p = p_;
    }
    @Override
    public void run()
    {
        for (Entity e : p.getNearbyEntities(20, 20, 20)) {
            if (e instanceof Player) {
                if (!p.equals(e)) {
                    ((Player)e).playSound(p.getLocation(), Sound.ITEM_ELYTRA_FLYING, (float) 1, (float) 0.0001);
                }
            }
        }
//        p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_LAND, (float) 0.1, (float) 0.001);
        Location l = p.getLocation();
        p.getWorld().spawnParticle(Particle.END_ROD, l.getX(), l.getY()+3, l.getZ(), 50, 0, 1, 0);
        p.playEffect(EntityEffect.VILLAGER_HAPPY);
    }
}
