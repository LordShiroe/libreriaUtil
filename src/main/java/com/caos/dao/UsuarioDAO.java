/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.caos.dao;

import com.caos.entidades.Usuario;
import com.caos.libreriautil.Conexion;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

/**
 * CLASE CON LOS METODOS CRUD A UN USUARIO
 *
 * @author CarlosArturo
 */
public class UsuarioDAO {

    private static final Logger LOG = Logger.getLogger(UsuarioDAO.class.getName());

    /**
     * CREA UN USUARIO NUEVO
     *
     * @param user Usuario a crear
     */
    public void crearUsuario(Usuario user) {
        SessionFactory sessionFactory = Conexion.getSessionFactory(Usuario.class);
        Transaction trns = null;
        try (Session session = sessionFactory.openSession()) {
            trns = session.beginTransaction();
            session.save(user);
            session.getTransaction().commit();
        } catch (RuntimeException e) {
            if (trns != null) {
                trns.rollback();
            }
            LOG.log(Level.SEVERE, null, e);
            throw new HibernateException(e);
        } 
    }

    /**
     * ELIMINA UN USUARIO A PARTIR DE SU NOMBRE DE USUARIO
     *
     * @param username Nombre de usuario
     */
    public void eliminarUsuario(String username) {
        SessionFactory sessionFactory = Conexion.getSessionFactory(Usuario.class);
        Transaction trns = null;
        try (Session session = sessionFactory.openSession()) {
            trns = session.beginTransaction();
            Usuario user = (Usuario) session.load(Usuario.class, new Integer(username));
            session.delete(user);
            session.getTransaction().commit();
        } catch (RuntimeException e) {
            if (trns != null) {
                trns.rollback();
            }
            LOG.log(Level.SEVERE, null, e);
            throw new HibernateException(e);
        }
    }

    /**
     * ACTUALIZA A UN USUARIO
     *
     * @param user Usuario a actualizar
     */
    public void actualizarUsuario(Usuario user) {
        SessionFactory sessionFactory = Conexion.getSessionFactory(Usuario.class);
        Transaction trns = null;
        try (Session session = sessionFactory.openSession()) {
            trns = session.beginTransaction();
            session.update(user);
            session.getTransaction().commit();
        } catch (RuntimeException e) {
            if (trns != null) {
                trns.rollback();
            }
            LOG.log(Level.SEVERE, null, e);
            throw new HibernateException(e);
        }
    }

    /**
     * LISTA TODOS LOS USUARIO
     *
     * @return Lista con todos los Usuarios
     */
    public List<Usuario> getUsuarios() {
        List<Usuario> users = new ArrayList<>();
        SessionFactory sessionFactory = Conexion.getSessionFactory(Usuario.class);
        Transaction trns = null;
        try (Session session = sessionFactory.openSession()) {
            trns = session.beginTransaction();
            users = session.createQuery("from Usuarios").list();
        } catch (RuntimeException e) {
            if (trns != null) {
                trns.rollback();
            }
            LOG.log(Level.SEVERE, null, e);
            throw new HibernateException(e);
        }
        return users;
    }

    /**
     * Lista un usuario a partir de su nombre de usuario.
     *
     * @param username Nombre de usuario
     * @return Usuario listado.
     */
    public Usuario getUsuarioByUsername(String username) {
        Usuario user = null;
        Transaction trns = null;
        SessionFactory sessionFactory = Conexion.getSessionFactory(Usuario.class);
        try (Session session = sessionFactory.openSession()) {
            trns = session.beginTransaction();
            String queryString = "from Usuario where username = :id";
            Query query = session.createQuery(queryString);
            query.setParameter("id", username);
            user = (Usuario) query.uniqueResult();
        } catch (RuntimeException e) {
            LOG.log(Level.SEVERE, null, e);
            throw new HibernateException(e);
        }
        return user;
    }
}
