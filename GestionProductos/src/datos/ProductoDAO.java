package datos;

import modelo.Producto;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * DAO (Data Access Object): único punto de acceso a los datos.
 * Los productos se mantienen en memoria y cada cambio se escribe de inmediato
 * en el archivo JSON, de modo que la información sobrevive al cierre del programa.
 */
public class ProductoDAO {

    private final Path archivo;
    private final List<Producto> productos = new ArrayList<>();

    public ProductoDAO(Path archivo) throws PersistenciaException {
        this.archivo = archivo;
        cargar();
    }

    // ------------------------------------------------------------------- CREATE

    public void crear(Producto p) throws PersistenciaException {
        if (buscarPorCodigo(p.getCodigo()) != null)
            throw new IllegalArgumentException("Ya existe un producto con el código " + p.getCodigo() + ".");
        productos.add(p);
        guardarOReversar();
    }

    // --------------------------------------------------------------------- READ

    public List<Producto> listar() {
        return new ArrayList<>(productos);
    }

    public Producto buscarPorCodigo(String codigo) {
        for (Producto p : productos)
            if (p.getCodigo().equalsIgnoreCase(codigo.trim())) return p;
        return null;
    }

    public List<Producto> buscarPorNombre(String texto) {
        String t = texto.trim().toLowerCase(Locale.ROOT);
        List<Producto> r = new ArrayList<>();
        for (Producto p : productos)
            if (p.getNombre().toLowerCase(Locale.ROOT).contains(t)) r.add(p);
        return r;
    }

    // ------------------------------------------------------------------- UPDATE

    /** Los parámetros null significan "no modificar ese campo". Devuelve false si el código no existe. */
    public boolean actualizar(String codigo, String descripcion, Double precio, Integer cantidad)
            throws PersistenciaException {
        Producto p = buscarPorCodigo(codigo);
        if (p == null) return false;
        String descOriginal = p.getDescripcion();
        double precioOriginal = p.getPrecio();
        int cantOriginal = p.getCantidad();
        try {
            if (descripcion != null) p.setDescripcion(descripcion);
            if (precio != null) p.setPrecio(precio);
            if (cantidad != null) p.setCantidad(cantidad);
            guardar();
        } catch (IllegalArgumentException | PersistenciaException e) {
            p.setDescripcion(descOriginal);
            p.setPrecio(precioOriginal);
            p.setCantidad(cantOriginal);
            throw e;
        }
        return true;
    }

    // ------------------------------------------------------------------- DELETE

    public boolean eliminar(String codigo) throws PersistenciaException {
        Producto p = buscarPorCodigo(codigo);
        if (p == null) return false;
        productos.remove(p);
        guardarOReversar();
        return true;
    }

    // -------------------------------------------------------------- PERSISTENCIA

    private void cargar() throws PersistenciaException {
        productos.clear();
        try {
            if (archivo.getParent() != null) Files.createDirectories(archivo.getParent());
            if (!Files.exists(archivo)) {          // primera ejecución: se crea el archivo vacío
                guardar();
                return;
            }
            String texto = Files.readString(archivo, StandardCharsets.UTF_8);
            if (texto.isBlank()) return;
            for (Map<String, Object> m : JsonUtil.leer(texto)) {
                productos.add(new Producto(
                        (String) m.get("codigo"),
                        (String) m.get("nombre"),
                        (String) m.get("descripcion"),
                        ((Number) m.get("precio")).doubleValue(),
                        ((Number) m.get("cantidad")).intValue()));
            }
        } catch (IOException e) {
            throw new PersistenciaException("No se pudo leer el archivo " + archivo + ".", e);
        } catch (IllegalArgumentException | ClassCastException | NullPointerException e) {
            throw new PersistenciaException("El archivo " + archivo + " tiene un formato inválido: "
                    + e.getMessage(), e);
        }
    }

    private void guardar() throws PersistenciaException {
        List<Map<String, Object>> datos = new ArrayList<>();
        for (Producto p : productos) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("codigo", p.getCodigo());
            m.put("nombre", p.getNombre());
            m.put("descripcion", p.getDescripcion());
            m.put("precio", p.getPrecio());
            m.put("cantidad", p.getCantidad());
            datos.add(m);
        }
        Path temporal = archivo.resolveSibling(archivo.getFileName() + ".tmp");
        try {
            // Se escribe en un temporal y luego se reemplaza, para no dejar el JSON a medias si falla.
            Files.writeString(temporal, JsonUtil.escribir(datos), StandardCharsets.UTF_8);
            try {
                Files.move(temporal, archivo, StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporal, archivo, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new PersistenciaException("No se pudo guardar el archivo " + archivo + ".", e);
        }
    }

    /** Si falla el guardado, se recarga desde disco para que memoria y archivo no se desincronicen. */
    private void guardarOReversar() throws PersistenciaException {
        try {
            guardar();
        } catch (PersistenciaException e) {
            try { cargar(); } catch (PersistenciaException ignorada) { /* se informa el error original */ }
            throw e;
        }
    }
}
