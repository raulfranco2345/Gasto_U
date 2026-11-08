const express = require('express');

const Budget = require('../models/Budget');
const { HttpError } = require('../middleware/errors');
const { requireAmount, requireMonth } = require('../utils/validation');

const router = express.Router();

// GET /api/budgets  -> presupuestos de todos los meses
router.get('/', async (req, res) => {
  const budgets = await Budget.find({ user: req.userId }).sort({ month: -1 });
  res.json(budgets.map((b) => b.toPublicJSON()));
});

// PUT /api/budgets/:month  { amount }
router.put('/:month', async (req, res) => {
  const month = requireMonth(req.params.month);
  const amount = requireAmount(req.body, 'amount');

  const budget = await Budget.findOneAndUpdate(
    { user: req.userId, month },
    { $set: { amount }, $setOnInsert: { user: req.userId, month } },
    { new: true, upsert: true, runValidators: true }
  );
  res.json(budget.toPublicJSON());
});

// DELETE /api/budgets/:month
router.delete('/:month', async (req, res) => {
  const month = requireMonth(req.params.month);
  const result = await Budget.deleteOne({ user: req.userId, month });
  if (result.deletedCount === 0) {
    throw new HttpError(404, 'No hay presupuesto para ese mes');
  }
  res.status(204).end();
});

module.exports = router;
