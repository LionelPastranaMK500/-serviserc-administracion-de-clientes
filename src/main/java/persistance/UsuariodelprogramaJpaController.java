package persistance;

import java.util.List;
import javax.persistence.*;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import modelo.Usuariodelprograma;

public class UsuariodelprogramaJpaController {

    private final EntityManagerFactory emf;

    public UsuariodelprogramaJpaController() {
        this(PersistenceManager.getEntityManagerFactory());
    }

    public UsuariodelprogramaJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Usuariodelprograma usuario) throws Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            em.persist(usuario);
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            if (usuario.getIdusuario() != null && findUsuariodelprograma(usuario.getIdusuario()) != null) {
                throw new Exception("El usuario con id " + usuario.getIdusuario() + " ya existe.");
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Usuariodelprograma usuario) throws Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            em.merge(usuario);
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            if (usuario.getIdusuario() != null && findUsuariodelprograma(usuario.getIdusuario()) == null) {
                throw new Exception("El usuario con id " + usuario.getIdusuario() + " no existe.");
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Long id) throws Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Usuariodelprograma usuario;
            try {
                usuario = em.getReference(Usuariodelprograma.class, id);
                usuario.getIdusuario();
            } catch (EntityNotFoundException enfe) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                throw new Exception("El usuario con id " + id + " no existe.", enfe);
            }
            em.remove(usuario);
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public Usuariodelprograma findUsuariodelprograma(Long id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Usuariodelprograma.class, id);
        } finally {
            em.close();
        }
    }

    public Usuariodelprograma findByNombre(String nombre) {
        EntityManager em = getEntityManager();
        try {
            List<Usuariodelprograma> users = em.createQuery(
                            "SELECT u FROM Usuariodelprograma u WHERE u.nombre = :nombre",
                            Usuariodelprograma.class)
                    .setParameter("nombre", nombre)
                    .getResultList();
            return users.isEmpty() ? null : users.get(0);
        } finally {
            em.close();
        }
    }

    public List<Usuariodelprograma> findUsuariodelprogramaEntities() {
        return findUsuariodelprogramaEntities(true, -1, -1);
    }

    public List<Usuariodelprograma> findUsuariodelprogramaEntities(int maxResults, int firstResult) {
        return findUsuariodelprogramaEntities(false, maxResults, firstResult);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private List<Usuariodelprograma> findUsuariodelprogramaEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Usuariodelprograma.class));
            Query q = em.createQuery(cq);
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public int getUsuariodelprogramaCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Usuariodelprograma> rt = cq.from(Usuariodelprograma.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
}
