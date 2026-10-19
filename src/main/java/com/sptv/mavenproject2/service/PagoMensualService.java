package com.sptv.mavenproject2.service;

import com.sptv.mavenproject2.dto.FilaPagosAnuales;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import modelo.Cliente;
import modelo.PagoMensual;
import persistance.PersistenceManager;

/**
 * Servicio de gestión de Pagos Mensuales por año (2024 - 2030).
 */
public class PagoMensualService {

    public static final int ANIO_MIN = 2024;
    public static final int ANIO_MAX = 2030;
    public static final String[] NOMBRES_MESES = {
        "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    };

    public List<Integer> listarAniosDisponibles() {
        List<Integer> anios = new ArrayList<>();
        for (int y = ANIO_MIN; y <= ANIO_MAX; y++) {
            anios.add(y);
        }
        return anios;
    }

    /**
     * Obtiene la matriz de pagos de todos los clientes para un año determinado.
     * Corrige la limitación del sistema legado donde los clientes sin filas previas
     * o las consultas N+1 afectaban la tabla.
     */
    public List<FilaPagosAnuales> obtenerMatrizPorAnio(int year) {
        if (year < 2000 || year > 2100) {
            throw new ValidationException("El año seleccionado no es válido: " + year);
        }

        EntityManager em = PersistenceManager.createEntityManager();
        try {
            List<Cliente> clientes = em.createQuery("SELECT c FROM Cliente c ORDER BY c.idcliente ASC", Cliente.class)
                    .getResultList();

            Map<Long, FilaPagosAnuales> mapaFilas = new LinkedHashMap<>();
            for (Cliente c : clientes) {
                mapaFilas.put(
                        c.getIdcliente(),
                        new FilaPagosAnuales(c.getIdcliente(), c.getRuc(), c.getNombre(), c.getHonorarios(), year)
                );
            }

            List<PagoMensual> pagos = em.createQuery(
                            "SELECT p FROM PagoMensual p WHERE p.año = :year", PagoMensual.class)
                    .setParameter("year", year)
                    .getResultList();

            for (PagoMensual pago : pagos) {
                if (pago.getCliente() != null && pago.getCliente().getIdcliente() != null) {
                    Long idCliente = pago.getCliente().getIdcliente();
                    FilaPagosAnuales fila = mapaFilas.get(idCliente);
                    if (fila == null) {
                        Cliente c = pago.getCliente();
                        fila = new FilaPagosAnuales(c.getIdcliente(), c.getRuc(), c.getNombre(), c.getHonorarios(), year);
                        mapaFilas.put(idCliente, fila);
                    }
                    int mes = pago.getMes() != null ? pago.getMes() : 0;
                    if (mes >= 1 && mes <= 12) {
                        fila.setMontoMes(mes, pago.getMonto());
                    }
                }
            }

            return new ArrayList<>(mapaFilas.values());
        } finally {
            em.close();
        }
    }

    /**
     * Actualiza o crea el registro de PagoMensual para un cliente, mes (1..12) y año dados.
     */
    public PagoMensual actualizarPagoMensual(Long idCliente, int mes, int anio, double monto) {
        if (idCliente == null) {
            throw new ValidationException("El ID del cliente es obligatorio.");
        }
        if (mes < 1 || mes > 12) {
            throw new ValidationException("El mes debe estar comprendido entre 1 (Enero) y 12 (Diciembre).");
        }
        if (anio < 2000 || anio > 2100) {
            throw new ValidationException("El año indicado no es válido: " + anio);
        }
        if (Double.isNaN(monto) || Double.isInfinite(monto) || monto < 0.0) {
            throw new ValidationException("Por favor, ingrese un monto válido mayor o igual a 0.00.");
        }

        EntityManager em = PersistenceManager.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            List<PagoMensual> existentes = em.createQuery(
                            "SELECT p FROM PagoMensual p WHERE p.cliente.idcliente = :idCliente AND p.año = :anio AND p.mes = :mes",
                            PagoMensual.class)
                    .setParameter("idCliente", idCliente)
                    .setParameter("anio", anio)
                    .setParameter("mes", mes)
                    .getResultList();

            PagoMensual pago;
            if (!existentes.isEmpty()) {
                pago = existentes.get(0);
                pago.setMonto(monto);
                em.merge(pago);
            } else {
                Cliente cliente = em.find(Cliente.class, idCliente);
                if (cliente == null) {
                    tx.rollback();
                    throw new ValidationException("Cliente no encontrado con ID " + idCliente + ".");
                }
                pago = new PagoMensual();
                pago.setCliente(cliente);
                pago.setAño(anio);
                pago.setMes(mes);
                pago.setMonto(monto);
                em.persist(pago);
            }

            tx.commit();
            return pago;
        } catch (ValidationException ve) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw ve;
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("Error al actualizar pago mensual: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    /**
     * Conserva la funcionalidad de Tabladedepagos que permite editar el nombre del cliente desde la matriz.
     */
    public void actualizarNombreCliente(Long idCliente, String nuevoNombre) {
        if (idCliente == null) {
            throw new ValidationException("Cliente no especificado.");
        }
        if (nuevoNombre == null || nuevoNombre.trim().isEmpty()) {
            throw new ValidationException("El nombre del cliente no puede estar vacío.");
        }

        EntityManager em = PersistenceManager.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Cliente cliente = em.find(Cliente.class, idCliente);
            if (cliente == null) {
                tx.rollback();
                throw new ValidationException("Cliente no encontrado.");
            }
            cliente.setNombre(nuevoNombre.trim());
            em.merge(cliente);
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
            throw new RuntimeException("Error al actualizar el nombre del cliente: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    public List<FilaPagosAnuales> obtenerMatrizAnual(int year) {
        return obtenerMatrizPorAnio(year);
    }

    public PagoMensual actualizarPagoMensual(Long idCliente, int anio, int mes, java.math.BigDecimal monto) {
        if (monto == null || monto.compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new ValidationException("Por favor, ingrese un monto válido mayor o igual a 0.00.");
        }
        return actualizarPagoMensual(idCliente, mes, anio, monto.doubleValue());
    }

    public void actualizarNombreClienteDesdePagos(Long idCliente, String nuevoNombre) {
        actualizarNombreCliente(idCliente, nuevoNombre);
    }
}
