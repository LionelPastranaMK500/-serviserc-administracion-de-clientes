package persistance;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import modelo.Cobranza;

public class CobranzaJpaController {

    private EntityManagerFactory emf = null;

    public CobranzaJpaController() {
        emf = Persistence.createEntityManagerFactory("persisventas");
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
        } finally {
            em.close();
        }
    }

    public void edit(Cobranza cobranza) throws Exception {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            cobranza = em.merge(cobranza);
            em.getTransaction().commit();
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
}
