# Contribuir a ResAnimApp

Para contribuir al proyecto Rescate Animal, es necesario que leas esto con atención. 

## Hacer preguntas

Puedes sentirte libre en hacer cualquier tipo de preguntas, o para pedir ayuda en el desarrollo del código a través de:

       Email: rescateanimal.app@gmail.com
       
Se piensa crear nuevos canales de comunicación más directos y dinámicos, cuando el proyecto así lo requiera. 

Los nuevos canales de comunicación se informarán por este medio. 

## Obtener ayuda

Para contribuir a Rescate Animal, un buen lugar para empezar es a través del email. Tienes total libertad para hacer cualquier tipo de preguntas referentes al código, proyecto y/o para plantear nuevas ideas que vendrían bien en el proyecto. 
No hace falta ser un experto programador. Los programadores utilizan una gran cantidad de tiempo para realizar documentación. También puedes contribuir al proyecto, documentando el código, y aportando en los documento de desarrollo. Este también es un buen medio para aprender la arquitectura y funcionamiento de cualquier código. 
Además, puedes contribuir arreglando bugs, o implementando nuevas funciones. 

## Guía para el manejo de issues

Cuando encuentres algún bug, puedes crear una issue en el repositorio de Github. Por lo pronto ese será el medio de comunicación de bugs. 
Asegurate de que el bugs que descubriste no exista ateriormente.
Si estas seguro de que el bug no existe, crea un nuevo issue. Por favor, agrega toda la información relevantes que se pueda. Trata de explicar como obtuviste el bug para que pueda ser reproducido por otros. 

## Guía para llevar a cabo pull request

Antes de relizar un pull request ten en cuenta:
1. Si es un bug, sigue los pasos de Guía para el manejo de issues.
1. Si es un “mejoramiento” de la APP, discutalo vía email o cree un issue con la etiqueta #feature. Explique cuál es la mejora y cuál es la posible solución. 

Luego debe crear una rama (branch) basado en la rama develop por cada una de las características o bugs que se resuelva. Por favor mantener las ramas lo más pequeña que sea posible y siempre resolviendo un problema. No trate de arregar varios bugs y hacer varias mejoras en la misma rama.

Por favor, siga la siguiente convención de nombre para las ramas
Si es una nueva característica, utilice el prefijo `feature/`. Ej. `feature/se-agrega-la-caracteristica-abc`
Si se está arreglando un bug usar el prefijo `fix/`. Ej. `fix/#numero-de-issue-para-mejorar-seguimiento`.
Si se está agregando documentación colocar el prefijo `doc/`. Ej `doc/agreagando-doc-de-funcionamiento-del-abc`
Sea ordenado en el código que escriba. 
Cuando se lleven a cabo nuevas características deben ir acompañadas de documentación. 
Escribir buenos mensajes de commit siguiendo el siguiente template: “tema:descripción” en la primera linea del mensaje del commit. Ej: “Main_Activity/mejora en la descarga de imagenes”
Luego utilizar el resto del cuerpo del mensaje para agregar toda la información necesaria. 

Al momento de realizar el pull request, hagalo a la rama develop. (Ver: https://help.github.com/articles/about-pull-requests/)

Te dejamos algunos links, como inspiración para escribir buenos mensajes de commit:

1. http://365git.tumblr.com/post/3308646748/writing-git-commit-messages
1. http://tbaggery.com/2008/04/19/a-note-about-git-commit-messages.html
1. http://who-t.blogspot.com.ar/2009/12/on-commit-messages.html

## Primeros pasos
En primer lugar, si vas a trabajar con las actividades que utilizan los mapas de Google, vas a neecesitar crear un Api Key de Google para trabajar con el mapa. 

Sería bueno que leas la documentación:  https://developers.google.com/maps/documentation/android-api/start?hl=es-419

Recuerda que no es necesario crear la api key si no vas a trabjar directamente con el mapa. 

NOTA: Debes tener una cuenta de GitHub.

Ahora sigue los siguientes pasos:
1. Haz un [Fork al repositorio](https://help.github.com/articles/fork-a-repo/)
1. Clona la copia que realizaste en tu computadora:

       $ git clone git@github.com:YouUserName/RescateAnimal.git
       $ cd RescateAnimal

1. Crear una rama para agregar una característica o arreglar un bug. NO TRABAJES NUNCA EN EL MASTER.

       $ git checkout -b my-new-feature

4. Cuando ya haz terminado de trabajar, a  tu repo local los archivos modificados y luego realiza el commit. 

       $ git add modified_files
       $ git commit -m “your messages”

	Nota: Si trabajas con el Google Maps, te recomendamos crear un archivos key.xml, donde almacenes las claves, caso contrario se van a publicar tus claves.  http://stackoverflow.com/a/43244176/6237334

5. Realiza un Push de tus cambios a tu GitHub

       $ git push -u origin my-feature
		
6. Por último, ya puedes realizar un Pull Request. Este documento te puede servir: https://help.github.com/articles/about-pull-requests/