package persistance;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import modelo.Cobranza;

public class CobranzaJpaController {

    private final EntityManagerFactory emf;

    public CobranzaJpaController() {
        this(PersistenceManager.getEntityManagerFactory());
    }

    public CobranzaJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Cobranza cobranza) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(cobranza);
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

    public void edit(Cobranza cobranza) throws Exception {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(cobranza);
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
            Cobranza cobranza = em.find(Cobranza.class, id);
            if (cobranza != null) {
                em.remove(cobranza);
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

    public Cobranza findCobranza(Long id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Cobranza.class, id);
        } finally {
            em.close();
        }
    }

    public Cobranza findByRuc(String ruc) {
        EntityManager em = getEntityManager();
        try {
            List<Cobranza> list = em.createQuery(
                            "SELECT co FROM Cobranza co WHERE co.cliente.ruc = :ruc", Cobranza.class)
                    .setParameter("ruc", ruc)
                    .getResultList();
            return list.isEmpty() ? null : list.get(0);
        } finally {
            em.close();
        }
    }
}
