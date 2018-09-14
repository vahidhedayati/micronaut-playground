Cloned from https://github.com/mfarache/micronaut-ms

The project did not appear to work for me so I have made it from scratch taking the content of the other project:


Been built into a micronaut project on micronaut:
```
 mn --version
| Micronaut Version: 1.0.0.M4
| JVM Version: 1.8.0_171
```

To run first either install consul locally and run `./consul agent dev` 
or if you have installed docker simply run `sudo docker run -p 8500:8500 consul`


To run 1 instance of thes beer billing waiter instances run this from within this project folder:

```
cd micronaut-ms

./gradlew beer-waiter:run beer-billing:run --parallel
```


Now open brower hit:

http://localhost:8082/waiter/beer/fred `{"name":"mahou","size":"MEDIUM"}`

Run again http://localhost:8082/waiter/beer/fred `{"name":"mahou","size":"MEDIUM"}`

Bill fred:
http://localhost:8082/waiter/bill/fred `{"cost":23.4,"deskId":8081}`

Start with Wilma

http://localhost:8082/waiter/beer/wilma `{"name":"mahou","size":"MEDIUM"}`

http://localhost:8082/waiter/bill/wilma `{"cost":11.7,"deskId":8081}`


Next test multiple waiters running 

`./gradlew beer-billing:run`
And again 
`./gradlew beer-billing:run`


Then run the `test.sh` script  watch all 3 billing systems 


Please look through screen shots for further details and running tests locally