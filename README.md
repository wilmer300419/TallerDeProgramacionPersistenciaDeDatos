# Sistema de Gestión de Productos (Java + JSON)

Aplicación de consola en Java que administra productos (CRUD) y conserva los datos en un
archivo JSON (`data/productos.json`). No requiere gestor de bases de datos ni librerías externas:
solo un JDK (versión 17 o superior).

## Estructura
- `src/modelo/Producto.java` – entidad y validaciones (precio >= 0, cantidad entera >= 0).
- `src/datos/ProductoDAO.java` – CRUD y persistencia en el archivo JSON (patrón DAO).
- `src/datos/JsonUtil.java` – lectura y escritura de JSON sin dependencias.
- `src/datos/PersistenciaException.java` – errores de lectura/escritura del archivo.
- `src/presentacion/ConsolaUI.java` – menú de consola.
- `src/Main.java` – punto de entrada.

## Compilar y ejecutar
Desde la carpeta `GestionProductos`:

    javac -encoding UTF-8 -d out src/Main.java src/modelo/*.java src/datos/*.java src/presentacion/*.java
    java -cp out Main

(En Windows PowerShell reemplaza `src/modelo/*.java` etc. por `(Get-ChildItem -Recurse src -Filter *.java).FullName`,
o simplemente abre la carpeta `src` en IntelliJ/Eclipse/NetBeans y ejecuta `Main`.)

El archivo `data/productos.json` se crea solo en la primera ejecución.

## Formato del archivo JSON
    [
      {
        "codigo": "P001",
        "nombre": "Arroz",
        "descripcion": "Bulto 500g",
        "precio": 3500.0,
        "cantidad": 40
      }
    ]
