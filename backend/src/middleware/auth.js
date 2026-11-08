const jwt = require('jsonwebtoken');
const { HttpError } = require('./errors');

const TOKEN_TTL = '30d';

function signToken(userId) {
  return jwt.sign({ sub: String(userId) }, process.env.JWT_SECRET, { expiresIn: TOKEN_TTL });
}

// Verifica el encabezado "Authorization: Bearer <token>" y deja el id del usuario en req.userId
function requireAuth(req, res, next) {
  const header = req.headers.authorization || '';
  const [scheme, token] = header.split(' ');
  if (scheme !== 'Bearer' || !token) {
    return next(new HttpError(401, 'Sesión no válida. Inicia sesión de nuevo.'));
  }
  try {
    const payload = jwt.verify(token, process.env.JWT_SECRET);
    req.userId = payload.sub;
    return next();
  } catch {
    return next(new HttpError(401, 'Tu sesión expiró. Inicia sesión de nuevo.'));
  }
}

module.exports = { signToken, requireAuth };
