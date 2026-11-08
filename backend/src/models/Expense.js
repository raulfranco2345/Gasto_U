const mongoose = require('mongoose');

// clientId es el UUID que genera la app. Permite que la app cree el gasto sin conexión
// y lo sincronice después sin duplicarlo (operación PUT idempotente).
const expenseSchema = new mongoose.Schema(
  {
    user: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true, index: true },
    clientId: { type: String, required: true },
    amount: { type: Number, required: true, min: 0.01 },
    category: { type: String, required: true, trim: true, maxlength: 60 },
    date: { type: String, required: true }, // AAAA-MM-DD
    displayDate: { type: String, default: '' },
    paymentMethod: { type: String, required: true, trim: true, maxlength: 60 },
    note: { type: String, default: '', maxlength: 300 },
  },
  { timestamps: true }
);

expenseSchema.index({ user: 1, clientId: 1 }, { unique: true });
expenseSchema.index({ user: 1, date: -1 });

expenseSchema.methods.toPublicJSON = function toPublicJSON() {
  return {
    clientId: this.clientId,
    amount: this.amount,
    category: this.category,
    date: this.date,
    displayDate: this.displayDate,
    paymentMethod: this.paymentMethod,
    note: this.note,
    updatedAt: this.updatedAt.toISOString(),
  };
};

module.exports = mongoose.model('Expense', expenseSchema);
