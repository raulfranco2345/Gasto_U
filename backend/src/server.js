require('dotenv').config();

const { createApp } = require('./app');
const { connectDatabase } = require('./config/db');

const PORT = process.env.PORT || 3000;

async function start() {
  if (!process.env.JWT_SECRET) {
    console.error('Falta la variable de entorno JWT_SECRET');
    process.exit(1);
  }
  await connectDatabase(process.env.MONGODB_URI);

  const app = createApp();
  app.listen(PORT, () => {
    console.log(`GastoU API escuchando en el puerto ${PORT}`);
  });
}

start().catch((error) => {
  console.error('No se pudo iniciar la API:', error.message);
  process.exit(1);
});
