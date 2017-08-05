/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.caos.libreriautil;

import java.util.List;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

/**
 * Clase de Utilidad para el manejo de conexiones a una base de datos usando
 * Hibernate. No lo he logrado hacer Funcionar!!
  *
 * @author CarlosArturo
 */
public class Conexion{

    private static SessionFactory sessionFactory;
    private static ServiceRegistry registry;
    /**
     * Crea un objeto SessionFactory de no existir o retorna el objeto estatico
     * actual.Crea un objeto SessionFactory a partir del archivo de
     * configuración <b>hibernate.cfg.xml</b> creado en la carpeta de resources
     * de este proyecto.
     *
     * @return Retorna un objeto de tipo SessionFactory para instanciar sesiones
     * con la base de datos.
     */
    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            try {
                Configuration configuration = new Configuration();
                configuration.configure();
                registry = new StandardServiceRegistryBuilder().applySettings(
                        configuration.getProperties()).build();
                sessionFactory = configuration.buildSessionFactory(registry);

            } catch (Exception e) {
                if (registry != null) {
                    StandardServiceRegistryBuilder.destroy(registry);
                }
            }
        }
        return sessionFactory;
    }

    /**
     *   
     * Crea un objeto SessionFactory de no existir o retorna el objeto estatico
     * actual.Crea un objeto SessionFactory a partir del archivo de
     * configuración <b>hibernate.cfg.xml</b> creado en la carpeta de resources
     * de este proyecto.
     *
     * @param <T> Clase esperada.
     * @param clase Clase anotada a registrar en la configuración.
     * @return
     */
    public static <T> SessionFactory getSessionFactory(Class<T> clase) {
        if (sessionFactory == null) {
            try {
                Configuration configuration = new Configuration();
                configuration.configure().addAnnotatedClass(clase);
                registry = new StandardServiceRegistryBuilder().applySettings(
                        configuration.getProperties()).build();
                sessionFactory = configuration.buildSessionFactory(registry);

            } catch (Exception e) {
                e.printStackTrace();
                if (registry != null) {
                    StandardServiceRegistryBuilder.destroy(registry);
                }
            }
        }
        return sessionFactory;
    }

    /**
     *
     * Crea un objeto SessionFactory de no existir o retorna el objeto estatico
     * actual.Crea un objeto SessionFactory a partir del archivo de
     * configuración <b>hibernate.cfg.xml</b> creado en la carpeta de resources
     * de este proyecto.
     *
     * @param <T> Clase esperada.
     * @param clases Listado de clases a registrar en la configuración.
     * @return
     */
    public static <T> SessionFactory getSessionFactory(List<Class<?>> clases) {
        if (sessionFactory == null) {
            try {
                //Genera la configuración y añade las clases anotadas
                Configuration configuration = new Configuration();
                for (Class<?> clase : clases) {
                    configuration.configure().addAnnotatedClass(clase);
                }
                registry = new StandardServiceRegistryBuilder().applySettings(
                        configuration.getProperties()).build();
                sessionFactory = configuration.buildSessionFactory(registry);

            } catch (Exception e) {
                e.printStackTrace();
                if (registry != null) {
                    StandardServiceRegistryBuilder.destroy(registry);
                }
            }
        }
        return sessionFactory;
    }

}
