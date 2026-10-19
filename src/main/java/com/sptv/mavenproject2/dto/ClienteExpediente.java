package com.sptv.mavenproject2.dto;

import modelo.Clavesol;
import modelo.Cliente;
import modelo.Cobranza;
import modelo.Libro;

/**
 * Vista agregada del expediente completo de un cliente:
 * Cliente + Clave SOL + Libros Contables + Estado de Cobranza.
 */
public class ClienteExpediente {

    private final Cliente cliente;
    private final Clavesol clavesol;
    private final Libro libro;
    private final Cobranza cobranza;

    public ClienteExpediente(Cliente cliente, Clavesol clavesol, Libro libro, Cobranza cobranza) {
        this.cliente = cliente;
        this.clavesol = clavesol;
        this.libro = libro;
        this.cobranza = cobranza;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public Clavesol getClavesol() {
        return clavesol;
    }

    public Libro getLibro() {
        return libro;
    }

    public Cobranza getCobranza() {
        return cobranza;
    }

    public String getUsuarioClaveSolTexto() {
        return clavesol != null && clavesol.getUsuario() != null ? clavesol.getUsuario() : "-";
    }

    public boolean isVentasActivo() {
        return libro != null && libro.getRegistrodeventas() == Libro.Estado.ACTIVO;
    }

    public boolean isComprasActivo() {
        return libro != null && libro.getRegistrodecompras() == Libro.Estado.ACTIVO;
    }

    public boolean isDiarioActivo() {
        return libro != null && libro.getDiariodeformatosimplificado() == Libro.Estado.ACTIVO;
    }

    public boolean isMensualAlDia() {
        return cobranza != null && Boolean.TRUE.equals(cobranza.getEstadomensual());
    }

    public boolean isAnualAlDia() {
        return cobranza != null && Boolean.TRUE.equals(cobranza.getEstadoanual());
    }

    public boolean isTotalAlDia() {
        return cobranza != null && Boolean.TRUE.equals(cobranza.getEstadototal());
    }
}
