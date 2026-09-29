const mineflayer = require('mineflayer');
const rcon = require('./rcon');
const fs = require('fs');
const sleep = ms => new Promise(r => setTimeout(r, ms));
(async () => {
  const bot = mineflayer.createBot({ host: '127.0.0.1', port: 25565, username: 'Tester', version: '1.19.4' });
  const chat = [];
  bot.on('message', m => { const t = m.toString(); chat.push(t); console.log('[chat]', t); });
  bot.on('kicked', r => console.log('kicked', r));
  await new Promise(r => bot.once('spawn', r));
  await rcon('op Tester'); await sleep(500);
  bot.chat('/dungeon enter'); await sleep(6000);
  const world = fs.readdirSync('../server').filter(f => f.startsWith('dungeon_'))[0];
  console.log('world', world);
  const tp = (x, y, z) => rcon(`execute in minecraft:${world} run tp Tester ${x} ${y} ${z}`);
  const p = bot.entity.position;
  console.log('SPAWN', p.x.toFixed(1), p.y.toFixed(1), p.z.toFixed(1));

  // 出口(エメラルド)へ: 部屋(9,9)中央 = (105.5, 65, 105.5)
  console.log('tp emerald:', await tp(105.5, 65, 105.5));
  await sleep(6000);
  let q = bot.entity.position;
  console.log('AFTER EMERALD', q.x.toFixed(1), q.y.toFixed(1), q.z.toFixed(1));
  const advanced = chat.some(c => c.includes('=== 1F ==='));
  console.log('advanced to 1F message:', advanced);

  // 次フロア(z+121)のゴールド(107.5,65,105.5+121)
  console.log('tp gold:', await tp(107.5, 65, 226.5));
  await sleep(7000);
  console.log('survived msg:', chat.some(c => c.includes('生還')));
  q = bot.entity.position;
  console.log('AFTER GOLD', q.x.toFixed(1), q.y.toFixed(1), q.z.toFixed(1));
  bot.chat('/dungeon info'); await sleep(500);
  bot.quit(); await sleep(500); process.exit(0);
})().catch(e => { console.log('ERR', e); process.exit(1); });
