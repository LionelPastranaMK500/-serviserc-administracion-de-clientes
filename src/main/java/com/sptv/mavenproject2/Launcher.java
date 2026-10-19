package com.sptv.mavenproject2;

/**
 * Lanzador estándar para aplicaciones JavaFX empaquetadas o ejecutadas desde IDE/Maven
 * sin requerir configuración manual de module-path en el comando java.
 */
public final class Launcher {

    private Launcher() {
    }

    public static void main(String[] args) {
        ServisercApp.main(args);
    }
}
