const { connect, rcon, sleep, dungeonWorld, tp } = require('./lib');
(async () => {
  const bot = await connect('Tester');
  bot.chat('/dungeon admin givemoney Tester 5000'); await sleep(600);
  bot.chat('/dungeon enter'); await sleep(6000);
  const w = dungeonWorld();
  // 湧き数（F0は 30+0*3=30 が期待値。エンティティ総数）
  console.log('count mobs:', await rcon(`execute in minecraft:${w} run kill @e[type=!player,type=!item,type=!experience_orb]`));
  // 湧きが戻る前に再度生成は無い。gm/装備状態
  console.log('gamemode:', bot.game.gameMode);
  // mercy
  bot.chat('/dungeon mercy'); await sleep(1000);
  console.log('inv after mercy:', bot.inventory.items().map(i => i.name + 'x' + i.count).join(', '));
  // 保険
  await rcon('give Tester iron_sword'); await sleep(800);
  await bot.equip(bot.inventory.items().find(i => i.name === 'iron_sword'), 'hand'); await sleep(300);
  bot.chat('/dungeon insurance'); await sleep(600);
  bot.chat('/dungeon insure'); await sleep(1000);
  console.log('window after insure:', bot.lastWindowTitle, bot.currentWindow ? bot.currentWindow.slots.slice(0,27).filter(Boolean).map(i=>i.displayName).join('|') : 'none');
  if (bot.currentWindow) { await bot.simpleClick.leftMouse(bot.currentWindow.slots.findIndex((s,i)=>s&&i<27)); await sleep(1200); }
  bot.chat('/dungeon insurance'); await sleep(800);
  // 死亡
  console.log('kill:', await rcon('kill Tester')); await sleep(3000);
  console.log('after death inv:', bot.inventory.items().map(i => i.name + 'x' + i.count).join(', ') || '(empty)');
  const p = bot.entity.position; console.log('pos after respawn:', p.x.toFixed(1), p.y.toFixed(1), p.z.toFixed(1));
  bot.chat('/dungeon info'); await sleep(600);
  bot.chat('/dungeon insurance'); await sleep(800);
  bot.chat('/dungeon claim'); await sleep(800);
  bot.chat('/dungeon enter'); await sleep(4000);   // 再入室できるか
  bot.chat('/dungeon info'); await sleep(600);
  bot.quit(); await sleep(500); process.exit(0);
})().catch(e => { console.log('ERR', e); process.exit(1); });
