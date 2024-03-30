package jp.minecraftuser.ecodragon.timer;

import jp.minecraftuser.ecoframework.PluginFrame;
import jp.minecraftuser.ecodragon.listener.RankingListener;
import jp.minecraftuser.ecoframework.TimerFrame;

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
            for (Player p : plg.getServer().getOnlinePlayers()) {
                if (p.getWorld().equals(lisn.getWorld())) {
                    lisn.addPoint(p, 10);
                }
            }
            lisn.refreshScoreBoard();
        }
    }
}
