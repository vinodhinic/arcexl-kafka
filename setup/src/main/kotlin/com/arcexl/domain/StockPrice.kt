package com.arcexl.domain

import java.time.LocalDate
import javax.persistence.Entity

@Entity
data class StockPrice(val stockSymbol: String,
                      val date: LocalDate,
                      val price: Double)