# E2E テスト（本物の Paper + MineFlayer）

1. 両プラグインをビルドして Paper 1.19.4 の `plugins/` に置く
   （`depth-crawler/`・`hacknslash-weaponly/` で `mvn -DskipTests package`）
2. `server.properties` に `online-mode=false`, `enable-rcon=true`, `rcon.port=25575`, `rcon.password=test`
3. サーバー起動後、このディレクトリで `npm install` してから実行:
   - `node exit-flow.js` … 入室→エメラルド(次フロア)→ゴールド(生還)
   - `node npc-gui.js` … 拠点構築・NPC設置・各NPCのGUIを開く
   - `node shop-weaponly.js` … ショップ購入と /weaponly 各コマンド

`exit-flow.js` はサーバー直下の `../server/dungeon_*` からワールド名を読むため、サーバーは `e2e/` の隣に `server/` として置くこと。
実行前にサーバーを再起動して `dungeon_*` と `plugins/DepthCrawler/playerdata` を消しておくと安定する。

## 追加スクリプト
- `rest-floor.js` … F5まで進み、休息フロアの石ボタン→アップグレードGUI
- `insurance-death.js` … mercy・保険・死亡・再入室
- `mob-count.js` … F0の湧き数（30+F×3）
- `party.js` … 2ボットでパーティ・切断再接続・同時生還
- `combat-weaponly.js` … 素の剣とWeaponly強化剣のダメージ比較
- `restart-recovery-1.js` / `-2.js` … ダンジョン内ログアウト→`KEEP=1 ./restart.sh`→再ログイン
- `restart.sh` … サーバー再起動（`KEEP=1`でプレイヤーデータ保持）。`../server/` に Paper がある前提
- `structure-check.js` … 入室後、床/空間/天井の高さ・ドア口・角のグロウストーンを確認（仕様3.3）
- `enter-latency.js` … 入室中のRCON応答遅延を測り、生成でサーバーが止まらないことを確認

※ 入室・フロア移動はチャンクを非同期生成するため数秒かかる。スクリプトは固定sleepではなく `enter()` / `waitZ()` で到着を待つこと。
- `awaken-flow.js` … 敵を倒して素材を入手 → 覚醒×3（素材消費・コスト・品質）、リフォージ後も覚醒保持、ダメージ反映、残高不足で素材返却
- `awaken-named-equipment.js` … DCの名前付き武器/防具で表記（`名前 Ⅱ +2 改[隼]`）と改造解除
- `awaken-cap.js` … +10到達と、11回目の拒否（素材・ゴールド非消費）
- `insurance-blackmarket.js` … 契約（二重契約不可）→ 生還しても返還エントリは作られない（複製防止）→ 死亡で退避 → ブラックマーケット購入
- `insurance-claim-after-expiry.js` + `expire-insurance.sh` … 期限を過去にして再起動し、`/dungeon claim` で返還・二重回収不可
- `boss-floor.js` … F10まで進み、ボス（200HP）の出現・撃破・ドロップ（ティアは階層に応じる）。※ボスは毎回ランダム（エンダードラゴンは巨大すぎるため候補から除外）
- `blackmarket-persist.js` … `insurance-blackmarket.js` の後に `KEEP=1 ./restart.sh` してから実行し、ブラックマーケットの在庫が再起動後も残ることを確認
