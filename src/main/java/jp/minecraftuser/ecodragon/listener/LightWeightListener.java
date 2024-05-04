
package jp.minecraftuser.ecodragon.listener;

import jp.minecraftuser.ecoframework.ListenerFrame;
import jp.minecraftuser.ecoframework.PluginFrame;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

/**
 * プレイヤーイベント処理リスナークラス
 * @author ecolight
 */
public class LightWeightListener extends ListenerFrame {
    private static RankingListener ranking = null;

    /**
     * コンストラクタ
     * @param plg_ プラグインインスタンス
     * @param name_ 名前
     */
    public LightWeightListener(PluginFrame plg_, String name_) {
        super(plg_, name_);
        ranking = (RankingListener) plg.getPluginListener("ranking");
    }
    @EventHandler(priority = EventPriority.LOWEST)
    public void CreatureSpawn(CreatureSpawnEvent event) {
        if (!ranking.isRanking()) return;
        if (!event.getEntity().getWorld().equals(ranking.getWorld())) return;
        if (event.getEntityType() == EntityType.ENDERMAN) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = false, priority = EventPriority.LOWEST)
    public void PlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();
        if ( ! world.getName().toLowerCase().startsWith(conf.getString("worldprefix").toLowerCase())) {
            return;
        }
        if ((event.getAction().equals(Action.RIGHT_CLICK_AIR)) ||
            (event.getAction().equals(Action.RIGHT_CLICK_BLOCK))) {
            ItemStack lhand = player.getInventory().getItemInOffHand();
            ItemStack rhand = player.getInventory().getItemInMainHand();
            ItemStack body = player.getInventory().getChestplate();
            if ((body == null) || (body.getType() != Material.ELYTRA)) {
                return;
            }
            if ((lhand.getType() != Material.FIREWORK_ROCKET) &&
                (rhand.getType() != Material.FIREWORK_ROCKET)) {
                return;
            }
            Location loc = player.getLocation();
            int x = Math.abs(loc.getBlockX());
            int z = Math.abs(loc.getBlockZ());
            if ((x >= 600) || (z >= 600)) {
                player.sendMessage("当該空間のエリトラでの飛行は禁止されています");
                event.setCancelled(true);
            }
        }
    }
}
