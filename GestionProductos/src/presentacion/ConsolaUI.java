package presentacion;

import datos.PersistenciaException;
import datos.ProductoDAO;
import modelo.Producto;

import java.util.List;
import java.util.Scanner;

/** Interfaz de consola: solo pide datos y muestra resultados; la lógica de datos está en el DAO. */
public class ConsolaUI {

    private final ProductoDAO dao;
    private final Scanner in = new Scanner(System.in);

    public ConsolaUI(ProductoDAO dao) { this.dao = dao; }

    public void iniciar() {
        int opcion;
        do {
            menu();
            opcion = leerEntero("Seleccione una opción: ");
            try {
                switch (opcion) {
                    case 1 -> registrar();
                    case 2 -> mostrar(dao.listar());
                    case 3 -> buscar();
                    case 4 -> actualizar();
                    case 5 -> eliminar();
                    case 0 -> System.out.println("Hasta luego. Los datos quedaron guardados en el archivo JSON.");
                    default -> System.out.println("Opción no válida.");
                }
            } catch (IllegalArgumentException e) {
                System.out.println("Dato inválido: " + e.getMessage());
            } catch (PersistenciaException e) {
                System.out.println("Error de persistencia: " + e.getMessage());
            }
        } while (opcion != 0);
    }

    private void menu() {
        System.out.println("\n===== SISTEMA DE GESTIÓN DE PRODUCTOS =====");
        System.out.println("1. Registrar producto");
        System.out.println("2. Listar todos los productos");
        System.out.println("3. Buscar por código o nombre");
        System.out.println("4. Actualizar producto");
        System.out.println("5. Eliminar producto");
        System.out.println("0. Salir");
    }

    private void registrar() throws PersistenciaException {
        String codigo = leerTexto("Código: ");
        String nombre = leerTexto("Nombre: ");
        String descripcion = leerTexto("Descripción: ");
        double precio = leerDecimal("Precio: ");
        int cantidad = leerEntero("Cantidad disponible: ");
        dao.crear(new Producto(codigo, nombre, descripcion, precio, cantidad));
        System.out.println("Producto registrado correctamente.");
    }

    private void buscar() {
        String t = leerTexto("Ingrese el código o parte del nombre: ");
        Producto porCodigo = dao.buscarPorCodigo(t);
        mostrar(porCodigo != null ? List.of(porCodigo) : dao.buscarPorNombre(t));
    }

    private void actualizar() throws PersistenciaException {
        String codigo = leerTexto("Código del producto a actualizar: ");
        Producto actual = dao.buscarPorCodigo(codigo);
        if (actual == null) { System.out.println("No existe un producto con ese código."); return; }
        System.out.println("Deje vacío un campo para conservar su valor actual.");
        String desc = leerTexto("Nueva descripción [" + actual.getDescripcion() + "]: ");
        String precioTxt = leerTexto("Nuevo precio [" + actual.getPrecio() + "]: ").trim();
        String cantTxt = leerTexto("Nueva cantidad [" + actual.getCantidad() + "]: ").trim();

        Double precio = precioTxt.isEmpty() ? null : convertirDecimal(precioTxt);
        Integer cantidad = cantTxt.isEmpty() ? null : convertirEntero(cantTxt);
        dao.actualizar(codigo, desc.isBlank() ? null : desc, precio, cantidad);
        System.out.println("Producto actualizado correctamente.");
    }

    private void eliminar() throws PersistenciaException {
        String codigo = leerTexto("Código del producto a eliminar: ");
        Producto p = dao.buscarPorCodigo(codigo);
        if (p == null) { System.out.println("No existe un producto con ese código."); return; }
        System.out.println(p);
        if (leerTexto("¿Confirma la eliminación? (s/n): ").trim().equalsIgnoreCase("s")) {
            dao.eliminar(codigo);
            System.out.println("Producto eliminado.");
        } else {
            System.out.println("Operación cancelada.");
        }
    }

    private void mostrar(List<Producto> lista) {
        if (lista.isEmpty()) { System.out.println("No se encontraron productos."); return; }
        System.out.printf("%-8s | %-20s | %-25s | %13s | %5s%n", "CÓDIGO", "NOMBRE", "DESCRIPCIÓN", "PRECIO", "CANT.");
        System.out.println("-".repeat(86));
        lista.forEach(System.out::println);
    }

    // ------------------------------------------------------ lectura de datos
    // Un valor no numérico se convierte en un valor inválido (NaN o -1) que el
    // modelo rechaza con un mensaje claro; así la validación queda en un solo lugar.

    private String leerTexto(String msg) {
        System.out.print(msg);
        return in.nextLine();
    }

    private int leerEntero(String msg) { return convertirEntero(leerTexto(msg).trim()); }

    private double leerDecimal(String msg) { return convertirDecimal(leerTexto(msg).trim()); }

    private int convertirEntero(String t) {
        try { return Integer.parseInt(t); } catch (NumberFormatException e) { return -1; }
    }

    private double convertirDecimal(String t) {
        try { return Double.parseDouble(t.replace(',', '.')); } catch (NumberFormatException e) { return Double.NaN; }
    }
}
