package com.arcexl.dao

import com.arcexl.BaseArcExlTest
import com.arcexl.domain.StockPrice
import io.kotlintest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.jdbc.Sql
import java.time.LocalDate

private const val TEST = "TEST"

/*
 * This test is same as @com.arcexl.dao.StockPriceJavaDaoTest. Just repeated the same in Kotlin to give you a flavor of
 * extension functions and kotlintest
 */
@Sql(statements = ["delete from stock_price where stock_symbol like '$TEST%'"], executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class StockPriceDaoTest : BaseArcExlTest() {

    private fun StockPrice.toTestStockPrice() = this.copy(stockSymbol = "$TEST-$stockSymbol")

    @Autowired
    private lateinit var stockPriceDao: StockPriceDao

    @Test
    fun `test stock price inserts and selects`() {
        val localDate = LocalDate.of(2020, 5, 16)

        val ibmStockPrice = StockPrice("IBM", localDate, 90.00).toTestStockPrice()
        stockPriceDao.insertStockPrice(ibmStockPrice)

        val mfstStockPrice = StockPrice("MFST", localDate, 190.00).toTestStockPrice()
        stockPriceDao.insertStockPrice(mfstStockPrice)

        stockPriceDao.selectStockPrice(ibmStockPrice.stockSymbol, ibmStockPrice.date) shouldBe ibmStockPrice
        stockPriceDao.selectStockPrice(mfstStockPrice.stockSymbol, mfstStockPrice.date) shouldBe mfstStockPrice

        stockPriceDao.selectAllStockPrice() shouldContainExactlyInAnyOrder listOf(mfstStockPrice, ibmStockPrice)
    }
}