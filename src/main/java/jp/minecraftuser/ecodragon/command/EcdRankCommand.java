
package jp.minecraftuser.ecodragon.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import jp.minecraftuser.ecodragon.EcoDragonPlayer;
import jp.minecraftuser.ecodragon.listener.RankingListener;
import jp.minecraftuser.ecoframework.CommandFrame;
import jp.minecraftuser.ecoframework.PluginFrame;
import static jp.minecraftuser.ecoframework.Utl.sendPluginMessage;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
        if ( ! (sender instanceof Player)) {
            return false;
        }
        Player pl = (Player) sender;
        // ランキング結果出力
        RankingListener listener = (RankingListener)plg.getPluginListener("ranking");
        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("true")) {
                listener.setScoreboard(pl, true);
                sendPluginMessage(plg, pl, "ランキングボード表示を有効にします");

            } else if (args[0].equalsIgnoreCase(("false"))) {
                listener.setScoreboard(pl, false);
                sendPluginMessage(plg, pl, "ランキングボード表示を無効にします");
            }
            return true;
        }

        ArrayList entries = listener.getRankList();
        log.info("list:" + entries.size());
        try {
            for (int rank = 1; rank <= entries.size(); rank++) {
                EcoDragonPlayer user = (EcoDragonPlayer)((Map.Entry)entries.get(rank - 1)).getValue();
                log.info(rank+":"+user.toString());
                if (rank == 1) {
                    sender.sendMessage("[" + plg.getName() + "] ");
                    sendPluginMessage(plg, pl, "現在のランキング1位プレイヤーは[" + user.getPlayer().getName() + "](" + user.getPoint() + "ポイント)です");
                }
                if (user.getPlayer().getName().equalsIgnoreCase(sender.getName())) {
                    sendPluginMessage(plg, pl, "現在のあなたのランキング順位は " + entries.size() + " 人中、" + rank + " 位(" + user.getPoint() + "ポイント)です");
                    return true;
                }
            }
        } catch (Exception e) {
            log.warning(e.getLocalizedMessage());
        }
        sendPluginMessage(plg, pl, "現在のあなたのランキング順位登録はありませんでした");
        return true;
    }

    /**
     * コマンド別タブコンプリート処理
     * @param sender コマンド送信者インスタンス
     * @param cmd コマンドインスタンス
     * @param string コマンド文字列
     * @param strings パラメタ文字列配列
     * @return 保管文字列配列
     */
    @Override
    protected List<String> getTabComplete(CommandSender sender, Command cmd, String string, String[] strings) {
        ArrayList<String> list = new ArrayList<>();
        list.add("show");
        list.add("true");
        list.add("false");
        return list;
    }
}
