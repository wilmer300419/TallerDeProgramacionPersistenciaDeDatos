package datos;

/** Error al leer o escribir el archivo JSON (equivalente a SQLException en JDBC). */
public class PersistenciaException extends Exception {
    public PersistenciaException(String mensaje) { super(mensaje); }
    public PersistenciaException(String mensaje, Throwable causa) { super(mensaje, causa); }
}
