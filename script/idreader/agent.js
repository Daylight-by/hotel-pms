#!/usr/bin/env node
/**
 * 身份证读卡器本地 Agent（P2 用）
 *
 * 背景：读卡器是前台电脑的本地硬件（串口/USB/厂商 SDK），浏览器和后端都碰不到，
 *       所以做一个常驻本机的 WebSocket 小服务，前端入住页直连它读卡。
 *
 * 用法：
 *   node agent.js                # 默认 17888 端口，mock 模式
 *   node agent.js --port 17888 --mock
 *   node agent.js --selftest     # 自检：起服务 → 连上 → 读一次 → 退出
 *
 * 协议（JSON 文本帧）：
 *   请求： {"cmd":"read"}  |  {"cmd":"status"}
 *   响应： {"ok":true,"cmd":"read","data":{"name":"张三","idCard":"...","sex":1,
 *           "birthday":"1990-01-01","address":"...","photo":null}}
 *          {"ok":false,"cmd":"read","error":"读卡失败：未检测到身份证"}
 *
 * 零第三方依赖（WebSocket 帧编解码手写，只用 Node 内置 crypto/net/http）。
 *
 * TODO(P2 执行 agent)：真实模式下把 readRealCard() 换成厂商 SDK 调用
 *   （常见：华视/精伦/神思/中控，多为 DLL，可用 node-ffi-napi 或由厂商提供的
 *     HTTP 本地服务转发）。保留 mock 分支不要删，验收脚本依赖它。
 */
'use strict';

const http = require('http');
const crypto = require('crypto');
const net = require('net');

const WS_GUID = '258EAFA5-E914-47DA-95CA-C5AB0DC85B11';
const DEFAULT_PORT = 17888;

// ------------------------------------------------------------------ 参数
const argv = process.argv.slice(2);
function opt(name, def) {
  const i = argv.indexOf('--' + name);
  return i >= 0 && argv[i + 1] && !argv[i + 1].startsWith('--') ? argv[i + 1] : def;
}
const PORT = parseInt(opt('port', String(DEFAULT_PORT)), 10);
const MOCK = argv.includes('--mock') || !argv.includes('--real');
const SELFTEST = argv.includes('--selftest');

// ------------------------------------------------------------------ 读卡
const MOCK_CARD = {
  name: '张三',
  sex: 1,
  nation: '汉',
  birthday: '1990-01-01',
  address: '浙江省杭州市西湖区文三路 100 号',
  idCard: '330281199203151234',
  issueOrg: '杭州市公安局西湖分局',
  validFrom: '2015-03-01',
  validTo: '2035-03-01',
  photo: null
};

/** Mock 读卡：返回固定假身份证，用于演示与验收脚本 */
function readMockCard() {
  return Object.assign({}, MOCK_CARD);
}

/**
 * 真实读卡：TODO(P2) 接厂商 SDK
 * 必须在 3 秒内返回，否则前端会超时。失败请 throw new Error(原因)。
 */
function readRealCard() {
  throw new Error('未接入读卡器厂商 SDK，请使用 --mock 或在 readRealCard() 中实现对接');
}

function handleCommand(msg) {
  const cmd = (msg && msg.cmd) || '';
  if (cmd === 'status') {
    return { ok: true, cmd, data: { mock: MOCK, port: PORT, ready: true } };
  }
  if (cmd === 'read') {
    try {
      const data = MOCK ? readMockCard() : readRealCard();
      return { ok: true, cmd, data };
    } catch (e) {
      return { ok: false, cmd, error: String((e && e.message) || e) };
    }
  }
  return { ok: false, cmd, error: '未知命令：' + cmd };
}

// ------------------------------------------------------------------ WS 帧
/** 解析一个客户端帧（客户端必须 mask），返回 {opcode, payload} 或 null */
function decodeFrame(buf) {
  if (buf.length < 2) return null;
  const b0 = buf[0], b1 = buf[1];
  const fin = (b0 & 0x80) !== 0;
  const opcode = b0 & 0x0f;
  const masked = (b1 & 0x80) !== 0;
  let len = b1 & 0x7f;
  let offset = 2;
  if (len === 126) {
    if (buf.length < 4) return null;
    len = buf.readUInt16BE(2);
    offset = 4;
  } else if (len === 127) {
    if (buf.length < 10) return null;
    len = Number(buf.readBigUInt64BE(2));
    offset = 10;
  }
  let mask = null;
  if (masked) {
    if (buf.length < offset + 4) return null;
    mask = buf.slice(offset, offset + 4);
    offset += 4;
  }
  if (buf.length < offset + len) return null;
  const payload = Buffer.from(buf.slice(offset, offset + len));
  if (mask) {
    for (let i = 0; i < payload.length; i++) payload[i] ^= mask[i & 3];
  }
  return { fin, opcode, payload, total: offset + len };
}

/** 服务端→客户端帧（不 mask） */
function encodeFrame(payload, opcode = 0x1) {
  const data = Buffer.isBuffer(payload) ? payload : Buffer.from(String(payload), 'utf8');
  const len = data.length;
  let header;
  if (len < 126) {
    header = Buffer.alloc(2);
    header[1] = len;
  } else if (len < 65536) {
    header = Buffer.alloc(4);
    header[1] = 126;
    header.writeUInt16BE(len, 2);
  } else {
    header = Buffer.alloc(10);
    header[1] = 127;
    header.writeBigUInt64BE(BigInt(len), 2);
  }
  header[0] = 0x80 | opcode; // FIN + text
  return Buffer.concat([header, data]);
}

// ------------------------------------------------------------------ 服务
function startServer(port) {
  return new Promise((resolve, reject) => {
    const server = http.createServer((req, res) => {
      res.writeHead(200, { 'Content-Type': 'text/plain; charset=utf-8' });
      res.end('id-reader-agent ok (mock=' + MOCK + ')\n');
    });

    server.on('upgrade', (req, socket) => {
      const key = req.headers['sec-websocket-key'];
      if (!key) {
        socket.destroy();
        return;
      }
      const accept = crypto
        .createHash('sha1')
        .update(key + WS_GUID)
        .digest('base64');
      socket.write(
        'HTTP/1.1 101 Switching Protocols\r\n' +
          'Upgrade: websocket\r\n' +
          'Connection: Upgrade\r\n' +
          'Sec-WebSocket-Accept: ' + accept + '\r\n\r\n'
      );

      let buffer = Buffer.alloc(0);
      socket.on('data', (chunk) => {
        buffer = Buffer.concat([buffer, chunk]);
        // 一个循环里尽量多解几帧
        for (;;) {
          const frame = decodeFrame(buffer);
          if (!frame) break;
          buffer = buffer.slice(frame.total);
          if (frame.opcode === 0x8) {
            socket.end();
            return;
          }
          if (frame.opcode === 0x9) {
            socket.write(encodeFrame(frame.payload, 0xa)); // pong
            continue;
          }
          if (frame.opcode !== 0x1) continue;
          let msg = null;
          try {
            msg = JSON.parse(frame.payload.toString('utf8'));
          } catch (e) {
            socket.write(encodeFrame(JSON.stringify({ ok: false, error: 'JSON 解析失败' })));
            continue;
          }
          const resp = handleCommand(msg);
          socket.write(encodeFrame(JSON.stringify(resp)));
        }
      });

      socket.on('error', () => socket.destroy());
    });

    server.on('error', reject);
    server.listen(port, '127.0.0.1', () => resolve(server));
  });
}

// ------------------------------------------------------------------ 自检客户端
function wsRequest(port, payload) {
  return new Promise((resolve, reject) => {
    const key = crypto.randomBytes(16).toString('base64');
    const sock = net.connect(port, '127.0.0.1', () => {
      sock.write(
        'GET / HTTP/1.1\r\n' +
          'Host: 127.0.0.1:' + port + '\r\n' +
          'Upgrade: websocket\r\n' +
          'Connection: Upgrade\r\n' +
          'Sec-WebSocket-Key: ' + key + '\r\n' +
          'Sec-WebSocket-Version: 13\r\n\r\n'
      );
    });
    let buf = Buffer.alloc(0);
    let upgraded = false;
    sock.on('data', (chunk) => {
      buf = Buffer.concat([buf, chunk]);
      if (!upgraded) {
        const idx = buf.indexOf('\r\n\r\n');
        if (idx < 0) return;
        const head = buf.slice(0, idx).toString();
        if (!/101/.test(head)) {
          sock.destroy();
          reject(new Error('握手失败：' + head.split('\r\n')[0]));
          return;
        }
        upgraded = true;
        buf = buf.slice(idx + 4);
        sock.write(encodeFrame(JSON.stringify(payload)));
      }
      const frame = decodeFrame(buf);
      if (frame && frame.opcode === 0x1) {
        sock.destroy();
        try {
          resolve(JSON.parse(frame.payload.toString('utf8')));
        } catch (e) {
          reject(e);
        }
      }
    });
    sock.on('error', reject);
    setTimeout(() => {
      sock.destroy();
      reject(new Error('超时'));
    }, 5000);
  });
}

// ------------------------------------------------------------------ 入口
(async () => {
  if (SELFTEST) {
    const server = await startServer(0); // 随机端口，避免冲突
    const port = server.address().port;
    const status = await wsRequest(port, { cmd: 'status' });
    const read = await wsRequest(port, { cmd: 'read' });
    console.log('status ->', JSON.stringify(status));
    console.log('read   ->', JSON.stringify(read));
    server.close();
    const ok = status.ok === true && read.ok === true && !!read.data.idCard;
    console.log(ok ? 'SELFTEST PASS' : 'SELFTEST FAIL');
    process.exit(ok ? 0 : 1);
  }

  const server = await startServer(PORT);
  console.log('[id-reader-agent] listening on ws://127.0.0.1:%d (mock=%s)', PORT, MOCK);
  console.log('[id-reader-agent] 前端入住页连接该地址，发送 {"cmd":"read"} 即可读卡');
  process.on('SIGINT', () => {
    server.close();
    process.exit(0);
  });
})().catch((e) => {
  console.error('[id-reader-agent] 启动失败：', e.message);
  process.exit(1);
});
