const { connect, sleep } = require('./lib');
const Vec3 = require('vec3');
(async () => {
  const bot = await connect('Tester');
  bot.chat('/dungeon enter'); await sleep(9000);
  const p = bot.entity.position;
  console.log('pos', p.toString(), 'below:', (bot.blockAt(p.offset(0, -1, 0)) || {}).name);
  const b = (x, y, z) => { const k = bot.blockAt(new Vec3(x, y, z)); return k ? k.name : 'null'; };
  console.log('x=6,z=3 y63..69:', [63,64,65,66,67,68,69].map(y => b(6,y,3)).join(','));
  console.log('corner (1,68,1):', b(1,68,1), '| outer wall x=0 z=5 y64..68:', [64,65,66,67,68].map(y => b(0,y,5)).join(','));
  console.log('line x=11 z=3 y64..68:', [64,65,66,67,68].map(y => b(11,y,3)).join(','));
  const doors = []; for (let z = 1; z <= 10; z++) if (b(11,65,z) === 'air') doors.push(z);
  console.log('door cells at x=11:', doors.join(',') || '(none)', doors.length ? '| y64:' + b(11,64,doors[0]) + ' y67:' + b(11,67,doors[0]) + ' y68:' + b(11,68,doors[0]) : '');
  console.log('exit emerald:', b(105,64,105), 'gold:', b(107,64,105));
  bot.quit(); await sleep(500); process.exit(0);
})();
