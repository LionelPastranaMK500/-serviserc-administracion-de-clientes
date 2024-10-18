package persistance;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import java.util.List;
import modelo.PagoMensual;

public class PagoMensualJpaController {

     private EntityManagerFactory emf = null;

    public PagoMensualJpaController() {
        emf = Persistence.createEntityManagerFactory("persisventas");
    }

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(PagoMensual pagoMensual) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(pagoMensual);
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (findPagoMensual(pagoMensual.getIdpago()) != null) {
                throw new RuntimeException("PagoMensual already exists.", ex);
            }
            throw new RuntimeException("Error while creating PagoMensual.", ex);
        } finally {
            em.close();
        }
    }

    public void edit(PagoMensual pagoMensual) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            pagoMensual = em.merge(pagoMensual);
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (findPagoMensual(pagoMensual.getIdpago()) == null) {
                throw new RuntimeException("The pagoMensual with id " + pagoMensual.getIdpago() + " no longer exists.", ex);
            }
            throw new RuntimeException("Error while editing PagoMensual.", ex);
        } finally {
            em.close();
        }
    }

    public void destroy(Long idpago) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            PagoMensual pagoMensual;
            try {
                pagoMensual = em.getReference(PagoMensual.class, idpago);
                pagoMensual.getIdpago(); // Esto lanzará una excepción si no existe
            } catch (Exception e) {
                throw new RuntimeException("The pagoMensual with id " + idpago + " no longer exists.", e);
            }
            em.remove(pagoMensual);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public PagoMensual findPagoMensual(Long idpago) {
        EntityManager em = getEntityManager();
        try {
            return em.find(PagoMensual.class, idpago);
        } finally {
            em.close();
        }
    }

    public List<PagoMensual> findPagoMensualEntities() {
        return findPagoMensualEntities(true, -1, -1);
    }

    public List<PagoMensual> findPagoMensualEntities(int maxResults, int firstResult) {
        return findPagoMensualEntities(false, maxResults, firstResult);
    }

    private List<PagoMensual> findPagoMensualEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<PagoMensual> query = em.createQuery("SELECT p FROM PagoMensual p", PagoMensual.class);
            if (!all) {
                query.setMaxResults(maxResults);
                query.setFirstResult(firstResult);
            }
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public int getPagoMensualCount() {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery("SELECT COUNT(p) FROM PagoMensual p", Long.class);
            return query.getSingleResult().intValue();
        } finally {
            em.close();
        }
    }
}