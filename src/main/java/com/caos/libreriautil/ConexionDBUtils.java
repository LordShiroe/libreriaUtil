package com.caos.libreriautil;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.Closeable;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;

/**
 * Conexion a la Base de datos Usando DBUtils
 *
 * @author CarlosArturo
 */
public class ConexionDBUtils implements Closeable {

    private static final Logger LOG = Logger.getLogger(ConexionDBUtils.class.getName());

    private Gson gson;
    private String url;
    private Connection oracle;
    private QueryRunner query;

    @Override
    public void close() throws IOException {
        try {
            if (!this.oracle.isClosed()) {
                rollback();
                cerrar();
            }
        } catch (SQLException ex) {
            Logger.getLogger(ConexionDBUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
        DbUtils.closeQuietly(this.oracle);
    }

    /**
     * ESTABLECE UNA CONEXION CON LA BASE DE DATOS SIN AUTO COMMIT USANDO LAS
     * CREDENCIALES POR DEFECTO DE LA LIBRERIA
     *
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public ConexionDBUtils() throws ClassNotFoundException, SQLException {
        this.url = "jdbc:oracle:thin:@127.0.0.1:1521:XE";
        this.gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd")
                .setPrettyPrinting()
                .serializeNulls()
                .create();
        Class.forName("oracle.jdbc.OracleDriver");
        this.oracle = DriverManager.getConnection(this.url, "LAN01", "3rg0_S4m");
        this.oracle.setAutoCommit(false);
        this.query = new QueryRunner();
    }

    /**
     *
     * @param username
     * @param pwd
     *
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public ConexionDBUtils(String username, String pwd) throws ClassNotFoundException, SQLException {
        //this.url = "jdbc:oracle:thin:@127.0.0.1:1521:XE";
        this.gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd")
                .setPrettyPrinting()
                .serializeNulls()
                .create();
        Class.forName("oracle.jdbc.OracleDriver");
        this.oracle = DriverManager.getConnection(this.url, username, pwd);
        this.oracle.setAutoCommit(false);
        this.query = new QueryRunner();
    }

    /**
     * HACE COMMIT DE LOS CAMBIOS REALIZADOS.
     *
     * @throws SQLException
     */
    public void commit() throws SQLException {
        getOracle().commit();
    }

    /**
     * REVERSA LOS CAMBIOS REALIZADOS POR LA CONEXION.
     *
     * @throws SQLException
     */
    public void rollback() throws SQLException {
        getOracle().rollback();
    }

    /**
     * CIERRA LA CONEXION A LA BASE DE DATOS.
     */
    public void cerrar() {
        try {
            DbUtils.closeQuietly(getOracle());
        } catch (NullPointerException ex) {
            Logger.getLogger(ConexionDBUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
    }



    /**
     * GENERA LA CONSULTA DE INSERTAR CON LOS DATOS SUMINISTRADOS. este metodo
     * recibe el nombre del usuario para registrarlo en la base de datos, este
     * identificador puede ser el nombre de la aplicacion. es necesario
     * suministrar el nombre de la tabala la lista de campos y la lista de datos
     * en el mismo orden para los dos objetos y finalmente recibe un parametro
     * <b>true</b> o <b>false</b> para indicar si se debe hacer <b>COMMIT</b> y
     * <b>CERRAR</b> la conexion al terminar el llamado
     *
     * @param db
     * @param username nombre de usuario o identifcador para la auditoria
     * @param tabla nombre de la tabla afectada
     * @param campos nombres de los campos a insertar
     * @param datos valores a insertar en el orden de los campos
     * @param terminar indica si debe hacer commit y cerrar la conexion
     *
     * @return cantidad de registros afectados o el mensaje de error
     *
     * @throws java.sql.SQLException
     */
    public String insertar(ConexionDBUtils db, String username, String tabla, String[] campos, Object[] datos, boolean terminar) throws SQLException {
        db.prepare(db.getOracle(), username);
        String consulta = "INSERT INTO " + tabla + " (";
        int i = 0;
        for (String campo : campos) {
            if (i == campos.length - 1) {
                consulta = consulta + "" + campo + ") VALUES (";
            } else {
                consulta = consulta + "" + campo + ", ";
            }
            i++;
        }
        i = 0;
        for (Object campo : datos) {
            if (!campo.toString().toLowerCase().equals("sysdate") && !campo.toString().toLowerCase().equals("user") && !campo.toString().toLowerCase().equals("null")) {
                campo = "'" + campo + "'";
            }
            if (i == campos.length - 1) {
                consulta = consulta + campo + ")";
            } else {
                consulta = consulta + campo + ", ";
            }
            i++;
        }
        Integer afectados = db.getQuery().update(db.getOracle(), consulta);
        if (terminar) {
            db.commit();
            db.cerrar();
        }
        return afectados.toString();

    }

    /**
     * OBTIENE EL TEXTO DE LA FECHA DEL DIA ACTUAL EN FORMATO DD/MM/YYYY
     *
     * @return
     */
    public String getHoy() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    /**
     * EJECUTA UNA SENTENCIA DELETE DE LA TABLA INDICADO CON LOS PARAMETROS
     * SUMINISTRADOS. este metodo recibe el nombre del usuario que ejecuta la
     * tarea, (este es el identificador de la auditoria) el nombre de la taba
     * afectada y los parametros del whee para ejcutar el llamado. y finalmente
     * recibe un parametro <b>true</b> o <b>false</b> para indicar si se debe
     * hacer <b>COMMIT</b> y <b>CERRAR</b> la conexion al terminar el llamado
     *
     * @param db
     * @param username nombre de usuario o identifcador para la auditoria
     * @param tabla nombre de la tabla afectada
     * @param where parametros para identificar ls registros a borrar
     * @param terminar indica si debe hacer commit y cerrar la conexion
     *
     * @return cantidad de registros afectados o el mensaje de error
     *
     * @throws java.sql.SQLException
     */
    public String borrar(ConexionDBUtils db, String username, String tabla, Map<String, String> where, boolean terminar) throws SQLException {
        db.prepare(db.getOracle(), username);
        String sql = "DELETE FROM " + tabla + " WHERE ";
        for (Map.Entry<String, String> datos : where.entrySet()) {
            String key = datos.getKey();
            String value = datos.getValue();
            sql = sql + key + "='" + value + "' ";
        }
        Integer resultado;

        resultado = db.getQuery().update(db.getOracle(), sql);
        String salida = getGson().toJson(resultado);
        if (terminar) {
            db.commit();
            db.cerrar();
        }
        return salida;
    }

    /**
     * REALIZA UN UPDATE A LA TABLA INDICADA CON LOS DATOS Y PARAMETROS. realiza
     * el update correspondiente recibiendo dos listas de datos una con los
     * datos a modificar y otra indicando los parametros de busqueda para el
     * where. este metodo recibe el nombre del usuario u aplicacion que quedara
     * regitrado al momento de ejecutar este update y finalmente recibe un
     * parametro <b>true</b> o <b>false</b> para indicar si se debe hacer
     * <b>COMMIT</b> y
     * <b>CERRAR</b> la conexion al terminar el llamado
     *
     * @param db
     * @param username nombre de usuario o identifcador para la auditoria
     * @param tabla nombre de la tabla afectada
     * @param campos diccionario de datos que tiene el nombre del campo y el
     * valor nuevo de la forma set "campo=dato"
     * @param where diccionario de datos para la busqueda de los campos a
     * modificar de la forma where "campo=dato"
     * @param terminar indica si debe hacer commit y cerrar la conexion
     *
     * @return cantidad de registros afectados o el mensaje de error
     *
     * @throws java.sql.SQLException
     */
    public String actualizar(ConexionDBUtils db, String username, String tabla, Map<String, String> campos, Map<String, String> where, boolean terminar) throws SQLException {
        db.prepare(db.getOracle(), username);
        String consulta = "UPDATE " + tabla + " SET ";
        int i = 0;
        for (Map.Entry<String, String> campo : campos.entrySet()) {
            String ncampo = campo.getKey();
            String vcampo = campo.getValue();
            if (!vcampo.toLowerCase().equals("sysdate") && !vcampo.toLowerCase().equals("user") && !vcampo.toLowerCase().equals("null")) {
                vcampo = "'" + vcampo + "'";
            }
            if (!vcampo.isEmpty()) {
                if (i == campos.size() - 1) {
                    consulta = consulta + "" + ncampo + "=" + vcampo + " WHERE ";
                } else {
                    consulta = consulta + "" + ncampo + "=" + vcampo + ", ";
                }
            }
            i++;
        }
        i = 0;
        for (Map.Entry<String, String> campo : where.entrySet()) {
            String ncampo = campo.getKey();
            String vcampo = campo.getValue();
            if (i == where.size() - 1) {
                consulta = consulta + "" + ncampo + "='" + vcampo + "'";
            } else {
                consulta = consulta + "" + ncampo + "='" + vcampo + "' AND ";
            }
            i++;
        }
        Integer afectados = db.getQuery().update(db.getOracle(), consulta);
        if (terminar) {
            db.commit();
            db.cerrar();
        }
        return afectados.toString();
    }

    /**
     * INSERTA EN CUALQUIER TABLA A PARTIR DE UN MAPA CON LOS PARAMETROS. LA
     * LLAVE ES EL NOMBRE DEL CAMPO Y EL VALOR ES EL DATO QUE SE REGISTRA.
     *
     * @param db Conexión a la base de datos.
     * @param username ID del usuario en oracle.
     * @param tabla Nombre de la tabla a la que se va a insertar.
     * @param parametros Mapa con los parametros a insertar donde la llave es el
     * <b>NOMBRE</b> del campo y el valor es el <b>DATO</b> a insertar.
     * @param terminar Indica si se realiza el commit luego de insertar o no.
     * @return Cantidad de filas afectadas.
     * @throws SQLException
     */
    public int insertar(ConexionDBUtils db, String username, String tabla, Map<String, String> parametros, boolean terminar) throws SQLException {
        db.setOracle(db.prepare(db.getOracle(), username));
        String consulta = "INSERT INTO " + tabla + "(";
        String nombres = "";
        String valores = "";
        int i = 0;
        for (Map.Entry<String, String> campo : parametros.entrySet()) {
            String ncampo = campo.getKey();
            String vcampo = campo.getValue();
            if (!vcampo.isEmpty()) {
                if (i == parametros.size() - 1) {
                    nombres = nombres + " " + ncampo.toUpperCase();
                    if (!vcampo.toLowerCase().equals("sysdate") && !vcampo.toLowerCase().equals("user") && !vcampo.toLowerCase().equals("null")) {
                        vcampo = "'" + vcampo + "'";
                    }
                    valores = valores + " " + vcampo;
                } else {
                    nombres = nombres + " " + ncampo + ",";
                    if (!vcampo.toLowerCase().equals("sysdate") && !vcampo.toLowerCase().equals("user") && !vcampo.toLowerCase().equals("null")) {
                        vcampo = "'" + vcampo + "'";
                    }
                    valores = valores + " " + vcampo + ",";
                }
            } else if (i == parametros.size() - 1) {
                nombres = nombres + " " + ncampo.toUpperCase();
                valores = valores + " " + "null";
            } else {
                nombres = nombres + " " + ncampo + ",";
                valores = valores + " " + "null" + ",";
            }
            i++;
        }
        consulta = consulta + nombres + ") VALUES (" + valores + ")";
        Integer afectados = db.getQuery().update(db.getOracle(), consulta);
        if (terminar) {
            db.commit();
            db.cerrar();
        }
        return afectados;
    }

    /**
     * REALIZA UNA CONSULTA DE UPDATE,DELETE,INSERT A LA BASE DE DATOS
     * SUMINISTRANDO LA CONSULTA SIN PARAMETROS YA SEA QUE ESTEN INCLUIDOS EN EL
     * QUERY O NO LOS REQUIERA
     *
     * @param db Conexion
     * @param username Usuario que realiza la accion
     * @param sql consulta a la base de datos
     * @param terminar indica si debe cerrar y hacer commit de la consulta
     *
     * @return
     *
     * @throws java.sql.SQLException
     */
    public String actualizar(ConexionDBUtils db, String username, String sql, boolean terminar) throws SQLException {
        Integer afectados;
        afectados = db.getQuery().update(db.prepare(db.getOracle(), username), sql);
        if (terminar) {
            db.commit();
            db.cerrar();
        }
        return afectados.toString();

    }

    /**
     * REALIZA UNA CONSULTA DE UPDATE,DELETE,INSERT A LA BASE DE DATOS
     * SUMINISTRANDO LA CONSULTA Y LOS PARAMETROS
     *
     * @param db Conexion
     * @param username Usuario que realiza la accion
     * @param sql consulta a la base de datos
     * @param datos parametros de la consulta
     * @param terminar indica si debe cerrar y hacer commit de la consulta
     *
     * @return
     *
     * @throws java.sql.SQLException
     */
    public String actualizar(ConexionDBUtils db, String username, String sql, Object[] datos, boolean terminar) throws SQLException {
        Integer afectados;
        afectados = db.getQuery().update(db.prepare(db.getOracle(), username), sql, datos);
        if (terminar) {
            db.commit();
            db.cerrar();
        }
        return afectados.toString();
    }

    /**
     * REALIZA UNAS CONSULTAS DE INSERTAR, ACTUALIZAR Y ELIMINAR EN BATCH.
     *
     * @param db Conexión a la base de datos
     * @param username Usuario que reliza la acción
     * @param sql Consulta en batch a ejecutar.
     * @param datos Matriz con los datos.
     * @return
     * @throws SQLException
     */
    public Integer actualizarBatch(ConexionDBUtils db, String username, String sql, Object[][] datos) throws SQLException {
        int[] batch = db.getQuery().batch(db.prepare(db.getOracle(), username), sql, datos);
        return batch.length;
    }

    /**
     * REALIZA UNA CONSULTA DE SELECT SIN PARAMETROS. este metodo recibe la
     * consulta para un select y la ejecuta para luego retornar los resultados.
     * este metodo adicionalmente recibe un parametro <b>true</b> o <b>false</b>
     * para indicar si se debe cerrar la conexion al terminar el llamado indicar
     *
     * @param db
     * @param sql sentencia SQL del select a ejecutar
     * @param terminar indica si debe hacer commit y cerrar la conexion
     *
     * @return retorna la lista de los registros encontrados o un mensaje de
     * error en un campo llamado mensaje en la posicion 0 de la lista
     *
     * @throws java.sql.SQLException
     */
    public List<Map<String, Object>> select(ConexionDBUtils db, String sql, boolean terminar) throws SQLException {
        List<Map<String, Object>> afectados;
        afectados = db.getQuery().query(db.getOracle(), sql, new MapListHandler());
        if (terminar) {
            db.cerrar();
        }
        return afectados;
    }

    /**
     * REALIZA UNA CONSULTA DE SELECT CON PARAMETROS. este metodo recibe la
     * consulta para un select y los parametros de este mismo. este metodo
     * adicionalmente recibe un parametro <b>true</b> o <b>false</b> para
     * indicar si se debe cerrar la conexion al terminar el llamado indicar
     *
     * @param db
     * @param sql sentencia SQL del select a ejecutar
     * @param datos parametros de la sentencia a ejecutar
     * @param terminar indica si debe hacer commit y cerrar la conexion
     *
     * @return retorna la lista de los registros encontrados o un mensaje de
     * error en un campo llamado mensaje en la posicion 0 de la lista
     *
     * @throws java.sql.SQLException
     */
    public List<Map<String, Object>> select(ConexionDBUtils db, String sql, Object[] datos, boolean terminar) throws SQLException {
        List<Map<String, Object>> afectados;
        afectados = db.getQuery().query(db.getOracle(), sql, new MapListHandler(), datos);
        if (terminar) {
            db.cerrar();
        }
        return afectados;
    }

    /**
     * REALIZA UNA CONSULTA DE SELECT CON PARAMETROS. este metodo recibe la
     * consulta para un select y la ejecuta para luego retornar los resultados
     * en el JavaBean dado. Este metodo adicionalmente recibe un parametro
     * <b>true</b> o <b>false</b>
     * para indicar si se debe cerrar la conexion al terminar el llamado indicar
     *
     * @param <T>
     * @param db Conexión a la base de datos
     * @param sql Consulta a ejecutar
     * @param datos Parametros de la consulta
     * @param clase Clase del JavaBean
     * @param terminar indica si debe hacer commit y cerrar la conexion
     * @return Lista de la clase del JavaBean pasado
     * @throws SQLException
     */
    public <T> List<T> select(ConexionDBUtils db, String sql, Object[] datos, Class<T> clase, boolean terminar) throws SQLException {
        ResultSetHandler<List<T>> beanHandler = new BeanListHandler<>(clase);
        List<T> afectados;
        afectados = db.getQuery().query(db.getOracle(), sql, beanHandler, datos);
        if (terminar) {
            db.cerrar();
        }
        return afectados;
    }

    /**
     * REALIZA UNA CONSULTA DE SELECT SIN PARAMETROS. este metodo recibe la
     * consulta para un select y la ejecuta para luego retornar los resultados
     * en el JavaBean dado. Este metodo adicionalmente recibe un parametro
     * <b>true</b> o <b>false</b>
     * para indicar si se debe cerrar la conexion al terminar el llamado indicar
     *
     * @param <T>
     * @param db Conexión a la base de datos
     * @param sql Consulta a ejecutar
     * @param clase Clase del JavaBean
     * @param terminar indica si debe hacer commit y cerrar la conexion
     * @return Lista de la clase del JavaBean pasado
     * @throws SQLException
     */
    public <T> List<T> select(ConexionDBUtils db, String sql, Class<T> clase, boolean terminar) throws SQLException {
        ResultSetHandler<List<T>> beanHandler = new BeanListHandler<>(clase);
        List<T> afectados;
        afectados = db.getQuery().query(db.getOracle(), sql, beanHandler);
        if (terminar) {
            db.cerrar();
        }
        return afectados;
    }

    /**
     * RECIBE UNA CONSULTA PARA UN SOLO DATO NUMERICO Y RESPONDE CON EL DATO EN
     * BIGDECIMAL
     *
     * @param db Conexion a la bd
     * @param sql consulta
     * @param datos parametros
     * @return
     * @throws SQLException
     */
    public BigDecimal selectOneNumber(ConexionDBUtils db, String sql, Object[] datos) throws SQLException {
        List<Map<String, Object>> afectados = db.select(db, sql, datos, false);
        BigDecimal respuesta = null;
        if (!afectados.isEmpty()) {
            if (afectados.size() == 1 && afectados.get(0).size() == 1) {
                for (Map.Entry<String, Object> campo : afectados.get(0).entrySet()) {
                    if (campo.getValue() != null) {
                        respuesta = new BigDecimal(campo.getValue().toString());
                    } else {
                        respuesta = null;
                    }
                }
                return respuesta;
            } else {
                return respuesta;
            }
        } else {
            return respuesta;
        }
    }

    /**
     * RECIBE UNA CONSULTA PARA UN SOLO DATO Y RESPONDE CON EL DATO EN OBJETO.
     *
     * @param db Conexion a la bd
     * @param sql consulta
     * @param datos parametros
     * @return
     * @throws SQLException
     */
    public Object selectOneObject(ConexionDBUtils db, String sql, Object[] datos) throws SQLException {
        List<Map<String, Object>> afectados = db.select(db, sql, datos, false);
        Object respuesta = null;
        if (!afectados.isEmpty()) {
            if (afectados.size() == 1 && afectados.get(0).size() == 1) {
                for (Map.Entry<String, Object> campo : afectados.get(0).entrySet()) {
                    respuesta = campo.getValue();
                }
                return respuesta;
            } else {
                return respuesta;
            }
        } else {
            return respuesta;
        }
    }

    /**
     * RECIBE UNA CONSULTA PARA UN SOLO DATO Y RESPONDE CON EL DATO EN STRING.
     *
     * @param db Conexion a la bd
     * @param sql consulta
     * @param datos parametros
     * @return
     * @throws SQLException
     */
    public String selectOneString(ConexionDBUtils db, String sql, Object[] datos) throws SQLException {
        List<Map<String, Object>> afectados = db.select(db, sql, datos, false);
        String respuesta = null;
        if (!afectados.isEmpty()) {
            if (afectados.size() == 1 && afectados.get(0).size() == 1) {
                for (Map.Entry<String, Object> campo : afectados.get(0).entrySet()) {
                    if (campo.getValue() != null) {
                        respuesta = campo.getValue().toString();
                    }
                }
                return respuesta;
            } else {
                return respuesta;
            }
        } else {
            return respuesta;
        }
    }

    /**
     * LISTA LOS CAMPOS SELECCIONADOS DE LA TABLA DADA Y CON LAS CONDICIONES DEL
     * WHERE.
     *
     * @param db Conexión a la base de datos.
     * @param tabla Nombre de la tabla a buscar.
     * @param datos Campos a mostrar de la tabla
     * @param where Condición a buscar
     * @return Filas de resultado.
     * @throws SQLException
     */
    public List<Map<String, Object>> selectTabla(ConexionDBUtils db, String tabla, String[] datos, Map<String, String> where) throws SQLException {
        List<Map<String, Object>> afectados;
        String sql = "SELECT ";
        for (String dato : datos) {
            sql = sql + dato + ", ";
        }
        sql = sql.substring(0, sql.length() - 2);
        sql = sql + "\n FROM " + tabla + "\n WHERE ";
        int i = 0;
        for (Map.Entry<String, String> campo : where.entrySet()) {
            if (i == where.size() - 1) {
                sql = sql + campo.getKey() + " = " + campo.getValue();
            } else {
                sql = sql + campo.getKey() + " = " + campo.getValue() + " AND \n";
            }
            i++;
        }
        afectados = db.getQuery().query(db.getOracle(), sql, new MapListHandler());
        return afectados;
    }

    /**
     * LISTA LOS CAMPOS SELECCIONADOS DE LA TABLA DADA Y CON LAS CONDICIONES DEL
     * WHERE.
     *
     * @param db Conexión a la base de datos.
     * @param tabla Nombre de la tabla a buscar.
     * @param datos Campos a mostrar de la tabla
     * @param where Condición a buscar
     * @param orderBy Orden para la consulta.
     * @return Filas de resultado.
     * @throws SQLException
     */
    public List<Map<String, Object>> selectTabla(ConexionDBUtils db, String tabla, String[] datos, Map<String, String> where, Map<String, String> orderBy) throws SQLException {
        List<Map<String, Object>> afectados;
        String sql = "SELECT ";
        for (String dato : datos) {
            sql = sql + dato + ", ";
        }
        sql = sql.substring(0, sql.length() - 2);
        sql = sql + "\n FROM " + tabla + "\n WHERE ";
        int i = 0;
        for (Map.Entry<String, String> campo : where.entrySet()) {
            if (i == where.size() - 1) {
                sql = sql + campo.getKey() + " = " + campo.getValue();
            } else {
                sql = sql + campo.getKey() + " = " + campo.getValue() + " AND \n";
            }
            i++;
        }
        sql = sql + "\n ORDER BY ";
        i = 0;
        for (Map.Entry<String, String> campo : orderBy.entrySet()) {
            if (i == orderBy.size() - 1) {
                sql = sql + campo.getKey() + " " + campo.getValue();
            } else {
                sql = sql + campo.getKey() + " " + campo.getValue() + ",";
            }
        }
        afectados = db.getQuery().query(db.getOracle(), sql, new MapListHandler());
        return afectados;
    }

    public Gson getGson() {
        return gson;
    }

    public void setGson(Gson gson) {
        this.gson = gson;
    }

    public Connection getOracle() {
        return oracle;
    }

    public void setOracle(Connection oracle) {
        this.oracle = oracle;
    }

    public QueryRunner getQuery() {
        return query;
    }

    public void setQuery(QueryRunner query) {
        this.query = query;
    }

    /**
     * ASIGNA LA IDENTIFICACION DEL USUARIO ACTUAL PARA LA BASE DE DATOS.
     *
     * @param conn conexion establecida
     * @param username nombre del usuario
     *
     * @return conexion preparada con el nombre del usuario
     */
    public Connection prepare(Connection conn, String username) {
        String sql = "{ call DBMS_SESSION.SET_IDENTIFIER('" + username + "') }";
        try (CallableStatement cs = conn.prepareCall(sql)) {
            cs.execute();
        } catch (SQLException ex) {
            Logger.getLogger(ConexionDBUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return conn;
    }

}
