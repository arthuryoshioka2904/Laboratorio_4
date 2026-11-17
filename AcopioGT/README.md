# Laboratorio 4: Polimorfismo

## Requisitos

JDK 21 o posterior. No necesita bibliotecas externas, Maven ni archivos de datos. Use una terminal con UTF-8. El código se verificó con OpenJDK 21 y se compiló con Java 21.

## Compilar y ejecutar

Desde la carpeta `AcopioGT`:

```sh
javac -encoding UTF-8 -Xlint:all -d out src/*.java
java -cp out Main
```

En Windows puede abrir `ejecutar.bat`. En Linux/macOS puede ejecutar `bash ejecutar.sh`.

## Ejecutar las pruebas

```sh
java -cp out Pruebas
```

Se ejecutan 80 casos y cada fila muestra entrada, resultado esperado, obtenido y estado. El proceso termina con código distinto de cero si alguna prueba falla. Para guardar el resultado:

```sh
java -cp out Pruebas > evidencia/pruebas.tsv
```

## Uso

El menú ofrece pre-liquidación, recepción, registro de bodegas, pedido y despacho, avance de fecha, existencias, contenido de bodegas, próximos vencimientos, reporte contable e historial. Para volver a utilizar una pre-liquidación, seleccione recepción e ingrese los mismos datos; la cotización no reserva un código ni registra movimientos.

Use punto decimal (`13.4`) y fechas ISO (`2026-03-13`). Los códigos ignoran espacios exteriores y se convierten a mayúsculas. Los nombres de las categorías se seleccionan mediante números en el menú. Café y café orgánico son categorías distintas para los pedidos.

La fecha inicial es **2026-03-12**, elegida para reproducir ejemplos del enunciado. Hay dos lotes por categoría y tres bodegas: SECA-1, SECA-2 y FRIA-1. HOR-A vence hoy; CAR-B mañana; QUE-A dentro de dos días; HOR-C dentro de tres días. CAF-A tiene humedad de 13.4 %. CAF-A y CAF-B ingresaron el mismo día y se desempatan por calidad. Los pagos, cobros y pérdidas iniciales son cero, aunque cada lote conserva su costo calculado.

## Decisiones

- `Lote` encapsula identidad, peso y estado, declara las reglas variables y cumple `Comparable<Lote>`.
- `LotePerecedero` implementa `Perecedero` y reutiliza el cálculo de fecha; `Refrigerado` expresa el almacenamiento frío compartido.
- `CafeOrganico` hereda de `Cafe` y añade certificación y bonificación mediante `super.calcularPago()`.
- `Bodega` cumple `Iterable<Lote>` con un iterador ordenado que permite retirar lotes terminales durante el recorrido. La colección interna no se expone.
- `AcopioGT` valida primero y construye un plan de despacho antes de consumir peso. Los servicios no usan `instanceof`, reflexión ni selección de tipos concretos.
- Los `switch` sobre calidad calculan tarifas; el `switch` de la consola elige acciones de menú. Los constructores concretos de categorías se registran en el borde de entrada (`Consola`).
- Dinero y kilogramos usan `BigDecimal`. El pago se redondea al centavo al recibir; costo unitario y prorrateos usan DECIMAL128; cada despacho se redondea al final y cada pérdida por lote al descartarlo, con HALF_UP. No se redondean los kilogramos. Los importes se muestran con dos decimales.
- El reporte de próximos tres días incluye hoy y los días +1, +2 y +3. La fecha solo avanza: se rechaza repetir el mismo día.
- La merma del 5 % solo reduce el pago de hortaliza: el inventario inicial sigue siendo todo el peso recibido.
- El historial conserva los códigos y la bodega original aunque un lote salga del inventario. No existe traslado ni reutilización de códigos.


