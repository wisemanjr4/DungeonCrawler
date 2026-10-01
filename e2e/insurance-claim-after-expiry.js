const { connect, rcon, sleep } = require('./lib');
const parse = s => { try { const j = JSON.parse(s); const f = o => typeof o === 'string' ? o : ((o.text||'') + (o.extra||[]).map(f).join('')); return f(j); } catch { return s; } };
const lore = it => { try { return (it.nbt.value.display.value.Lore.value.value || []).map(parse); } catch { return []; } };
(async () => {
  const bot = await connect('Tester');
  const log = []; bot.on('message', m => log.push(m.toString()));
  const say = async (cmd, ms = 900) => { const n = log.length; bot.chat(cmd); await sleep(ms); return log.slice(n).filter(l => !l.includes('Rcon')); };
  console.log('list before claim:', (await say('/dungeon insurance')).join(' | '));
  console.log('claim:', (await say('/dungeon claim')).join(' | '));
  const sword = bot.inventory.items().find(i => i.name === 'iron_sword');
  console.log('iron_sword returned:', !!sword, '| lore:', sword ? JSON.stringify(lore(sword)) : '-');
  console.log('list after claim:', (await say('/dungeon insurance')).join(' | '));
  console.log('claim again (二重回収不可):', (await say('/dungeon claim')).join(' | '));
  bot.quit(); await sleep(500); process.exit(0);
})();
