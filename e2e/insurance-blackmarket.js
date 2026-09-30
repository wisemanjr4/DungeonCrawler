// 保険 + ブラックマーケット（前半: 再起動前）
const { connect, rcon, sleep, enter, waitFor } = require('./lib');
const parse = s => { try { const j = JSON.parse(s); const f = o => typeof o === 'string' ? o : ((o.text||'') + (o.extra||[]).map(f).join('')); return f(j); } catch { return s; } };
const lore = it => { try { return (it.nbt.value.display.value.Lore.value.value || []).map(parse); } catch { return []; } };
(async () => {
  const bot = await connect('Tester');
  const log = []; bot.on('message', m => log.push(m.toString()));
  const say = async (cmd, ms = 800) => { const n = log.length; bot.chat(cmd); await sleep(ms); return log.slice(n).filter(l => !l.includes('Rcon')); };
  const bal = async () => { const r = await say('/shop balance', 600); const l = r.find(x => x.includes('所持金')); return l ? +l.replace(/[^\d]/g, '') : null; };
  await say('/dungeon admin givemoney Tester 100000');

  console.log('--- 1. 契約（拠点で）');
  await rcon('give Tester iron_sword'); await sleep(700);
  await bot.equip(bot.inventory.items().find(i => i.name === 'iron_sword'), 'hand');
  console.log('insure:', (await say('/dungeon insure')).join(' | '));
  console.log('held lore:', lore(bot.heldItem).join(' / '));
  console.log('insure again:', (await say('/dungeon insure')).join(' | '));
  console.log('insurance list (生存中は空のはず):', (await say('/dungeon insurance')).join(' | '));

  console.log('--- 2. 生還すれば手元に残り、返還エントリは作られない（複製防止）');
  await enter(bot);
  const w = require('./lib').dungeonWorld();
  const p = () => bot.entity.position;
  await rcon(`execute in minecraft:${w} positioned 55 66 55 run kill @e[distance=..100,type=!player,type=!item]`);
  for (let i = 0; i < 16 && p().y > 0; i++) {
    await require('./lib').tp('Tester', 107.5, 65, 105.5);
    await rcon(`execute in minecraft:${w} positioned 107 65 105 as @e[distance=..40,type=!player,type=!item] run data merge entity @s {NoAI:1b}`);
    await sleep(500);
  }
  await waitFor(() => p().y < 0); await sleep(1000);
  console.log('extracted, sword still in inventory:', bot.inventory.items().some(i => i.name === 'iron_sword'));
  console.log('insurance list after extract:', (await say('/dungeon insurance')).join(' | '));

  console.log('--- 3. 死亡（未保険8本 + 保険1本）');
  await rcon('give Tester diamond_sword 8'); await sleep(900);
  console.log('swords in inventory:', bot.inventory.items().filter(i => i.name.endsWith('_sword')).length);
  await enter(bot);
  console.log('kill:', await rcon('kill Tester')); await sleep(3000);
  console.log('inventory after death:', bot.inventory.items().length, 'items');
  console.log('insurance list:', (await say('/dungeon insurance')).join(' | '));
  console.log('claim now (期限前):', (await say('/dungeon claim')).join(' | '));

  console.log('--- 4. ブラックマーケット');
  await say('/dungeon npc blackmarket'); await sleep(500);
  const npc = Object.values(bot.entities).find(e => e.name === 'villager');
  let title = null; bot.once('windowOpen', w2 => { title = w2; });
  await bot.activateEntity(npc); await waitFor(() => title, 5000);
  const items = title ? title.slots.slice(0, 45).filter(Boolean) : [];
  console.log('stock shown:', items.length, items.slice(0, 5).map(i => i.name).join(','));
  if (items.length) {
    const b0 = await bal(), before = bot.inventory.items().filter(i => i.name === 'diamond_sword').length;
    const slot = title.slots.findIndex((it, i) => it && i < 45);
    await bot.simpleClick.leftMouse(slot); await sleep(1200);
    const b1 = await bal();
    console.log(`buy slot ${slot}: ${log.filter(l => l.includes('購入しました')).slice(-1)[0]} | balance ${b0}->${b1} | swords ${before}->${bot.inventory.items().filter(i => i.name === 'diamond_sword').length}`);
    console.log('window after buy:', bot.currentWindow ? 'still open (refreshed), stock=' + bot.currentWindow.slots.slice(0, 45).filter(Boolean).length : 'closed');
  }
  bot.quit(); await sleep(500); process.exit(0);
})().catch(e => { console.log('ERR', e); process.exit(1); });
