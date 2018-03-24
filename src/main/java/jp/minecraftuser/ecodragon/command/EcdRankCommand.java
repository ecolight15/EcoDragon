
package jp.minecraftuser.ecodragon.command;

import java.util.ArrayList;
import java.util.Map;
import jp.minecraftuser.ecodragon.EcoDragonUser;
import jp.minecraftuser.ecodragon.listener.RankingListener;
import jp.minecraftuser.ecoframework.CommandFrame;
import jp.minecraftuser.ecoframework.PluginFrame;
import org.bukkit.command.CommandSender;

/**
 * リロードコマンドクラス
 * @author ecolight
 */
public class EcdRankCommand extends CommandFrame {

    /**
     * コンストラクタ
     * @param plg_ プラグインインスタンス
     * @param name_ コマンド名
     */
    public EcdRankCommand(PluginFrame plg_, String name_) {
        super(plg_, name_);
    }

    /**
     * コマンド権限文字列設定
     * @return 権限文字列
     */
    @Override
    public String getPermissionString() {
        return "ecodragon.rank";
    }

    /**
     * 処理実行部
     * @param sender コマンド送信者
     * @param args パラメタ
     * @return コマンド処理成否
     */
    @Override
    public boolean worker(CommandSender sender, String[] args) {
        // ランキング結果出力
        ArrayList entries = ((RankingListener)plg.getPluginListerner("ranking")).getRankList();
        log.info("list:"+entries.size());
        try {
            for (int rank = 1; rank <= entries.size(); rank++) {
                EcoDragonUser user = (EcoDragonUser)((Map.Entry)entries.get(rank - 1)).getValue();
                log.info(rank+":"+user.toString());
                if (rank == 1) {
                    sender.sendMessage("[" + plg.getName() + "] 現在のランキング1位プレイヤーは[" + user.getPlayer().getName() + "](" + user.getPoint() + "ポイント)です");
                }
                if (user.getPlayer().getName().equalsIgnoreCase(sender.getName())) {
                    sender.sendMessage("[" + plg.getName() + "] 現在のあなたのランキング順位は " + entries.size() + " 人中、" + rank + " 位(" + user.getPoint() + "ポイント)です");
                    return true;
                }
            }
        } catch (Exception e) {
            log.warning(e.getLocalizedMessage());
        }
        sender.sendMessage("[" + plg.getName() + "] 現在のあなたのランキング順位登録はありませんでした");
        return true;
    }
    
}
