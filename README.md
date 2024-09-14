# Telemetry System

## Descripción

El proyecto **Telemetry System** es una aplicación de monitoreo de red que utiliza SNMP (Simple Network Management Protocol) para recopilar y visualizar datos sobre el rendimiento y la configuración de servidores y dispositivos de red. La aplicación proporciona una interfaz gráfica en JavaFX que muestra gráficos de pastel para el uso de CPU, memoria RAM y almacenamiento, así como información general sobre la red y el servidor.

## Funcionalidades

- **Monitoreo de CPU**: Muestra un gráfico de pastel que representa el uso actual de la CPU y el tiempo libre.
- **Monitoreo de RAM**: Muestra un gráfico de pastel que representa la memoria RAM utilizada y disponible.
- **Monitoreo de Almacenamiento**: Muestra un gráfico de pastel que representa el almacenamiento utilizado y disponible.
- **Información General**: Muestra información detallada sobre la red y el servidor, incluyendo:
  - ID de NIC
  - Velocidad de conexión
  - Dirección MAC
  - Nombre del dispositivo
  - Nombre del servidor
  - Ubicación del servidor

## Requisitos

- **Java**: La aplicación está desarrollada en Java y requiere Java 8 o superior.
- **JavaFX**: Utiliza JavaFX para la interfaz gráfica.
- **Biblioteca SNMP4J**: Se utiliza para la comunicación SNMP con el servidor.

## Instalación

1. **Clonar el repositorio**:

   ```bash
   git clone https://github.com/tu-usuario/telemetry-system.git
   
2. **Compilar el proyecto**:
   Navega al directorio del proyecto y compila el código:

   ```bash
    cd telemetry-system
    mvn clean install
3. **Configurar el servidor SNMP**:

   Asegúrate de que el servidor SNMP esté en funcionamiento y accesible desde la aplicación. Modifica las constantes `IP_ADDRESS` y `COMMUNITY` en la clase `MonitorController` para que coincidan con la configuración de tu servidor SNMP.

4. **Ejecutar la aplicación**:

   Utiliza el siguiente comando para ejecutar la aplicación:

   ```bash
   mvn javafx:run
