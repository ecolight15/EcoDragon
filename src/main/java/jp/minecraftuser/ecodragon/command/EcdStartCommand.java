
package jp.minecraftuser.ecodragon.command;

import jp.minecraftuser.ecoframework.PluginFrame;
import jp.minecraftuser.ecodragon.listener.RankingListener;
import jp.minecraftuser.ecoframework.CommandFrame;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * ecd startコマンドクラス
 * @author ecolight
 */
public class EcdStartCommand extends CommandFrame {

    /**
     * コンストラクタ
     * @param plg_ プラグインインスタンス
     * @param name_ コマンド名
     */
    public EcdStartCommand(PluginFrame plg_, String name_) {
        super(plg_, name_);
    }

    /**
     * コマンド権限文字列設定
     * @return 権限文字列
     */
    @Override
    public String getPermissionString() {
        return "ecodragon.start";
    }

    /**
     * 処理実行部
     * @param sender コマンド送信者
     * @param args パラメタ
     * @return コマンド処理成否
     */
    @Override
    public boolean worker(CommandSender sender, String[] args) {
        Player p = (Player) sender;
        RankingListener lisn = (RankingListener) plg.getPluginListerner("ranking");
        lisn.setFirst();
        if (!lisn.startEnderDragonRanking(p.getWorld(), true)) {
            sender.sendMessage("[" + plg.getName() + "] エンドラ戦が開始できませんでした。");
        }
        return true;
    }
    
}
