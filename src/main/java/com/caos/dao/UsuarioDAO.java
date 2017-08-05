/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.caos.dao;

import com.caos.entidades.Usuario;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

/**
 * CLASE CON LOS METODOS CRUD A UN USUARIO
 *
 * @author CarlosArturo
 */
public class UsuarioDAO {

    private static final Logger LOG = Logger.getLogger(UsuarioDAO.class.getName());

    private Session session;
    private Transaction trns;

    public UsuarioDAO(Session session, Transaction trns) {
        this.session = session;
        this.trns = trns;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public Transaction getTrns() {
        return trns;
    }

    public void setTrns(Transaction trns) {
        this.trns = trns;
    }

    /**
     * CREA UN USUARIO NUEVO (NO HACE COMMIT)
     *
     * @param user Usuario a crear
     */
    public void crearUsuario(Usuario user) {
        try {
            session.save(user);
        } catch (RuntimeException e) {
            if (trns != null) {
                trns.rollback();
            }
            LOG.log(Level.SEVERE, null, e);
            throw new HibernateException(e);
        } 
    }

    /**
     * ELIMINA UN USUARIO A PARTIR DE SU NOMBRE DE USUARIO (NO HACE COMMIT)
     *
     * @param username Nombre de usuario
     */
    public void eliminarUsuario(String username) {
        try {
            Usuario user = (Usuario) session.load(Usuario.class, username);
            session.delete(user);
        } catch (RuntimeException e) {
            if (trns != null) {
                trns.rollback();
            }
            LOG.log(Level.SEVERE, null, e);
            throw new HibernateException(e);
        }
    }

    /**
     * ACTUALIZA A UN USUARIO (NO HACE COMMIT)
     *
     * @param user Usuario a actualizar
     */
    public void actualizarUsuario(Usuario user) {
        try {
            session.update(user);
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
        try  {
            users = session.createQuery("from Usuario").list(); //Usa nombre de clase en vez de tabla
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
        try {
            String queryString = "from Usuario where username = :id"; //Usa nombre de clase en vez de tabla
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
