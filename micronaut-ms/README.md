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


The test scrip when executed will show something like this:
```
./test.sh 
Serving beer to fred ------------------------------------------------------------
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
Billing fred ------------------------------------------------------------
{"cost":245.7,"deskId":34878}{"cost":245.7,"deskId":10963}{"cost":327.6,"deskId":6200}{"cost":245.7,"deskId":34878}
Serving beer to wilma ------------------------------------------------------------
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
Billing wilma ------------------------------------------------------------
{"cost":245.7,"deskId":34878}{"cost":245.7,"deskId":10963}{"cost":327.6,"deskId":6200}{"cost":245.7,"deskId":34878}
Serving beer to barney ------------------------------------------------------------
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
Billing barney ------------------------------------------------------------
{"cost":245.7,"deskId":34878}{"cost":245.7,"deskId":10963}{"cost":327.6,"deskId":6200}{"cost":245.7,"deskId":34878}
Serving beer to betty ------------------------------------------------------------
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
Billing betty ------------------------------------------------------------
{"cost":245.7,"deskId":34878}{"cost":245.7,"deskId":10963}{"cost":327.6,"deskId":6200}{"cost":245.7,"deskId":34878}


```

We actually have 3 different desks and each one has a different cost for each person, the more the test script is executed whilst the app is running the more the values go up since it is all in memory


The application port of the waiter has been hard coded in application.properties.

This is how we can be sure by hitting 8082 is the 1 waiter, this line can be disabled in the application.properties file and it is then a case of either watching or looking at http://localhost:8500 consol to see which ports they are running on for a local test.

This is all to be able to start multiple instances of the waiter.

TODO
--------
Unsure if my example test model is correct, i.e. to have multiple tills which each are storing a different set of figures, we in the end have to find all instances of billing service to get a final total for a given user.
You could easily run multiple instances of the waiter and you would then need to find each port to do different tests for a given user - this probably would give a more accurate result since the billing system would keep a central cost of all things.

Perhaps the problem currently with running multiple billing instances is a local map storing the costs.

 




 