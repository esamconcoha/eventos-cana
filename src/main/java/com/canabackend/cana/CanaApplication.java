package com.canabackend.cana;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CanaApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(CanaApplication.class);
        // Se registra a mano y no con @Bean para que alcance a escuchar los
        // eventos tempranos, que ocurren antes de que exista el contexto.
        app.addListeners(new StartupProbe());
        app.run(args);
    }

}
