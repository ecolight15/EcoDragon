package jp.minecraftuser.ecodragon.timer;

import jp.minecraftuser.ecoframework.PluginFrame;
import jp.minecraftuser.ecoframework.TimerFrame;

import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;


public class EndEventTimer extends TimerFrame {
    private String msg = null;
    private World w = null;
    private Scoreboard board = null;
    private Objective dmgobj = null;
    public EndEventTimer(PluginFrame plg) {
        super(plg, "end");
    }
    public EndEventTimer(PluginFrame plg, String msg_) {
        super(plg, "end");
        msg = msg_;
        w = null;
        board = null;
        dmgobj = null;
    }
    public EndEventTimer(PluginFrame plg, String msg_, World w_) {
        super(plg, "end");
        msg = msg_;
        w = w_;
        board = null;
        dmgobj = null;
    }
    public EndEventTimer(PluginFrame plg, String msg_, Scoreboard board_, Objective dmgobj_) {
        super(plg, "end");
        msg = msg_;
        w = null;
        board = board_;
        dmgobj = dmgobj_;
    }
    @Override
    public void run()
    {
        if (msg != null) {
            // メッセージ配信
            plg.getServer().broadcastMessage("[" + plg.getName() + "] " + msg);
        }
        if (board != null) {
            // スコアボード破棄
            for (Player p: plg.getServer().getOnlinePlayers()) {
                if (p.hasPermission("ecodragon.score")) {
                    if (p.getScoreboard().equals(board)) {
                        p.setScoreboard(plg.getServer().getScoreboardManager().getMainScoreboard());
                    }
                }
            }
            if (board != null) board.clearSlot(DisplaySlot.PLAYER_LIST);
            if (dmgobj != null) dmgobj.unregister();
        }
        if (w != null) {
            w.setGameRule(GameRule.KEEP_INVENTORY, false);
        }
    }
}
