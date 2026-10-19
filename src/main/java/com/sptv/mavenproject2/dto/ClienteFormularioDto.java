package com.sptv.mavenproject2.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Datos de entrada para el registro y la edición integral de un cliente.
 */
public class ClienteFormularioDto {

    private String nombre;
    private String ruc;
    private String regimen;
    private LocalDate fechaInicioActividades;
    private LocalDate fechaPrimeraDeclaracion;
    private BigDecimal honorarios;
    private String correo;
    private String telefono;
    private String numeroDeCliente;
    private String usuarioClaveSol;
    private String claveSolPassword;
    private boolean registroVentasActivo;
    private boolean registroComprasActivo;
    private boolean diarioSimplificadoActivo;
    private boolean estadoMensualAlDia = true;
    private boolean estadoAnualAlDia = false;
    private boolean estadoTotalAlDia = true;

    public ClienteFormularioDto() {
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getRuc() {
        return ruc;
    }

    public void setRuc(String ruc) {
        this.ruc = ruc;
    }

    public String getRegimen() {
        return regimen;
    }

    public void setRegimen(String regimen) {
        this.regimen = regimen;
    }

    public LocalDate getFechaInicioActividades() {
        return fechaInicioActividades;
    }

    public void setFechaInicioActividades(LocalDate fechaInicioActividades) {
        this.fechaInicioActividades = fechaInicioActividades;
    }

    public LocalDate getFechaPrimeraDeclaracion() {
        return fechaPrimeraDeclaracion;
    }

    public void setFechaPrimeraDeclaracion(LocalDate fechaPrimeraDeclaracion) {
        this.fechaPrimeraDeclaracion = fechaPrimeraDeclaracion;
    }

    public BigDecimal getHonorarios() {
        return honorarios;
    }

    public void setHonorarios(BigDecimal honorarios) {
        this.honorarios = honorarios;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getNumeroDeCliente() {
        return numeroDeCliente;
    }

    public void setNumeroDeCliente(String numeroDeCliente) {
        this.numeroDeCliente = numeroDeCliente;
    }

    public String getUsuarioClaveSol() {
        return usuarioClaveSol;
    }

    public void setUsuarioClaveSol(String usuarioClaveSol) {
        this.usuarioClaveSol = usuarioClaveSol;
    }

    public String getClaveSolPassword() {
        return claveSolPassword;
    }

    public void setClaveSolPassword(String claveSolPassword) {
        this.claveSolPassword = claveSolPassword;
    }

    public boolean isRegistroVentasActivo() {
        return registroVentasActivo;
    }

    public void setRegistroVentasActivo(boolean registroVentasActivo) {
        this.registroVentasActivo = registroVentasActivo;
    }

    public boolean isRegistroComprasActivo() {
        return registroComprasActivo;
    }

    public void setRegistroComprasActivo(boolean registroComprasActivo) {
        this.registroComprasActivo = registroComprasActivo;
    }

    public boolean isDiarioSimplificadoActivo() {
        return diarioSimplificadoActivo;
    }

    public void setDiarioSimplificadoActivo(boolean diarioSimplificadoActivo) {
        this.diarioSimplificadoActivo = diarioSimplificadoActivo;
    }

    public boolean isEstadoMensualAlDia() {
        return estadoMensualAlDia;
    }

    public void setEstadoMensualAlDia(boolean estadoMensualAlDia) {
        this.estadoMensualAlDia = estadoMensualAlDia;
    }

    public boolean isEstadoAnualAlDia() {
        return estadoAnualAlDia;
    }

    public void setEstadoAnualAlDia(boolean estadoAnualAlDia) {
        this.estadoAnualAlDia = estadoAnualAlDia;
    }

    public boolean isEstadoTotalAlDia() {
        return estadoTotalAlDia;
    }

    public void setEstadoTotalAlDia(boolean estadoTotalAlDia) {
        this.estadoTotalAlDia = estadoTotalAlDia;
    }
}
