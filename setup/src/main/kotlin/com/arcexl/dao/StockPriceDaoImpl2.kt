package com.arcexl.dao

import com.arcexl.dao.mybatis.mapper.StockPriceMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

@Repository("stockPriceDao")
class StockPriceDaoImpl2(@Autowired val stockPriceMapper: StockPriceMapper) : StockPriceDao by stockPriceMapper