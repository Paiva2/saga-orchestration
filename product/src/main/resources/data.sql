insert into public.tb_products (pd_id, pd_sku, pd_price, pd_available, pd_seller_name, pd_created_at, pd_updated_at)
values (1, 'sku-1', 200.00, 10, 'Seller 1', now(), now()),
       (2, 'sku-2', 500.00, 20, 'Seller 2', now(), now()),
       (3, 'sku-3', 1000.00, 5, 'Seller 3', now(), now()),
       (4, 'sku-4', 100.00, 3, 'Seller 4', now(), now());