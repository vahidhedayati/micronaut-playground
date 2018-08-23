package orders.view

class OrderView {

    Long userId
    Long productId

    //This part is provided by ProductClient
    String productName
    String productDescription

    BigDecimal price
    Integer quantity



    //This is a rough price of the quantity and price  -
    // in real life you would be more concerned about exact figures
    BigDecimal getTotal() {
        return new BigDecimal(price * quantity).round(2)
    }


    //This part is provided by UserClient
    String username
    String firstName
    String lastName

    Date date

}