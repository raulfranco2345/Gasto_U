// Pruebas que no necesitan base de datos: salud, validaciones y autenticación.
process.env.NODE_ENV = 'test';
process.env.JWT_SECRET = 'secreto-de-prueba';

const { test } = require('node:test');
const assert = require('node:assert');
const request = require('supertest');
const { createApp } = require('../src/app');
const { signToken } = require('../src/middleware/auth');
const { monthRange } = require('../src/utils/validation');

const app = createApp();

test('GET /api/health responde ok', async () => {
  const res = await request(app).get('/api/health');
  assert.strictEqual(res.status, 200);
  assert.strictEqual(res.body.status, 'ok');
});

test('registro rechaza correo inválido', async () => {
  const res = await request(app)
    .post('/api/auth/register')
    .send({ nombre: 'Walter', email: 'no-es-correo', password: '123456' });
  assert.strictEqual(res.status, 400);
  assert.match(res.body.message, /correo/);
});

test('registro rechaza contraseña corta', async () => {
  const res = await request(app)
    .post('/api/auth/register')
    .send({ nombre: 'Walter', email: 'walter@test.com', password: '123' });
  assert.strictEqual(res.status, 400);
});

test('JSON mal formado devuelve 400', async () => {
  const res = await request(app)
    .post('/api/auth/login')
    .set('Content-Type', 'application/json')
    .send('{"email":');
  assert.strictEqual(res.status, 400);
});

test('rutas protegidas exigen token', async () => {
  const res = await request(app).get('/api/expenses');
  assert.strictEqual(res.status, 401);
});

test('token inválido devuelve 401', async () => {
  const res = await request(app).get('/api/goals').set('Authorization', 'Bearer abc');
  assert.strictEqual(res.status, 401);
});

test('gasto con monto negativo devuelve 400', async () => {
  const token = signToken('64b7f9f1c2a4b5d6e7f8a9b0');
  const res = await request(app)
    .put('/api/expenses/1234567890abcdef')
    .set('Authorization', `Bearer ${token}`)
    .send({ amount: -5, category: 'Comida', date: '2026-11-08', paymentMethod: 'Efectivo' });
  assert.strictEqual(res.status, 400);
});

test('mes con formato incorrecto devuelve 400', async () => {
  const token = signToken('64b7f9f1c2a4b5d6e7f8a9b0');
  const res = await request(app)
    .get('/api/expenses/summary?month=2026-13')
    .set('Authorization', `Bearer ${token}`);
  assert.strictEqual(res.status, 400);
});

test('monthRange calcula el cambio de año', () => {
  assert.deepStrictEqual(monthRange('2026-12'), { start: '2026-12-01', end: '2027-01-01' });
});

test('ruta inexistente devuelve 404', async () => {
  const res = await request(app).get('/api/nada');
  assert.strictEqual(res.status, 404);
});
