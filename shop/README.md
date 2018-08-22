
```
./gradlew users:run
```
When you execute above, the app starts up on http://localhost:8183/

and you see:
```
[{"username":"jsmith","firstName":"John","lastName":"Smith","password":"password","id":1},{"username":"cjones","firstName":"Casey","lastName":"Jones","password":"password1","id":2},{"username":"mbutler","firstName":"Mike","lastName":"Butler","password":"password2","id":3},{"username":"jhanes","firstName":"Jim","lastName":"Hanes","password":"password3","id":4},{"username":"ksmith","firstName":"Kevin","lastName":"Smith","password":"password4","id":5}]
```

This is an extended domain class example all now working
```
./gradlew products:run
```

which provides http://localhost:8182/
and you see:
```
[{"name":"Executive Chair","description":"Exclusive table available in our shop for a limited period, hand crafted.","date":1534954831690,"price":554.55,"id":1,"width":1400,"height":300},{"name":"Mid Executive Table","description":"\n             Exclusive table available in our shop for a limited period, hand crafted.\n            ","date":1534954831767,"price":314.55,"id":2,"width":1200,"height":300},{"name":"Standard Table","description":"\n             Table available in our shop for a limited period, hand crafted.\n            ","date":1534954831772,"price":154.55,"id":3,"width":500,"height":300},{"name":"Executive Table","description":"\n             Exclusive chair available in our shop for a limited period, hand crafted.\n            ","date":1534954831777,"price":254.55,"id":4,"width":300,"height":300,"wheels":true},{"name":"Mid Executive chair","description":"\n             Exclusive chair available in our shop for a limited period, hand crafted.\n            ","date":1534954831782,"price":114.55,"id":5,"width":200,"height":300,"wheels":false},{"name":"Standard chair","description":"\n             Chair available in our shop for a limited period, hand crafted.\n            ","date":1534954831787,"price":54.55,"id":6,"width":200,"height":300,"wheels":false}]
```




This is not done as yet Ignore below
----

Once those are running the rest may be started individually or as a group.
```
./gradlew orders:run  --parallel
```

