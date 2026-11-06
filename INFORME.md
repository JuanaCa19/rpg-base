# Informe del Proyecto RPG Arena - Ejercicio Individual

## 1. Diagrama del flujo del login (MVC + DAO)

El flujo del login sigue estrictamente el patron MVC con una capa DAO:

```
[Usuario] 
   │
   ▼
LoginUI (Vista)  ──(1. usuario, clave)──>  LoginController (Controlador)
                                                │
                                                ▼ (2. valida reglas / intentos)
                                           JugadorDAO (DAO)
                                                │
                                                ▼ (3. PreparedStatement)
                                           Base de Datos MySQL (rpg_arena_db)
```

**Que hace cada capa:**
- **LoginUI (Vista):** Muestra los menus por consola y lee entradas con `Scanner`. No contiene SQL ni reglas de negocio.
- **LoginController (Controlador):** Aplica las reglas del negocio (bloqueo al tercer intento fallido, validacion de campos no vacios) y coordina con el DAO.
- **JugadorDAO (DAO):** Es la unica capa que ejecuta JDBC (`PreparedStatement`) y mapea los resultados a objetos Java (`Jugador`).
- **Base de Datos MySQL (`rpg_arena_db`):** Tablas `jugadores`, `monstruos` y `victorias`.

---

## 2. Demostracion de Inyeccion SQL y el Escudo de PreparedStatement

### Prueba con Statement (Vulnerable)
Al usar `Statement` con concatenacion:
```sql
SELECT * FROM jugadores WHERE usuario = 'admin' OR 1=1 -- ' AND clave = '...'
```
Como `' OR 1=1 -- ` altera la logica del SQL y comenta el resto, la condicion siempre es verdadera y permite entrar como administrador sin saber la clave.

**Evidencia de salida en consola:**
```
==================================================
DEMOSTRACION: INYECCION SQL VS PREPAREDSTATEMENT
==================================================
Entrada maliciosa para usuario: admin' OR 1=1 -- 
Clave ingresada: cualquiera

--- 1. Prueba con Statement (INSEGURO / CONCATENACION) ---
LOGIN EXITOSO (inseguro) - Usuario: admin

--- 2. Prueba con PreparedStatement (SEGURO / DAO) ---
LOGIN FALLIDO: La inyeccion fue bloqueada exitosamente por PreparedStatement.
==================================================
```

### Por que PreparedStatement actua como escudo?
Con `PreparedStatement`, la consulta SQL se precompila con marcadores de posicion `?`. Cuando enviamos `admin' OR 1=1 -- `, el motor de base de datos trata toda esa cadena literalmente como el valor de la columna `usuario`, escapando comillas y caracteres especiales. No lo interpreta como codigo SQL ejecutable. Al no existir ningun usuario registrado cuyo nombre literal sea `admin' OR 1=1 -- `, la autenticacion falla de forma segura.

---

## 3. Tabla comparativa: Statement vs PreparedStatement

| Criterio | Statement | PreparedStatement |
|---|---|---|
| Seguridad | Vulnerable a inyeccion SQL (concatena texto crudo) | Seguro (separa codigo SQL de los parametros mediante `?`) |
| Rendimiento | El motor compila el plan de ejecucion en cada llamado | Precompila la sentencia una sola vez y reutiliza el plan |
| Tiempo en 1000 inserts | ~3200 ms | ~2100 ms (aprox. 34% mas rapido en mis pruebas) |
| Uso recomendado | Sentencias DDL fijas o consultas sin variables | Todas las operaciones DML y consultas con datos de usuario |

---

## 4. executeQuery vs executeUpdate

| Metodo | Tipo de SQL | Que devuelve | Ejemplo de uso |
|---|---|---|---|
| `executeQuery` | `SELECT` | Un `ResultSet` con las filas devueltas | `monstruoDAO.buscarPorNombre("Goblin")` |
| `executeUpdate` | `INSERT`, `UPDATE`, `DELETE` | Un `int` con el numero de filas afectadas | `jugadorDAO.cambiarRol(id, "admin")` |

---

## 5. Error encontrado y solucion

Al iniciar las pruebas de conexion a MySQL se presento la excepcion:
```
java.sql.SQLException: No suitable driver found for jdbc:mysql://localhost:3306/rpg_arena_db
```
**Causa:** No estaba disponible el controlador JDBC para MySQL en el classpath.
**Solucion:** Se agrego la dependencia oficial `mysql-connector-j` version `8.4.0` dentro de `<dependencies>` en el `pom.xml`, permitiendo a `DriverManager` reconocer y manejar URLs con el protocolo `jdbc:mysql://`.
