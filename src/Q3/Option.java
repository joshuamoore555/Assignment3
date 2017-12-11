package Q3;

// Do not modify this class
class Option {
    private double value;
    private final double strike;
    private final double deadline;
    private final double volatility;
    private final char type;

    Option( double value, double strike, double deadline, double volatility, char type) {
        this.value = value;
        this.strike = strike;
        this.deadline = deadline;
        this.volatility = volatility;
        this.type = type;
    }

    double getValue() { return value; }

    void setValue( double value_ ) { value = value_; }

    double getStrike() { return strike; }
    double getDeadline() { return deadline; }
    double getVolatility() { return volatility; }
    char getType() { return type; }
}