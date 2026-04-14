

import java.io.*;
import java.util.*;

public class DataProcessingModule {

    private static final String PRODUCTOS_FILE = "data/productos.csv";
    private static final String VENDEDORES_FILE = "data/vendedores.csv";
    private static final String VENDEDOR_PREFIX = "data/vendedor_";
    private static final String CSV_SEPARATOR = ";";

    private Map<Integer, Producto> productos;
    private Map<String, Vendedor> vendedores;

    public DataProcessingModule() {
        this.productos = new HashMap<>();
        this.vendedores = new HashMap<>();
    }

    public static void main(String[] args) {
        try {
            DataProcessingModule processor = new DataProcessingModule();
            System.out.println("Iniciando procesamiento de datos...");
            
            processor.loadProducts();
            processor.loadVendedores();
            processor.generateReport();
            
            System.out.println("\n¡Procesamiento completado exitosamente!");
        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadProducts() throws Exception {
        System.out.println("Cargando productos...");
        try (BufferedReader reader = new BufferedReader(new FileReader(PRODUCTOS_FILE))) {
            String line;
            boolean isHeader = true;
            
            while ((line = reader.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }
                
                if (validarLinea(line)) {
                    try {
                        String[] parts = line.split(CSV_SEPARATOR, -1);
                        int id = Integer.parseInt(parts[0].trim());
                        String nombre = parts[1].trim();
                        double precio = Double.parseDouble(parts[2].trim());
                        
                        productos.put(id, new Producto(id, nombre, precio));
                        System.out.println("✓ Producto cargado: " + nombre);
                    } catch (NumberFormatException e) {
                        System.out.println("  ✗ Error al parsear línea: " + line);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println("ERROR: Archivo no encontrado: " + PRODUCTOS_FILE);
        }
    }

    private void loadVendedores() throws Exception {
        System.out.println("\nCargando vendedores...");
        try (BufferedReader reader = new BufferedReader(new FileReader(VENDEDORES_FILE))) {
            String line;
            boolean isHeader = true;
            
            while ((line = reader.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }
                
                if (validarLinea(line)) {
                    try {
                        String[] parts = line.split(CSV_SEPARATOR, -1);
                        if (parts.length < 4) {
                            System.out.println("  ✗ Línea con datos incompletos: " + line);
                            continue;
                        }
                        
                        String tipoDoc = parts[0].trim();
                        String id = parts[1].trim();
                        String nombre = parts[2].trim();
                        String apellido = parts[3].trim();
                        
                        String key = tipoDoc + "-" + id;
                        Vendedor vendedor = new Vendedor(tipoDoc, id, nombre, apellido);
                        vendedores.put(key, vendedor);
                        
                        loadVentasVendedor(vendedor);
                        System.out.println("✓ Vendedor cargado: " + nombre + " " + apellido);
                    } catch (Exception e) {
                        System.out.println("  ✗ Error al procesar vendedor: " + e.getMessage());
                    }
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println("ERROR: Archivo no encontrado: " + VENDEDORES_FILE);
        }
    }

    private void loadVentasVendedor(Vendedor vendedor) throws Exception {
        String fileName = VENDEDOR_PREFIX + vendedor.getId() + ".csv";
        File file = new File(fileName);
        
        if (!file.exists()) {
            System.out.println("  ⚠ Archivo de ventas no encontrado: " + fileName);
            return;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            int lineCount = 0;
            
            while ((line = reader.readLine()) != null) {
                lineCount++;
                
                // Saltar encabezados
                if (lineCount <= 2) continue;
                
                if (validarLinea(line)) {
                    try {
                        String[] parts = line.split(CSV_SEPARATOR, -1);
                        if (parts.length < 2) {
                            System.out.println("  ✗ Línea de venta incompleta: " + line);
                            continue;
                        }
                        
                        int productoId = Integer.parseInt(parts[0].trim());
                        int cantidad = Integer.parseInt(parts[1].trim());
                        
                        if (productos.containsKey(productoId)) {
                            Producto producto = productos.get(productoId);
                            double precioUnitario = producto.getPrecio();
                            double total = precioUnitario * cantidad;
                            
                            // MEJORA: Pasar el precio unitario al momento de la venta para trazabilidad
                            Venta venta = new Venta(productoId, producto.getNombre(), 
                                                   cantidad, precioUnitario, total);
                            vendedor.agregarVenta(venta);
                        } else {
                            System.out.println("  ⚠ Producto ID no encontrado: " + productoId);
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("  ✗ Error al parsear venta: " + line);
                    }
                }
            }
        }
    }

    private boolean validarLinea(String line) {
        if (line == null || line.trim().isEmpty()) {
            return false;
        }
        
        String[] parts = line.split(CSV_SEPARATOR, -1);
        if (parts.length < 2) {
            System.out.println("  ✗ Línea inválida (faltan separadores): " + line);
            return false;
        }
        
        return true;
    }

    private void generateReport() {
        System.out.println("\n========== REPORTE DE VENTAS ==========");
        
        if (vendedores.isEmpty()) {
            System.out.println("No hay vendedores para mostrar.");
            return;
        }
        
        double totalGeneral = 0;
        
        for (Vendedor vendedor : vendedores.values()) {
            double totalVendedor = vendedor.calcularTotalVentas();
            totalGeneral += totalVendedor;
            
            System.out.println("\nVendedor: " + vendedor.getNombre() + " " + vendedor.getApellido());
            System.out.println("  Documento: " + vendedor.getTipoDoc() + "-" + vendedor.getId());
            System.out.println("  Total de ventas: $" + String.format("%.2f", totalVendedor));
            System.out.println("  Cantidad de transacciones: " + vendedor.getVentas().size());
            
            // Mostrar detalle de ventas
            System.out.println("  Detalle de ventas:");
            for (Venta venta : vendedor.getVentas()) {
                System.out.println("    - " + venta.getNombreProducto() + 
                                 " (ID: " + venta.getProductoId() + "): " +
                                 venta.getCantidad() + " unidades × $" + 
                                 String.format("%.2f", venta.getPrecioUnitario()) + 
                                 " = $" + String.format("%.2f", venta.getTotal()));
            }
        }
        
        System.out.println("\n========== TOTAL GENERAL ==========");
        System.out.println("Total vendido: $" + String.format("%.2f", totalGeneral));
    }

    // Clases internas
    private static class Producto {
        private int id;
        private String nombre;
        private double precio;

        public Producto(int id, String nombre, double precio) {
            this.id = id;
            this.nombre = nombre;
            this.precio = precio;
        }

        public int getId() { return id; }
        public String getNombre() { return nombre; }
        public double getPrecio() { return precio; }
    }

    private static class Vendedor {
        private String tipoDoc;
        private String id;
        private String nombre;
        private String apellido;
        private List<Venta> ventas;

        public Vendedor(String tipoDoc, String id, String nombre, String apellido) {
            this.tipoDoc = tipoDoc;
            this.id = id;
            this.nombre = nombre;
            this.apellido = apellido;
            this.ventas = new ArrayList<>();
        }

        public void agregarVenta(Venta venta) {
            ventas.add(venta);
        }

        public double calcularTotalVentas() {
            return ventas.stream().mapToDouble(Venta::getTotal).sum();
        }

        public String getTipoDoc() { return tipoDoc; }
        public String getId() { return id; }
        public String getNombre() { return nombre; }
        public String getApellido() { return apellido; }
        public List<Venta> getVentas() { return ventas; }
    }

    // MEJORA: Clase Venta actualizada con precio unitario y nombre de producto
    private static class Venta {
        private int productoId;
        private String nombreProducto;
        private int cantidad;
        private double precioUnitario;
        private double total;

        public Venta(int productoId, String nombreProducto, int cantidad, 
                    double precioUnitario, double total) {
            this.productoId = productoId;
            this.nombreProducto = nombreProducto;
            this.cantidad = cantidad;
            this.precioUnitario = precioUnitario;
            this.total = total;
        }

        public int getProductoId() { return productoId; }
        public String getNombreProducto() { return nombreProducto; }
        public int getCantidad() { return cantidad; }
        public double getPrecioUnitario() { return precioUnitario; }
        public double getTotal() { return total; }
    }
}

