import datos.PersistenciaException;
import datos.ProductoDAO;
import presentacion.ConsolaUI;

import java.nio.file.Path;

public class Main {
    public static void main(String[] args) {
        try {
            ProductoDAO dao = new ProductoDAO(Path.of("data", "productos.json"));
            new ConsolaUI(dao).iniciar();
        } catch (PersistenciaException e) {
            System.err.println("No se pudo iniciar la aplicación: " + e.getMessage());
        }
    }
}
