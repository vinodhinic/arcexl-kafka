package com.arcexl.dao;

import com.arcexl.BaseArcExlTest;
import com.arcexl.domain.StockPrice;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/*
    Since we are connecting to uat DB for test cases (for now). We need to clear test data after test method
 */
@Sql(statements = {"delete from stock_price where stock_symbol like 'TEST%'"},
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class StockPriceJavaDaoTest extends BaseArcExlTest {

    @Autowired
    private StockPriceDao stockPriceDao;

    @Test
    public void testDao() {
        LocalDate localDate = LocalDate.of(2020, 5, 16);

        StockPrice ibmStockPrice = new StockPrice("TEST-IBM", localDate, 90.00);
        stockPriceDao.insertStockPrice(ibmStockPrice);

        StockPrice mfstStockPrice = new StockPrice("TEST-MFST", localDate, 190.00);
        stockPriceDao.insertStockPrice(mfstStockPrice);

        StockPrice ibmStockPriceReadBackFromDB = stockPriceDao.selectStockPrice(ibmStockPrice.getStockSymbol(), ibmStockPrice.getDate());
        assertEquals(ibmStockPrice, ibmStockPriceReadBackFromDB);

        StockPrice mfstStockPriceReadBackFromDB = stockPriceDao.selectStockPrice(mfstStockPrice.getStockSymbol(), mfstStockPrice.getDate());
        assertEquals(mfstStockPrice, mfstStockPriceReadBackFromDB);

        List<StockPrice> stockPrices = stockPriceDao.selectAllStockPrice();
        assertNotNull(stockPrices);
        assertEquals(2, stockPrices.size());
        assertTrue(stockPrices.containsAll(List.of(ibmStockPrice, mfstStockPrice)));
    }
}
