package com.canabackend.cana;

import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

/**
 * Traza cada fase del arranque de Spring Boot con el estado del heap en ese
 * momento. Existe para diagnosticar arranques que mueren sin dejar stacktrace:
 * si el kernel mata el proceso por OOM (como pasa en una maquina de Fly con
 * poca RAM), no hay excepcion ni log de Spring, solo un corte seco. Viendo cual
 * fue la ultima fase impresa se sabe hasta donde llego.
 *
 * Usa System.out y no un Logger porque los primeros eventos ocurren antes de
 * que Spring Boot termine de inicializar el sistema de logging.
 */
public class StartupProbe implements ApplicationListener<ApplicationEvent> {

    private static final long INICIO = System.currentTimeMillis();
    private static final long MB = 1024L * 1024L;

    @Override
    public void onApplicationEvent(ApplicationEvent evento) {
        String fase = evento.getClass().getSimpleName();
        // Solo los eventos del ciclo de vida de la aplicacion (Starting,
        // EnvironmentPrepared, ContextInitialized, Prepared, Started, Ready,
        // Failed). El resto es ruido de contexto.
        if (!fase.startsWith("Application")) {
            return;
        }

        Runtime rt = Runtime.getRuntime();
        long usado = (rt.totalMemory() - rt.freeMemory()) / MB;

        System.out.printf(
                "[ARRANQUE] %-36s t=%6.1fs  heap usado=%4d MB  reservado=%4d MB  max=%4d MB%n",
                fase,
                (System.currentTimeMillis() - INICIO) / 1000.0,
                usado,
                rt.totalMemory() / MB,
                rt.maxMemory() / MB);

        if (evento instanceof ApplicationFailedEvent fallo) {
            System.out.println("[ARRANQUE] causa del fallo: " + fallo.getException());
        }
    }
}
