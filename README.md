CinemaManager es una aplicación de consola desarrollada en Java que simula la gestión básica de un sistema de cine, incluyendo el manejo de datos de usuarios y operaciones sobre colecciones persistidas en archivos JSON.

El proyecto fue desarrollado con foco en:

• reutilización de código.

• abstracción de operaciones sobre colecciones.

• validación robusta de entrada de usuario.

• persistencia simple basada en JSON.

• organización clara del dominio.

────── Funcionalidades ──────

El sistema permite:

• registrar y gestionar datos personales de usuarios.

• validar información ingresada por consola.

• evitar duplicación de registros.

• persistir información automáticamente en archivos JSON.

• recuperar datos persistidos al iniciar la aplicación.

• administrar colecciones tipadas mediante un gestor genérico.

────── Arquitectura del proyecto ──────

El sistema utiliza gestores de dominio (Manager classes) encargados de coordinar las operaciones sobre cada entidad del sistema.

Cada manager se encarga de:

• administrar su colección correspondiente.

• aplicar validaciones de negocio.

• coordinar la persistencia.

• interactuar con la interfaz de consola.

Aunque este enfoque centraliza varias responsabilidades, permite mantener una estructura clara y simple para aplicaciones CLI.

────── Persistencia JSON ──────

La persistencia se implementa mediante serialización y deserialización de objetos a archivos JSON.
Las colecciones en memoria se reconstruyen automáticamente al iniciar la aplicación, permitiendo que los datos del sistema se mantengan entre ejecuciones.

────── Componentes reutilizables ──────

"StorageManager"
Uno de los objetivos principales del proyecto fue evitar duplicación de lógica CRUD. Para ello se implementó un gestor genérico de almacenamiento que abstrae las operaciones comunes sobre colecciones.
Sus principales responsabilidades son:

• agregar elementos.

• prevenir duplicados.

• buscar por identificador.

• buscar mediante predicados.

• obtener todos los elementos.

• limpiar colecciones.

Este componente permite reutilizar la lógica de manejo de colecciones entre distintos managers del sistema.

"ConsoleUtil"
La interacción con el usuario fue centralizada en una utilidad de consola que:

• valida distintos tipos de entrada (nombre, email, teléfono, DNI, etc).

• controla opciones de menú válidas.

• maneja confirmaciones.

• evita repetición de código de validación.

Esto mejora la legibilidad del código y evita duplicar lógica de entrada de usuario en múltiples clases.

"JsonUtil"
Del mismo modo se utiliza una clase utilitaria para la reutilización de los métodos comúnes lectura y escritura. Así mismo, se incluyen los adapters necesarios para lograr la serialización y deserialización de Duration, LocalTime y LocalDate.

────── Decisiones de diseño ──────

Durante el desarrollo se priorizaron:

• reutilización de código mediante componentes genéricos.

• encapsulación de operaciones sobre datos.

• validación consistente de entrada de usuario.

• manejo explícito de errores.

• claridad estructural del dominio.

────── Posible evolución arquitectónica ──────

Actualmente los managers combinan responsabilidades de:

• lógica de negocio.

• interacción con el usuario.

• persistencia.

Una posible evolución del proyecto sería migrar hacia una arquitectura en capas, separando responsabilidades en:

• Repository layer: acceso y persistencia de datos.

• Service layer: lógica de negocio.

• Controller layer: interacción con el usuario.

Esta separación permitiría: reducir acoplamiento, aumentar cohesión, mejorar testabilidad, aplicar correctamente el principio de responsabilidad única (SRP) en todo el proyecto y alinearse con patrones como MVC o arquitectura por capas.

────── Objetivo del proyecto ──────

Este proyecto fue desarrollado como práctica de diseño estructurado en aplicaciones Java de consola, con foco en:

• modelado de dominio.

• abstracción de estructuras de datos.

• reutilización de código.

• organización modular del sistema.
