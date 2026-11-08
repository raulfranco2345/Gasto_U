const mongoose = require('mongoose');

const goalSchema = new mongoose.Schema(
  {
    user: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true, index: true },
    clientId: { type: String, required: true },
    name: { type: String, required: true, trim: true, maxlength: 80 },
    targetAmount: { type: Number, required: true, min: 0.01 },
    currentAmount: { type: Number, default: 0, min: 0 },
    category: { type: String, required: true, trim: true, maxlength: 60 },
    targetDate: { type: String, default: null }, // AAAA-MM-DD o null
    isCompleted: { type: Boolean, default: false },
  },
  { timestamps: true }
);

goalSchema.index({ user: 1, clientId: 1 }, { unique: true });

goalSchema.methods.toPublicJSON = function toPublicJSON() {
  return {
    clientId: this.clientId,
    name: this.name,
    targetAmount: this.targetAmount,
    currentAmount: this.currentAmount,
    category: this.category,
    targetDate: this.targetDate,
    isCompleted: this.isCompleted,
    updatedAt: this.updatedAt.toISOString(),
  };
};

module.exports = mongoose.model('Goal', goalSchema);
