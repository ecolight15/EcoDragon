
package jp.minecraftuser.ecodragon.listener;

import static jp.minecraftuser.ecoframework.Utl.sendPluginMessage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import jp.minecraftuser.ecodragon.EcoDragonPlayer;
import jp.minecraftuser.ecodragon.EcoDragonPlayerList;
import jp.minecraftuser.ecoframework.PluginFrame;
import jp.minecraftuser.ecodragon.config.CertificateConfig;
import jp.minecraftuser.ecodragon.timer.EndEventTimer;
import jp.minecraftuser.ecodragon.timer.EndPvPTimer;
import jp.minecraftuser.ecodragon.timer.GatewayAnnounce;
import jp.minecraftuser.ecodragon.timer.WorldTimer;
import jp.minecraftuser.ecoframework.ListenerFrame;
import jp.minecraftuser.ecoframework.TimerFrame;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Egg;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.entity.Trident;
import org.bukkit.entity.Warden;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.entity.LingeringPotionSplashEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

/**
 * ランキング関連イベント処理リスナークラス
 * - プレイヤーがテレポートした先 or ログイン先が登録プリフィックス名のワールド名
 * 　であればエンドラを検索しエンドラが存在する場合には開始処理を実施する。
 *
 * @author ecolight
 */
public class RankingListener extends ListenerFrame {

    private EcoDragonPlayerList ecoDragonUserList;
    private static final HashMap<UUID, Long> intervalList = new HashMap<>();
    private static final ArrayList<Block> existCrystal = new ArrayList<>();
    private static final ArrayList<TimerFrame> evtimer = new ArrayList<>();
    private static final HashMap<UUID, TimerFrame> pvptimer = new HashMap<>();
    private static CertificateConfig cerConf = null;
    private static World curWorld = null;
    private static int round = 0;
    private static Scoreboard board = null;
    private static Objective dmgobj = null;
    private static long lastInterval = 0;
    private static WorldTimer timer = null;
    private static boolean first = true;
    private static int gateReleaseinterval = 300; // 5 min 

    /**
     * コンストラクタ
     *
     * @param plg_  プラグインインスタンス
     * @param name_ 名前
     */
    public RankingListener(PluginFrame plg_, String name_) {
        super(plg_, name_);
        cerConf = (CertificateConfig) plg.getPluginConfig("certificate");
        ecoDragonUserList = new EcoDragonPlayerList(plg_);
    }


    /**
     * プレイヤーログイン後イベント処理
     *
     * @param event イベント情報
     */
    @EventHandler(priority = EventPriority.LOW)
    public void PlayerJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();

        // エンドラ戦中であればスコアボード設定する
        if (curWorld != null) {
            setScoreboard(p, true);
            if (curWorld.equals(p.getWorld())) {
                // 透明化が掛かっていたら解除する
                for (PotionEffect pe : p.getActivePotionEffects()) {
                    if (pe.getType().equals(PotionEffectType.INVISIBILITY)) {
                        p.removePotionEffect(PotionEffectType.INVISIBILITY);
                    }
                }
            }
            sendPluginMessage(plg, p, "強化エンダードラゴン戦が開催されています");
            sendPluginMessage(plg, p, "あと " + round + " 回エンダードラゴンを討伐するとエンドゲートウェイの転送が開放されます");
            sendPluginMessage(plg, p, "エンドゲートウェイの開放は個人ごとに (ランキング順位 * " + gateReleaseinterval + "秒) のインターバルを要します");
            this.plg.getServer().dispatchCommand(this.plg.getServer().getConsoleSender(), "tellraw " + p.getName() + " [\"\",{\"text\":\"\\u30e9\\u30f3\\u30ad\\u30f3\\u30b0\\u78ba\\u8a8d\\u306f \"},{\"text\":\"/ecd rank\",\"bold\":true,\"underlined\":true,\"color\":\"yellow\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/ecd rank\"}}]");
            this.plg.getServer().dispatchCommand(this.plg.getServer().getConsoleSender(), "tellraw " + p.getName() + " [\"\",{\"text\":\"\\u30b9\\u30b3\\u30a2\\u30dc\\u30fc\\u30c9\\u306e\\u975e\\u8868\\u793a\\u306f \"},{\"text\":\"/ecd rank false\",\"bold\":true,\"underlined\":true,\"color\":\"yellow\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/ecd rank false\"}}]");
        }

        // エンドラ戦が未実行の場合は開始処理を走らせる（最初の一人がエンドに入らないとエンドラが検出できない）
        startEnderDragonRanking(p.getWorld(), false);
    }

    /**
     * Entityテレポートイベント処理
     *
     * @param event イベント情報
     */
    @EventHandler
    public void EntityPortalEvent(EntityPortalEvent event) {
        // エンドラ戦対象ワールドでない場合は何もしない
        String prefix = plg.getDefaultConfig().getString("worldprefix");
        if (!event.getFrom().getWorld().getName().toLowerCase().startsWith(prefix.toLowerCase())) return;

        Entity e = event.getEntity();
        if (e instanceof Warden) {
            event.setCancelled(true);
        }
    }

    /**
     * プレイヤーテレポートイベント処理
     *
     * @param event イベント情報
     */
    @EventHandler
    public void PlayerTeleport(PlayerTeleportEvent event) {
        startEnderDragonRanking(event.getTo().getWorld(), false);

        Player player = event.getPlayer();
        if (curWorld != null) {
            if (curWorld.equals(event.getTo().getWorld())) {
                // 透明化が掛かっていたら解除する
                for (PotionEffect pe : player.getActivePotionEffects()) {
                    if (pe.getType().equals(PotionEffectType.INVISIBILITY)) {
                        player.removePotionEffect(PotionEffectType.INVISIBILITY);
                    }
                }
            }
        }

        // エンドラランキング中の場合は、エンドゲートウェイの転送は抑止する。
        // エンドラランキング後はインターバルに応じて開放する

        // テレポートイベントがエンドゲートウェイ由来でなければ何もしない
        if (event.getCause() != TeleportCause.END_GATEWAY) return;

        // エンドラ戦対象ワールドでない場合は何もしない
        String prefix = plg.getDefaultConfig().getString("worldprefix");
        World w = player.getWorld();
        if (!w.getName().toLowerCase().startsWith(prefix.toLowerCase())) return;

        // インターバルテーブルが空の場合には何もしない
        if (intervalList.isEmpty()) {
            // ワールド境界の状態をチェック(直径1000の場合、未拡張なので抑止する)
            WorldBorder wb = w.getWorldBorder();
            if (wb.getSize() == conf.getInt("before-world-border")) {
                sendPluginMessage(plg, player, "強化エンダードラゴンが規定回数攻略されるまでエンドゲートウェイの使用は抑止されます");
                event.setCancelled(true);
            }
            sendPluginMessage(plg, player, "現在エンドシティでのエリトラの使用は全面抑止されているのでご注意ください");
            return;
        }

        // 最後のインターバル時刻を超えていたら強制解除
        Date now = new Date();
        if (lastInterval < now.getTime()) {
            intervalList.clear();
            sendPluginMessage(plg, player, "現在エンドシティでのエリトラの使用は全面抑止されているのでご注意ください");
            return;
        }

        // インターバルテーブルがある場合、自分のインターバルを超えてるか確認し超えていない場合は抑止する
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        if (intervalList.containsKey(player.getUniqueId())) {
            if (intervalList.get(player.getUniqueId()) > now.getTime()) {
                // まだ未達
                event.setCancelled(true);
                player.sendMessage("[" + plg.getName() + "] 現在のサーバー時刻 " + sdf.format(new Date()));
                player.sendMessage("[" + plg.getName() + "] エンドゲートウェイ開放目安 " + sdf.format(new Date(intervalList.get(player.getUniqueId()))));
            } else {
                // 到達
                sendPluginMessage(plg, player, "現在エンドシティでのエリトラの使用は全面抑止されているのでご注意ください");
            }
        } else {
            // インターバルテーブルに登録がない場合は空になるまで待つ
            event.setCancelled(true);
            player.sendMessage("[" + plg.getName() + "] 現在のサーバー時刻 " + sdf.format(new Date()));
            player.sendMessage("[" + plg.getName() + "] エンドゲートウェイ開放目安 " + sdf.format(new Date(lastInterval)));
        }
    }

    /**
     * エンドラ討伐判定
     *
     * @param event
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void EntityDeath(EntityDeathEvent event) {
        if (curWorld == null) return;
        if (event.getEntityType() == EntityType.ENDER_DRAGON) {
            // エンダークリスタル設置ボーナスブロック初期化
            existCrystal.clear();

            // 討伐時メッセージ表示
            round--;
            roundMessage();

            // 終了判定
            if (round == 0) {
                endRankingPresent();
                endEnderDragonRanking();
            }
        }
    }

    /**
     * エンドラ討伐判定
     *
     * @param event
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void PlayerDeath(PlayerDeathEvent event) {

        // エンドラ戦中でなければ何もしない
        if (curWorld == null) return;

        // エンドラ戦中のワールドでなければ何もしない
        Player player = event.getEntity();
        if (!player.getWorld().equals(curWorld)) return;

        // 死亡メッセージ無し
        //event.setDeathMessage(null);

        // PvPでの死亡の場合、ランキングポイントを半分譲渡する
        Player killerPlayer = player.getKiller();
        if (killerPlayer != null) {
            if (ecoDragonUserList.ContainsEcoDragonUser(player)) {
                EcoDragonPlayer ecoDragonPlayer = ecoDragonUserList.getEcoDragonPlayer(player);
                if (ecoDragonPlayer.isPvP()) {
                    EcoDragonPlayer ecoDragonPlayerKiller = ecoDragonUserList.getEcoDragonPlayer(killerPlayer);
                    if ((ecoDragonPlayer.isPvP()) &&
                        (ecoDragonPlayerKiller.isPvP())) {

                        int harf = ecoDragonPlayer.getPoint() / 2;
                        StringBuilder sb = new StringBuilder();
                        sb.append("before point ");
                        sb.append(player.getName());
                        sb.append("(");
                        sb.append(ecoDragonPlayer.getPoint());
                        sb.append(") -> ");
                        sb.append(killerPlayer.getName());
                        sb.append(ecoDragonPlayerKiller.getPoint());
                        sb.append(")");
                        sb.append("after point ");

                        ecoDragonPlayer.addPoint(-harf);
                        ecoDragonPlayerKiller.addPoint(harf);

                        player.sendMessage("[" + plg.getName() + "] エンドラ戦 PvPデスペナルティ: -" + harf + " pt");
                        killerPlayer.sendMessage("[" + plg.getName() + "] エンドラ戦 PvP討伐ボーナス: +" + harf + " pt");

                        // リスト更新
                        refreshScoreBoard();
                        sb.append(player.getName());
                        sb.append("(");
                        sb.append(ecoDragonPlayer.getPoint());
                        sb.append(") -> ");
                        sb.append(killerPlayer.getName());
                        sb.append(ecoDragonPlayerKiller.getPoint());
                        sb.append(")");
                        log.info(sb.toString());
                    }
                }
            }
        }
    }

    /**
     * エンドラ戦中の一部ブロック設置抑止
     *
     * @param event イベント情報
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void BlockPlaceEvent(BlockPlaceEvent event) {
        // エンドラ戦中だけ
        if (curWorld == null) {
            return;
        }

        // 当該ワールドだけ
        Block b = event.getBlock();
        if (!curWorld.equals(b.getWorld())) {
            return;
        }

        // 黒曜石の設置を禁止する
        if (b.getType() == Material.OBSIDIAN) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("[" + plg.getName() + "] エンドラ戦中の黒曜石設置破壊は禁止されています");
        } else if (b.getType() == Material.DISPENSER) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("[" + plg.getName() + "] エンドラ戦中のディスペンサー設置は禁止されています");
        } else if (b.getType() == Material.TNT) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("[" + plg.getName() + "] エンドラ戦中のTNT設置は禁止されています");
        }

    }

    /**
     * プレイヤー接触イベント
     *
     * @param event
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void PlayerInteract(PlayerInteractEvent event) {
        // エンドラ戦中だけ
        if (curWorld == null) {
            return;
        }
        // 当該ワールドだけ
        Block b = event.getClickedBlock();
        if (b == null) {
            return;
        }
        if (!curWorld.equals(b.getWorld())) {
            return;
        }

        // エンダークリスタル設置
        Player player = event.getPlayer();
        if (Action.RIGHT_CLICK_BLOCK == event.getAction()) {
            if (Material.BEDROCK == event.getClickedBlock().getType()) {
                if (Material.END_CRYSTAL == event.getMaterial()) {
                    // 所定の位置か確認
                    Location x = b.getLocation();
                    Location z = b.getLocation();
                    Location xx = b.getLocation();
                    Location zz = b.getLocation();
                    x.setX(x.getX() - 1);
                    xx.setX(xx.getX() + 1);
                    z.setZ(z.getZ() - 1);
                    zz.setZ(zz.getZ() + 1);
                    if ((((x.getBlock().getType() == Material.BEDROCK) || (xx.getBlock().getType() == Material.BEDROCK)) &&
                         (z.getBlock().getType() != Material.BEDROCK) && (zz.getBlock().getType() != Material.BEDROCK)) ||
                        (((z.getBlock().getType() == Material.BEDROCK) || (zz.getBlock().getType() == Material.BEDROCK)) &&
                         (x.getBlock().getType() != Material.BEDROCK) && (xx.getBlock().getType() != Material.BEDROCK))) {
                        Bukkit.getScheduler().runTask(plg, new Runnable() {
                            @Override
                            public void run() {
                                List<Entity> entities = event.getPlayer().getNearbyEntities(4, 4, 4);
                                for (Entity entity : entities) {
                                    if (EntityType.ENDER_CRYSTAL == entity.getType()) {
                                        EnderCrystal crystal = (EnderCrystal) entity;
                                        Block belowCrystal = crystal.getLocation().getBlock().getRelative(BlockFace.DOWN);
                                        if (event.getClickedBlock().equals(belowCrystal)) {
                                            if (!existCrystal.contains(belowCrystal)) {
                                                // 中心のブロックと2つとなりのブロックまでクリスタル設置済みとしてマークしておく
                                                existCrystal.add(belowCrystal);
                                                Block buf = belowCrystal.getRelative(BlockFace.NORTH);
                                                existCrystal.add(buf);
                                                buf = buf.getRelative(BlockFace.NORTH);
                                                existCrystal.add(buf);
                                                buf = belowCrystal.getRelative(BlockFace.SOUTH);
                                                existCrystal.add(buf);
                                                buf = buf.getRelative(BlockFace.SOUTH);
                                                existCrystal.add(buf);
                                                buf = belowCrystal.getRelative(BlockFace.EAST);
                                                existCrystal.add(buf);
                                                buf = buf.getRelative(BlockFace.EAST);
                                                existCrystal.add(buf);
                                                buf = belowCrystal.getRelative(BlockFace.WEST);
                                                existCrystal.add(buf);
                                                buf = buf.getRelative(BlockFace.WEST);
                                                existCrystal.add(buf);

                                                // 別スレッドで実行する系列はgetPlayerでサーバーから改めて取得
                                                EcoDragonPlayer ecoDragonPlayer = ecoDragonUserList.getEcoDragonPlayer(player);
                                                int bonus = conf.getInt("crystal-place-bonus");
                                                ecoDragonPlayer.addPoint(bonus);
                                                ecoDragonPlayer.getPlayer().sendMessage("[" + plg.getName() + "] エンダークリスタルの設置ボーナス: " + bonus + " pt");
                                                plg.getServer().broadcastMessage("[" + plg.getName() + "] " + ecoDragonPlayer.getPlayer().getName() + " がエンダークリスタルを設置しました(bonus: " + bonus + " pt)");
                                                log.info("[" + plg.getName() + "] " + ecoDragonPlayer.getPlayer().getName() + " エンダークリスタルの設置ボーナス: " + bonus + " pt");
                                                refreshScoreBoard();
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        });
                    }
                }
            }
        }
        if (b != null) {
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                if (event.getItem() != null) {
                    // トロッコ禁止
                    if ((event.getItem().getType() == Material.TNT_MINECART) ||
                            (event.getItem().getType() == Material.HOPPER_MINECART)) {
                        if ((b.getType() == Material.RAIL) ||
                            (b.getType() == Material.POWERED_RAIL) ||
                            (b.getType() == Material.DETECTOR_RAIL) ||
                            (b.getType() == Material.ACTIVATOR_RAIL)) {

                            event.getPlayer().sendMessage("[" + plg.getName() + "] エンドラ戦中のTNTマインカートの設置は禁止されています");
                            event.setCancelled(true);
                        }

                    } else if (event.getItem().getType() == Material.END_CRYSTAL) {
                        for (Entity e : b.getWorld().getEntities()) {
                            if (e.getType() == EntityType.ENDER_DRAGON) {
                                player.sendMessage("[" + plg.getName() + "] エンドラ戦中はエンドラがいない間だけエンダークリスタルの設置が許可されています");
                                event.setCancelled(true);
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * エンティティ対エンティティダメージ判定イベントハンドラ
     *
     * @param event
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void EntityDamageByEntity(EntityDamageByEntityEvent event) {
        // エンドラ戦中だけ
        if (curWorld == null) {
            return;
        }

        // 当該ワールドだけ
        Entity eventEntity = event.getEntity();
        if (!curWorld.equals(eventEntity.getWorld())) {
            return;
        }

        ProjectileSource attackEntity = null;
        Entity damager = event.getDamager();
        switch (damager.getType()) {
            case PLAYER:
                attackEntity = (Player) damager;
                break;
            case ARROW:
                attackEntity = ((Arrow) damager).getShooter();
                break;
            case SNOWBALL:
                attackEntity = ((Snowball) damager).getShooter();
                break;
            case EGG:
                attackEntity = ((Egg) damager).getShooter();
                break;
            case SPLASH_POTION:
                attackEntity = ((ThrownPotion) damager).getShooter();
                break;
            case TRIDENT:
                attackEntity = ((Trident) damager).getShooter();
                break;
            case ENDER_PEARL:
                attackEntity = ((EnderPearl) damager).getShooter();
                break;
            default:
                break;
        }
        //攻撃エンティティがプレイヤーでなければreturn
        if ((attackEntity == null) || (!(attackEntity instanceof Player))) return;
        Player attackPlayer = (Player) attackEntity;

        switch (eventEntity.getType()) {
            case PLAYER:
                //PVP
                Player targetPlayer = (Player) event.getEntity();
                if (event.getEntity() instanceof Player) {
                    if (!checkCanPVP(attackPlayer, targetPlayer)) {
                        event.setCancelled(true);
                        return;
                    }
                }
                break;
            case ENDER_CRYSTAL: {
                // 破壊者のランキング操作
                EcoDragonPlayer ecoDragonPlayer = ecoDragonUserList.getEcoDragonPlayer(attackPlayer);
                boolean dragonExist = false;
                for (Entity e : curWorld.getEntities()) {
                    if (e.getType() == EntityType.ENDER_DRAGON) {
                        dragonExist = true;
                        break;
                    }
                }
                if (dragonExist) {
                    // 塔のエンドクリスタル以外は無視する
                    Location crLoc = event.getEntity().getLocation();
                    if (crLoc.getBlock().getType() != Material.FIRE) {
                        log.info("top:"+crLoc.getBlock().getType());
                        return;
                    }
                    crLoc.setY(crLoc.getY() - 1);
                    if (crLoc.getBlock().getType() != Material.BEDROCK) {
                        log.info("down:"+crLoc.getBlock().getType());
                        return;
                    }
                    if (crLoc.getBlock().getRelative(BlockFace.EAST).getType() == Material.BEDROCK) {
                        log.info("x+:"+crLoc.getBlock().getRelative(BlockFace.EAST).getType());
                        return;
                    }
                    if (crLoc.getBlock().getRelative(BlockFace.WEST).getType() == Material.BEDROCK) {
                        log.info("x-:"+crLoc.getBlock().getRelative(BlockFace.WEST).getType());
                        return;
                    }
                    if (crLoc.getBlock().getRelative(BlockFace.SOUTH).getType() == Material.BEDROCK) {
                        log.info("z+:"+crLoc.getBlock().getRelative(BlockFace.SOUTH).getType());
                        return;
                    }
                    if (crLoc.getBlock().getRelative(BlockFace.NORTH).getType() == Material.BEDROCK) {
                        log.info("z-:"+crLoc.getBlock().getRelative(BlockFace.NORTH).getType());
                        return;
                    }
                    crLoc.setY(crLoc.getY() - 1);
                    if (crLoc.getBlock().getType() != Material.OBSIDIAN) {
                        log.info("deep:"+crLoc.getBlock().getType());
                        return;
                    }
    
                    int bonus = conf.getInt("crystal-break-bonus");
                    ecoDragonPlayer.addPoint(bonus);
                    plg.getServer().broadcastMessage("[" + plg.getName() + "] " + attackPlayer.getName() + " がエンダークリスタルを破壊しました(bonus: " + bonus + " pt)");
                    attackPlayer.sendMessage("[" + plg.getName() + "] エンダークリスタルの破壊ボーナス: " + bonus + " pt");
                    log.info("[" + plg.getName() + "] " + attackPlayer.getName() + " エンダークリスタルの破壊ボーナス: " + bonus + " pt");
                } else {
                    int penalty = conf.getInt("crystal-break-penalty");
                    ecoDragonPlayer.addPoint(penalty);
                    plg.getServer().broadcastMessage("[" + plg.getName() + "] " + attackPlayer.getName() + " がエンダークリスタルを破壊しました(penalty: " + penalty + " pt)");
                    attackPlayer.sendMessage("[" + plg.getName() + "] エンダークリスタルの破壊ペナルティ: " + penalty + " pt");
                    log.info("[" + plg.getName() + "] " + attackPlayer.getName() + "エンダークリスタルの破壊ペナルティ: " + penalty + " pt");
                }
                refreshScoreBoard();
                break;
            }
            case ENDER_DRAGON: {
                EcoDragonPlayer ecoDragonPlayer = ecoDragonUserList.getEcoDragonPlayer(attackPlayer);
                ecoDragonPlayer.addDamage((int) event.getDamage());
                refreshScoreBoard();
                break;
            }
            default: {
                EcoDragonPlayer ecoDragonPlayer = ecoDragonUserList.getEcoDragonPlayer(attackPlayer);
                ecoDragonPlayer.addDamageEtc((int) event.getDamage());
                refreshScoreBoard();
                break;
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void InventoryClick(InventoryClickEvent event) {
        // エンドラ戦中だけ
        if (curWorld == null) {
            return;
        }

        // 当該ワールドだけ
        HumanEntity e = event.getWhoClicked();
        if (!curWorld.equals(e.getWorld())) {
            return;
        }

        InventoryType type = event.getInventory().getType();
        switch (type) {
            case CHEST:
            case BREWING:
            case DISPENSER:
            case FURNACE:
            case HOPPER:
                boolean hit = false;
                if (event.getCurrentItem() != null) {
                    if (event.getCurrentItem().getType() == Material.TNT_MINECART) {
                        hit = true;
                    }
                }
                if (event.getCursor() != null) {
                    if (event.getCursor().getType() == Material.TNT_MINECART) {
                        hit = true;
                    }
                }
                if (hit) {
                    plg.getServer().getPlayer(event.getWhoClicked().getName()).sendMessage("[" + plg.getName() + "] エンドラ戦中のTNTマインカートの操作は禁止されています");
                    event.setCancelled(true);
                }
            default:
        }
    }

    /**
     * エンドラ戦中の一部ブロック破壊抑止
     *
     * @param event イベント情報
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void BlockBreakEvent(BlockBreakEvent event) {
        // エンドラ戦中だけ
        if (curWorld == null) {
            return;
        }

        // 当該ワールドだけ
        Block b = event.getBlock();
        if (!curWorld.equals(b.getWorld())) {
            return;
        }

        // 黒曜石の破壊を禁止する
        if (b.getType() == Material.OBSIDIAN) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("[" + plg.getName() + "] エンドラ戦中の黒曜石設置破壊は禁止されています");
        }
    }

    /**
     * エンドラ戦中の一部ブロック設置抑止(バケツ経由)
     *
     * @param event イベント情報
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void PlayerBucketEmptyEvent(PlayerBucketEmptyEvent event) {
        // エンドラ戦中だけ
        if (curWorld == null) {
            return;
        }

        // 当該ワールドだけ
        Player p = event.getPlayer();
        if (!curWorld.equals(p.getWorld())) {
            return;
        }

        // 溶岩バケツは抑止
        if (event.getBucket() == Material.LAVA_BUCKET) {
            event.setCancelled(true);
            p.sendMessage("[" + plg.getName() + "] エンドラ戦中の当該ワールドにおける溶岩バケツの使用は禁止されています");
        }
    }

    /**
     * つり
     *
     * @param event
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void PlayerFish(PlayerFishEvent event) {
        // エンドラ戦中だけ
        if (curWorld == null) {
            return;
        }

        // 当該ワールドだけ
        Player player = event.getPlayer();
        if (!curWorld.equals(player.getWorld())) {
            return;
        }

        if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
            EcoDragonPlayer ecoDragonPlayer = ecoDragonUserList.getEcoDragonPlayer(player);

            int point = 0;
            log.info("fish caught=" + event.getCaught().getName());
            if (event.getCaught().getName().equalsIgnoreCase("Raw Cod")) {
                point = conf.getInt("fishing-bonus");
                ecoDragonPlayer.addPoint(point);
                player.sendMessage("[" + plg.getName() + "] さかなだー！ (" + point + " pt)");
            } else if (event.getCaught().getName().equalsIgnoreCase("Raw Salmon")) {
                point = conf.getInt("fishing-salmon-bonus");
                ecoDragonPlayer.addPoint(point);
                player.sendMessage("[" + plg.getName() + "] しゃけだー！ (" + point + " pt)");
            } else if (event.getCaught().getName().equalsIgnoreCase("Pufferfish")) {
                point = conf.getInt("fishing-pufferfish-bonus");
                ecoDragonPlayer.addPoint(point);
                player.sendMessage("[" + plg.getName() + "] ふぐだー！ (" + point + " pt)");
            } else if ((event.getCaught().getName().equalsIgnoreCase("Tropical fish")) || (event.getCaught().getName().equalsIgnoreCase("Clownfish"))) {
                point = conf.getInt("fishing-clownfish-bonus");
                ecoDragonPlayer.addPoint(point);
                player.sendMessage("[" + plg.getName() + "] くまのみだー！ (" + point + " pt)");
            } else if (event.getCaught().getName().equalsIgnoreCase("Enchanted Book")) {
                point = conf.getInt("fishing-enchantbook-bonus");
                ecoDragonPlayer.addPoint(point);
                player.sendMessage("[" + plg.getName() + "] エンチャント本だー！ (" + point + " pt)");
            } else if (event.getCaught().getName().equalsIgnoreCase("Name Tag")) {
                point = conf.getInt("fishing-nametag-bonus");
                ecoDragonPlayer.addPoint(point);
                player.sendMessage("[" + plg.getName() + "] 名札だー！ (" + point + " pt)");
            } else if (event.getCaught().getName().equalsIgnoreCase("Nautilus Shell")) {
                point = conf.getInt("fishing-shell-bonus");
                ecoDragonPlayer.addPoint(point);
                player.sendMessage("[" + plg.getName() + "] 化石だー！ (" + point + " pt)");
            } else if (event.getCaught().getName().equalsIgnoreCase("Saddle")) {
                point = conf.getInt("fishing-saddle-bonus");
                ecoDragonPlayer.addPoint(point);
                player.sendMessage("[" + plg.getName() + "] サドルだー！ (" + point + " pt)");
            } else {
                point = conf.getInt("fishing-trash");
                ecoDragonPlayer.addPoint(point);
                player.sendMessage("[" + plg.getName() + "] ごみだー！ (" + point + " pt)");
            }
        }
        refreshScoreBoard();
    }

    /**
     * エンドラ戦中透明化ポーション抑止(残留)
     *
     * @param event
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void LingeringPotionSplashEvent(LingeringPotionSplashEvent event) {

        ThrownPotion potion = event.getEntity();
        if (potion.getWorld().equals(curWorld)) {
            for (PotionEffect po : potion.getEffects()) {
                if (po.getType() == PotionEffectType.INVISIBILITY) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    /**
     * エンドラ戦中透明化ポーション抑止(投合)
     *
     * @param event
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void PotionSplash(PotionSplashEvent event) {
        ThrownPotion potion = event.getEntity();
        if (potion.getWorld().equals(curWorld)) {
            for (PotionEffect po : potion.getEffects()) {
                if (po.getType() == PotionEffectType.INVISIBILITY) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    /**
     * エンドラ戦中透明化ポーション抑止(飲用)
     *
     * @param event
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPotionDrink(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (curWorld != null) {
            if (curWorld.equals(player.getWorld())) {
                if (((event.getAction() == Action.RIGHT_CLICK_AIR) || (event.getAction() == Action.RIGHT_CLICK_BLOCK)) && (player.getItemInHand().getType() == Material.POTION)) {
                    if (((PotionMeta) player.getItemInHand().getItemMeta()).getBasePotionData().getType() == PotionType.INVISIBILITY) {
                        int hs = player.getInventory().getHeldItemSlot();
                        drink(player, hs);
                    }
                }
            }
        }
    }

    public void drink(final Player p, int hs) {
        plg.getServer().getScheduler().scheduleSyncDelayedTask(plg, new Runnable() {
            public void run() {
                if (p.getInventory().getItem(hs).getType() == Material.GLASS_BOTTLE) {
                    // 透明化が掛かっていたら解除する
                    for (PotionEffect pe : p.getActivePotionEffects()) {
                        if (pe.getType().equals(PotionEffectType.INVISIBILITY)) {
                            p.removePotionEffect(PotionEffectType.INVISIBILITY);
                        }
                    }
                }
            }
        }, 35);
    }

    /**
     * エンドラ討伐時メッセージ
     */
    private void roundMessage() {
        // ラウンドに応じてメッセージ出力する
        if (round == conf.getInt("roundmax")) {
            plg.getServer().broadcastMessage("§d[" + plg.getName() + "]§f 強化エンダードラゴン戦が開始されました");
            plg.getServer().broadcastMessage("§d[" + plg.getName() + "]§f あと " + round + " 回エンダードラゴンを討伐するとエンドゲートウェイの転送が開放されます。");
            plg.getServer().broadcastMessage("§d[" + plg.getName() + "]§f エンドゲートウェイの開放は個人ごとに (ランキング順位 * " + gateReleaseinterval + "秒) のインターバルを要します");
            for (Player pl : plg.getServer().getOnlinePlayers()) {
                this.plg.getServer().dispatchCommand(this.plg.getServer().getConsoleSender(), "tellraw " + pl.getName() + " [\"\",{\"text\":\"\\u30e9\\u30f3\\u30ad\\u30f3\\u30b0\\u78ba\\u8a8d\\u306f \"},{\"text\":\"/ecd rank\",\"bold\":true,\"underlined\":true,\"color\":\"yellow\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/ecd rank\"}}]");
                this.plg.getServer().dispatchCommand(this.plg.getServer().getConsoleSender(), "tellraw " + pl.getName() + " [\"\",{\"text\":\"\\u30b9\\u30b3\\u30a2\\u30dc\\u30fc\\u30c9\\u306e\\u975e\\u8868\\u793a\\u306f \"},{\"text\":\"/ecd rank false\",\"bold\":true,\"underlined\":true,\"color\":\"yellow\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/ecd rank false\"}}]");
            }
        } else if (round != 0) {
            // ラウンド継続中の場合
            plg.getServer().broadcastMessage("§d[" + plg.getName() + "]§f あと " + round + " 回エンダードラゴンを討伐するとエンドゲートウェイの転送が開放されます。");
            plg.getServer().broadcastMessage("§d[" + plg.getName() + "]§f エンダードラゴンの復活は自動的には行われませんのでご注意ください");
            plg.getServer().broadcastMessage("§d[" + plg.getName() + "]§f エンドゲートウェイの開放は個人ごとに (ランキング順位 * " + gateReleaseinterval + "秒) のインターバルを要します");
        } else {
            // 最終ラウンド後の場合
            plg.getServer().broadcastMessage("§d[" + plg.getName() + "]§f 強化エンダードラゴン戦が終了しました");
            plg.getServer().broadcastMessage("§d[" + plg.getName() + "]§f エンドゲートウェイの転送が順次開放されます。");
            plg.getServer().broadcastMessage("§d[" + plg.getName() + "]§f エンドゲートウェイの開放は個人ごとに (ランキング順位 * " + gateReleaseinterval + "秒) のインターバルを要します");
        }
    }

    /**
     * エンドラランキングの公開処理とゲート開放時間計測処理を行う
     */
    private void endRankingPresent() {
        int totalPoint = 0;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        // エンドラ戦中でなければ何もしない
        if (curWorld == null) return;

        // ランキング集計
        log.info("ranking集計スタート ");
        ArrayList entries = getRankList();
        try {
            totalPoint = 0;
            Long cur = new Date().getTime();
            // 全体のポイントを集計する、ゲート開放予定時刻を算出
            int skipUser = 0;
            for (int rank = 1; rank <= entries.size(); rank++) {
                EcoDragonPlayer rankUser = (EcoDragonPlayer) ((Map.Entry) entries.get(rank - 1)).getValue();
                totalPoint += rankUser.getPoint();
                lastInterval = cur + 10000 + (rank - 1 - skipUser) * gateReleaseinterval * 1000; // interval 5 min
                Player player = rankUser.getPlayer();
                if (player == null) {
                    skipUser++;
                    continue;
                }
                UUID uuid = player.getUniqueId();
                if (uuid == null) {
                    skipUser++;
                    continue;
                }
                intervalList.put(rankUser.getPlayer().getUniqueId(), lastInterval);
                new GatewayAnnounce(plg, rankUser.getPlayer(), lastInterval).runTaskTimer(plg, 100, 100);
            }
            // 個々人のランキング表彰、アイテム進呈
            for (int rank = 1; rank <= entries.size(); rank++) {
                EcoDragonPlayer rankUser = (EcoDragonPlayer) ((Map.Entry) entries.get(rank - 1)).getValue();
                Player player = rankUser.getPlayer();
                if (player == null) {
                    log.warning("rank = " + rank + ", getPlayer failed UUID=" + rankUser.playerUUID.toString());
                    continue;
                }
                log.info("ranking:" + (rank) + "位:" + player.getName());
                int per = (rankUser.getPoint() * 100) / totalPoint;
                if (rank <= 3) {
                    plg.getServer().broadcastMessage("§d[" + plg.getName() + "]§f 討伐ランキング上位 [" + rank + "位:" + player.getName() + "](" + rankUser.getPoint() + "/" + totalPoint + " ポイント(" + per + "%))");
                }
                int lv = 0;
                rankUser.setRanking(true);
                switch (rank) {
                    case 1:
                        lv = 30 * 4;
                        presentItem(player, Material.DIAMOND_BLOCK, 5);
                        presentItem(player, Material.EMERALD_BLOCK, 3);
                        presentItem(player, makeCertificate(player.getName()));
                        break;
                    case 2:
                        lv = 30 * 3;
                        presentItem(player, Material.DIAMOND_BLOCK, 4);
                        presentItem(player, Material.EMERALD_BLOCK, 3);
                        break;
                    case 3:
                        lv = 30 * 2;
                        presentItem(player, Material.DIAMOND_BLOCK, 3);
                        presentItem(player, Material.EMERALD_BLOCK, 1);
                        break;
                    default:
                        rankUser.setRanking(false);
                }
                player.sendMessage("§d[" + plg.getName() + "]§f EnderDragon討伐ランキング あなたは[" + rank + "位:" + rankUser.getPoint() + " / " + totalPoint + " ポイント(" + per + "%)]でした");
                if (rankUser.getRanking()) {
                    player.setLevel(player.getLevel() + lv);
                    player.sendMessage("§d[" + plg.getName() + "]§f 討伐ボーナス [" + lv + " LV] 獲得しました");
                }
                if (intervalList.containsKey(player.getUniqueId())) {
                    player.sendMessage("§d[" + plg.getName() + "]§f あなたのエンドゲートウェイ開放時刻は" + sdf.format(new Date(intervalList.get(player.getUniqueId()))) + "頃です");
                }
            }
        } catch (IndexOutOfBoundsException e) {
            // 人数が足りなくて順位表示できない場合は中断
            log.info("IndexOutOfBoundsException");
        }
        //
        for (Player player : plg.getServer().getOnlinePlayers()) {
            if (ecoDragonUserList.ContainsEcoDragonUser(player)) {
                EcoDragonPlayer ecoDragonPlayer = ecoDragonUserList.getEcoDragonPlayer(player);
                // すでに賞品もらってたらキャンセル
                if (ecoDragonPlayer != null) {
                    if (ecoDragonPlayer.getRanking() == true) {
                        continue;
                    }
                }
                // 参加賞
                player.setLevel(player.getLevel() + 30);
                player.sendMessage("§d[" + plg.getName() + "]§f 討伐参加賞 [30 LV] 獲得しました");
                presentItem(player);
            }
        }
        ecoDragonUserList.clearMap();
    }

    /**
     * 賞状作成処理
     *
     * @param name 表彰者名
     * @return 賞状インスタンス
     */
    public ItemStack makeCertificate(String name) {
        SimpleDateFormat sdf1 = new SimpleDateFormat("[yyyy/MM/dd]");
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy年MM月dd日 HH時mm分ss秒");
        ItemStack item = new ItemStack(Material.WRITTEN_BOOK);
        Date date = new Date();
        BookMeta meta = (BookMeta) item.getItemMeta();
        meta.setAuthor(cerConf.getString("author"));
        meta.setDisplayName(sdf1.format(date) + cerConf.getString("name"));
        meta.setTitle(sdf1.format(date) + cerConf.getString("title"));
        for (String page : cerConf.getArrayList("pages")) {
            meta.addPage(page + "\n\n§c§l成績 1 位 プレイヤー[" + name + "]§r\n" + sdf2.format(date));
        }
        item.setItemMeta(meta);
        return item;
    }

    /**
     * 参加賞進呈処理
     *
     * @param player 進呈プレイヤー
     */
    private void presentItem(Player player) {
        presentItem(player, Material.AIR, 1);
    }

    /**
     * アイテム進呈処理
     *
     * @param player      進呈プレイヤー
     * @param item   アイテム指定
     * @param amount 個数指定
     */
    private void presentItem(Player player, Material item, int amount) {
        ItemStack i = null;
        if (item == Material.AIR) {
            ArrayList<ItemStack> list = new ArrayList<>();
            list.add(new ItemStack(Material.ANCIENT_DEBRIS, 1));
            list.add(new ItemStack(Material.BEEHIVE, 3));
            list.add(new ItemStack(Material.BIG_DRIPLEAF, 16));
            list.add(new ItemStack(Material.BONE_BLOCK, 16));
            list.add(new ItemStack(Material.BONE_BLOCK, 16));
            list.add(new ItemStack(Material.BOOKSHELF, 16));
            list.add(new ItemStack(Material.BROWN_MUSHROOM, 10));
            list.add(new ItemStack(Material.CANDLE, 32));
            list.add(new ItemStack(Material.COBWEB, 32));
            list.add(new ItemStack(Material.CRAFTING_TABLE));
            list.add(new ItemStack(Material.DIAMOND_BLOCK, 3));
            ItemStack rod = new ItemStack(Material.FISHING_ROD, 1);
            rod.addUnsafeEnchantment(Enchantment.LUCK, 5);
            rod.setData(null);
            ItemMeta rodData = rod.getItemMeta();
            rodData.setDisplayName("えこ釣り竿");
            rod.setItemMeta(rodData);
            list.add(rod);
            list.add(new ItemStack(Material.EMERALD_BLOCK, 3));
            list.add(new ItemStack(Material.FLINT, 64));
            list.add(new ItemStack(Material.GOLDEN_APPLE, 3));
            list.add(new ItemStack(Material.GRASS_BLOCK, 64));
            list.add(new ItemStack(Material.HAY_BLOCK, 32));
            list.add(new ItemStack(Material.LIGHTNING_ROD, 32));
            list.add(new ItemStack(Material.LAPIS_BLOCK));
            list.add(new ItemStack(Material.MOSSY_COBBLESTONE, 64));
            list.add(new ItemStack(Material.POPPY, 10));
            list.add(new ItemStack(Material.RED_MUSHROOM, 10));
            list.add(new ItemStack(Material.SOUL_SOIL, 64));
            i = list.get(new Random().nextInt(list.size()));
            player.sendMessage("§d[" + plg.getName() + "]§f 討伐参加賞アイテム [" + i.getType().name() + "] x " + i.getAmount() + " 獲得しました");
        } else {
            i = new ItemStack(item, amount);
            player.sendMessage("§d[" + plg.getName() + "]§f 討伐ボーナスアイテム [" + i.getType().name() + "] x " + i.getAmount() + "獲得しました");
        }
        player.getInventory().addItem(i);
    }

    /**
     * 賞状進呈処理
     *
     * @param player 表彰プレイヤー
     * @param itemStack 賞状インスタンス
     */
    private void presentItem(Player player, ItemStack itemStack) {
        player.getInventory().addItem(itemStack);
        player.sendMessage("§d[" + plg.getName() + "]§f 討伐ボーナスアイテム [エンドラ討伐ランキング賞状] 獲得しました");
    }

    public void setFirst() {
        first = false;
    }

    /**
     * エンドラランキング開始処理
     *
     * @param world 開始ワールド名
     * @return 開始結果
     */
    public boolean startEnderDragonRanking(World world, boolean force) {

        // 開始済みであれば何もしない
        if (curWorld != null) {
            if (force) {
                endEnderDragonRanking();
            } else {
                return false;
            }
        }

        // 暫定終了処理
        // 一回やったらもうやらない
        if ((!force) && (first)) {
            log.info("一回終了済み");
            return false;
        }

        // 開始条件チェック
        if ((force) || checkEnderDragon(world)) {
            curWorld = world;
            round = conf.getInt("roundmax");
            ecoDragonUserList.clearMap();
            intervalList.clear();
            world.setDifficulty(Difficulty.HARD);
            world.setPVP(true);
            world.setGameRule(GameRule.KEEP_INVENTORY, true);

            // タイマ起動
            for (TimerFrame tm : pvptimer.values()) {
                tm.cancel();
            }
            pvptimer.clear();
            for (TimerFrame tm : evtimer) {
                tm.cancel();
            }
            evtimer.clear();
            timer = new WorldTimer(plg, this);
            timer.runTaskTimer(plg, 0, 200);
            evtimer.add(timer);

            // スコアボード初期化
            resetScoreboard();

            roundMessage();
            return true;
        }

        return false;
    }

    /**
     * エンドラ戦強制終了処理
     *
     * @return 終了結果
     */
    public boolean abortEnderDragonRanking() {
        return endEnderDragonRanking();
    }

    /**
     * エンドラランキング終了処理
     *
     * @return 終了結果
     */
    private boolean endEnderDragonRanking() {
        // エンドラ戦中でなければ何もしない
        if (curWorld == null) {
            log.info("終了済み");
            return false;
        }

        //エンドラ戦終わったワールドを記録
        ArrayList<String> list = (ArrayList<String>) conf.getArrayList("stopworld");
        if (!list.contains(curWorld.getName())) {
            list.add(curWorld.getName());
            conf.getConf().set("stopworld", list);
            conf.saveConfig();
            conf.reload();
        }

        // 設定戻し
        WorldBorder bd = curWorld.getWorldBorder();
        bd.setDamageAmount(2);
        bd.setDamageBuffer(10);
        bd.setWarningDistance(20);
        bd.setWarningTime(10);
        bd.setSize(4000, 100);
        curWorld.setPVP(false);
        for (TimerFrame tm : pvptimer.values()) {
            tm.cancel();
        }
        pvptimer.clear();
        EndEventTimer e = null;
        e = new EndEventTimer(plg, "1200 tick後に World:" + curWorld.getName() + " のエンドラ戦後処理を開始します");
        e.runTaskLater(plg, 1);
        evtimer.add(e);
        e = new EndEventTimer(plg, "1000 tick後に World:" + curWorld.getName() + " のエンドラ戦後処理を開始します");
        e.runTaskLater(plg, 200);
        evtimer.add(e);
        e = new EndEventTimer(plg, "800 tick後に World:" + curWorld.getName() + " のエンドラ戦後処理を開始します");
        e.runTaskLater(plg, 400);
        evtimer.add(e);
        e = new EndEventTimer(plg, "600 tick後に World:" + curWorld.getName() + " のエンドラ戦後処理を開始します");
        e.runTaskLater(plg, 600);
        evtimer.add(e);
        e = new EndEventTimer(plg, "400 tick後に World:" + curWorld.getName() + " のエンドラ戦後処理を開始します");
        e.runTaskLater(plg, 800);
        evtimer.add(e);
        e = new EndEventTimer(plg, "200 tick後に World:" + curWorld.getName() + " のエンドラ戦後処理を開始します");
        e.runTaskLater(plg, 1000);
        evtimer.add(e);
        e = new EndEventTimer(plg, "World:" + curWorld.getName() + " のKeepInventoryを解除しました。", curWorld);
        e.runTaskLater(plg, 1200);
        evtimer.add(e);

        // スコアボード破棄
        e = new EndEventTimer(plg, "ランキングスコアボードを破棄しました。", board, dmgobj);
        e.runTaskLater(plg, 1200);
        evtimer.add(e);

        curWorld = null;

        return false;
    }

    /**
     * スコアボード初期化処理
     */
    private void resetScoreboard() {
        board = plg.getServer().getScoreboardManager().getNewScoreboard();
        dmgobj = board.getObjective("damage");
        if (dmgobj != null) {
            dmgobj.unregister();
            dmgobj = null;
        }
        dmgobj = board.registerNewObjective("damage", Criteria.TEAM_KILL_YELLOW, "dummy");
        dmgobj.setDisplayName("エンドラ討伐貢献度");
        dmgobj.setDisplaySlot(DisplaySlot.SIDEBAR);

        for (Player player : plg.getServer().getOnlinePlayers()) {
            setScoreboard(player, true);
        }
    }

    /**
     * スコアボード設定処理
     */
    public void setScoreboard(Player player, boolean flag) {
        if (player.hasPermission("ecodragon.board")) {
            if (flag) {
                player.setScoreboard(board);
            } else {
                if (player.getScoreboard().equals(board)) {
                    player.setScoreboard(plg.getServer().getScoreboardManager().getMainScoreboard());
                }
            }
        }
    }

    /**
     * エンドラランキング開始条件チェック処理
     *
     * @param world ワールド名
     * @return 開始可否
     */
    private boolean checkEnderDragon(World world) {
        // ランキング開始済みであれば本メソッドはコールしないこと

        // 指定ワールドがエンドラランキング対象かチェック
        if (!world.getName().toLowerCase().startsWith(conf.getString("worldprefix").toLowerCase())) {
            return false;
        }

        // 未実施か？
        ArrayList<String> list = (ArrayList<String>) conf.getArrayList("stopworld");
        if (list.contains(world.getName())) {
            return false;
        }

        // エンドラチェック
        log.info("EnderDragonCheck[" + world.getName() + "]");
        for (Entity ent : world.getEntities()) {
            if (ent.getType() == EntityType.ENDER_DRAGON) {
                return true;
            }
        }
        for (LivingEntity ent : world.getLivingEntities()) {
            if (ent.getType() == EntityType.ENDER_DRAGON) {
                return true;
            }
        }
        log.info("unhit:EnderDragon");

        return false;
    }

    /**
     * ランキングデータ取得処理
     *
     * @return
     */
    @SuppressWarnings("unchecked")
    public ArrayList getRankList() {
        ArrayList entries = new ArrayList(ecoDragonUserList.getEcoDragonPlayerMap().entrySet());
        Collections.sort(entries, new Comparator() {
            public int compare(Object obj1, Object obj2) {
                Map.Entry ent1 = (Map.Entry) obj1;
                Map.Entry ent2 = (Map.Entry) obj2;
                EcoDragonPlayer val1 = (EcoDragonPlayer) ent1.getValue();
                EcoDragonPlayer val2 = (EcoDragonPlayer) ent2.getValue();
                return (val2.getPoint()) - (val1.getPoint());
            }
        });
        return entries;
    }

    public void addPoint(Player p, int i) {
        EcoDragonPlayer ecoDragonPlayer = ecoDragonUserList.getEcoDragonPlayer(p);
        ecoDragonPlayer.addPoint(i);
    }

    public boolean isRanking() {
        return (curWorld != null);
    }

    public World getWorld() {
        return curWorld;
    }


    public void refreshScoreBoard() {
        for (EcoDragonPlayer rankUser : ecoDragonUserList.getEcoDragonPlayerMap().values()) {
            dmgobj.getScore(rankUser.getName()).setScore(rankUser.getPoint());
        }
    }

    public void setPlayerPvP(Player player, boolean pvp_) {
        EcoDragonPlayer ecoDragonPlayer = ecoDragonUserList.getEcoDragonPlayer(player);
        UUID uuid = player.getUniqueId();
        ecoDragonPlayer.setPvP(pvp_);
        if (pvp_) {
            if (pvptimer.containsKey(uuid)) {
                pvptimer.get(uuid).cancel();
                pvptimer.remove(uuid);
            }

            EndPvPTimer t = new EndPvPTimer(plg, player);
            t.runTaskTimer(plg, 0, 20 * 5);
            pvptimer.put(uuid, t);
        } else {
            if (pvptimer.containsKey(uuid)) {
                pvptimer.get(uuid).cancel();
                pvptimer.remove(uuid);
            }
        }
        return;
    }

    public boolean isExistPlayer(Player player) {
        return ecoDragonUserList.ContainsEcoDragonUser(player);
    }

    public boolean isPlayerPvP(Player player) {
        boolean result = false;
        result = ecoDragonUserList.getEcoDragonPlayer(player).isPvP();
        return result;
    }

    public boolean checkCanPVP(Player attacker, Player target) {
        return isPlayerPvP(attacker) && isPlayerPvP(target);
    }
}
