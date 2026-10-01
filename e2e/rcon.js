const net = require('net');
module.exports = function rcon(cmd, port = 25575, pass = 'test') {
  return new Promise((resolve, reject) => {
    const s = net.connect(port, '127.0.0.1');
    const pkt = (id, type, body) => {
      const b = Buffer.from(body, 'utf8');
      const buf = Buffer.alloc(14 + b.length);
      buf.writeInt32LE(10 + b.length, 0); buf.writeInt32LE(id, 4); buf.writeInt32LE(type, 8);
      b.copy(buf, 12); return buf;
    };
    let stage = 0;
    s.on('data', d => {
      if (stage === 0) { stage = 1; s.write(pkt(2, 2, cmd)); }
      else { resolve(d.slice(12, d.length - 2).toString('utf8')); s.end(); }
    });
    s.on('error', reject);
    s.write(pkt(1, 3, pass));
  });
};
