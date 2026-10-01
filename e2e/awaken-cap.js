const { connect, rcon, sleep, dungeonWorld, enter } = require('./lib');
const parse = s => { try { const j = JSON.parse(s); const f = o => typeof o === 'string' ? o : ((o.text||'') + (o.extra||[]).map(f).join('')); return f(j); } catch { return s; } };
const lore = it => { try { return (it.nbt.value.display.value.Lore.value.value || []).map(parse); } catch { return []; } };
(async () => {
  const bot = await connect('Tester');
  const log = []; bot.on('message', m => log.push(m.toString()));
  bot.chat('/dungeon admin givemoney Tester 100000'); await sleep(600);
  await enter(bot);
  const w = dungeonWorld();
  await rcon(`execute in minecraft:${w} positioned 55 66 55 run kill @e[distance=..100,type=!player,type=!item]`); await sleep(1200);
  for (let i = 0; i < 4; i++) { await rcon(`execute in minecraft:${w} positioned 55 66 55 run tp @e[distance=..100,type=item] Tester`); await sleep(1300); }
  const mats = () => bot.inventory.items().filter(i => lore(i).some(l => l.includes('ダンジョン素材'))).reduce((a, i) => a + i.count, 0);
  console.log('materials available:', mats());
  await rcon('give Tester iron_sword'); await sleep(700);
  await bot.equip(bot.inventory.items().find(i => i.name === 'iron_sword'), 'hand');
  const bal = async () => { const n = log.length; bot.chat('/shop balance'); await sleep(600); const l = log.slice(n).find(x => x.includes('所持金')); return +l.replace(/[^\d]/g, ''); };
  let prev = await bal();
  for (let k = 1; k <= 11; k++) {
    const n = log.length; bot.chat('/weaponly awaken'); await sleep(800);
    const now = await bal();
    console.log(`#${k}: ${log.slice(n).filter(x => /覚醒|最大|必要|不足/.test(x)).join(' ')} | cost=${prev - now} | materials left=${mats()}`);
    prev = now;
  }
  console.log('final lore:', lore(bot.heldItem).filter(l => l.includes('覚醒')).join(' / '));
  bot.quit(); await sleep(500); process.exit(0);
})().catch(e => { console.log('ERR', e); process.exit(1); });
