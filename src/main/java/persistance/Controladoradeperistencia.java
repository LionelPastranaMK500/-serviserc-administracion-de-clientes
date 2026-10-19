package persistance;

import java.util.List;
import modelo.*;

/**
 * Fachada de persistencia corregida (en el sistema legado los métodos eran privados
 * e invocaban Long.MIN_VALUE en operaciones de búsqueda y eliminación).
 */
public class Controladoradeperistencia {

    private final ClavesolJpaController clavejpa = new ClavesolJpaController();
    private final ClienteJpaController clientejpa = new ClienteJpaController();
    private final CobranzaJpaController cobranzajpa = new CobranzaJpaController();
    private final LibroJpaController librojpa = new LibroJpaController();
    private final PagoMensualJpaController pagomenjpa = new PagoMensualJpaController();
    private final UsuariodelprogramaJpaController usuarioappjpa = new UsuariodelprogramaJpaController();
    private final ContrasenaGeneralJpaController contrasenageneraljpa = new ContrasenaGeneralJpaController();

    // Métodos para Clavesol
    public void crearclavesol(Clavesol clavesol) {
        clavejpa.create(clavesol);
    }

    public void editarclavesol(Clavesol clavesol) throws Exception {
        clavejpa.edit(clavesol);
    }

    public void eliminarclavesol(Clavesol clavesol) throws Exception {
        if (clavesol != null && clavesol.getIdusuario() != null) {
            clavejpa.destroy(clavesol.getIdusuario());
        }
    }

    public Clavesol buscarclavesol(Long id) {
        return clavejpa.findUsuario(id);
    }

    // Métodos para Cliente
    public void crearcliente(Cliente cliente) {
        clientejpa.create(cliente);
    }

    public void editarcliente(Cliente cliente) {
        clientejpa.edit(cliente);
    }

    public void eliminarcliente(Cliente cliente) {
        if (cliente != null && cliente.getIdcliente() != null) {
            clientejpa.destroy(cliente.getIdcliente());
        }
    }

    public Cliente buscarcliente(Long id) {
        return clientejpa.findCliente(id);
    }

    public Cliente buscarClientePorRuc(String ruc) {
        return clientejpa.findByRuc(ruc);
    }

    public List<Cliente> listarClientes() {
        return clientejpa.findClienteEntities();
    }

    // Métodos para Cobranza
    public void crearcobranza(Cobranza cobranza) {
        cobranzajpa.create(cobranza);
    }

    public void editarcobranza(Cobranza cobranza) throws Exception {
        cobranzajpa.edit(cobranza);
    }

    public void eliminarcobranza(Cobranza cobranza) throws Exception {
        if (cobranza != null && cobranza.getIdcobranza() != null) {
            cobranzajpa.destroy(cobranza.getIdcobranza());
        }
    }

    public Cobranza buscarcobranza(Long id) {
        return cobranzajpa.findCobranza(id);
    }

    // Métodos para Libro
    public void crearlibro(Libro libro) {
        librojpa.create(libro);
    }

    public void editarlibro(Libro libro) throws Exception {
        librojpa.edit(libro);
    }

    public void eliminarlibro(Libro libro) throws Exception {
        if (libro != null && libro.getIdlibros() != null) {
            librojpa.destroy(libro.getIdlibros());
        }
    }

    public Libro buscarlibro(Long id) {
        return librojpa.findLibro(id);
    }

    // Métodos para PagoMensual
    public void crearpagomensual(PagoMensual pagomensual) {
        pagomenjpa.create(pagomensual);
    }

    public void editarpagomensual(PagoMensual pagomensual) {
        pagomenjpa.edit(pagomensual);
    }

    public void eliminarpagomensual(PagoMensual pagomensual) {
        if (pagomensual != null && pagomensual.getIdpago() != null) {
            pagomenjpa.destroy(pagomensual.getIdpago());
        }
    }

    public PagoMensual buscarpagomensual(Long id) {
        return pagomenjpa.findPagoMensual(id);
    }

    // Métodos para Usuariodelprograma
    public void crearusuarioapp(Usuariodelprograma usuarioapp) throws Exception {
        usuarioappjpa.create(usuarioapp);
    }

    public void editarusuarioapp(Usuariodelprograma usuarioapp) throws Exception {
        usuarioappjpa.edit(usuarioapp);
    }

    public void eliminarusuarioapp(Usuariodelprograma usuarioapp) throws Exception {
        if (usuarioapp != null && usuarioapp.getIdusuario() != null) {
            usuarioappjpa.destroy(usuarioapp.getIdusuario());
        }
    }

    public Usuariodelprograma buscarusuarioapp(Long id) {
        return usuarioappjpa.findUsuariodelprograma(id);
    }

    public ContrasenaGeneralJpaController getContrasenaGeneralJpaController() {
        return contrasenageneraljpa;
    }

    public Cliente buscarCliente(Long id) {
        return buscarcliente(id);
    }

    public List<PagoMensual> listarPagosMensuales() {
        return pagomenjpa.findPagoMensualEntities();
    }
}
