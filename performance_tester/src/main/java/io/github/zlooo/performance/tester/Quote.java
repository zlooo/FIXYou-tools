package io.github.zlooo.performance.tester;

import lombok.Value;

import java.util.concurrent.ThreadLocalRandom;

@Value
public class Quote {

    public static final int NUMBER_OF_PREPARED_QUOTES = 10_000;
    private static final double SCALE_FACTOR = 100.0;
    private static final int MIN_PRICE = 5;
    private static final int MAX_PRICE = 100000;
    private static final int SPREAD = 5;

    private final int bidPrice;
    private final double bidPriceAsDouble;
    private final int offerPrice;
    private final double offerPriceAsDouble;

    public Quote(int bidPrice, int offerPrice) {
        this.bidPrice = bidPrice;
        this.offerPrice = offerPrice;
        this.bidPriceAsDouble = bidPrice / SCALE_FACTOR;
        this.offerPriceAsDouble = offerPrice / SCALE_FACTOR;
    }

    public static Quote[] prepareQuotes(int numberOfQuotesToPrepare) {
        return ThreadLocalRandom.current().ints(numberOfQuotesToPrepare, MIN_PRICE, MAX_PRICE).mapToObj(priceSrc -> new Quote(priceSrc, priceSrc - SPREAD)).toArray(Quote[]::new);
    }
}
