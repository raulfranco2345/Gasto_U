const mongoose = require('mongoose');

const userSchema = new mongoose.Schema(
  {
    nombre: { type: String, required: true, trim: true, maxlength: 80 },
    email: { type: String, required: true, unique: true, lowercase: true, trim: true },
    passwordHash: { type: String, required: true },
    universidad: { type: String, trim: true, maxlength: 120, default: '' },
  },
  { timestamps: true }
);

// Nunca se devuelve el hash de la contraseña al cliente
userSchema.methods.toPublicJSON = function toPublicJSON() {
  return {
    id: this._id.toString(),
    nombre: this.nombre,
    email: this.email,
    universidad: this.universidad,
    createdAt: this.createdAt.toISOString(),
  };
};

module.exports = mongoose.model('User', userSchema);
