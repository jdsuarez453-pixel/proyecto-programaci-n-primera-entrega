import java.io.File;
import java.io.PrintWriter;
import java.util.Random;

/**
 * Genera los archivos planos de prueba que sirven como entrada para la clase Main.
 * Crea: productos.csv, vendedores.csv y los archivos de ventas en la carpeta ventas/.
 */
public class GenerateInfoFiles {

    private static final String[] NOMBRES     = {"Carlos", "Ana", "Luis", "Maria", "Juan"};
    private static final String[] APELLIDOS   = {"Gomez", "Perez", "Rodriguez", "Martinez"};
    private static final String[] TIPOS_DOC   = {"CC", "CE", "TI"};
    private static final String[] PROD_NOMBRES = {"Laptop", "Mouse", "Teclado", "Monitor"};
    private static final double[] PROD_PRECIOS = {2500000.50, 80000.00, 150000.99, 950000.00};

    private static final String VENTAS_DIR = "ventas";

    public static void main(String[] args) {
        try {
            System.out.println("Iniciando generacion de archivos...");

            File carpetaVentas = new File(VENTAS_DIR);
            if (!carpetaVentas.exists()) {
                carpetaVentas.mkdir();
            }

            createProductsFile(PROD_NOMBRES.length);
            createSalesManInfoFile(3);

            System.out.println("Archivos generados exitosamente.");
        } catch (Exception e) {
            System.err.println("Error al generar archivos: " + e.getMessage());
        }
    }

    /**
     * Crea el archivo productos.csv con la cantidad indicada de productos.
     *
     * @param productsCount numero de productos a generar
     * @throws Exception si ocurre un error al escribir el archivo
     */
    public static void createProductsFile(int productsCount) throws Exception {
        try (PrintWriter writer = new PrintWriter("productos.csv", "UTF-8")) {
            for (int i = 0; i < productsCount; i++) {
                writer.println((i + 1) + ";" + PROD_NOMBRES[i] + ";" + PROD_PRECIOS[i]);
            }
        }
        System.out.println("  -> productos.csv creado (" + productsCount + " productos).");
    }

    /**
     * Crea el archivo vendedores.csv y un archivo de ventas por cada vendedor.
     *
     * @param salesmanCount numero de vendedores a generar
     * @throws Exception si ocurre un error al escribir los archivos
     */
    public static void createSalesManInfoFile(int salesmanCount) throws Exception {
        Random rand = new Random();
        try (PrintWriter writer = new PrintWriter("vendedores.csv", "UTF-8")) {
            for (int i = 0; i < salesmanCount; i++) {
                long id = 100000000L + rand.nextInt(900000000);
                String tipoDoc = TIPOS_DOC[rand.nextInt(TIPOS_DOC.length)];
                String nombre   = NOMBRES[rand.nextInt(NOMBRES.length)];
                String apellido = APELLIDOS[rand.nextInt(APELLIDOS.length)];
                writer.println(tipoDoc + ";" + id + ";" + nombre + ";" + apellido);
                createSalesMenFile(rand.nextInt(4) + 2, nombre, id);
            }
        }
        System.out.println("  -> vendedores.csv creado (" + salesmanCount + " vendedores).");
    }

    /**
     * Crea un archivo de ventas pseudoaleatorio para un vendedor especifico
     * dentro de la carpeta ventas/.
     *
     * @param randomSalesCount numero de lineas de venta a generar
     * @param name             nombre del vendedor (no se usa en el archivo, solo referencial)
     * @param id               numero de documento del vendedor
     * @throws Exception si ocurre un error al escribir el archivo
     */
    public static void createSalesMenFile(int randomSalesCount, String name, long id) throws Exception {
        Random rand = new Random();
        String fileName = VENTAS_DIR + "/vendedor_" + id + ".csv";
        try (PrintWriter writer = new PrintWriter(fileName, "UTF-8")) {
            writer.println(TIPOS_DOC[rand.nextInt(TIPOS_DOC.length)] + ";" + id);
            for (int i = 0; i < randomSalesCount; i++) {
                int idProducto = rand.nextInt(PROD_NOMBRES.length) + 1;
                int cantidad   = rand.nextInt(10) + 1;
                writer.println(idProducto + ";" + cantidad + ";");
            }
        }
        System.out.println("  -> " + fileName + " creado (" + randomSalesCount + " ventas).");
    }
}
