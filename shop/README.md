MicroNaut quick tester
---

Just trying to get my head around all of this new stuff, This sample project contains 3 micro service applications:


1. Users - has a list of users the details are stored on h2 DB internally in memory.

2. Products - has a list of products - this is a more complex example which uses extended features to expand over multiple product types again the data is held on h2 db internally in memory

3. Orders - This has a few entries which simply store the order Id, product Id, userId, price at time and quantity - this requires consul technology to route the clients to correct micro service apps as per above.
 So above needs to be running before order is started.
 The OrdersView class is used to bind those ids  and within OrderService it interacts with the end service to fill in the missing bits i.e. the username and product name.

  The overview Class has a dynamic getter which auto populates final total based on quantity user purchased and price at the given time:

As you can see by the very last JSON String:
```
quantity":6,"username":"jsmith","firstName":"John","lastName":"Smith","date":1535035782440,"total":1887.30}
```



1. Starting users microservice:
---
From the shop folder run:

```
./gradlew users:run
```
When you execute above, the app starts up on http://localhost:8183/

and you see:
```
[{"username":"jsmith","firstName":"John","lastName":"Smith","password":"password","id":1},{"username":"cjones","firstName":"Casey","lastName":"Jones","password":"password1","id":2},{"username":"mbutler","firstName":"Mike","lastName":"Butler","password":"password2","id":3},{"username":"jhanes","firstName":"Jim","lastName":"Hanes","password":"password3","id":4},{"username":"ksmith","firstName":"Kevin","lastName":"Smith","password":"password4","id":5}]
```



2. Starting products microservice:
---
This is an extended domain class example all now working
```
./gradlew products:run
```

which provides http://localhost:8182/
and you see:
```
[{"name":"Executive Chair","description":"Exclusive table available in our shop for a limited period, hand crafted.","date":1534954831690,"price":554.55,"id":1,"width":1400,"height":300},{"name":"Mid Executive Table","description":"\n             Exclusive table available in our shop for a limited period, hand crafted.\n            ","date":1534954831767,"price":314.55,"id":2,"width":1200,"height":300},{"name":"Standard Table","description":"\n             Table available in our shop for a limited period, hand crafted.\n            ","date":1534954831772,"price":154.55,"id":3,"width":500,"height":300},{"name":"Executive Table","description":"\n             Exclusive chair available in our shop for a limited period, hand crafted.\n            ","date":1534954831777,"price":254.55,"id":4,"width":300,"height":300,"wheels":true},{"name":"Mid Executive chair","description":"\n             Exclusive chair available in our shop for a limited period, hand crafted.\n            ","date":1534954831782,"price":114.55,"id":5,"width":200,"height":300,"wheels":false},{"name":"Standard chair","description":"\n             Chair available in our shop for a limited period, hand crafted.\n            ","date":1534954831787,"price":54.55,"id":6,"width":200,"height":300,"wheels":false}]
```



3. Starting Orders microservice:
---

Once those are running the rest may be started individually or as a group.
```
./gradlew orders:run
```

Once you run this you will get: http://localhost:8180/

This produces:
```
[{"productName":"Executive Chair","productDescription":"Exclusive table available in our shop for a limited period, hand crafted.","price":554.55,"quantity":1,"username":"jsmith","firstName":"John","lastName":"Smith","date":1535035782365,"total":554.55},{"productName":"Mid Executive Table","productDescription":"\n             Exclusive table available in our shop for a limited period, hand crafted.\n            ","price":314.55,"quantity":6,"username":"jsmith","firstName":"John","lastName":"Smith","date":1535035782440,"total":1887.30}]
```


In order to run this project you will also need to get hold of and install consul and launch by default: https://www.consul.io/downloads.html
```
 ./consul agent -dev

```

This is required for the final bit but configured in all - so that it can route the traffic for routed app i.e. products / users


---------------

HQL on Micronauts
----

 > http://localhost:8180/custom
 > produces: [{"date":1535038163686,"productId":1,"orderId":1,"userId":1,"price":55.50},{"date":1535038163743,"productId":2,"orderId":2,"userId":1,"price":155.50}]

Please refer to orderService.customList()
```
Was unable to cross connect to Product or User since they are on the other microservice apps  -
there is nothing stopping using something like above to pass bits of below or all of below to remote calls
that then do HQL queries to return results this way if you prefer
```

So if you wanted to do things the unprofessional way and rather than bind via a view you could just write raw HQL queries across your apps -

Not sure if this is a good practise or not - it appears to work
