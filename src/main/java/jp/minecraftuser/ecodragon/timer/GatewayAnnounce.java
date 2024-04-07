package jp.minecraftuser.ecodragon.timer;

import jp.minecraftuser.ecoframework.PluginFrame;
import jp.minecraftuser.ecoframework.TimerFrame;

import static jp.minecraftuser.ecoframework.Utl.sendPluginMessage;

import java.util.Date;
import java.util.UUID;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class GatewayAnnounce extends TimerFrame {
    private UUID uuid;
    private long openTime;
    public GatewayAnnounce(PluginFrame plg, Player player, long openTime_) {
        super(plg, "gateway");
        uuid = player.getUniqueId();
        openTime = openTime_;
    }
    @Override
    public void run()
    {
        Date now = new Date();
        if (openTime < now.getTime()) {
            for (Player player : plg.getServer().getOnlinePlayers()) {
                if (player.getUniqueId().equals(uuid)) {
                    sendPluginMessage(plg, player, "エンドシティへのゲートがあなたを対象に開放されました");
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 0.1f);
                    break;
                }
            }
            this.cancel();
        }
    }
}
