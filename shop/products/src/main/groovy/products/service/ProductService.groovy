package products.service

import grails.gorm.services.Service
import grails.gorm.transactions.Transactional
import products.domain.Chair
import products.domain.Product
import products.domain.ShopTable

import javax.validation.Valid
import javax.validation.constraints.NotNull

@Service(Product)
abstract class ProductService {

    abstract Product save(@Valid Product product)
    abstract ShopTable save(@Valid ShopTable shopTable)
    abstract Chair save(@Valid Chair chair)

    abstract List<Product> findAll()
   // abstract List<ShopTable> findAllTables()
   // abstract List<Chair> findAllChairs()
//

    abstract Number count()


    abstract Product find(@NotNull Long id)
    abstract ShopTable findTable(@NotNull Long id)
    abstract Chair findChair(@NotNull Long id)

    @Transactional(readOnly = true)
    List findBatch(List<Long> ids) {
        //select new map( id as id, name as productName,
        //description as description, price as price, width as width, height as height, wheels as wheels)
        final String query = """
            from Product where id in (:ids)
            """
        return Product.executeQuery(query,[ids:ids],[readOnly:true])
    }


}