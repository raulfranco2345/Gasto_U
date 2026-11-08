const mongoose = require('mongoose');

async function connectDatabase(uri) {
  if (!uri) {
    throw new Error('Falta la variable de entorno MONGODB_URI');
  }
  mongoose.set('strictQuery', true);
  await mongoose.connect(uri);
  console.log('Conectado a MongoDB');
}

module.exports = { connectDatabase };
