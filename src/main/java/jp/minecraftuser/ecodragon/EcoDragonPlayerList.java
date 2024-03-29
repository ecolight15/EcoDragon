package jp.minecraftuser.ecodragon;

import jp.minecraftuser.ecoframework.PluginFrame;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EcoDragonPlayerList {
    private HashMap<UUID, EcoDragonPlayer> ecoDragonPlayerList;
    private PluginFrame plg = null;

    public EcoDragonPlayerList(PluginFrame plg_) {
        plg = plg_;
        ecoDragonPlayerList = new HashMap<UUID, EcoDragonPlayer>();
    }

    public EcoDragonPlayer getEcoDragonPlayer(Player player_) {
        if (ecoDragonPlayerList.containsKey(player_.getUniqueId())) {
            return ecoDragonPlayerList.get(player_.getUniqueId());
        } else {
            EcoDragonPlayer user = new EcoDragonPlayer(player_, plg);
            ecoDragonPlayerList.put(player_.getUniqueId(), user);
            return user;
        }
    }

    public Boolean ContainsEcoDragonUser(Player player_) {
        return ecoDragonPlayerList.containsKey(player_.getUniqueId());
    }

    public Map<UUID, EcoDragonPlayer> getEcoDragonPlayerMap() {
        return ecoDragonPlayerList;
    }

    public void clearMap() {
        ecoDragonPlayerList = new HashMap<UUID, EcoDragonPlayer>();
    }
}
