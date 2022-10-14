package ee.ut.math.tvt.salessystem;

import ee.ut.math.tvt.salessystem.SalesSystemException;

public class ProductValidationException extends SalesSystemException {

    public ProductValidationException(String message) {
        super(message);
    }
}
