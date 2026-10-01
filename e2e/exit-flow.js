// 出口フロー: 入室 → エメラルド(次フロア) → ゴールド(生還)
const { connect, rcon, sleep, tp, dungeonWorld, enter, waitFor } = require('./lib');
(async () => {
  const bot = await connect('Tester');
  const chat = []; bot.on('message', m => chat.push(m.toString()));
  await enter(bot);
  const world = dungeonWorld();
  const p = () => bot.entity.position;
  console.log('SPAWN', p().x.toFixed(1), p().y.toFixed(1), p().z.toFixed(1));

  // 判定だけを見たいので、そのフロアの敵を掃除して、目的の床に立ち続ける
  const clear = zc => rcon(`execute in minecraft:${world} positioned 55 66 ${zc} run kill @e[distance=..100,type=!player,type=!item]`);
  // 撤退ラッシュで湧く敵は動きを止める（ノックバックで足場から外れるのを防ぎ、判定だけを見る）
  const freeze = (x, z) => rcon(`execute in minecraft:${world} positioned ${x} 65 ${z} as @e[distance=..40,type=!player,type=!item] run data merge entity @s {NoAI:1b}`);
  const hold = async (x, z, done, secs = 14) => {
    for (let i = 0; i < secs * 2 && !done(); i++) { await tp('Tester', x, 65, z); await freeze(x, z); await sleep(500); }
  };

  await clear(55);
  await hold(105.5, 105.5, () => p().z >= 121);
  await sleep(1500);
  console.log('AFTER EMERALD', p().x.toFixed(1), p().y.toFixed(1), p().z.toFixed(1));
  console.log('advanced to 1F message:', chat.some(c => c.includes('=== 1F ===')));

  await clear(55 + 121);
  await hold(107.5, 226.5, () => p().y < 0);
  await sleep(1000);
  console.log('AFTER GOLD', p().x.toFixed(1), p().y.toFixed(1), p().z.toFixed(1));
  console.log('survived msg:', chat.some(c => c.includes('生還')));
  bot.chat('/dungeon info'); await sleep(600);
  console.log('left session:', chat.some(c => c.includes('ダンジョンに参加していません')));
  bot.quit(); await sleep(500); process.exit(0);
})().catch(e => { console.log('ERR', e); process.exit(1); });
