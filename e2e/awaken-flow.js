const { connect, rcon, sleep, dungeonWorld, enter, tp } = require('./lib');
const lore = it => { try { return (it.nbt.value.display.value.Lore.value.value || []).map(s => { try { const j = JSON.parse(s); const f = o => typeof o === 'string' ? o : ((o.text||'') + (o.extra||[]).map(f).join('')); return f(j); } catch { return s; } }); } catch { return []; } };
const nameOf = it => { try { const s = it.nbt.value.display.value.Name.value; const j = JSON.parse(s); const f = o => typeof o === 'string' ? o : ((o.text||'') + (o.extra||[]).map(f).join('')); return f(j); } catch { return it.displayName; } };
const materials = bot => bot.inventory.items().filter(i => lore(i).some(l => l.includes('ダンジョン素材')));
(async () => {
  const bot = await connect('Tester');
  const log = []; bot.on('message', m => log.push(m.toString()));
  bot.chat('/dungeon admin givemoney Tester 100000'); await sleep(600);
  await enter(bot);
  const w = dungeonWorld();
  await rcon('give Tester iron_sword'); await sleep(700);
  await bot.equip(bot.inventory.items().find(i => i.name === 'iron_sword'), 'hand');

  // --- 1. ドロップ収集: 敵を倒してアイテムを集める
  const kill = () => rcon(`execute in minecraft:${w} positioned 55 66 55 run kill @e[distance=..100,type=!player,type=!item]`);
  console.log('kill:', await kill());
  await sleep(1500);
  console.log('items dropped:', await rcon(`execute in minecraft:${w} positioned 55 66 55 if entity @e[distance=..100,type=item]`));
  for (let i = 0; i < 4; i++) { await rcon(`execute in minecraft:${w} positioned 55 66 55 run tp @e[distance=..100,type=item] Tester`); await sleep(1500); }
  const inv = bot.inventory.items();
  console.log('inventory item kinds:', [...new Set(inv.map(i => i.name))].length, '| total stacks:', inv.length);
  const armorDrops = inv.filter(i => /_(helmet|chestplate|leggings|boots)$/.test(i.name));
  console.log('armor drops (material/slot vs name):');
  armorDrops.slice(0, 8).forEach(i => console.log('   ', i.name, '=>', nameOf(i)));
  const mats = materials(bot);
  console.log('dungeon materials:', mats.length, 'stacks =', mats.reduce((a, i) => a + i.count, 0), 'pcs', mats.slice(0,3).map(i => nameOf(i) + 'x' + i.count).join(', '));
  if (!mats.length) { console.log('NO MATERIAL DROPPED'); bot.quit(); process.exit(1); }

  // --- 2. 覚醒
  const bal = async () => { const n = log.length; bot.chat('/shop balance'); await sleep(700); const l = log.slice(n).find(x => x.includes('所持金')); return l ? +l.replace(/[^\d]/g, '') : null; };
  const matCount = () => materials(bot).reduce((a, i) => a + i.count, 0);
  const held = () => bot.heldItem;
  const total0 = matCount();
  console.log('--- awaken x3 | balance', await bal(), 'materials', total0);
  for (let k = 1; k <= 3; k++) {
    const n = log.length, b0 = await bal(), m0 = matCount();
    bot.chat('/weaponly awaken'); await sleep(1000);
    const b1 = await bal();
    console.log(`awaken #${k}: ${log.slice(n).filter(x => x.includes('覚醒')).join(' | ')} | balance ${b0}->${b1} (cost ${b0 - b1}) | materials ${m0}->${matCount()} | name: ${nameOf(held())}`);
  }
  console.log('lore:', lore(held()).join(' / '));

  // --- 3. リフォージ後も覚醒は保持される（仕様3.1）
  bot.chat('/weaponly reforge'); await sleep(900);
  bot.chat('/weaponly info'); await sleep(800);
  console.log('after reforge, name:', nameOf(held()), '| lore:', lore(held()).filter(l => l.includes('覚醒') || l.includes('ジェム') || l.includes('改')).join(' / '));

  // --- 4. 覚醒のダメージ反映（NoAIゾンビで測定）
  const tag = 'tg' + Date.now();
  await tp('Tester', 6.5, 65, 6.5); await sleep(500);
  await rcon(`execute in minecraft:${w} run summon zombie 6.5 65 9.5 {NoAI:1b,Tags:["${tag}"],Attributes:[{Name:"generic.max_health",Base:300}],Health:300f,PersistenceRequired:1b}`);
  await sleep(800);
  const z = Object.values(bot.entities).find(e => e.name === 'zombie' && e.position.distanceTo(bot.entity.position) < 5);
  const hp = async () => { const m = (await rcon(`execute in minecraft:${w} run data get entity @e[tag=${tag},limit=1] Health`)).match(/: ([\d.]+)f/); return m ? +m[1] : null; };
  await bot.lookAt(z.position.offset(0, 1, 0)); await sleep(1400);
  const h0 = await hp(); bot.attack(z); await sleep(700); const h1 = await hp();
  console.log(`damage with awakened sword: ${(h0 - h1).toFixed(2)}`);

  // --- 5. 残高不足のとき素材は返却される
  bot.chat('/dungeon admin givemoney Tester -100000'); await sleep(700);
  const bLow = await bal(), mLow = matCount();
  const n = log.length; bot.chat('/weaponly awaken'); await sleep(1000);
  console.log(`insufficient funds: balance=${bLow} -> msg: ${log.slice(n).join(' | ')} | materials ${mLow}->${matCount()}`);
  bot.quit(); await sleep(500); process.exit(0);
})().catch(e => { console.log('ERR', e); process.exit(1); });
