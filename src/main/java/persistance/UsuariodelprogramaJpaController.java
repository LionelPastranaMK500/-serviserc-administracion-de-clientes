package persistance;

import javax.persistence.*;

import java.util.List;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import modelo.Usuariodelprograma;

public class UsuariodelprogramaJpaController {

    private EntityManagerFactory emf = null;

    public UsuariodelprogramaJpaController() {
        emf = Persistence.createEntityManagerFactory("persisventas");
    }

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    // Método para crear un nuevo usuario
    public void create(Usuariodelprograma usuario) throws Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            em.persist(usuario);
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (findUsuariodelprograma(usuario.getIdusuario()) != null) {
                throw new Exception("El usuario con id " + usuario.getIdusuario() + " ya existe.");
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    // Método para actualizar un usuario existente
    public void edit(Usuariodelprograma usuario) throws Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            usuario = em.merge(usuario);
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (findUsuariodelprograma(usuario.getIdusuario()) == null) {
                throw new Exception("El usuario con id " + usuario.getIdusuario() + " no existe.");
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    // Método para eliminar un usuario
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
                throw new Exception("El usuario con id " + id + " no existe.", enfe);
            }
            em.remove(usuario);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    // Método para encontrar un usuario por ID
    public Usuariodelprograma findUsuariodelprograma(Long id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Usuariodelprograma.class, id);
        } finally {
            em.close();
        }
    }

    // Método para obtener una lista de todos los usuarios
    public List<Usuariodelprograma> findUsuariodelprogramaEntities() {
        return findUsuariodelprogramaEntities(true, -1, -1);
    }

    public List<Usuariodelprograma> findUsuariodelprogramaEntities(int maxResults, int firstResult) {
        return findUsuariodelprogramaEntities(false, maxResults, firstResult);
    }

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

    // Método para contar el número de usuarios
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
