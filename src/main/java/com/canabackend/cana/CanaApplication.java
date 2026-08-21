package com.canabackend.cana;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class CanaApplication {

    /**
     * Zona del negocio. Todo el sistema opera en Guatemala y las columnas de
     * fecha son "without time zone", asi que la hora del reloj que ve el
     * servidor tiene que ser la misma que la del usuario.
     */
    private static final String ZONA_NEGOCIO = "America/Guatemala";

    public static void main(String[] args) {
        // Se fija ANTES de arrancar Spring para que todo LocalDate.now() y
        // LocalDateTime.now() use la hora local del negocio y no la del host.
        // Sin esto el contenedor corre en UTC y, estando en UTC-6, de 18:00 a
        // 23:59 locales el servidor ya esta en el dia siguiente: el dashboard
        // contaba las entregas de manana como "de hoy" y las validaciones de
        // "fecha no anterior a hoy" rechazaban pedidos del mismo dia.
        //
        // Va aqui y no solo en el -Duser.timezone del Dockerfile para que la
        // garantia viaje con el codigo: no depende de como se lance el jar ni
        // de la zona horaria de la maquina de quien desarrolla.
        TimeZone.setDefault(TimeZone.getTimeZone(ZONA_NEGOCIO));

        SpringApplication app = new SpringApplication(CanaApplication.class);
        // Se registra a mano y no con @Bean para que alcance a escuchar los
        // eventos tempranos, que ocurren antes de que exista el contexto.
        app.addListeners(new StartupProbe());
        app.run(args);
    }

}
