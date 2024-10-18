package persistance;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.Query;
import java.io.Serializable;
import java.util.List;
import modelo.ContrasenaGeneral;

public class ContrasenaGeneralJpaController implements Serializable {

    private EntityManagerFactory emf = null;

    public ContrasenaGeneralJpaController() {
        emf = Persistence.createEntityManagerFactory("persisventas");
    }

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(ContrasenaGeneral contrasenaGeneral) {
        EntityManager em = getEntityManager();
        EntityTransaction et = em.getTransaction();
        try {
            et.begin();
            em.persist(contrasenaGeneral);
            et.commit();
        } catch (Exception ex) {
            if (et.isActive()) {
                et.rollback();
            }
            throw new RuntimeException("Error creating ContrasenaGeneral entity", ex);
        } finally {
            em.close();
        }
    }

    public void edit(ContrasenaGeneral contrasenaGeneral) {
        EntityManager em = getEntityManager();
        EntityTransaction et = em.getTransaction();
        try {
            et.begin();
            em.merge(contrasenaGeneral);
            et.commit();
        } catch (Exception ex) {
            if (et.isActive()) {
                et.rollback();
            }
            throw new RuntimeException("Error updating ContrasenaGeneral entity", ex);
        } finally {
            em.close();
        }
    }

    public void destroy(Long id) {
        EntityManager em = getEntityManager();
        EntityTransaction et = em.getTransaction();
        try {
            et.begin();
            ContrasenaGeneral contrasenaGeneral = em.find(ContrasenaGeneral.class, id);
            if (contrasenaGeneral != null) {
                em.remove(contrasenaGeneral);
            }
            et.commit();
        } catch (Exception ex) {
            if (et.isActive()) {
                et.rollback();
            }
            throw new RuntimeException("Error deleting ContrasenaGeneral entity", ex);
        } finally {
            em.close();
        }
    }

    public ContrasenaGeneral findContrasenaGeneral(Long id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(ContrasenaGeneral.class, id);
        } finally {
            em.close();
        }
    }

    public List<ContrasenaGeneral> findContrasenaGeneralEntities() {
        return findContrasenaGeneralEntities(true, -1, -1);
    }

    public List<ContrasenaGeneral> findContrasenaGeneralEntities(int maxResults, int firstResult) {
        return findContrasenaGeneralEntities(false, maxResults, firstResult);
    }

    private List<ContrasenaGeneral> findContrasenaGeneralEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT c FROM ContrasenaGeneral c";
            Query q = em.createQuery(jpql);
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }
}