package com.sptv.mavenproject2.service;

import com.sptv.mavenproject2.dto.ClienteExpediente;
import com.sptv.mavenproject2.dto.ClienteFormularioDto;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;
import modelo.Clavesol;
import modelo.Cliente;
import modelo.Cobranza;
import modelo.Libro;
import modelo.PagoMensual;
import persistance.PersistenceManager;

/**
 * Servicio de lógica de negocio para la gestión integral de clientes,
 * credenciales Clave SOL, libros contables, cobranza y generación de cronograma de pagos.
 */
public class ClienteService {

    public static final int ANIO_INICIO_PAGOS = 2024;
    public static final int ANIO_FIN_PAGOS = 2030;
    public static final List<String> REGIMENES_DISPONIBLES = List.of(
            "Nuevo RUS",
            "Régimen Especial (RER)",
            "Régimen MYPE Tributario",
            "Régimen General"
    );

    /**
     * Valida todos los campos del formulario de cliente según las reglas de negocio.
     */
    public void validarFormulario(ClienteFormularioDto dto) {
        if (dto == null) {
            throw new ValidationException("Los datos del formulario no pueden ser nulos.");
        }

        String ruc = dto.getRuc() != null ? dto.getRuc().trim() : "";
        if (ruc.length() != 11 || !ruc.matches("\\d{11}")) {
            throw new ValidationException("El RUC debe tener exactamente 11 dígitos numéricos.");
        }

        if (isBlank(dto.getNombre())) {
            throw new ValidationException("El nombre o razón social del cliente es obligatorio.");
        }
        if (isBlank(dto.getRegimen())) {
            throw new ValidationException("El régimen tributario es obligatorio.");
        }
        if (dto.getFechaInicioActividades() == null) {
            throw new ValidationException("La fecha de inicio de actividades es obligatoria (formato YYYY-MM-DD).");
        }
        if (dto.getFechaPrimeraDeclaracion() == null) {
            throw new ValidationException("El periodo de primera declaración es obligatorio (formato YYYY-MM-DD).");
        }
        if (dto.getHonorarios() == null) {
            throw new ValidationException("El monto de honorarios es obligatorio.");
        }
        if (dto.getHonorarios().compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Los honorarios no pueden ser negativos.");
        }
        if (isBlank(dto.getCorreo())) {
            throw new ValidationException("El correo electrónico es obligatorio.");
        }
        String correo = dto.getCorreo().trim();
        if (!correo.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new ValidationException("El correo electrónico no tiene un formato válido (ejemplo: contacto@empresa.pe).");
        }
        if (isBlank(dto.getTelefono())) {
            throw new ValidationException("El teléfono de contacto es obligatorio.");
        }
        if (isBlank(dto.getUsuarioClaveSol())) {
            throw new ValidationException("El usuario de Clave SOL es obligatorio.");
        }
    }

    /**
     * Registra un nuevo cliente junto con su Clave SOL, Libros, Cobranza y los 84 registros
     * de PagoMensual (enero-diciembre 2024-2030) dentro de una única transacción atómica.
     */
    public ClienteExpediente registrarClienteIntegral(ClienteFormularioDto dto) {
        validarFormulario(dto);

        String ruc = dto.getRuc().trim();
        EntityManager em = PersistenceManager.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            Long count = em.createQuery("SELECT COUNT(c) FROM Cliente c WHERE c.ruc = :ruc", Long.class)
                    .setParameter("ruc", ruc)
                    .getSingleResult();
            if (count != null && count > 0) {
                throw new ValidationException("Ya existe un cliente con este RUC (" + ruc + ") en la base de datos.");
            }

            tx.begin();

            Cliente cliente = new Cliente();
            cliente.setNombre(dto.getNombre().trim());
            cliente.setRuc(ruc);
            cliente.setRegimen(dto.getRegimen().trim());
            cliente.setFechainicioact(Date.valueOf(dto.getFechaInicioActividades()));
            cliente.setPrimeradec(Date.valueOf(dto.getFechaPrimeraDeclaracion()));
            cliente.setHonorarios(dto.getHonorarios());
            cliente.setCorreo(dto.getCorreo().trim());
            cliente.setTelefono(dto.getTelefono().trim());
            String numCliente = !isBlank(dto.getNumeroDeCliente())
                    ? dto.getNumeroDeCliente().trim()
                    : dto.getTelefono().trim();
            cliente.setNumerodecliente(numCliente);

            em.persist(cliente);
            em.flush();

            Clavesol clavesol = new Clavesol();
            clavesol.setUsuario(dto.getUsuarioClaveSol().trim());
            if (!isBlank(dto.getClaveSolPassword())) {
                clavesol.setClave(dto.getClaveSolPassword().trim());
            }
            clavesol.setCliente(cliente);
            em.persist(clavesol);
            em.flush();

            Libro libro = new Libro();
            libro.setRegistrodeventas(dto.isRegistroVentasActivo() ? Libro.Estado.ACTIVO : Libro.Estado.INACTIVO);
            libro.setRegistrodecompras(dto.isRegistroComprasActivo() ? Libro.Estado.ACTIVO : Libro.Estado.INACTIVO);
            libro.setDiariodeformatosimplificado(dto.isDiarioSimplificadoActivo() ? Libro.Estado.ACTIVO : Libro.Estado.INACTIVO);
            libro.setCliente(cliente);
            libro.setClavesol(clavesol);
            libro.setRuc(cliente.getRuc());
            em.persist(libro);

            Cobranza cobranza = new Cobranza();
            cobranza.setCliente(cliente);
            cobranza.setEstadomensual(dto.isEstadoMensualAlDia());
            cobranza.setEstadoanual(dto.isEstadoAnualAlDia());
            cobranza.setEstadototal(dto.isEstadoTotalAlDia());
            cobranza.setRuc(cliente.getRuc());
            em.persist(cobranza);

            for (int mes = 1; mes <= 12; mes++) {
                for (int anio = ANIO_INICIO_PAGOS; anio <= ANIO_FIN_PAGOS; anio++) {
                    PagoMensual pagoMensual = new PagoMensual();
                    pagoMensual.setCliente(cliente);
                    pagoMensual.setAño(anio);
                    pagoMensual.setMes(mes);
                    pagoMensual.setMonto(0.0);
                    em.persist(pagoMensual);
                }
            }

            tx.commit();
            return new ClienteExpediente(cliente, clavesol, libro, cobranza);
        } catch (ValidationException ve) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw ve;
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("Error al agregar cliente: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    /**
     * Actualiza integralmente los datos del cliente, su Clave SOL, sus 3 libros contables
     * (incluyendo Registro de Compras, que en la versión Swing original no se actualizaba)
     * y su estado de cobranza (mensual, anual y total).
     */
    public ClienteExpediente actualizarClienteIntegral(String rucCliente, ClienteFormularioDto dto) {
        if (isBlank(rucCliente)) {
            throw new ValidationException("Debe especificar el RUC del cliente a editar.");
        }
        dto.setRuc(rucCliente.trim());
        validarFormulario(dto);

        String ruc = rucCliente.trim();
        EntityManager em = PersistenceManager.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            List<Cliente> clientes = em.createQuery("SELECT c FROM Cliente c WHERE c.ruc = :ruc", Cliente.class)
                    .setParameter("ruc", ruc)
                    .getResultList();
            if (clientes.isEmpty()) {
                tx.rollback();
                throw new ValidationException("No se encontró el cliente con RUC " + ruc + ".");
            }
            Cliente clienteExistente = clientes.get(0);

            clienteExistente.setNombre(dto.getNombre().trim());
            clienteExistente.setCorreo(dto.getCorreo().trim());
            clienteExistente.setTelefono(dto.getTelefono().trim());
            String numCliente = !isBlank(dto.getNumeroDeCliente())
                    ? dto.getNumeroDeCliente().trim()
                    : dto.getTelefono().trim();
            clienteExistente.setNumerodecliente(numCliente);
            clienteExistente.setRegimen(dto.getRegimen().trim());
            clienteExistente.setHonorarios(dto.getHonorarios());
            clienteExistente.setFechainicioact(Date.valueOf(dto.getFechaInicioActividades()));
            clienteExistente.setPrimeradec(Date.valueOf(dto.getFechaPrimeraDeclaracion()));
            em.merge(clienteExistente);

            // Actualizar o crear Clavesol
            List<Clavesol> claves = em.createQuery("SELECT cl FROM Clavesol cl WHERE cl.cliente.ruc = :ruc", Clavesol.class)
                    .setParameter("ruc", ruc)
                    .getResultList();
            Clavesol clavesol;
            if (claves.isEmpty()) {
                clavesol = new Clavesol();
                clavesol.setCliente(clienteExistente);
                clavesol.setUsuario(dto.getUsuarioClaveSol().trim());
                if (!isBlank(dto.getClaveSolPassword())) {
                    clavesol.setClave(dto.getClaveSolPassword().trim());
                }
                em.persist(clavesol);
                em.flush();
            } else {
                clavesol = claves.get(0);
                clavesol.setUsuario(dto.getUsuarioClaveSol().trim());
                if (dto.getClaveSolPassword() != null) {
                    clavesol.setClave(dto.getClaveSolPassword().trim());
                }
                em.merge(clavesol);
            }

            // Actualizar o crear Libro (los 3 libros contables)
            List<Libro> libros = em.createQuery("SELECT l FROM Libro l WHERE l.cliente.ruc = :ruc", Libro.class)
                    .setParameter("ruc", ruc)
                    .getResultList();
            Libro libro;
            if (libros.isEmpty()) {
                libro = new Libro();
                libro.setCliente(clienteExistente);
                libro.setClavesol(clavesol);
                libro.setRuc(ruc);
                libro.setRegistrodeventas(dto.isRegistroVentasActivo() ? Libro.Estado.ACTIVO : Libro.Estado.INACTIVO);
                libro.setRegistrodecompras(dto.isRegistroComprasActivo() ? Libro.Estado.ACTIVO : Libro.Estado.INACTIVO);
                libro.setDiariodeformatosimplificado(dto.isDiarioSimplificadoActivo() ? Libro.Estado.ACTIVO : Libro.Estado.INACTIVO);
                em.persist(libro);
            } else {
                libro = libros.get(0);
                libro.setRegistrodeventas(dto.isRegistroVentasActivo() ? Libro.Estado.ACTIVO : Libro.Estado.INACTIVO);
                libro.setRegistrodecompras(dto.isRegistroComprasActivo() ? Libro.Estado.ACTIVO : Libro.Estado.INACTIVO);
                libro.setDiariodeformatosimplificado(dto.isDiarioSimplificadoActivo() ? Libro.Estado.ACTIVO : Libro.Estado.INACTIVO);
                em.merge(libro);
            }

            // Actualizar o crear Cobranza
            List<Cobranza> cobranzas = em.createQuery("SELECT co FROM Cobranza co WHERE co.cliente.ruc = :ruc", Cobranza.class)
                    .setParameter("ruc", ruc)
                    .getResultList();
            Cobranza cobranza;
            if (cobranzas.isEmpty()) {
                cobranza = new Cobranza();
                cobranza.setCliente(clienteExistente);
                cobranza.setRuc(ruc);
                cobranza.setEstadomensual(dto.isEstadoMensualAlDia());
                cobranza.setEstadoanual(dto.isEstadoAnualAlDia());
                cobranza.setEstadototal(dto.isEstadoTotalAlDia());
                em.persist(cobranza);
            } else {
                cobranza = cobranzas.get(0);
                cobranza.setEstadomensual(dto.isEstadoMensualAlDia());
                cobranza.setEstadoanual(dto.isEstadoAnualAlDia());
                cobranza.setEstadototal(dto.isEstadoTotalAlDia());
                em.merge(cobranza);
            }

            tx.commit();
            return new ClienteExpediente(clienteExistente, clavesol, libro, cobranza);
        } catch (ValidationException ve) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw ve;
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("Ocurrió un error al actualizar los datos del cliente: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    public ClienteExpediente actualizarClienteIntegral(Long idCliente, ClienteFormularioDto dto) {
        if (idCliente == null) {
            throw new ValidationException("Debe especificar el ID del cliente a editar.");
        }
        EntityManager em = PersistenceManager.createEntityManager();
        String rucActual;
        try {
            Cliente c = em.find(Cliente.class, idCliente);
            if (c == null) {
                throw new ValidationException("No se encontró el cliente con ID " + idCliente + ".");
            }
            rucActual = c.getRuc();
        } finally {
            em.close();
        }
        return actualizarClienteIntegral(rucActual, dto);
    }

    /**
     * Actualización rápida desde la edición en línea de la Tabla de Clientes (Tabladeclientes).
     */
    public Cliente actualizarDatosRapidosEnTabla(
            Long idCliente,
            String nombre,
            String correo,
            BigDecimal honorarios,
            String numeroDeCliente,
            String regimen,
            String telefono) {

        if (idCliente == null) {
            throw new ValidationException("ID de cliente inválido.");
        }
        if (isBlank(nombre)) {
            throw new ValidationException("El nombre del cliente no puede estar vacío.");
        }
        if (isBlank(correo) || !correo.trim().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new ValidationException("Ingrese un correo electrónico válido.");
        }
        if (honorarios == null || honorarios.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Los honorarios deben ser un monto decimal mayor o igual a cero.");
        }
        if (isBlank(regimen)) {
            throw new ValidationException("El régimen no puede estar vacío.");
        }
        if (isBlank(telefono)) {
            throw new ValidationException("El teléfono no puede estar vacío.");
        }

        EntityManager em = PersistenceManager.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Cliente cliente = em.find(Cliente.class, idCliente);
            if (cliente == null) {
                tx.rollback();
                throw new ValidationException("No se encontró el cliente con ID " + idCliente + ".");
            }
            cliente.setNombre(nombre.trim());
            cliente.setCorreo(correo.trim());
            cliente.setHonorarios(honorarios);
            cliente.setNumerodecliente(numeroDeCliente != null ? numeroDeCliente.trim() : telefono.trim());
            cliente.setRegimen(regimen.trim());
            cliente.setTelefono(telefono.trim());
            em.merge(cliente);
            tx.commit();
            return cliente;
        } catch (ValidationException ve) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw ve;
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("Error al actualizar cliente en tabla: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    /**
     * Busca el expediente completo de un cliente por su RUC de 11 dígitos (BuscarporRuc).
     */
    public ClienteExpediente buscarExpedientePorRuc(String rucInput) {
        String ruc = rucInput != null ? rucInput.trim() : "";
        if (ruc.length() != 11 || !ruc.matches("\\d{11}")) {
            throw new ValidationException("El RUC debe tener exactamente 11 dígitos numéricos.");
        }

        EntityManager em = PersistenceManager.createEntityManager();
        try {
            TypedQuery<Cliente> queryCliente = em.createQuery(
                    "SELECT c FROM Cliente c WHERE c.ruc = :ruc", Cliente.class);
            queryCliente.setParameter("ruc", ruc);
            List<Cliente> clientes = queryCliente.getResultList();
            if (clientes.isEmpty()) {
                throw new ValidationException("No se encontró ningún cliente con el RUC proporcionado (" + ruc + ").");
            }
            Cliente cliente = clientes.get(0);

            List<Libro> libros = em.createQuery("SELECT l FROM Libro l WHERE l.cliente.ruc = :ruc", Libro.class)
                    .setParameter("ruc", ruc)
                    .getResultList();
            Libro libro = libros.isEmpty() ? null : libros.get(0);

            List<Cobranza> cobranzas = em.createQuery("SELECT co FROM Cobranza co WHERE co.cliente.ruc = :ruc", Cobranza.class)
                    .setParameter("ruc", ruc)
                    .getResultList();
            Cobranza cobranza = cobranzas.isEmpty() ? null : cobranzas.get(0);

            List<Clavesol> claves = em.createQuery("SELECT cl FROM Clavesol cl WHERE cl.cliente.ruc = :ruc", Clavesol.class)
                    .setParameter("ruc", ruc)
                    .getResultList();
            Clavesol clavesol = claves.isEmpty() ? null : claves.get(0);

            return new ClienteExpediente(cliente, clavesol, libro, cobranza);
        } finally {
            em.close();
        }
    }

    /**
     * Lista todos los expedientes de clientes registrados de forma eficiente.
     */
    public List<ClienteExpediente> listarExpedientes() {
        EntityManager em = PersistenceManager.createEntityManager();
        try {
            List<Cliente> clientes = em.createQuery("SELECT c FROM Cliente c ORDER BY c.idcliente ASC", Cliente.class)
                    .getResultList();
            List<Clavesol> claves = em.createQuery("SELECT cl FROM Clavesol cl", Clavesol.class).getResultList();
            List<Libro> libros = em.createQuery("SELECT l FROM Libro l", Libro.class).getResultList();
            List<Cobranza> cobranzas = em.createQuery("SELECT co FROM Cobranza co", Cobranza.class).getResultList();

            Map<String, Clavesol> claveByRuc = new HashMap<>();
            for (Clavesol cl : claves) {
                if (cl.getCliente() != null && cl.getCliente().getRuc() != null) {
                    claveByRuc.put(cl.getCliente().getRuc(), cl);
                }
            }

            Map<String, Libro> libroByRuc = new HashMap<>();
            for (Libro l : libros) {
                String ruc = l.getRuc() != null ? l.getRuc() : (l.getCliente() != null ? l.getCliente().getRuc() : null);
                if (ruc != null) {
                    libroByRuc.put(ruc, l);
                }
            }

            Map<String, Cobranza> cobranzaByRuc = new HashMap<>();
            for (Cobranza co : cobranzas) {
                String ruc = co.getRuc() != null ? co.getRuc() : (co.getCliente() != null ? co.getCliente().getRuc() : null);
                if (ruc != null) {
                    cobranzaByRuc.put(ruc, co);
                }
            }

            List<ClienteExpediente> resultado = new ArrayList<>(clientes.size());
            for (Cliente c : clientes) {
                String ruc = c.getRuc();
                resultado.add(new ClienteExpediente(
                        c,
                        claveByRuc.get(ruc),
                        libroByRuc.get(ruc),
                        cobranzaByRuc.get(ruc)
                ));
            }
            return resultado;
        } finally {
            em.close();
        }
    }

    /**
     * Elimina de forma segura un cliente y todos sus registros dependientes
     * (PagoMensual, Libro, Cobranza, Clavesol) en orden de integridad referencial.
     */
    public void eliminarClienteIntegral(Long idCliente) {
        if (idCliente == null) {
            throw new ValidationException("Debe indicar el ID del cliente a eliminar.");
        }
        EntityManager em = PersistenceManager.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Cliente cliente = em.find(Cliente.class, idCliente);
            if (cliente == null) {
                tx.rollback();
                throw new ValidationException("No se encontró el cliente con ID " + idCliente + ".");
            }
            String ruc = cliente.getRuc();

            List<PagoMensual> pagos = em.createQuery(
                    "SELECT p FROM PagoMensual p WHERE p.cliente.idcliente = :idCliente", PagoMensual.class)
                    .setParameter("idCliente", idCliente)
                    .getResultList();
            for (PagoMensual p : pagos) {
                em.remove(p);
            }

            List<Libro> libros = em.createQuery(
                    "SELECT l FROM Libro l WHERE l.ruc = :ruc OR l.cliente.ruc = :ruc", Libro.class)
                    .setParameter("ruc", ruc)
                    .getResultList();
            for (Libro l : libros) {
                em.remove(l);
            }

            List<Cobranza> cobranzas = em.createQuery(
                    "SELECT co FROM Cobranza co WHERE co.ruc = :ruc OR co.cliente.ruc = :ruc", Cobranza.class)
                    .setParameter("ruc", ruc)
                    .getResultList();
            for (Cobranza co : cobranzas) {
                em.remove(co);
            }

            List<Clavesol> claves = em.createQuery(
                    "SELECT cl FROM Clavesol cl WHERE cl.cliente.ruc = :ruc", Clavesol.class)
                    .setParameter("ruc", ruc)
                    .getResultList();
            for (Clavesol cl : claves) {
                em.remove(cl);
            }

            em.flush();
            em.remove(cliente);
            tx.commit();
        } catch (ValidationException ve) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw ve;
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("Error al eliminar el cliente y sus registros: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    /**
     * Convierte un ClienteExpediente existente en un ClienteFormularioDto listo para edición.
     */
    public ClienteFormularioDto toFormularioDto(ClienteExpediente exp) {
        ClienteFormularioDto dto = new ClienteFormularioDto();
        Cliente c = exp.getCliente();
        dto.setNombre(c.getNombre());
        dto.setRuc(c.getRuc());
        dto.setRegimen(c.getRegimen());
        if (c.getFechainicioact() != null) {
            dto.setFechaInicioActividades(c.getFechainicioact().toLocalDate());
        }
        if (c.getPrimeradec() != null) {
            dto.setFechaPrimeraDeclaracion(c.getPrimeradec().toLocalDate());
        }
        dto.setHonorarios(c.getHonorarios());
        dto.setCorreo(c.getCorreo());
        dto.setTelefono(c.getTelefono());
        dto.setNumeroDeCliente(c.getNumerodecliente());

        if (exp.getClavesol() != null) {
            dto.setUsuarioClaveSol(exp.getClavesol().getUsuario());
            dto.setClaveSolPassword(exp.getClavesol().getClave());
        }
        if (exp.getLibro() != null) {
            dto.setRegistroVentasActivo(exp.getLibro().getRegistrodeventas() == Libro.Estado.ACTIVO);
            dto.setRegistroComprasActivo(exp.getLibro().getRegistrodecompras() == Libro.Estado.ACTIVO);
            dto.setDiarioSimplificadoActivo(exp.getLibro().getDiariodeformatosimplificado() == Libro.Estado.ACTIVO);
        }
        if (exp.getCobranza() != null) {
            dto.setEstadoMensualAlDia(Boolean.TRUE.equals(exp.getCobranza().getEstadomensual()));
            dto.setEstadoAnualAlDia(Boolean.TRUE.equals(exp.getCobranza().getEstadoanual()));
            dto.setEstadoTotalAlDia(Boolean.TRUE.equals(exp.getCobranza().getEstadototal()));
        }
        return dto;
    }

    private static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }
}
