package persistance;

import modelo.*;

public class Controladoradeperistencia {

    ClavesolJpaController clavejpa = new ClavesolJpaController();
    ClienteJpaController clientejpa = new ClienteJpaController();
    CobranzaJpaController cobranzajpa = new CobranzaJpaController();
    LibroJpaController librojpa = new LibroJpaController();
    PagoMensualJpaController pagomenjpa = new PagoMensualJpaController();
    UsuariodelprogramaJpaController usuarioappjpa = new UsuariodelprogramaJpaController();

    private void crearclavesol(Clavesol clavesol) {
        clavejpa.create(clavesol);
    }

    private void editarclavesol(Clavesol clavesol) throws Exception {
        clavejpa.edit(clavesol);
    }

    private void eliminarclavesol(Clavesol clavesol) throws Exception {
        clavejpa.destroy(Long.MIN_VALUE);
    }

    private void buscarclavesol(Clavesol clavesol) {
        clavejpa.findUsuario(Long.MIN_VALUE);
    }

// Métodos para Cliente
    private void crearcliente(Cliente cliente) {
        clientejpa.create(cliente);
    }

    private void editarcliente(Cliente cliente) throws Exception {
        clientejpa.edit(cliente);
    }

    private void eliminarcliente(Cliente cliente) throws Exception {
        clientejpa.destroy(Long.MIN_VALUE);
    }

    private void buscarcliente(Cliente cliente) {
        clientejpa.findCliente(Long.MIN_VALUE);
    }

// Métodos para Cobranza
    private void crearcobranza(Cobranza cobranza) {
        cobranzajpa.create(cobranza);
    }

    private void editarcobranza(Cobranza cobranza) throws Exception {
        cobranzajpa.edit(cobranza);
    }

    private void eliminarcobranza(Cobranza cobranza) throws Exception {
        cobranzajpa.destroy(Long.MIN_VALUE);
    }

    private void buscarcobranza(Cobranza cobranza) {
        cobranzajpa.findCobranza(Long.MIN_VALUE);
    }

// Métodos para Libro
    private void crearlibro(Libro libro) {
        librojpa.create(libro);
    }

    private void editarlibro(Libro libro) throws Exception {
        librojpa.edit(libro);
    }

    private void eliminarlibro(Libro libro) throws Exception {
        librojpa.destroy(Long.MIN_VALUE);
    }

    private void buscarlibro(Libro libro) {
        librojpa.findLibro(Long.MIN_VALUE);
    }

// Métodos para PagoMensual
    private void crearpagomensual(PagoMensual pagomensual) {
        pagomenjpa.create(pagomensual);
    }

    private void editarpagomensual(PagoMensual pagomensual) throws Exception {
        pagomenjpa.edit(pagomensual);
    }

    private void eliminarpagomensual(PagoMensual pagomensual) throws Exception {
        pagomenjpa.destroy(Long.MIN_VALUE);
    }

    private void buscarpagomensual(PagoMensual pagomensual) {
        pagomenjpa.findPagoMensual(Long.MIN_VALUE);
    }

// Métodos para Usuariodelprograma
    private void crearusuarioapp(Usuariodelprograma usuarioapp) throws Exception {
        usuarioappjpa.create(usuarioapp);
    }

    private void editarusuarioapp(Usuariodelprograma usuarioapp) throws Exception {
        usuarioappjpa.edit(usuarioapp);
    }

    private void eliminarusuarioapp(Usuariodelprograma usuarioapp) throws Exception {
        usuarioappjpa.destroy(Long.MIN_VALUE);
    }

    private void buscarusuarioapp(Usuariodelprograma usuarioapp) {
        usuarioappjpa.findUsuariodelprograma(Long.MIN_VALUE);
    }
}
