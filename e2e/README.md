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
