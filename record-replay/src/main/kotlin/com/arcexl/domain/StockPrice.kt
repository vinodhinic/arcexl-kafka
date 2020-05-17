package com.arcexl.domain

import com.opencsv.bean.CsvBindByName
import com.opencsv.bean.CsvDate
import java.time.LocalDate
import javax.persistence.Entity

@Entity
data class StockPrice(
        @CsvBindByName(required = true, column = "stockSymbol")
        val stockSymbol: String,

        @CsvBindByName(required = true, column = "date")
        @CsvDate("yyyy-MM-dd")
        val date: LocalDate,

        @CsvBindByName(required = true, column = "price")
        val price: Double)