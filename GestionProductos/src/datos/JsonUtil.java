package datos;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Lector/escritor JSON mínimo y sin dependencias externas.
 * Soporta objetos, arreglos, cadenas, números, booleanos y null.
 */
public final class JsonUtil {

    private JsonUtil() { }

    // ---------------------------------------------------------------- ESCRITURA

    public static String escribir(List<Map<String, Object>> lista) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < lista.size(); i++) {
            sb.append(i == 0 ? "\n" : ",\n").append("  {");
            int j = 0;
            for (Map.Entry<String, Object> e : lista.get(i).entrySet()) {
                sb.append(j++ == 0 ? "\n" : ",\n").append("    ");
                sb.append(cadena(e.getKey())).append(": ").append(valor(e.getValue()));
            }
            sb.append("\n  }");
        }
        return sb.append(lista.isEmpty() ? "]" : "\n]").append("\n").toString();
    }

    private static String valor(Object v) {
        if (v == null) return "null";
        if (v instanceof String s) return cadena(s);
        return String.valueOf(v); // números y booleanos
    }

    private static String cadena(String s) {
        StringBuilder sb = new StringBuilder("\"");
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
                }
            }
        }
        return sb.append('"').toString();
    }

    // ------------------------------------------------------------------ LECTURA

    /** Lee un arreglo JSON de objetos. Lanza IllegalArgumentException si el formato es inválido. */
    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> leer(String texto) {
        Object raiz = new Lector(texto).leerDocumento();
        if (!(raiz instanceof List<?> lista))
            throw new IllegalArgumentException("Se esperaba un arreglo JSON en la raíz.");
        List<Map<String, Object>> resultado = new ArrayList<>();
        for (Object o : lista) {
            if (!(o instanceof Map<?, ?>))
                throw new IllegalArgumentException("Cada elemento del arreglo debe ser un objeto.");
            resultado.add((Map<String, Object>) o);
        }
        return resultado;
    }

    private static final class Lector {
        private final String s;
        private int i = 0;

        Lector(String s) { this.s = s; }

        Object leerDocumento() {
            Object v = leerValor();
            saltarEspacios();
            if (i < s.length()) throw error("Contenido inesperado tras el documento");
            return v;
        }

        private Object leerValor() {
            saltarEspacios();
            if (i >= s.length()) throw error("Fin inesperado del documento");
            char c = s.charAt(i);
            if (c == '{') return leerObjeto();
            if (c == '[') return leerArreglo();
            if (c == '"') return leerCadena();
            if (s.startsWith("true", i)) { i += 4; return Boolean.TRUE; }
            if (s.startsWith("false", i)) { i += 5; return Boolean.FALSE; }
            if (s.startsWith("null", i)) { i += 4; return null; }
            return leerNumero();
        }

        private Map<String, Object> leerObjeto() {
            Map<String, Object> m = new LinkedHashMap<>();
            i++; // {
            saltarEspacios();
            if (peek() == '}') { i++; return m; }
            while (true) {
                saltarEspacios();
                if (peek() != '"') throw error("Se esperaba el nombre de una propiedad");
                String clave = leerCadena();
                saltarEspacios();
                esperar(':');
                m.put(clave, leerValor());
                saltarEspacios();
                if (peek() == ',') { i++; continue; }
                esperar('}');
                return m;
            }
        }

        private List<Object> leerArreglo() {
            List<Object> l = new ArrayList<>();
            i++; // [
            saltarEspacios();
            if (peek() == ']') { i++; return l; }
            while (true) {
                l.add(leerValor());
                saltarEspacios();
                if (peek() == ',') { i++; continue; }
                esperar(']');
                return l;
            }
        }

        private String leerCadena() {
            StringBuilder sb = new StringBuilder();
            i++; // comilla inicial
            while (i < s.length()) {
                char c = s.charAt(i++);
                if (c == '"') return sb.toString();
                if (c != '\\') { sb.append(c); continue; }
                if (i >= s.length()) break;
                char e = s.charAt(i++);
                switch (e) {
                    case '"', '\\', '/' -> sb.append(e);
                    case 'n' -> sb.append('\n');
                    case 'r' -> sb.append('\r');
                    case 't' -> sb.append('\t');
                    case 'b' -> sb.append('\b');
                    case 'f' -> sb.append('\f');
                    case 'u' -> {
                        if (i + 4 > s.length()) throw error("Secuencia \\u incompleta");
                        sb.append((char) Integer.parseInt(s.substring(i, i + 4), 16));
                        i += 4;
                    }
                    default -> throw error("Secuencia de escape inválida: \\" + e);
                }
            }
            throw error("Cadena sin cerrar");
        }

        private Number leerNumero() {
            int inicio = i;
            while (i < s.length() && "+-0123456789.eE".indexOf(s.charAt(i)) >= 0) i++;
            if (inicio == i) throw error("Valor inesperado");
            try {
                return Double.parseDouble(s.substring(inicio, i));
            } catch (NumberFormatException ex) {
                throw error("Número inválido");
            }
        }

        private char peek() {
            if (i >= s.length()) throw error("Fin inesperado del documento");
            return s.charAt(i);
        }

        private void esperar(char c) {
            if (peek() != c) throw error("Se esperaba '" + c + "'");
            i++;
        }

        private void saltarEspacios() {
            while (i < s.length() && Character.isWhitespace(s.charAt(i))) i++;
        }

        private IllegalArgumentException error(String msg) {
            return new IllegalArgumentException("JSON inválido en la posición " + i + ": " + msg);
        }
    }
}
