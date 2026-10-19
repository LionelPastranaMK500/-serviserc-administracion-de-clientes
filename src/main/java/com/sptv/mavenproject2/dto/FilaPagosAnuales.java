package com.sptv.mavenproject2.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;

/**
 * Representa la fila de pagos mensuales (Enero a Diciembre) de un cliente para un año específico.
 */
public class FilaPagosAnuales {

    public static final String[] NOMBRES_MESES = {
        "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    };

    private final Long idCliente;
    private final String ruc;
    private String nombre;
    private final BigDecimal honorarios;
    private final int anio;
    private final BigDecimal[] meses = new BigDecimal[12];

    public FilaPagosAnuales(Long idCliente, String ruc, String nombre, BigDecimal honorarios, int anio) {
        this.idCliente = idCliente;
        this.ruc = ruc;
        this.nombre = nombre;
        this.honorarios = honorarios != null ? honorarios : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        this.anio = anio;
        Arrays.fill(this.meses, BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
    }

    public Long getIdCliente() {
        return idCliente;
    }

    public String getRuc() {
        return ruc;
    }

    public String getNombre() {
        return nombre;
    }

    public String getNombreCliente() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public BigDecimal getHonorarios() {
        return honorarios;
    }

    public int getAnio() {
        return anio;
    }

    /**
     * Obtiene el monto de un mes (1 = Enero ... 12 = Diciembre) con 2 decimales.
     */
    public BigDecimal getMontoMes(int mes) {
        if (mes < 1 || mes > 12) {
            throw new IllegalArgumentException("El mes debe estar entre 1 y 12.");
        }
        return meses[mes - 1];
    }

    /**
     * Establece el monto de un mes (1 = Enero ... 12 = Diciembre) a partir de double.
     */
    public void setMontoMes(int mes, double monto) {
        if (mes < 1 || mes > 12) {
            throw new IllegalArgumentException("El mes debe estar entre 1 y 12.");
        }
        meses[mes - 1] = BigDecimal.valueOf(monto).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Establece el monto de un mes (1 = Enero ... 12 = Diciembre) a partir de BigDecimal.
     */
    public void setMontoMes(int mes, BigDecimal monto) {
        if (mes < 1 || mes > 12) {
            throw new IllegalArgumentException("El mes debe estar entre 1 y 12.");
        }
        meses[mes - 1] = (monto != null ? monto : BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getTotalAnual() {
        BigDecimal suma = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        for (BigDecimal m : meses) {
            if (m != null) {
                suma = suma.add(m);
            }
        }
        return suma.setScale(2, RoundingMode.HALF_UP);
    }

    public int getMesesPagadosCount() {
        int count = 0;
        for (BigDecimal m : meses) {
            if (m != null && m.compareTo(BigDecimal.ZERO) > 0) {
                count++;
            }
        }
        return count;
    }
}
