const express = require('express');

const Goal = require('../models/Goal');
const { HttpError } = require('../middleware/errors');
const {
  requireString,
  requireAmount,
  optionalDate,
  requireClientId,
} = require('../utils/validation');

const router = express.Router();

// GET /api/goals  -> metas de ahorro del usuario
router.get('/', async (req, res) => {
  const goals = await Goal.find({ user: req.userId }).sort({ isCompleted: 1, createdAt: -1 });
  res.json(goals.map((g) => g.toPublicJSON()));
});

// PUT /api/goals/:clientId  -> crea o actualiza (abonos, completar, editar)
router.put('/:clientId', async (req, res) => {
  const clientId = requireClientId(req.params.clientId);
  const targetAmount = requireAmount(req.body, 'targetAmount');
  const currentAmount = requireAmount(req.body, 'currentAmount', { allowZero: true });
  const data = {
    name: requireString(req.body, 'name', { max: 80 }),
    targetAmount,
    currentAmount,
    category: requireString(req.body, 'category', { max: 60 }),
    targetDate: optionalDate(req.body, 'targetDate'),
    isCompleted: req.body.isCompleted === true,
  };

  const goal = await Goal.findOneAndUpdate(
    { user: req.userId, clientId },
    { $set: data, $setOnInsert: { user: req.userId, clientId } },
    { new: true, upsert: true, runValidators: true }
  );
  res.json(goal.toPublicJSON());
});

// DELETE /api/goals/:clientId
router.delete('/:clientId', async (req, res) => {
  const clientId = requireClientId(req.params.clientId);
  const result = await Goal.deleteOne({ user: req.userId, clientId });
  if (result.deletedCount === 0) {
    throw new HttpError(404, 'La meta no existe');
  }
  res.status(204).end();
});

module.exports = router;
