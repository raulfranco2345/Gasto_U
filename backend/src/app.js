const express = require('express');
const cors = require('cors');
const morgan = require('morgan');

const authRoutes = require('./routes/auth');
const expenseRoutes = require('./routes/expenses');
const goalRoutes = require('./routes/goals');
const budgetRoutes = require('./routes/budgets');
const { requireAuth } = require('./middleware/auth');
const { notFound, errorHandler } = require('./middleware/errors');

function createApp() {
  const app = express();

  app.use(cors());
  app.use(express.json({ limit: '100kb' }));
  if (process.env.NODE_ENV !== 'test') {
    app.use(morgan('dev'));
  }

  // Ruta pública para comprobar que el servicio está en línea (Render la usa como health check)
  app.get('/api/health', (req, res) => {
    res.json({ status: 'ok', service: 'gastou-api', time: new Date().toISOString() });
  });

  app.use('/api/auth', authRoutes);
  app.use('/api/expenses', requireAuth, expenseRoutes);
  app.use('/api/goals', requireAuth, goalRoutes);
  app.use('/api/budgets', requireAuth, budgetRoutes);

  app.use(notFound);
  app.use(errorHandler);

  return app;
}

module.exports = { createApp };
