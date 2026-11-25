# Guía de clases para la defensa

Cada nombre corresponde a un archivo `.java` en la carpeta de su paquete. Las clases de la aplicación están en `src/sifac`. JUnit y las demostraciones auxiliares se compilan desde `test/sifac`.

## Recorrido de una venta

1. `ConsolaCajaLocal` lee la cédula y la opción elegida mediante `Scanner`. `ServicioClientes` busca a la persona a través de la interfaz `RepositorioClientes`.
2. La consola crea la subclase de `Documento` elegida. `ServicioFacturacion` busca el producto y construye cada `LineaDetalle`. El documento apila las líneas conforme se agregan.
3. Al emitir, `ServicioFacturacion` valida el borrador. El inventario comprueba todas las cantidades dentro de `descontar()`. Si la venta cabe, el servicio asigna el consecutivo, registra `EMITIDO`, guarda y encola el documento.
4. `ProcesadorEnvios` toma el documento del frente, lo entrega al receptor local y registra `ENVIADO`. El menú consulta el historial, los pendientes y el cierre mediante el servicio y las entidades.

## Entidades de `sifac.comun.entidades`

| Clase | Qué hace |
| --- | --- |
| `Cliente` | Guarda la cédula y el nombre de la persona asociada al documento. |
| `Producto` | Guarda código, nombre y precio unitario. Las existencias se administran en el repositorio de inventario. |
| `LineaDetalle` | Une un producto y una cantidad positiva. Calcula el subtotal al multiplicar el precio unitario por la cantidad. |
| `EstadoDocumento` | Define los estados `BORRADOR`, `EMITIDO` y `ENVIADO`. |
| `Documento` | Es la base abstracta. Maneja cliente, líneas, fecha, consecutivo e historial. Centraliza el cálculo del importe con impuesto y deja que cada subclase defina su total y su efecto en inventario. |
| `FacturaContado` | Devuelve el importe con impuesto y solicita el descuento de inventario al emitir. |
| `FacturaCredito` | Usa el importe con impuesto y solicita el descuento de inventario. La regla actual aplica la misma fórmula que en el contado. |
| `NotaCredito` | Devuelve el importe con signo negativo. Su emisión conserva las existencias según el criterio provisional. |
| `Proforma` | Muestra un importe estimado con impuesto y conserva las existencias al emitir. |

## Estructuras de `sifac.comun.estructuras`

| Clase | Qué hace |
| --- | --- |
| `Nodo<T>` | Sostiene un valor y el enlace al siguiente nodo. Lo comparten las tres estructuras. |
| `ListaEnlazada<T>` | Agrega estados al final y permite eliminar un valor reajustando sus enlaces. Copia en orden para consultar el historial. |
| `Pila<T>` | Agrega y retira en la cima. Así, deshacer elimina la línea agregada más recientemente. También permite ver las líneas en su orden original. |
| `Cola<T>` | Agrega al final y retira del frente. Con `synchronized`, `wait()` y `notifyAll()` coordina al cajero y al consumidor. `copiar()` da una vista en orden para consultar los pendientes. |

## Datos de `sifac.comun.datos`

| Clase o interfaz | Qué hace |
| --- | --- |
| `RepositorioClientes` | Define el contrato de búsqueda por cédula. `ServicioClientes` recibe este tipo en su constructor. |
| `RepositorioInventario` | Declara búsqueda, listado, consulta de existencias y descuento de líneas. |
| `RepositorioInventarioMemoria` | Guarda el catálogo y las existencias. Prepara saldos provisionales dentro de un método sincronizado y los aplica juntos cuando todas las líneas caben. |
| `RepositorioDocumentos` | Define cómo asignar el siguiente número, guardar un documento y listar los registros. |
| `RepositorioDocumentosMemoria` | Usa un mapa por consecutivo y sincroniza el acceso. Acepta guardar de nuevo el mismo objeto y protege el original si otro documento reclama su número. |
| `RepositorioClientesPadron` | Es el adaptador por socket de la demostración. Usa tiempos de espera de conexión y lectura y traduce fallos a `ServicioPadronException`. |
| `ProtocoloPadron` | Interpreta `OK` y `NOTFOUND`. Valida que la respuesta corresponda a la cédula pedida antes de crear un `Cliente`. |
| `ServicioPadronException` | Comunica que la consulta remota falló y conserva la causa para diagnosticarla. |

## Lógica de `sifac.comun.logica`

| Clase | Qué hace |
| --- | --- |
| `ServicioClientes` | Pide el cliente al repositorio inyectado y muestra un error de negocio cuando la cédula carece de registro. |
| `ServicioFacturacion` | Coordina búsquedas de productos, alta y deshacer de líneas, emisión, cola y cierre diario. Ordena por monto o por número según la consulta. Es el productor de los documentos pendientes. |
| `ProcesadorEnvios` | Es el `Runnable` consumidor. Registra `ENVIADO` y retira el documento de la cola después de guardarlo en el receptor. Si hay un fallo, conserva la causa para notificarla al emitir o cerrar. |

## Caja y central

| Clase | Qué hace |
| --- | --- |
| `sifac.caja.ConsolaCajaLocal` | Es el punto de entrada de la Entrega 1. Instancia los servicios y repositorios locales, mantiene los menús y presenta avisos y valores calculados por el dominio. |
| `sifac.caja.ConsolaCaja` | Es la demostración independiente de consulta por socket. Selecciona servidor, puerto y cédula y usa el mismo `ServicioClientes`. |
| `sifac.caja.EnviarDocumento` | Envía en la demostración de red un identificador y un monto con `REGISTRAR`. Espera el `ACK` del mismo identificador. |
| `sifac.central.ReceptorDocumentosLocal` | Representa al central en la Entrega 1. El hilo de envío espera dos segundos para simular la entrega y luego guarda el documento en memoria. |
| `sifac.central.CentralDemo` | Es el servidor de demostración por socket. Atiende cada conexión en un hilo y registra identificadores y montos con confirmación. |

## Padrón

| Clase | Qué hace |
| --- | --- |
| `sifac.padron.Elector` | Representa una fila del padrón. Compara electores por cédula y puede armar el nombre completo. |
| `sifac.padron.Distrito` | Guarda el código electoral y arma una ubicación con distrito, cantón y provincia. |
| `sifac.padron.RepositorioPadron` | Lee ambos archivos, ordena los electores por cédula y permite buscarlos linealmente o por búsqueda binaria. |
| `sifac.padron.RepositorioClientesArchivo` | Implementa `RepositorioClientes` para la caja local. Valida el formato de cédula, busca en el padrón y convierte un `Elector` en `Cliente`. |
| `sifac.padron.ServidorSecuencial` | Demuestra una atención por socket donde cada conexión se completa antes de atender la siguiente. |
| `sifac.padron.ServidorConcurrente` | Carga el padrón y arranca un hilo `AtencionCliente` por conexión. |
| `sifac.padron.AtencionCliente` | Lee solicitudes de un socket, consulta el padrón y construye respuestas en el hilo de esa conexión. |
| `sifac.padron.ClienteConsulta` | Envía varias cédulas al servidor de demostración y muestra las respuestas y el tiempo transcurrido. |

## Demostraciones y verificaciones de `sifac.pruebas`

| Clase | Qué hace |
| --- | --- |
| `DemoEstados` | Muestra cambios de estado de un hilo antes de `start()`, durante la tarea y después de `join()`. |
| `DemoThreadVsRunnable` | Compara dos formas de escribir la tarea. `ComoThread` extiende `Thread` y `ComoRunnable` implementa `Runnable`. También distingue ejecutar `run()` en el hilo actual de arrancar con `start()`. |
| `DemoPrioridades` | Ejecuta varias rondas con prioridades distintas y registra los órdenes en que terminan los hilos. |
| `DemoCarreraInventario` | Fuerza que dos hilos lean el saldo de diez antes de vender siete cada uno. Después repite el caso con el repositorio sincronizado y compara las ventas y el saldo. |
| `VerificarPadron` | Ejecuta casos de protocolo y de fallos contra sockets de prueba en la propia máquina. Es una demostración separada de JUnit. |

## Pruebas JUnit

| Clase | Qué comprueba |
| --- | --- |
| `ServicioClientesTest` | Prueba el intercambio de repositorio mediante la interfaz y la búsqueda válida e inválida con el padrón sintético local. |
| `RepositorioDocumentosMemoriaTest` | Comprueba que repetir el guardado conserva un registro y que un consecutivo duplicado de otro documento conserva el original. |
| `ServicioFacturacionTest` | Prueba los cuatro totales, el redondeo, las validaciones, deshacer, emisión, inventario, historial, listado, búsqueda, cierre y el intento de dos cajas sobre las mismas existencias. |
| `ServicioFacturacionConcurrenciaTest` | Detiene la primera entrega con `DestinoConDemora`, que es un receptor auxiliar de la prueba. Comprueba la emisión de otros documentos, el orden de entrega y la conservación de pendientes ante fallos. |
| `ListaEnlazadaTest` | Prueba inserciones, eliminaciones, el caso vacío y una inserción después de vaciar la lista. |
| `PilaTest` | Prueba el caso vacío, el orden de consulta y la salida de la última entrada primero. |
| `ColaTest` | Prueba el caso vacío, la vista FIFO, el cierre y la espera del consumidor hasta que llega un valor. |

Para explicar la sincronización, ubica el monitor del repositorio que protege las existencias y el monitor de la cola donde espera el consumidor. Para explicar las consultas, distingue ordenar por monto de ordenar por consecutivo antes de la búsqueda binaria. [Tabla de cumplimiento y defensa](CUMPLIMIENTO.md)
