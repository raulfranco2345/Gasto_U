const mongoose = require('mongoose');

// Un presupuesto por usuario y por mes ("AAAA-MM")
const budgetSchema = new mongoose.Schema(
  {
    user: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true },
    month: { type: String, required: true },
    amount: { type: Number, required: true, min: 0 },
  },
  { timestamps: true }
);

budgetSchema.index({ user: 1, month: 1 }, { unique: true });

budgetSchema.methods.toPublicJSON = function toPublicJSON() {
  return { month: this.month, amount: this.amount };
};

module.exports = mongoose.model('Budget', budgetSchema);
