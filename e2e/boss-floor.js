const { connect, rcon, sleep, dungeonWorld, enter, tp, waitFor } = require('./lib');
const parse = s => { try { const j = JSON.parse(s); const f = o => typeof o === 'string' ? o : ((o.text||'') + (o.extra||[]).map(f).join('')); return f(j); } catch { return s; } };
const nameOf = it => { try { return parse(it.nbt.value.display.value.Name.value); } catch { return null; } };
(async () => {
  const bot = await connect('Tester');
  const log = []; bot.on('message', m => log.push(m.toString()));
  await enter(bot);
  const w = dungeonWorld();
  const p = () => bot.entity.position;
  const clear = z => rcon(`execute in minecraft:${w} positioned 55 66 ${z} run kill @e[distance=..100,type=!player,type=!item]`);
  for (let f = 0; f < 10; f++) {
    await clear(55 + 121 * f);
    for (let i = 0; i < 30 && p().z < 121 * (f + 1); i++) { await tp('Tester', 105.5, 65, 105.5 + 121 * f); await sleep(500); }
    process.stdout.write(`F${f + 1} `);
  }
  console.log('\narrived z=', p().z.toFixed(1), '(F10 expects ~1216)');
  await sleep(2500);
  console.log('msgs:', log.filter(l => /BOSS|休息|=== 10F/.test(l)).join(' | '));
  const zc = 55 + 1210;
  const q = sel => rcon(`execute in minecraft:${w} positioned 55 66 ${zc} if entity @e[distance=..120,${sel}]`);
  const bosses = ['iron_golem','zombie','enderman','ravager','wither','warden','evoker','piglin_brute','elder_guardian','ender_dragon'];
  let bossType = null;
  for (const t of bosses) { const r = await q(`type=${t},nbt={CustomNameVisible:1b}`); if (/count/.test(r)) { bossType = t; console.log('BOSS entity:', t, '|', r); } }
  const total = await q('type=!player,type=!item');
  console.log('entities on F10 (boss+mobs):', total);
  if (bossType) {
    const hp = await rcon(`execute in minecraft:${w} positioned 55 66 ${zc} run data get entity @e[type=${bossType},distance=..120,limit=1,nbt={CustomNameVisible:1b}] Health`);
    const mh = await rcon(`execute in minecraft:${w} positioned 55 66 ${zc} run data get entity @e[type=${bossType},distance=..120,limit=1,nbt={CustomNameVisible:1b}] Attributes[{Name:"minecraft:generic.max_health"}].Base`);
    console.log('boss health:', hp.trim(), '| max_health attr:', mh.trim());
    const posRaw = await rcon(`execute in minecraft:${w} positioned 55 66 ${zc} run data get entity @e[type=${bossType},distance=..120,limit=1,nbt={CustomNameVisible:1b}] Pos`);
    const [bx, by, bz] = [...posRaw.matchAll(/(-?[\d.]+)d/g)].map(m => m[1]);
    console.log('boss pos:', bx, by, bz, '(exit room centre is x=105.5 z=' + (1210 + 105.5) + ')');
    const snap = () => bot.inventory.items().map(i => i.name + '|' + (nameOf(i) || '') + '|' + i.count);
    const beforeKill = snap();
    console.log('kill boss:', await rcon(`execute in minecraft:${w} positioned 55 66 ${zc} run kill @e[type=${bossType},distance=..120,nbt={CustomNameVisible:1b}]`));
    await sleep(bossType === 'ender_dragon' ? 14000 : 1500);   // ドラゴンは死亡アニメーション後にドロップ
    for (let i = 0; i < 4; i++) { await rcon(`execute in minecraft:${w} positioned ${bx} ${by} ${bz} run tp @e[distance=..8,type=item] Tester`); await sleep(1300); }
    const after = snap(); const pool = [...beforeKill];
    const fresh = after.filter(x => { const k = pool.indexOf(x); if (k >= 0) { pool.splice(k, 1); return false; } return true; });
    console.log('NEW items after boss kill:', fresh.length ? fresh.join(' ; ') : '(none)');
  } else console.log('NO BOSS FOUND');
  bot.quit(); await sleep(500); process.exit(0);
})().catch(e => { console.log('ERR', e); process.exit(1); });
