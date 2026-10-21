package modelo;

/**
 * Entidad del dominio: representa un producto del establecimiento.
 * Las reglas de validación viven aquí para que ningún producto inválido
 * pueda existir en memoria ni llegar al archivo JSON.
 */
public class Producto {
    private String codigo;
    private String nombre;
    private String descripcion;
    private double precio;
    private int cantidad;

    public Producto(String codigo, String nombre, String descripcion, double precio, int cantidad) {
        setCodigo(codigo);
        setNombre(nombre);
        setDescripcion(descripcion);
        setPrecio(precio);
        setCantidad(cantidad);
    }

    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public double getPrecio() { return precio; }
    public int getCantidad() { return cantidad; }

    public void setCodigo(String codigo) {
        if (codigo == null || codigo.isBlank())
            throw new IllegalArgumentException("El código no puede estar vacío.");
        this.codigo = codigo.trim();
    }

    public void setNombre(String nombre) {
        if (nombre == null || nombre.isBlank())
            throw new IllegalArgumentException("El nombre no puede estar vacío.");
        this.nombre = nombre.trim();
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion == null ? "" : descripcion.trim();
    }

    public void setPrecio(double precio) {
        if (Double.isNaN(precio) || Double.isInfinite(precio) || precio < 0)
            throw new IllegalArgumentException("El precio debe ser un número no negativo.");
        this.precio = precio;
    }

    public void setCantidad(int cantidad) {
        if (cantidad < 0)
            throw new IllegalArgumentException("La cantidad debe ser un número entero no negativo.");
        this.cantidad = cantidad;
    }

    @Override
    public String toString() {
        return String.format("%-8s | %-20s | %-25s | $%,12.2f | %5d",
                codigo, nombre, descripcion, precio, cantidad);
    }
}
