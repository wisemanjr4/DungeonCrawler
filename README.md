# Depth Crawler プラグイン再構築プロジェクト

仕様書 `SPEC.md`（DepthCrawler 1.1.0）と `hacknslash_weaponly_v0.1.md`（Weaponly 0.1.0）に基づき、
ゼロから再構築した2つの Paper プラグインです。

> **ライセンス**: All Rights Reserved (ARR)。本リポジトリの複製・改変・再配布・商用利用は、
> 作者の明示的な許可がない限り禁止されています。詳細は [LICENSE](./LICENSE) を参照してください。

## 構成

```
E:\Deepseek\DungeonCrawler\
├── LICENSE                           # All Rights Reserved ライセンス
├── SPEC.md                          # DepthCrawler プラグイン仕様書
├── depth_crawler_v1.1.md            # ゲーム企画書
├── hacknslash_weaponly_v0.1.md      # Weaponly 企画書
├── build.bat                        # 一括ビルドスクリプト
├── depth-crawler/                   # DepthCrawler 1.1.0
│   ├── pom.xml
│   └── src/main/
│       ├── resources/plugin.yml     # コマンド/権限定義
│       ├── resources/config.yml     # 設定（自動コピー）
│       └── java/com/dungeoncrawler/
│           ├── DepthCrawlerPlugin.java
│           ├── command/   (Dungeon, Build, Setup, Shop, SafeBox, ShowItem)
│           ├── config/    (DungeonConfig)
│           ├── data/      (PlayerDataManager)
│           ├── dungeon/   (DungeonManager, DungeonSession, FloorGenerator,
│           │               DungeonBuilder, RoomManager, RoomTemplate)
│           ├── game/      (GameManager, LootManager, InsuranceManager, MercyManager,
│           │               ExtractionManager, ChestStreakManager, DifficultyManager,
│           │               ScoreboardManager, UpgradeManager, UpgradeType,
│           │               FloorModifier, GameState)
│           ├── item/      (ItemNameGenerator, ItemRegistry, NameStats, ItemGenerationResult)
│           ├── listener/  (DungeonListener, ChestListener, NpcListener, CombatListener,
│           │               MobDropListener, ConsumableListener)
│           ├── maze/      (MazeGenerator)
│           ├── mob/       (CustomMobManager, CustomMobType)
│           ├── npc/       (NpcManager, NpcType, BlackMarketManager, ReliefGui, BlacksmithGui)
│           ├── party/     (Party, PartyManager, DisconnectHandler)
│           ├── player/    (PlayerData, SafeBoxManager, InsuranceEntry, ItemSerializer)
│           └── shop/      (ShopManager, ShopItem)
└── hacknslash-weaponly/             # HACKnSLASH Weaponly 0.1.0
    ├── pom.xml
    └── src/main/
        ├── resources/plugin.yml
        ├── resources/config.yml
        └── java/com/hacknslash/weaponly/
            ├── WeaponlyPlugin.java
            ├── command/WeaponlyCommand.java
            ├── data/WeaponStats.java
            └── listener/WeaponlyListener.java
```

## 要件

- Paper 1.19.4 サーバー
- Java 17+（ビルドは JDK 21 で検証済み）
- Maven 3.9+

## ビルド

```
build.bat
```

または各ディレクトリで:

```
mvn clean package
```

成果物:
- `depth-crawler/target/DepthCrawler-1.1.0.jar`
- `hacknslash-weaponly/target/HACKnSLASH-Weaponly-0.1.0.jar`

## 導入

1. 両JARをサーバーの `plugins/` へ配置
2. 起動 → `config.yml` が自動生成
3. 権限付与: `/lp user <name> permission set depthcrawler.admin true`
4. 拠点構築（仕様書 セクション18 参照）:
   ```
   /dungeon setup spawn
   /dungeon npc dungeon
   /dungeon npc shop
   /dungeon npc safebox
   /dungeon npc blacksmith
   /dungeon npc relief
   /dungeon npc blackmarket
   ```
5. `/dungeon enter` で開始

## 実装メモ

- ワールドは入室時に `dungeon_<uuid8>_<timestamp>` のフラットワールドを生成、セッション終了時に削除
- 迷路は DFS で 10×10 グリッド（100部屋）全接続保証
- 装備は PDC（PersistentDataContainer）に NameStats（ATK/DEF/SPD/CRIT/HP/LS 倍率）を保存
- 保険・セーフボックスは `playerdata/<UUID>.yml` に永続化（Base64 シリアライズ）
- Weaponly は DepthCrawler へのコンパイル時依存なし（リフレクションで残高連携）
- 死亡時: 保険アイテムのみ退避しインベントリ全消去、リスポーン時に拠点へTP（PlayerRespawnEvent）
- 帰還・死亡時に拠点座標は入室前の位置を記録して戻す
- セッション終了時に run-task（スコアボード更新/抽出判定）を必ずキャンセル
- 宝箱は開けた時点でルート生成、一度開いたチェストはcustomNameで再生成防止
- 動的湧きは2秒クールダウン付き（移動イベント高頻度対策）
- モブ/エリート/BOSSはPDCで種別マーク、死亡時にドロップ（BOSS=ネームド+100〜500G）
- 撤退ラッシュ: ゴールドで帰還する際に魔物が押し寄せる
- 品質アフィックス・ネームド装備がNameStatsを通じて戦闘に反映（CombatListener）
- レアアップグレード（★7種）は戦闘・ドロップに反映済み
