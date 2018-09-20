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

./gradlew beer-waiter:run  beer-websocket:run --parallel

```
Then on separaete instances launch the beer-billing
```
./gradlew beer-billing:run
./gradlew beer-billing:run
./gradlew beer-billing:run
```


Now open browser hit:

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

I have uploaded a video on you tube based on this branch:[youtube: micronaut : micronaut-ms websocket demo of beer-billing and beer-waiter](https://www.youtube.com/watch?v=p96gYPVgPB8)

Despite the tests on the video the actual product appears to work really well whilst I am not recording so something perhaps resources on PC stops it from working whilst demoing

This is results locally same as video which works absolutely fine which failed on video:

```
./test.sh 
Serving beer to fred1 ------------------------------------------------------------
{"name":"mahou","size":"MEDIUM"}
{"name":"mahou","size":"MEDIUM"}
{"name":"mahou","size":"MEDIUM"}
{"name":"mahou","size":"MEDIUM"}
{"name":"mahou","size":"MEDIUM"}
{"name":"mahou","size":"MEDIUM"}
{"name":"mahou","size":"MEDIUM"}
{"name":"mahou","size":"MEDIUM"}
{"name":"mahou","size":"MEDIUM"}
{"name":"mahou","size":"MEDIUM"}
Billing fred1 ------------------------------------------------------------
{"cost":1872.0,"deskId":28375}{"cost":1872.0,"deskId":11156}{"cost":1872.0,"deskId":31617}{"cost":1872.0,"deskId":28375}
Serving beer to wilma1 ------------------------------------------------------------
{"name":"mahou","size":"MEDIUM"}
{"name":"mahou","size":"MEDIUM"}
{"name":"mahou","size":"MEDIUM"}
{"name":"mahou","size":"MEDIUM"}
{"name":"mahou","size":"MEDIUM"}
{"name":"mahou","size":"MEDIUM"}
{"name":"mahou","size":"MEDIUM"}
{"name":"mahou","size":"MEDIUM"}
{"name":"mahou","size":"MEDIUM"}
{"name":"mahou","size":"MEDIUM"}
Billing wilma1 ------------------------------------------------------------
{"cost":1872.0,"deskId":28375}{"cost":1872.0,"deskId":11156}{"cost":1872.0,"deskId":31617}{"cost":1872.0,"deskId":28375}
Serving beer to barney1 ------------------------------------------------------------
{"name":"mahou","size":"MEDIUM"}
{"name":"mahou","size":"MEDIUM"}
{"name":"mahou","size":"MEDIUM"}
{"name":"mahou","size":"MEDIUM"}
{"name":"mahou","size":"MEDIUM"}
{"name":"mahou","size":"MEDIUM"}
{"name":"mahou","size":"MEDIUM"}
{"name":"mahou","size":"MEDIUM"}
{"name":"mahou","size":"MEDIUM"}
{"name":"mahou","size":"MEDIUM"}
Billing barney1 ------------------------------------------------------------
{"cost":1872.0,"deskId":28375}{"cost":1872.0,"deskId":11156}{"cost":1872.0,"deskId":31617}{"cost":1872.0,"deskId":28375}
Serving beer to betty1 ------------------------------------------------------------
{"name":"mahou","size":"MEDIUM"}
{"name":"mahou","size":"MEDIUM"}
{"name":"mahou","size":"MEDIUM"}
{"name":"mahou","size":"MEDIUM"}
{"name":"mahou","size":"MEDIUM"}
{"name":"mahou","size":"MEDIUM"}
{"name":"mahou","size":"MEDIUM"}
{"name":"mahou","size":"MEDIUM"}
{"name":"mahou","size":"MEDIUM"}
{"name":"mahou","size":"MEDIUM"}
Billing betty1 ------------------------------------------------------------
{"cost":1872.0,"deskId":28375}{"cost":1872.0,"deskId":11156}{"cost":1872.0,"deskId":31617}{"cost":1872.0,"deskId":28375}

```


The websockerts has transmitted the same cost to all 3 instances of the beer-billing application.


