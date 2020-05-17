package com.arcexl.reader;

import com.arcexl.domain.StockPrice;

import java.util.List;

public interface StockPriceReader {
    List<StockPrice> read();
}
