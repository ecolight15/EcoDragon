
package jp.minecraftuser.ecodragon.command;

import jp.minecraftuser.ecodragon.listener.RankingListener;
import jp.minecraftuser.ecoframework.CommandFrame;
import jp.minecraftuser.ecoframework.PluginFrame;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * ecd pvpコマンドクラス
 * @author ecolight
 */
public class EcdPvPCommand extends CommandFrame {

    /**
     * コンストラクタ
     * @param plg_ プラグインインスタンス
     * @param name_ コマンド名
     */
    public EcdPvPCommand(PluginFrame plg_, String name_) {
        super(plg_, name_);
    }

    /**
     * コマンド権限文字列設定
     * @return 権限文字列
     */
    @Override
    public String getPermissionString() {
        return "ecodragon.pvp";
    }

    /**
     * 処理実行部
     * @param sender コマンド送信者
     * @param args パラメタ
     * @return コマンド処理成否
     */
    @Override
    public boolean worker(CommandSender sender, String[] args) {
        RankingListener ranking = (RankingListener) plg.getPluginListener("ranking");
        // エンドラ戦未参加の場合
        if (!ranking.isExistPlayer((Player) sender)) {
            sender.sendMessage("[" + plg.getName() + "] あなたは現在エンダードラゴン戦に未参加です");
            return true;
        }
        // パラメタなしの場合は現在の設定表示
        if (args.length == 0) {
            if (ranking.isPlayerPvP((Player) sender)) {
                sender.sendMessage("[" + plg.getName() + "] あなたの現在のエンダードラゴン戦PvP設定は" + ChatColor.BLUE + "有効" + ChatColor.RESET + "です");
            } else {
                sender.sendMessage("[" + plg.getName() + "] あなたの現在のエンダードラゴン戦PvP設定は" + ChatColor.RED + "無効" + ChatColor.RESET + "です");
            }
            return true;
        }
        
        // パラメタありの場合
        if (args[0].equalsIgnoreCase("true")) {
            ranking.setPlayerPvP((Player) sender, true);
            sender.sendMessage("[" + plg.getName() + "] あなたのエンダードラゴン戦PvP設定を" + ChatColor.BLUE + "有効" + ChatColor.RESET + "に設定しました");           
        } else if (args[0].equalsIgnoreCase("false")) {
            //ranking.setPlayerPvP((Player) sender, false);
            //sender.sendMessage("[" + plg.getName() + "] あなたのエンダードラゴン戦PvP設定を" + ChatColor.RED + "無効" + ChatColor.RESET + "に設定しました");
            sender.sendMessage("[" + plg.getName() + "] 現在あなたのエンダードラゴン戦PvP設定を" + ChatColor.RED + "無効" + ChatColor.RESET + "に設定することはできません");
        } else {
            sender.sendMessage("[" + plg.getName() + "] パラメータが異常です");
        }
        return true;
    }
    
}
