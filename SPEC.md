# Depth Crawler プラグイン仕様書

> **対象**: Paper 1.19.4  
> **Java**: 17+  
> **同梱プラグイン**: HACKnSLASH-Weaponly v0.1.0  
> **開発状態**: アルファ（コア機能実装済み、バグ修正中）

---

## 1. 概要

毎回ランダム生成されるダンジョンを探索し、敵を倒して装備を収集・強化するハクスラ系プラグイン。パーティプレイ対応。死亡時は装備全ロスト（保険あり）。

### 1.1 ゲームサイクル

```
拠点 → NPC「案内人」→ 確認画面 → ダンジョン入室
  → フロア探索（迷路 + 戦闘 + ルート）
  → 出口部屋（エメラルド=次フロア / ゴールド=帰還）
  → 5Fごと休息F（回復 + アップグレード選択）
  → 10FごとBOSS戦
  → 死亡 or 帰還 → 拠点
```

### 1.2 前提環境

| 項目 | 要件 |
|------|------|
| サーバー | Paper 1.19.4 |
| Java | 17以上 |
| 導入ファイル | `DepthCrawler-1.1.0.jar` + `HACKnSLASH-Weaponly-0.1.0.jar` |
| 管理者権限 | `depthcrawler.admin` |

---

## 2. コマンド一覧

### 2.1 一般プレイヤー

| コマンド | エイリアス | 説明 |
|---------|-----------|------|
| `/dungeon enter` | `/dc enter`, `/dc go` | ダンジョン入室 |
| `/dungeon info` | `/dc status` | 現在のセッション情報 + パーティ表示 |
| `/dungeon rules` | `/dc help` | 遊び方ガイド |
| `/dungeon mercy` | | 装備なし時に仮初装備一式支給（1回/ラン） |
| `/dungeon insure` | | 手持ちアイテムに保険（冒険者ギルド30%） |
| `/dungeon claim` | | 返還可能な保険アイテム回収 |
| `/dungeon insurance` | | 保険ステータス表示 |
| `/dungeon party invite <player>` | | パーティ招待 |
| `/dungeon party accept` | | 招待承認 |
| `/dungeon party leave` | | パーティ脱退（ダンジョン外のみ） |
| `/dungeon party info` | | メンバー一覧 |
| `/shop` | `/market` | ショップ購入メニュー |
| `/shop balance` | | 所持金表示 |
| `/safebox` | `/sb`, `/box` | セーフティボックス（5ページ） |
| `/safebox deposit` | | 手持ちアイテムを預入 |
| `/showitem` | `/si`, `/show` | 手持ち装備のステータスをサーバー全体にブロードキャスト |

### 2.2 管理者（`depthcrawler.admin` 必須）

| コマンド | 説明 |
|---------|------|
| `/dungeon setup spawn` | 25×25の拠点エリア生成 |
| `/dungeon setup dungeon` | ダンジョン入口エリア生成 |
| `/dungeon setup shop` | ショップエリア生成 |
| `/dungeon setup arena [w] [d]` | 闘技場生成（4〜200ブロック） |
| `/dungeon setup wall` | 周囲を壁で囲む |
| `/dungeon npc shop` | ショップNPC設置 |
| `/dungeon npc safebox` | セーフボックスNPC設置 |
| `/dungeon npc dungeon` | ダンジョン案内人NPC設置 |
| `/dungeon npc blacksmith` | 鍛冶屋NPC設置 |
| `/dungeon npc relief` | 救助班NPC設置（保険+情け） |
| `/dungeon npc blackmarket` | ブラックマーケットNPC設置 |
| `/dungeon admin givemoney <player> <amount>` | 残高付与 |
| `/dungeon room save <name> <normal\|boss\|rest>` | 選択範囲を部屋テンプレート保存 |
| `/dungeon room list` | 登録部屋一覧 |
| `/dungeon room info <name>` | 部屋詳細 |
| `/dungeon room delete <name>` | 部屋削除 |

### 2.3 ビルドコマンド（`/db`）

| コマンド | 説明 |
|---------|------|
| `/db guide` | 建築ガイド |
| `/db pos1` / `/db pos2` | 範囲選択 |
| `/db material <素材>` | デフォルト素材変更 |
| `/db room <w> <d>` | 部屋（壁/床/天井付き） |
| `/db corridor <x> <z>` | 通路 |
| `/db doorway` | 向いてる壁に2×3通路 |
| `/db platform <w> <d>` | 足場 |
| `/db wall` | 壁（pos1→pos2) |
| `/db torch <間隔>` | 松明 |
| `/db stairs <方角> <段数>` | 階段 |
| `/db fountain` | 噴水 |
| `/db chest` | チェスト設置 |
| `/db pillar <高さ>` | 柱 |
| `/db maze <w> <h>` | 迷路生成 |
| `/db undo` | 1手戻す |
| `/db reset` | リセット |

### 2.4 Weaponly コマンド

| コマンド | 説明 |
|---------|------|
| `/weaponly reforge [precise]` | 武器性能再抽選 |
| `/weaponly awaken` | 素材消費で覚醒（+1〜+10） |
| `/weaponly modify <type>` | 改造（隼/重/烈/円/堅/鋭/魔/真） |
| `/weaponly unmodify` | 改造解除 |
| `/weaponly gem apply <effect>` | ジェム付与 |
| `/weaponly gem remove <id>` | ジェム除去 |
| `/weaponly synthesize` | 素材合成 |
| `/weaponly info` | 装備情報表示 |

---

## 3. ダンジョン生成

### 3.1 ワールド

| 項目 | 値 |
|------|-----|
| 種別 | フラットワールド（石1層 + 平原バイオーム） |
| ワールド名 | `dungeon_<UUID8桁>_<timestamp>` |
| ライフサイクル | 入室時に生成、セッション終了時に削除 |
| パーティ | 1パーティ = 1インスタンス = 1ワールド |
| MOB_GRIEFING | false（クリーパー爆発無効） |
| DO_FIRE_TICK | false（延焼無効） |

### 3.2 フロア生成アルゴリズム

```
┌─────────────────────────────────────┐
│ ■■■■■■■■■■■■■■■■■■■■■■■■■■ │ ← 外壁（全周）
│ ■ ┌─┐══┌─┐══┌─┐ ... ┌─┐ ■ │
│ ■ │ │  │ │  │ │     │EM│ ■ │  EM=エメラルド
│ ■ │ │  │ │  │ │     │G │ ■ │  G =ゴールド
│ ■ └─┘══└─┘══└─┘ ... └─┘ ■ │  ══=3ブロック通路
│ ■■■■■■■■■■■■■■■■■■■■■■■■■■ │
└─────────────────────────────────────┘
```

| パラメータ | 値 |
|-----------|-----|
| グリッド | 10×10 = 100部屋 |
| 1部屋内寸 | 10×10ブロック |
| 壁厚 | 1ブロック |
| 迷路全体サイズ | 111×111ブロック（10×(10+1)+1） |
| 天井高 | Y=68（4ブロック高） |
| 通路幅 | 接続部屋間の壁中央3ブロック |
| 迷路アルゴリズム | DFS（深さ優先探索）→ 全Room接続保証 |

### 3.3 ブロック構成

| Y座標 | 内容 | 素材 |
|-------|------|------|
| 63 | 基礎床 | 石レンガ |
| 64 | 床面 | 部屋ごとにランダム（磨かれた安山岩/石レンガ/テラコッタ） |
| 65-67 | 空間 | 壁以外はAIR |
| 68 | 天井 | 石レンガ + 部屋四隅にグロウストーン |

### 3.4 装飾

- 部屋四隅天井にグロウストーン照明
- 30%の部屋の中央に柱（石レンガの壁）+ 松明
- 休息F: 出口部屋にビーコン
- BOSS F: 出口部屋にレッドストーンブロック

### 3.5 出口部屋

右下の部屋（grid[9][9]）に以下を設置:
- **エメラルドブロック**（床）: 3秒立つ → 次フロアへ進行
- **ゴールドブロック**（床）: 3秒立つ → 拠点帰還 + 報酬
- **石ボタン**（壁）: 休息FでのみアップグレードGUIを開く
- 立っている間: パーティクル演出 + 1.5秒後に警告

### 3.6 フロア間隔

```
floor0: Z=0〜110
floor1: Z=121〜231
floor2: Z=242〜352
...
間隔 = 111 + 10 = 121ブロック
```

---

## 4. フロア修飾子

全フロアに必ず1つ付与。ランダム抽選（均等確率）。

| 修飾子 | ゲーム内表示 | 敵ATK | 敵DEF | 自SPD | ドロップ | 特殊効果 |
|--------|------------|--------|--------|--------|---------|---------|
| DARKNESS | §8暗闇 | ×1.0 | ×1.0 | ×1.0 | ×1.0 | 盲目ポーション付与 |
| BERSERK | §c狂乱 | **×1.3** | ×1.0 | ×1.0 | **×1.2** | — |
| FORTIFIED | §7要塞 | ×1.0 | **×1.25** | ×1.0 | ×1.0 | 経験値+50% |
| HASTE | §b加速 | ×1.0 | ×1.0 | **×1.2** | ×1.0 | 速度ポーション |
| PLAGUE | §2疫病 | ×1.0 | ×1.0 | ×1.0 | **×1.3** | 毎2秒1ダメージ |
| TREASURE | §6宝物庫 | ×1.0 | ×1.0 | ×1.0 | ×1.0 | 宝箱出現率2倍 |
| SWARM | §e大群 | ×1.0 | ×1.0 | ×1.0 | ×1.0 | mob数1.5倍 |
| WEAKNESS | §8脆弱 | ×1.0 | ×1.0 | ×1.0 | **×1.25** | プレイヤーATK-15% |
| REGENERATION | §d再生 | ×1.0 | ×1.0 | ×1.0 | ×1.0 | 敵HP徐々に回復 |

---

## 5. 敵（Mob）

### 5.1 クラス別一覧

| クラス | 数 | 出現 | 代表EntityType |
|--------|-----|------|---------------|
| NORMAL | 24 | F1〜 | ZOMBIE, SKELETON, SPIDER, SLIME, PIGLIN, HOGLIN, WOLF, POLAR_BEAR, IRON_GOLEM, WARDEN... |
| RANGED | 10 | F1〜 | SKELETON, STRAY, WITHER_SKELETON, PILLAGER, BLAZE, SHULKER, GUARDIAN... |
| FAST | 8 | F1〜 | SPIDER, WOLF, CAVE_SPIDER, VEX, PHANTOM, ENDERMITE, SILVERFISH... |
| EXPLOSIVE | 6 | F2〜 | CREEPER, GHAST, BLAZE... |
| HEAVY | 8 | F2〜 | WARDEN, RAVAGER, IRON_GOLEM, ZOMBIE, ENDERMAN, HOGLIN... |
| CASTER | 10 | F4〜 | WITCH, EVOKER, ILLUSIONER, ENDERMAN, VEX... |
| FLYING | 8 | F4〜 | PHANTOM, VEX, BLAZE, GHAST, BEE... |
| ELITE | 10 | 確率混入 | 各クラス強化版 |
| BOSS | 10 | 10F毎 | IRON_GOLEM, ZOMBIE(巨大), ENDERMAN, RAVAGER, WITHER, WARDEN, EVOKER, PIGLIN_BRUTE, ELDER_GUARDIAN, ENDER_DRAGON |

### 5.2 出現テーブル

```
Phase 0 (F1-10):   初級8種
Phase 1 (F11-20):  +中級9種
Phase 2 (F21-30):  +上級10種
Phase 3 (F31-40):  +超級11種
Phase 4 (F41-50):  +12種
Phase 5 (F51+):    +10種
エリート混入率: 10% + phase×3%
```

### 5.3 湧き数

| 計算式 | F1 | F10 | F30 | F50 |
|--------|-----|------|------|------|
| `30 + floorLevel × 3` | 33 | 60 | 120 | 180 |
| SWARM修飾子時 | ×1.5 | ×1.5 | ×1.5 | ×1.5 |

### 5.4 動的湧き

2秒ごとに1/15の確率でプレイヤーを中心とする21×21エリア内のランダムな空気ブロックに1体追加スポーン。

---

## 6. 装備システム

### 6.1 名前生成パイプライン

```
rollWeapon() → 70%自動生成 / 30%ネームド固定
  │
  ├─ 品質修飾子（20種）: 伝説の, 神話の, 神威の, 破壊の, 狂気の...
  ├─ 属性接頭辞（32種）: 烈火の, 氷獄の, 雷轟の, 暴風の, 深淵の...
  ├─ 獣神話接頭辞（25種）: 獅子の, 鳳凰の, 古龍の, 魔王の, 死神の...
  ├─ 材質接頭辞（18種）: 鋼鉄の, 白金の, 水晶の, 金剛の...
  ├─ 状態接頭辞（13種）: 血濡れの, 輝く, 穢れた, 宿命の...
  ├─ 武器基本名（48種）: 剣, 刀, クレイモア, ハルバード, フレイル...
  ├─ 防具基本名（71種）: 兜, フード, プレート, ローブ, グリーブ...
  ├─ 接尾辞（10種）: 改, 真打, 零式, ・極, ・天命...
  └─ "of the X"（32種）: 不死鳥, 深淵, 終焉, 混沌, 龍神...
```

### 6.2 名前パターン（武器）

| 確率 | パターン | 例 |
|------|---------|-----|
| 15% | 品質 + 接頭辞 + 基本名 + of the X | 伝説の烈火のクレイモア of the 不死鳥 |
| 13% | 品質 + 接頭辞 + 基本名 | 伝説の烈火のクレイモア |
| 22% | 品質 + 接頭辞 + 基本名 + 接尾辞 | 伝説の烈火のクレイモア ・天命 |
| 20% | 品質 + 基本名 + of the X | 伝説のクレイモア of the 不死鳥 |
| 15% | 品質 + 基本名 + 接尾辞 | 伝説のクレイモア ・極 |
| 15% | 品質 + 基本名 | 伝説のクレイモア |

### 6.3 品質修飾子のステータス補正

| 品質 | ATK | DEF | SPD | CRIT | HP | 吸血 |
|------|-----|-----|-----|------|-----|------|
| 伝説の | +20% | — | +10% | +15% | +5% | — |
| 破壊の | +25% | — | -5% | +5% | — | — |
| 狂気の | +10% | -10% | — | +20% | — | — |
| 不滅の | -5% | +10% | — | — | +20% | — |
| 疾風の | +5% | — | +20% | — | — | — |
| 無慈悲な | +20% | — | — | +5% | — | +3% |
| ...他14種 | | | | | | |

### 6.4 ティアと素材

| Tier | 武器素材 | 武器ATK | 防具素材 | 防具DEF |
|------|---------|---------|---------|---------|
| 1 | WOOD | 0-3 | LEATHER | 0-3 |
| 2 | STONE | 2-6 | CHAINMAIL | 2-7 |
| 3 | IRON | 4-10 | IRON | 4-11 |
| 4 | GOLD | 6-14 | GOLD | 6-15 |
| 5 | DIAMOND | 8-18 | DIAMOND | 9-20 |
| 6 | NETHERITE | 12-25 | NETHERITE | 13-28 |

### 6.5 ステータス付与

装備生成時に `NameStats`（ATK倍率/DEF倍率/SPD倍率/CRIT加算/HP倍率/吸血加算）を計算し、ItemStackのPDC（PersistentDataContainer）に保存。

**戦闘時:**
- 武器: PDCからNameStatsを読み取り `atkMult` を攻撃力に乗算
- 防具: 装備中の全パーツの `defMult` を合算して被ダメに乗算

### 6.6 ネームド武器（BOSS/ELITE/SPECIAL固定）

30%の確率で出現。例:
- 絶望を刻む者（BOSS）: ATK+30%, CRIT+15%, LS+5%
- 竜殺しの大剣（BOSS）: ATK+35%, DEF+5%, CRIT+10%, HP+10%
- 虚空を裂く刃（ELITE）: ATK+18%, SPD+10%, CRIT+12%

---

## 7. アイテム

### 7.1 ドロップテーブル

| 種別 | 確率 | 説明 |
|------|------|------|
| 武器 | 35%×修飾子倍率 | 48種基本名 × 数百万通りの修飾子組み合わせ |
| 防具 | 20%×修飾子倍率 | 71種基本名 × 同修飾子 |
| 素材 | 30%×修飾子倍率 | 40種（深層の核, 竜の鱗片, 螺環の破片...） |
| 換金品 | 25%×修飾子倍率 | 30種（古代の貨, 深層の真珠, 魔王の指輪...） |
| BOSSドロップ | 100% | ネームド武器/防具 + 100〜500G |

### 7.2 レアリティ

| 等級 | 色 | 売却額目安 |
|------|-----|----------|
| COMMON | §7 | 5-20G |
| UNCOMMON | §a | 25-60G |
| RARE | §b | 50-100G |
| EPIC | §d | 120-200G |
| LEGENDARY | §6 | 200-500G |

### 7.3 消費アイテム効果

| アイテム | 右クリック時 |
|---------|------------|
| 撤退コンパス | ダンジョン内で撤退ポイントを指す |
| リペアキット | `/weaponly unmodify` 実行 + 消費 |
| バックパック | その場でセーフティボックスを開く |
| 食料パック | バニラ食料効果 |
| 各種ポーション | バニラ効果 |

---

## 8. NPC

### 8.1 NPC一覧

| NPC | 職業 | 右クリック |
|-----|------|----------|
| §6ショップ | 司祭 | 購入/売却 選択メニュー |
| §8セーフティボックス | 地図職人 | セーフティボックス（5P） |
| §cダンジョン案内人 | 武器鍛冶 | 入室確認（潜る/パーティ/やめる） |
| §d鍛冶屋 | 道具鍛冶 | 素材換金 / 装備強化 |
| §b救助班 | 無職 | 情け / 保険(1個) / 一括保険 / 保険回収 |
| §5ブラックマーケット | 無職 | 死亡時流出品を購入 |

### 8.2 NPC実装

- バニラの村人エンティティ（AI=false, Invulnerable=true）
- `PersistentDataContainer` キー `depthcrawler_npc_type` で種別識別
- 右クリック → 対応GUIを開く
- `/dungeon npc <type>` でスポーン（管理者専用）

### 8.3 ショップGUI詳細

**購入/売却選択**: クリックで分岐
- 購入: 54スロットのショップ一覧。価格表示、クリック購入
- 売却: 36スロットの売却GUI。アイテム配置→右下ボタンで一括売却

### 8.4 鍛冶屋GUI

| ボタン | 機能 |
|--------|------|
| 装備を強化 | 手持ち武器を覚醒（素材1個+200G消費） |
| 素材を換金 | インベントリ内の全素材アイテムをゴールド化 |
| 素材情報 | （説明表示） |

### 8.5 救助班GUI

| ボタン | 機能 |
|--------|------|
| 情けを乞う | 装備全没収 → 仮初装備一式 + 食料支給 |
| 保険を掛ける(1個) | 手持ちアイテムに保険 → 業者選択 → 契約 |
| 全装備一括保険 | 全武器/防具を検出 → 業者選択 → 一括契約 |
| 保険を回収 | 返還待ちの保険アイテムを回収 |

---

## 9. 保険システム

### 9.1 提供者

| # | 名前 | 料金率 | 返還率 | 返還時間 | アイコン |
|---|------|--------|--------|---------|---------|
| 1 | §6王国保険組合 | 50% | 100% | 6時間 | GOLD_BLOCK |
| 2 | §e冒険者ギルド | 30% | 100% | 24時間 | IRON_BLOCK |
| 3 | §b早馬急便 | 55% | 85% | 3時間 | DIAMOND |
| 4 | §7裏路地の質屋 | 15% | 70% | 48時間 | DARK_OAK_LOG |
| 5 | §5闇市の仲介人 | 8% | 50% | 72時間 | OBSIDIAN |

### 9.2 フロー

```
1. 救助班NPC → 保険ボタン → 提供者選択
2. 手持ちアイテム（or 全装備一括）を選択
3. 保険料（アイテム推定価値 × 料金率）を支払い
4. アイテムの説明文に「【ROYAL 保障済】」が追記
5. 死亡時: 保険アイテムは退避（インベントリから消えるがPDC保存）
6. 返還時間経過後: /dungeon claim で回収
7. 回収時: 返還率でロール → 失敗で消失
```

### 9.3 データ永続化

`playerdata/<UUID>.yml` 内の `insurance` セクションに保存:
```yaml
insurance:
  - entry-id: <uuid>
    expiry: <timestamp>
    claimed: false
    provider: ROYAL
    item: <serialized>
```

---

## 10. セーフティボックス

| 項目 | 値 |
|------|-----|
| ページ数 | 5 |
| 1ページあたりスロット | 45（54インベントリのうち下段9スロットはナビ用） |
| 総容量 | 225スロット |
| 操作 | 預入（ボタン）/ 取出（スロットクリック） / ページ移動 |
| 永続化 | `playerdata/<UUID>.yml` の `safebox-slots` |

---

## 11. パーティシステム

### 11.1 基本ルール

| 項目 | 内容 |
|------|------|
| 作成 | `/dungeon party invite <player>` |
| 承認 | `/dungeon party accept`（30秒以内） |
| 脱退 | `/dungeon party leave`（ダンジョン外のみ可） |
| リーダー | パーティ作成者が自動リーダー。脱退時はオンラインメンバーに継承 |
| インスタンス | 1パーティ = 1ダンジョンワールド |
| 入室 | リーダーのみ `/dungeon enter` 可。メンバー全員が同じワールドにTP |

### 11.2 切断処理

| 状況 | 処理 |
|------|------|
| PTメンバー切断 | セッション維持。他メンバーがオンラインなら即時復帰可能 |
| ソロ切断 | 5分タイマー開始。時間内復帰で元位置にTP |
| タイムアウト | 死亡扱い。保険未加入の装備は全ロスト |
| 復帰時 | PTメンバーの位置にTP。ソロなら切断位置にTP |

---

## 12. アップグレードシステム

### 12.1 19種一覧

| # | 名前 | 効果 | レア度 |
|---|------|------|--------|
| 1 | 攻撃力UP | ATK+25%（重複可、最大75%） | - |
| 2 | 防御力UP | 被ダメ-15%（重複可、下限10%） | - |
| 3 | 速度UP | 移動+20%（重複可、最大60%） | - |
| 4 | 会心UP | CRIT率+10%（重複可、最大30%） | - |
| 5 | ライフスティール | 攻撃時5%吸血（重複可） | - |
| 6 | 自動回復 | HP+2/sec（重複可） | - |
| 7 | 回避UP | 回避率+6%（重複可） | - |
| 8 | 反撃 | 被ダメ8%反射（重複可） | - |
| 9 | 二段攻撃 | 20%で追撃（重複可） | - |
| 10 | 範囲攻撃 | 周囲30%ダメ（重複可） | - |
| 11 | 宝箱強化 | 報酬+50% | - |
| 12 | 属性強化 | 属性ダメ+30% | - |
| 13 | 🔥連鎖電撃 | 近くの敵2体に連鎖ダメ30% | ★ |
| 14 | 💥爆裂の一撃 | 10%で爆発+範囲ダメ50% | ★ |
| 15 | 😤狂戦士 | キル毎ATK+5%（5重/10秒） | ★ |
| 16 | 💉アドレナリン | HP30%以下でATK+35%/SPD+20% | ★ |
| 17 | 💔ガラスの大砲 | ATK+40% / DEF-20% | ★ |
| 18 | 🩸吸血のオーラ | 5ブロック以内の敵からHP吸収 | ★ |
| 19 | 🍀幸運の発見 | ドロップ品質+25% | ★ |

### 12.2 獲得タイミング

- 5Fごとの休息フロア出口部屋で石ボタンを押す
- GUIが開き3つのランダム選択肢から1つ選択
- 各アップグレードは最大3レベルまで重複取得可能
- 全種MAXの場合「取得可能なアップグレードがありません」

---

## 13. スコアボード

ダンジョン滞在中、画面右に表示（2秒更新）:

```
§6§l⚔ DEPTH CRAWLER ⚔
§7━━━━━━━━━━━━
§e📊 12F  §a通常
§d⚡ 狂乱
§8
§c❤ HP: §f18/20
§6⚔ コンボ: §e5
§8
§6⛁ 所持金: §e1,250G
§8
§7次休息: §b15F  §7次BOSS: §c20F
§7━━━━━━━━━━━━
```

退出時にクリアされ通常スコアボードに戻る。

---

## 14. ダンジョン保護

| 制限 | 実装 |
|------|------|
| ブロック破壊 | BlockBreakEvent キャンセル |
| ブロック設置 | BlockPlaceEvent キャンセル |
| ゲームモード変更 | クリエイティブ/スペクテイター不可（adminは除外） |
| テレポート | ENDER_PEARL / CHORUS_FRUIT 無効 |
| アイテム投棄 | PlayerDropItemEvent キャンセル |
| ゲームモード | 入室時自動でADVENTUREに設定 |
| 爆発ブロック破壊 | MOB_GRIEFING=false |

---

## 15. 設定ファイル

### 15.1 config.yml（自動生成）

```yaml
dungeon:
  min-floors: 3
  max-floors: 8
  min-room-size: 15
  max-room-size: 30
  extraction-rush-duration: 60.0
  chest-streak-multiplier: 0.25
player:
  max-safe-box-slots: 54
shop:
  reforge-cost: 100.0
```

### 15.2 rooms.yml（管理者が部屋登録時に自動生成）

```yaml
rooms:
  arena1:
    category: BOSS
    width: 20
    depth: 20
    spawnX: 10
    spawnZ: 2
    exitX: 10
    exitZ: 18
    blocks:
      - "0,0,-1,STONE_BRICKS"
      - "0,0,4,STONE_BRICKS"
      ...
```

### 15.3 プレイヤーデータ

`plugins/DepthCrawler/playerdata/<UUID>.yml`:
```yaml
balance: 1250.0
streak: 3
highest-floor: 15
total-runs: 42
successful-extractions: 18
mercy-used: false
safebox-slots:
  - slot: 0
    item: <serialized>
insurance:
  - entry-id: <uuid>
    expiry: 1783152793000
    claimed: false
    provider: ROYAL
    item: <serialized>
```

---

## 16. 既知の問題（2024-07時点）

| # | 問題 | 深刻度 | 状態 |
|---|------|--------|------|
| 1 | エメラルド/ゴールドに3秒立っても反応しない | **高** | 調査中 |
| 2 | セーフボックスに直接ドラッグで入れたアイテムが消失 | **高** | 修正済み（全クリックキャンセル） |
| 3 | 防具名の素材表示と実体が不一致（「革製」なのに鉄ヘルメット） | 中 | ティアと素材を一致させる修正が必要 |
| 4 | 鍛冶屋GUIの素材情報が不十分 | 中 | GUI改善が必要 |
| 5 | Weaponly強化がGUIで完結しない | 中 | 鍛冶屋GUI拡張が必要 |
| 6 | `/dc extract` 使用不可（一般プレイヤー） | 低 | 意図的（ゴールドブロック使用推奨） |
| 7 | 修飾子の効果が戦闘に反映されていないケースがある | 中 | 一部修正済み、要検証 |

---

## 17. ファイル構成

```
depth-crawler/
├── src/main/java/com/dungeoncrawler/
│   ├── DepthCrawlerPlugin.java
│   ├── command/
│   │   ├── BuildCommand.java
│   │   ├── DungeonCommand.java
│   │   ├── SafeBoxCommand.java
│   │   ├── SetupCommand.java
│   │   ├── ShopCommand.java
│   │   └── ShowItemCommand.java
│   ├── config/
│   │   └── DungeonConfig.java
│   ├── data/
│   │   └── PlayerDataManager.java
│   ├── dungeon/
│   │   ├── DungeonBuilder.java
│   │   ├── DungeonManager.java
│   │   ├── DungeonSession.java
│   │   ├── FloorGenerator.java
│   │   ├── RoomManager.java
│   │   └── RoomTemplate.java
│   ├── game/
│   │   ├── ChestStreakManager.java
│   │   ├── DifficultyManager.java
│   │   ├── ExtractionManager.java
│   │   ├── FloorModifier.java
│   │   ├── GameManager.java
│   │   ├── GameState.java
│   │   ├── InsuranceManager.java
│   │   ├── LootManager.java
│   │   ├── MercyManager.java
│   │   ├── ScoreboardManager.java
│   │   ├── UpgradeManager.java
│   │   └── UpgradeType.java
│   ├── item/
│   │   ├── ItemNameGenerator.java
│   │   └── ItemRegistry.java
│   ├── listener/
│   │   ├── ChestListener.java
│   │   ├── DungeonListener.java
│   │   └── NpcListener.java
│   ├── maze/
│   │   └── MazeGenerator.java
│   ├── mob/
│   │   ├── CustomMobManager.java
│   │   └── CustomMobType.java
│   ├── npc/
│   │   ├── NpcManager.java
│   │   └── NpcType.java
│   ├── party/
│   │   ├── DisconnectHandler.java
│   │   ├── Party.java
│   │   └── PartyManager.java
│   ├── player/
│   │   ├── PlayerData.java
│   │   └── SafeBoxManager.java
│   └── shop/
│       ├── ShopItem.java
│       └── ShopManager.java
└── src/main/resources/
    ├── config.yml
    └── plugin.yml
```

---

## 18. セットアップ手順（運営向け）

```bash
# 1. サーバーに両jarを導入
cp build/*.jar /path/to/server/plugins/

# 2. 起動 → 自動的にconfig.ymlが生成される

# 3. 管理者権限付与
/lp user <name> permission set depthcrawler.admin true

# 4. 拠点構築
/dungeon setup spawn
/dungeon npc dungeon
/dungeon npc shop
/dungeon npc safebox
/dungeon npc blacksmith
/dungeon npc relief
/dungeon npc blackmarket

# 5. 必要に応じて部屋テンプレート登録
/db pos1
/db pos2
/dungeon room save <name> normal

# 6. プレイ開始
/dungeon enter
```
