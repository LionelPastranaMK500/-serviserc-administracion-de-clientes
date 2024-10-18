package persistance;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import modelo.Clavesol;

public class ClavesolJpaController {

    private EntityManagerFactory emf = null;

    public ClavesolJpaController() {
        emf = Persistence.createEntityManagerFactory("persisventas");
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
        } finally {
            em.close();
        }
    }

    public void edit(Clavesol usuario) throws Exception {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            usuario = em.merge(usuario);
            em.getTransaction().commit();
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
}
