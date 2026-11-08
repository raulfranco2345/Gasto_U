const express = require('express');
const bcrypt = require('bcryptjs');

const User = require('../models/User');
const { signToken, requireAuth } = require('../middleware/auth');
const { HttpError } = require('../middleware/errors');
const { requireString, optionalString, requireEmail } = require('../utils/validation');

const router = express.Router();

// POST /api/auth/register  { nombre, email, password }
router.post('/register', async (req, res) => {
  const nombre = requireString(req.body, 'nombre', { min: 2, max: 80 });
  const email = requireEmail(req.body);
  const password = requireString(req.body, 'password', { min: 6, max: 72 });

  const exists = await User.exists({ email });
  if (exists) {
    throw new HttpError(409, 'Ya existe una cuenta con ese correo');
  }

  const passwordHash = await bcrypt.hash(password, 10);
  const user = await User.create({ nombre, email, passwordHash });

  res.status(201).json({ token: signToken(user._id), user: user.toPublicJSON() });
});

// POST /api/auth/login  { email, password }
router.post('/login', async (req, res) => {
  const email = requireEmail(req.body);
  const password = requireString(req.body, 'password', { min: 1, max: 72 });

  const user = await User.findOne({ email });
  const valid = user && (await bcrypt.compare(password, user.passwordHash));
  if (!valid) {
    throw new HttpError(401, 'Correo o contraseña incorrectos');
  }

  res.json({ token: signToken(user._id), user: user.toPublicJSON() });
});

// GET /api/auth/me  -> perfil del usuario autenticado
router.get('/me', requireAuth, async (req, res) => {
  const user = await User.findById(req.userId);
  if (!user) throw new HttpError(404, 'Usuario no encontrado');
  res.json(user.toPublicJSON());
});

// PUT /api/auth/me  { nombre, universidad }
router.put('/me', requireAuth, async (req, res) => {
  const nombre = requireString(req.body, 'nombre', { min: 2, max: 80 });
  const universidad = optionalString(req.body, 'universidad', 120);

  const user = await User.findByIdAndUpdate(
    req.userId,
    { nombre, universidad },
    { new: true, runValidators: true }
  );
  if (!user) throw new HttpError(404, 'Usuario no encontrado');
  res.json(user.toPublicJSON());
});

module.exports = router;
