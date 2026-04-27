import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lee los archivos generados por GenerateInfoFiles, procesa las ventas
 * y produce dos reportes CSV: uno por vendedor y otro por producto.
 *
 * No solicita informacion al usuario; todo el procesamiento es automatico.
 */
public class Main {

    private static final String PRODUCTOS_FILE      = "productos.csv";
    private static final String VENDEDORES_FILE     = "vendedores.csv";
    private static final String VENTAS_DIR          = "ventas";
    private static final String REPORTE_VENDEDORES  = "reporte_vendedores.csv";
    private static final String REPORTE_PRODUCTOS   = "reporte_productos.csv";

    public static void main(String[] args) {
        try {
            System.out.println("Iniciando procesamiento de ventas...");

            // id -> precio del producto
            Map<String, Double> preciosProductos = new HashMap<>();
            // id -> nombre del producto
            Map<String, String> nombresProductos = new HashMap<>();
            // numero de documento -> "Nombre Apellido"
            Map<String, String> nombresVendedores = new HashMap<>();
            // numero de documento -> total vendido (en pesos)
            Map<String, Double> ventasPorVendedor = new HashMap<>();
            // id de producto -> cantidad total vendida
            Map<String, Integer> cantidadPorProducto = new HashMap<>();

            leerProductos(preciosProductos, nombresProductos);
            leerVendedores(nombresVendedores);
            leerVentas(preciosProductos, ventasPorVendedor, cantidadPorProducto);
            generarReporteVendedores(nombresVendedores, ventasPorVendedor);
            generarReporteProductos(nombresProductos, preciosProductos, cantidadPorProducto);

            System.out.println("Procesamiento completado exitosamente.");
        } catch (Exception e) {
            System.err.println("Error en el procesamiento: " + e.getMessage());
        }
    }

    /**
     * Lee el archivo de productos y llena los mapas de precios y nombres.
     * Formato esperado: IDProducto;NombreProducto;Precio
     */
    private static void leerProductos(Map<String, Double> precios,
                                      Map<String, String> nombres) throws Exception {
        try (BufferedReader br = new BufferedReader(new FileReader(PRODUCTOS_FILE))) {
            String linea;
            int numeroLinea = 0;
            while ((linea = br.readLine()) != null) {
                numeroLinea++;
                if (linea.trim().isEmpty()) continue;

                String[] datos = linea.split(";");
                if (datos.length < 3) {
                    System.out.println("  Advertencia: linea " + numeroLinea
                            + " de productos con formato invalido, se omite.");
                    continue;
                }
                try {
                    String id     = datos[0].trim();
                    String nombre = datos[1].trim();
                    double precio = Double.parseDouble(datos[2].trim());
                    precios.put(id, precio);
                    nombres.put(id, nombre);
                } catch (NumberFormatException e) {
                    System.out.println("  Advertencia: precio invalido en linea " + numeroLinea
                            + " de productos, se omite.");
                }
            }
        }
        System.out.println("  -> " + precios.size() + " producto(s) cargado(s).");
    }

    /**
     * Lee el archivo de vendedores y llena el mapa de nombres completos.
     * Formato esperado: TipoDocumento;NumeroDocumento;Nombres;Apellidos
     */
    private static void leerVendedores(Map<String, String> nombres) throws Exception {
        try (BufferedReader br = new BufferedReader(new FileReader(VENDEDORES_FILE))) {
            String linea;
            int numeroLinea = 0;
            while ((linea = br.readLine()) != null) {
                numeroLinea++;
                if (linea.trim().isEmpty()) continue;

                String[] datos = linea.split(";");
                if (datos.length < 4) {
                    System.out.println("  Advertencia: linea " + numeroLinea
                            + " de vendedores con formato invalido, se omite.");
                    continue;
                }
                String numDoc        = datos[1].trim();
                String nombreCompleto = datos[2].trim() + " " + datos[3].trim();
                nombres.put(numDoc, nombreCompleto);
            }
        }
        System.out.println("  -> " + nombres.size() + " vendedor(es) cargado(s).");
    }

    /**
     * Lee todos los archivos de la carpeta ventas/ y acumula totales por vendedor
     * y cantidades por producto. Valida cada linea antes de procesarla.
     */
    private static void leerVentas(Map<String, Double> precios,
                                   Map<String, Double> ventasPorVendedor,
                                   Map<String, Integer> cantidadPorProducto) throws Exception {

        File carpeta = new File(VENTAS_DIR);
        File[] archivos = carpeta.listFiles();

        if (archivos == null || archivos.length == 0) {
            System.out.println("  Advertencia: no se encontraron archivos en la carpeta "
                    + VENTAS_DIR + ".");
            return;
        }

        for (File archivo : archivos) {
            if (!archivo.isFile()) continue;
            procesarArchivoVentas(archivo, precios, ventasPorVendedor, cantidadPorProducto);
        }
        System.out.println("  -> " + archivos.length + " archivo(s) de ventas procesado(s).");
    }

    /**
     * Procesa un unico archivo de ventas.
     * Primera linea: TipoDocumento;NumeroDocumento del vendedor.
     * Lineas siguientes: IDProducto;Cantidad;
     */
    private static void procesarArchivoVentas(File archivo,
                                              Map<String, Double> precios,
                                              Map<String, Double> ventasPorVendedor,
                                              Map<String, Integer> cantidadPorProducto) {
        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String primeraLinea = br.readLine();
            if (primeraLinea == null || primeraLinea.trim().isEmpty()) {
                System.out.println("  Advertencia: " + archivo.getName() + " vacio, se omite.");
                return;
            }

            String[] encabezado = primeraLinea.split(";");
            if (encabezado.length < 2 || encabezado[1].trim().isEmpty()) {
                System.out.println("  Advertencia: encabezado invalido en "
                        + archivo.getName() + ", se omite.");
                return;
            }
            String idVendedor = encabezado[1].trim();
            double totalVendedor = 0;
            String linea;
            int numeroLinea = 1;

            while ((linea = br.readLine()) != null) {
                numeroLinea++;
                if (linea.trim().isEmpty()) continue;

                String[] datos = linea.split(";");
                if (datos.length < 2
                        || datos[0].trim().isEmpty()
                        || datos[1].trim().isEmpty()) {
                    System.out.println("  Advertencia: linea " + numeroLinea
                            + " en " + archivo.getName() + " con formato invalido, se omite.");
                    continue;
                }

                try {
                    String idProducto = datos[0].trim();
                    int cantidad      = Integer.parseInt(datos[1].trim());

                    if (cantidad <= 0) {
                        System.out.println("  Advertencia: cantidad invalida ("
                                + cantidad + ") en linea " + numeroLinea
                                + " de " + archivo.getName() + ", se omite.");
                        continue;
                    }

                    if (!precios.containsKey(idProducto)) {
                        System.out.println("  Advertencia: producto ID " + idProducto
                                + " no encontrado, linea " + numeroLinea
                                + " de " + archivo.getName() + " se omite.");
                        continue;
                    }

                    double precio = precios.get(idProducto);
                    totalVendedor += precio * cantidad;

                    int cantidadActual = cantidadPorProducto.containsKey(idProducto)
                            ? cantidadPorProducto.get(idProducto) : 0;
                    cantidadPorProducto.put(idProducto, cantidadActual + cantidad);

                } catch (NumberFormatException e) {
                    System.out.println("  Advertencia: cantidad no numerica en linea "
                            + numeroLinea + " de " + archivo.getName() + ", se omite.");
                }
            }

            double totalActual = ventasPorVendedor.containsKey(idVendedor)
                    ? ventasPorVendedor.get(idVendedor) : 0.0;
            ventasPorVendedor.put(idVendedor, totalActual + totalVendedor);

        } catch (IOException e) {
            System.out.println("  Error leyendo " + archivo.getName() + ": " + e.getMessage());
        }
    }

    /**
     * Genera reporte_vendedores.csv ordenado de mayor a menor por total vendido.
     * Formato: Nombre Completo;TotalVentas
     */
    private static void generarReporteVendedores(Map<String, String> nombres,
                                                  Map<String, Double> ventas) throws Exception {
        List<Map.Entry<String, Double>> lista = new ArrayList<>(ventas.entrySet());
        Collections.sort(lista, new Comparator<Map.Entry<String, Double>>() {
            public int compare(Map.Entry<String, Double> a, Map.Entry<String, Double> b) {
                return Double.compare(b.getValue(), a.getValue());
            }
        });

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(REPORTE_VENDEDORES))) {
            for (Map.Entry<String, Double> entry : lista) {
                String nombre = nombres.containsKey(entry.getKey())
                        ? nombres.get(entry.getKey())
                        : "Vendedor " + entry.getKey();
                bw.write(nombre + ";" + String.format("%.2f", entry.getValue()));
                bw.newLine();
            }
        }
        System.out.println("  -> " + REPORTE_VENDEDORES + " generado.");
    }

    /**
     * Genera reporte_productos.csv ordenado de mayor a menor por cantidad vendida.
     * Formato: NombreProducto;Precio;CantidadVendida
     */
    private static void generarReporteProductos(Map<String, String> nombres,
                                                 Map<String, Double> precios,
                                                 Map<String, Integer> cantidades) throws Exception {
        List<Map.Entry<String, Integer>> lista = new ArrayList<>(cantidades.entrySet());
        Collections.sort(lista, new Comparator<Map.Entry<String, Integer>>() {
            public int compare(Map.Entry<String, Integer> a, Map.Entry<String, Integer> b) {
                return Integer.compare(b.getValue(), a.getValue());
            }
        });

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(REPORTE_PRODUCTOS))) {
            for (Map.Entry<String, Integer> entry : lista) {
                String id     = entry.getKey();
                String nombre = nombres.containsKey(id) ? nombres.get(id) : "Producto " + id;
                double precio = precios.containsKey(id) ? precios.get(id) : 0.0;
                bw.write(nombre + ";" + precio + ";" + entry.getValue());
                bw.newLine();
            }
        }
        System.out.println("  -> " + REPORTE_PRODUCTOS + " generado.");
    }
}
