const { connect, rcon, sleep, dungeonWorld, enter } = require('./lib');
(async () => {
  const bot = await connect('Tester');
  await enter(bot);
  const w = dungeonWorld();
  const c = async sel => { const r = await rcon(`execute in minecraft:${w} positioned 55 66 55 if entity @e[distance=..90,${sel}]`); const m = r.match(/count: (\d+)/); return m ? +m[1] : 0; };
  const types = ['zombie','skeleton','spider','slime','piglin','hoglin','wolf','polar_bear','iron_golem','warden','stray','wither_skeleton','pillager','blaze','shulker','guardian','cave_spider','vex','phantom','endermite','silverfish','creeper','ghast','ravager','enderman','witch','evoker','illusioner','bee','zombie_villager','husk','drowned','piglin_brute','magma_cube','chicken','horse','skeleton_horse','zoglin','vindicator','elder_guardian','wither','ender_dragon','item','experience_orb','armor_stand','area_effect_cloud','lightning_bolt'];
  let sum = 0;
  for (const t of types) { const n = await c('type=' + t); if (n) { console.log(' ', t, n); sum += n; } }
  console.log('sum', sum, 'all', await c('type=!player'));
  const r = await rcon(`execute in minecraft:${w} positioned 55 66 55 run data get entity @e[distance=..90,type=slime,limit=1] Size`); console.log(r);
  bot.quit(); await sleep(500); process.exit(0);
})();
