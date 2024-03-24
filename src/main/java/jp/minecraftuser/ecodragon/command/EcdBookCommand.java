
package jp.minecraftuser.ecodragon.command;

import jp.minecraftuser.ecoframework.PluginFrame;
import jp.minecraftuser.ecodragon.listener.RankingListener;
import jp.minecraftuser.ecoframework.CommandFrame;

import static jp.minecraftuser.ecoframework.Utl.sendPluginMessage;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * ecd endコマンドクラス
 * エンドラ戦の強制停止
 * @author ecolight
 */
public class EcdBookCommand extends CommandFrame {

    /**
     * コンストラクタ
     * @param plg_ プラグインインスタンス
     * @param name_ コマンド名
     */
    public EcdBookCommand(PluginFrame plg_, String name_) {
        super(plg_, name_);
    }

    /**
     * コマンド権限文字列設定
     * @return 権限文字列
     */
    @Override
    public String getPermissionString() {
        return "ecodragon.book";
    }

    /**
     * 処理実行部
     * @param sender コマンド送信者
     * @param args パラメタ
     * @return コマンド処理成否
     */
    @Override
    public boolean worker(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            Player pl = (Player) sender;
            
            RankingListener lisn = (RankingListener) plg.getPluginListener("ranking");
            ItemStack item = lisn.makeCertificate(sender.getName());
            pl.getInventory().addItem(item);
            sendPluginMessage(plg, sender, "賞状の本を取得しました");
        }
        return true;
    }
    
}
