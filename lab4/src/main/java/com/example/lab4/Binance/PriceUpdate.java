package com.example.lab4.Binance;

public class PriceUpdate {
    private String symbol;
    private String price;

    public PriceUpdate(String symbol, String price) {
        this.symbol = symbol;
        this.price = price;
    }

    // Getters
    public String getSymbol() {
        return symbol;
    }

    public String getPrice() {
        return price;
    }

    // Setters
    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "PriceUpdate{" +
                "symbol='" + symbol + '\'' +
                ", price='" + price + '\'' +
                '}';
    }
}