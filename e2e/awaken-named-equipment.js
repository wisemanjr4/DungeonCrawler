const { connect, rcon, sleep, dungeonWorld, enter } = require('./lib');
const parse = s => { try { const j = JSON.parse(s); const f = o => typeof o === 'string' ? o : ((o.text||'') + (o.extra||[]).map(f).join('')); return f(j); } catch { return s; } };
const lore = it => { try { return (it.nbt.value.display.value.Lore.value.value || []).map(parse); } catch { return []; } };
const nameOf = it => { try { return parse(it.nbt.value.display.value.Name.value); } catch { return null; } };
(async () => {
  const bot = await connect('Tester');
  const log = []; bot.on('message', m => log.push(m.toString()));
  bot.chat('/dungeon admin givemoney Tester 100000'); await sleep(600);
  await enter(bot);
  const w = dungeonWorld();
  // 敵を倒してドロップを集める（武器・防具が出るまで数フロア）
  const collect = async () => {
    await rcon(`execute in minecraft:${w} positioned 55 66 55 run kill @e[distance=..100,type=!player,type=!item]`); await sleep(1200);
    for (let i = 0; i < 4; i++) { await rcon(`execute in minecraft:${w} positioned 55 66 55 run tp @e[distance=..100,type=item] Tester`); await sleep(1300); }
  };
  await collect();
  const named = bot.inventory.items().filter(i => nameOf(i) && lore(i).some(l => l.includes('等級')));
  console.log('named DC equipment dropped:', named.length);
  named.slice(0, 10).forEach(i => console.log('   ', i.name.padEnd(20), nameOf(i), '|', lore(i).find(l => l.includes('等級'))));
  const weapon = named.find(i => /_sword$|_axe$/.test(i.name));
  const armor = named.find(i => /_(helmet|chestplate|leggings|boots)$/.test(i.name));
  for (const [label, it] of [['weapon', weapon], ['armor', armor]]) {
    if (!it) { console.log(label, ': none dropped'); continue; }
    await bot.equip(it, 'hand'); await sleep(400);
    console.log(`[${label}] before:`, nameOf(bot.heldItem));
    for (let k = 0; k < 2; k++) { bot.chat('/weaponly awaken'); await sleep(900); }
    bot.chat('/weaponly modify 隼'); await sleep(900);
    console.log(`[${label}] after awaken x2 + modify 隼:`, nameOf(bot.heldItem));
    console.log(`[${label}] lore:`, lore(bot.heldItem).join(' / '));
    bot.chat('/weaponly unmodify'); await sleep(900);
    console.log(`[${label}] after unmodify:`, nameOf(bot.heldItem));
  }
  console.log('log msgs:', log.filter(x => /覚醒|改造|失敗|必要/.test(x)).slice(-6).join(' | '));
  bot.quit(); await sleep(500); process.exit(0);
})().catch(e => { console.log('ERR', e); process.exit(1); });
