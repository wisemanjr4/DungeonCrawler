const { connect, rcon, sleep, tp, dungeonWorld } = require('./lib');
const hpOf = async (w, tag) => { const r = await rcon(`execute in minecraft:${w} run data get entity @e[tag=${tag},limit=1] Health`); const m = r.match(/: ([\d.]+)f/); return m ? +m[1] : null; };
async function hit(bot, w, label) {
  const tag = 'tgt' + Math.floor(Math.random() * 1e6);
  await rcon(`execute in minecraft:${w} run summon zombie 6.5 65 9.5 {NoAI:1b,Tags:["${tag}"],Attributes:[{Name:"generic.max_health",Base:200}],Health:200f,PersistenceRequired:1b,ArmorItems:[{},{},{},{}]}`);
  await sleep(700);
  const e = Object.values(bot.entities).find(x => x.name === 'zombie' && x.position.distanceTo(bot.entity.position) < 5);
  if (!e) { console.log(label, 'target not visible'); return; }
  await bot.lookAt(e.position.offset(0, 1, 0)); await sleep(1300);   // 攻撃クールダウン回復
  const before = await hpOf(w, tag);
  bot.attack(e); await sleep(600);
  const after = await hpOf(w, tag);
  console.log(`${label}: ${before} -> ${after}  damage=${(before - after).toFixed(2)}`);
  await rcon(`execute in minecraft:${w} run kill @e[tag=${tag}]`);
}
(async () => {
  const bot = await connect('Tester');
  bot.chat('/dungeon admin givemoney Tester 5000'); await sleep(600);
  bot.chat('/dungeon enter'); await sleep(8000);
  const w = dungeonWorld();
  await rcon('give Tester iron_sword'); await sleep(700);
  await bot.equip(bot.inventory.items().find(i => i.name === 'iron_sword'), 'hand'); await sleep(300);
  await tp('Tester', 6.5, 65, 6.5); await sleep(500);
  await hit(bot, w, 'plain iron_sword');
  bot.chat('/weaponly info'); await sleep(700);           // 初期化（基礎攻撃力付与）
  await hit(bot, w, 'weaponly-initialized');
  bot.chat('/weaponly modify 重'); await sleep(700);
  await hit(bot, w, '+ mod 重 (x1.15)');
  bot.chat('/weaponly gem apply atk'); await sleep(700);
  await hit(bot, w, '+ gem atk (x1.10)');
  bot.chat('/weaponly modify 烈'); await sleep(700);
  bot.chat('/weaponly info'); await sleep(700);
  bot.quit(); await sleep(500); process.exit(0);
})().catch(e => { console.log('ERR', e); process.exit(1); });
