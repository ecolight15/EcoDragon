
package jp.minecraftuser.ecodragon.listener;

import jp.minecraftuser.ecoframework.ListenerFrame;
import jp.minecraftuser.ecoframework.PluginFrame;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;

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

}
