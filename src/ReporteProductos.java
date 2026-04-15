import java.io.*;
import java.util.*;

public class ReporteProductos {

    public static void main(String[] args) {

        try {
            BufferedReader br = new BufferedReader(new FileReader("productos.csv"));

            List<String> lista = new ArrayList<>();
            String linea;

            while ((linea = br.readLine()) != null) {
                String[] datos = linea.split(";");

                String nombreProducto = datos[1];
                double precio = Double.parseDouble(datos[2]);

                // Cantidad simulada
                int cantidad = (int)(Math.random() * 100);

                lista.add(nombreProducto + ";" + precio + ";" + cantidad);
            }

            br.close();

            BufferedWriter bw = new BufferedWriter(new FileWriter("reporte_productos.csv"));

            for (String item : lista) {
                bw.write(item);
                bw.newLine();
            }

            bw.close();

            System.out.println("Reporte de productos generado");

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}Nuevo reporte de productos
