
package jp.minecraftuser.ecodragon.listener;

import static jp.minecraftuser.ecoframework.Utl.sendPluginMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import jp.minecraftuser.ecodragon.timer.MobKillTimer;
import jp.minecraftuser.ecoframework.ListenerFrame;
import jp.minecraftuser.ecoframework.PluginFrame;
import jp.minecraftuser.ecoegg.EcoEgg;
import jp.minecraftuser.ecoegg.EcoEggUtil;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Egg;
import org.bukkit.entity.CaveSpider;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.Player;
import org.bukkit.entity.Stray;
import org.bukkit.entity.Warden;
import org.bukkit.entity.WitherSkeleton;
import org.bukkit.entity.Zombie;
import org.bukkit.entity.Trident;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;

/**
 * エンドラ戦調整関連イベント処理リスナークラス
 * @author ecolight
 */
public class PowerDragonListener extends ListenerFrame {
    private static RankingListener ranking = null;
    private static double lasthp = 0;
    private static boolean bossdamage = false;
    public static int hp = 0;

    /**
     * コンストラクタ
     * @param plg_ プラグインインスタンス
     * @param name_ 名前
     */
    public PowerDragonListener(PluginFrame plg_, String name_) {
        super(plg_, name_);
        ranking = (RankingListener) plg.getPluginListener("ranking");
    }

    private boolean isFallingDamageCanceled(EntityDamageEvent event) {
        if (!ranking.isRanking()) return false;
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) return false;
        if (event.getEntity().getType() == EntityType.PLAYER) return false;
        if (!event.getEntity().getWorld().getName().equals(ranking.getWorld().getName())) return false;
        return true;
    }
    private boolean isEtcDamageCanceled(EntityDamageEvent event) {
        if (!ranking.isRanking()) return false;
        if (!event.getEntity().getWorld().getName().equals(ranking.getWorld().getName())) return false;
        switch (event.getCause()) {
            case LIGHTNING:
                switch (event.getEntityType()) {
                    case ENDER_DRAGON: return true;
                    case CREEPER:
                        event.setDamage(0);
                        break;
                }
                break;
            case FIRE_TICK:
                switch (event.getEntityType()) {
                    case CREEPER: return true;
                }
                break;
        }
        return false;
    }
    private void playerLightningDamageExplosion(EntityDamageEvent event) {
        if (!ranking.isRanking()) return;
        if (!event.getEntity().getWorld().getName().equals(ranking.getWorld().getName())) return;
        if (event.getCause() != EntityDamageEvent.DamageCause.LIGHTNING) return;
        if (event.getEntityType() != EntityType.PLAYER) return;

        Player p = (Player)event.getEntity();
        Location loc = p.getLocation();
        loc.setY(loc.getY() - 1);

        // 一定高さ以上の雷ダメージはHPを半分削る(即死はしないよう+1)
        if (p.getLocation().getY() > 60) {
            event.setCancelled(true);
            p.setHealth(p.getHealth() / 2 + 1);
        }

        // 一定以上の雷ダメージは地形を損傷させる
        if (p.getLocation().getY() <= 55) return;

//        // 足元が空気ならば何もしない
//        if (loc.getBlock().getType() == Material.AIR) return;
//
        // 爆発エフェクト
        p.getWorld().createExplosion(loc, 1);

        // player周りのブロックを消滅☆
        Random rand = new Random();
        int area = 4 + rand.nextInt(7);
        Location ploc = p.getLocation();
        Location changeLoc = p.getLocation();
        Location checkLoc = null;
        for (int x = -area; x < area; x++) {
            changeLoc.setX(loc.getX() + x);
            for (int y = -area; y < area; y++) {
                changeLoc.setY(loc.getY() + y);
                for (int z = -area; z < area; z++) {
                    changeLoc.setZ(loc.getZ() + z);
                    if (ploc.distance(changeLoc) > area-1) continue;
//                    // 上下どちらかが水の黒曜石は破壊しない
//                    checkLoc = changeLoc.clone();
//                    checkLoc.setY(changeLoc.getY()+1);
//                    if ((changeLoc.getBlock().getType() == Material.OBSIDIAN) && ((checkLoc.getBlock().getType() == Material.WATER) || (checkLoc.getBlock().getType() == Material.STATIONARY_WATER))) continue;
//                    checkLoc = changeLoc.clone();
//                    checkLoc.setY(changeLoc.getY()-1);
//                    if ((changeLoc.getBlock().getType() == Material.OBSIDIAN) && ((checkLoc.getBlock().getType() == Material.WATER) || (checkLoc.getBlock().getType() == Material.STATIONARY_WATER))) continue;
                    // 水も破壊しない
                    if (changeLoc.getBlock().getType() == Material.WATER) continue;
                    if (changeLoc.getBlock().getType() == Material.LAVA) continue;
                    // あと看板も
                    if (changeLoc.getBlock().getType() == Material.OAK_SIGN) continue;
                    if (changeLoc.getBlock().getType().name().contains("SIGN")) continue;
                    // チェストも壊さない
                    //if (changeLoc.getBlock().getType() == Material.CHEST) continue;
                    //if (changeLoc.getBlock().getType() == Material.ENDER_CHEST) continue;

                    // 黒曜石と岩盤とゲートウェイは破壊しない
                    if (changeLoc.getBlock().getType() == Material.OBSIDIAN) continue;
                    if (changeLoc.getBlock().getType() == Material.BEDROCK) continue;
                    if (changeLoc.getBlock().getType() == Material.END_GATEWAY) continue;

                    changeLoc.getBlock().setType(Material.AIR);
                }
            }
        }

    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void EntityTargetLivingEntity(EntityTargetLivingEntityEvent event) {
        if (!ranking.isRanking()) return;
        if (!event.getEntity().getWorld().equals(ranking.getWorld())) return;
        LivingEntity target = event.getTarget();
        if (!(target instanceof Player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void EntityDamage(EntityDamageEvent event) {
        if (!ranking.isRanking()) return;
        if (!event.getEntity().getWorld().getName().equals(ranking.getWorld().getName())) return;
        // イベント中MOBの落下ダメージはキャンセル
        if (isFallingDamageCanceled(event)) {
            event.setCancelled(true);
        }
        // イベント中のMOBダメージその他(雷など)
        if (isEtcDamageCanceled(event)) {
            event.setCancelled(true);
            return;
        }

        // プレイヤーの雷ダメージで地形を粉砕
        playerLightningDamageExplosion(event);

        // エンドラ消滅救済用
//        if (lasthp > 0) {
//            boolean drahit = false;
//            for (Entity ent : event.getEntity().getWorld().getEntities()) {
//                if (ent.getType() == EntityType.ENDER_DRAGON) {
//                    drahit = true;
//                }
//            }
//            if (!drahit) {
//                EnderDragon endra = (EnderDragon)event.getEntity().getWorld().spawnEntity(event.getEntity().getLocation(), EntityType.ENDER_DRAGON);
//                endra.setHealth(lasthp);
//                plg.getServer().broadcastMessage("エンダードラゴンが行方不明なためクローン体が召喚されました");
//            }
//        }

        /* エンダードラゴン強化用ロジック */
        if ((event.getEntityType() == EntityType.ENDER_DRAGON) &&
            ((event.getCause() == EntityDamageEvent.DamageCause.PROJECTILE) ||
             (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK))) {

            Random rand = new Random();
            EnderDragon dra = (EnderDragon) event.getEntity();

            List<Player> survivalPlayers = new ArrayList<>();
            //サバイバルプレイヤーのリストを作成
            event.getEntity().getWorld().getPlayers().forEach(p -> {
                if (p.getGameMode() != GameMode.CREATIVE) {
                    survivalPlayers.add(p);
                }
            });

            lasthp = dra.getHealth();
            // HPの割合算出
            hp = ((int) dra.getHealth() * 100) / (int) dra.getMaxHealth();
            //m.info("hp:" + hp + " health:" + dra.getHealth() + " max:" + dra.getMaxHealth());

//            // 残存HP50%メッセージ
//            if ((bossdamage == false) && (hp <= 20)) {
//                for (Player p: dra.getWorld().getPlayers()) {
//                    p.sendMessage("§d[" + plg.getName() + "]§f エンダードラゴンの残存体力が低下、エンダードラゴンの様子が・・・・？");
//                    p.getWorld().playSound(p.getLocation(), Sound.ENTITY_ENDERDRAGON_DEATH, 0.5f, 0.5f);
//                }
//                PotionEffect potion = new PotionEffect(PotionEffectType.SPEED, 20*60*60*24*7*5, 5);
//                dra.addPotionEffect(potion);
//                dra.setHealth(dra.getMaxHealth());
//                bossdamage = true;
//            }

            // ドラゴンはHP50%以下の場合、遠距離武器を3/5の確立で無効化し、イベントをキャンセルする。
            if (event.getCause() == EntityDamageEvent.DamageCause.PROJECTILE) {
                if (event instanceof EntityDamageByEntityEvent) {
                    if ((hp < 50) && (rand.nextInt(5) > 1)) {
                        EntityDamageByEntityEvent entityDamageByEntityEvent = (EntityDamageByEntityEvent) event;
                        ProjectileSource attackEntity = null;
                        Entity damager = entityDamageByEntityEvent.getDamager();
                        switch (damager.getType()) {
                            case ARROW:
                                attackEntity = ((Arrow) damager) .getShooter();
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
                        }
                        if (attackEntity instanceof Player) {
                            Player attackPlayer = (Player) attackEntity;
                            //ダメージを弾いた音を流す
                            attackPlayer.playSound(attackPlayer.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1, 0.1f);
                        }
                        event.setCancelled(true);
                        return;
                    }
                }
            }

            // 魔女を投下
            event.getEntity().getWorld().spawnEntity(event.getEntity().getLocation(), EntityType.WITCH);

            // ドラゴンに落雷
            if (hp < 20) {
                dra.getWorld().strikeLightning(dra.getLocation());
            }

            if (!survivalPlayers.isEmpty()) {
                // プレイヤーリストと初期ターゲット取得
                Player targetPlayer = survivalPlayers.get(rand.nextInt(survivalPlayers.size()));

                // 1/20 の確立でドラゴンをランダムプレイヤー下の位置にテレポート
                if (rand.nextInt(20) == 1) {
                    Location draloc = targetPlayer.getLocation();
                    draloc.setY(draloc.getY() - 5);
                    if (draloc.getBlock().getType() != Material.OBSIDIAN) {
                        if (draloc.getY() >= 10) {
                            dra.teleport(draloc);
                        }
                    }
                }
                // 1/10 の確立でランダムプレイヤーの位置にウィザー召還
                if (rand.nextInt(10) == 1) {
                    Location witherloc = targetPlayer.getLocation();
                    witherloc.setY(witherloc.getY() + 5);
                    if (witherloc.getY() <= 60) witherloc.setY(70);
                    event.getEntity().getWorld().spawnEntity(witherloc, EntityType.WITHER);
                }

                // エンドラが特定HP未満の場合、プレイヤーに落雷させる
                int tgtcnt = 0;
                if (hp < 90) {
                    tgtcnt = 1;
                }
                if (hp < 50) {
                    tgtcnt = 3;
                }
                if (hp < 20) {
                    tgtcnt = 5;
                }
                if (hp < 10) {
                    tgtcnt = 7;
                }

                for (int loop = 0; loop < tgtcnt; loop++) {
                    // ランダムプレイヤー算出
                    Player lightningTargetPlayer = survivalPlayers.get(rand.nextInt(survivalPlayers.size()));

                    // プレイヤーの居る位置が高さ60以下だったら対象外
                    if (lightningTargetPlayer.getLocation().getY() <= 60) {
                        tgtcnt++;
                        if (tgtcnt >= 20) { // 最大試行20回
                            break;
                        }
                        continue;
                    }
                    if (hp > 0) {
                        dra.getWorld().strikeLightning(lightningTargetPlayer.getLocation());
                    }
                }
                int mobcount = 0;
                // ドラゴンはHPの残量に応じてMOBを投下する
                if (hp > 80) {
                    mobcount = 2;   // 80%以上の場合のMOB量
                } else if (hp > 60) {
                    mobcount = 2;   // 60%以上の場合のMOB量
                } else if (hp > 40) {
                    mobcount = 2;   // 40%以上の場合のMOB量
                } else if (hp > 20) {
                    mobcount = 1;   // 20%以上の場合のMOB量
                } else {
                    mobcount = 5;   // 19%以下の場合のMOB量
                }

                for (int cnt = 0; cnt < mobcount; cnt++) {
                    // ランダムプレイヤーを算出
                    Player monsterTargetPlayer = survivalPlayers.get(rand.nextInt(survivalPlayers.size()));

                    // MOB召還
                    if (hp > 90) {          // HPが80%以上の場合のMOB
                        CaveSpider ent = (CaveSpider) event.getEntity().getWorld().spawnEntity(event.getEntity().getLocation(),
                                EntityType.CAVE_SPIDER);
                        ent.setTarget(monsterTargetPlayer);
                    } else if (hp > 80) {   // HPが60%以上の場合のMOB
                        Stray ent = spawnSkeleton(event.getEntity());
                        ent.setTarget(monsterTargetPlayer);
                    } else if (hp > 60) {   // HPが40%以上の場合のMOB
                        Zombie ent = spawnZombie(event.getEntity());
                        ent.setTarget(monsterTargetPlayer);
                    } else if (hp > 40) {   // HPが20%以上の場合のMOB
                        Phantom ent = spawnPhantom((Player) event.getEntity(), event.getEntity().getLocation(), 0, 3);
                        ent.setTarget(monsterTargetPlayer);
                    } else if (hp > 20) {   // HPが20%以上の場合のMOB
                        WitherSkeleton ent = spawnWitherSkeleton(event.getEntity());
                        ent.setTarget(monsterTargetPlayer);
                    } else {                // HPが19%以下の場合のMOB
                        Creeper ent = (Creeper) event.getEntity().getWorld().spawnEntity(event.getEntity().getLocation(),
                                EntityType.CREEPER);
                        AttributeInstance attr = ent.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
                        attr.setBaseValue(attr.getBaseValue() * 2);
                        ent.setTarget(monsterTargetPlayer);
                    }
                }
            }
        }
    }
    private ItemStack addAtkEnchant(ItemStack item) {
        item.addEnchantment(Enchantment.KNOCKBACK, 2);
        item.addEnchantment(Enchantment.DAMAGE_ALL, 5);
        item.addEnchantment(Enchantment.FIRE_ASPECT, 2);
        item.addEnchantment(Enchantment.DURABILITY, 3);
        return item;
    }
    private ItemStack addShotEnchant(ItemStack item) {
        item.addEnchantment(Enchantment.ARROW_DAMAGE, 5);
        item.addEnchantment(Enchantment.ARROW_FIRE, 1);
        item.addEnchantment(Enchantment.ARROW_INFINITE, 1);
        item.addEnchantment(Enchantment.ARROW_KNOCKBACK, 2);
        return item;
    }
    private ItemStack addDefEnchant(ItemStack item) {
        item.addEnchantment(Enchantment.PROTECTION_PROJECTILE, 4);
        item.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
        item.addEnchantment(Enchantment.THORNS, 3);
        item.addEnchantment(Enchantment.DURABILITY, 3);
        return item;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void EntityDeath(EntityDeathEvent event) {
        if ( ! event.getEntity().getWorld().getName().toLowerCase().startsWith(conf.getString("worldprefix").toLowerCase())) {
            return;
        }
        EntityType type = event.getEntityType();
        if (null != type) 
            switch (type) {
            /* エンダードラゴン強化用ロジック */
            case ENDER_DRAGON:
                /* 一度全MOBを消滅 */
                for (Entity ent : event.getEntity().getWorld().getEntities()) {
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
                    if (ent.getType() == EntityType.PHANTOM) ent.remove();
                    if (ent.getType() == EntityType.WITHER_SKELETON) ent.remove();
                    if (ent.getType() == EntityType.SKELETON) ent.remove();
                    if (ent.getType() == EntityType.WARDEN) ent.remove();
                }   bossdamage = false;
                Player killer = event.getEntity().getKiller();
                if (killer != null) {
                    plg.getServer().broadcastMessage("§d[" + plg.getName() + "]§f ["+event.getEntity().getKiller().getName()+"]がエンダードラゴンを撃破");
                }
                if (ranking.isRanking()) {
                    World w = event.getEntity().getWorld();
                    Location l = event.getEntity().getLocation();
                    for (int cnt = 0; cnt < 8; cnt++) {

                        w.spawnEntity(l, EntityType.ENDERMAN);
                        w.spawnEntity(l, EntityType.GHAST);
                        w.spawnEntity(l, EntityType.CREEPER);
                    }
                    for (int cnt = 0; cnt < 4; cnt++) {
                        spawnWarden(event.getEntity());
                    }
                }
                break;
            case WITHER:
                if (ranking.isRanking()) {
                    spawnWitherSkeleton(event.getEntity());
                }
                break;
            case ENDERMAN:
                if (ranking.isRanking()) {
                    event.setDroppedExp(event.getDroppedExp()*3);
                }
                break;
            default:
                break;
        }
    }

    public WitherSkeleton spawnWitherSkeleton(Entity entity) {
        WitherSkeleton ent = (WitherSkeleton)entity.getWorld().spawnEntity(entity.getLocation(), EntityType.WITHER_SKELETON);
        ent.getEquipment().setItemInMainHand(addAtkEnchant(new ItemStack(Material.DIAMOND_SWORD)));
        ent.getEquipment().setItemInMainHandDropChance(0.001f);
        ent.getEquipment().setBoots(addDefEnchant(new ItemStack(Material.DIAMOND_BOOTS)));
        ent.getEquipment().setBootsDropChance(0.001f);
        ent.getEquipment().setChestplate(addDefEnchant(new ItemStack(Material.DIAMOND_CHESTPLATE)));
        ent.getEquipment().setChestplateDropChance(0.001f);
        ent.getEquipment().setHelmet(addDefEnchant(new ItemStack(Material.DIAMOND_HELMET)));
        ent.getEquipment().setHelmetDropChance(0.001f);
        ent.getEquipment().setLeggings(addDefEnchant(new ItemStack(Material.DIAMOND_LEGGINGS)));
        ent.getEquipment().setLeggingsDropChance(0.001f);
        // ステータス
        ent.setCanPickupItems(false);
        ent.setCustomName("闇の眷属(骨)");
        ent.setCustomNameVisible(true);
        ent.setRemoveWhenFarAway(false);

        PotionEffect p = new PotionEffect(PotionEffectType.SPEED, 20*60*60*24*7*5, 5);
        ent.addPotionEffect(p);
        p = new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 20*60*60*24*7*5, 5);
        ent.addPotionEffect(p);

        return ent;
    }
    public Stray spawnSkeleton(Entity entity) {
        Stray ent = (Stray)entity.getWorld().spawnEntity(entity.getLocation(), EntityType.STRAY);
        ent.getEquipment().setItemInMainHand(addShotEnchant(new ItemStack(Material.BOW)));
        ent.getEquipment().setItemInMainHandDropChance(0.001f);
        ent.getEquipment().setBoots(addDefEnchant(new ItemStack(Material.DIAMOND_BOOTS)));
        ent.getEquipment().setBootsDropChance(0.001f);
        ent.getEquipment().setChestplate(addDefEnchant(new ItemStack(Material.DIAMOND_CHESTPLATE)));
        ent.getEquipment().setChestplateDropChance(0.001f);
        ent.getEquipment().setHelmet(addDefEnchant(new ItemStack(Material.DIAMOND_HELMET)));
        ent.getEquipment().setHelmetDropChance(0.001f);
        ent.getEquipment().setLeggings(addDefEnchant(new ItemStack(Material.DIAMOND_LEGGINGS)));
        ent.getEquipment().setLeggingsDropChance(0.001f);
        // ステータス
        ent.setCanPickupItems(false);
        ent.setCustomName("闇の眷属(弓)");
        ent.setCustomNameVisible(true);
        ent.setRemoveWhenFarAway(false);

        PotionEffect p = new PotionEffect(PotionEffectType.SPEED, 20*60*60*24*7*5, 5);
        ent.addPotionEffect(p);
        p = new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 20*60*60*24*7*5, 5);
        ent.addPotionEffect(p);

        return ent;
    }
    public Zombie spawnZombie(Entity entity) {
        Zombie ent = (Zombie)entity.getWorld().spawnEntity(entity.getLocation(),
                EntityType.ZOMBIE);
        ent.getEquipment().setItemInMainHand(addAtkEnchant(new ItemStack(Material.DIAMOND_SWORD)));
        ent.getEquipment().setItemInMainHandDropChance(0.001f);
        ent.getEquipment().setBoots(addDefEnchant(new ItemStack(Material.DIAMOND_BOOTS)));
        ent.getEquipment().setBootsDropChance(0.001f);
        ent.getEquipment().setChestplate(addDefEnchant(new ItemStack(Material.DIAMOND_CHESTPLATE)));
        ent.getEquipment().setChestplateDropChance(0.001f);
        ent.getEquipment().setHelmet(addDefEnchant(new ItemStack(Material.DIAMOND_HELMET)));
        ent.getEquipment().setHelmetDropChance(0.001f);
        ent.getEquipment().setLeggings(addDefEnchant(new ItemStack(Material.DIAMOND_LEGGINGS)));
        ent.getEquipment().setLeggingsDropChance(0.001f);
        
        // ステータス
        ent.setCanPickupItems(false);
        ent.setCustomName("闇の眷属(腐)");
        ent.setCustomNameVisible(true);
        ent.setRemoveWhenFarAway(false);
        PotionEffect p = new PotionEffect(PotionEffectType.SPEED, 20*60*60*24*7*5, 5);
        ent.addPotionEffect(p);
        p = new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 20*60*60*24*7*5, 5);
        ent.addPotionEffect(p);
        return ent;
    }
    public Warden spawnWarden(Entity entity) {
        Warden ent = (Warden)entity.getWorld().spawnEntity(entity.getLocation(), EntityType.WARDEN);
        // ステータス
        ent.setCanPickupItems(false);
        ent.setCustomName("闇の眷属(深)");
        ent.setCustomNameVisible(true);
        ent.setRemoveWhenFarAway(false);
        ent.setHealth(ent.getHealth() / 10);

        PotionEffect p = new PotionEffect(PotionEffectType.SPEED, 20*60*60*24*7*5, 5);
        ent.addPotionEffect(p);
        p = new PotionEffect(PotionEffectType.GLOWING, 20*60*60*24*7*5, 5);
        ent.addPotionEffect(p);

        return ent;
    }

    public Phantom spawnPhantom(LivingEntity target, Location loc, int killTimer, int level) {
        Phantom ent = (Phantom) loc.getWorld().spawnEntity(loc, EntityType.PHANTOM);
        Stray sub = (Stray)loc.getWorld().spawnEntity(loc, EntityType.STRAY);

        // ステータス
        ent.addPassenger(sub);
        ent.setCanPickupItems(false);
        ent.setCustomName("闇の眷属(飛)");
        ent.setCustomNameVisible(true);
        ent.setRemoveWhenFarAway(false);
        ent.setArrowCooldown(1);
        ent.setSize(5);
        ent.setTarget(target);

        ItemStack bow = new ItemStack(Material.BOW);
        if (level > 0) {
            bow.addUnsafeEnchantment(Enchantment.ARROW_DAMAGE, level);
            bow.addUnsafeEnchantment(Enchantment.ARROW_KNOCKBACK, level);
            bow.addUnsafeEnchantment(Enchantment.ARROW_FIRE, level);
        } else {
            bow.addUnsafeEnchantment(Enchantment.ARROW_KNOCKBACK, 1);
            bow.addUnsafeEnchantment(Enchantment.ARROW_FIRE, 1);
        }
        sub.getEquipment().setItemInMainHand(addShotEnchant(bow));
        sub.getEquipment().setItemInMainHandDropChance(0.001f);
        sub.getEquipment().setBoots(addDefEnchant(new ItemStack(Material.DIAMOND_BOOTS)));
        sub.getEquipment().setBootsDropChance(0.001f);
        sub.getEquipment().setChestplate(addDefEnchant(new ItemStack(Material.DIAMOND_CHESTPLATE)));
        sub.getEquipment().setChestplateDropChance(0.001f);
        sub.getEquipment().setHelmet(addDefEnchant(new ItemStack(Material.DIAMOND_HELMET)));
        sub.getEquipment().setHelmetDropChance(0.001f);
        sub.getEquipment().setLeggings(addDefEnchant(new ItemStack(Material.DIAMOND_LEGGINGS)));
        sub.getEquipment().setLeggingsDropChance(0.001f);
        // ステータス
        sub.setCanPickupItems(false);
        sub.setCustomName("闇の眷属(飛弓)");
        sub.setCustomNameVisible(true);
        sub.setRemoveWhenFarAway(false);

        PotionEffect p = new PotionEffect(PotionEffectType.SPEED, 20*60*60*24*7*5, 5);
        ent.addPotionEffect(p);
        sub.addPotionEffect(p);
        p = new PotionEffect(PotionEffectType.GLOWING, 20*60*60*24*7*5, 5);
        ent.addPotionEffect(p);
        sub.addPotionEffect(p);

        // killタイマー指定があったら指定tick後にremoveする
        if (killTimer != 0) {
            MobKillTimer timer = new MobKillTimer(plg, ent);
            timer.runTaskLater(plg, killTimer);
            timer = new MobKillTimer(plg, sub);
            timer.runTaskLater(plg, killTimer);
        }

        return ent;
    }

    /**
     * プレイヤーエンティティ作用イベント
     * @param event
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void PlayerInteractEntity(PlayerInteractEntityEvent event) {

        //----------------------------------------------------------------------
        // たまごのあるMOBの場合
        //----------------------------------------------------------------------
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();

        //LivienEntity以外はキャンセル
        if (!(entity instanceof LivingEntity)) {
            return;
        }
        LivingEntity le = (LivingEntity) entity;
        if (!(EcoEggUtil.existMonsterEgg(le))) return;

        //----------------------------------------------------------------------
        // プレイヤーの持っている物が魔道書か
        //----------------------------------------------------------------------
        boolean bookcheck = true;
        boolean hand = false;
        EcoEgg egg = (EcoEgg) plg.getPluginFrame("EcoEgg");
        if (egg.getGetter() != null) {
            if (egg.getGetter().equals(player)) {
                // MOB取得readyと判定
                bookcheck = false;
                egg.setGetter(null);
                log.info("EcoEgg get ready");
            }
        }
        if (bookcheck) {
            if(!egg.isBook(player.getInventory().getItemInMainHand())){
                return;
            }
            log.info("Detect EcoEgg book (ranking:)" + ranking.isRanking() + ")");
            hand = true;
            bookcheck = false;
        }
        // えこたまご使用準備が出来ている状態でのインタラクトであればキャンセルする
        if (!bookcheck) {
            if (ranking.isRanking()) {
                if (hand) {
                    log.info("exec drop EcoEgg");
                    Location loc = player.getLocation();
                    Random rand = new Random();
                    int x = rand.nextInt(6) -3;
                    int z = rand.nextInt(6) -3;
                    loc.add(x, loc.getY() + 2, z);
                    ItemStack item = player.getInventory().getItemInMainHand();
                    ItemStack drop = item.clone();
                    drop.setAmount(1);
                    player.getWorld().dropItem(loc, drop);
                    if (item.getAmount() > 1) {
                        item.setAmount(item.getAmount() - 1);
                        player.getInventory().setItemInMainHand(item);
                    } else {
                        player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
                    }
                    sendPluginMessage(plg, player, "手に持っていた " + item.getItemMeta().getDisplayName() + "が不思議な力でどこかへ飛んでいった");
                } else {
                    sendPluginMessage(plg, player, "不思議な力でえこたまごの力が霧散していった");
                }
                event.setCancelled(true);
            }
        }
    }

}
