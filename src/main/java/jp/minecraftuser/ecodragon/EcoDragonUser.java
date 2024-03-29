
package jp.minecraftuser.ecodragon;

import org.bukkit.entity.Player;

/**
 * エンダードラゴン戦ユーザーデータクラス
 * @author ecolight
 */
public class EcoDragonUser {
    private int point = 0;
    private int damage = 0;
    private int damageEtc = 0;
    private Player player = null;
    private boolean rank = false;
    private boolean pvp = false;
    private boolean pvpbonus = false;
    
    /**
     * コンストラクタ(プレイヤー指定)
     * @param p 
     */
    public EcoDragonUser(Player p) {
        player = p;
    }

    /**
     * プレイヤー取得
     * @return プレイヤーインスタンス
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * ポイント加算
     * @param num 加算ポイント基礎値
     * @return 加算後のポイントを返却
     */
    public int addPoint(int num) {
        if (pvpbonus) num *= 1.2;
        point += num;
        return point;
    }

    /**
     * エンダードラゴンへのダメージによるポイント加算
     * @param num 加算値のもとになるダメージ値
     * @return 通算のダメージ量を返却する
     */
    public int addDamage(int num) {
        if (pvpbonus) num *= 1.2;
        damage += num;
        point += damage * 100;
        return damage;
    }

    /**
     * 通算ダメージ量の取得
     * @return 
     */
    public int getDamage() {
        return damage;
    }

    /**
     * エンダードラゴン以外のMOBへのダメージ量加算
     * @param num 加算値のもとになるダメージ値
     * @return 通算のエンドラ以外のMOBへのダメージ量
     */
    public int addDamageEtc(int num) {
        if (pvpbonus) num *= 1.2;
        damageEtc += num;
        point += damageEtc;
        return damageEtc;
    }

    /**
     * 通算のポイント数を取得
     * @return 通算ポイント
     */
    public int getPoint() {
        return point;
    }

    /**
     * ランキング入賞したか否かのフラグを設定する
     * @param r true:入賞した false:してない
     */
    public void setRanking(boolean r) {
        rank = r;
        return;
    }

    /**
     * ランキング入賞したか否かのフラグを取得する
     * @return true:入賞した false:してない
     */
    public boolean getRanking() {
        return rank;
    }
    
    /**
     * PvP許可モードを設定
     * @param f true:PvP受け入れ false:PvPしてない
     */
    public void setPvP(boolean f) {
        pvp = f;
        pvpbonus = f;
        return;
    }

    /**
     * PvP受け入れモードか否か
     * @return true:PvP受け入れ false:PvPしてない
     */
    public boolean isPvP() {
        return pvp;
    }

}
