package persistance;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import modelo.Clavesol;

public class ClavesolJpaController {

    private final EntityManagerFactory emf;

    public ClavesolJpaController() {
        this(PersistenceManager.getEntityManagerFactory());
    }

    public ClavesolJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Clavesol usuario) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(usuario);
            em.getTransaction().commit();
        } catch (RuntimeException ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw ex;
        } finally {
            em.close();
        }
    }

    public void edit(Clavesol usuario) throws Exception {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(usuario);
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw ex;
        } finally {
            em.close();
        }
    }

    public void destroy(Long id) throws Exception {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            Clavesol usuario = em.find(Clavesol.class, id);
            if (usuario != null) {
                em.remove(usuario);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw ex;
        } finally {
            em.close();
        }
    }

    public Clavesol findUsuario(Long id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Clavesol.class, id);
        } finally {
            em.close();
        }
    }

    public Clavesol findByRuc(String ruc) {
        EntityManager em = getEntityManager();
        try {
            List<Clavesol> list = em.createQuery(
                            "SELECT cl FROM Clavesol cl WHERE cl.cliente.ruc = :ruc", Clavesol.class)
                    .setParameter("ruc", ruc)
                    .getResultList();
            return list.isEmpty() ? null : list.get(0);
        } finally {
            em.close();
        }
    }
}
