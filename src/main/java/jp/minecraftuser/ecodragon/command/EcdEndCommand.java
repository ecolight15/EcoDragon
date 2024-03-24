
package jp.minecraftuser.ecodragon.command;

import jp.minecraftuser.ecoframework.PluginFrame;
import jp.minecraftuser.ecodragon.listener.RankingListener;
import jp.minecraftuser.ecoframework.CommandFrame;
import org.bukkit.command.CommandSender;

/**
 * ecd endコマンドクラス
 * エンドラ戦の強制停止
 * @author ecolight
 */
public class EcdEndCommand extends CommandFrame {

    /**
     * コンストラクタ
     * @param plg_ プラグインインスタンス
     * @param name_ コマンド名
     */
    public EcdEndCommand(PluginFrame plg_, String name_) {
        super(plg_, name_);
        setAuthBlock(true);
        setAuthConsole(true);
    }

    /**
     * コマンド権限文字列設定
     * @return 権限文字列
     */
    @Override
    public String getPermissionString() {
        return "ecodragon.end";
    }

    /**
     * 処理実行部
     * @param sender コマンド送信者
     * @param args パラメタ
     * @return コマンド処理成否
     */
    @Override
    public boolean worker(CommandSender sender, String[] args) {
        RankingListener lisn = (RankingListener) plg.getPluginListener("ranking");
        if (!lisn.abortEnderDragonRanking()) {
            sender.sendMessage("[" + plg.getName() + "] エンドラ戦が終了できませんでした。");
        }
        return true;
    }
    
}
