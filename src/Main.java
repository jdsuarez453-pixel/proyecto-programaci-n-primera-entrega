import java.io.*;
import java.util.*;

public class Main {

    public static void main(String[] args) {

        try {
            Map<String, Double> productos = new HashMap<>();
            Map<String, String> vendedores = new HashMap<>();
            Map<String, Double> ventasPorVendedor = new HashMap<>();

            // Leer productos
            BufferedReader brProductos = new BufferedReader(new FileReader("productos.csv"));
            String linea;
            while ((linea = brProductos.readLine()) != null) {
                String[] datos = linea.split(";");
                productos.put(datos[0], Double.parseDouble(datos[2]));
            }
            brProductos.close();

            // Leer vendedores
            BufferedReader brVendedores = new BufferedReader(new FileReader("vendedores.csv"));
            while ((linea = brVendedores.readLine()) != null) {
                String[] datos = linea.split(";");
                vendedores.put(datos[1], datos[2] + " " + datos[3]);
            }
            brVendedores.close();

            // Leer ventas (carpeta ventas)
            File carpeta = new File("ventas");
            File[] archivos = carpeta.listFiles();

            for (File archivo : archivos) {

                BufferedReader brVentas = new BufferedReader(new FileReader(archivo));

                String primeraLinea = brVentas.readLine();
                String[] vendedorInfo = primeraLinea.split(";");
                String idVendedor = vendedorInfo[1];

                double total = 0;

                while ((linea = brVentas.readLine()) != null) {
                    String[] datos = linea.split(";");
                    String idProducto = datos[0];
                    int cantidad = Integer.parseInt(datos[1]);

                    double precio = productos.getOrDefault(idProducto, 0.0);
                    total += precio * cantidad;
                }

                ventasPorVendedor.put(idVendedor,
                        ventasPorVendedor.getOrDefault(idVendedor, 0.0) + total);

                brVentas.close();
            }

            // Ordenar resultados
            List<Map.Entry<String, Double>> lista = new ArrayList<>(ventasPorVendedor.entrySet());
            lista.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

            // Crear archivo final
            BufferedWriter bw = new BufferedWriter(new FileWriter("reporte_vendedores.csv"));

            for (Map.Entry<String, Double> entry : lista) {
                String nombre = vendedores.get(entry.getKey());
                bw.write(nombre + ";" + entry.getValue());
                bw.newLine();
            }

            bw.close();

            System.out.println("Reporte generado correctamente");

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
