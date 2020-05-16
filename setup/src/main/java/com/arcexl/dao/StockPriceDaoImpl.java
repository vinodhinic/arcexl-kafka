package com.arcexl.dao;

import com.arcexl.dao.mybatis.mapper.StockPriceMapper;
import com.arcexl.domain.StockPrice;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;

/*
Check out @com.arcexl.dao.StockPriceDaoImpl2 at Kotlin.
That is a nice reference for delegate pattern.
 */
//@Repository("stockPriceDao")
public class StockPriceDaoImpl implements StockPriceDao {

    @Autowired
    private StockPriceMapper stockPriceMapper;

    @Override
    public void insertStockPrice(StockPrice stockPrice) {
        stockPriceMapper.insertStockPrice(stockPrice);
    }

    @Override
    public StockPrice selectStockPrice(String stockSymbol, LocalDate date) {
        return stockPriceMapper.selectStockPrice(stockSymbol, date);
    }

    @Override
    public List<StockPrice> selectAllStockPrice() {
        return stockPriceMapper.selectAllStockPrice();
    }

    @Override
    public void deleteStockPrice(String stockSymbol, LocalDate date) {
        stockPriceMapper.deleteStockPrice(stockSymbol, date);
    }
}
