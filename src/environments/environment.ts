// Este archivo es el que usa "ng build" (produccion) por defecto, sin pasar
// --configuration. Angular compila este valor DENTRO del bundle: no es una
// variable de entorno que se pueda cambiar despues del build, hay que
// reemplazar la URL de abajo por la real del backend desplegado (Render, Fly,
// etc.) y volver a desplegar el frontend en Vercel cada vez que cambie.
export const environment={
    production: true,
    apiUrl: 'https://TU-BACKEND-DESPLEGADO.onrender.com/cana/api/',
}
