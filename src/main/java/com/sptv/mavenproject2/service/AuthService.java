package com.sptv.mavenproject2.service;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;
import modelo.ContrasenaGeneral;
import modelo.Usuariodelprograma;
import persistance.PersistenceManager;

/**
 * Servicio de autenticación, gestión de usuarios del sistema y contraseña general de seguridad.
 */
public class AuthService {

    public static final String DEFAULT_ADMIN_USER = "admin";
    public static final String DEFAULT_ADMIN_PASS = "admin123";
    public static final String DEFAULT_GENERAL_PASS = "serviserc2024";

    /**
     * Garantiza que exista al menos un usuario administrador y una contraseña general
     * cuando la base de datos se inicializa por primera vez.
     */
    public void ensureInitialSeed() {
        EntityManager em = PersistenceManager.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Long userCount = em.createQuery("SELECT COUNT(u) FROM Usuariodelprograma u", Long.class)
                    .getSingleResult();
            if (userCount == null || userCount == 0L) {
                Usuariodelprograma admin = new Usuariodelprograma(
                        DEFAULT_ADMIN_USER,
                        PasswordHasher.generarHash(DEFAULT_ADMIN_PASS)
                );
                em.persist(admin);
            }

            Long generalPassCount = em.createQuery("SELECT COUNT(c) FROM ContrasenaGeneral c", Long.class)
                    .getSingleResult();
            if (generalPassCount == null || generalPassCount == 0L) {
                ContrasenaGeneral cg = new ContrasenaGeneral();
                cg.setContrasena(DEFAULT_GENERAL_PASS);
                em.persist(cg);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("Error al inicializar datos semilla de seguridad: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    /**
     * Verifica si el usuario por defecto (admin / admin123) sigue activo con su clave inicial.
     */
    public boolean isDefaultAdminActive() {
        EntityManager em = PersistenceManager.createEntityManager();
        try {
            List<Usuariodelprograma> users = em.createQuery(
                            "SELECT u FROM Usuariodelprograma u WHERE u.nombre = :nombre",
                            Usuariodelprograma.class)
                    .setParameter("nombre", DEFAULT_ADMIN_USER)
                    .getResultList();
            if (users.isEmpty()) {
                return false;
            }
            String expectedHash = PasswordHasher.generarHash(DEFAULT_ADMIN_PASS);
            return expectedHash.equals(users.get(0).getContraseña());
        } finally {
            em.close();
        }
    }

    public boolean isCredencialInicialPorDefectoActiva() {
        return isDefaultAdminActive();
    }

    /**
     * Autentica un usuario comparando el hash SHA-256 de la contraseña ingresada.
     */
    public Usuariodelprograma autenticar(String nombreUsuario, String passwordPlano) {
        if (nombreUsuario == null || nombreUsuario.trim().isEmpty()) {
            throw new ValidationException("Por favor, ingrese su nombre de usuario.");
        }
        if (passwordPlano == null || passwordPlano.isEmpty()) {
            throw new ValidationException("Por favor, ingrese su contraseña.");
        }

        String nombreLimpio = nombreUsuario.trim();
        String hashIngresado = PasswordHasher.generarHash(passwordPlano);

        EntityManager em = PersistenceManager.createEntityManager();
        try {
            TypedQuery<Usuariodelprograma> query = em.createQuery(
                    "SELECT u FROM Usuariodelprograma u WHERE u.nombre = :nombre",
                    Usuariodelprograma.class
            );
            query.setParameter("nombre", nombreLimpio);
            List<Usuariodelprograma> usuarios = query.getResultList();

            if (usuarios.isEmpty()) {
                throw new ValidationException("Usuario no encontrado.");
            }

            for (Usuariodelprograma usuario : usuarios) {
                if (hashIngresado.equals(usuario.getContraseña())) {
                    return usuario;
                }
            }
            throw new ValidationException("Contraseña incorrecta. Acceso denegado.");
        } finally {
            em.close();
        }
    }

    public List<Usuariodelprograma> listarUsuarios() {
        EntityManager em = PersistenceManager.createEntityManager();
        try {
            return em.createQuery("SELECT u FROM Usuariodelprograma u ORDER BY u.idusuario ASC", Usuariodelprograma.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public Usuariodelprograma registrarUsuario(String nombre, String passwordPlano) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new ValidationException("El nombre de usuario no puede estar vacío.");
        }
        String nombreLimpio = nombre.trim();
        if (nombreLimpio.length() > 20) {
            throw new ValidationException("El nombre de usuario no debe superar los 20 caracteres.");
        }
        if (passwordPlano == null || passwordPlano.trim().isEmpty()) {
            throw new ValidationException("La contraseña no puede estar vacía.");
        }
        if (passwordPlano.length() < 4) {
            throw new ValidationException("La contraseña debe tener al menos 4 caracteres.");
        }

        EntityManager em = PersistenceManager.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            Long existentes = em.createQuery(
                            "SELECT COUNT(u) FROM Usuariodelprograma u WHERE LOWER(u.nombre) = LOWER(:nombre)",
                            Long.class)
                    .setParameter("nombre", nombreLimpio)
                    .getSingleResult();
            if (existentes != null && existentes > 0) {
                throw new ValidationException("Ya existe un usuario registrado con el nombre '" + nombreLimpio + "'.");
            }

            tx.begin();
            String hash = PasswordHasher.generarHash(passwordPlano);
            Usuariodelprograma nuevo = new Usuariodelprograma(nombreLimpio, hash);
            em.persist(nuevo);
            tx.commit();
            return nuevo;
        } catch (ValidationException ve) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw ve;
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("Error al guardar el usuario: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    /**
     * Elimina un usuario verificando que el ID y el nombre coincidan (regla de negocio original)
     * e impidiendo dejar el sistema sin ningún usuario.
     */
    public void eliminarUsuario(Long idUsuario, String nombreConfirmacion) {
        if (idUsuario == null) {
            throw new ValidationException("Por favor, ingrese un ID de usuario válido.");
        }
        if (nombreConfirmacion == null || nombreConfirmacion.trim().isEmpty()) {
            throw new ValidationException("Por favor, ingrese el nombre del usuario para confirmar la eliminación.");
        }

        EntityManager em = PersistenceManager.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Usuariodelprograma usuario = em.find(Usuariodelprograma.class, idUsuario);
            if (usuario == null) {
                tx.rollback();
                throw new ValidationException("Usuario no encontrado con ID " + idUsuario + ".");
            }

            if (!usuario.getNombre().equals(nombreConfirmacion.trim())) {
                tx.rollback();
                throw new ValidationException("El nombre de usuario no coincide con el ID proporcionado.");
            }

            Long totalUsuarios = em.createQuery("SELECT COUNT(u) FROM Usuariodelprograma u", Long.class)
                    .getSingleResult();
            if (totalUsuarios != null && totalUsuarios <= 1L) {
                tx.rollback();
                throw new ValidationException("No se puede eliminar el único usuario activo del sistema.");
            }

            em.remove(usuario);
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
            throw new RuntimeException("Error al eliminar el usuario: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    /**
     * Verifica la Contraseña General de Seguridad (módulo SeguridadUsuarios).
     */
    public boolean verificarContrasenaGeneral(String passwordIngresada) {
        if (passwordIngresada == null || passwordIngresada.trim().isEmpty()) {
            throw new ValidationException("La contraseña general no puede estar vacía.");
        }

        EntityManager em = PersistenceManager.createEntityManager();
        try {
            List<ContrasenaGeneral> registros = em.createQuery(
                            "SELECT c FROM ContrasenaGeneral c ORDER BY c.id ASC",
                            ContrasenaGeneral.class)
                    .getResultList();
            if (registros.isEmpty()) {
                throw new ValidationException("No se encontró la contraseña general configurada.");
            }
            ContrasenaGeneral cg = registros.get(0);
            if (!PasswordHasher.matches(passwordIngresada.trim(), cg.getContrasena())) {
                throw new ValidationException("Contraseña general incorrecta. Acceso denegado.");
            }
            return true;
        } finally {
            em.close();
        }
    }

    /**
     * Permite actualizar la Contraseña General una vez desbloqueado el módulo de seguridad.
     */
    public void actualizarContrasenaGeneral(String nuevaContrasena) {
        if (nuevaContrasena == null || nuevaContrasena.trim().length() < 4) {
            throw new ValidationException("La nueva contraseña general debe tener al menos 4 caracteres.");
        }

        EntityManager em = PersistenceManager.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            List<ContrasenaGeneral> registros = em.createQuery(
                            "SELECT c FROM ContrasenaGeneral c ORDER BY c.id ASC",
                            ContrasenaGeneral.class)
                    .getResultList();
            String hashed = PasswordHasher.generarHash(nuevaContrasena.trim());
            ContrasenaGeneral cg;
            if (registros.isEmpty()) {
                cg = new ContrasenaGeneral();
                cg.setContrasena(hashed);
                em.persist(cg);
            } else {
                cg = registros.get(0);
                cg.setContrasena(hashed);
                em.merge(cg);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("Error al actualizar la contraseña general: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    /**
     * Permite actualizar la Contraseña General verificando primero la contraseña actual.
     */
    public void actualizarContrasenaGeneral(String contrasenaActual, String nuevaContrasena) {
        verificarContrasenaGeneral(contrasenaActual);
        actualizarContrasenaGeneral(nuevaContrasena);
    }
}
