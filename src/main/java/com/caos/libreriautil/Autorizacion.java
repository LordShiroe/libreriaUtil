
package com.caos.libreriautil;

import com.caos.dao.UsuarioDAO;
import com.caos.entidades.Hijo;
import com.caos.entidades.Roles;
import com.caos.entidades.Usuario;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

/**
 *
 * @author CarlosArturo
 */
public class Autorizacion {

    private static final Logger LOG = Logger.getLogger(Autorizacion.class.getName());

    private static final String AUTHORIZATION_PROPERTY = "Authorization";
    private static final String AUTHENTICATION_SCHEME = "Basic";

    /**
     * Verifica los permisos de acceso basado en la petición y el grupo de
     * roles.
     *
     * @param cookies Listado de cookies
     * @param rolesSet Listado de roles que tienen acceso al modulo.
     * @return <b>true</b> si es permitido el acceso, <b>false</b> de lo
     * contrario.
     */
    public boolean verificarPermisos(Cookie[] cookies, Set<String> rolesSet) {
        String authorization = null;
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("LOGIN")) {
                authorization = cookie.getValue();
            }
        }
//Get request headers

        //If no authorization information present; block access
        if (authorization == null || authorization.isEmpty()) {
            return false;
        }

        //Get encoded username and password
        final String encodedUserPassword = authorization.replaceFirst(AUTHENTICATION_SCHEME + " ", "");
        System.out.println(encodedUserPassword);
        //Decode username and password
        String usernameAndPassword = new String(Base64.getDecoder().decode(encodedUserPassword.getBytes()));
        //Split username and password tokens
        final StringTokenizer tokenizer = new StringTokenizer(usernameAndPassword, ":");
        final String username = tokenizer.nextToken();
        final String password = tokenizer.nextToken();
        boolean valido = false;
        SessionFactory sessionFactory = Conexion.getSessionFactory(Usuario.class);
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            UsuarioDAO dao = new UsuarioDAO(session, transaction);
            Usuario usuario = dao.getUsuarioByUsername(username.toLowerCase());
            System.out.println("Usuario BASIC: " + usuario);
            //Valida que el usuario exista y que su contraseña y rol sea valido para el módulo actual.
            if (usuario != null) {
                if (usuario.getPassword().equals(password)) {
                    if (rolesSet.contains("ALL")) { //Rol ALL indica
                        valido = true;
                    } else if (rolesSet.contains(usuario.getRol())) {
                        valido = true;
                    }
                }
            }
            transaction.commit();
            session.close();
            return valido;
        } catch (HibernateException | NullPointerException ex) {
            LOG.log(Level.SEVERE, null, ex);
            return false;
        }
    }

    /**
     * Verifica los permisos de acceso basado en la petición y el grupo de
     * roles.
     *
     * @param request Petición http del cliente. Debe llevar un encabezado con
     * la autorizacion en BASIC.
     * @param rolesSet Listado de roles que tienen acceso al modulo.
     * @return <b>true</b> si es permitido el acceso, <b>false</b> de lo
     * contrario.
     */
    public boolean verificarPermisos(HttpServletRequest request, Set<String> rolesSet) {
        //Get request headers
        String authorization = request.getHeader(AUTHORIZATION_PROPERTY);

        //If no authorization information present; block access
        if (authorization == null || authorization.isEmpty()) {
            return false;
        }

        //Get encoded username and password
        final String encodedUserPassword = authorization.replaceFirst(AUTHENTICATION_SCHEME + " ", "");
        System.out.println(encodedUserPassword);
        //Decode username and password
        String usernameAndPassword = new String(Base64.getDecoder().decode(encodedUserPassword.getBytes()));
        //Split username and password tokens
        final StringTokenizer tokenizer = new StringTokenizer(usernameAndPassword, ":");
        final String username = tokenizer.nextToken();
        final String password = tokenizer.nextToken();
        boolean valido = false;
        SessionFactory sessionFactory = Conexion.getSessionFactory(Usuario.class);
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            UsuarioDAO dao = new UsuarioDAO(session, transaction);
            Usuario usuario = dao.getUsuarioByUsername(username.toLowerCase());
            System.out.println("Usuario BASIC: " + usuario);
            //Valida que el usuario exista y que su contraseña y rol sea valido para el módulo actual.
            if (usuario != null) {
                if (usuario.getPassword().equals(password)) {
                    if (rolesSet.contains("ALL")) { //Rol ALL indica
                        valido = true;
                    } else if (rolesSet.contains(usuario.getRol())) {
                        valido = true;
                    }
                } 
            } 
            transaction.commit();
            session.close();
            return valido;
        } catch (HibernateException | NullPointerException ex) {
            LOG.log(Level.SEVERE, null, ex);
            return false;
        }
    }

    /**
     * Verifica los permisos de acceso basado en la petición y id del modulo
     *
     * @param request Petición http del cliente. Debe llevar un encabezado con
     * la autorizacion en BASIC.
     * @param idHijo ID del modulo actual.
     * @return <b>true</b> si es permitido el acceso, <b>false</b> de lo
     * contrario.
     */
    public boolean verificarPermisos(HttpServletRequest request, Integer idHijo) {
        //Get request headers
        String authorization = request.getHeader(AUTHORIZATION_PROPERTY);

        //If no authorization information present; block access
        if (authorization == null || authorization.isEmpty()) {
            return false;
        }

        //Get encoded username and password
        final String encodedUserPassword = authorization.replaceFirst(AUTHENTICATION_SCHEME + " ", "");

        //Decode username and password
        String usernameAndPassword = new String(Base64.getDecoder().decode(encodedUserPassword.getBytes()));
        //Split username and password tokens
        final StringTokenizer tokenizer = new StringTokenizer(usernameAndPassword, ":");
        final String username = tokenizer.nextToken();
        final String password = tokenizer.nextToken();
        boolean valido = false;
        List<Class<?>> classList = new ArrayList<>();
        classList.add(Usuario.class);
        classList.add(Hijo.class);
        classList.add(Roles.class);
        SessionFactory sessionFactory = Conexion.getSessionFactory(classList);
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            UsuarioDAO dao = new UsuarioDAO(session, transaction);
            Usuario usuario = dao.getUsuarioByUsername(username.toLowerCase());
            //Valida que el usuario exista y que su contraseña y rol sea valido para el módulo actual.
            if (usuario != null) {
                if (usuario.getPassword().equals(password)) {
                    Roles rol = (Roles) session.getNamedQuery("Roles.findByNombre").setParameter("nombre", usuario.getRol()).uniqueResult();
                    Hijo hijo = (Hijo) session.getNamedQuery("Hijo.findByIdHijo").setParameter("idHijo", idHijo.shortValue()).uniqueResult();
                    //Valida que haya un hijo y un rol
                    if (hijo != null && rol != null) {
                        //Valida que exista el rol solicitado para ese hijo.
                        if (hijo.getRolesCollection().contains(rol)) {
                            valido = true;
                        }
                    }
                }
            }
            transaction.commit();
            session.close();
            return valido;
        } catch (HibernateException | NullPointerException ex) {
            LOG.log(Level.SEVERE, null, ex);
            return false;
        }
    }

    /**
     * RETORNA EL USUARIO DE LA PETICIÓN
     *
     * @param request Petición http del cliente. Debe llevar un encabezado con
     * la autorizacion en BASIC.
     * @return Username
     */
    public String obtenerUsername(HttpServletRequest request) {
        //Get request headers
        String authorization = request.getHeader(AUTHORIZATION_PROPERTY);

        //If no authorization information present; block access
        if (authorization == null || authorization.isEmpty()) {
            return null;
        }

        //Get encoded username and password
        final String encodedUserPassword = authorization.replaceFirst(AUTHENTICATION_SCHEME + " ", "");

        //Decode username and password
        String usernameAndPassword = new String(Base64.getDecoder().decode(encodedUserPassword.getBytes()));
        //Split username and password tokens
        final StringTokenizer tokenizer = new StringTokenizer(usernameAndPassword, ":");
        final String username = tokenizer.nextToken();
        return username;
    }

    /**
     * RETORNA LA CONTRASEÑA DE LA PETICIÓN
     *
     * @param request Petición http del cliente. Debe llevar un encabezado con
     * la autorizacion en BASIC.
     * @return Password
     */
    public String obtenerPassword(HttpServletRequest request) {
        //Get request headers
        String authorization = request.getHeader(AUTHORIZATION_PROPERTY);

        //If no authorization information present; block access
        if (authorization == null || authorization.isEmpty()) {
            return null;
        }

        //Get encoded username and password
        final String encodedUserPassword = authorization.replaceFirst(AUTHENTICATION_SCHEME + " ", "");

        //Decode username and password
        String usernameAndPassword = new String(Base64.getDecoder().decode(encodedUserPassword.getBytes()));
        //Split username and password tokens
        final StringTokenizer tokenizer = new StringTokenizer(usernameAndPassword, ":");
        final String username = tokenizer.nextToken();
        final String password = tokenizer.nextToken();
        return password;
    }

    /**
     * RETORNA UN MAPA CON LA RESPUESTA DE ACCESO DENEGADO.
     *
     * @return Mapa con respuesta de acceso denegado
     */
    public Map<String, Object> accesoDenegado() {
        Map<String, Object> salida = new HashMap();
        salida.put("mensaje", "ACCESO DENEGADO");
        salida.put("respuesta", "No tiene suficientes privilegios para acceder a este contenido");
        salida.put("data", null);
        salida.put("cantidad", "0");
        salida.put("estado", "0");
        return salida;
    }
}
