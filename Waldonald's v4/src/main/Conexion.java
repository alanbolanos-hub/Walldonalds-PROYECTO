/*
 * Clase encargada de manejar la conexión a la base de datos embebida
 * (SQLite) del proyecto Waldonald's. No requiere servidor: la base de
 * datos vive en un solo archivo local que se crea automáticamente.
 *
 * Requiere tener agregada la librería sqlite-jdbc en el proyecto
 * (Properties -> Libraries -> Add JAR/Folder, o vía Maven/Gradle:
 * org.xerial:sqlite-jdbc).
 */
package main;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 *
 * @author Waldonald's
 */
public class Conexion {

    // Nombre del archivo de base de datos. Se crea junto al ejecutable/proyecto.
    private static final String RUTA_DB = "WalDonals_db.sqlite";
    private static final String URL = "jdbc:sqlite:" + RUTA_DB;

    private static Connection conexion;

    private Conexion() {
    }

    /**
     * Devuelve una conexión activa a la base de datos embebida.
     * La primera vez que se llama, crea el archivo y todas las tablas
     * si todavía no existen.
     */
    public static Connection getConexion() {
        try {
            if (conexion == null || conexion.isClosed()) {
                Class.forName("org.sqlite.JDBC");
                conexion = DriverManager.getConnection(URL);

                try (Statement st = conexion.createStatement()) {
                    st.execute("PRAGMA foreign_keys = ON;");
                }

                inicializarBaseDatos();
                migrarColumnasNuevas();
            }
        } catch (ClassNotFoundException e) {
            System.err.println("No se encontró el driver de SQLite (sqlite-jdbc): " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Error al conectar con la base de datos: " + e.getMessage());
        }
        return conexion;
    }

    /**
     * Crea todas las tablas del sistema (si no existen) y siembra los
     * roles y usuarios iniciales. Se ejecuta automáticamente al abrir
     * la conexión por primera vez.
     */
    private static void inicializarBaseDatos() {
        String[] sentencias = {
            "CREATE TABLE IF NOT EXISTS Roles (" +
                "ID_Rol INTEGER PRIMARY KEY, " +
                "Rol TEXT NOT NULL)",

            "CREATE TABLE IF NOT EXISTS Usuarios (" +
                "ID_Usuario INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "id_rol INTEGER, " +
                "Usuario TEXT NOT NULL, " +
                "Password TEXT NOT NULL, " +
                "Estado INTEGER DEFAULT 1, " +
                "Fecha_creacion TEXT DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (id_rol) REFERENCES Roles(ID_Rol))",

            "CREATE TABLE IF NOT EXISTS Categorias (" +
                "id_categoria INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nombre TEXT NOT NULL, " +
                "descripcion TEXT, " +
                "imagen TEXT, " +
                "orden_visual INTEGER, " +
                "estado INTEGER DEFAULT 1)",

            "CREATE TABLE IF NOT EXISTS Productos (" +
                "id_producto INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "id_categoria INTEGER NOT NULL, " +
                "nombre TEXT NOT NULL, " +
                "descripcion TEXT, " +
                "precio REAL NOT NULL, " +
                "costo REAL, " +
                "calorias INTEGER, " +
                "disponible INTEGER DEFAULT 1, " +
                "fecha_creacion TEXT DEFAULT CURRENT_TIMESTAMP, " +
                "fecha_actualizacion TEXT DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (id_categoria) REFERENCES Categorias(id_categoria))",

            "CREATE TABLE IF NOT EXISTS Inventario (" +
                "id_inventario INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "id_producto INTEGER NOT NULL, " +
                "stock_actual INTEGER NOT NULL DEFAULT 0, " +
                "stock_minimo INTEGER NOT NULL DEFAULT 10, " +
                "stock_maximo INTEGER NOT NULL DEFAULT 100, " +
                "ultima_actualizacion TEXT DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (id_producto) REFERENCES Productos(id_producto))",

            "CREATE TABLE IF NOT EXISTS Pedidos (" +
                "id_pedido INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nit_cliente TEXT DEFAULT 'CF', " +
                "nombre_cliente TEXT NOT NULL, " +
                "fecha_pedido TEXT DEFAULT CURRENT_TIMESTAMP, " +
                "subtotal REAL NOT NULL DEFAULT 0.00, " +
                "descuento REAL DEFAULT 0.00, " +
                "total REAL NOT NULL DEFAULT 0.00, " +
                "estado TEXT DEFAULT 'Completado', " +
                "metodo_pago TEXT DEFAULT 'Efectivo', " +
                "observaciones TEXT)",

            "CREATE TABLE IF NOT EXISTS Detalle_Pedidos (" +
                "id_detalle_pedido INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "id_pedido INTEGER NOT NULL, " +
                "id_producto INTEGER NOT NULL, " +
                "cantidad INTEGER NOT NULL DEFAULT 1, " +
                "precio_unitario REAL NOT NULL, " +
                "descuento REAL DEFAULT 0.00, " +
                "subtotal REAL NOT NULL, " +
                "observaciones TEXT, " +
                "FOREIGN KEY (id_pedido) REFERENCES Pedidos(id_pedido) ON DELETE CASCADE, " +
                "FOREIGN KEY (id_producto) REFERENCES Productos(id_producto))",

            "CREATE TABLE IF NOT EXISTS Pagos (" +
                "id_pago INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "id_pedido INTEGER NOT NULL, " +
                "metodo_pago TEXT NOT NULL, " +
                "monto REAL NOT NULL, " +
                "fecha_pago TEXT DEFAULT CURRENT_TIMESTAMP, " +
                "estado TEXT DEFAULT 'Aprobado', " +
                "FOREIGN KEY (id_pedido) REFERENCES Pedidos(id_pedido) ON DELETE CASCADE)",

            "CREATE TABLE IF NOT EXISTS Promociones (" +
                "id_promocion INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nombre TEXT NOT NULL, " +
                "descripcion TEXT, " +
                "codigo TEXT NOT NULL UNIQUE, " +
                "tipo_descuento TEXT NOT NULL, " +
                "valor_descuento REAL NOT NULL, " +
                "fecha_inicio TEXT NOT NULL, " +
                "fecha_fin TEXT NOT NULL, " +
                "cantidad_minima INTEGER DEFAULT 5, " +
                "estado INTEGER DEFAULT 1)",

            "CREATE TABLE IF NOT EXISTS Producto_Promocion (" +
                "id_producto INTEGER NOT NULL, " +
                "id_promocion INTEGER NOT NULL, " +
                "PRIMARY KEY (id_producto, id_promocion), " +
                "FOREIGN KEY (id_producto) REFERENCES Productos(id_producto) ON DELETE CASCADE, " +
                "FOREIGN KEY (id_promocion) REFERENCES Promociones(id_promocion) ON DELETE CASCADE)",

            "CREATE TABLE IF NOT EXISTS Combos (" +
                "id_combo INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nombre TEXT NOT NULL, " +
                "descripcion TEXT, " +
                "precio REAL NOT NULL, " +
                "disponible INTEGER DEFAULT 1, " +
                "fecha_creacion TEXT DEFAULT CURRENT_TIMESTAMP, " +
                "fecha_actualizacion TEXT DEFAULT CURRENT_TIMESTAMP)",

            "CREATE TABLE IF NOT EXISTS Detalle_Combo (" +
                "id_combo INTEGER NOT NULL, " +
                "id_producto INTEGER NOT NULL, " +
                "cantidad INTEGER NOT NULL DEFAULT 5, " +
                "PRIMARY KEY (id_combo, id_producto), " +
                "FOREIGN KEY (id_combo) REFERENCES Combos(id_combo) ON DELETE CASCADE, " +
                "FOREIGN KEY (id_producto) REFERENCES Productos(id_producto))",

            // Datos iniciales (solo se insertan si todavía no existen)
            "INSERT INTO Roles (ID_Rol, Rol) " +
                "SELECT 1, 'Administrador' WHERE NOT EXISTS (SELECT 1 FROM Roles WHERE ID_Rol = 1)",
            "INSERT INTO Roles (ID_Rol, Rol) " +
                "SELECT 2, 'Cajero' WHERE NOT EXISTS (SELECT 1 FROM Roles WHERE ID_Rol = 2)",

            "INSERT INTO Usuarios (id_rol, Usuario, Password, Estado) " +
                "SELECT 1, 'admin', 'admin123', 1 WHERE NOT EXISTS (SELECT 1 FROM Usuarios WHERE Usuario = 'admin')",
            "INSERT INTO Usuarios (id_rol, Usuario, Password, Estado) " +
                "SELECT 2, 'caja', 'cajero123', 1 WHERE NOT EXISTS (SELECT 1 FROM Usuarios WHERE Usuario = 'caja')",

            "INSERT INTO Categorias (id_categoria, nombre, orden_visual) "
                + "SELECT 1, 'Desayunos', 1 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Categorias WHERE id_categoria = 1)",
            "INSERT INTO Categorias (id_categoria, nombre, orden_visual) "
                + "SELECT 2, 'Almuerzos y Cenas', 2 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Categorias WHERE id_categoria = 2)",
            "INSERT INTO Categorias (id_categoria, nombre, orden_visual) "
                + "SELECT 3, 'Antojos', 3 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Categorias WHERE id_categoria = 3)",
            "INSERT INTO Categorias (id_categoria, nombre, orden_visual) "
                + "SELECT 4, 'Bebidas', 4 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Categorias WHERE id_categoria = 4)",
            "INSERT INTO Categorias (id_categoria, nombre, orden_visual) "
                + "SELECT 5, 'Cafes y Tes', 5 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Categorias WHERE id_categoria = 5)",
            "INSERT INTO Categorias (id_categoria, nombre, orden_visual) "
                + "SELECT 6, 'Postres', 6 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Categorias WHERE id_categoria = 6)",
            "INSERT INTO Categorias (id_categoria, nombre, orden_visual) "
                + "SELECT 7, 'Cajita Wally', 7 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Categorias WHERE id_categoria = 7)",
            "INSERT INTO Categorias (id_categoria, nombre, orden_visual) "
                + "SELECT 8, 'Promociones', 8 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Categorias WHERE id_categoria = 8)",

            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 1, 'Hotcakes', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'Hotcakes')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 1, 'WallMurf Huevo y Frijol', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'WallMurf Huevo y Frijol')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 1, 'Wall Murf Super Chapín Con Salchicha', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'Wall Murf Super Chapín Con Salchicha')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 1, 'WallMurf Salchicha y doble huevo', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'WallMurf Salchicha y doble huevo')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 1, 'WallMurf Tocino Doble Huevo', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'WallMurf Tocino Doble Huevo')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 1, 'Egg WallMurf', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'Egg WallMurf')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 1, 'WallMurf Tocino Doble y Huevo', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'WallMurf Tocino Doble y Huevo')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 1, 'Desyuno Clásico', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'Desyuno Clásico')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 1, 'Desayuno Deluxe', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'Desayuno Deluxe')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 1, 'WallMurf de Salchicha Doble y Huevo', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'WallMurf de Salchicha Doble y Huevo')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 1, 'WallMurf Chapín Con Salchicha', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'WallMurf Chapín Con Salchicha')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 1, 'WallMurf Super Chapín Con Jamón', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'WallMurf Super Chapín Con Jamón')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 1, 'Desayuno Tradicional', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'Desayuno Tradicional')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 2, 'Big Wall', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'Big Wall')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 2, 'WallRoyal Bacon Doble con Queso', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'WallRoyal Bacon Doble con Queso')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 2, 'Big Wall Doble', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'Big Wall Doble')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 2, 'WallMax', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'WallMax')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 2, 'Quesoburquesa Doble', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'Quesoburquesa Doble')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 2, 'Quesoburquesa Triple', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'Quesoburquesa Triple')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 2, 'Sanwitch WallCrispy Doble', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'Sanwitch WallCrispy Doble')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 2, 'Gourmet Bacon', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'Gourmet Bacon')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 2, 'Gourmet Bacon Doble', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'Gourmet Bacon Doble')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 2, 'Wulpreme de Res', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'Wulpreme de Res')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 2, 'WallMax Doble', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'WallMax Doble')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 2, 'WallRoyal con Queso', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'WallRoyal con Queso')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 2, 'WallRoyal Doble con Queso', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'WallRoyal Doble con Queso')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 2, 'WallMax Bacon', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'WallMax Bacon')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 2, 'Wulpreme de Res Doble', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'Wulpreme de Res Doble')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 2, 'Gourmet Avocato', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'Gourmet Avocato')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 2, 'WallMax Bacon Doble', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'WallMax Bacon Doble')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 2, 'WallCrispy Chicken Bacon Ranch', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'WallCrispy Chicken Bacon Ranch')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 2, 'Gourmet Avotaco Doble', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'Gourmet Avotaco Doble')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 2, 'WallRoyal Bacon con Queso', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'WallRoyal Bacon con Queso')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 3, 'Papas', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'Papas')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 3, 'Hash brown', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'Hash brown')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 3, 'Derretido Clásico', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'Derretido Clásico')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 3, 'WallNuggets', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'WallNuggets')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 3, 'WacPatatas', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'WacPatatas')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 3, 'Tostado Queso y Tomate', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'Tostado Queso y Tomate')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 3, 'Tostado Queso y Jamon', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'Tostado Queso y Jamon')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 3, 'Tostado Queso y Frijol', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'Tostado Queso y Frijol')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 4, 'Cocacola Zero', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'Cocacola Zero')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 4, 'Rosa de Jamaica', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'Rosa de Jamaica')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 4, 'Smoothie Berries', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'Smoothie Berries')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 4, 'WallFrizz A.M', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'WallFrizz A.M')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 4, 'Sprite', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'Sprite')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 4, 'WallFrizz Blue', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'WallFrizz Blue')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 4, 'WallFrizz Green', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'WallFrizz Green')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 4, 'WallFrizz Pink', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'WallFrizz Pink')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 4, 'Jugo de Manzana', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'Jugo de Manzana')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 4, 'Agua Pura', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'Agua Pura')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 4, 'cocacola', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'cocacola')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 4, 'Jugo de Naranja', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'Jugo de Naranja')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 4, 'Fanta', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'Fanta')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 4, 'Smoothie Mango', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'Smoothie Mango')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 5, 'Frappe Oreo', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'Frappe Oreo')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 5, 'Te Chai Vainilla Light', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'Te Chai Vainilla Light')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 5, 'Cappuccino', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'Cappuccino')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 5, 'Cafe Guatemalteco', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'Cafe Guatemalteco')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 5, 'Frappe OG', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'Frappe OG')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 5, 'Frappe Chocolate', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'Frappe Chocolate')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 5, 'Frappe Caramelo', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'Frappe Caramelo')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 5, 'Chocolate Caliente', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'Chocolate Caliente')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 5, 'Te Chai Te Verde', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'Te Chai Te Verde')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 5, 'Te Chai OG', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'Te Chai OG')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 5, 'Te Chai Frappe Vainilla', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'Te Chai Frappe Vainilla')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 5, 'Te Chai Frappe Vainilla Light', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'Te Chai Frappe Vainilla Light')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 5, 'Te Chai Frappe Te Verde', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'Te Chai Frappe Te Verde')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 5, 'Te Chai Frappe OG', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'Te Chai Frappe OG')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 5, 'Latte', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'Latte')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 5, 'Iced Coffee Vainilla Light', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'Iced Coffee Vainilla Light')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 5, 'Iced Coffee OG', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'Iced Coffee OG')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 5, 'Te Guatemalteco Menta', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'Te Guatemalteco Menta')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 5, 'Iced Coffee Caramelo', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'Iced Coffee Caramelo')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 5, 'Te Guatemalteco Melocoton', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'Te Guatemalteco Melocoton')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 5, 'Iced Coffee Chocolate', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'Iced Coffee Chocolate')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 5, 'Te Guatemalteco Manzanilla', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'Te Guatemalteco Manzanilla')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 5, 'Frappe Vainilla', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'Frappe Vainilla')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 5, 'Te Guatemalteco Bora Bora', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'Te Guatemalteco Bora Bora')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 5, 'Frappe Vainilla Ligth', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'Frappe Vainilla Ligth')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 5, 'Te de Matcha', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'Te de Matcha')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 5, 'Te Chai Vainilla', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'Te Chai Vainilla')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 6, 'WalFlurry M&Mâ__s', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'WalFlurry M&Mâ__s')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 6, 'WalFlurry M&Mâ__s Caramelo', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'WalFlurry M&Mâ__s Caramelo')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 6, 'WalFlurry Oreo Chocolate', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'WalFlurry Oreo Chocolate')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 6, 'WalFlurry Oreo Caramelo', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'WalFlurry Oreo Caramelo')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 6, 'WalFlurry M&Mâ__s Fresa', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'WalFlurry M&Mâ__s Fresa')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 6, 'WalFlurry Oreo Fresa', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'WalFlurry Oreo Fresa')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 6, 'WalFlurry M&Mâ__s Chocolate', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'WalFlurry M&Mâ__s Chocolate')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 6, 'WalFlurry Oreo', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'WalFlurry Oreo')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 6, 'Pastel de Queso', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'Pastel de Queso')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 6, 'WallPop Chocolate', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'WallPop Chocolate')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 6, 'Brownies Bite', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'Brownies Bite')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 7, 'cajita wally frijol', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'cajita wally frijol')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 7, 'Cajita Wally de WacNuggets', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'Cajita Wally de WacNuggets')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 7, 'Cajita Wally de WacMuffin de Salchicha', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'Cajita Wally de WacMuffin de Salchicha')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 7, 'Cajita Wally de WacMuffin de Huevo y Queso', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'Cajita Wally de WacMuffin de Huevo y Queso')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 7, 'Cajita Wally de Quesoburguesa', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'Cajita Wally de Quesoburguesa')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 7, 'Cajita Wally de Quesoburguesa (1)', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'Cajita Wally de Quesoburguesa (1)')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 7, 'Cajita Wally de pollo', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'Cajita Wally de pollo')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 7, 'Cajita Wally de Hotcakes', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'Cajita Wally de Hotcakes')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 7, 'Cajita Wally de Hamburguesa Jr', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'Cajita Wally de Hamburguesa Jr')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 7, 'Cajita Wally de Derretido', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'Cajita Wally de Derretido')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 8, 'Caja Grande Desayuno', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'Caja Grande Desayuno')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 8, 'Caja Grande', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'Caja Grande')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 8, 'Caja Grande Deluxe', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'Caja Grande Deluxe')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 8, 'Caja Grande Snack', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'Caja Grande Snack')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 8, 'Caja Grande Con Postre', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'Caja Grande Con Postre')",
            "INSERT INTO Productos (id_categoria, nombre, precio) "
                + "SELECT 8, 'Caja de 24 WalNuggets', 0.00 WHERE NOT EXISTS "
                + "(SELECT 1 FROM Productos WHERE nombre = 'Caja de 24 WalNuggets')",

            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'Hotcakes' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'WallMurf Huevo y Frijol' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'Wall Murf Super Chapín Con Salchicha' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'WallMurf Salchicha y doble huevo' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'WallMurf Tocino Doble Huevo' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'Egg WallMurf' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'WallMurf Tocino Doble y Huevo' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'Desyuno Clásico' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'Desayuno Deluxe' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'WallMurf de Salchicha Doble y Huevo' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'WallMurf Chapín Con Salchicha' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'WallMurf Super Chapín Con Jamón' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'Desayuno Tradicional' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'Big Wall' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'WallRoyal Bacon Doble con Queso' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'Big Wall Doble' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'WallMax' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'Quesoburquesa Doble' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'Quesoburquesa Triple' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'Sanwitch WallCrispy Doble' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'Gourmet Bacon' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'Gourmet Bacon Doble' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'Wulpreme de Res' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'WallMax Doble' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'WallRoyal con Queso' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'WallRoyal Doble con Queso' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'WallMax Bacon' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'Wulpreme de Res Doble' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'Gourmet Avocato' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'WallMax Bacon Doble' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'WallCrispy Chicken Bacon Ranch' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'Gourmet Avotaco Doble' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'WallRoyal Bacon con Queso' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'Papas' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'Hash brown' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'Derretido Clásico' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'WallNuggets' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'WacPatatas' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'Tostado Queso y Tomate' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'Tostado Queso y Jamon' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'Tostado Queso y Frijol' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'Cocacola Zero' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'Rosa de Jamaica' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'Smoothie Berries' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'WallFrizz A.M' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'Sprite' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'WallFrizz Blue' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'WallFrizz Green' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'WallFrizz Pink' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'Jugo de Manzana' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'Agua Pura' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'cocacola' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'Jugo de Naranja' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'Fanta' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'Smoothie Mango' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'Frappe Oreo' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'Te Chai Vainilla Light' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'Cappuccino' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'Cafe Guatemalteco' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'Frappe OG' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'Frappe Chocolate' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'Frappe Caramelo' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'Chocolate Caliente' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'Te Chai Te Verde' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'Te Chai OG' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'Te Chai Frappe Vainilla' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'Te Chai Frappe Vainilla Light' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'Te Chai Frappe Te Verde' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'Te Chai Frappe OG' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'Latte' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'Iced Coffee Vainilla Light' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'Iced Coffee OG' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'Te Guatemalteco Menta' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'Iced Coffee Caramelo' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'Te Guatemalteco Melocoton' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'Iced Coffee Chocolate' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'Te Guatemalteco Manzanilla' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'Frappe Vainilla' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'Te Guatemalteco Bora Bora' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'Frappe Vainilla Ligth' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'Te de Matcha' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'Te Chai Vainilla' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'WalFlurry M&Mâ__s' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'WalFlurry M&Mâ__s Caramelo' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'WalFlurry Oreo Chocolate' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'WalFlurry Oreo Caramelo' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'WalFlurry M&Mâ__s Fresa' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'WalFlurry Oreo Fresa' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'WalFlurry M&Mâ__s Chocolate' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'WalFlurry Oreo' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'Pastel de Queso' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'WallPop Chocolate' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'Brownies Bite' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'cajita wally frijol' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'Cajita Wally de WacNuggets' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'Cajita Wally de WacMuffin de Salchicha' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'Cajita Wally de WacMuffin de Huevo y Queso' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'Cajita Wally de Quesoburguesa' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'Cajita Wally de Quesoburguesa (1)' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'Cajita Wally de pollo' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'Cajita Wally de Hotcakes' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'Cajita Wally de Hamburguesa Jr' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'Cajita Wally de Derretido' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'Caja Grande Desayuno' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'Caja Grande' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'Caja Grande Deluxe' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'Caja Grande Snack' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'Caja Grande Con Postre' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)",
            "INSERT INTO Inventario (id_producto, stock_actual) "
                + "SELECT id_producto, 20 FROM Productos WHERE nombre = 'Caja de 24 WalNuggets' "
                + "AND NOT EXISTS (SELECT 1 FROM Inventario i WHERE i.id_producto = Productos.id_producto)"
        };

        try (Statement st = conexion.createStatement()) {
            for (String sql : sentencias) {
                st.execute(sql);
            }
            System.out.println("Base de datos verificada/creada correctamente en " + RUTA_DB);
        } catch (SQLException e) {
            System.err.println("Error al inicializar la base de datos: " + e.getMessage());
        }
        actualizarPrecios();
    }

    /**
     * Asigna precio a los productos que todavía estén en Q0.00 (recién
     * creados). No pisa precios que ya hayas modificado a mano.
     */
    private static void actualizarPrecios() {
        java.util.Map<String, Double> precios = new java.util.LinkedHashMap<>();

        // Desayunos
        precios.put("Hotcakes", 22.00);
        precios.put("WallMurf Huevo y Frijol", 24.00);
        precios.put("Wall Murf Super Chapín Con Salchicha", 32.00);
        precios.put("WallMurf Salchicha y doble huevo", 30.00);
        precios.put("WallMurf Tocino Doble Huevo", 33.00);
        precios.put("Egg WallMurf", 20.00);
        precios.put("WallMurf Tocino Doble y Huevo", 33.00);
        precios.put("Desyuno Clásico", 28.00);
        precios.put("Desayuno Deluxe", 38.00);
        precios.put("WallMurf de Salchicha Doble y Huevo", 30.00);
        precios.put("WallMurf Chapín Con Salchicha", 28.00);
        precios.put("WallMurf Super Chapín Con Jamón", 30.00);
        precios.put("Desayuno Tradicional", 26.00);

        // Almuerzos y Cenas
        precios.put("Big Wall", 38.00);
        precios.put("WallRoyal Bacon Doble con Queso", 52.00);
        precios.put("Big Wall Doble", 46.00);
        precios.put("WallMax", 34.00);
        precios.put("Quesoburquesa Doble", 36.00);
        precios.put("Quesoburquesa Triple", 42.00);
        precios.put("Sanwitch WallCrispy Doble", 40.00);
        precios.put("Gourmet Bacon", 44.00);
        precios.put("Gourmet Bacon Doble", 50.00);
        precios.put("Wulpreme de Res", 48.00);
        precios.put("WallMax Doble", 40.00);
        precios.put("WallRoyal con Queso", 42.00);
        precios.put("WallRoyal Doble con Queso", 48.00);
        precios.put("WallMax Bacon", 40.00);
        precios.put("Wulpreme de Res Doble", 54.00);
        precios.put("Gourmet Avocato", 46.00);
        precios.put("WallMax Bacon Doble", 46.00);
        precios.put("WallCrispy Chicken Bacon Ranch", 42.00);
        precios.put("Gourmet Avotaco Doble", 50.00);
        precios.put("WallRoyal Bacon con Queso", 46.00);

        // Antojos
        precios.put("Papas", 14.00);
        precios.put("Hash brown", 12.00);
        precios.put("Derretido Clásico", 24.00);
        precios.put("WallNuggets", 22.00);
        precios.put("WacPatatas", 16.00);
        precios.put("Tostado Queso y Tomate", 20.00);
        precios.put("Tostado Queso y Jamon", 22.00);
        precios.put("Tostado Queso y Frijol", 20.00);

        // Bebidas
        precios.put("Cocacola Zero", 12.00);
        precios.put("Rosa de Jamaica", 14.00);
        precios.put("Smoothie Berries", 20.00);
        precios.put("WallFrizz A.M", 15.00);
        precios.put("Sprite", 12.00);
        precios.put("WallFrizz Blue", 15.00);
        precios.put("WallFrizz Green", 15.00);
        precios.put("WallFrizz Pink", 15.00);
        precios.put("Jugo de Manzana", 13.00);
        precios.put("Agua Pura", 8.00);
        precios.put("cocacola", 12.00);
        precios.put("Jugo de Naranja", 13.00);
        precios.put("Fanta", 12.00);
        precios.put("Smoothie Mango", 20.00);

        // Cafes y Tes
        precios.put("Frappe Oreo", 24.00);
        precios.put("Te Chai Vainilla Light", 18.00);
        precios.put("Cappuccino", 18.00);
        precios.put("Cafe Guatemalteco", 14.00);
        precios.put("Frappe OG", 22.00);
        precios.put("Frappe Chocolate", 24.00);
        precios.put("Frappe Caramelo", 24.00);
        precios.put("Chocolate Caliente", 16.00);
        precios.put("Te Chai Te Verde", 18.00);
        precios.put("Te Chai OG", 18.00);
        precios.put("Te Chai Frappe Vainilla", 22.00);
        precios.put("Te Chai Frappe Vainilla Light", 22.00);
        precios.put("Te Chai Frappe Te Verde", 22.00);
        precios.put("Te Chai Frappe OG", 22.00);
        precios.put("Latte", 16.00);
        precios.put("Iced Coffee Vainilla Light", 18.00);
        precios.put("Iced Coffee OG", 18.00);
        precios.put("Te Guatemalteco Menta", 12.00);
        precios.put("Iced Coffee Caramelo", 18.00);
        precios.put("Te Guatemalteco Melocoton", 12.00);
        precios.put("Iced Coffee Chocolate", 18.00);
        precios.put("Te Guatemalteco Manzanilla", 12.00);
        precios.put("Frappe Vainilla", 22.00);
        precios.put("Te Guatemalteco Bora Bora", 12.00);
        precios.put("Frappe Vainilla Ligth", 22.00);
        precios.put("Te de Matcha", 20.00);
        precios.put("Te Chai Vainilla", 18.00);

        // Postres
        precios.put("WalFlurry M&Mâ__s", 14.00);
        precios.put("WalFlurry M&Mâ__s Caramelo", 16.00);
        precios.put("WalFlurry Oreo Chocolate", 16.00);
        precios.put("WalFlurry Oreo Caramelo", 16.00);
        precios.put("WalFlurry M&Mâ__s Fresa", 16.00);
        precios.put("WalFlurry Oreo Fresa", 16.00);
        precios.put("WalFlurry M&Mâ__s Chocolate", 16.00);
        precios.put("WalFlurry Oreo", 14.00);
        precios.put("Pastel de Queso", 18.00);
        precios.put("WallPop Chocolate", 10.00);
        precios.put("Brownies Bite", 12.00);

        // Cajita Wally
        precios.put("cajita wally frijol", 24.00);
        precios.put("Cajita Wally de WacNuggets", 26.00);
        precios.put("Cajita Wally de WacMuffin de Salchicha", 26.00);
        precios.put("Cajita Wally de WacMuffin de Huevo y Queso", 26.00);
        precios.put("Cajita Wally de Quesoburguesa", 28.00);
        precios.put("Cajita Wally de Quesoburguesa (1)", 28.00);
        precios.put("Cajita Wally de pollo", 30.00);
        precios.put("Cajita Wally de Hotcakes", 24.00);
        precios.put("Cajita Wally de Hamburguesa Jr", 26.00);
        precios.put("Cajita Wally de Derretido", 26.00);

        // Cajas Grandes / Promociones
        precios.put("Caja Grande Desayuno", 45.00);
        precios.put("Caja Grande", 55.00);
        precios.put("Caja Grande Deluxe", 65.00);
        precios.put("Caja Grande Snack", 40.00);
        precios.put("Caja Grande Con Postre", 60.00);
        precios.put("Caja de 24 WalNuggets", 50.00);

        String sql = "UPDATE Productos SET precio = ? WHERE nombre = ? AND precio = 0.00";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            for (java.util.Map.Entry<String, Double> e : precios.entrySet()) {
                ps.setDouble(1, e.getValue());
                ps.setString(2, e.getKey());
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) {
            System.err.println("Error al actualizar precios: " + e.getMessage());
        }
    }

    /**
     * Agrega columnas que no existían cuando se crearon las tablas la
     * primera vez (SQLite no falla si ya existen: el error se ignora).
     */
    private static void migrarColumnasNuevas() {
        String[] alters = {
            "ALTER TABLE Productos ADD COLUMN imagen TEXT"
        };
        for (String sql : alters) {
            try (Statement st = conexion.createStatement()) {
                st.execute(sql);
            } catch (SQLException ignored) {
                // La columna ya existe, no hay nada que hacer.
            }
        }
    }

    // =====================================================================
    //  CLASES Y MÉTODOS DE APOYO PARA EL PANEL DE ADMINISTRADOR
    //  (Ver menú, Registrar/Consultar/Modificar pedidos, Registrar pagos,
    //  Crear/Modificar/Eliminar productos, Categorías, Inventario,
    //  Promociones y Cajeros). Agregado 100% por código.
    // =====================================================================

    /** Info de una categoría junto con la cantidad de productos que tiene. */
    public static class CategoriaInfo {
        public final int idCategoria;
        public final String nombre;
        public final String descripcion;
        public final int cantidadProductos;

        public CategoriaInfo(int idCategoria, String nombre, String descripcion, int cantidadProductos) {
            this.idCategoria = idCategoria;
            this.nombre = nombre;
            this.descripcion = descripcion;
            this.cantidadProductos = cantidadProductos;
        }
    }

    /** Info completa de un producto (para listar / editar / almacén). */
    public static class ProductoInfo {
        public final int idProducto;
        public final int idCategoria;
        public final String categoria;
        public final String nombre;
        public final String descripcion;
        public final double precio;
        public final String imagen;
        public final boolean disponible;
        public final int stock;

        public ProductoInfo(int idProducto, int idCategoria, String categoria, String nombre,
                String descripcion, double precio, String imagen, boolean disponible, int stock) {
            this.idProducto = idProducto;
            this.idCategoria = idCategoria;
            this.categoria = categoria;
            this.nombre = nombre;
            this.descripcion = descripcion;
            this.precio = precio;
            this.imagen = imagen;
            this.disponible = disponible;
            this.stock = stock;
        }
    }

    /** Fila de un pedido, usada en Consultar / Modificar pedidos y Pagos. */
    public static class PedidoInfo {
        public final int idPedido;
        public final String cliente;
        public final String fecha;
        public final double total;
        public final String estado;
        public final String metodoPago;
        public final boolean pagado;

        public PedidoInfo(int idPedido, String cliente, String fecha, double total,
                String estado, String metodoPago, boolean pagado) {
            this.idPedido = idPedido;
            this.cliente = cliente;
            this.fecha = fecha;
            this.total = total;
            this.estado = estado;
            this.metodoPago = metodoPago;
            this.pagado = pagado;
        }
    }

    /** Línea de detalle de un pedido ya guardado (producto + cantidad). */
    public static class DetallePedidoInfo {
        public final int idDetalle;
        public final int idProducto;
        public final String nombreProducto;
        public final int cantidad;
        public final double precioUnitario;
        public final double subtotal;

        public DetallePedidoInfo(int idDetalle, int idProducto, String nombreProducto,
                int cantidad, double precioUnitario, double subtotal) {
            this.idDetalle = idDetalle;
            this.idProducto = idProducto;
            this.nombreProducto = nombreProducto;
            this.cantidad = cantidad;
            this.precioUnitario = precioUnitario;
            this.subtotal = subtotal;
        }
    }

    /** Ítem para armar un pedido nuevo o reemplazar el de uno existente. */
    public static class ItemPedidoNuevo {
        public final int idProducto;
        public final String nombreProducto;
        public final int cantidad;
        public final double precioUnitario;

        public ItemPedidoNuevo(int idProducto, String nombreProducto, int cantidad, double precioUnitario) {
            this.idProducto = idProducto;
            this.nombreProducto = nombreProducto;
            this.cantidad = cantidad;
            this.precioUnitario = precioUnitario;
        }

        public double getSubtotal() {
            return cantidad * precioUnitario;
        }
    }

    /** Fila de inventario (producto + stock), para ordenar de menor a mayor. */
    public static class InventarioInfo {
        public final int idProducto;
        public final String nombreProducto;
        public final String categoria;
        public final int stockActual;
        public final int stockMinimo;

        public InventarioInfo(int idProducto, String nombreProducto, String categoria,
                int stockActual, int stockMinimo) {
            this.idProducto = idProducto;
            this.nombreProducto = nombreProducto;
            this.categoria = categoria;
            this.stockActual = stockActual;
            this.stockMinimo = stockMinimo;
        }
    }

    /** Fila de una promoción. */
    public static class PromocionInfo {
        public final int idPromocion;
        public final String nombre;
        public final String descripcion;
        public final String codigo;
        public final String tipoDescuento;
        public final double valorDescuento;
        public final String fechaInicio;
        public final String fechaFin;
        public final int cantidadMinima;
        public final boolean activa;

        public PromocionInfo(int idPromocion, String nombre, String descripcion, String codigo,
                String tipoDescuento, double valorDescuento, String fechaInicio, String fechaFin,
                int cantidadMinima, boolean activa) {
            this.idPromocion = idPromocion;
            this.nombre = nombre;
            this.descripcion = descripcion;
            this.codigo = codigo;
            this.tipoDescuento = tipoDescuento;
            this.valorDescuento = valorDescuento;
            this.fechaInicio = fechaInicio;
            this.fechaFin = fechaFin;
            this.cantidadMinima = cantidadMinima;
            this.activa = activa;
        }

        /** Días que quedan de vigencia (negativo si ya expiró, Long.MIN_VALUE si no se pudo calcular). */
        public long diasRestantes() {
            try {
                java.time.LocalDate fin = java.time.LocalDate.parse(fechaFin.trim().substring(0, 10));
                return java.time.temporal.ChronoUnit.DAYS.between(java.time.LocalDate.now(), fin);
            } catch (Exception e) {
                return Long.MIN_VALUE;
            }
        }
    }

    /** Fila de un pago registrado. */
    public static class PagoInfo {
        public final int idPago;
        public final int idPedido;
        public final String metodoPago;
        public final double monto;
        public final String fecha;
        public final String estado;

        public PagoInfo(int idPago, int idPedido, String metodoPago, double monto, String fecha, String estado) {
            this.idPago = idPago;
            this.idPedido = idPedido;
            this.metodoPago = metodoPago;
            this.monto = monto;
            this.fecha = fecha;
            this.estado = estado;
        }
    }

    /** Fila de un usuario (administrador o cajero). */
    public static class UsuarioInfo {
        public final int idUsuario;
        public final String usuario;
        public final String rol;
        public final boolean activo;

        public UsuarioInfo(int idUsuario, String usuario, String rol, boolean activo) {
            this.idUsuario = idUsuario;
            this.usuario = usuario;
            this.rol = rol;
            this.activo = activo;
        }
    }

    // ------------------------- CATEGORÍAS -------------------------

    public static List<CategoriaInfo> listarCategorias() {
        List<CategoriaInfo> lista = new java.util.ArrayList<>();
        String sql = "SELECT c.id_categoria, c.nombre, c.descripcion, "
                + "(SELECT COUNT(*) FROM Productos p WHERE p.id_categoria = c.id_categoria AND p.disponible = 1) AS total "
                + "FROM Categorias c WHERE c.estado = 1 ORDER BY c.orden_visual";
        try (Statement st = getConexion().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new CategoriaInfo(rs.getInt("id_categoria"), rs.getString("nombre"),
                        rs.getString("descripcion"), rs.getInt("total")));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar categorías: " + e.getMessage());
        }
        return lista;
    }

    // ------------------------- PRODUCTOS -------------------------

    public static List<ProductoInfo> listarProductos(boolean soloDisponibles) {
        List<ProductoInfo> lista = new java.util.ArrayList<>();
        String sql = "SELECT p.id_producto, p.id_categoria, c.nombre AS categoria, p.nombre, p.descripcion, "
                + "p.precio, p.imagen, p.disponible, COALESCE(i.stock_actual, 0) AS stock "
                + "FROM Productos p "
                + "JOIN Categorias c ON c.id_categoria = p.id_categoria "
                + "LEFT JOIN Inventario i ON i.id_producto = p.id_producto "
                + (soloDisponibles ? "WHERE p.disponible = 1 " : "")
                + "ORDER BY c.orden_visual, p.nombre";
        try (Statement st = getConexion().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new ProductoInfo(rs.getInt("id_producto"), rs.getInt("id_categoria"),
                        rs.getString("categoria"), rs.getString("nombre"), rs.getString("descripcion"),
                        rs.getDouble("precio"), rs.getString("imagen"), rs.getInt("disponible") == 1,
                        rs.getInt("stock")));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar productos: " + e.getMessage());
        }
        return lista;
    }

    public static boolean crearProducto(int idCategoria, String nombre, String descripcion,
            double precio, int stockInicial) {
        Connection con = getConexion();
        try {
            con.setAutoCommit(false);
            int idProducto;
            String sqlProd = "INSERT INTO Productos (id_categoria, nombre, descripcion, precio) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = con.prepareStatement(sqlProd, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, idCategoria);
                ps.setString(2, nombre);
                ps.setString(3, descripcion);
                ps.setDouble(4, precio);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    rs.next();
                    idProducto = rs.getInt(1);
                }
            }
            String sqlInv = "INSERT INTO Inventario (id_producto, stock_actual) VALUES (?, ?)";
            try (PreparedStatement ps = con.prepareStatement(sqlInv)) {
                ps.setInt(1, idProducto);
                ps.setInt(2, Math.max(stockInicial, 0));
                ps.executeUpdate();
            }
            con.commit();
            return true;
        } catch (SQLException e) {
            try {
                con.rollback();
            } catch (SQLException ignored) {
            }
            System.err.println("Error al crear producto: " + e.getMessage());
            return false;
        } finally {
            try {
                con.setAutoCommit(true);
            } catch (SQLException ignored) {
            }
        }
    }

    public static boolean actualizarProducto(int idProducto, String nombre, String descripcion,
            double precio, String imagen) {
        String sql = "UPDATE Productos SET nombre = ?, descripcion = ?, precio = ?, imagen = ?, "
                + "fecha_actualizacion = CURRENT_TIMESTAMP WHERE id_producto = ?";
        try (PreparedStatement ps = getConexion().prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.setString(2, descripcion);
            ps.setDouble(3, precio);
            ps.setString(4, imagen);
            ps.setInt(5, idProducto);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar producto: " + e.getMessage());
            return false;
        }
    }

    /** Elimina (baja lógica) un producto: deja de mostrarse en el menú y en los listados. */
    public static boolean eliminarProducto(int idProducto) {
        String sql = "UPDATE Productos SET disponible = 0 WHERE id_producto = ?";
        try (PreparedStatement ps = getConexion().prepareStatement(sql)) {
            ps.setInt(1, idProducto);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al eliminar producto: " + e.getMessage());
            return false;
        }
    }

    /** Suma (delta positivo) o resta (delta negativo) unidades al stock de un producto. */
    public static boolean ajustarStock(int idProducto, int delta) {
        Connection con = getConexion();
        try {
            Integer actual = null;
            String sqlSel = "SELECT stock_actual FROM Inventario WHERE id_producto = ?";
            try (PreparedStatement ps = con.prepareStatement(sqlSel)) {
                ps.setInt(1, idProducto);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        actual = rs.getInt("stock_actual");
                    }
                }
            }
            if (actual == null) {
                String sqlIns = "INSERT INTO Inventario (id_producto, stock_actual) VALUES (?, ?)";
                try (PreparedStatement ps = con.prepareStatement(sqlIns)) {
                    ps.setInt(1, idProducto);
                    ps.setInt(2, Math.max(delta, 0));
                    ps.executeUpdate();
                }
                return true;
            }
            int nuevo = Math.max(actual + delta, 0);
            String sqlUpd = "UPDATE Inventario SET stock_actual = ?, ultima_actualizacion = CURRENT_TIMESTAMP "
                    + "WHERE id_producto = ?";
            try (PreparedStatement ps = con.prepareStatement(sqlUpd)) {
                ps.setInt(1, nuevo);
                ps.setInt(2, idProducto);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error al ajustar stock: " + e.getMessage());
            return false;
        }
    }

    // ------------------------- INVENTARIO -------------------------

    /** Devuelve el inventario ordenado de MENOR a MAYOR cantidad de stock. */
    public static List<InventarioInfo> listarInventarioOrdenado() {
        List<InventarioInfo> lista = new java.util.ArrayList<>();
        String sql = "SELECT p.id_producto, p.nombre, c.nombre AS categoria, "
                + "COALESCE(i.stock_actual, 0) AS stock, COALESCE(i.stock_minimo, 10) AS minimo "
                + "FROM Productos p "
                + "JOIN Categorias c ON c.id_categoria = p.id_categoria "
                + "LEFT JOIN Inventario i ON i.id_producto = p.id_producto "
                + "WHERE p.disponible = 1 "
                + "ORDER BY stock ASC, p.nombre ASC";
        try (Statement st = getConexion().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new InventarioInfo(rs.getInt("id_producto"), rs.getString("nombre"),
                        rs.getString("categoria"), rs.getInt("stock"), rs.getInt("minimo")));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar inventario: " + e.getMessage());
        }
        return lista;
    }

    // ------------------------- PEDIDOS -------------------------

    public static List<PedidoInfo> listarPedidos() {
        List<PedidoInfo> lista = new java.util.ArrayList<>();
        String sql = "SELECT p.id_pedido, p.nombre_cliente, p.fecha_pedido, p.total, p.estado, p.metodo_pago, "
                + "(SELECT COUNT(*) FROM Pagos pa WHERE pa.id_pedido = p.id_pedido) AS pagos "
                + "FROM Pedidos p ORDER BY p.id_pedido DESC";
        try (Statement st = getConexion().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new PedidoInfo(rs.getInt("id_pedido"), rs.getString("nombre_cliente"),
                        rs.getString("fecha_pedido"), rs.getDouble("total"), rs.getString("estado"),
                        rs.getString("metodo_pago"), rs.getInt("pagos") > 0));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar pedidos: " + e.getMessage());
        }
        return lista;
    }

    /** Pedidos que todavía no tienen un pago registrado (para "Registrar pagos"). */
    public static List<PedidoInfo> listarPedidosSinPago() {
        List<PedidoInfo> lista = new java.util.ArrayList<>();
        for (PedidoInfo p : listarPedidos()) {
            if (!p.pagado) {
                lista.add(p);
            }
        }
        return lista;
    }

    public static List<DetallePedidoInfo> listarDetallePedido(int idPedido) {
        List<DetallePedidoInfo> lista = new java.util.ArrayList<>();
        String sql = "SELECT d.id_detalle_pedido, d.id_producto, p.nombre, d.cantidad, d.precio_unitario, d.subtotal "
                + "FROM Detalle_Pedidos d JOIN Productos p ON p.id_producto = d.id_producto "
                + "WHERE d.id_pedido = ?";
        try (PreparedStatement ps = getConexion().prepareStatement(sql)) {
            ps.setInt(1, idPedido);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new DetallePedidoInfo(rs.getInt("id_detalle_pedido"), rs.getInt("id_producto"),
                            rs.getString("nombre"), rs.getInt("cantidad"), rs.getDouble("precio_unitario"),
                            rs.getDouble("subtotal")));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al listar detalle del pedido " + idPedido + ": " + e.getMessage());
        }
        return lista;
    }

    /**
     * Registra un nuevo pedido (panel "Registrar pedidos"), descontando
     * inventario. Devuelve el id del pedido generado, o -1 si no había
     * stock suficiente o hubo un error (no se aplica ningún cambio).
     */
    public static int registrarPedido(String nombreCliente, List<ItemPedidoNuevo> items, String metodoPago) {
        Connection con = getConexion();
        try {
            con.setAutoCommit(false);
            for (ItemPedidoNuevo item : items) {
                int disponible = obtenerStockDisponible(con, item.idProducto);
                if (disponible < item.cantidad) {
                    con.rollback();
                    return -1;
                }
            }
            double total = 0;
            for (ItemPedidoNuevo item : items) {
                total += item.getSubtotal();
            }
            String cliente = (nombreCliente == null || nombreCliente.trim().isEmpty())
                    ? "Cliente Mostrador" : nombreCliente.trim();

            int idPedido;
            String sqlPedido = "INSERT INTO Pedidos (nombre_cliente, subtotal, total, metodo_pago) "
                    + "VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = con.prepareStatement(sqlPedido, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, cliente);
                ps.setDouble(2, total);
                ps.setDouble(3, total);
                ps.setString(4, metodoPago);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    rs.next();
                    idPedido = rs.getInt(1);
                }
            }
            String sqlDetalle = "INSERT INTO Detalle_Pedidos (id_pedido, id_producto, cantidad, precio_unitario, subtotal) "
                    + "VALUES (?, ?, ?, ?, ?)";
            String sqlDescuento = "UPDATE Inventario SET stock_actual = stock_actual - ?, "
                    + "ultima_actualizacion = CURRENT_TIMESTAMP WHERE id_producto = ?";
            try (PreparedStatement psDetalle = con.prepareStatement(sqlDetalle);
                 PreparedStatement psDescuento = con.prepareStatement(sqlDescuento)) {
                for (ItemPedidoNuevo item : items) {
                    psDetalle.setInt(1, idPedido);
                    psDetalle.setInt(2, item.idProducto);
                    psDetalle.setInt(3, item.cantidad);
                    psDetalle.setDouble(4, item.precioUnitario);
                    psDetalle.setDouble(5, item.getSubtotal());
                    psDetalle.executeUpdate();

                    psDescuento.setInt(1, item.cantidad);
                    psDescuento.setInt(2, item.idProducto);
                    psDescuento.executeUpdate();
                }
            }
            con.commit();
            return idPedido;
        } catch (SQLException e) {
            try {
                con.rollback();
            } catch (SQLException ignored) {
            }
            System.err.println("Error al registrar pedido: " + e.getMessage());
            return -1;
        } finally {
            try {
                con.setAutoCommit(true);
            } catch (SQLException ignored) {
            }
        }
    }

    /**
     * Reemplaza el detalle de un pedido existente por uno nuevo (panel
     * "Modificar pedidos"): repone el inventario de los productos
     * anteriores, descuenta el de los nuevos y recalcula el total.
     * Devuelve false si no había stock suficiente para los nuevos productos
     * (en ese caso no se aplica ningún cambio).
     */
    public static boolean modificarPedido(int idPedido, List<ItemPedidoNuevo> nuevosItems) {
        Connection con = getConexion();
        try {
            con.setAutoCommit(false);

            String sqlActuales = "SELECT id_producto, cantidad FROM Detalle_Pedidos WHERE id_pedido = ?";
            try (PreparedStatement ps = con.prepareStatement(sqlActuales)) {
                ps.setInt(1, idPedido);
                try (ResultSet rs = ps.executeQuery()) {
                    String sqlRepone = "UPDATE Inventario SET stock_actual = stock_actual + ? WHERE id_producto = ?";
                    try (PreparedStatement psRepone = con.prepareStatement(sqlRepone)) {
                        while (rs.next()) {
                            psRepone.setInt(1, rs.getInt("cantidad"));
                            psRepone.setInt(2, rs.getInt("id_producto"));
                            psRepone.executeUpdate();
                        }
                    }
                }
            }

            for (ItemPedidoNuevo item : nuevosItems) {
                int disponible = obtenerStockDisponible(con, item.idProducto);
                if (disponible < item.cantidad) {
                    con.rollback();
                    return false;
                }
            }

            try (PreparedStatement ps = con.prepareStatement("DELETE FROM Detalle_Pedidos WHERE id_pedido = ?")) {
                ps.setInt(1, idPedido);
                ps.executeUpdate();
            }

            double total = 0;
            String sqlDetalle = "INSERT INTO Detalle_Pedidos (id_pedido, id_producto, cantidad, precio_unitario, subtotal) "
                    + "VALUES (?, ?, ?, ?, ?)";
            String sqlDescuento = "UPDATE Inventario SET stock_actual = stock_actual - ? WHERE id_producto = ?";
            try (PreparedStatement psDetalle = con.prepareStatement(sqlDetalle);
                 PreparedStatement psDescuento = con.prepareStatement(sqlDescuento)) {
                for (ItemPedidoNuevo item : nuevosItems) {
                    psDetalle.setInt(1, idPedido);
                    psDetalle.setInt(2, item.idProducto);
                    psDetalle.setInt(3, item.cantidad);
                    psDetalle.setDouble(4, item.precioUnitario);
                    psDetalle.setDouble(5, item.getSubtotal());
                    psDetalle.executeUpdate();

                    psDescuento.setInt(1, item.cantidad);
                    psDescuento.setInt(2, item.idProducto);
                    psDescuento.executeUpdate();

                    total += item.getSubtotal();
                }
            }

            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE Pedidos SET subtotal = ?, total = ? WHERE id_pedido = ?")) {
                ps.setDouble(1, total);
                ps.setDouble(2, total);
                ps.setInt(3, idPedido);
                ps.executeUpdate();
            }

            con.commit();
            return true;
        } catch (SQLException e) {
            try {
                con.rollback();
            } catch (SQLException ignored) {
            }
            System.err.println("Error al modificar pedido " + idPedido + ": " + e.getMessage());
            return false;
        } finally {
            try {
                con.setAutoCommit(true);
            } catch (SQLException ignored) {
            }
        }
    }

    // ------------------------- PAGOS -------------------------

    public static boolean registrarPago(int idPedido, String metodoPago, double monto) {
        String sql = "INSERT INTO Pagos (id_pedido, metodo_pago, monto) VALUES (?, ?, ?)";
        try (PreparedStatement ps = getConexion().prepareStatement(sql)) {
            ps.setInt(1, idPedido);
            ps.setString(2, metodoPago);
            ps.setDouble(3, monto);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al registrar pago: " + e.getMessage());
            return false;
        }
    }

    public static List<PagoInfo> listarPagos() {
        List<PagoInfo> lista = new java.util.ArrayList<>();
        String sql = "SELECT id_pago, id_pedido, metodo_pago, monto, fecha_pago, estado "
                + "FROM Pagos ORDER BY id_pago DESC";
        try (Statement st = getConexion().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new PagoInfo(rs.getInt("id_pago"), rs.getInt("id_pedido"), rs.getString("metodo_pago"),
                        rs.getDouble("monto"), rs.getString("fecha_pago"), rs.getString("estado")));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar pagos: " + e.getMessage());
        }
        return lista;
    }

    // ------------------------- PROMOCIONES -------------------------

    public static List<PromocionInfo> listarPromociones() {
        List<PromocionInfo> lista = new java.util.ArrayList<>();
        String sql = "SELECT id_promocion, nombre, descripcion, codigo, tipo_descuento, valor_descuento, "
                + "fecha_inicio, fecha_fin, cantidad_minima, estado FROM Promociones ORDER BY id_promocion DESC";
        try (Statement st = getConexion().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new PromocionInfo(rs.getInt("id_promocion"), rs.getString("nombre"),
                        rs.getString("descripcion"), rs.getString("codigo"), rs.getString("tipo_descuento"),
                        rs.getDouble("valor_descuento"), rs.getString("fecha_inicio"), rs.getString("fecha_fin"),
                        rs.getInt("cantidad_minima"), rs.getInt("estado") == 1));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar promociones: " + e.getMessage());
        }
        return lista;
    }

    public static boolean crearPromocion(String nombre, String descripcion, String codigo, String tipoDescuento,
            double valorDescuento, String fechaInicio, String fechaFin, int cantidadMinima) {
        String sql = "INSERT INTO Promociones (nombre, descripcion, codigo, tipo_descuento, valor_descuento, "
                + "fecha_inicio, fecha_fin, cantidad_minima) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConexion().prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.setString(2, descripcion);
            ps.setString(3, codigo);
            ps.setString(4, tipoDescuento);
            ps.setDouble(5, valorDescuento);
            ps.setString(6, fechaInicio);
            ps.setString(7, fechaFin);
            ps.setInt(8, cantidadMinima);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al crear promoción: " + e.getMessage());
            return false;
        }
    }

    public static boolean cambiarEstadoPromocion(int idPromocion, boolean activa) {
        String sql = "UPDATE Promociones SET estado = ? WHERE id_promocion = ?";
        try (PreparedStatement ps = getConexion().prepareStatement(sql)) {
            ps.setInt(1, activa ? 1 : 0);
            ps.setInt(2, idPromocion);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al cambiar estado de la promoción: " + e.getMessage());
            return false;
        }
    }

    // ------------------------- USUARIOS / CAJEROS -------------------------

    public static java.util.Map<Integer, String> listarRoles() {
        java.util.Map<Integer, String> mapa = new java.util.LinkedHashMap<>();
        try (Statement st = getConexion().createStatement();
             ResultSet rs = st.executeQuery("SELECT ID_Rol, Rol FROM Roles ORDER BY ID_Rol")) {
            while (rs.next()) {
                mapa.put(rs.getInt("ID_Rol"), rs.getString("Rol"));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar roles: " + e.getMessage());
        }
        return mapa;
    }

    public static List<UsuarioInfo> listarUsuarios() {
        List<UsuarioInfo> lista = new java.util.ArrayList<>();
        String sql = "SELECT u.ID_Usuario, u.Usuario, r.Rol, u.Estado FROM Usuarios u "
                + "JOIN Roles r ON r.ID_Rol = u.id_rol ORDER BY u.ID_Usuario";
        try (Statement st = getConexion().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new UsuarioInfo(rs.getInt("ID_Usuario"), rs.getString("Usuario"),
                        rs.getString("Rol"), rs.getInt("Estado") == 1));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar usuarios: " + e.getMessage());
        }
        return lista;
    }

    /** true si se creó, false si el usuario ya existía o hubo un error. */
    public static boolean crearCajero(String usuario, String password, int idRol) {
        String sqlExiste = "SELECT 1 FROM Usuarios WHERE Usuario = ?";
        try (PreparedStatement ps = getConexion().prepareStatement(sqlExiste)) {
            ps.setString(1, usuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return false;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al validar usuario: " + e.getMessage());
            return false;
        }
        String sql = "INSERT INTO Usuarios (id_rol, Usuario, Password, Estado) VALUES (?, ?, ?, 1)";
        try (PreparedStatement ps = getConexion().prepareStatement(sql)) {
            ps.setInt(1, idRol);
            ps.setString(2, usuario);
            ps.setString(3, password);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al crear cajero: " + e.getMessage());
            return false;
        }
    }

    /**
     * Busca un producto por su nombre exacto (columna 'nombre' de Productos).
     * Devuelve null si no existe o no está disponible.
     */
    public static ProductoDB buscarProductoPorNombre(String nombre) {
        String sql = "SELECT id_producto, nombre, precio FROM Productos WHERE nombre = ? AND disponible = 1";
        try (PreparedStatement ps = getConexion().prepareStatement(sql)) {
            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new ProductoDB(rs.getInt("id_producto"), rs.getString("nombre"), rs.getDouble("precio"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar producto \"" + nombre + "\": " + e.getMessage());
        }
        return null;
    }

    /** Devuelve el stock actual de un producto en Inventario (0 si no se encuentra). */
    public static int obtenerStockDisponible(int idProducto) {
        try {
            return obtenerStockDisponible(getConexion(), idProducto);
        } catch (SQLException e) {
            System.err.println("Error al consultar stock del producto " + idProducto + ": " + e.getMessage());
            return 0;
        }
    }

    private static int obtenerStockDisponible(Connection con, int idProducto) throws SQLException {
        String sql = "SELECT stock_actual FROM Inventario WHERE id_producto = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idProducto);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("stock_actual") : 0;
            }
        }
    }

    /**
     * Procesa una compra de forma transaccional: valida que haya stock
     * suficiente de TODOS los productos, registra el pedido y su detalle,
     * y descuenta el inventario. Si algún producto no tiene stock
     * suficiente, no se aplica ningún cambio (rollback) y se lanza
     * NoHayStockException.
     */
    public static void procesarCompra(List<CarritoItem> items, double total) throws NoHayStockException {
        Connection con = getConexion();
        try {
            con.setAutoCommit(false);

            // 1. Validar stock de todos los productos antes de modificar nada
            for (CarritoItem item : items) {
                int disponible = obtenerStockDisponible(con, item.getIdProducto());
                if (disponible < item.getCantidad()) {
                    con.rollback();
                    throw new NoHayStockException(item.getNombre(), disponible);
                }
            }

            // 2. Insertar el pedido
            int idPedido;
            String sqlPedido = "INSERT INTO Pedidos (nombre_cliente, subtotal, total) VALUES (?, ?, ?)";
            try (PreparedStatement ps = con.prepareStatement(sqlPedido, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, "Cliente Mostrador");
                ps.setDouble(2, total);
                ps.setDouble(3, total);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    rs.next();
                    idPedido = rs.getInt(1);
                }
            }

            // 3. Insertar detalle y descontar inventario de cada producto
            String sqlDetalle = "INSERT INTO Detalle_Pedidos (id_pedido, id_producto, cantidad, precio_unitario, subtotal) "
                    + "VALUES (?, ?, ?, ?, ?)";
            String sqlDescuento = "UPDATE Inventario SET stock_actual = stock_actual - ?, "
                    + "ultima_actualizacion = CURRENT_TIMESTAMP WHERE id_producto = ?";
            try (PreparedStatement psDetalle = con.prepareStatement(sqlDetalle);
                 PreparedStatement psDescuento = con.prepareStatement(sqlDescuento)) {
                for (CarritoItem item : items) {
                    psDetalle.setInt(1, idPedido);
                    psDetalle.setInt(2, item.getIdProducto());
                    psDetalle.setInt(3, item.getCantidad());
                    psDetalle.setDouble(4, item.getPrecioUnitario());
                    psDetalle.setDouble(5, item.getSubtotal());
                    psDetalle.executeUpdate();

                    psDescuento.setInt(1, item.getCantidad());
                    psDescuento.setInt(2, item.getIdProducto());
                    psDescuento.executeUpdate();
                }
            }

            con.commit();
        } catch (SQLException e) {
            try {
                con.rollback();
            } catch (SQLException ignored) {
            }
            System.err.println("Error al procesar la compra: " + e.getMessage());
        } finally {
            try {
                con.setAutoCommit(true);
            } catch (SQLException ignored) {
            }
        }
    }

    /**
     * Cierra la conexión si está abierta. Útil al cerrar la aplicación.
     */
    public static void cerrarConexion() {
        try {
            if (conexion != null && !conexion.isClosed()) {
                conexion.close();
            }
        } catch (SQLException e) {
            System.err.println("Error al cerrar la conexión: " + e.getMessage());
        }
    }
}