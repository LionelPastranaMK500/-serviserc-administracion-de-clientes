package persistance;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import modelo.Libro;

public class LibroJpaController {

    private EntityManagerFactory emf = null;

    public LibroJpaController() {
        emf = Persistence.createEntityManagerFactory("persisventas");
    }

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Libro libro) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(libro);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public void edit(Libro libro) throws Exception {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            libro = em.merge(libro);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public void destroy(Long id) throws Exception {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            Libro libro = em.find(Libro.class, id);
            if (libro != null) {
                em.remove(libro);
            }
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public Libro findLibro(Long id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Libro.class, id);
        } finally {
            em.close();
        }
    }
}
