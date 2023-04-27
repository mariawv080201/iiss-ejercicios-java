# Práctica 2: Retrollamadas y futuros

## Repaso de conceptos teóricos

## Programación asíncrona en Java

En Java 5 se añadió la interfaz `Future` para integrar la programación asíncrona. Sin embargo, esta interfaz no proporcionaba la posibilidad de combinar los resultados obtenidos al aplicar diferentes operaciones o manejar los posibles errores. Por este motivo, en Java 8 se incluyó la clase `CompletableFuture`, junto a la interfaz `Future`, la cual implementa también la interfaz `CompletionStage`. Esta interfaz incluye el contrato de operaciones para incluir la combinación de resultados procedentes de diferentes operaciones. 

En las secciones posteriores se verán algunos de los usos propuestos por esta interfaz en Java 8.

### `CompletableFuture` como (simple) `Future`

A continuación se muestra un ejemplo de uso de la clase `CompletableFuture`:


#### `Main.java`
```java
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class Main {
	public static void main(String args[]) throws InterruptedException, ExecutionException {
		Future<String> completableFuture = AsynchronousAPI.helloWorldAsync();
		String result = completableFuture.get();
		System.out.println("The result is " + result);
	}
}
```

#### `AsynchronousAPI.java`
```java
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class AsynchronousAPI {
	
	public static Future<String> helloWorldAsync() throws InterruptedException {
	    CompletableFuture<String> completableFuture 
	      = new CompletableFuture<>();
	 
	    Executors.newCachedThreadPool().submit(() -> {
	        Thread.sleep(1000);
	        completableFuture.complete("Hello world");
	        return null;
	    });
	 
	    return completableFuture;
	}
}
``` 

En primer lugar, en la función `main` se obtiene la variable `completableFuture`, la cual es de tipo `Future<String>`, esta variable obtendrá su valor cuando la función `helloWorldAsync()` se resuelva.

A tráves de la operación `completableFuture.get()` se obtiene el resultado de la función y, posteriormente, se muestra en consola.

Por otro lado, el comportamiento de la función `helloWorldAsync`se basa en el uso de la operación `submit()` de `Executors.newCachedThreadPool()`, que crea un nuevo hilo que se suspende durante 10 segundos para añadir un retardo temporal y, posteriormente, completa la operación con el valor `Hello world`.

En este caso, cuando finalice el procesamiento del programa se mostrará por consola el mensaje `The result is Hello world`.

### Procesamiento de resultados asíncronos

A continuación, se muestra un ejemplo del uso del resultado devuelto en un `CompletableFuture` desde otra variable del mismo tipo:

#### `Main.java`
```java
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Main {
	public static void main(String args[]) throws InterruptedException, ExecutionException {
		
		//Then apply
		CompletableFuture<String> completableFuture =
		   AsynchronousAPI.helloWorldAsync();
		CompletableFuture<String> future =
		   completableFuture.thenApply(s -> s + " world");
		String result = future.get();
		System.out.println(result);
		
		//Then accept
		completableFuture = AsynchronousAPI.helloWorldAsync();
		CompletableFuture<Void> futureVoid =
		   completableFuture.thenAccept(s -> System.out.println(s + " world"));
		futureVoid.get();
		
		//Then run
		completableFuture = AsynchronousAPI.helloWorldAsync();
		futureVoid = completableFuture.thenRun(() -> System.out.println( "world"));
		futureVoid.get();
	}
}
```

#### `AsynchronousAPI.java`
```java
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class AsynchronousAPI {
	
	public static CompletableFuture<String> helloWorldAsync() throws InterruptedException {
	    CompletableFuture<String> completableFuture 
	      = new CompletableFuture<>();
	 
	    Executors.newCachedThreadPool().submit(() -> {
	        Thread.sleep(1000);
	        completableFuture.complete("Hello");
	        return null;
	    });
	 
	    return completableFuture;
	}

}
``` 

En este caso, la función `helloWorldAsync` realiza el mismo comportamiento que en el ejemplo anterior, únicamente con la diferencia de que devuelve `Hello` y no `Hello world` completo.

Por otro lado, en la función `main` se utilizan tres funciones de la clase `CompletableFuture`. A continuación se detalla el comportamiento de cada una de ellas:

- `thenApply()`: La variable `future` espera el resultado de la resolución de la variable `completableFuture`. En este caso, espera un resultado de tipo `String`. Finalmente, concatena a ese resultado la palabra `world` y obtiene el valor completo de la cadena en su resolución, es decir, `Hello world`.

- `thenAccept()`: La variable `futureVoid` espera el resultado de la resolución de la variable `completableFuture`, como en el caso anterior. La diferencia es que la variable `futureVoid` no podrá devolver un resultado de tipo `String` en su resolución, sino que será de tipo `Void`. Por este motivo, se lanza directamente la función `System.out.println` en la resolución de la variable `futureVoid`.

- `thenRun()`: La variable `futureVoid` en este caso no espera ningún resultado, sino que sólo espera la resolución de la variable `completableFuture`. Como en el caso anterior, la variable `futureVoid` lanzará directamente la función `System.out.println` en su resolución, pero no concatenará el resultado de la variable `completableFuture`, mostrando únicamente la palabra `world`.


### Combinación de Futures

A continuación, se muestran varios fragmentos de código que ilustran la forma de combinar varios resultados procedentes de diferentes variables de tipo `CompletableFuture`:

```java
CompletableFuture<String> completableFuture 
  = CompletableFuture.supplyAsync(() -> "Hello")
    .thenCompose(s -> CompletableFuture.supplyAsync(() -> s + " World"));
```

```java
CompletableFuture<String> completableFuture 
  = CompletableFuture.supplyAsync(() -> "Hello")
    .thenCombine(
	  CompletableFuture.supplyAsync(() -> " World"),
	  (s1, s2) -> s1 + s2)
	);
```

```java
CompletableFuture future = CompletableFuture.supplyAsync(() -> "Hello")
  .thenAcceptBoth(CompletableFuture.supplyAsync(() -> " World"),
    (s1, s2) -> System.out.println(s1 + s2)
  );
```

En la sección anterior se utilizan tres funciones de la clase `CompletableFuture` para combinar los resultados. A continuación se detalla el comportamiento de cada una de ellas:

- `thenCompose()`: Esta función permite _concatenar_ el resultado de una variable `CompletableFuture` en otra una vez que haya finalizado su procesamiento. En este caso, la ejecución de los `CompletableFuture` no es independiente, sino que se encadenan de forma secuencial.

- `thenCombine()`: Esta función permite ejecutar los `CompletableFuture` de forma independiente y, posteriormente, combinar los resultados en la variable `CompletableFuture` resultante.

- `thenAcceptBoth()`: Esta función realiza el mismo procesamiento que `thenCombine()`, pero con la diferencia de que la variable `CompletableFuture` resultante (future) es de tipo `Void` y realiza una acción. En este caso, invoca a la función `System.out.println`.

### Ejecución de Futures en paralelo

A continuación, se muestra un fragmento de código que ilustra la forma de combinar varios resultados procedentes de diferentes variables de tipo `CompletableFuture`, como en la sección anterior, pero realizando su procesamiento en paralelo:

```java
CompletableFuture<String> future1  
  = CompletableFuture.supplyAsync(() -> "Hello");
CompletableFuture<String> future2  
  = CompletableFuture.supplyAsync(() -> "Beautiful");
CompletableFuture<String> future3  
  = CompletableFuture.supplyAsync(() -> "World");
 
CompletableFuture<Void> combinedFuture 
  = CompletableFuture.allOf(future1, future2, future3);
 
// ...
 
combinedFuture.get();
```

En este caso, hay que tener en cuenta que el resultado de la función `allOf` de la clase `CompletableFuture` es de tipo `Void` y, por tanto, una limitación de este método es que no se pueden combinar los resultados de las variables `future1`, `future2` y `future3`.

No obstante, haciendo uso del API para Streams de Java 8 se puede solventar este problema:

```java
String combined = Stream.of(future1, future2, future3)
  .map(CompletableFuture::join)
  .collect(Collectors.joining(" "));
```

## Ejercicios propuestos

### Ejercicio 1

Dados los siguientes fragmentos de código, responder a las siguientes preguntas:

#### `AsynchronousAPI.java`

```java
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class AsynchronousAPI {
	
	public static Future<Integer> additionAsync(List<Integer> elements) throws InterruptedException {
	    CompletableFuture<Integer> completableFuture 
	      = new CompletableFuture<>();
	 
	    Executors.newCachedThreadPool().submit(() -> {
	    //TO-DO
	    });
	 
	    return completableFuture;
	}
}
```

#### `Main.java`

```java
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class Main {
	
	public static void main(String args[]) throws InterruptedException, ExecutionException {
		List<Integer> elements = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
		Future<Integer> completableFuture = //TO-DO;
		Integer result = //TO-DO;
		System.out.println("The result is " + result);
	}
}
```

#### Preguntas propuestas

Complete las secciones TO-DO de las clases `AsynchronousAPI` y `Main`, teniendo en cuenta que:

- El método `additionAsync` debe devolver la suma de todos los números contenidos en `elements`.
- El método `additionAsync` debe mostrar por consola cada uno de los elementos que esta sumando con el mensaje `Adding (element)`.
- El método `additionAsync` debe añadir un retardo de 5 segundos en la suma de cada elemento.
- En la función `main` se debe mostrar en consola el resultado de la suma completa con el mensaje `The result is (result)`.

##### `AsynchronousAPI.java`

En el método additionAsync creo un objeto CompletableFuture que será contendrá la suma de los elementos de la lista. Luego, usamos un objeto ExecutorService para enviar un trabajo asíncrono que realiza la suma de cada elemento de la lista. En el bucle for, sumamos cada elemento, mostramos un mensaje de Adding (element) y añadimos un retardo de 5 segundos para simular una operación costosa. Una vez que se completa el bucle for, llamamos al método completableFuture.complete(sum) para establecer el valor completo del objeto CompletableFuture con la suma.

```java
mport java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class AsynchronousAPI {
	public static Future<Integer> additionAsync(List<Integer> elements) throws InterruptedException {
    		CompletableFuture<Integer> completableFuture = new CompletableFuture<>();
    
    		Executors.newCachedThreadPool().submit(() -> {
        		int sum = 0;
        		for (int element : elements) {
            			sum += element;
            			System.out.println("Adding " + element);
            			try {
                			Thread.sleep(5000); // Delay of 5 seconds for each element
            			} catch (InterruptedException e) { e.printStackTrace(); }
        		}
        		completableFuture.complete(sum);
    		});
    		return completableFuture;
	}
}
```

##### `Main.java`

Creo una lista de enteros y llamo al método additionAsync de la clase AsynchronousAPI, que devuelve un objeto Future que representa el resultado de la suma asíncrona. Luego, llamo al método get() del objeto Future para esperar a que se complete la operación y obtener el resultado de la suma. Finalmente, mostramos el resultado completo en la consola con el mensaje The result is (result).

```java
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class Main {
	public static void main(String args[]) throws InterruptedException, ExecutionException {
		List<Integer> elements = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
		Future<Integer> completableFuture = AsynchronousAPI.additionAsync(elements);
		Integer result = completableFuture.get();
		System.out.println("The result is " + result);
	}
}
```



### Ejercicio 2

Dado los siguientes fragmentos de código responder a las siguientes preguntas:

#### `AsynchronousAPI.java`

```java
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

public class AsynchronousAPI {
	
	public static CompletableFuture<Integer> additionAsync(List<Integer> elements) throws InterruptedException {
	 //TO-DO
	}
	
	public static CompletableFuture<Integer> mutiplicationAsync(List<Integer> elements) throws InterruptedException {
	//TO-DO
	}

}
```

#### `Main.java`

```java
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Main {
	
	public static void main(String args[]) throws InterruptedException, ExecutionException {
		List<Integer> elements = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
		List<Integer> elements2 = Arrays.asList(11, 12, 13, 14, 15, 16, 17, 18, 19, 20);
		CompletableFuture<Void> completableFuture = //TO-DO;
		
		completableFuture.get();
	}
}
```

#### Preguntas propuestas

1. Complete las secciones TO-DO de las clases `AsynchronousAPI` y `Main`, teniendo en cuenta que:

- El método `additionAsync` debe devolver la suma de todos los números contenidos en `elements`.
- El método `additionAsync` debe mostrar por consola cada uno de los elementos que esta sumando con el mensaje `Adding (element)`.
- El método `additionAsync` debe añadir un retardo de 2 segundos en la suma de cada elemento.
- El método `multiplicationAsync` debe devolver la producto de todos los números contenidos en `elements2`.
- El método `multiplicationAsync` debe mostrar por consola cada uno de los elementos que esta multiplicando con el mensaje `Multiplying (element)`.
- El método `multiplicationAsync` debe añadir un retardo de 3 segundos en el producto de cada elemento.
- En la función `main` se debe mostrar en consola el resultado de la suma completa con el mensaje `The result is (result)`. Este mensaje debe ser mostrado de forma directa cuando se complete el resultado de la variable `completableFuture`.

2. Modifique el código de la clase `Main` para que el procesamiento se realice en paralelo y se obtenga el mismo resultado por consola que en el apartado anterior.

##### `AsynchronousAPI.java`

```java
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

public class AsynchronousAPI {
	public static CompletableFuture<Integer> additionAsync(List<Integer> elements) throws InterruptedException {
		CompletableFuture<Integer> completableFuture = new CompletableFuture<>();
		Executors.newCachedThreadPool().submit(() -> {
			int sum = 0;
			for (int element : elements) {
			System.out.println("Adding " + element);
			try {
				Thread.sleep(2000); // espera de 2 segundos
			} catch (InterruptedException e) { completableFuture.completeExceptionally(e); }
			sum += element;
		}
		completableFuture.complete(sum);
		});
		return completableFuture;
}

	public static CompletableFuture<Integer> multiplicationAsync(List<Integer> elements2) throws InterruptedException {
		CompletableFuture<Integer> completableFuture = new CompletableFuture<>();
		Executors.newCachedThreadPool().submit(() -> {
		int product = 1;
		for (int element : elements2) {
			System.out.println("Multiplying " + element);
			try {
				Thread.sleep(3000); // espera de 3 segundos
			} catch (InterruptedException e) {
				completableFuture.completeExceptionally(e);
			}
			product *= element;
		}
		completableFuture.complete(product);
		});
		return completableFuture;
	}
}
```

##### `Main.java`

```java
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Main {
	public static void main(String args[]) throws InterruptedException, ExecutionException {
		List<Integer> elements = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
		List<Integer> elements2 = Arrays.asList(11, 12, 13, 14, 15, 16, 17, 18, 19, 20);
	
		CompletableFuture<Integer> sumFuture = AsynchronousAPI.additionAsync(elements);
		CompletableFuture<Integer> productFuture = AsynchronousAPI.multiplicationAsync(elements2);
	
		CompletableFuture<Void> completableFuture = CompletableFuture.allOf(sumFuture, productFuture);
		completableFuture.thenRun(() -> {
			try {
				int sumResult = sumFuture.get();
				int productResult = productFuture.get();
				System.out.println("The result is " + (sumResult + productResult));
			} catch (InterruptedException | ExecutionException e) { e.printStackTrace(); }
		});
	
		// Espera a que se completen los cálculos
		completableFuture.get();
	}
}
```

## Referencias

[Guide to CompletableFuture]: https://www.baeldung.com/java-completablefuture
[[1] Blog Guide to CompletableFuture][Guide to CompletableFuture]

[API Java 8 CompletableFuture]: https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/CompletableFuture.html
[[2] CompletableFuture API Java 8][API Java 8 CompletableFuture]
