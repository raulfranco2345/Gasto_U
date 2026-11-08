const express = require('express');
const mongoose = require('mongoose');

const Expense = require('../models/Expense');
const Budget = require('../models/Budget');
const { HttpError } = require('../middleware/errors');
const {
  requireString,
  optionalString,
  requireAmount,
  requireMonth,
  requireDate,
  requireClientId,
  monthRange,
} = require('../utils/validation');

const router = express.Router();

// GET /api/expenses?month=AAAA-MM  -> gastos del usuario (todos si no se manda mes)
router.get('/', async (req, res) => {
  const filter = { user: req.userId };
  if (req.query.month) {
    const { start, end } = monthRange(requireMonth(req.query.month));
    filter.date = { $gte: start, $lt: end };
  }
  const expenses = await Expense.find(filter).sort({ date: -1, createdAt: -1 });
  res.json(expenses.map((e) => e.toPublicJSON()));
});

// GET /api/expenses/summary?month=AAAA-MM  -> total, presupuesto y gasto por categoría
router.get('/summary', async (req, res) => {
  const month = requireMonth(req.query.month);
  const { start, end } = monthRange(month);
  const userId = new mongoose.Types.ObjectId(String(req.userId));

  const byCategory = await Expense.aggregate([
    { $match: { user: userId, date: { $gte: start, $lt: end } } },
    { $group: { _id: '$category', total: { $sum: '$amount' }, count: { $sum: 1 } } },
    { $sort: { total: -1 } },
  ]);
  const budget = await Budget.findOne({ user: req.userId, month });

  const total = byCategory.reduce((sum, c) => sum + c.total, 0);
  res.json({
    month,
    total: Math.round(total * 100) / 100,
    budget: budget ? budget.amount : null,
    byCategory: byCategory.map((c) => ({
      category: c._id,
      total: Math.round(c.total * 100) / 100,
      count: c.count,
    })),
  });
});

// PUT /api/expenses/:clientId  -> crea o actualiza (idempotente, sirve para sincronizar)
router.put('/:clientId', async (req, res) => {
  const clientId = requireClientId(req.params.clientId);
  const data = {
    amount: requireAmount(req.body, 'amount'),
    category: requireString(req.body, 'category', { max: 60 }),
    date: requireDate(req.body, 'date'),
    displayDate: optionalString(req.body, 'displayDate', 60),
    paymentMethod: requireString(req.body, 'paymentMethod', { max: 60 }),
    note: optionalString(req.body, 'note', 300),
  };

  const expense = await Expense.findOneAndUpdate(
    { user: req.userId, clientId },
    { $set: data, $setOnInsert: { user: req.userId, clientId } },
    { new: true, upsert: true, runValidators: true }
  );
  res.json(expense.toPublicJSON());
});

// DELETE /api/expenses/:clientId
router.delete('/:clientId', async (req, res) => {
  const clientId = requireClientId(req.params.clientId);
  const result = await Expense.deleteOne({ user: req.userId, clientId });
  if (result.deletedCount === 0) {
    throw new HttpError(404, 'El gasto no existe');
  }
  res.status(204).end();
});

module.exports = router;
