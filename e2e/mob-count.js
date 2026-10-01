const { connect, rcon, sleep, dungeonWorld, enter } = require('./lib');
(async () => {
  const bot = await connect('Tester');
  await enter(bot);
  const w = dungeonWorld();
  const c = async sel => { const r = await rcon(`execute in minecraft:${w} positioned 55 66 55 if entity @e[distance=..90,${sel}]`); const m = r.match(/count: (\d+)/); return m ? +m[1] : 0; };
  console.log('world', w);
  console.log('all non-player in floor0:', await c('type=!player'));
  for (const t of ['zombie','skeleton','spider','slime','creeper','item','cow','pig','sheep']) console.log(' ', t, await c('type=' + t));
  console.log('gamerule', await rcon(`execute in minecraft:${w} run gamerule doMobSpawning`));
  bot.quit(); await sleep(500); process.exit(0);
})();
