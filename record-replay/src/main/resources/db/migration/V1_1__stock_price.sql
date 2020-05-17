CREATE TABLE IF NOT EXISTS stock_price (
                    stock_symbol varchar(100),
                    "date" date,
                    price numeric,
                    constraint stock_price_pk primary key (stock_symbol, "date")
                    )