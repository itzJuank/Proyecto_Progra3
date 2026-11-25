# Cumplimiento de la Entrega 1

La revisión usa los apartados 2, 4 y 5 del PDF del proyecto programado. La caja de esta entrega funciona en una sola máquina. Los resultados salen de la compilación limpia, las pruebas JUnit, el recorrido por consola y la demostración aislada de inventario.

## Requisitos funcionales

| Requisito del PDF | Estado | Clases principales | Cómo demostrarlo |
| --- | --- | --- | --- |
| 1. Emitir los cuatro tipos y calcular sus totales | Verificado con criterios provisionales | `Documento`, `FacturaContado`, `FacturaCredito`, `NotaCredito`, `Proforma`, `ServicioFacturacion` | Inicia la caja con un impuesto de prueba del `10 %` y usa la cédula `112340567`. Emite contado con `P001 × 2`, crédito con `P001 × 3`, nota con `P001 × 1` y proforma con `P002 × 1`. Los totales son `7700.00`, `11550.00`, `-3850.00` y `1320.00`. |
| 2. Validar producto existente y cantidad positiva al agregar líneas | Verificado | `ConsolaCajaLocal`, `ServicioFacturacion`, `RepositorioInventario`, `LineaDetalle` | En un borrador, intenta agregar `P999` y luego `P001` con cantidad `0`. Corrige con `P001 × 2` y muestra el borrador. |
| 3. Consultar cliente por cédula contra la capa de datos | Verificado | `ServicioClientes`, `RepositorioClientes`, `RepositorioClientesArchivo`, `RepositorioPadron` | Usa la opción `1` con `112340567` para encontrar a la cliente del padrón sintético. Prueba `123` para formato inválido y `999999999` para cédula inexistente. |
| 4. Descontar existencias y rechazar una venta que no alcance | Verificado | `ServicioFacturacion`, `RepositorioInventarioMemoria`, `FacturaContado`, `FacturaCredito` | Consulta `P001` con la opción `2`. Después del contado quedan `3` unidades. Intenta emitir el crédito con `4`, comprueba el rechazo, deshaz esa línea y emite con `3`. El saldo final es `0`. |
| 5. Deshacer la última línea agregada | Verificado | `Documento`, `Pila`, `Nodo`, `ServicioFacturacion` | Antes de emitir el contado, agrega `P001 × 2` y luego `P002 × 1`. Deshaz con la opción `2` del borrador y usa `3` para comprobar que la línea de `P001` sigue allí. |
| 6. Registrar y consultar el historial de estados | Verificado | `Documento`, `EstadoDocumento`, `ListaEnlazada`, `Nodo` | Usa `3` en el borrador para ver `BORRADOR`. Después de emitir, busca ese número con la opción `5` del menú principal y muestra `BORRADOR`, `EMITIDO` y `ENVIADO`. |
| 7. Encolar y enviar en segundo plano | Verificado | `ServicioFacturacion`, `Cola`, `ProcesadorEnvios`, `ReceptorDocumentosLocal` | Elige `7` justo después de emitir para ver el pendiente. Consulta un producto mientras se procesa. Tras la entrega, `7` muestra la cola vacía y el historial del documento incluye `ENVIADO`. |
| 8. Generar el cierre del día | Verificado con criterios provisionales | `ServicioFacturacion`, `RepositorioDocumentos`, `ConsolaCajaLocal` | Emite los cuatro documentos de la primera fila en la misma sesión y usa `6`. La suma con signo es `16720.00`. |
| 9. Listar por monto y buscar por número | Verificado | `ServicioFacturacion`, `RepositorioDocumentosMemoria`, `ConsolaCajaLocal` | Usa `4` y comprueba el orden `3`, `4`, `1`, `2`. Usa `5` para buscar el número `1` y luego `999` para ver el aviso de documento inexistente. |

## Requisitos técnicos

| Requisito del PDF | Estado | Clases principales | Cómo demostrarlo |
| --- | --- | --- | --- |
| Clase abstracta y polimorfismo | Verificado | `Documento` y sus cuatro subclases | Abre `Documento.calcularTotal()` y las implementaciones. Explica cómo el servicio invoca `descuentaInventario()` sobre un `Documento` y la subclase decide el resultado. |
| Separación por capas y dependencia | Verificado | `ConsolaCajaLocal`, `ServicioFacturacion`, `ServicioClientes`, `Documento`, repositorios | Sigue la opción `3` desde la presentación a los servicios. Señala dónde se comprueban las reglas y dónde se guardan los datos. Las entidades utilizan las estructuras propias para sus estados y líneas. |
| Interfaces de repositorio con implementaciones locales | Verificado | `RepositorioClientes`, `RepositorioInventario`, `RepositorioDocumentos` y sus implementaciones | Señala los parámetros de los constructores de los servicios. Muestra cómo la caja usa `RepositorioClientesArchivo` y la demostración por socket usa `RepositorioClientesPadron` con el mismo `ServicioClientes`. |
| JUnit sobre reglas de negocio, incluidos rechazos | Verificado | `ServicioClientesTest`, `ServicioFacturacionTest`, `RepositorioDocumentosMemoriaTest` y las suites de estructuras y concurrencia | Ejecuta **Test Project** o el comando de Ant del README. Abre una prueba de rechazo con stock insuficiente y enseña que también comprueba el saldo y el consecutivo. |
| Lista enlazada propia con inserción y eliminación | Verificado | `Nodo`, `ListaEnlazada`, `Documento`, `ListaEnlazadaTest` | Traza los tres nodos de un historial y muestra la prueba que elimina nodos al principio, en medio y al final. |
| Pila propia para deshacer | Verificado | `Nodo`, `Pila`, `Documento`, `PilaTest` | Explica qué nodo queda en la cima después de agregar dos líneas y muestra que deshacer retira la última. |
| Cola propia de documentos pendientes | Verificado | `Nodo`, `Cola`, `ServicioFacturacion`, `ColaTest` | Muestra cómo la cola agrega al final y retira por el frente después de confirmar. Compara la consulta del menú con la prueba de orden FIFO. |
| Colecciones de la API, ordenamiento y búsqueda binaria | Verificado | `ServicioFacturacion`, `RepositorioInventarioMemoria`, `RepositorioDocumentosMemoria` | Señala el comparador por monto y el orden distinto por número que precede a `Collections.binarySearch`. Muestra ambas opciones en el menú. |
| Hilo de envío mientras la caja opera | Verificado | `ServicioFacturacion`, `ProcesadorEnvios`, `ReceptorDocumentosLocal`, `ServicioFacturacionConcurrenciaTest` | Muestra `Runnable`, `start()` y `join()`. La prueba detiene el primer envío, emite otros documentos y verifica las 20 entregas al cerrar. |
| Sincronización del inventario con evidencia antes y después | Verificado | `RepositorioInventarioMemoria`, `DemoCarreraInventario`, `ServicioFacturacionTest` | Ejecuta `DemoCarreraInventario`. Dos cajas solicitan `7` unidades cada una de un stock de `10`. Antes se aceptan las dos y el saldo registrado contradice las ventas. Con sincronización se acepta una y quedan `3`. |
| Productor y consumidor sobre la misma cola | Verificado | `ServicioFacturacion`, `ProcesadorEnvios`, `Cola`, `ColaTest` | Ubica `encolar`, `wait()` y `notifyAll()`. La prueba muestra `WAITING` con la cola vacía y la entrega de los pendientes en orden al cerrar. |

## Rúbrica del PDF

| Criterio | Peso | Resultado de la revisión local | Punto para la defensa |
| --- | ---: | --- | --- |
| Pertinencia | 25 % | Los nueve requisitos funcionales se verificaron con la caja local. La tasa, el redondeo y algunos efectos por tipo son criterios provisionales. | Explica cómo cada tipo afecta al inventario y al cierre de la ferretería. |
| Calidad | 25 % | Se revisaron las dependencias, los repositorios, las estructuras propias y las pruebas de aceptación y rechazo. El historial semanal queda por verificar en GitHub. | Elige una regla y sigue sus llamadas desde la consola hasta la entidad o el repositorio. |
| Desarrollo | 25 % | Se verificaron el polimorfismo, el productor/consumidor y el descuento sincronizado. Se guardaron las dos salidas de la condición de carrera. | Explica por qué se validan todas las líneas antes de aplicar los saldos y cuándo se retira un pendiente de la cola. |
| Presentación | 25 % | Se verificó el recorrido por consola. El README incluye el arranque y esta tabla da un orden de demostración. La explicación oral corresponde al estudiante. | Ejecuta el recorrido y deja tiempo para abrir el código, explicar el resultado y señalar las limitaciones. |

## Condiciones de entrega y alcance

| Condición | Estado de la revisión |
| --- | --- |
| Proyecto Java con Ant y apertura directa en NetBeans | La carpeta tiene metadatos J2SE de NetBeans, `build.xml`, datos y fuentes propios. El lanzador de NetBeans registró la copia aislada como proyecto abierto en un perfil temporal sin interfaz gráfica. Se verificaron también la compilación y la entrada interactiva en esa copia. |
| Repositorio sin archivos compilados | `.gitignore` excluye salidas de Ant, archivos `.class` y `.jar` y la configuración privada de NetBeans. |
| README con instrucciones y orden de arranque | Incluye la caja local y, para las demostraciones por socket, el orden padrón, central y caja. |
| GitHub, acceso del docente e historial semanal | Pendientes de verificar en el repositorio de entrega. La carpeta revisada carece de historial Git local. |
| Defensa individual y autoría | El PDF prevé unos 12 minutos y preguntas sobre código elegido al azar. El estudiante explica la implementación y sus decisiones. Se tomó como contexto la autorización de IA comunicada por el estudiante. |

Los requisitos 10 a 15 y los técnicos del apartado 3.2 corresponden a la Entrega 2. Ahí se evalúan la integración por sockets de los tres componentes, el consecutivo central concurrente, el tipo de cambio del BCCR, el uso del último dato disponible ante fallas y la auditoría de recursos. Las clases de red de las semanas 8 y 11 están disponibles como demostraciones independientes. [Guía de clases](GUIA_DE_CLASES.md)
