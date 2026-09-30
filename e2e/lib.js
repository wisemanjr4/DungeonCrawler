const mineflayer = require('mineflayer');
const fs = require('fs');
const rcon = require('./rcon');
const sleep = ms => new Promise(r => setTimeout(r, ms));
const strip = t => { try { const j = JSON.parse(t); const f = o => typeof o === 'string' ? o : ((o.text||'') + (o.extra||[]).map(f).join('')); return f(j); } catch { return t; } };
async function connect(name) {
  const bot = mineflayer.createBot({ host: '127.0.0.1', port: 25565, username: name, version: '1.19.4' });
  bot.chatLog = [];
  bot.on('message', m => { const t = m.toString(); bot.chatLog.push(t); console.log(`[${name}]`, t); });
  bot.on('kicked', r => console.log('kicked', name, r));
  bot.on('windowOpen', w => { bot.lastWindowTitle = strip(w.title); });
  await new Promise(r => bot.once('spawn', r));
  await rcon('op ' + name); await sleep(1200);
  return bot;
}
const dungeonWorld = () => fs.readdirSync(process.env.SERVER_DIR || '../server').filter(f => f.startsWith('dungeon_'))
  .map(f => ({ f, t: fs.statSync('../server/' + f).mtimeMs })).sort((a, b) => b.t - a.t)[0]?.f;
const tp = (name, x, y, z) => rcon(`execute in minecraft:${dungeonWorld()} run tp ${name} ${x} ${y} ${z}`);
// 条件が満たされるまで待つ（固定sleepだと非同期生成の完了前に進んでしまう）
async function waitFor(cond, timeout = 40000, step = 100) {
  const t0 = Date.now();
  while (!cond() && Date.now() - t0 < timeout) await sleep(step);
  return cond();
}
// ボットは戦わないので、テスト中は敵に倒されないようダメージ耐性を付ける（/kill は貫通する）
const godmode = name => rcon(`effect give ${name} minecraft:resistance 1000 4 true`);
const enter = async bot => { bot.chat('/dungeon enter'); await waitFor(() => bot.entity.position.y > 0); await godmode(bot.username); await sleep(1500); };
const waitZ = (bot, minZ) => waitFor(() => bot.entity.position.z >= minZ && bot.entity.position.y > 0).then(r => sleep(1200).then(() => r));
const waitHub = bot => waitFor(() => bot.entity.position.y < 0).then(r => sleep(800).then(() => r));
module.exports = { godmode, waitFor, enter, waitZ, waitHub, connect, rcon, sleep, strip, dungeonWorld, tp };
