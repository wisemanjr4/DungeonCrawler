const { connect, rcon, sleep, strip, dungeonWorld, tp } = require('./lib');
(async () => {
  const bot = await connect('Tester');
  bot.chat('/dungeon enter'); await sleep(5000);
  const zBase = f => f * 121;
  for (let f = 0; f < 5; f++) {          // F0→F5
    await tp('Tester', 105.5, 65, 105.5 + zBase(f)); await sleep(5500);
    const p = bot.entity.position;
    console.log(`floor ${f + 1} arrived at z=${p.z.toFixed(1)} (expect ~${(6.5 + zBase(f + 1)).toFixed(1)})`);
  }
  // F5 = 休息フロア。モブが湧いていないこと
  const mobs = Object.values(bot.entities).filter(e => e.type === 'hostile' || e.type === 'mob');
  console.log('mobs visible on rest floor:', mobs.length, mobs.slice(0,5).map(m => m.name).join(','));
  console.log('HP/food:', bot.health, bot.food);
  // 石ボタンを探す: 東壁付近 (109, 66, 105.5+605)
  await tp('Tester', 108.5, 65, 110.5 + 605); await sleep(2500);
  const btn = bot.findBlock({ matching: b => b && b.name === 'stone_button', maxDistance: 8 });
  console.log('stone_button found:', btn ? btn.position.toString() + ' facing/props ' + JSON.stringify(btn.getProperties()) : 'NO');
  if (btn) {
    bot.lastWindowTitle = null;
    await bot.activateBlock(btn); await sleep(1500);
    console.log('window title after click:', bot.lastWindowTitle);
    if (bot.currentWindow) {
      console.log('upgrade items:', bot.currentWindow.slots.slice(0, 27).filter(Boolean).map(i => i.displayName + (i.customName ? '' : '')).join(' | '));
      const slot = bot.currentWindow.slots.findIndex((it, idx) => it && idx < 27);
      await bot.simpleClick.leftMouse(slot); await sleep(1200);
      console.log('window after pick:', bot.currentWindow ? 'still open' : 'closed');
    }
  }
  bot.chat('/dungeon info'); await sleep(800);
  bot.quit(); await sleep(500); process.exit(0);
})().catch(e => { console.log('ERR', e); process.exit(1); });
