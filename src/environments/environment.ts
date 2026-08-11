// Este archivo es el que usa "ng build" (produccion) por defecto, sin pasar
// --configuration. Angular compila este valor DENTRO del bundle: no es una
// variable de entorno que se pueda cambiar despues del build, asi que si el
// backend cambia de URL hay que editar esta linea y volver a desplegar el
// frontend en Vercel.
export const environment={
    production: true,
    apiUrl: 'https://eventos-cana.fly.dev/cana/api/',
}
