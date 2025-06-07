# EcoDragon

EcoDragonは、Minecraftのエンダードラゴン戦を拡張・改良するSpigotプラグインです。ランキングシステム、PvP機能、ポイント制度などを追加し、より競争的で楽しいエンダードラゴン戦を提供します。

## 主な機能

### 🏆 ランキングシステム
- エンダードラゴン戦での活動に基づいてプレイヤーをランク付け
- クリスタル破壊・設置、釣り、モブ討伐などでポイントを獲得
- リアルタイムでランキングを表示
- 上位プレイヤーには賞状を授与

### ⚔️ 拡張されたドラゴン戦
- 複数ラウンドのドラゴン戦をサポート（デフォルト5ラウンド）
- プレイヤー間でのPvP機能
- カスタマイズされたモブスポーン
- ファントムの自動スポーン機能

### 🎣 特別な釣りシステム
- 釣りでポイントを獲得
- レアアイテム（エンチャント本、名札など）で高ポイント
- 通常の魚でもボーナスポイント

### 🌍 ワールド管理
- エンドワールドの境界管理
- ワールドプレフィックスによる複数ワールド対応
- 戦闘前のワールドボーダー調整

## 必要な環境

- **Minecraft**: 1.20.4 以上
- **サーバー**: Spigot/Paper
- **Java**: 8 以上
- **依存プラグイン**: 
  - EcoFramework (v0.29)
  - EcoEgg (v1.11)

## インストール

1. 必要な依存プラグインをダウンロードしてインストール
2. `EcoDragon.jar` をサーバーの `plugins` フォルダに配置
3. サーバーを再起動
4. 生成された設定ファイルを必要に応じて編集

## コマンド

すべてのコマンドは `/ecd` から始まります。

| コマンド | 説明 | 権限 |
|----------|------|------|
| `/ecd start` | エンダードラゴン戦を開始 | `ecodragon.start` |
| `/ecd end` | エンダードラゴン戦を強制終了 | `ecodragon.end` |
| `/ecd rank [show/true/false]` | ランキング表示の切り替え | `ecodragon.rank` |
| `/ecd pvp [true/false]` | PvP設定の切り替え | `ecodragon.pvp` |
| `/ecd book` | 賞状を取得 | `ecodragon.book` |
| `/ecd clear` | モブをクリア | `ecodragon.clear` |
| `/ecd reload` | 設定ファイルをリロード | `ecodragon.reload` |

## 設定ファイル

### config.yml

```yaml
# エンドワールドのプレフィックス
worldprefix: end

# 最大ラウンド数
roundmax: 5

# モブ経験値倍率
mob-exp-multiplier: 10

# クリスタル破壊時のペナルティポイント
crystal-break-penalty: -200000

# クリスタル破壊時のボーナスポイント
crystal-break-bonus: 10000

# クリスタル設置時のボーナスポイント
crystal-place-bonus: 25000

# 釣りボーナス設定
fishing-trash: 1000           # ゴミ釣り
fishing-bonus: 2000           # 通常の魚
fishing-salmon-bonus: 6000    # サーモン
fishing-clownfish-bonus: 100000    # クマノミ
fishing-pufferfish-bonus: 12000   # フグ
fishing-enchantbook-bonus: 150000 # エンチャント本
fishing-nametag-bonus: 150000     # 名札
fishing-shell-bonus: 150000       # シェル
fishing-saddle-bonus: 150000      # サドル

# ワールドボーダー設定前の待機時間
before-world-border: 1000

# 停止するワールドのリスト
stopworld: []
```

### certificate.yml

賞状システムの設定を管理します。

```yaml
# 賞状のタイトル
title: '賞状[エンドラ討伐ランキング]'

# アイテム名
name: '賞状[エンドラ討伐ランキング]'

# 著者名
author: '節電鯖'

# 賞状の内容
pages:
  - 'あなたは節電鯖エンダードラゴン討伐ランキングに於いて優秀な成績を収めましたのでその栄誉を称えこれを表彰いたします'
```

## ポイントシステム

プレイヤーは以下の行動でポイントを獲得できます：

### ポジティブポイント
- **クリスタル破壊**: 10,000ポイント
- **クリスタル設置**: 25,000ポイント
- **釣り**:
  - 通常の魚: 2,000ポイント
  - サーモン: 6,000ポイント
  - フグ: 12,000ポイント
  - クマノミ: 100,000ポイント
  - エンチャント本/名札/シェル/サドル: 150,000ポイント
- **モブ討伐**: 経験値 × 10倍のポイント

### ネガティブポイント
- **クリスタル破壊（ペナルティ）**: -200,000ポイント

## 使用方法

1. **ドラゴン戦開始**: エンドワールドで `/ecd start` を実行
2. **ランキング確認**: `/ecd rank show` でランキングを表示
3. **PvP設定**: `/ecd pvp true` でPvPを有効化
4. **戦闘**: 通常通りエンダードラゴンと戦い、ポイントを獲得
5. **賞状取得**: 戦闘後、上位プレイヤーは `/ecd book` で賞状を取得

## 権限

- `ecodragon.*` - 全ての権限
- `ecodragon.start` - ドラゴン戦開始
- `ecodragon.end` - ドラゴン戦終了
- `ecodragon.rank` - ランキング表示
- `ecodragon.pvp` - PvP設定変更
- `ecodragon.book` - 賞状取得
- `ecodragon.clear` - モブクリア
- `ecodragon.reload` - 設定リロード

## 開発者情報

- **作者**: ecolight
- **バージョン**: 0.15
- **ライセンス**: MIT License
- **依存**: EcoFramework, EcoEgg

## サポート

問題や質問がある場合は、GitHubのIssuesページで報告してください。

## 変更履歴

現在リファクタリング中です。将来的には、すべての動作設定を外部設定ファイルに移行し、ソースコードから独立した設定管理を実現予定です。