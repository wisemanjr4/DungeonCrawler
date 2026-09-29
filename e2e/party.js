const { connect, rcon, sleep, tp, dungeonWorld, waitFor, godmode } = require('./lib');
const pos = b => { const p = b.entity.position; return `(${p.x.toFixed(1)}, ${p.y.toFixed(1)}, ${p.z.toFixed(1)})`; };
(async () => {
  const a = await connect('Tester');
  let b = await connect('Mate');
  a.chat('/dungeon party invite Mate'); await sleep(1000);
  b.chat('/dungeon party accept'); await sleep(1000);
  a.chat('/dungeon party info'); await sleep(800);
  b.chat('/dungeon enter'); await sleep(1500);                 // メンバーは入室不可のはず
  console.log('--- leader enters');
  a.chat('/dungeon enter'); await waitFor(() => a.entity.position.y > 0 && b.entity.position.y > 0); await godmode('Tester'); await godmode('Mate'); await sleep(1500);
  console.log('Tester', pos(a), 'Mate', pos(b));
  b.chat('/dungeon party leave'); await sleep(800);            // ダンジョン中は脱退不可のはず
  // 切断→再接続（他メンバーはオンライン）
  console.log('--- Mate disconnects and rejoins');
  await tp('Tester', 50.5, 65, 50.5); await sleep(500);
  b.quit(); await sleep(2500);
  b = await connect('Mate'); await sleep(3500);
  console.log('Mate after rejoin', pos(b), '(Tester at', pos(a) + ')');
  b.chat('/dungeon info'); await sleep(700);
  // 二人ともゴールドで帰還
  console.log('--- both extract');
  const w = dungeonWorld();
  for (let i = 0; i < 24 && !(a.entity.position.y < 0 && b.entity.position.y < 0); i++) {
    await tp('Tester', 107.5, 65, 105.5); await tp('Mate', 107.5, 65, 105.5);
    await rcon(`execute in minecraft:${w} positioned 107 65 105 as @e[distance=..40,type=!player,type=!item] run data merge entity @s {NoAI:1b}`);
    await sleep(500);
  }
  await sleep(800);
  a.chat('/dungeon info'); b.chat('/dungeon info'); await sleep(1000);
  a.quit(); b.quit(); await sleep(500); process.exit(0);
})().catch(e => { console.log('ERR', e); process.exit(1); });
