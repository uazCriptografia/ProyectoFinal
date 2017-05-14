# Proyecto Final de Criptografía

Este proyecto muestra la utilización de cifrado de llave pública para la
autenticación de mensajes. El ejemplo programado representa una entidad que
tiene destinado recibir archivos cifrados de otras varias, y por medio de la
autenticación de mensajes, el receptor deberá identificar cuál de los
emisores fue quien le envió cada archivo.

## Integrantes del Equipo

> Porfirio Ángel Díaz Sánchez

> Adrián Homero Moreno García

> Santiago García Cabral

## Instrucciones de Uso

### Ejecución automática en un solo equipo

Para esto solamente necesario compilar y ejecutar el archivo ```Main.java```,
localizado en el paquete ```com.poopers.proyectocriptografia```, y automáticamente
se ejecutará la autoridad certificadora, el servidor y una instancia de cliente,
listos para ser usados por medio de sus GUIs.

### Ejecución manual en uno o varios equipos

Aquí la diferencia es que se ejecutarán cada una de las partes del sistema por 
separado, pudiendo permitir ejecutar cada una en diferentes equipos conectados
en una misma red local y configurando sus IPs por medio de las interfaces de 
usuario provistas. Los archivos que se deben compilar y ejecutar son los 
siguientes:

```
com.poopers.proyectocriptografia.comunicacion.AutoridadCertificadora.java

com.poopers.proyectocriptografia.pantallas.ReceptorArchivoForm.java

com.poopers.proyectocriptografia.pantallas.EmisorArchivoForm.java
```

