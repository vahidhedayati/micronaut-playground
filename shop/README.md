There is some form of a bug, at the moment can't really trace down where the problem resides:

```
./gradlew users:run
```
When you execute above, the app starts up on http://localhost:8183/

and you see:
```
[{"username":"jsmith","firstName":"John","lastName":"Smith","password":"password","id":1},{"username":"cjones","firstName":"Casey","lastName":"Jones","password":"password1","id":2},{"username":"mbutler","firstName":"Mike","lastName":"Butler","password":"password2","id":3},{"username":"jhanes","firstName":"Jim","lastName":"Hanes","password":"password3","id":4},{"username":"ksmith","firstName":"Kevin","lastName":"Smith","password":"password4","id":5}]
```

But when you launch:
```
./gradlew products:run
```

Which gives: http://localhost:8182/nonExtended or http://localhost:8182/

or:

 ```
 ./gradlew myproduct:run
 ```



Which gives url: http://localhost:8184/

Neither 8182/8184 appear to list any items. Even though they follow the same principal as users project.

8184 was cut down to have only 1 class to minimise any additional complexity


This is not done as yet Ignore below
----

Once those are running the rest may be started individually or as a group.
```
./gradlew orders:run  --parallel
```

