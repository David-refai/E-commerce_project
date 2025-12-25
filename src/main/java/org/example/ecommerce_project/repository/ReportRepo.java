package org.example.ecommerce_project.repository;

import org.example.ecommerce_project.entity.Inventory;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;


@org.springframework.stereotype.Repository
public interface ReportRepo extends Repository<Inventory, Long> {


    @Query(
            value = """
                    select
                        p.id   as productId,
                        p.sku  as sku,
                        p.name as name,
                        sum(oi.qty) as qtySold
                    from order_item oi
                    join product p on p.id = oi.product_id
                    join orders o on o.id = oi.order_id
                    where o.status = 'PAID'
                    group by p.id, p.sku, p.name
                    order by qtySold desc
                    limit 5
                    """,
            nativeQuery = true
    )
    List<TopSellingProductView> top5BestSellingProducts();


    @Query(
            """
                        select i
                        from Inventory i
                        join fetch i.product p
                        where i.inStock < :threshold
                    """
    )
    List<Inventory> lowStockProducts(@Param("threshold") int threshold);


    @Query(
            value = """
                    select
                        date(o.created_at) as day,
                        sum(o.total)       as revenue
                    from orders o
                    where o.status = 'PAID'
                      and o.created_at >= :fromInclusive
                      and o.created_at <  :toExclusive
                    group by date(o.created_at)
                    order by day
                    """,
            nativeQuery = true
    )
    List<RevenuePerDayView> revenuePerDay(
            @Param("fromInclusive") Instant fromInclusive,
            @Param("toExclusive") Instant toExclusive
    );
}
