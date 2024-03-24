
package jp.minecraftuser.ecodragon.command;

import jp.minecraftuser.ecoframework.CommandFrame;
import jp.minecraftuser.ecoframework.PluginFrame;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

/**
 * EcdClearコマンドクラス
 * エンドラ戦でばらまいたMOBを掃除する
 * @author ecolight
 */
public class EcdClearCommand extends CommandFrame {

    /**
     * コンストラクタ
     * @param plg_ プラグインインスタンス
     * @param name_ コマンド名
     */
    public EcdClearCommand(PluginFrame plg_, String name_) {
        super(plg_, name_);
    }

    /**
     * コマンド権限文字列設定
     * @return 権限文字列
     */
    @Override
    public String getPermissionString() {
        return "ecodragon.clear";
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

        /* 一度全MOBを消滅 */
        for (Entity ent : p.getWorld().getEntities()) {
            if (ent.getType() == EntityType.SNOWMAN) ent.remove();
            if (ent.getType() == EntityType.WITCH) ent.remove();
            if (ent.getType() == EntityType.ENDERMAN) ent.remove();
            if (ent.getType() == EntityType.CREEPER) ent.remove();
            if (ent.getType() == EntityType.BLAZE) ent.remove();
            if (ent.getType() == EntityType.CAVE_SPIDER) ent.remove();
            if (ent.getType() == EntityType.GHAST) ent.remove();
            if (ent.getType() == EntityType.IRON_GOLEM) ent.remove();
            if (ent.getType() == EntityType.WITHER) ent.remove();
            if (ent.getType() == EntityType.ZOMBIE) ent.remove();
            if (ent.getType() == EntityType.STRAY) ent.remove();
            if (ent.getType() == EntityType.WITHER_SKELETON) ent.remove();
            if (ent.getType() == EntityType.SKELETON) ent.remove();
        }
        return true;
    }
    
}
