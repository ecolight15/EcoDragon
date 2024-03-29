package jp.minecraftuser.ecodragon;

import jp.minecraftuser.ecoframework.PluginFrame;
import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EcoDragonPlayerList {
    private HashMap<UUID, EcoDragonPlayer> ecoDragonPlayerList;
    private PluginFrame plg = null;

    /**
     * コンストラクタ
     * @param plg_ プラグインインスタンス
     */
    public EcoDragonPlayerList(PluginFrame plg_) {
        plg = plg_;
        ecoDragonPlayerList = new HashMap<UUID, EcoDragonPlayer>();
    }

    /**
     * プラグインプレイヤーインスタンスの取得
     * @param player_ プレイヤーインスタンス
     * @return プラグインプレイヤーインスタンス
     */
    public EcoDragonPlayer getEcoDragonPlayer(Player player_) {
        if (ecoDragonPlayerList.containsKey(player_.getUniqueId())) {
            return ecoDragonPlayerList.get(player_.getUniqueId());
        } else {
            EcoDragonPlayer user = new EcoDragonPlayer(player_, plg);
            ecoDragonPlayerList.put(player_.getUniqueId(), user);
            return user;
        }
    }

    /**
     * プラグインプレイヤーインスタンスの存在チェック
     * @param player_ プレイヤーインスタンス
     * @return プラグインプレイヤーインスタンス
     */
    public boolean ContainsEcoDragonUser(Player player_) {
        return ecoDragonPlayerList.containsKey(player_.getUniqueId());
    }

    /**
     * プラグインプレイヤーリストの取得
     * @return 全エンドラ戦プレイヤーのマップ
     */
    public Map<UUID, EcoDragonPlayer> getEcoDragonPlayerMap() {
        return ecoDragonPlayerList;
    }

    /**
     * プラグインプレイヤーリストのクリア
     */
    public void clearMap() {
        ecoDragonPlayerList = new HashMap<UUID, EcoDragonPlayer>();
    }
}
