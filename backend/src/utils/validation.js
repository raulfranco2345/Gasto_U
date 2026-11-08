const { HttpError } = require('../middleware/errors');

const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/;
const MONTH_REGEX = /^\d{4}-(0[1-9]|1[0-2])$/;
const DATE_REGEX = /^\d{4}-\d{2}-\d{2}$/;
const MAX_AMOUNT = 10_000_000;

function requireString(body, field, { min = 1, max = 200 } = {}) {
  const value = typeof body[field] === 'string' ? body[field].trim() : '';
  if (value.length < min || value.length > max) {
    throw new HttpError(400, `El campo "${field}" debe tener entre ${min} y ${max} caracteres`);
  }
  return value;
}

function optionalString(body, field, max = 300) {
  if (body[field] === undefined || body[field] === null) return '';
  if (typeof body[field] !== 'string') {
    throw new HttpError(400, `El campo "${field}" debe ser texto`);
  }
  const value = body[field].trim();
  if (value.length > max) {
    throw new HttpError(400, `El campo "${field}" no puede tener más de ${max} caracteres`);
  }
  return value;
}

function requireAmount(body, field, { allowZero = false } = {}) {
  const value = Number(body[field]);
  const lowerOk = allowZero ? value >= 0 : value > 0;
  if (!Number.isFinite(value) || !lowerOk || value > MAX_AMOUNT) {
    throw new HttpError(400, `El campo "${field}" debe ser un monto válido`);
  }
  return Math.round(value * 100) / 100;
}

function requireEmail(body) {
  const email = requireString(body, 'email', { min: 5, max: 120 }).toLowerCase();
  if (!EMAIL_REGEX.test(email)) {
    throw new HttpError(400, 'El correo electrónico no es válido');
  }
  return email;
}

function requireMonth(value) {
  if (!MONTH_REGEX.test(value || '')) {
    throw new HttpError(400, 'El mes debe tener el formato AAAA-MM');
  }
  return value;
}

function requireDate(body, field) {
  const value = body[field];
  if (!DATE_REGEX.test(value || '') || Number.isNaN(Date.parse(value))) {
    throw new HttpError(400, `El campo "${field}" debe tener el formato AAAA-MM-DD`);
  }
  return value;
}

function optionalDate(body, field) {
  if (body[field] === undefined || body[field] === null || body[field] === '') return null;
  return requireDate(body, field);
}

function requireClientId(value) {
  if (typeof value !== 'string' || value.length < 8 || value.length > 64) {
    throw new HttpError(400, 'Identificador inválido');
  }
  return value;
}

// Rango [inicio, fin) de un mes "AAAA-MM" como cadenas AAAA-MM-DD para comparar fechas
function monthRange(month) {
  const [year, m] = month.split('-').map(Number);
  const next = m === 12 ? `${year + 1}-01` : `${year}-${String(m + 1).padStart(2, '0')}`;
  return { start: `${month}-01`, end: `${next}-01` };
}

module.exports = {
  requireString,
  optionalString,
  requireAmount,
  requireEmail,
  requireMonth,
  requireDate,
  optionalDate,
  requireClientId,
  monthRange,
};
