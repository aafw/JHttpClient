The library provides high level api to accessing jetty dynamic HttpClient, which supports HTTP/2 and HTTP/1.1.  
The api is Jsoup-like.
# Example

```java
// send get request
try {
    System.out.println(JHttpClient.connect("https://www.github.com/").get());
} catch (ExecutionException | InterruptedException | TimeoutException e) {
    e.printStackTrace();
}
```
