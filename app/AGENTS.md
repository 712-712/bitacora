# Contexto del Proyecto e Instrucciones para Gemini / Agentes

## 1. Arquitectura de Vistas y Gestión de Datos
- **Patrón en Pantallas (Composables):**
    - Las pantallas de Jetpack Compose interactúan de forma directa con los objetos `Repository` (singletons).
    - La gestión de estado se realiza localmente en el Composable mediante `remember`, `mutableStateOf` y corrutinas (`rememberCoroutineScope`, `LaunchedEffect`) para operaciones asíncronas de base de datos.
- **Inmunidad a Fallos en Repositorios y Contexto Activo (Room Safety):**
    - Todos los repositorios (`ClienteRepository`, `AutoRepository`, `OrdenServicioRepository`, `RepuestoRepository`, `GastoRepository`, `FacturaRepository`) utilizan un getter perezoso de DAO pasando `Context` (`obtenerDao(context)`).
    - **Regla Obligatoria:** Toda invocación a métodos de repositorio (`obtener...`, `guardar...`, `eliminar...`, `actualizar...`) desde Composables debe enviar la referencia de `LocalContext.current` (`context`) y estar envuelta en bloques `try-catch` dentro de `LaunchedEffect` o `scope.launch`.

## 2. Inyección de Dependencias y Persistencia
- **Sin Framework de DI (Hilt/Koin):** El proyecto no utiliza inyección de dependencias por frameworks externos. Los repositorios se consumen directamente como instancias estáticas (`object`), inicializándose a través del contexto de la base de datos Room cuando es requerido.
- **Manejo de Versiones en Room Database:**
    - `ClienteDatabase` utiliza `version = 8` con `.fallbackToDestructiveMigration()`. Cualquier modificación a entidades (`Cliente`, `Auto`, `OrdenServicio`, `Repuesto`, `Gasto`, `Factura`) debe acompañarse del incremento en la versión de la base de datos para prevenir conflictos de integridad de esquema (`schema identity hash mismatch`).
- **Preferencias Locales:**
    - Para configuraciones o formularios del área cliente (ej. `MisDatosFacturacionScreen.kt`), se utiliza `SharedPreferences` (`datos_facturacion_prefs`) para mantener la persistencia local sin requerir backend externo.

## 3. Reglas de Formularios y Estandarización de Entrada de Texto
- **Mayúsculas Automáticas (Homologación Obligatoria):**
    - Todos los campos de texto (`OutlinedTextField`) deben configurar `keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters)` y forzar `.uppercase()` en `onValueChange` para garantizar entradas homogéneas en mayúsculas (`A, B, C`).
- **Estilo de Texto Estándar:**
    - `textStyle = TextStyle(color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)` sobre contenedores de color `Colores.FondoSecundario` (`Color(0xFF0D284B)`).
- **Cursor e Indicador de Selección Blanco (Sin Puntos Negros):**
    - Todos los campos deben configurar `cursorColor = Color.White` y `selectionColors = TextSelectionColors(handleColor = Color.White, backgroundColor = Color(...).copy(alpha = 0.4f))` para evitar la gota/punto negro inferior en Android Material3.
- **Navegación del Teclado:**
    - Incluir `.imePadding()` en el contenedor principal y `ImeAction.Next` con `focusManager.moveFocus(FocusDirection.Down)` para navegación fluida campo por campo sin tapar la interfaz.

## 4. Validación Antiduplicados y Tarjeta de Alerta
- **Consulta Previa en Tiempo Real:**
    - En todas las pantallas para crear o agregar elementos (`NuevoClienteScreen`, `NuevoAutoScreen`, `NuevoRepuestoScreen`, `NuevaFacturaScreen`), la pantalla consulta en tiempo real a la base de datos Room mientras el usuario escribe.
- **Tarjeta M3 de Alerta Destacada:**
    - Si se detecta una coincidencia por clave (Nombre, Teléfono, Placa, VIN, Nombre de Repuesto o Folio de Factura), se despliega una **Tarjeta M3 de Alerta en Rojo (`⚠️ ELEMENTO / CLIENTE YA REGISTRADO`)** mostrando los detalles del registro existente e impidiendo la duplicación de datos.

## 5. Integración con Mapa y Servicios Externos
- **Búsqueda Directa en Google Maps:**
    - La búsqueda de refaccionarias y talleres ejecuta directamente un `Intent` nativo a la app de Google Maps (`geo:0,0?q=refaccionaria+$query` o `https://www.google.com/maps/search/?api=1&query=...`), desplegando refaccionarias reales en un radio de 5 km alrededor de la ubicación del usuario, sin pantallas intermedias ni WebViews propensos a errores de la Embed API.
- **WhatsApp:**
    - Generación y envío de cotizaciones a través de `https://wa.me/52$telefono` con resúmenes detallados de fallo, diagnóstico, mano de obra, refacciones y total formateado.

## 6. Arquitectura de Botón REGRESAR Fijo e Inmóvil al Fondo (Fixed Bottom Regresar Button)
- **Criterio Global Obligatorio en TODAS las Pantallas:**
    - El contenedor raíz debe ser un `Box(modifier = Modifier.fillMaxSize().background(Colores.FondoPantalla).statusBarsPadding().navigationBarsPadding().imePadding())`.
    - La columna de contenido desplazable (`Column` o `LazyColumn` o `.verticalScroll(scrollState)`) toma `.fillMaxSize()` con un padding inferior amplio (ej: `.padding(bottom = 76.dp)`).
    - El botón de **`REGRESAR`** o **`SALIR DE LA APP`** (`BotonModulo3D`) se posiciona **fijo e inmóvil al fondo** alineado con `Alignment.BottomCenter` dentro del `Box` externo (`modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(horizontal = 24.dp, vertical = 10.dp)`).
    - **Resultado:** La lista o formulario de datos se desplaza libremente por detrás del botón, mientras que el botón **REGRESAR permanece siempre visible, accesible e inmóvil al fondo** sin necesidad de hacer scroll hasta el final de la pantalla.

## 7. Diseño, Accesibilidad y Paleta de Colores por Módulo (Reglas Globales)
- **Accesibilidad (Adultos Mayores):**
  - Todos los botones (`BotonModulo3D`) deben tener un tamaño accesible, áreas táctiles amplias y texto en **Negro Negrita** (`colorTexto = Color.Black`), excepto los botones de regresó o salir que usan texto en **Blanco** (`colorTexto = Color.White`).
- **Paleta de Colores por Módulo (Criterio obligatorio en TODOS los submenús y pantallas de la app):**
  - **Clientes:** Azul (`#80D8FF`, `#00B8D4`, `#006064`)
  - **Autos / Vehículos:** Verde (`#B9F6CA`, `#00C853`, `#00695C`)
  - **Cotizaciones / Órdenes:** Café (`#D7B899`, `#9B6B43`, `#5D3A1A`)
  - **Inventario:** Morado / Fucsia (`#F3A7FF`, `#D83CFF`, `#7B1599`)
  - **Gastos:** Rojo (`#xFFFF9999`, `#xFFFF4141`, `#B51F1F`)
  - **Facturación:** Cian (`#8FFFFF`, `#00DDEB`, `#007F88`)
  - **Reportes:** Índigo (`#ADB8FF`, `#5C70FF`, `#29399E`)
  - **Configuración:** Amarillo (`#FFF59D`, `#FFEB3B`, `#FBC02D`)
  - **Área del Cliente:** Café (`#D7B899`, `#9B6B43`, `#5D3A1A`)
  - **Regresar / Salir de la app:** Gris unificado (`#D5E1E6`, `#90A4AE`, `#455A64`)
