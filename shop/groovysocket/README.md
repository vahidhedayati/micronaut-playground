Groovy socket client project
---

I think this is really lack of understanding how you enable websocket connections on a microanut app.
Following basic examples of netty websocket howto's this does appear to work.

The connection from microservices is initiated from within Orders microservice.

The issue appears to be when enabling the websocket port in the DataLoader.groovy of this app -
the consul aspect of the site stops working


The WebsocketHander did not appear to be able to inject the userService so I ended up created a NonUserService just to make things
work for now. I was more interested in trying to get flow working